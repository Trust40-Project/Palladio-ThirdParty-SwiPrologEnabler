package swiprolog.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swiprolog.SwiPrologInterface;

/**
 * Benchmark speed of importing/cloning a (knowledge) module into some other
 * module.
 */
public class BenchmarkImportModule {

	// from tictactoe
	private final static String knowledge = "pos(Pos) :- between(1, 9, Pos)." + "free(Pos) :- pos(Pos, empty)."
			+ "free(List, Pos) :- member(Pos, List), pos(Pos,empty)."
			+ "corner(1). corner(3). corner(7). corner(9). center(5)."
			+ "line(A, B, C) :- pos(A), pos(B), pos(C), B is A+1, C is B+1, 0 is C mod 3."
			+ "line(A, B, C) :- pos(A), pos(B), pos(C), B is A+3, C is B+3." + "line(1, 5, 9). line(3, 5, 7)."
			+ "winning_line(Player,A,B,C) :- me(P), line(A, B, C), pos(A, P), pos(B, P), pos(C, P)."
			+ "winning_move(Player, Pos) :- line(A, B, C), pos(A, Pa), pos(B, Pb), pos(C, Pc),"
			+ "count([Pa,Pb,Pc], Player, 2), free([A, B, C], Pos)."
			+ "possible_winning_move(Player, [A, B, C], Pos) :- line(A, B, C), pos(A, Pa), pos(B, Pb), pos(C, Pc),"
			+ "count([Pa,Pb,Pc], Player, 1), count([Pa,Pb,Pc], empty, 2), free([A, B, C], Pos)."
			+ "fork(Player, Pos) :- possible_winning_move(Player, [A, B, C], Pos),"
			+ "possible_winning_move(Player, [D, E, F], Pos), intersection([A, B, C], [D, E, F], L), not(length(L, 3))."
			+ "count([], A, 0)." + "count([A|T], A, C) :- count(T, A, TC), C is TC+1."
			+ "count([B|T], A, C) :- not(A=B), count(T, A, C)." + "opponent(x) :- me(o)." + "opponent(o) :- me(x).";

	private String KB = "kb";
	private String DB = "db";
	private SwiPrologInterface swi;
	private final int NTESTS = 100;

	private List<Term> kbterms = new ArrayList<>();

	private long start;

	@Before
	public void setUp() throws Exception {

		swi = new SwiPrologInterface();

		for (String know : knowledge.split("\\.")) {
			kbterms.add(Util.textToTerm(know));
		}
		insertAllKnowledge(KB);
	}

	@After
	public void tearDown() throws Exception {
		swi.release();
	}

	@Test
	public void testKB() {
		query(KB, "corner(9)");
	}

	@Test
	public void benchmarkAddImport() {
		start();
		for (int n = 0; n < NTESTS; n++) {
			makeBeliefbaseAddImportModule("bb" + n);
		}
		end("benchmarkAddImport");
	}

	@Test
	public void benchmarkInsertImport() {
		start();
		for (int n = 0; n < NTESTS; n++) {
			insertAllKnowledge("bb" + n);
		}
		end("benchmarkInsertImport");
	}

	/*********************** support functions *****************/
	private void start() {
		start = System.nanoTime();

	}

	private void end(String string) {
		long end = System.nanoTime();
		double time = (end - start) / NTESTS; // ns
		System.out.println(string + " took on average " + time / 1000000 + "ms.");
	}

	/**
	 * Create new module with name "bb<n>" and "insert" knowledge by using
	 * "add_import_module".
	 * 
	 * @param base
	 *            the module name to create
	 */
	private void makeBeliefbaseAddImportModule(String base) {
		query(base, "assert(p)"); // create the new module
		query(base, "add_import_module(" + KB + "," + base + ",start)");
	}

	/**
	 * Create new module with name "bb<n>" and "insert" knowledge by inserting
	 * all knowledge one by one.
	 * 
	 * @param n
	 *            the number of the module
	 */

	private void insertAllKnowledge(String module) {
		for (Term knowledge : kbterms) {
			insert(module, knowledge);
		}

	}

	/**
	 * Insert knowledge in database
	 * 
	 * @param know
	 */
	private void insert(String module, Term know) {
		Compound queryterm = new Compound("assert",
				new Term[] { new Compound(":", new Term[] { new Atom(module), know }) });
		query(new Query(queryterm));
	}

	/**
	 * Do a query and check the result is OK.
	 * 
	 * @param query
	 *            the string to query
	 */
	private Map<String, Term>[] query(String module, String query) {
		return query(new Query(module + ":" + query));

	}

	/**
	 * Do a query and check the result is OK.
	 * 
	 * @param query
	 *            the string to query
	 */
	private Map<String, Term>[] query(Query query) {
		Map<String, Term>[] res;

		try {
			res = query.allSolutions();
		} catch (PrologException e) {
			throw new IllegalStateException("knowledge query " + query + " failed: ", e);
		}
		if (res.length == 0) {
			throw new IllegalStateException("knowledge query " + query + " returns false");
		}
		return res;

	}

}
