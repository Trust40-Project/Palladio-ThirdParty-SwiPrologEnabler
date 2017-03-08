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

import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;

/**
 * A Prolog query.
 */
public class PrologQueryImpl implements PrologQuery {
	/**
	*
	*/
	private final PrologCompound compound;

	/**
	 * Creates a Prolog query.
	 *
	 * <p>
	 * Performs no checks whether the term can be queried on a Prolog database
	 * for efficiency reasons (to avoid checks at run time, e.g., as a result
	 * from applying a substitution). These checks have been delegated to the
	 * parser (to perform checks at compile time only).
	 * </p>
	 *
	 * @param compound
	 *            A compound that can be used as a query.
	 */
	public PrologQueryImpl(PrologCompound compound) {
		this.compound = compound;
	}

	@Override
	public PrologCompound getCompound() {
		return this.compound;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.compound.getSourceInfo();
	}

	@Override
	public PrologQuery applySubst(Substitution substitution) {
		return new PrologQueryImpl((PrologCompound) this.compound.applySubst(substitution));
	}

	@Override
	public boolean isUpdate() {
		return true; // TODO
	}

	/**
	 * ASSUMES the inner prolog term of the query can also be parsed as an
	 * update. If called on (a-)goal literals in the context of a module, this
	 * has already been checked by the parser.
	 */
	@Override
	public Update toUpdate() {
		return new PrologUpdateImpl(this.compound);
	}

	@Override
	public String getSignature() {
		return this.compound.getSignature();
	}

	@Override
	public boolean isClosed() {
		return this.compound.isClosed();
	}

	@Override
	public Set<Var> getFreeVar() {
		return this.compound.getFreeVar();
	}

	@Override
	public Substitution mgu(Expression expression) {
		return this.compound.mgu(expression);
	}
}