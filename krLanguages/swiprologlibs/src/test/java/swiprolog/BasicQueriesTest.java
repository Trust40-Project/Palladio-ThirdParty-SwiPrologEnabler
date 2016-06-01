package swiprolog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import jpl.Atom;
import jpl.Compound;
import jpl.PrologException;
import jpl.Query;
import jpl.Term;
import jpl.Variable;

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

	@SuppressWarnings("rawtypes")
	@Test
	public void computeQuery() {
		Hashtable[] solutions = new Query("X is 1+1").allSolutions();
		assertEquals(1, solutions.length);
		Hashtable solution = solutions[0];
		assertTrue(solution.containsKey("X"));
		Object result = solution.get("X");
		assertTrue(result instanceof jpl.Integer);
		assertEquals(2, ((jpl.Integer) result).intValue());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void varIsInfinity() {
		jpl.Float inf = new jpl.Float(Double.POSITIVE_INFINITY);
		System.out.println("infinity term: " + inf);

		Variable x = new jpl.Variable("X");
		Query query = new Query(new jpl.Compound("is",
				new jpl.Term[] { x, inf }));
		Hashtable[] result = query.allSolutions();
		System.out.println("query " + query + "->" + result[0]);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void divByInfinity() {
		jpl.Float inf = new jpl.Float(Double.POSITIVE_INFINITY);
		jpl.Float ten = new jpl.Float(10.0);
		Variable x = new jpl.Variable("X");

		Query query = new Query(new jpl.Compound("is", new jpl.Term[] { x,
				new jpl.Compound("/", new jpl.Term[] { ten, inf }) }));
		Hashtable[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testMember() {
		Variable x = new jpl.Variable("X");
		Term list = new Compound(".", new Term[] {
				new jpl.Float(1.1),
				new Compound(".", new Term[] { new jpl.Float(2.2),
						new Atom("[]") }) });

		Query query = new Query(new jpl.Compound("member", new jpl.Term[] { x,
				list }));

		// Query query = new Query("member(X,[1.1,2.2]");
		Hashtable[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRandom() {
		Variable x = new jpl.Variable("X");

		Query query = new Query(new Compound("random", new Term[] { x }));

		// Query query = new Query("member(X,[1.1,2.2]");
		Hashtable[] result1 = query.allSolutions();
		System.out.println("query " + query + "->" + result1[0]);
	}

}