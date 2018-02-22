package swiprolog.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Integer;
import org.jpl7.Term;
import org.jpl7.Variable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;

/**
 * Test speed of inserts and deletes in a database.
 */

public class TestInsertDeleteBenchmarks {

	private final static int NINSERTS = 2000;

	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;

	private final Atom p = new Atom("p");
	private final Variable X = new Variable("X");
	private final Compound pX = new Compound("p", new Term[] { X });
	private final Compound dynamicpX = new org.jpl7.Compound("dynamic", new org.jpl7.Term[] { pX });

	private long start, end;

	@Before
	public void setUp() throws Exception {
		this.language = new SwiPrologInterface();
		this.knowledgebase = this.language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase.query(new PrologQuery(this.dynamicpX, null));
	}

	private void start() {
		start = System.nanoTime();
	}

	private void end(String name) {
		if (start == 0) {
			throw new IllegalStateException("timer has not been started");
		}
		end = System.nanoTime();
		System.out.println("Test " + name + " took " + (end - start) + "ns");

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

	@Test
	public void insertBenchmark() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryP().isEmpty());
		start();
		doInserts();
		end("insertBenchmark");
		assertEquals(NINSERTS, QueryP().size());
	}

	@Test
	public void queryBenchmark1() throws KRDatabaseException, KRQueryFailedException {
		doInserts();
		start();
		assertEquals(NINSERTS, QueryP().size());
		end("queryBenchmark1");
	}

	/**
	 * You can insert duplicates. But you won't see them as query returns a SET
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void insertDuplicateBenchmark() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryP().isEmpty());
		doInserts();
		start();
		doInserts();
		end("insertDuplicateBenchmark");

		assertEquals(NINSERTS, QueryP().size());
	}

	/**
	 * Check that delete deletes ALL duplicates.
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testDeletes() throws KRDatabaseException, KRQueryFailedException {

		doInserts();
		start();
		doDeletes();
		end("delete");
		assertTrue(QueryP().isEmpty());
	}

	/****************************** PRIVATE ***********************/
	private void doInserts() throws KRDatabaseException {
		for (int n = 0; n < NINSERTS; n++) {
			Compound pN = new Compound("p", new Term[] { new Integer(n) });
			this.beliefbase.insert(new PrologDBFormula(pN, null));
		}

	}

	private void doDeletes() throws KRDatabaseException {
		for (int n = 0; n < NINSERTS; n++) {
			Compound pN = new Compound("p", new Term[] { new Integer(n) });
			this.beliefbase.delete(new PrologDBFormula(pN, null));
		}

	}

	/**
	 * Query p
	 *
	 * @return set of solutions for query p.
	 */
	private Set<Substitution> QueryP() throws KRQueryFailedException {

		return this.beliefbase.query(new PrologQuery(pX, null));
	}

}
