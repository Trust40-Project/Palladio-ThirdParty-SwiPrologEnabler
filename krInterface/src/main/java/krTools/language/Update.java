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

package krTools.language;

import java.util.List;

/**
 * An update is an expression that can be used to update a database.
 * 
 * <p>Updates are
 * also used to represent goals, but goals are updates with empty delete and
 * mailbox update.</p>
 * 
 * <p>It is assumed that an update is a conjunction of more basic updates. Basic
 * updates are either positive or negative literals (i.e. a formula), or
 * possibly another type of update such as a conditional effect in ADL. Updates
 * are related to DatabaseFormula as database formulas are assumed to be the
 * most basic updates possible (such as a positive literal). The UpdateEngine
 * should be able to handle an Update and should possess the know-how to update
 * a database with it. An update may have free variables as any other logical
 * expression.</p>
 * 
 * <p>See Trac #775. This is now a collection of positive and negative updates.
 * There is no separation anymore between beliefbase and mailbox updates. All
 * semantic checking is now left to the parser as we do not know at this point
 * what the context is (insert, delete, mailbox actions allowed, etc). This
 * separation is made when necessary, in InsertAction and DeleteAction.
 * InsertAction will insert positives and delete negatives. DeleteAction will
 * delete positives and insert negatives. We always first apply the delete and
 * then the insert.</p>
 */
public interface Update extends Expression {

	/**
	 * Returns the add list of this {@link Update} in the form of a list of
	 * {@link DatabaseFormula}s. The formulas returned are positive literals.
	 * 
	 * @return The add list of this update, i.e. the positive literals
	 * 		that occur in this update.
	 */
	List<DatabaseFormula> getAddList();

	/**
	 * Returns the delete list of this {@link Update} in the form of a list of
	 * {@link DatabaseFormula}s. The formulas returned are positive literals.
	 * 
	 * @return The delete list of this update, i.e. the negative literals
	 * 		that occur in this update (but the negations are removed in the
	 * 		list that is returned).
	 */
	List<DatabaseFormula> getDeleteList();

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	Update applySubst(Substitution substitution);

	/**
	 * Converts an update to a query.
	 * 
	 * <p>A goal in GOAL is inserted in a database, and is also queried on the
	 * belief base. As goals are represented as updates, we need a method to
	 * convert them to a query to be able to use a goal as a query.</p>
	 * 
	 * @return query formula that is result from converting update, using the
	 *         add-list associated with the update only. Used for goals. See
	 *         Goalbase.java.
	 */
	Query toQuery();

}
