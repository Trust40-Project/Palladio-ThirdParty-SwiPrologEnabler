import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import jpl.PrologException;
import jpl.Query;

import org.junit.Test;

import swiprolog.SwiInstaller;

/**
 * A few basic tests to see if SwiInstaller is working as expected
 * 
 * @author W.Pasman 1dec14
 *
 */
public class BasicQueries {

	static {
		SwiInstaller.init();
	}

	public BasicQueries() {
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
		Query q = new Query("syntax)error");

	}

	@Test
	public void computeQuery() {
		Hashtable[] solutions = new Query("X is 1+1").allSolutions();
		assertEquals(1, solutions.length);
		Hashtable solution = solutions[0];
		assertTrue(solution.containsKey("X"));
		Object result = solution.get("X");
		assertTrue(result instanceof jpl.Integer);
		assertEquals(2, ((jpl.Integer) result).value());
	}
}