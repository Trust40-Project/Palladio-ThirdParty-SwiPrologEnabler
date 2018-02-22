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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
public class PrologAtomImpl extends org.jpl7.Atom implements PrologCompound {
	/**
	 * Information about the source used to construct this atom.
	 */
	private final SourceInfo info;

	/**
	 * Creates an atom (i.e. a compound without arguments).
	 *
	 * @param name
	 *            The atom's name
	 * @param info
	 *            A source info object.
	 */
	public PrologAtomImpl(String name, SourceInfo info) {
		super(name);
		this.info = info;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public Term getArg(int i) {
		return null;
	}

	@Override
	public boolean isClosed() {
		return true;
	}

	@Override
	public boolean isPredication() {
		return PrologOperators.is_L_atom(this.name);
	}

	@Override
	public boolean isPredicateIndicator() {
		return false;
	}

	@Override
	public boolean isQuery() {
		return true; // FIXME
	}

	@Override
	public Set<Var> getFreeVar() {
		return new HashSet<>(0);
	}

	@Override
	public String getSignature() {
		return this.name + "/0";
	}

	@Override
	public List<Term> getOperands(String operator) {
		List<Term> list = new ArrayList<>(1);
		list.add(this);
		return list;
	}

	@Override
	public PrologAtomImpl applySubst(Substitution s) {
		return this;
	}

	@Override
	public int hashCode() {
		return (this.name == null) ? 0 : this.name.hashCode();
	}

	@Override
	public Iterator<Term> iterator() {
		return getOperands(null).iterator();
	}
}