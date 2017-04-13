package swiprolog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;
import org.junit.Test;

/**
 * A few basic tests to see if SwiInstaller is working as expected
 *
 * @author W.Pasman 1dec14
 */
public class BasicQueriesTest {
	static {
		SwiInstaller.init();
	}

	@Test
	public void simpleQuery() {
		new Query("X is 1+1").hasSolution();
	}

	@Test
	public void printAllPrologSettings() {
		new Query("current_prolog_flag(K,V),write(K-V),nl,fail").oneSolution();
	}

	@Test(expected = PrologException.class)
	public void syntaxError() {
		new Query("syntax)error");
	}

	@Test
	public void computeQuery() {
		Map<String, Term>[] solutions = new Query("X is 1+1").allSolutions();
		assertEquals(1, solutions.length);
		Map<String, Term> solution = solutions[0];
		assertTrue(solution.containsKey("X"));
		Term result = solution.get("X");
		assertTrue(result instanceof org.jpl7.Integer);
		assertEquals(2, ((org.jpl7.Integer) result).intValue());
	}

	@Test
	public void varIsInfinity() {
		org.jpl7.Float inf = new org.jpl7.Float(Double.POSITIVE_INFINITY);
		System.out.println("infinity term: " + inf);

		Variable x = new org.jpl7.Variable("X");
		Query query = new Query(new org.jpl7.Compound("is", new org.jpl7.Term[] { x, inf }));
		Map<String, Term>[] result = query.allSolutions();
		System.out.println("query " + query + "->" + result[0]);
	}

	@Test
	public void divByInfinity() {
		org.jpl7.Float inf = new org.jpl7.Float(Double.POSITIVE_INFINITY);
		org.jpl7.Float ten = new org.jpl7.Float(10.0);
		Variable x = new org.jpl7.Variable("X");

		Query query = new Query(new org.jpl7.Compound("is",
				new org.jpl7.Term[] { x, new org.jpl7.Compound("/", new org.jpl7.Term[] { ten, inf }) }));
		Map<String, Term>[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

	@Test
	public void testMember() {
		Variable x = new org.jpl7.Variable("X");
		Term list = new Compound(".", new Term[] { new org.jpl7.Float(1.1),
				new Compound(".", new Term[] { new org.jpl7.Float(2.2), new Atom("[]") }) });

		Query query = new Query(new org.jpl7.Compound("member", new org.jpl7.Term[] { x, list }));

		// Query query = new Query("member(X,[1.1,2.2]");
		Map<String, Term>[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

	// @SuppressWarnings("rawtypes")
	// @Test FIXME: fails on Linux
	public void testRandom() {
		Variable x = new org.jpl7.Variable("X");

		Query query = new Query(new Compound("random", new Term[] { x }));

		// Query query = new Query("member(X,[1.1,2.2]");
		Map<String, Term>[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

}