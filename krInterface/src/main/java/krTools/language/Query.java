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

/**
 * A query is an expression that can be queried on a database for an answer.
 * 
 * <p>The expression should be a formula that may evaluate to true or false, possibly after
 * instantiating any free variables. The corresponding InferenceEngine should
 * support the evaluation of a QueryExpression on a database. Typically, as a
 * result of the evaluation, a substitution - binding free variables in the
 * QueryExpression with terms - may be returned by the InferenceEngine.</p>
 */

public interface Query extends Expression {

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	Query applySubst(Substitution substitution);

	/**
	 * Converts a {@link Query} to an {@link Update}.
	 * 
	 * TODO:
	 * <p>All Mental Literals contain Queries. However goals are represented by
	 * Updates. In order to convert a mental literal into a goal, as is
	 * necessary when instantiating a module, a way to convert a Query into an
	 * Update is needed.</p>
	 * 
	 * @return An Update with an empty delete list and an add list with the
	 *         content of this {@link Query}.
	 */
	Update toUpdate();

}
