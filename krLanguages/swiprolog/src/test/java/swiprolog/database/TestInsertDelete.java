package swiprolog.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Term;
import org.jpl7.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;

/**
 * Test inserts and deletes in a database.
 */

public class TestInsertDelete {

	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;

	private final Atom p1 = new org.jpl7.Atom("p");
	private final Atom p2 = new org.jpl7.Atom("p");
	private final Compound dynamicp = new org.jpl7.Compound("dynamic", new org.jpl7.Term[] { this.p1 });

	@Before
	public void setUp() throws Exception {
		this.language = new SwiPrologInterface();
		this.knowledgebase = this.language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase.query(new PrologQuery(this.dynamicp, null));
	}

	@After
	public void tearDown() throws Exception {
		if (this.beliefbase != null) {
			this.beliefbase.destroy();
		}
		if (this.knowledgebase != null) {
			this.knowledgebase.destroy();
		}
	}

	/**
	 * Query p
	 *
	 * @return set of solutions for query p.
	 */
	Set<Substitution> QueryP() throws KRQueryFailedException {
		return this.beliefbase.query(new PrologQuery(this.p1, null));
	}

	@Test
	public void testInsert() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryP().isEmpty());

		this.beliefbase.insert(new PrologDBFormula(this.p1, null));

		assertEquals(1, QueryP().size());
	}

	/**
	 * You can insert duplicates. But you won't see them as query returns a SET
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testInsertDuplicate() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryP().isEmpty());

		this.beliefbase.insert(new PrologDBFormula(this.p1, null));
		this.beliefbase.insert(new PrologDBFormula(this.p2, null));

		assertEquals(1, QueryP().size());
	}

	/**
	 * Check that delete deletes ALL duplicates.
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testDeleteAfterDuplicate() throws KRDatabaseException, KRQueryFailedException {
		testInsertDuplicate();
		this.beliefbase.delete(new PrologDBFormula(this.p1, null));
		assertTrue(QueryP().isEmpty());
	}

	@Test
	public void testDatabaseErase() throws KRDatabaseException, KRQueryFailedException {
		String stringterm = "requests([request('INTERACTION', 2, '.'(answer(0, 'OK'), [])),request('INTERACTION', 3, '.'(answer(0, 'OK'), []))])";
		Term t = Util.textToTerm(stringterm);
		this.beliefbase.insert(new PrologDBFormula(t, null));

		Term queryterm = Util.textToTerm("requests(X)");
		Query query = new PrologQuery(queryterm, null);
		assertEquals(1, this.beliefbase.query(query).size());

		this.beliefbase.destroy();
		assertEquals(0, this.beliefbase.query(query).size());

	}
}
