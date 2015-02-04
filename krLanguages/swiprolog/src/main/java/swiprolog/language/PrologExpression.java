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
import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.parser.PrologOperators;

/**
 * DOC
 */
public abstract class PrologExpression implements Expression {

	/**
	 * A JPL term representing a Prolog expression.
	 */
	private final jpl.Term term;

	/**
	 * Information about the source used to construct this expression.
	 */
	private final SourceInfo info;

	/**
	 * Creates a Prolog expression.
	 *
	 * @term A JPL term.
	 */
	public PrologExpression(jpl.Term term, SourceInfo info) {
		this.term = term;
		this.info = info;
	}

	/**
	 * Returns the JPL term.
	 *
	 * @return A {@link jpl.Term}.
	 */
	public jpl.Term getTerm() {
		return this.term;
	}

	/**
	 * @return A {@link SourceInfo} object with information about the source
	 *         used to construct this expression.
	 */
	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	/**
	 * Checks whether this expression is a variables.
	 *
	 * @return {@code true} if this expression is a variable; {@code false}
	 *         otherwise.
	 */
	@Override
	public boolean isVar() {
		return this.getTerm().isVariable();
	}

	/**
	 * Returns the (free) variables that occur in this expression.
	 *
	 * @return The (free) variables that occur in this expression.
	 */
	@Override
	public Set<Var> getFreeVar() {
		ArrayList<jpl.Variable> jplvars = new ArrayList<jpl.Variable>(
				JPLUtils.getFreeVar(this.getTerm()));
		Set<Var> variables = new LinkedHashSet<Var>();

		// Build VariableTerm from jpl.Variable.
		for (jpl.Variable var : jplvars) {
			variables.add(new PrologVar(var, getSourceInfo()));
		}

		return variables;
	}

	/**
	 * Checks whether this expression is closed, i.e., has no occurrences of
	 * (free) variables.
	 *
	 * @return {@code true} if this expression is closed.
	 */
	@Override
	public boolean isClosed() {
		return JPLUtils.getFreeVar(this.getTerm()).isEmpty();
	}

	/**
	 * Returns a most general unifier, if it exists, that unifies this and the
	 * given expression.
	 *
	 * @return A unifier for this and the given expression, if it exists;
	 *         {@code null} otherwise.
	 */
	@Override
	public Substitution mgu(Expression expression) {
		jpl.Term otherterm = ((PrologExpression) expression).getTerm();
		return PrologSubstitution.getSubstitutionOrNull(JPLUtils.mgu(
				this.getTerm(), otherterm));
	}

	/**
	 * Returns the signature of this expression.
	 * <p>
	 * Signature is funcname+"/"+#arguments, eg "member/2". default is
	 * mainoperator+"/"+arity so you do not have ot override this. Note that
	 * signature of a variable is set to X/0.
	 * </p>
	 *
	 * @return The signature of this Prolog expression.
	 */
	@Override
	public String getSignature() {
		return JPLUtils.getSignature(this.term);
	}

	/**
	 *
	 */
	public boolean isEmpty() {
		return this.getSignature().equals("true/0");
	}

	@Override
	public String toString() {
		if (this.term.isAtom()) {
			return this.term.toString();
		}

		if (this.term.isVariable()) {
			return this.term.name();
		}

		if (this.term.isInteger()) {
			return Long.toString(this.term.longValue());
		}

		if (this.term.isFloat()) {
			return Float.toString(this.term.floatValue());
		}

		if (this.term.isCompound()) {
			/**
			 * Special treatment of (non-empty) lists.
			 */
			if (this.term.name().equals(".") && this.term.arity() == 2) {
				return "[" + this.term.arg(1) + tailToString(this.term.arg(2))
						+ "]";
			}

			switch (JPLUtils.getFixity(this.term)) {
			case FX:
			case FY:
				/*
				 * "-" is tight binding in which case the extra brackets are not
				 * needed but the :- is not tight binding so there we need
				 * brackets.
				 */
				// SWI bug workaround. Mantis 280
				if (this.term.name().equals("-")) {
					return this.term.name() + maybeBracketed(1);
				}
				return this.term.name() + " " + maybeBracketed(1);
			case XFX:
			case XFY:
			case YFX:
				return maybeBracketed(1) + " " + this.term.name() + " "
						+ maybeBracketed(2);
			case XF:
				return maybeBracketed(1) + " " + this.term.name() + " ";
			default:
				/**
				 * Default: return functional notation (canonical form).
				 */
				String s = this.term.name() + "(" + maybeBracketedArgument(1);
				for (int i = 2; i <= this.term.arity(); i++) {
					s = s + "," + maybeBracketedArgument(i);
				}
				s = s + ")";
				return s;
			}
		}

		// Don't know what this is; throw.
		throw new UnsupportedOperationException("Unknown JPL term of type "
				+ this.term.getClass());
	}

	/**
	 * Support function for toString of a tail of a lists.
	 *
	 * @param term
	 *            is a Prolog term that is part of a list.
	 * @return given term t in pretty-printed list form but without "[" or "]"
	 */
	private String tailToString(jpl.Term term) {
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
	 * @param argument
	 *            Either 1 or 2 to indicate JPL argument.
	 */
	private String maybeBracketed(int argument) {
		jpl.Term arg = this.term.arg(argument);
		PrologExpression argexpression = new PrologTerm(arg, null);
		int argprio = JPLUtils.getPriority(arg);
		int ourprio = JPLUtils.getPriority(this.term);

		if (argprio > ourprio) {
			return "(" + argexpression.toString() + ")";
		}
		if (argprio == ourprio) {
			// let's say we have an xfy operator here. The y side can have equal
			// priority by default,
			// and that side can be printed without brackets.
			// but if the x side has same prio that's only possible if there
			// were brackets.
			// System.out.println("getting spec of "+label+"/"+arguments.size()+"="+GetSpec());
			// System.out.println("prio of arg "+argumentnumber+" "+arg+" = "+argprio);
			PrologOperators.Fixity spec = JPLUtils.getFixity(this.term);
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
	 * @return bracketed term if required, and without brackets if not needed.
	 */
	private String maybeBracketedArgument(int argument) {
		jpl.Term arg = this.term.arg(argument);
		int argprio = JPLUtils.getPriority(arg);

		// prio of ','. If we encounter a ","(..) inside arglist we also need
		// brackets.
		if (argprio >= 1000) {
			return "(" + arg + ")";
		}

		PrologExpression expression = new PrologTerm(arg, null);
		return expression.toString();
	}

	@Override
	public int hashCode() {
		return JPLUtils.hashCode(getTerm());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PrologExpression other = (PrologExpression) obj;
		if (this.term == null) {
			if (other.term != null) {
				return false;
			}
		} // JPL does not implement equals...
		else if (!JPLUtils.equals(this.term, other.term)) {
			return false;
		}
		return true;
	}

}