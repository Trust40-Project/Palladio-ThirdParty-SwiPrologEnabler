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
import swiprolog.language.PrologTerm;

/**
 * A Prolog integer.
 */
public class PrologIntImpl extends jpl.Integer implements PrologTerm {
	/**
	 * Information about the source used to construct this integer.
	 */
	private final SourceInfo info;

	/**
	 * Creates an integer (actually a long).
	 *
	 * @param value
	 *            The integer value.
	 * @param info
	 *            A source info object.
	 */
	public PrologIntImpl(long value, SourceInfo info) {
		super(value);
		this.info = info;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public boolean isClosed() {
		return true;
	}

	@Override
	public boolean isQuery() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		return new HashSet<>(0);
	}

	@Override
	public String getSignature() {
		return this.value + "/0";
	}

	@Override
	public Term applySubst(Substitution s) {
		return this;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.value);
	}
}