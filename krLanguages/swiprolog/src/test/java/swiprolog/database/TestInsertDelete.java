package swiprolog.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import jpl.Atom;
import jpl.Compound;
import krTools.KRInterface;
import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

	private final Atom p1 = new jpl.Atom("p");
	private final Atom p2 = new jpl.Atom("p");
	private final Compound dynamicp = new jpl.Compound("dynamic",
			new jpl.Term[] { p1 });

	@Before
	public void setUp() throws Exception {
		this.language = swiprolog.SWIPrologInterface.getInstance();

		this.knowledgebase = this.language
				.getDatabase(new LinkedHashSet<DatabaseFormula>());

		this.beliefbase = this.language
				.getDatabase(new LinkedHashSet<DatabaseFormula>());

		beliefbase.query(new PrologQuery(dynamicp, null));
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
		return beliefbase.query(new PrologQuery(p1, null));
	}

	@Test
	public void testInsert() throws KRDatabaseException, KRQueryFailedException {

		assertTrue(QueryP().isEmpty());

		beliefbase.insert(new PrologDBFormula(p1, null));

		assertEquals(1, QueryP().size());
	}

	/**
	 * You can insert duplicates. But you won't see them as query returns a SET
	 * 
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testInsertDuplicate() throws KRDatabaseException,
			KRQueryFailedException {

		assertTrue(QueryP().isEmpty());

		beliefbase.insert(new PrologDBFormula(p1, null));
		beliefbase.insert(new PrologDBFormula(p2, null));

		assertEquals(1, QueryP().size());
	}

	/**
	 * Check that delete deletes ALL duplicates.
	 * 
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testDeleteAfterDuplicate() throws KRDatabaseException,
			KRQueryFailedException {
		testInsertDuplicate();
		beliefbase.delete(new PrologDBFormula(p1, null));
		assertTrue(QueryP().isEmpty());
	}

}
