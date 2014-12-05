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

package swiprolog.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import swiprolog.SWIPrologInterface;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologSubstitution;

/**
 * 
 */
public class SWIPrologDatabase implements Database {

	/**
	 * Name of this database; used to name a SWI-Prolog module that implements
	 * the database.
	 */
	private jpl.Atom name;
	/**
	 * Static int for representing unique number to be able to generate unique
	 * database names.
	 */
	private static Integer uniqueNumberCounter = 0;

	/**
	 * @param content
	 *            the theory containing initial database contents
	 * @throws KRInitFailedException
	 *             If database creation failed.
	 */
	public SWIPrologDatabase(Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		int number;
		synchronized (uniqueNumberCounter) {
			number = uniqueNumberCounter++;
		}
		// Name consists of (lower case) database type post-fixed with unique
		// number.
		this.name = new jpl.Atom("db"+ number);

			// FIXME: SWI Prolog needs access to various libaries at runtime and
			// loads these
			// dynamically; if many agents try to do this at the same time this
			// gives access
			// errors ('No permission to load'). We can now decide to fix this
			// by loading these
			// libraries upfront when we need them (that implies work to check
			// whether we need
			// a library of course). Benefit is that we ONLY need to synchronize
			// creating of
			// databases (this code) and NOT all calls to rawquery... We should
			// check whether
			// this impacts performance or not.
			// FOr now, solved this issue by adding synchronized modifier to
			// rawquery.

			// EXAMPLE BELOW: only loads lists.pl but no other libraries.
			// jpl.Term loadlists = JPLUtils.createCompound("ensure_loaded", new
			// jpl.Atom("c:/program files/pl/library/lists.pl"));
			// SWIQuery.rawquery(JPLUtils.createCompound(":", this.name,
			// loadlists));
			// }
	}

	public String getName() {
		return name.name();
	}

	/**
	 * 
	 * @return
	 */
	public jpl.Atom getJPLName() {
		return this.name;
	}
	
	/**
	 * Removes a database from the list of databases maintained by SWI Prolog.
	 * 
	 * TRAC #2027 there seems no factory pattern dealing with instance deletion.
	 * 
	 * @param database
	 *            The database that has been deleted.
	 * @throws KRDatabaseException 
	 */
	public void destroy() throws KRDatabaseException {
		eraseContent();
		cleanUp();
	}


	/**
	 * Performs given query on the database. As databases are implemented as
	 * modules in SWI Prolog, a query is constructed that contains a reference
	 * to the corresponding module.
	 */
	public Set<Substitution> query(Query pQuery) throws KRQueryFailedException {
		Set<Substitution> substSet = new LinkedHashSet<Substitution>();

		jpl.Term query = ((PrologQuery) pQuery).getTerm();
		jpl.Term db_query = JPLUtils.createCompound(":", getJPLName(), query);
		// We need to create conjunctive query with "true" as first conjunct and
		// db_query
		// as second conjunct as JPL query dbname:not(..) does not work
		// otherwise...
		substSet.addAll(rawquery(JPLUtils.createCompound(",",
				new jpl.Atom("true"), db_query)));
		return substSet;
	}

	/**
	 * <p>
	 * Inserts formula into SWI prolog database without any checks. You are
	 * responsible for creating legal SWI prolog query. The formula will be
	 * prefixed with the label of the database: the SWI prolog query will look
	 * like <br>
	 * <tt>insert(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 * ASSUMES formula can be argument of assert (fact, rules). <br>
	 * 
	 * @param formula
	 *            is the PrologTerm to be inserted into database. appropriate
	 *            database label will be prefixed to your formula
	 * @throws KRDatabaseException 
	 */
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		insert(((PrologDBFormula) formula).getTerm());
	}

	/**
	 * Inserts positive literals that are part of the update into the database
	 * and retracts the negative literals. CHECK maybe it is faster to compile
	 * to a single large JPL call before
	 * 
	 * @param update
	 *            The basic databaseformula, not(databaseformula) or
	 *            comma-separated list of dtabase formulas. comma-separated as
	 *            usual in Prolog: (t1,(t2,(t3,(t4...,(..,tn)))))
	 * @param database
	 *            The database into which the update has been inserted.
	 * @throws KRDatabaseException 
	 */
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete((formula));
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert((formula));
		}
	}

	/**
	 * Creates JPL term that wraps given term inside
	 * "assert(databaseName:term)".
	 * <p>
	 * Prefix notation is used below to construct the assert term.
	 * </p>
	 * 
	 * @param formula
	 *            The JPL term to be inserted.
	 * @param database
	 *            The database the term should be inserted into.
	 * @throws KRDatabaseException 
	 */
	public void insert(jpl.Term formula) throws KRDatabaseException {
		jpl.Term db_formula = JPLUtils.createCompound(":", getJPLName(),
				formula);
		try {
			rawquery(JPLUtils.createCompound("assert", db_formula));
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException(e.getMessage(), e);
		}
	}

	// ***************** delete methods ****************/

	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete((formula));
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert((formula));
		}
	}

	/**
	 * <p>
	 * Deletes a formula from a SWI Prolog Database. You are responsible for
	 * creating legal SWI prolog query. The formula will be prefixed with the
	 * label of the database: the SWI prolog query will look like <br>
	 * <tt>retract(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 * 
	 * @param formula
	 *            is the DatabaseFormula to be retracted from SWI. ASSUMES
	 *            formula can be argument of retract (fact, rules). CHECK rules
	 *            need to be converted into string correctly! toString may be
	 *            insufficient for SWI queries
	 * @throws KRDatabaseException 
	 */
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		delete(((PrologDBFormula) formula).getTerm());
	}

	/**
	 * Creates JPL term that wraps given term inside
	 * "retract(databaseName:term)".
	 * <p>
	 * Prefix notation is used below to construct the retract term.
	 * </p>
	 * 
	 * @param formula
	 *            The JPL term to be deleted.
	 * @param database
	 *            The database the term should be deleted from.
	 * @throws KRDatabaseException 
	 */
	public void delete(jpl.Term formula) throws KRDatabaseException {
		jpl.Term db_formula = JPLUtils.createCompound(":", getJPLName(),
				formula);
		try {
			rawquery(JPLUtils.createCompound("retract", db_formula));
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * <p>
	 * A call to SWI Prolog that converts the solutions obtained into
	 * {@link PrologSubstitution}s.
	 * </p>
	 * <p>
	 * WARNING. this is for internal use in KR implementation only. There is a
	 * known issue with floats (TRAC #726).
	 * </p>
	 * 
	 * @param query A JPL query.
	 * @return A set of substitutions, empty set if there are no solutions, and
	 * 			a set with the empty substitution if the query succeeds but does
	 * 			not return any bindings of variables.
	 * @throws KRQueryFailedException 
	 */
	@SuppressWarnings("unchecked")
	public static synchronized Set<PrologSubstitution> rawquery(jpl.Term query) throws KRQueryFailedException {
		
		// Create JPL query.
		jpl.Query jplQuery = new jpl.Query(query);
		
		// Get all solutions.
		@SuppressWarnings("rawtypes")
		Hashtable[] solutions;
		try {
			solutions = jplQuery.allSolutions();
		} catch (Throwable e) {
			throw new KRQueryFailedException(handleRawqueryException(jplQuery, e), e);
		}
				
		// Convert to PrologSubstitution.
		LinkedHashSet<PrologSubstitution> substitutions = new LinkedHashSet<PrologSubstitution>();
		for (int i=0; i < solutions.length; i++) {
			substitutions.add(PrologSubstitution.getSubstitutionOrNull(solutions[i]));
		}

		return substitutions;
	}
	
	/**
	 * Converts exception into more readable warning message.
	 *  
	 * @param query
	 * @param e
	 * @return
	 */
	private static String handleRawqueryException(jpl.Query query, Throwable e) {
		// free up the memory.
		System.gc();

		String warning = "Query " + query + " failed.";
		if (e instanceof Exception) {
			String mess = e.getMessage();
			int i = mess.indexOf("existence_error(procedure, ");
			if (i != -1) {
				// JPL existence error. ASSUMES modules are queried.
				int start = i + 29;
				int end = mess.indexOf(")", start); // should be there
				warning = "Query " + query
						+ " failed because predicate has not been implemented: "
						+ mess.substring(start, end).replace(',', '/');
			}
		}
		return warning;
	}
	
	
	
	/**
	 * Read a file generated by SWI Prolog.
	 * 
	 * @param lFilename
	 *            is the name of the file to be read
	 * @throws KRException 
	 */
	private List<String> readTempFile(String lFilename) throws KRException {
		String lLine;
		ArrayList<String> lRules = new ArrayList<String>();
		File lListingfile = new File(lFilename);
		FileReader lFileReader;
		try {
			lFileReader = new FileReader(lListingfile);
		} catch (FileNotFoundException e) {
			throw new KRException("failed to open internal tmp file", e);
		}

		BufferedReader lBufferReader = new BufferedReader(lFileReader);
		String lCurrentRule = new String();
		try {
			while ((lLine = lBufferReader.readLine()) != null) {

				// FIXME: we really should keep them but parser crashes
				// currently on
				// "dynamic wumpus/1"
				if (lLine.contains(":- dynamic")) {
					continue;
				}
				lCurrentRule = lCurrentRule + lLine;
				if (lCurrentRule.length() > 0
						&& lCurrentRule.charAt(lCurrentRule.length() - 1) == '.') {

					// Clauses need WHITESPACE at end.
					lRules.add(lCurrentRule + " ");
					lCurrentRule = "";
				}
			}
			lBufferReader.close();
			lListingfile.delete(); // for debugging turn this off.
		} catch (IOException e) {
			throw new KRException("internal error while reading tmp file", e);
		}
		return lRules;
	}

	/**
	 * <p>
	 * Removes all predicates and clauses from the SWI Prolog database.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This is not implementable fully in SWI prolog. You can
	 * reset a database to free up some memory, but do not re-use the database.
	 * It will NOT reset the dynamic declarations. This is an issue but the JPL
	 * interface to SWI Prolog does not support removing these. Suggested
	 * workaround: After resetting do not re-use this database but make a new
	 * one.
	 * </p>
	 * <p>  
	 * @throws KRDatabaseException 
	 */
	protected void eraseContent() throws KRDatabaseException {
		// String deleteone =
		// "("
		// + this.name + ":current_predicate(Predicate, Head),"
		// + "not(predicate_property(" + this.name + ":Head, built_in)),"
		// + "not(predicate_property(" + this.name + ":Head, foreign)),"
		// + "not(predicate_property(" + this.name +
		// ":Head, imported_from(_))),"
		// + "retractall(" + this.name + ":Head)"
		// + ").";
		// Construct jpl term for above.
		jpl.Variable predicate = new jpl.Variable("Predicate");
		jpl.Variable head = new jpl.Variable("Head");
		jpl.Term db_head = JPLUtils.createCompound(":", this.name, head);
		jpl.Term current = JPLUtils.createCompound("current_predicate",
				predicate, head);
		jpl.Term db_current = JPLUtils.createCompound(":", this.name, current);
		jpl.Term built_in = JPLUtils.createCompound("predicate_property",
				db_head, new jpl.Atom("built_in"));
		jpl.Term foreign = JPLUtils.createCompound("predicate_property",
				db_head, new jpl.Atom("foreign"));
		jpl.Term imported_from = JPLUtils.createCompound("imported_from",
				new jpl.Variable("_"));
		jpl.Term imported = JPLUtils.createCompound("predicate_property",
				db_head, imported_from);
		jpl.Term not_built_in = JPLUtils.createCompound("not", built_in);
		jpl.Term not_foreign = JPLUtils.createCompound("not", foreign);
		jpl.Term not_imported = JPLUtils.createCompound("not", imported);
		jpl.Term retract = JPLUtils.createCompound("retractall", db_head);
		jpl.Term conj45 = JPLUtils.createCompound(",", not_imported, retract);
		jpl.Term conj345 = JPLUtils.createCompound(",", not_foreign, conj45);
		jpl.Term conj2345 = JPLUtils.createCompound(",", not_built_in, conj345);
		jpl.Term query = JPLUtils.createCompound(",", db_current, conj2345);

		try {
			rawquery(query);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException(e.getMessage(), e);
		}
	}

	/** 
	 * Frees memory used by database upon deletion of the database.
	 * 
	 * @throws KRDatabaseException
	 */
	protected void cleanUp() throws KRDatabaseException {
		this.eraseContent();
		try {
			SWIPrologInterface.getInstance().removeDatabase(this);
		} catch (KRInitFailedException e) {
			throw new KRDatabaseException("Clean up before initialization: " + e.getMessage(), e);
		}
	}

	public void showStatistics() {
		try {
			rawquery(new jpl.Atom("statistics"));
		} catch (Exception e) {
			// TODO new Warning("Cannot retrieve statistics: " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "<" + this.name + ">";
	}

}
