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
import java.util.Map.Entry;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import swiprolog.SwiPrologInterface;
import swiprolog.errors.PrologError;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologSubstitution;
import swiprolog.language.PrologTerm;
import swiprolog.language.impl.PrologImplFactory;

public class PrologDatabase implements Database {
	private static final Object queryLock = new Object();
	/**
	 * Name of this database; used to name a SWI-Prolog module that implements the
	 * database.
	 */
	private final PrologCompound name;
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
	private PrologCompound writecache;
	private int cachecount = 0;
	private final boolean isStatic;

	/**
	 * @param name
	 *            A human-readable name for the database.
	 * @param content
	 *            The theory containing initial database contents. Ignored if null.
	 * @param owner
	 *            The interface instance that creates this database.
	 * @throws KRInitFailedException
	 *             If database creation failed.
	 */
	public PrologDatabase(String name, Collection<DatabaseFormula> content, SwiPrologInterface owner, boolean isStatic)
			throws KRDatabaseException {
		this.name = PrologImplFactory.getAtom(name, null);
		this.owner = owner;
		this.theory = new Theory();
		// Create SWI Prolog module that will act as our database.
		// FIXME: this is an expensive operation that is now run for
		// knowledge bases as well, and might be run for bases in a mental
		// model that will never be used anyway too.
		PrologCompound init = prefix(PrologImplFactory.getAtom("true", null));
		addToWriteCache(init);
		if (content != null) {
			for (DatabaseFormula dbf : content) {
				insert(dbf);
			}
			try {
				// Databases may be re-used by other databases (eg KBs).
				// flush to ensure dbs are ready to use now
				flushWriteCache();
			} catch (KRQueryFailedException e) {
				throw new KRDatabaseException("Failed to initialize database", e);
			}
		}
		// set static only after all KB has been inserted!
		this.isStatic = isStatic;
	}

	private PrologCompound prefix(Term term) {
		return PrologImplFactory.getCompound(":", new Term[] { this.name, term }, null);
	}

	@Override
	public String getName() {
		return this.name.getName();
	}

	/**
	 * @return atom with name of this database
	 */
	public PrologCompound getJPLName() {
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
		PrologCompound query = ((PrologQuery) pQuery).getCompound();
		// We need to create conjunctive query with "true" as first conjunct and
		// db_query as second conjunct as JPL query dbname:not(..) does not work
		// otherwise...
		PrologCompound db_query_final = PrologImplFactory.getCompound(",",
				new Term[] { PrologImplFactory.getAtom("true", null), prefix(query) }, null);
		// Perform the query
		flushWriteCache();
		return rawquery(db_query_final);
	}

	/**
	 * Check that this database can be modified
	 *
	 * @throws KRDatabaseException
	 */
	private void checkModifyable() throws KRDatabaseException {
		if (this.isStatic) {
			throw new KRDatabaseException("Database is static and can not be modified");
		}
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
		checkModifyable();
		for (DatabaseFormula formula : knowledge) {
			insert(formula);
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
	 *            is the formula to be inserted into database. appropriate database
	 *            label will be prefixed to your formula
	 * @throws KRDatabaseException
	 */
	@Override
	public boolean insert(DatabaseFormula formula) throws KRDatabaseException {
		checkModifyable();
		if (this.theory.add(formula)) {
			insert(((PrologDBFormula) formula).getCompound());
			return true;
		} else {
			return false;
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
	private void insert(PrologCompound formula) throws KRDatabaseException {
		checkModifyable();
		PrologCompound query = null;
		if (formula.isDirective()) {
			formula = (PrologCompound) formula.getArg(0);
			query = PrologImplFactory.getCompound(",",
					new Term[] { PrologImplFactory.getAtom("true", null), prefix(formula) }, null);
		} else {
			query = PrologImplFactory.getCompound("assert", new Term[] { prefix(formula) }, null);
		}
		addToWriteCache(query);
	}

	// ***************** delete methods ****************/

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
	public boolean delete(DatabaseFormula formula) throws KRDatabaseException {
		checkModifyable();
		if (this.theory.remove(formula)) {
			delete(((PrologDBFormula) formula).getCompound());
			return true;
		} else {
			return false;
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
	private void delete(PrologCompound formula) throws KRDatabaseException {
		checkModifyable();
		PrologCompound retraction = PrologImplFactory.getCompound("retractall", new Term[] { prefix(formula) }, null);
		addToWriteCache(retraction);
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
	public static Set<Substitution> rawquery(PrologTerm query) throws KRQueryFailedException {
		// Create JPL query.
		org.jpl7.Query jplQuery = new org.jpl7.Query((org.jpl7.Compound) query);

		// Get all solutions.
		Map<String, org.jpl7.Term>[] solutions;
		try {
			// EXCEPTION_ACCESS_VIOLATIONs can occur with multi-threading :(
			synchronized (queryLock) {
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
		Set<Substitution> substitutions = new LinkedHashSet<>(solutions.length);
		for (Map<String, org.jpl7.Term> solution : solutions) {
			Substitution subst = new PrologSubstitution();
			for (Entry<String, org.jpl7.Term> entry : solution.entrySet()) {
				Var var = PrologImplFactory.getVar(entry.getKey(), null);
				Term term = fromJpl(entry.getValue());
				subst.addBinding(var, term);
			}
			substitutions.add(subst);
		}

		return substitutions;
	}

	public static Term fromJpl(org.jpl7.Term term) {
		if (term.isAtom()) {
			org.jpl7.Atom atom = (org.jpl7.Atom) term;
			return PrologImplFactory.getAtom(atom.name(), null);
		} else if (term.isCompound()) {
			org.jpl7.Compound compound = (org.jpl7.Compound) term;
			Term[] args = new Term[compound.arity()];
			for (int i = 1; i <= compound.arity(); ++i) {
				args[i - 1] = fromJpl(compound.arg(i));
			}
			return PrologImplFactory.getCompound(compound.name(), args, null);
		} else if (term.isFloat()) {
			org.jpl7.Float flot = (org.jpl7.Float) term;
			return PrologImplFactory.getNumber(flot.doubleValue(), null);
		} else if (term.isInteger()) {
			org.jpl7.Integer integer = (org.jpl7.Integer) term;
			return PrologImplFactory.getNumber(integer.longValue(), null);
		} else if (term.isVariable()) {
			org.jpl7.Variable var = (org.jpl7.Variable) term;
			return PrologImplFactory.getVar(var.name(), null);
		} else {
			return null;
		}
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
		this.cachecount = 0;
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
		Term predicate = PrologImplFactory.getVar("Predicate", null);
		Term head = PrologImplFactory.getVar("Head", null);
		PrologCompound db_head = prefix(head);
		PrologCompound current = PrologImplFactory.getCompound("current_predicate", new Term[] { predicate, head },
				null);
		PrologCompound db_current = prefix(current);
		PrologCompound built_in_atom = PrologImplFactory.getAtom("built_in", null);
		PrologCompound built_in = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, built_in_atom }, null);
		PrologCompound foreign_atom = PrologImplFactory.getAtom("foreign", null);
		PrologCompound foreign = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, foreign_atom }, null);
		Term anon = PrologImplFactory.getVar("_", null);
		PrologCompound imported_from = PrologImplFactory.getCompound("imported_from", new Term[] { anon }, null);
		PrologCompound imported = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, imported_from }, null);
		PrologCompound not_built_in = PrologImplFactory.getCompound("not", new Term[] { built_in }, null);
		PrologCompound not_foreign = PrologImplFactory.getCompound("not", new Term[] { foreign }, null);
		PrologCompound not_imported = PrologImplFactory.getCompound("not", new Term[] { imported }, null);
		PrologCompound retract = PrologImplFactory.getCompound("retractall", new Term[] { db_head }, null);
		PrologCompound conj45 = PrologImplFactory.getCompound(",", new Term[] { not_imported, retract }, null);
		PrologCompound conj345 = PrologImplFactory.getCompound(",", new Term[] { not_foreign, conj45 }, null);
		PrologCompound conj2345 = PrologImplFactory.getCompound(",", new Term[] { not_built_in, conj345 }, null);
		PrologCompound query = PrologImplFactory.getCompound(",", new Term[] { db_current, conj2345 }, null);

		try {
			rawquery(query);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("erasing the contents of database '" + this.name + "' failed.", e);
		}
	}

	// NEW: MERGE ALL ASSERTS AND RETRACTS...
	private void addToWriteCache(PrologCompound formula) throws KRDatabaseException {
		if (this.writecache == null) {
			this.writecache = formula;
		} else {
			this.writecache = PrologImplFactory.getCompound(",", new Term[] { this.writecache, formula },
					formula.getSourceInfo());
		}
		if (++this.cachecount == Byte.MAX_VALUE) {
			try { // prevents stackoverflows
				flushWriteCache();
			} catch (KRQueryFailedException e) {
				throw new KRDatabaseException("", e);
			}
		}
	}

	// ... TO EXECUTE THEM ALLTOGETHER AT (BEFORE) THE NEXT QUERY
	private void flushWriteCache() throws KRQueryFailedException {
		if (this.writecache != null) {
			try {
				rawquery(this.writecache);
			} finally {
				this.writecache = null;
				this.cachecount = 0;
			}
		}
	}

	@Override
	public String toString() {
		return "<" + this.name + ">";
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
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
		} else if (this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * @return true iff this database is static i.e. it can not be modified.
	 */
	public boolean isStatic() {
		return this.isStatic;
	}
}
