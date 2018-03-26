package swiprolog.database;

import java.util.Map;

import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
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

	@Before
	public void setUp() throws Exception {

		swi = new SwiPrologInterface();

		for (String know : knowledge.split("\\.")) {

			query(KB, "assert(" + fixAssert(know) + ")");
		}

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
		long start = System.nanoTime();
		for (int n = 0; n < 100; n++) {
			makeBeliefbaseAddImportModule(n);
		}
		long end = System.nanoTime();

		System.out.println("benchmarkAddImport took " + (end - start) + "ns.");
	}

	private void makeBeliefbaseAddImportModule(int n) {
		String base = "bb" + n;
		query(base, "assert(p)"); // create the new module
		query(base, "add_import_module(" + KB + "," + base + ",start)");
	}

	/**
	 * The RH of the assert must be in brackets because :- is infix of arity 2.
	 * So p:-q,r must be converted into p:-(q,r).
	 * 
	 * @param know
	 *            the assertion to be made
	 * @return the assertion but with brackets added if the assertion contains
	 *         :-.
	 */
	private String fixAssert(String know) {
		if (!know.contains(":-"))
			return know;
		return know.replaceFirst(":-", ":- (") + ")";
	}

	/**
	 * Do a query and check the result is OK.
	 * 
	 * @param query
	 *            the string to query
	 */
	private Map<String, Term>[] query(String module, String query) {
		Map<String, Term>[] res;
		try {
			res = new Query(module + ":" + query).allSolutions();
		} catch (PrologException e) {
			throw new IllegalStateException("knowledge query " + query + " failed: ", e);
		}
		if (res.length == 0) {
			throw new IllegalStateException("knowledge query " + query + " returns false");
		}
		return res;

	}
}
