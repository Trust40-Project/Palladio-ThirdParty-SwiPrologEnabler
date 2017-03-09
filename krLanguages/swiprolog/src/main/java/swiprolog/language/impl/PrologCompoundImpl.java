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
		return getSignature().equals("/2") && (getArg(0) instanceof PrologAtomImpl)
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
}