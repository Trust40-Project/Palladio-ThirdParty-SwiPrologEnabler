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

package krTools.database;

import java.util.Set;

import krTools.KRInterface;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

/**
 * A database that contains content represented in the KR language. A database
 * is viewed as a set of {@link DatabaseFormula}s.
 *
 * <p>
 * Also see: {@link KRInterface#getDatabase(java.util.Collection)}.
 * </p>
 */
public interface Database {

	/**
	 * @return The name of the database.
	 */
	String getName();

	/**
	 * Defines the inference mechanism associated with a KRlanguage. The GOAL
	 * programming language requires that at least this method is specified in
	 * order to be able to effectively operate with the KRlanguage.
	 *
	 * @return Set of substitutions. This set is empty if there are no
	 *         solutions. If there is a one solution without substitutions,
	 *         returns set with one empty substitution.
	 * @throws KRQueryFailedException
	 *             If performing the query failed for some reason.
	 */
	Set<Substitution> query(Query query) throws KRQueryFailedException;

	/**
	 * Inserts a formula into the database.
	 *
	 * <p>
	 * After addition of the formula, the database should entail the information
	 * added (and, if applicable, no longer entail the information removed from
	 * the database, e.g., when a negated fact is "inserted" by removing the
	 * fact from the database).
	 * </p>
	 *
	 * @param formula
	 *            The database formula to be added. throws KRDatabaseException
	 *            If formula could not be inserted.
	 */
	void insert(DatabaseFormula formula) throws KRDatabaseException;

	/**
	 * Updates the database with the {@link Update}.
	 *
	 * <p>
	 * After performing the update, the database should entail the information
	 * added by the update and, in principle, no longer entail the information
	 * removed from the database.
	 * </p>
	 *
	 * @param update
	 *            The update made to the database.
	 * @throws KRDatabaseException
	 *             If update could not be performed.
	 */
	void insert(Update update) throws KRDatabaseException;

	/**
	 * Removes a formula from the database.
	 *
	 * <p>
	 * After removal of the formula, in principle, the database should no longer
	 * entail the information removed from the database and, if applicable,
	 * entail any information that is added, e.g., when a negated fact is
	 * "deleted" by adding the fact to the database).
	 *
	 * @param formula
	 *            The formula to be removed.
	 * @throws KRDatabaseException
	 *             If formula could not be removed.
	 */
	void delete(DatabaseFormula formula) throws KRDatabaseException;

	/**
	 * Updates the database with the {@link Update}.
	 *
	 * <p>
	 * After performing the update, the database should entail the information
	 * added by the update and, in principle, no longer entail the information
	 * removed from the database.
	 * </p>
	 *
	 * @param update
	 *            The update made to the database.
	 * @throws KRDatabaseException
	 *             If update could not be performed.
	 */
	void delete(Update update) throws KRDatabaseException;

	/**
	 * Cleans up a database. Should free all memory used by the database.
	 *
	 * @throws KRDatabaseException
	 *             If something went wrong while bringing the database down.
	 */
	void destroy() throws KRDatabaseException;

}
