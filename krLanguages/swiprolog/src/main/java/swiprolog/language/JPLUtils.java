/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package swiprolog.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpl.Compound;
import jpl.Term;
import jpl.Variable;
import swiprolog.parser.PrologOperators;

/**
 * Utility class for JPL objects.
 */
public class JPLUtils {

	/**
	 * Returns the signature of the JPL term.
	 * <p>
	 * A signature is of the form "<op>/<#arg>" where <op> is the main operator
	 * of the term and <#arg> the number of arguments that the operator expects.
	 * For example, the signature of the 'member(X, L)' predicate is "member/2".
	 * </p>
	 * <p>
	 * The signatures of integers and floats are defined here as "<value>/0" and
	 * the signature of a variable "X" is defined as "X/0".
	 * </p>
	 *
	 * @return The signature of the term.
	 */
	public static String getSignature(jpl.Term term) {
		if (term.isInteger()) { // does not support name() method
			// use the longValue, #3399
			return Long.toString(term.longValue()) + "/0";
		} else if (term.isFloat()) { // does not support name() method
			return Float.toString(term.floatValue()) + "/0";
		} else if (term.isVariable()) { // does not support arity() method
			return term.name() + "/0";
		} else {
			return term.name() + "/" + term.arity();
		}
	}

	/**
	 *
	 *
	 * @return The F-ixity of the term: returns NOT_OPERATOR for non-operator
	 *         terms.
	 * @see PrologOperators.Fixity for a list of f-ixities.
	 */
	public static PrologOperators.Fixity getFixity(jpl.Term term) {
		PrologOperators.Fixity spec = PrologOperators.getFixity(getSignature(term));
		if (spec == null) {
			return PrologOperators.Fixity.NOT_OPERATOR;
		}
		return spec;
	}

	/**
	 * Returns the priority of the main operator of the term.
	 *
	 * @return The priority of the term's operator. Default is 0.
	 */
	public static int getPriority(jpl.Term term) {
		Integer prio = PrologOperators.getPriority(getSignature(term));
		if (prio == null) {
			return 0;
		}
		return prio;
	}

	/**
	 * Returns the (free) variables that occur in the term.
	 *
	 * @param term
	 *            The term whose variables are returned.
	 * @return The variables that occur in the term.
	 */
	public static Set<Variable> getFreeVar(Term term) {
		SetWithoutHash<Variable> freeVars = new SetWithoutHash<Variable>();

		if (term.isVariable()) {
			freeVars.add((jpl.Variable) term);
		}
		if (term.isCompound()) {
			for (Term argument : term.args()) {
				freeVars.addAll(getFreeVar(argument));
			}
		}

		return freeVars;
	}

	/**
	 * Creates a new term, cloning the given term and substituting variables
	 * where applicable. JPL does not provide this.
	 * <p>
	 * Variables that are not in the substi list will be left untouched.
	 * <p>
	 * There is a {jpl.Term#getSubst} function but it seems to do something
	 * else.
	 *
	 * @param solution
	 *            is a map String,{@link jpl.Term} pairs. String is the name of
	 *            the var to be substitued with Term. indicating that all
	 *            occurences of variable have to be replaced with a term.
	 * @param term
	 *            is the term on which to apply the substi. The term t will be
	 *            left untouched
	 * @return a copy of t but with all occurences of variables substituted as
	 *         indicated in the substi map.
	 */
	public static jpl.Term applySubst(Map<String, Term> solution, jpl.Term term) {
		// Cases: Atom, Integer, Float.
		if (term.isAtom() || term.isInteger() || term.isFloat()) {
			return term;
		}
		// Case: Variable
		if (term.isVariable()) {
			Term value = (solution == null) ? null : ((Term) solution.get(term.name()));
			if (value == null) {
				return term;
			}
			return value;
		}
		// Case: Compound
		if (term.isCompound()) {
			jpl.Term[] instantiatedArgs = new Term[term.args().length];
			// Recursively apply the substitution to all sub-terms.
			for (int i = 1; i <= term.args().length; i++) {
				instantiatedArgs[i - 1] = applySubst(solution, term.arg(i));
			}
			return new jpl.Compound(term.name(), instantiatedArgs);
		}
		throw new IllegalArgumentException("term ' " + term + "' is of an unknown type.");
	}

	/**
	 * Checks whether term can be used as query, i.e., that term is a well
	 * formed Prolog goal.
	 * <p>
	 * ISO requires rebuild of the term but in our case we do not allow
	 * variables and hence a real rebuild is not necessary. Instead, we simply
	 * return the original term after checking.
	 * </p>
	 *
	 * @return {@code true} if term is a Prolog goal according to ISO.
	 */
	public static boolean isQuery(jpl.Term t) {
		// 7.6.2.a use article 7.8.3
		if (t.isVariable()) {
			// Variables cannot be used as goals
			return false;
		}
		// 7.6.2.b
		String sig = JPLUtils.getSignature(t);
		if (PrologOperators.goalProtected(t.name())) {
			// The use of operator in a goal is not supported.
			return false;
		}
		if (sig.equals(":-/2")) {
			return false;
		}
		if (sig.equals(",/2") || sig.equals(";/2") || sig.equals("->/2")) {
			isQuery(t.arg(1));
			isQuery(t.arg(2));
		}
		// 7.6.2.c
		// no action required.
		return true;
	}

	/**
	 * D-is-a-predication in ISO p.132-.
	 */
	public static boolean isPredication(jpl.Term term) {
		// only compound has concrete implementation of name(). #3463
		if (!(term instanceof jpl.Compound)) {
			return false;
		}

		// CHECK this seems nonsense, jpl.Term objects are not String objects?
		if (term.equals("&")) {
			return false;
		}
		if (term.equals(";")) {
			return false;
		}
		if (term.equals("->")) {
			return false;
		}

		/*
		 * Arguments must be a D-is-an-arglist see ISO p.132 but all arguments
		 * must already be PrologTerm and no further checks are needed. TODO
		 * handle special D-is-a-predication cases. E.g., dewey numbers, and
		 * other special cases.
		 */
		return PrologOperators.is_L_atom(term.name());
	}

	/**
	 * Try to combine two substitutions into a new substi.
	 * <p>
	 * The map that we return contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 *
	 * @param subst
	 * @param newsubst
	 * @return combined subst, or null if they can not be combined (variable
	 *         conflict)
	 */
	protected static Map<String, Term> combineSubstitutions(Map<String, Term> thissubst, Map<String, Term> othersubst) {
		Map<String, Term> combination = new Hashtable<>();

		// Combining with {@code null}, i.e., failure, yields a failure {@code
		// null}.
		if (othersubst == null) {
			return null;
		}

		// Apply the parameter substitution to this substitution.
		Set<String> domain = thissubst.keySet();
		for (String variable : domain) {
			// Add binding for variable to term obtained by applying the
			// parameter substitution
			// to the original term.
			Term term = applySubst(othersubst, thissubst.get(variable));
			// Add binding if resulting term is not equal to variable.
			if (!(new jpl.Variable(variable)).equals(term)) {
				combination.put(variable, term);
			} else {
				combination.put(variable, thissubst.get(variable));
			}
		}
		// Add the bindings of the parameter substitution for variables that are
		// not in the
		// domain of this substitution; otherwise check for inconsistencies.
		for (String variable : othersubst.keySet()) {
			if (!domain.contains(variable)) {
				Term term = applySubst(combination, othersubst.get(variable));
				if (!(new jpl.Variable(variable)).equals(term)) {
					combination.put(variable, term);
				}
			} else { // two bindings for one and the same variable.
				// Check whether terms can be unified
				Map<String, Term> mgu = mgu(othersubst.get(variable), thissubst.get(variable));
				if (mgu != null) {
					combination = combineSubstitutions(combination, mgu);
				} else { // fail: two different bindings for one and the same
					// variable.
					combination = null;
					break;
				}
			}
		}

		return combination;
	}

	/**
	 * Returns the operands of a (repeatedly used) right associative binary
	 * operator.
	 * <p>
	 * Can be used, for example, to get the conjuncts of a conjunction or the
	 * elements of a list. Note that the <i>second</i> conjunct or element in a
	 * list concatenation can be a conjunct or list itself again.
	 * </p>
	 * <p>
	 * A list (term) of the form '.'(a,'.'(b,'.'(c, []))), for example, returns
	 * the elements a, b, c, <i>and</i> the empty list []. A conjunction of the
	 * form ','(e0,','(e1,','(e2...((...,en)))...) returns the list of conjuncts
	 * e0, e1, e2, etc.
	 * </p>
	 *
	 * @param operator
	 *            The binary operator.
	 * @param term
	 *            The term to be unraveled.
	 * @return A list of operands.
	 */
	public static List<jpl.Term> getOperands(String operator, jpl.Term term) {
		List<jpl.Term> list = new LinkedList<>();
		if (term.isCompound() && term.name().equals(operator) && term.arity() == 2) {
			list.add(term.arg(1));
			list.addAll(getOperands(operator, term.arg(2)));
		} else {
			list.add(term);
		}
		return list;
	}

	/**
	 * Returns a (possibly empty) Prolog list with the given terms as elements
	 * of the list.
	 *
	 * @param terms
	 *            The elements to be included in the list.
	 * @return A Prolog list using the "." and "[]" list constructors.
	 */
	public static jpl.Term termsToList(List<jpl.Term> terms) {
		// Start with element in list, since innermost term of Prolog list is
		// the last term.
		jpl.Term list = new jpl.Atom("[]");
		for (int i = terms.size() - 1; i >= 0; i--) {
			list = new Compound(".", new Term[] { terms.get(i), list });
		}
		return list;
	}

	/**
	 * Returns a (possibly empty) conjunct containing the given terms.
	 *
	 * @param newterm
	 * @return possibly empty conjunct containing the given terms
	 */
	public static Term termsToConjunct(List<Term> terms) {
		if (terms.isEmpty()) {
			return new jpl.Atom("true");
		}
		// build up list last to first.
		jpl.Term list = terms.get(terms.size() - 1);// last
		for (int i = terms.size() - 2; i >= 0; i--) {
			list = new Compound(",", new Term[] { terms.get(i), list });
		}
		return list;
	}

	/**
	 * Creates a JPL compound term.
	 *
	 * @param operator
	 * @param args
	 * @return
	 */
	public static jpl.Term createCompound(String operator, jpl.Term... args) {
		return new jpl.Compound(operator, args);
	}

	/**
	 * Workaround for bug in jpl #3399. Create float for large integers.
	 *
	 * @param number
	 *            long number
	 * @return term representing the long.
	 */
	public static jpl.Term createIntegerNumber(long number) {
		// int or long. Check if it fits
		if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
			System.out.println(
					"SwiPrologMentalState: Warning: Converting large integer number coming from environment to floating point");
			return new jpl.Float(number);
		}
		return new jpl.Integer(number);
	}

	/**
	 * Returns a hash code for the JPL term.
	 *
	 * @param term
	 *            A JPL term.
	 * @return A hash code for the term.
	 */
	public static int hashCode(jpl.Term term) {
		if (term instanceof jpl.Compound) {
			jpl.Compound compound = (jpl.Compound) term;
			List<Integer> args = new ArrayList<>(compound.arity() + 1);
			args.add(compound.name().hashCode());
			for (int i = 1; i <= compound.arity(); i++) {
				args.add(hashCode(compound.arg(i)));
			}
			return args.hashCode();
		}
		if (term instanceof jpl.Atom) {
			return ((jpl.Atom) term).name().hashCode();
		}
		if (term instanceof jpl.Variable) {
			return ((jpl.Variable) term).name().hashCode();
		}
		if (term instanceof jpl.Integer) {
			// use the longValue, #3399
			return Long.toString(((jpl.Integer) term).longValue()).hashCode();
		}
		if (term instanceof jpl.Float) {
			return Float.toString(((jpl.Float) term).floatValue()).hashCode();
		}
		// we're not using anything else.
		throw new UnsupportedOperationException("the hashcode of '" + term + "' could not be computed.");
	}

	/**
	 * Checks whether two JPL terms are equal.
	 *
	 * @param term1
	 *            A JPL term.
	 * @param term2
	 *            A JPL term.
	 * @return {@code true} if both terms are equal, i.e., they represent the
	 *         same term.
	 */
	// TODO: needs careful review and checking...
	public static boolean equals(jpl.Term term1, jpl.Term term2) {
		assert (term1 != null);
		assert (term2 != null);

		if (term1 == term2) {
			return true;
		}
		if (term1.getClass() != term2.getClass()) {
			return false;
		}
		if (term1 instanceof jpl.Compound) {
			jpl.Compound compound1 = (jpl.Compound) term1;
			jpl.Compound compound2 = (jpl.Compound) term2;
			if (!compound1.name().equals(compound2.name())) {
				return false;
			}
			if (compound1.arity() != compound2.arity()) {
				return false;
			}
			for (int i = 1; i <= compound1.arity(); i++) {
				if (!equals(compound1.arg(i), compound2.arg(i))) {
					return false;
				}
			}
			return true;
		}
		if (term1 instanceof jpl.Atom || term1 instanceof Variable) {
			return term1.name().equals(term2.name());
		}
		if (term1 instanceof jpl.Integer) {
			// compare longs, #3399
			return ((jpl.Integer) term1).longValue() == ((jpl.Integer) term2).longValue();
		}
		if (term1 instanceof jpl.Float) {
			return ((jpl.Float) term1).floatValue() == ((jpl.Float) term2).floatValue();
		}
		// we're not using anything else.
		throw new UnsupportedOperationException("equals for '" + term1 + "' and '" + term2 + "' is not defined.");
	}

	/**
	 * Convert a {@link Term} to a pretty printed string.
	 *
	 * @param term
	 *            the term to print
	 * @return pretty printed term.
	 */
	public static String toString(Term term) {
		if (term.isAtom()) {
			return term.toString();
		}

		if (term.isVariable()) {
			return term.name();
		}

		if (term.isInteger()) {
			return Long.toString(term.longValue());
		}

		if (term.isFloat()) { // actually a double.
			return Double.toString(term.doubleValue());
		}

		if (term.isCompound()) {
			/**
			 * Special treatment of (non-empty) lists.
			 */
			if (term.name().equals(".") && term.arity() == 2) {
				return "[" + term.arg(1) + tailToString(term.arg(2)) + "]";
			}

			switch (JPLUtils.getFixity(term)) {
			case FX:
			case FY:
				/*
				 * "-" is tight binding in which case the extra brackets are not
				 * needed but the :- is not tight binding so there we need
				 * brackets.
				 */
				// SWI bug workaround. Mantis 280
				if (term.name().equals("-")) {
					return term.name() + maybeBracketed(term, 1);
				}
				return term.name() + " " + maybeBracketed(term, 1);
			case XFX:
			case XFY:
			case YFX:
				return maybeBracketed(term, 1) + " " + term.name() + " " + maybeBracketed(term, 2);
			case XF:
				return maybeBracketed(term, 1) + " " + term.name() + " ";
			default:
				/**
				 * Default: return functional notation (canonical form).
				 */
				String s = term.name() + "(" + maybeBracketedArgument(term, 1);
				for (int i = 2; i <= term.arity(); i++) {
					s = s + "," + maybeBracketedArgument(term, i);
				}
				s = s + ")";
				return s;
			}
		}

		// Don't know what this is; throw.
		throw new UnsupportedOperationException("unknown term '" + term + "'.");
	}

	/**
	 * TODO is there a smarter way to do the bracketing? I guess so but then we
	 * need to determine actual priorities of subtrees.
	 */
	/**
	 * Support function for toString that checks if context requires brackets
	 * around term. Converts argument to string and possibly places brackets
	 * around it, if the term has a principal functor whose priority is so high
	 * that the term could not be re-input correctly. Use for operators. see ISO
	 * p.45 part h 2.
	 *
	 * @param term
	 *            the Term
	 * @param argument
	 *            Either 1 or 2 to indicate JPL argument.
	 */
	private static String maybeBracketed(Term term, int argument) {
		jpl.Term arg = term.arg(argument);
		PrologExpression argexpression = new PrologTerm(arg, null);
		int argprio = JPLUtils.getPriority(arg);
		int ourprio = JPLUtils.getPriority(term);

		if (argprio > ourprio) {
			return "(" + argexpression.toString() + ")";
		}
		if (argprio == ourprio) {
			// let's say we have an xfy operator here. The y side can have equal
			// priority by default,
			// and that side can be printed without brackets.
			// but if the x side has same prio that's only possible if there
			// were brackets.
			PrologOperators.Fixity spec = JPLUtils.getFixity(term);
			if (spec == null) {
				return argexpression.toString(); // no spec, no op.
			}
			switch (spec) {
			case FX: // args without Y need brackets anyway
			case XF:
			case XFX:
				return "(" + argexpression.toString() + ")";
			case YFX:
				if (argument == 2) {
					return "(" + argexpression.toString() + ")";
				}
				break;
			case XFY:
				if (argument == 1) {
					return "(" + argexpression.toString() + ")";
				}
				break;
			default:
				//
			}
		}

		return argexpression.toString();
	}

	/**
	 * Support function for toString that checks if context requires brackets
	 * around term. Checks if argument[argument] needs bracketing for printing.
	 * Arguments inside a predicate are priority 1000. All arguments higher than
	 * that must have been bracketed.
	 *
	 * @param term
	 *            the {@link Term} to convert
	 * @param argument
	 *            Either 1 or 2 to indicate JPL argument.
	 * @return bracketed term if required, and without brackets if not needed.
	 */
	private static String maybeBracketedArgument(Term term, int argument) {
		jpl.Term arg = term.arg(argument);
		int argprio = JPLUtils.getPriority(arg);

		// prio of ','. If we encounter a ","(..) inside arglist we also need
		// brackets.
		if (argprio >= 1000) {
			return "(" + arg + ")";
		}

		PrologExpression expression = new PrologTerm(arg, null);
		return expression.toString();
	}

	/**
	 * Support function for toString of a tail of a lists.
	 *
	 * @param term
	 *            is a Prolog term that is part of a list.
	 * @return given term t in pretty-printed list form but without "[" or "]"
	 */
	private static String tailToString(Term term) {
		// Did we reach end of the list?
		// TODO: empty list
		if (term.isAtom() && term.name().equals("[]")) {
			return "";
		}

		// check that we are still in a list and continue.
		if (term.isCompound()) {
			jpl.Term[] args = term.args();
			if (!(term.name().equals(".")) || args.length != 2) {
				return "|" + term; // no good list.
			}
			return "," + args[0] + tailToString(args[1]);
		}

		// If we arrive here the remainder is either a var or not a good list.
		// Finish it off.
		return "|" + term;
	}

	/**
	 * Create most general unifier (mgu) of two terms. The returned most general
	 * unifier is a substitution which, if applied to both terms, will make the
	 * terms equal. This algorithm currently will first try to set variables in
	 * x. So it has a bias towards filling in the x variables over filling in y
	 * variables. <br>
	 * The map that is returned contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 *
	 * @param x
	 *            first term
	 * @param y
	 *            second term
	 * @return mgu, or null if no mgu exists.
	 */
	public static Map<String, Term> mgu(jpl.Term x, jpl.Term y) {
		return unify(x, y, new HashMap<String, Term>(0));
	}

	/**
	 * Textbook implementation of Unify algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace. <br>
	 * This textbook implementation has a bias towards assigning variables in
	 * the left hand term. <br>
	 * The returned map contains String as key objects, because jpl.Variable can
	 * not be used for key (as it does not implement hashCode).
	 *
	 * @param x
	 *            the first term.
	 * @param y
	 *            the second term.
	 * @param s
	 *            the substitutions used so far.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	public static Map<String, Term> unify(jpl.Term x, jpl.Term y, Map<String, Term> s) {
		if (s == null) {
			return null;
		}
		if (equals(x, y)) {
			return s;
		}
		if (x.isVariable()) {
			return unifyVar((Variable) x, y, s);
		}
		if (y.isVariable()) {
			return unifyVar((Variable) y, x, s);
		}
		if (x.isCompound() && y.isCompound()) {
			return unifyCompounds((Compound) x, (Compound) y, s);
		}
		// we do not have lists. List is just another compound.
		return null;
	}

	/**
	 * Unify 2 {@link Compound}s. Implements the bit vague element in the
	 * textbook UNIFY(x.ARGS, y.ARGS, UNIFY(x.OP, y.OP,s)).
	 *
	 * @param x
	 *            the first {@link Compound}
	 * @param y
	 *            The second {@link Compound}
	 * @param s
	 *            the substitutions used so far. s must not be null.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	private static Map<String, Term> unifyCompounds(Compound x, Compound y, Map<String, Term> s) {
		if (x.arity() != y.arity()) {
			return null;
		}
		if (!x.name().equals(y.name())) {
			return null;
		}
		for (int i = 1; i <= x.arity(); i++) {
			s = unify(x.arg(i), y.arg(i), s);
		}
		return s;
	}

	/**
	 * Textbook implementation of Unify-Var algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition. unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace.
	 *
	 * @param var
	 *            the {@link jpl.Variable}.
	 * @param y
	 *            the {@link Term}.
	 * @param s
	 *            the substitutions used so far. Must not be null. This set can
	 *            be modified by this function.
	 *
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */

	private static Map<String, Term> unifyVar(Variable var, Term x, Map<String, Term> s) {
		if (s.containsKey(var.name)) {
			return unify(s.get(var.name), x, s);
		}
		if (x.isVariable() && s.containsKey(((Variable) x).name)) {
			return unify(var, s.get(((Variable) x).name), s);
		}
		if (getFreeVar(x).contains(var)) {
			return null;
		}
		s.put(var.name, x);
		return s;
	}
}
