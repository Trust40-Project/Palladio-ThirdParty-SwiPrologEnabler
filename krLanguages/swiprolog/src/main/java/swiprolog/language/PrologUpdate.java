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
import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

/**
 * See {@link Update}.
 */
public class PrologUpdate extends PrologExpression implements Update {
	
	/**
	 * List of literals that occur positively in the term used to construct this update.
	 */
	private List<DatabaseFormula> positiveLiterals = new ArrayList<DatabaseFormula>();
	/**
	 * List of literals that occur negated in the term used to construct this update.
	 */
	private List<DatabaseFormula> negativeLiterals = new ArrayList<DatabaseFormula>();

	/**
	 * Creates a Prolog {@link Update}.
	 * 
	 * <p>Analyzes the JPL term and separates the positive from the negative
	 * literals to create add and delete lists.</p>
	 * 
	 * @param term A JPL term. Assumes that this term is a conjunction and can be
	 * 			split into a list of conjuncts.
	 */
	public PrologUpdate(jpl.Term term) {
		super(term);

		List<jpl.Term> conjuncts = JPLUtils.getOperands(",", term);

		// Sort positive and negative literals, assuming that each conjunct
		// is a database formula (which should have been checked by the parser).
		for (jpl.Term conjunct : conjuncts) {
			if (JPLUtils.getSignature(conjunct).equals("not/1")) {
				negativeLiterals.add(new PrologDBFormula(conjunct.arg(1)));
			} else if (!JPLUtils.getSignature(conjunct).equals("true/0")) {
				positiveLiterals.add(new PrologDBFormula(conjunct));
			}
		}
	}

	/**
	 * Returns the add list of this update.
	 * 
	 * @return The positive literals that occur in this update.
	 */
	public List<DatabaseFormula> getAddList() {
		return this.positiveLiterals;
	}

	/**
	 * Returns the delete list of this update.
	 * 
	 * @return The negative literals that occur in this update.
	 */
	public List<DatabaseFormula> getDeleteList() {
		return this.negativeLiterals;
	}

	/**
	 * @return Instantiated {@link PrologUpdate} with applied substitution.
	 */
	public PrologUpdate applySubst(Substitution s) {
		Hashtable<String, jpl.Term> jplSubstitution = (s == null) ? null : ((PrologSubstitution) s).getJPLSolution();

		jpl.Term term = JPLUtils.applySubst(jplSubstitution, this.getTerm());
		PrologUpdate update = new PrologUpdate(term);
		update.positiveLiterals = new ArrayList<DatabaseFormula>();
		update.negativeLiterals = new ArrayList<DatabaseFormula>();

		for (DatabaseFormula formula : this.positiveLiterals) {
			update.positiveLiterals.add(formula.applySubst(s));
		}
		for (DatabaseFormula formula : this.negativeLiterals) {
			update.negativeLiterals.add(formula.applySubst(s));
		}

		return update;
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Converts this update into a query, simply using the JPL term of this {@link Update}.
	 * Note that a conjunction of literals can also be used as a query.
	 * 
	 * @return A {@link Query}.
	 */
	public Query toQuery() {
		return new PrologQuery(this.getTerm());
	}

}