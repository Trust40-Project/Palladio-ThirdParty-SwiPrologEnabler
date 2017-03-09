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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologUpdate;

/**
 * See {@link Update}.
 */
public class PrologUpdateImpl implements PrologUpdate {
	/**
	 *
	 */
	private PrologCompound compound;
	/**
	 * List of literals that occur positively in the term used to construct this
	 * update.
	 */
	private List<DatabaseFormula> positiveLiterals = new LinkedList<>();
	/**
	 * List of literals that occur negated in the term used to construct this
	 * update.
	 */
	private List<DatabaseFormula> negativeLiterals = new LinkedList<>();

	/**
	 * Creates a Prolog {@link Update}.
	 *
	 * <p>
	 * Analyzes the JPL term and separates the positive from the negative
	 * literals to create add and delete lists.
	 * </p>
	 *
	 * @param term
	 *            A JPL term. Assumes that this term is a conjunction and can be
	 *            split into a list of conjuncts.
	 * @param info
	 *            A source info object.
	 */
	public PrologUpdateImpl(PrologCompound compound) {
		this.compound = compound;

		// Sort positive and negative literals, assuming that each conjunct
		// is a database formula (which should have been checked by the parser).
		for (Term conjunct : compound.getOperands(",")) {
			if (conjunct.getSignature().equals("not/1")) {
				PrologCompound content = (PrologCompound) ((PrologCompound) conjunct).getArg(0);
				this.negativeLiterals.add(new PrologDBFormulaImpl(content));
			} else {
				this.positiveLiterals.add(new PrologDBFormulaImpl((PrologCompound) conjunct));
			}
		}
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.compound.getSourceInfo();
	}

	/**
	 * Returns the add list of this update.
	 *
	 * @return The positive literals that occur in this update.
	 */
	@Override
	public List<DatabaseFormula> getAddList() {
		return this.positiveLiterals;
	}

	/**
	 * Returns the delete list of this update.
	 *
	 * @return The negative literals that occur in this update.
	 */
	@Override
	public List<DatabaseFormula> getDeleteList() {
		return this.negativeLiterals;
	}

	/**
	 * @return Instantiated {@link PrologUpdate} with applied substitution.
	 */
	@Override
	public PrologUpdate applySubst(Substitution s) {
		PrologCompound term = (PrologCompound) this.compound.applySubst(s);
		PrologUpdateImpl update = new PrologUpdateImpl(term);

		update.positiveLiterals = new ArrayList<>(this.positiveLiterals.size());
		for (DatabaseFormula formula : this.positiveLiterals) {
			update.positiveLiterals.add(formula.applySubst(s));
		}
		update.negativeLiterals = new ArrayList<>(this.negativeLiterals.size());
		for (DatabaseFormula formula : this.negativeLiterals) {
			update.negativeLiterals.add(formula.applySubst(s));
		}

		return update;
	}

	@Override
	public boolean isQuery() {
		return true; // TODO
	}

	/**
	 * Converts this update into a query, simply using the JPL term of this
	 * {@link Update}. Note that a conjunction of literals can also be used as a
	 * query.
	 *
	 * @return A {@link Query}.
	 */
	@Override
	public Query toQuery() {
		return new PrologQueryImpl(this.compound);
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

	@Override
	public String toString() {
		return this.compound.toString();
	}

	@Override
	public int hashCode() {
		return this.compound.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj == null) {
			return true;
		} else if (!(obj instanceof PrologUpdateImpl)) {
			return false;
		}
		PrologUpdateImpl other = (PrologUpdateImpl) obj;
		if (this.compound == null) {
			if (other.compound != null) {
				return false;
			}
		} else if (!this.compound.equals(other.compound)) {
			return false;
		}
		return true;
	}
}