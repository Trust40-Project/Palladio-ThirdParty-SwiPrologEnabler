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

package krTools;

import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;

/**
 * The knowledge representation (KR) interface.
 * 
 * <p>A knowledge representation interface should provide the following services:
 * <ul>
 * 	<li>retrieving the name of the interface.</li>
 * 	<li>initializing the interface to enable use of it.</li>
 *  <li>resetting the interface.</li>
 * 	<li>creating a database.</li>
 * 	<li>providing a parser for parsing source (files) represented in the KR language.</li>
 * 	<li>mapping a map of variables and terms to a substitution.</li>
 * </ul>
 */
public interface KRInterface {

	/**
	 * @return The name of the KR interface.
	 */
	String getName();

	/**
	 * Performs any initializations that need to be performed before the
	 * KR interface can be used.
	 * 
	 * @throws KRInitFailedException If initialization of the KR interface failed.
	 */
	void initialize() throws KRInitFailedException;

	/**
	 * Resets the KR interface. Should clear and free all memory used by the inference engine
	 * and any of the databases that have been created.
	 * 
	 * @throws KRDatabaseException If releasing resources was not successful or incomplete. 
	 */
	void release() throws KRDatabaseException;

	/**
	 * Creates new database from the content provided.
	 * 
	 * @param content A list of {@link DatabaseFormula}s that should be inserted in database.
	 * @return A database with the given content.
	 * @throws KRDatabaseException If the database could not be created, or the content
	 * 			provided could not be added.
	 */
	Database getDatabase(Collection<DatabaseFormula> content) throws KRDatabaseException;

	/**
	 * Returns a parser for this KR language. Only initializes the parser but
	 * does not parse the source yet. See {@link Parser} for methods that parse
	 * the input. 
	 * 
	 * @param source The source that is to be parsed.
	 * @throws ParserException If anything went wrong during initialization of the
	 * 		parser, e.g., due to a problem with the source.
	 */
	Parser getParser(Reader source) throws ParserException;

	/**
	 * Creates a substitution from a map of variables to terms. 
	 * 
	 * @return A substitution which binds all variables in the map to the associated terms.
	 */
	public Substitution getSubstitution(Map<Var,Term> map);
	
	/**
	 * Reports the queries in the given set of queries that have not been defined in the set of
	 * database formulas as well as those queries that are implicitly queries in the set of 
	 * database formulas itself (e.g., as condition in a rule).
	 */
	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries);

	/**
	 * Reports the formulas that have been defined in the set of database formulas
	 * but are not used (queried).
	 */
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs, Set<Query> queries);

}
