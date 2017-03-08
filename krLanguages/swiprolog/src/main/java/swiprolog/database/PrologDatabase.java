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
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import jpl.Term;
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
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologSubstitution;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologFloatImpl;
import swiprolog.language.impl.PrologIntImpl;
import swiprolog.language.impl.PrologVarImpl;

public class PrologDatabase implements Database {
	private final static PrologCompound trueterm = new PrologAtomImpl("true", null);
	/**
	 * Name of this database; used to name a SWI-Prolog module that implements
	 * the database.
	 */
	private final PrologAtomImpl name;
	/**
	 * The KRI that is managing this database.
	 */
	private final SwiPrologInterface owner;
	/**
	 * A corresponding theory
	 */
	private final Theory theory;

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
		this.name = new PrologAtomImpl(name, null);
		this.owner = owner;
		this.theory = new Theory(content);
		try {
			// Create SWI Prolog module that will act as our database.
			// FIXME: this is an expensive operation that is now run for
			// knowledge bases as well, and might be run for bases in a mental
			// model that will never be used anyway too.
			PrologCompound init = prefix(trueterm);
			rawquery(init);
			if (content != null) {
				for (DatabaseFormula dbf : content) {
					insert(dbf);
				}
			}
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("unable to create a Prolog database module '" + name + "'.", e);
		}
	}

	private PrologCompound prefix(PrologTerm term) {
		return new PrologCompoundImpl(":", new PrologTerm[] { this.name, term }, null);
	}

	@Override
	public String getName() {
		return this.name.name();
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
	 * Performs given query on the database. As databases are implemented as
	 * modules in SWI Prolog, a query is constructed that contains a reference
	 * to the corresponding module.
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
		PrologCompound conjunctive = new PrologCompoundImpl(",", new PrologTerm[] { trueterm, prefix(query) }, null);
		// Perform the query
		return rawquery(conjunctive);
	}

	/**
	 * Inserts a set of like {@link #insert(DatabaseFormula)}, but does not add
	 * them to the theory. This makes sure that they will not show up when the
	 * set of formulas in this base is requested, and that they cannot be
	 * modified either (because the theory is always checked for that).
	 *
	 * @param knowledge
	 *            the set of knowledge that should be imposed on this database.
	 * @throws KRDatabaseException
	 */
	public void addKnowledge(Set<DatabaseFormula> knowledge) throws KRDatabaseException {
		for (DatabaseFormula formula : knowledge) {
			insert(formula);
		}
	}

	/**
	 * <p>
	 * Inserts formula into SWI prolog database without any checks. You are
	 * responsible for creating legal SWI prolog query. The formula will be
	 * prefixed with the label of the database: the SWI prolog query will look
	 * like <br>
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
			insert(((PrologDBFormula) formula).getCompound());
		}
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
	 * Creates JPL term that wraps given term inside "assert(databaseName:term)"
	 * for clauses, and just databaseName:term for directives (without the :-).
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
		PrologCompound query = null;
		if (formula.getSignature().equals(":-/1")) { // directive
			formula = (PrologCompound) formula.getArg(0);
			query = new PrologCompoundImpl(",", new PrologTerm[] { trueterm, prefix(formula) }, null);
		} else { // clause
			query = new PrologCompoundImpl("assert", new PrologTerm[] { prefix(formula) }, null);
		}
		try {
			rawquery(query);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("inserting '" + formula + "' failed.", e);
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
	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		if (this.theory.remove(formula)) {
			delete(((PrologDBFormula) formula).getCompound());
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
		PrologCompound retraction = new PrologCompoundImpl("retractall", new PrologTerm[] { prefix(formula) }, null);
		try {
			rawquery(retraction);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("deleting '" + formula + "' failed.", e);
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
	 * @param query
	 *            A JPL query.
	 * @return A set of substitutions, empty set if there are no solutions, and
	 *         a set with the empty substitution if the query succeeds but does
	 *         not return any bindings of variables.
	 * @throws KRQueryFailedException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set<Substitution> rawquery(PrologTerm query) throws KRQueryFailedException {
		// Create JPL query.
		jpl.Query jplQuery = new jpl.Query((jpl.Term) query);

		// Get all solutions.
		Hashtable[] solutions;
		try {
			solutions = jplQuery.allSolutions();
		} catch (jpl.PrologException e) {
			throw new PrologError(e);
		} catch (Throwable e) {
			// catch all other (runtime) exceptions and wrap into checked
			// exception with general message
			throw new KRQueryFailedException("swi prolog says the query " + jplQuery + " failed", e);
		}

		// Convert to PrologSubstitution.
		Set<Substitution> substitutions = new LinkedHashSet<>(solutions.length);
		for (Hashtable<String, jpl.Term> solution : solutions) {
			Substitution subst = new PrologSubstitution();
			for (Entry<String, Term> entry : solution.entrySet()) {
				PrologVar var = new PrologVarImpl(entry.getKey(), null);
				PrologTerm term = fromJpl(entry.getValue());
				subst.addBinding(var, term);
			}
			substitutions.add(subst);
		}

		return substitutions;
	}

	private static PrologTerm fromJpl(jpl.Term term) {
		if (term.isAtom()) {
			jpl.Atom atom = (jpl.Atom) term;
			return new PrologAtomImpl(atom.name(), null);
		} else if (term.isCompound()) {
			jpl.Compound compound = (jpl.Compound) term;
			PrologTerm[] args = new PrologTerm[compound.arity()];
			for (int i = 1; i <= compound.arity(); ++i) {
				args[i - 1] = fromJpl(compound.arg(i));
			}
			return new PrologCompoundImpl(compound.name(), args, null);
		} else if (term.isFloat()) {
			jpl.Float flot = (jpl.Float) term;
			return new PrologFloatImpl(flot.doubleValue(), null);
		} else if (term.isInteger()) {
			jpl.Integer integer = (jpl.Integer) term;
			return new PrologIntImpl(integer.longValue(), null);
		} else if (term.isVariable()) {
			jpl.Variable var = (jpl.Variable) term;
			return new PrologVarImpl(var.name(), null);
		} else {
			return null;
		}
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
	 *
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
		PrologVar predicate = new PrologVarImpl("Predicate", null);
		PrologVar head = new PrologVarImpl("Head", null);
		PrologCompound db_head = prefix(head);
		PrologCompound current = new PrologCompoundImpl("current_predicate", new PrologTerm[] { predicate, head },
				null);
		PrologCompound db_current = prefix(current);
		PrologCompound built_in_atom = new PrologAtomImpl("built_in", null);
		PrologCompound built_in = new PrologCompoundImpl("predicate_property",
				new PrologTerm[] { db_head, built_in_atom }, null);
		PrologCompound foreign_atom = new PrologAtomImpl("foreign", null);
		PrologCompound foreign = new PrologCompoundImpl("predicate_property",
				new PrologTerm[] { db_head, foreign_atom }, null);
		PrologVar anon = new PrologVarImpl("_", null);
		PrologCompound imported_from = new PrologCompoundImpl("imported_from", new PrologTerm[] { anon }, null);
		PrologCompound imported = new PrologCompoundImpl("predicate_property",
				new PrologTerm[] { db_head, imported_from }, null);
		PrologCompound not_built_in = new PrologCompoundImpl("not", new PrologTerm[] { built_in }, null);
		PrologCompound not_foreign = new PrologCompoundImpl("not", new PrologTerm[] { foreign }, null);
		PrologCompound not_imported = new PrologCompoundImpl("not", new PrologTerm[] { imported }, null);
		PrologCompound retract = new PrologCompoundImpl("retractall", new PrologTerm[] { db_head }, null);
		PrologCompound conj45 = new PrologCompoundImpl(",", new PrologTerm[] { not_imported, retract }, null);
		PrologCompound conj345 = new PrologCompoundImpl(",", new PrologTerm[] { not_foreign, conj45 }, null);
		PrologCompound conj2345 = new PrologCompoundImpl(",", new PrologTerm[] { not_built_in, conj345 }, null);
		PrologCompound query = new PrologCompoundImpl(",", new PrologTerm[] { db_current, conj2345 }, null);

		try {
			rawquery(query);
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("erasing the contents of database '" + this.name + "' failed.", e);
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
}
