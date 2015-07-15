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

package swiprolog;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRQueryFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import swiprolog.database.PrologDatabase;
import swiprolog.language.Analyzer;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologSubstitution;
import swiprolog.parser.KRInterfaceParser4;

/**
 * Implementation of {@link KRInterface} for SWI Prolog.
 */
public final class SwiPrologInterface implements KRInterface {
	static {
		SwiInstaller.init();
	}

	/**
	 * Contains all databases that are maintained by SWI Prolog. The key is the
	 * owner of the database. The value is a list of databases associated with
	 * that agent. An owner that has no associated databases should be removed
	 * from the map.
	 */
	private Map<String, PrologDatabase> databases = new HashMap<String, PrologDatabase>();

	/**
	 * Creates new inference engine and empty set of databases.
	 *
	 * @throws KRInitFailedException
	 *             If failed to create inference engine or database.
	 */
	public SwiPrologInterface() throws KRInitFailedException {
		// Initialize inference engine.
		try {
			PrologDatabase.rawquery(
					JPLUtils.createCompound("set_prolog_flag", new jpl.Atom("debug_on_error"), new jpl.Atom("false")));
		} catch (KRQueryFailedException e) {
			throw new KRInitFailedException("swi prolog says it can't set the run mode", e);
		}
		// See http://www.swi-prolog.org/packages/jpl/release_notes.html for
		// explanation why Don't Tell Me Mode needs to be false. Setting this
		// mode to false ensures that variables with initial '_' are treated as
		// regular variables.
		jpl.JPL.setDTMMode(false);
	}

	/**
	 * Returns a database of a particular type associated with a given agent.
	 * <p>
	 * <b>Warning</b>: Cannot be used to get goal bases because in contrast with
	 * all other types of databases an agent can have multiple databases that
	 * are used for storing goals.
	 * </p>
	 *
	 * @param name
	 *            the owner name of the database
	 * @returns The database associated with a given agent of a given type, or
	 *          {@code null} if no database of the given type exists.
	 */
	protected PrologDatabase getDatabase(String name) {
		return this.databases.get(name);
	}

	@Override
	public Database getDatabase(Collection<DatabaseFormula> theory) throws KRDatabaseException {
		// Create new database of given type, content;
		// use name as base name for name of database.
		PrologDatabase database = new PrologDatabase(theory, this);
		// Add database to list of databases maintained by SWI Prolog and
		// associated with name.
		this.databases.put(database.getName(), database);

		// Return new database.
		return database;
	}

	/**
	 *
	 * @param db
	 */
	public void removeDatabase(PrologDatabase db) {
		this.databases.remove(db.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parser getParser(Reader r, SourceInfo info) throws ParserException {
		try {
			return new KRInterfaceParser4(r, info);
		} catch (IOException e) {
			throw new ParserException("could not parse the data as SWI prolog", info, e);
		}
	}

	/**
	 * Computes predicates that need to be declared dynamically for later
	 * reference. A SWI Prolog database assumes that all predicates that are
	 * queried have been either asserted or dynamically declared, otherwise an
	 * existence_error is produced. FIXME: does nothing?!
	 */
	@Override
	public void initialize(List<URI> uris) throws KRInitFailedException {

	}

	/**
	 * @throws KRDatabaseException
	 */
	@Override
	public void release() throws KRDatabaseException {
		for (PrologDatabase db : this.databases.values()) {
			// TODO: new InfoLog("Taking down database " + getName() + ".\n");
			db.destroy();
		}
		this.databases = new HashMap<String, PrologDatabase>();
	}

	@Override
	public Substitution getSubstitution(Map<Var, Term> map) {
		PrologSubstitution substitution = new PrologSubstitution();
		if (map != null) {
			for (Var var : map.keySet()) {
				substitution.addBinding(var, map.get(var));
			}
		}
		return substitution;
	}

	@Override
	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		Analyzer analyzer = new Analyzer(dbfs, queries);
		analyzer.analyze();
		return analyzer.getUndefined();
	}

	@Override
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		Analyzer analyzer = new Analyzer(dbfs, queries);
		analyzer.analyze();
		return analyzer.getUnused();
	}

	@Override
	public boolean supportsSerialization() {
		return false;
	}
}