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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;

import org.antlr.runtime.ANTLRReaderStream;

import swiprolog.database.SWIPrologDatabase;
import swiprolog.language.Analyzer;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologSubstitution;
import swiprolog.parser.KRInterfaceParser;

/**
 * Implementation of {@link KRInterface} for SWI Prolog.
 */
public final class SWIPrologInterface implements KRInterface {
	static {
		SwiInstaller.init();
	}

	/**
	 * use this for Singleton Design Pattern but this is not really a singleton
	 * as {@link #reset()} erases all internal fields. In fact this is now more
	 * a utility class except that we kept the getInstance(). FIXME: this
	 * implies we now HAVE to REUSE THIS INSTANCE every time we launch a MAS and
	 * RESET local fields INSTEAD of simply creating a NEW instance....
	 */
	private static SWIPrologInterface instance = null;
	/**
	 * Contains all databases that are maintained by SWI Prolog. The key is the
	 * owner of the database. The value is a list of databases associated with
	 * that agent. An owner that has no associated databases should be removed
	 * from the map.
	 */
	private Map<String, SWIPrologDatabase> databases = new HashMap<String, SWIPrologDatabase>();
	/**
	 * Properties
	 */
	private final Properties properties;

	/**
	 * Creates new inference engine and empty set of databases.
	 *
	 * @throws KRInitFailedException
	 *             If failed to create inference engine or database.
	 */
	private SWIPrologInterface() throws KRInitFailedException {
		// Initialize inference engine.
		try {
			SWIPrologDatabase.rawquery(JPLUtils.createCompound(
					"set_prolog_flag", new jpl.Atom("debug_on_error"),
					new jpl.Atom("false")));
		} catch (KRQueryFailedException e) {
			throw new KRInitFailedException(e.getMessage(), e);
		}
		// See http://www.swi-prolog.org/packages/jpl/release_notes.html for
		// explanation why Don't Tell Me Mode needs to be false. Setting this
		// mode to false ensures that variables with initial '_' are treated as
		// regular variables.
		jpl.JPL.setDTMMode(false);

		// TODO: This can be removed??
		this.properties = new Properties();
		this.properties.setProperty("EnableJPLQueryBugFix", "true");
	}

	/**
	 * Returns THE SWIPrologLanguage instance.
	 *
	 * @return The instance of {@link SWIPrologInterface}.
	 * @throws KRInitFailedException
	 */
	public static synchronized SWIPrologInterface getInstance()
			throws KRInitFailedException {
		if (SWIPrologInterface.instance == null) {
			SWIPrologInterface.instance = new SWIPrologInterface();
		}
		return SWIPrologInterface.instance;
	}

	/**
	 * @return The name of this KR interface.
	 */
	@Override
	public final String getName() {
		return "swiprolog";
	}

	/**
	 * Returns a database of a particular type associated with a given agent.
	 * <p>
	 * <b>Warning</b>: Cannot be used to get goal bases because in contrast with
	 * all other types of databases an agent can have multiple databases that
	 * are used for storing goals.
	 * </p>
	 *
	 * @param agent
	 *            The name of an agent.
	 * @param type
	 *            The type that is requested.
	 * @returns The database associated with a given agent of a given type, or
	 *          {@code null} if no database of the given type exists.
	 */
	protected SWIPrologDatabase getDatabase(String id) {
		return this.databases.get(id);
	}

	@Override
	public Database getDatabase(Collection<DatabaseFormula> theory)
			throws KRDatabaseException {
		// Create new database of given type, content;
		// use name as base name for name of database.
		SWIPrologDatabase database = new SWIPrologDatabase(theory);
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
	public void removeDatabase(SWIPrologDatabase db) {
		this.databases.remove(db.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parser getParser(Reader r) throws ParserException {
		ANTLRReaderStream cs;
		try {
			cs = new ANTLRReaderStream(r);
		} catch (IOException e) {
			throw new ParserException(e.getMessage());
		}
		return new KRInterfaceParser(cs);
	}

	/**
	 * Computes predicates that need to be declared dynamically for later
	 * reference. A SWI Prolog database assumes that all predicates that are
	 * queried have been either asserted or dynamically declared, otherwise an
	 * existence_error is produced. FIXME: does nothing?!
	 */
	@Override
	public void initialize() throws KRInitFailedException {

	}

	/**
	 * @throws KRDatabaseException
	 */
	@Override
	public void release() throws KRDatabaseException {
		for (SWIPrologDatabase db : this.databases.values()) {
			// TODO: new InfoLog("Taking down database " + getName() + ".\n");
			db.destroy();
		}
		this.databases = new HashMap<String, SWIPrologDatabase>();
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
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		Analyzer analyzer = new Analyzer(dbfs, queries);
		analyzer.analyze();
		return analyzer.getUnused();
	}

	/**
	 * @return The name of this {@link SWIPrologInterface}.
	 */
	@Override
	public String toString() {
		return getName();
	}

}