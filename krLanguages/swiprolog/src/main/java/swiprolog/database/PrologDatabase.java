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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import swiprolog.SwiPrologInterface;
import swiprolog.errors.PrologError;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologSubstitution;

public class PrologDatabase implements Database {
	/**
	 * Name of this database; used to name a SWI-Prolog module that implements the
	 * database.
	 */
	private final org.jpl7.Atom name;
	/**
	 * The KRI that is managing this database.
	 */
	private final SwiPrologInterface owner;
	/**
	 * A corresponding theory
	 */
	private final Theory theory;
	/**
	 * A cache of write operations. Insert or deletes are queued here until the next
	 * query, at which point those operations are first all performed.
	 */
	private org.jpl7.Term writecache;
	/**
	 * Query lock (https://github.com/SWI-Prolog/issues/issues/60)
	 */
	private static final Object lock = new Object();

	/**
	 * @param name
	 *            A human-readable name for the database.
	 * @param content
	 *            The theory containing initial database contents.
	 * @param owner
	 *            The interface instance that creates this database.
	 * @throws KRInitFailedException
	 *             If database creation failed.
	 */
	public PrologDatabase(String name, Collection<DatabaseFormula> content, SwiPrologInterface owner)
			throws KRDatabaseException {
		this.name = new org.jpl7.Atom(name);
		this.owner = owner;
		this.theory = new Theory(content);
		// Create SWI Prolog module that will act as our database.
		// FIXME: this is an expensive operation that is now run for
		// knowledge bases as well, and might be run for bases in a mental
		// model that will never be used anyway too.
		addToWriteCache(JPLUtils.createCompound(":", getJPLName(), new org.jpl7.Atom("true")));
		if (content != null) {
			for (DatabaseFormula dbf : content) {
				insert(((PrologDBFormula) dbf).getTerm());
			}
		}
	}

	@Override
	public String getName() {
		return this.name.name();
	}

	/**
	 * @return atom with name of this database
	 */
	public org.jpl7.Atom getJPLName() {
		return this.name;
	}

	public Theory getTheory() {
		return this.theory;
	}

	/**
	 * Removes a database from the list of databases maintained by SWI Prolog.
	 *
	 * TRAC #2027 there seems no factory pattern dealing with instance deletion.
	 *
	 * @throws KRDatabaseException
	 */
	@Override
	public void destroy() throws KRDatabaseException {
		eraseContent();
		this.owner.removeDatabase(this);
	}

	/**
	 * Performs given query on the database. As databases are implemented as modules
	 * in SWI Prolog, a query is constructed that contains a reference to the
	 * corresponding module.
	 *
	 * @param pQuery
	 *            the query to be performed.
	 * @return set of substitutions satisfying the query.
	 */
	@Override
	public Set<Substitution> query(Query pQuery) throws KRQueryFailedException {
		Set<Substitution> substSet = new LinkedHashSet<>();
		org.jpl7.Term query = ((PrologQuery) pQuery).getTerm();
		org.jpl7.Term db_query = JPLUtils.createCompound(":", getJPLName(), query);
		// We need to create conjunctive query with "true" as first conjunct and
		// db_query as second conjunct as JPL query dbname:not(..) does not work
		// otherwise...
		org.jpl7.Term db_query_final = JPLUtils.createCompound(",", new org.jpl7.Atom("true"), db_query);
		flushWriteCache();
		substSet.addAll(rawquery(db_query_final));
		return substSet;
	}

	/**
	 * Inserts a set of like {@link #insert(DatabaseFormula)}, but does not add them
	 * to the theory. This makes sure that they will not show up when the set of
	 * formulas in this base is requested, and that they cannot be modified either
	 * (because the theory is always checked for that).
	 *
	 * @param knowledge
	 *            the set of knowledge that should be imposed on this database.
	 * @throws KRDatabaseException
	 */
	public void addKnowledge(Set<DatabaseFormula> knowledge) throws KRDatabaseException {
		for (DatabaseFormula formula : knowledge) {
			insert(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * <p>
	 * Inserts formula into SWI prolog database without any checks. You are
	 * responsible for creating legal SWI prolog query. The formula will be prefixed
	 * with the label of the database: the SWI prolog query will look like <br>
	 * <tt>insert(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 * ASSUMES formula can be argument of assert (fact, rules).
	 *
	 * @param formula
	 *            is the PrologTerm to be inserted into database. appropriate
	 *            database label will be prefixed to your formula
	 * @throws KRDatabaseException
	 */
	@Override
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		if (this.theory.add(formula)) {
			insert(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * Inserts positive literals that are part of the update into the database and
	 * retracts the negative literals. CHECK maybe it is faster to compile to a
	 * single large JPL call before
	 *
	 * @param update
	 *            The basic databaseformula, not(databaseformula) or comma-separated
	 *            list of dtabase formulas. comma-separated as usual in Prolog:
	 *            (t1,(t2,(t3,(t4...,(..,tn)))))
	 * @param database
	 *            The database into which the update has been inserted.
	 * @throws KRDatabaseException
	 */
	@Override
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert(formula);
		}
	}

	/**
	 * Creates JPL term that wraps given term inside "assert(databaseName:term)" for
	 * clauses, and just databaseName:term for directives (without the :-).
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
	private void insert(org.jpl7.Term formula) throws KRDatabaseException {
		if (JPLUtils.getSignature(formula).equals(":-/1")) { // directive
			org.jpl7.Term query = JPLUtils.createCompound(":", getJPLName(), formula.arg(1));
			org.jpl7.Term queryt = JPLUtils.createCompound(",", new org.jpl7.Atom("true"), query);
			addToWriteCache(queryt);
		} else { // clause
			org.jpl7.Term dbformula = JPLUtils.createCompound(":", getJPLName(), formula);
			addToWriteCache(JPLUtils.createCompound("assert", dbformula));
		}
	}

	// ***************** delete methods ****************/

	@Override
	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert(formula);
		}
	}

	/**
	 * <p>
	 * Deletes a formula from a SWI Prolog Database. You are responsible for
	 * creating legal SWI prolog query. The formula will be prefixed with the label
	 * of the database: the SWI prolog query will look like <br>
	 * <tt>retract(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 *
	 * @param formula
	 *            is the DatabaseFormula to be retracted from SWI. ASSUMES formula
	 *            can be argument of retract (fact, rules). CHECK rules need to be
	 *            converted into string correctly! toString may be insufficient for
	 *            SWI queries
	 * @throws KRDatabaseException
	 */
	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		if (this.theory.remove(formula)) {
			delete(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * Creates JPL term that wraps given term inside
	 * "retractall(databaseName:term)".
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
	private void delete(org.jpl7.Term formula) throws KRDatabaseException {
		org.jpl7.Term db_formula = JPLUtils.createCompound(":", getJPLName(), formula);
		addToWriteCache(JPLUtils.createCompound("retractall", db_formula));
	}

	/**
	 * <p>
	 * A call to SWI Prolog that converts the solutions obtained into
	 * {@link PrologSubstitution}s.
	 * </p>
	 * <p>
	 * WARNING. this is for internal use in KR implementation only. There is a known
	 * issue with floats (TRAC #726).
	 * </p>
	 *
	 * @param query
	 *            A JPL query.
	 * @return A set of substitutions, empty set if there are no solutions, and a
	 *         set with the empty substitution if the query succeeds but does not
	 *         return any bindings of variables.
	 * @throws KRQueryFailedException
	 */
	public static Set<PrologSubstitution> rawquery(org.jpl7.Term query) throws KRQueryFailedException {
		// Create JPL query.
		org.jpl7.Query jplQuery = new org.jpl7.Query(query);

		// Get all solutions.
		Map<String, org.jpl7.Term>[] solutions;
		try {
			synchronized (lock) {
				solutions = jplQuery.allSolutions();
			}
		} catch (org.jpl7.PrologException e) {
			throw new PrologError(e);
		} catch (Throwable e) {
			// catch all other (runtime) exceptions and wrap into checked
			// exception with general message
			throw new KRQueryFailedException("swi prolog says the query " + jplQuery + " failed", e);
		}

		// Convert to PrologSubstitution.
		Set<PrologSubstitution> substitutions = new LinkedHashSet<>(solutions.length);
		for (Map<String, org.jpl7.Term> solution : solutions) {
			substitutions.add(PrologSubstitution.getSubstitutionOrNull(new TreeMap<>(solution)));
		}

		return substitutions;
	}

	/**
	 * <p>
	 * Removes all predicates and clauses from the SWI Prolog database.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This is not implementable fully in SWI prolog. You can reset
	 * a database to free up some memory, but do not re-use the database. It will
	 * NOT reset the dynamic declarations. This is an issue but the JPL interface to
	 * SWI Prolog does not support removing these. Suggested workaround: After
	 * resetting do not re-use this database but make a new one.
	 * </p>
	 * <p>
	 *
	 * @throws KRDatabaseException
	 */
	protected void eraseContent() throws KRDatabaseException {
		this.writecache = null;
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
		org.jpl7.Variable predicate = new org.jpl7.Variable("Predicate");
		org.jpl7.Variable head = new org.jpl7.Variable("Head");
		org.jpl7.Term db_head = JPLUtils.createCompound(":", this.name, head);
		org.jpl7.Term current = JPLUtils.createCompound("current_predicate", predicate, head);
		org.jpl7.Term db_current = JPLUtils.createCompound(":", this.name, current);
		org.jpl7.Term built_in = JPLUtils.createCompound("predicate_property", db_head, new org.jpl7.Atom("built_in"));
		org.jpl7.Term foreign = JPLUtils.createCompound("predicate_property", db_head, new org.jpl7.Atom("foreign"));
		org.jpl7.Term imported_from = JPLUtils.createCompound("imported_from", new org.jpl7.Variable("_"));
		org.jpl7.Term imported = JPLUtils.createCompound("predicate_property", db_head, imported_from);
		org.jpl7.Term not_built_in = JPLUtils.createCompound("not", built_in);
		org.jpl7.Term not_foreign = JPLUtils.createCompound("not", foreign);
		org.jpl7.Term not_imported = JPLUtils.createCompound("not", imported);
		org.jpl7.Term retract = JPLUtils.createCompound("retractall", db_head);
		org.jpl7.Term conj45 = JPLUtils.createCompound(",", not_imported, retract);
		org.jpl7.Term conj345 = JPLUtils.createCompound(",", not_foreign, conj45);
		org.jpl7.Term conj2345 = JPLUtils.createCompound(",", not_built_in, conj345);
		org.jpl7.Term query = JPLUtils.createCompound(",", db_current, conj2345);

		try {
			rawquery(query);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("erasing the contents of database '" + this.name + "' failed.", e);
		}
	}

	// NEW: MERGE ALL ASSERTS AND RETRACTS...
	private void addToWriteCache(org.jpl7.Term formula) {
		if (this.writecache == null) {
			this.writecache = formula;
		} else {
			this.writecache = JPLUtils.createCompound(",", this.writecache, formula);
		}
	}

	// ... TO EXECUTE THEM ALLTOGETHER AT (BEFORE) THE NEXT QUERY
	private void flushWriteCache() throws KRQueryFailedException {
		if (this.writecache != null) {
			rawquery(this.writecache);
			this.writecache = null;
		}
	}

	@Override
	public String toString() {
		return "<" + this.name + ">";
	}

	@Override
	public int hashCode() {
		return JPLUtils.hashCode(this.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof PrologDatabase)) {
			return false;
		}
		PrologDatabase other = (PrologDatabase) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!JPLUtils.equals(this.name, other.name)) {
			return false;
		}
		return true;
	}
}
