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

package swiprolog.language.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologTerm;
import swiprolog.parser.PrologOperators;

/**
 * A Prolog variable.
 */
public class PrologCompoundImpl extends jpl.Compound implements PrologCompound {
	/**
	 * Information about the source used to construct this compound.
	 */
	private final SourceInfo info;
	/**
	 *
	 */
	private List<Term> args;

	/**
	 * Creates a compound with 1 or more arguments.
	 *
	 * @param name
	 *            The compound's name.
	 * @param args
	 *            The arguments.
	 * @param info
	 *            A source info object.
	 */
	public PrologCompoundImpl(String name, Term[] args, SourceInfo info) {
		super(name, jplTypedArray(args));
		this.info = info;
		this.args = Arrays.asList(args);
	}

	private static jpl.Term[] jplTypedArray(Term[] args) {
		jpl.Term[] jpl = new jpl.Term[args.length];
		for (int i = 0; i < args.length; ++i) {
			jpl[i] = (jpl.Term) args[i];
			if (jpl[i] == null) {
				throw new IllegalArgumentException("Null term passed into compound");
			}
		}
		return jpl;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getArity() {
		return this.args.size();
	}

	@Override
	public Term getArg(int i) {
		return this.args.get(i);
	}

	@Override
	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	@Override
	public boolean isPredication() {
		String sig = getSignature();
		return !(sig.equals("&/2") || sig.equals(";/2") || sig.equals("->/2"));
	}

	@Override
	public boolean isPredicateIndicator() {
		return getSignature().equals("//2") && (getArg(0) instanceof PrologAtomImpl)
				&& (getArg(1) instanceof PrologIntImpl);
	}

	@Override
	public boolean isQuery() {
		if (PrologOperators.goalProtected(getName())) {
			// The use of operator in a goal is not supported.
			return false;
		}
		String sig = getSignature();
		if (sig.equals(":-/2")) {
			return false;
		} else if (sig.equals(",/2") || sig.equals(";/2") || sig.equals("->/2")) {
			return ((PrologCompound) getArg(0)).isQuery() && ((PrologCompound) getArg(1)).isQuery();
		} else {
			return true;
		}
	}

	@Override
	public Set<Var> getFreeVar() {
		Set<Var> set = new LinkedHashSet<>();
		for (Term term : this.args) {
			set.addAll(term.getFreeVar());
		}
		return set;
	}

	@Override
	public String getSignature() {
		return this.name + "/" + getArity();
	}

	@Override
	public PrologCompoundImpl applySubst(Substitution s) {
		Term[] instantiatedArgs = new Term[getArity()];
		// Recursively apply the substitution to all sub-terms.
		for (int i = 0; i < getArity(); ++i) {
			instantiatedArgs[i] = getArg(i).applySubst(s);
		}
		return new PrologCompoundImpl(getName(), instantiatedArgs, this.info);
	}

	@Override
	public List<Term> getOperands(String operator) {
		List<Term> list = new LinkedList<>();
		if (getSignature().equals(operator + "/2")) {
			list.add(getArg(0));
			Term next = getArg(1);
			if (next instanceof PrologCompound) {
				list.addAll(((PrologCompound) next).getOperands(operator));
			} else {
				list.add(next);
			}
		} else {
			list.add(this);
		}
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.args == null) ? 0 : this.args.hashCode());
		return result;
	}

	@Override
	public Iterator<Term> iterator() {
		return this.args.iterator();
	}

	@Override
	public String toString() {
		// Special treatment of (non-empty) lists.
		if (getSignature().equals("./2")) {
			return "[" + getArg(0) + tailToString(getArg(1)) + "]";
		} else {
			switch (getFixity()) {
			case FX:
			case FY:
				// if we get here, term is known prefix operator.
				/*
				 * "-" is tight binding in which case the extra brackets are not
				 * needed but :- is not tight binding so there we need brackets.
				 */
				if (this.name.equals("-")) {
					return this.name + maybeBracketed(0);
				} else {
					return this.name + " " + maybeBracketed(0);
				}
			case XFX:
			case XFY:
			case YFX:
				// if we get here, term is a known infix operator
				return maybeBracketed(0) + " " + this.name + " " + maybeBracketed(1);
			case XF:
				// if we get here, term is a known post-fix operator (we don't
				// have any currently)
				return maybeBracketed(0) + " " + this.name + " ";
			default:
				// if we get here, term is not a known operator.
				// use default prefix functional notation.
				String s = getQuotedName() + "(" + maybeBracketedArgument(0);
				for (int i = 1; i < getArity(); i++) {
					s = s + "," + maybeBracketedArgument(i);
				}
				return s + ")";
			}
		}
	}

	/**
	 * ASSUMES the name is not a known operator (eg ';', ':-', etc, see table 5
	 * in ISO 12311). '.' is not an operator.
	 *
	 * @return name, properly single-quoted if necessary. (known operators
	 *         should not be quoted).
	 */
	private String getQuotedName() {
		// simple names starting with lower case char are not quoted
		if (this.name.matches("\\p{Lower}\\w*")) {
			return this.name;
		} else {
			// known operators should not be quoted, but then this should not be
			// called. others are quoted.
			return "'" + this.name + "'";
		}
	}

	/**
	 * Support function for toString that checks if context requires brackets
	 * around term. Converts argument to string and possibly places brackets
	 * around it, if the term has a principal functor whose priority is so high
	 * that the term could not be re-input correctly. Use for operators. see ISO
	 * p.45 part h 2.
	 *
	 * @param argument
	 * @todo Is there a smarter way to do the bracketing? I guess so but then we
	 *       need to determine actual priorities of subtrees.
	 */
	private String maybeBracketed(int argument) {
		PrologTerm arg = (PrologTerm) getArg(argument);
		int argprio = arg.getPriority();
		int ourprio = getPriority();
		if (argprio > ourprio) {
			return "(" + arg + ")";
		} else if (argprio == ourprio) {
			/*
			 * X arguments need brackets for same prio. Y arguments do not need
			 * brackets for same prio. Eg, assume we have an xfy operator here.
			 * The y side can have equal priority by default, and that side can
			 * be printed without brackets. but if the x side has same prio
			 * that's only possible if there were brackets.
			 */
			switch (getFixity()) {
			case FX:
			case XF:
			case XFX:
				return "(" + arg + ")";
			case FY:
				break; // argument can have same level of prio.
			case YFX:
				if (argument == 1) {
					return "(" + arg + ")";
				}
				break;
			case XFY:
				if (argument == 0) {
					return "(" + arg + ")";
				}
				break;
			case NOT_OPERATOR:
				throw new IllegalArgumentException("bug: " + getSignature() + " is not a known operator");
			}
		}
		/*
		 * if we get here, the argument does not need bracketing, either because
		 * it has lower prio or because it has equal prio and the operator
		 * allows that without brackets.
		 */
		return arg.toString();
	}

	/**
	 * Support function for toString that checks if context requires brackets
	 * around term. Checks if argument[argument] needs bracketing for printing.
	 * Arguments inside a predicate are priority 1000. All arguments higher than
	 * that must have been bracketed.
	 *
	 * @param argument
	 * @return bracketed term if required, and without brackets if not needed.
	 */
	private String maybeBracketedArgument(int argument) {
		PrologTerm arg = (PrologTerm) getArg(argument);
		int argprio = arg.getPriority();
		// prio of ','. If we encounter a ","(..) inside arglist we also need
		// brackets.
		if (argprio >= 1000) {
			return "(" + arg + ")";
		} else {
			return arg.toString();
		}
	}

	/**
	 * Support function for toString of a tail of a lists.
	 *
	 * @param argument
	 * @return argument in pretty-printed list form but without "[" or "]"
	 */
	private static String tailToString(Term arg) {
		// Did we reach end of the list?
		// TODO: empty list
		if (arg instanceof PrologAtomImpl && ((PrologAtomImpl) arg).getName().equals("[]")) {
			return "";
		} else if (arg instanceof PrologCompound) {
			// check that we are still in a list and continue.
			if (arg.getSignature().equals("./2")) {
				PrologCompound compound = (PrologCompound) arg;
				return "," + compound.getArg(0) + tailToString(compound.getArg(1));
			} else {
				return "|" + arg; // not a good list.
			}
		} else {
			// If we arrive here the remainder is either a var or not a good
			// list. Finish it off.
			return "|" + arg;
		}
	}
}