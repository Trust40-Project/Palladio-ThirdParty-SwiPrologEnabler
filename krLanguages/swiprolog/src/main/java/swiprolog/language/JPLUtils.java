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
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jpl.Compound;
import jpl.Term;
import jpl.Variable;
import swiprolog.parser.PrologOperators;

/**
 * Utility class for JPL objects.
 *
 * DOC
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
			return Integer.toString(term.intValue()) + "/0";
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
		PrologOperators.Fixity spec = PrologOperators
				.getFixity(getSignature(term));
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
	public static Set<jpl.Variable> getFreeVar(jpl.Term term) {
		Set<Variable> freeVars = new LinkedHashSet<Variable>();

		if (term.isVariable()) {
			freeVars.add((jpl.Variable) term);
		}
		if (term.isCompound()) {
			for (jpl.Term argument : term.args()) {
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
	@SuppressWarnings("rawtypes")
	public static jpl.Term applySubst(Hashtable solution, jpl.Term term) {
		// Cases: Atom, Integer, Float.
		if (term.isAtom() || term.isInteger() || term.isFloat()) {
			return term;
		}
		// Case: Variable
		if (term.isVariable()) {
			Term value = (solution == null) ? null : ((Term) solution.get(term
					.name()));
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
		throw new IllegalArgumentException("Term " + term + "unknown type "
				+ term.getClass());
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
	 * Method for computing the mgu (most general unifier). Tries to instantiate
	 * such that the outterm variables get assigned with otherterm objects.
	 * Built as close as possible to JPL, with the idea that we try to get close
	 * to JPL for efficiency reasons.
	 * <p>
	 * ourterm and otherterm share variable names. So if X gets assigned in
	 * ourterm, that immediately assigns X in the other side.
	 * <p>
	 * The map that we return contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 *
	 * @param thisterm
	 *            is the term that we want to assign variables so that it
	 *            matches the other term
	 * @param otherterm
	 *            is the term that we want to match with
	 * @return hashmap containing a substitution for the left and right hand
	 *         variables if unification is possible, or null if no unification
	 *         is possible.
	 */
	public static Hashtable<String, Term> mgu(jpl.Term thisterm,
			jpl.Term otherterm) {
		Hashtable<String, Term> result = new Hashtable<String, jpl.Term>();

		// First term is a constant.
		if (thisterm.isAtom() || thisterm.isInteger() || thisterm.isFloat()) {
			if (thisterm.equals(otherterm)) {
				return result;
			}
			if (otherterm.isVariable()) {
				result.put(otherterm.name(), thisterm);
				return result;
			}
		}
		// First term is a variable.
		if (thisterm.isVariable()) {
			if (thisterm.name().equals("_")) { // is anonymous
				return result;
			}
			// Occurs check.
			if (JPLUtils.getFreeVar(otherterm).contains(thisterm)) {
				return null;
			}
			result.put(thisterm.name(), otherterm);
			return result;
		}
		// First term is a compound term.
		if (thisterm.isCompound()) {
			if (otherterm.isVariable()) {
				result.put(otherterm.name(), thisterm);
				return result;
			}
			if (otherterm.isCompound()) {
				return mguCompound((Compound) thisterm, (Compound) otherterm);
			}
		}
		return null;
	}

	/**
	 * MGU where left hand is a compound
	 * <p>
	 * The map that we return contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 *
	 * @param thisterm
	 * @return
	 */
	public static Hashtable<String, Term> mguCompound(Compound thisterm,
			Compound otherterm) {
		Hashtable<String, Term> result = new Hashtable<String, jpl.Term>();

		if (!thisterm.name().equals(otherterm.name())) {
			return null;
		}
		if (thisterm.arity() != otherterm.arity()) {
			return null;
		}

		/*
		 * Functions have same number of arguments. Match all the arguments and
		 * merge results. Returns null if no mgu.
		 */
		for (int i = 1; i <= thisterm.arity(); i++) {
			result = combineSubstitutions(result,
					mgu(thisterm.arg(i), otherterm.arg(i)));
			if (result == null) {
				break;
			}
		}
		return result;
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
	protected static Hashtable<String, Term> combineSubstitutions(
			Hashtable<String, Term> thissubst,
			Hashtable<String, Term> othersubst) {
		Hashtable<String, Term> combination = new Hashtable<String, Term>();

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
				Hashtable<String, Term> mgu = mgu(othersubst.get(variable),
						thissubst.get(variable));
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
		List<jpl.Term> list = new ArrayList<jpl.Term>();

		if (term.isCompound() && term.name().equals(operator)
				&& term.arity() == 2) {
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
	 * Returns a hash code for the JPL term.
	 *
	 * @param term
	 *            A JPL term.
	 * @return A hash code for the term.
	 */
	public static int hashCode(jpl.Term term) {
		if (term instanceof jpl.Compound) {
			jpl.Compound compound = (jpl.Compound) term;
			ArrayList<Integer> args = new ArrayList<Integer>();
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
			return Integer.toString(((jpl.Integer) term).intValue()).hashCode();
		}
		if (term instanceof jpl.Float) {
			return Float.toString(((jpl.Float) term).floatValue()).hashCode();
		}
		// we're not using anything else.
		throw new UnsupportedOperationException("Hashcode of JPL term " + term
				+ " could not be computed.");
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
		if (term1 instanceof jpl.Atom) {
			return ((jpl.Atom) term1).name().equals((term2));
		}
		if (term1 instanceof jpl.Variable) {
			return ((jpl.Variable) term1).name().equals((term2));
		}
		if (term1 instanceof jpl.Integer) {
			return ((jpl.Integer) term1).intValue() == ((jpl.Integer) term2)
					.intValue();
		}
		if (term1 instanceof jpl.Float) {
			return ((jpl.Float) term1).floatValue() == ((jpl.Float) term2)
					.floatValue();
		}
		// we're not using anything else.
		throw new UnsupportedOperationException("Equals for JPL terms " + term1
				+ " and " + term2 + " is not defined.");
	}

}