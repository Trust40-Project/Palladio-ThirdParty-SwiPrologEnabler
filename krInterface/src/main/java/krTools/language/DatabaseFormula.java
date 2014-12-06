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

import krTools.database.Database;

/**
 * A database formula is an expression that can be inserted into a
 * {@link Database}.
 *
 * <p>
 * Examples of database formulas, of course, differ from one KR language to
 * another. In Prolog clauses of the form {@code p :- q} and {@code p} where
 * {@code p} and {@code q} are literals, for example, can be part of a Prolog
 * database. A negative literal such as {@code not(p)}, however, cannot be
 * inserted into a Prolog database. Continuing the example of Prolog, it is also
 * important to realize that a conjunction {@code p, q} is not a database
 * formula as it cannot be asserted, even though both {@code p} and {@code q}
 * separately may be inserted into a Prolog database.
 * </p>
 *
 * <p>
 * A database formula may have free variables as any other logical expression.
 * Before inserting a database formula into a database, however, these variables
 * may need to have been instantiated (by applying a substitution).
 * </p>
 *
 * <p>
 * Make sure to also implement {@link java.lang.Object#equals(Object)} and
 * {@link java.lang.Object#hashCode()}, which are needed for implementing
 * {@link Expression#mgu(Expression)}.
 * </p>
 */
public interface DatabaseFormula extends Expression {

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables
	 * that are bound to a term in the substitution by that term (or, only
	 * renames in case the substitution binds a variable to another one).
	 */
	@Override
	DatabaseFormula applySubst(Substitution substitution);

	/**
	 * @return {@code true} if this database formula also can be used as a
	 *         {@link Query} (after conversion using {@link #toQuery()}),
	 *         {@code false} otherwise.
	 */
	boolean isQuery();

	/**
	 * Converts a database formula to a query. May not perform any check whether
	 * a conversion can be successfully performed. Use {@link #isQuery()} to
	 * check this.
	 *
	 * @return A {@link Query}.
	 */
	Query toQuery();

}
