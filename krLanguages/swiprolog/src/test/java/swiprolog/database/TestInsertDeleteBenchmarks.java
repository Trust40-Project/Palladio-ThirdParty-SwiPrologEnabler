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
import swiprolog.language.PrologVar;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologDBFormulaImpl;
import swiprolog.language.impl.PrologIntImpl;
import swiprolog.language.impl.PrologQueryImpl;
import swiprolog.language.impl.PrologVarImpl;

/**
 * Test speed of inserts and deletes in a database.
 */

public class TestInsertDeleteBenchmarks {
	private final static int NINSERTS = 100000;

	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;

	private final PrologVar X = new PrologVarImpl("X", null);
	private final PrologVar Y = new PrologVarImpl("Y", null);
	private final PrologCompound pX = new PrologCompoundImpl("p", new Term[] { this.X }, null);
	private final PrologCompound pXY = new PrologCompoundImpl("p", new Term[] { this.X, this.Y }, null);
	private final PrologCompound dynamicpX = new PrologCompoundImpl("dynamic", new Term[] { this.pX }, null);
	private final PrologCompound dynamicpXY = new PrologCompoundImpl("dynamic", new Term[] { this.pXY }, null);
	// private final Atom listing = new Atom("listing");

	private long start, end;

	@Before
	public void setUp() throws Exception {
		this.language = new SwiPrologInterface();
		this.knowledgebase = this.language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>());
		this.beliefbase.query(new PrologQueryImpl(this.dynamicpX));
		this.beliefbase.query(new PrologQueryImpl(this.dynamicpXY));
	}

	private void start() {
		this.start = System.nanoTime();
	}

	private void end(String name) {
		if (this.start == 0) {
			throw new IllegalStateException("timer has not been started");
		}
		this.end = System.nanoTime();
		System.out.println("Test " + name + " took " + (this.end - this.start) / 1000000 + "ms");
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
	public void uploadBenchmark() throws KRDatabaseException, KRQueryFailedException {
		start();
		uploadGenerator();
		end("uploadBenchmark");
		assertEquals(1, QueryPXY(0).size());
	}

	@Test
	public void insertsBenchmark() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryPX().isEmpty());
		start();
		doInserts();
		end("insertsBenchmark");
		assertEquals(NINSERTS, QueryPX().size());
	}

	@Test
	public void largeInsertBenchmark() throws KRDatabaseException, KRQueryFailedException {
		start();
		this.beliefbase.insert(new PrologDBFormulaImpl(largeTerm(16)));
		end("largeInsertBenchmark");
	}

	@Test
	public void queryBenchmark1() throws KRDatabaseException, KRQueryFailedException {
		doInserts();
		start();
		QueryPX();
		end("queryBenchmark1");
	}

	@Test
	public void queryBenchmark2() throws KRDatabaseException, KRQueryFailedException {
		uploadGenerator();
		start();
		QueryPXY(14);
		end("queryBenchmark2");
	}

	/**
	 * You can insert duplicates. But you won't see them as query returns a SET
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void insertDuplicateBenchmark() throws KRDatabaseException, KRQueryFailedException {
		assertTrue(QueryPX().isEmpty());
		doInserts();
		start();
		doInserts();
		end("insertDuplicateBenchmark");

		assertEquals(NINSERTS, QueryPX().size());
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
		assertTrue(QueryPX().isEmpty());
	}

	/****************************** PRIVATE ***********************/
	private void doInserts() throws KRDatabaseException {
		for (int n = 0; n < NINSERTS; n++) {
			PrologCompound pN = new PrologCompoundImpl("p", new Term[] { new PrologIntImpl(n, null) }, null);
			this.beliefbase.insert(new PrologDBFormulaImpl(pN));
		}
	}

	/**
	 * @param N
	 *            the size
	 * @return large term 2^N elements
	 *
	 */
	private PrologCompound largeTerm(int N) {
		if (N > 0) {
			return new PrologCompoundImpl("p", new Term[] { largeTerm(N - 1), largeTerm(N - 1) }, null);
		} else {
			return new PrologCompoundImpl("p", new Term[] { new PrologIntImpl(0, null), new PrologIntImpl(0, null) },
					null);
		}
	}

	private void doDeletes() throws KRDatabaseException {
		for (int n = 0; n < NINSERTS; n++) {
			PrologCompound pN = new PrologCompoundImpl("p", new Term[] { new PrologIntImpl(n, null) }, null);
			this.beliefbase.delete(new PrologDBFormulaImpl(pN));
		}
	}

	/**
	 *
	 * @return set of solutions for query p(X).
	 */
	private Set<Substitution> QueryPX() throws KRQueryFailedException {
		return this.beliefbase.query(new PrologQueryImpl(this.pX));
	}

	/**
	 * @param N
	 *            the first parameter for N. Determines the size of the returned
	 *            object.
	 * @return set of solutions for query p(N,Y).
	 */
	private Set<Substitution> QueryPXY(int N) throws KRQueryFailedException {
		PrologCompound query = new PrologCompoundImpl("p", new Term[] { new PrologIntImpl(N, null), this.Y }, null);
		return this.beliefbase.query(new PrologQueryImpl(query));
	}

	/**
	 * Prints listing to stdout. For quick test if we have the right formulas in the
	 * database
	 *
	 * @throws KRQueryFailedException
	 */
	// private void listing() throws KRQueryFailedException {
	// System.out.println(this.beliefbase.query(new PrologQuery(this.listing,
	// null)));
	// }

	/**
	 * Upload the large-structure generator into SWI. Returns object that is size
	 * 2^N.
	 *
	 * @throws KRQueryFailedException
	 * @throws KRDatabaseException
	 */
	private void uploadGenerator() throws KRQueryFailedException, KRDatabaseException {
		org.jpl7.Term term1 = org.jpl7.Util.textToTerm("p(0,0)");
		org.jpl7.Term term2 = org.jpl7.Util.textToTerm("p(N,s(X,X)):-( N>0, N1 is N-1, p(N1, X))");
		this.beliefbase.insert(new PrologDBFormulaImpl((PrologCompound) PrologDatabase.fromJpl(term1)));
		this.beliefbase.insert(new PrologDBFormulaImpl((PrologCompound) PrologDatabase.fromJpl(term2)));
		// listing();
	}
}
