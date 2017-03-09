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

import java.util.HashSet;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologVar;

/**
 * A Prolog variable.
 */
public class PrologVarImpl extends jpl.Variable implements PrologVar, Comparable<PrologVar> {
	/**
	 * Information about the source used to construct this variable.
	 */
	private final SourceInfo info;

	/**
	 * Creates a variable.
	 *
	 * @param var
	 *            The variable name
	 * @param info
	 *            A source info object.
	 */
	public PrologVarImpl(String name, SourceInfo info) {
		super(name);
		this.info = info;
	}

	/**
	 * Creates a variable with a random unique name (_ + number).
	 *
	 * @param info
	 *            A source info object.
	 */
	public PrologVarImpl(SourceInfo info) {
		super();
		this.info = info;
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
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isQuery() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		Set<Var> set = new HashSet<>(1);
		set.add(this);
		return set;
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		int n = 1;
		Var newVar;
		do {
			newVar = new PrologVarImpl(this.name + "_" + n, this.info);
			n++;
		} while (usedNames.contains(newVar));

		return newVar;
	}

	@Override
	public String getSignature() {
		return this.name + "/0";
	}

	@Override
	public Term applySubst(Substitution s) {
		Term value = (s == null) ? null : s.get(this);
		return (value == null) ? this : value;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public int compareTo(PrologVar o) {
		return this.name.compareTo(o.getName());
	}
}