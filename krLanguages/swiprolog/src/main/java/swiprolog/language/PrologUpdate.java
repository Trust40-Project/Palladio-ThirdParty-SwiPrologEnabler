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
 * <p>
 * Implements a GOAL Update object which basically is a STRIPS representation.
 * The STRIPS command is notated in Prolog style using a conjunct
 * (comma-separated list of arguments) of basicstrips or not(basicstrips)
 * objects. basicstrips objects are any legal PrologDBFormula object.
 * </p>
 * <p>
 * It is used both to represent a GOAL (a goal as in the goals section) and to
 * represent mental state change (eg insert and delete)
 * </p>
 * <p>
 * There may be still variables, to be instantiated before update is executed.
 * That is a runtime check required to be done by UpdateEngine. update actions
 * are not allowed to be built-in terms. The update action "true" does not have
 * any effect and is used to represent empty update actions.
 * </p>
 * 
 * <p>
 * All negative parts in the conjunction (the parts of form not(X)) are checked.
 * If of the form not(sent(X,Y)) or not(received(X,Y)) then the part is
 * considered a mailbox update and placed as sent(X,Y) or received(X,Y) in the
 * mailbox update. Otherwise it's placed in the normal delete list.
 * </p>
 * <p>
 * All positive parts in the conjunction are placed in the add list.
 * </p>
 */
public class PrologUpdate extends PrologExpression implements Update {
	
	/**
	 * Positive occurrences of database formulas in original term.
	 */
	private List<DatabaseFormula> positiveOccurrences = new ArrayList<DatabaseFormula>();
	/**
	 * Negative occurrences in original term, excluding those related to the
	 * mailbox.
	 */
	private List<DatabaseFormula> negativeOccurrences = new ArrayList<DatabaseFormula>();

	/**
	 * DOC
	 * 
	 * @param conj
	 *            A JPL Term that consists of a conjunction of positive and
	 *            negative terms.
	 * @param source
	 *            The source code location of this action, if available;
	 *            {@code null} otherwise.
	 */
	public PrologUpdate(jpl.Term conj) {
		// conj might be 1-element conjunction or even null.
		super(conj);

		List<jpl.Term> conjuncts = JPLUtils.getOperands(",", conj);

		// Sort positive and negative literals. Using the fact that each literal
		// is a
		// database formula (checked by parser).
		for (jpl.Term term : conjuncts) {
			if (JPLUtils.getSignature(term).equals("not/1")) {
				negativeOccurrences.add(new PrologDBFormula(term.arg(1)));
			} else {
				if (!JPLUtils.getSignature(term).equals("true/0")) {
					positiveOccurrences.add(new PrologDBFormula(term));
				}
			}
		}
	}

	/**
	 * Returns the add list of this update.
	 * 
	 * @return The positive literals that occur in this update.
	 */
	public List<DatabaseFormula> getAddList() {
		return this.positiveOccurrences;
	}

	/**
	 * Returns the delete list of this update.
	 * 
	 * @return The negative literals that occur in this update.
	 */
	public List<DatabaseFormula> getDeleteList() {
		return this.negativeOccurrences;
	}

	/**
	 * 
	 * @return PrologUpdate with applied substitution. TODO returns null if
	 *         original term was null !
	 */
	public PrologUpdate applySubst(Substitution substitution) {
		Hashtable<String, jpl.Term> solution = ((PrologSubstitution) substitution)
				.getJPLSolution();

		jpl.Term term = JPLUtils.applySubst(solution, this.getTerm());
		PrologUpdate update = new PrologUpdate(term);
		update.positiveOccurrences = new ArrayList<DatabaseFormula>();
		update.negativeOccurrences = new ArrayList<DatabaseFormula>();

		for (DatabaseFormula formula : this.positiveOccurrences) {
			update.positiveOccurrences.add(formula.applySubst(substitution));
		}
		for (DatabaseFormula formula : this.negativeOccurrences) {
			update.negativeOccurrences.add(formula.applySubst(substitution));
		}

		return update;
	}

	/**
	 * Converts this update into a query.
	 * 
	 * @return Query based on the Prolog term that represents this update.
	 */
	public Query toQuery() {
		/*
		 * We removed all checks, to avoid needless checking at runtime.
		 * currently this is only database formula and not a query. We decided
		 * that the compiler will check for all cases where the following cast
		 * will be needed
		 */
		return new PrologQuery(this.getTerm());
	}

}