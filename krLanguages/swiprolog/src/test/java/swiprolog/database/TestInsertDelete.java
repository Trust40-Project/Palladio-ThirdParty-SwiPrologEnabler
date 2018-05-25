package swiprolog.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Term;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;

/**
 * Test inserts and deletes in a database.
 */
public class TestInsertDelete {
	// components enabling us to run the tests...
	private static KRInterface language = new SwiPrologInterface();
	private Database beliefbase;
	private Database knowledgebase;
	// beliefs
	private PrologCompound p1;
	private PrologCompound p2;
	private PrologCompound dynamicp;

	@Before
	public void setUp() throws Exception {
		this.knowledgebase = language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>(), true);
		this.beliefbase = language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(), false);
		this.p1 = PrologImplFactory.getAtom("p", null);
		this.p2 = PrologImplFactory.getAtom("p", null);
		this.dynamicp = PrologImplFactory.getCompound("dynamic", new Term[] { this.p1 }, null);
		this.beliefbase.query(PrologImplFactory.getQuery(this.dynamicp));
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
		return this.beliefbase.query(PrologImplFactory.getQuery(this.p1));
	}

	@Test
	public void testInsert() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryP().isEmpty());

		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p1));

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

		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p1));
		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p2));

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
		this.beliefbase.delete(PrologImplFactory.getDBFormula(this.p1));
		assertTrue(QueryP().isEmpty());
	}

	@Test
	public void testDatabaseErase() throws KRDatabaseException, KRQueryFailedException {
		String stringterm = "requests([request('INTERACTION', 2,'.'(answer(0, 'OK'), [])),request('INTERACTION', 3, '.'(answer(0,'OK'), []))])";
		org.jpl7.Term t = org.jpl7.Util.textToTerm(stringterm);
		this.beliefbase.insert(PrologImplFactory.getDBFormula((PrologCompound) PrologDatabase.fromJpl(t)));

		org.jpl7.Term queryterm = org.jpl7.Util.textToTerm("requests(X)");
		PrologQuery query = PrologImplFactory.getQuery((PrologCompound) PrologDatabase.fromJpl(queryterm));
		assertEquals(1, this.beliefbase.query(query).size());

		this.beliefbase.destroy();
		assertEquals(0, this.beliefbase.query(query).size());
	}
}
