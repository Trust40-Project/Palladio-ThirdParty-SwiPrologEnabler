/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package swiprolog.language;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Hashtable;

import jpl.Term;
import jpl.Variable;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.language.Substitution;
import krTools.language.Var;

import org.junit.Before;
import org.junit.Test;

public class TestUnification {

	@Before
	public void init() throws KRInitFailedException {
		swiprolog.SWIPrologInterface.getInstance();
	}
	
	/**
	 * Returns a substitution built from given variable and term.
	 *
	 * @param var
	 * @param term
	 * @return
	 * @throws KRInitFailedException
	 */
	public Substitution getSubstitution(PrologVar var, jpl.Term term)
			throws KRInitFailedException {
		Substitution unifier = swiprolog.SWIPrologInterface.getInstance()
				.getSubstitution(new HashMap<Var, krTools.language.Term>());
		unifier.addBinding(var, new PrologTerm(term, null));
		return unifier;
	}

	/**
	 * Test case: unification of basic constants.
	 * 
	 * @throws KRInitFailedException
	 */
	@Test
	public void test1Mgu() throws KRInitFailedException {

		// Construct a (twice)
		jpl.Term a1 = new jpl.Atom("a");
		// jpl.Term a2 = new jpl.Atom("a");
		// Construct b
		jpl.Term b = new jpl.Atom("b");

		// TODO
		// assertEquals(SWIPrologInterface.getInstance().getSubstitution(new
		// HashMap<Var, Term>()).getJPLSolution(),
		// JPLUtils.mgu(a1,a2));
		assertEquals(null, JPLUtils.mgu(a1, b));
	}

	/**
	 * Test case: unification of single variable X with constant a.
	 */
	@Test
	public void test2Mgu() {

		// Construct X
		jpl.Variable x = new jpl.Variable("X");
		// Construct a
		jpl.Atom a = new jpl.Atom("a");

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(x.name(), a);

		assertEquals(unifier, JPLUtils.mgu(x, a));
	}

	/**
	 * Test case: unification of variable X with term f(...) where ... does not
	 * contain X.
	 */
	@Test
	public void test3Mgu() {

		// Construct X
		jpl.Variable x = new jpl.Variable("X");

		// Construct f(a)
		jpl.Term a = new jpl.Atom("a");
		jpl.Term[] args = { a };
		jpl.Term fa = new jpl.Compound("f", args);

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(x.name(), fa);
		assertEquals(unifier, JPLUtils.mgu(x, fa));
		assertEquals(unifier, JPLUtils.mgu(fa, x));

		// Construct f(a, b)
		jpl.Term b = new jpl.Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<String, jpl.Term>();
		unifier2.put(x.name(), fab);
		assertEquals(unifier2, JPLUtils.mgu(x, fab));
		assertEquals(unifier2, JPLUtils.mgu(fab, x));

		// Construct f(Y, b)
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Term[] args3 = { y, b };
		jpl.Term fYb = new jpl.Compound("f", args3);

		Hashtable<String, jpl.Term> unifier3 = new Hashtable<String, jpl.Term>();
		unifier3.put(x.name(), fYb);
		assertEquals(unifier3, JPLUtils.mgu(x, fYb));
		assertEquals(unifier3, JPLUtils.mgu(fYb, x));
	}

	/**
	 * Test case: unification of f(X,Y) and f(a,b) and f(a,b,c).
	 */
	@Test
	public void test4Mgu() {

		// Construct f(X, Y)
		jpl.Variable x = new jpl.Variable("X");
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Term[] args = { x, y };
		jpl.Term fXY = new jpl.Compound("f", args);
		// Construct f(a, b)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Atom b = new jpl.Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);
		jpl.Atom c = new jpl.Atom("c");
		jpl.Term[] args3 = { a, b, c };
		jpl.Term fabc = new jpl.Compound("f", args3);

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(x.name(), a);
		unifier.put(y.name(), b);

		assertEquals(unifier, JPLUtils.mgu(fXY, fab));
		assertEquals(unifier, JPLUtils.mgu(fab, fXY));
		assertEquals(null, JPLUtils.mgu(fXY, fabc));
		assertEquals(null, JPLUtils.mgu(fabc, fXY));
	}

	/**
	 * Test case: unification of f(a, X) and f(Y, Z).
	 */
	@Test
	public void test5Mgu() {

		// Construct f(a, X)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Variable x = new jpl.Variable("X");
		jpl.Term[] args = { a, x };
		jpl.Term faX = new jpl.Compound("f", args);
		// Construct f(Y, Z)
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Variable z = new jpl.Variable("Z");
		jpl.Term[] args2 = { y, z };
		jpl.Term fyz = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(y.name(), a);
		unifier.put(x.name(), z);

		assertEquals(unifier, JPLUtils.mgu(faX, fyz));

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<String, jpl.Term>();
		unifier2.put(y.name(), a);
		unifier2.put(z.name(), x);

		assertEquals(unifier2, JPLUtils.mgu(fyz, faX));
	}

	/**
	 * Test case: unification of f(a, X) and f(X, a).
	 */
	@Test
	public void test6Mgu() {

		// Construct f(a, X)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Variable x = new jpl.Variable("X");
		jpl.Term[] args = { a, x };
		jpl.Term faX = new jpl.Compound("f", args);
		// Construct f(X, a)
		jpl.Term[] args2 = { x, a };
		jpl.Term fXa = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(x.name(), a);

		assertEquals(unifier, JPLUtils.mgu(faX, fXa));
		assertEquals(unifier, JPLUtils.mgu(fXa, faX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, b).
	 */
	@Test
	public void test7Mgu() {

		// Construct f(a, X)
		jpl.Variable x = new jpl.Variable("X");
		jpl.Term[] args = { x, x };
		jpl.Term fXX = new jpl.Compound("f", args);
		// Construct f(X, a)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Atom b = new jpl.Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);

		assertEquals(null, JPLUtils.mgu(fXX, fab));
		assertEquals(null, JPLUtils.mgu(fab, fXX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, Y).
	 */
	@Test
	public void test8Mgu() {

		// Construct f(a, X)
		jpl.Variable x = new jpl.Variable("X");
		jpl.Term[] args = { x, x };
		jpl.Term fXX = new jpl.Compound("f", args);
		// Construct f(X, a)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Term[] args2 = { a, y };
		jpl.Term faY = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<String, jpl.Term>();
		unifier.put(x.name(), a);
		unifier.put(y.name(), a);

		assertEquals(unifier, JPLUtils.mgu(fXX, faY));
		assertEquals(unifier, JPLUtils.mgu(faY, fXX));
	}

	/**
	 * Test case: f(g(Y), X, Y) = f(X, g(a), a).
	 */
	@Test
	public void test9Mgu() {

		// Construct f(g(Y), X, Y)
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Term[] args = { y };
		jpl.Term gY = new jpl.Compound("g", args);
		jpl.Variable x = new jpl.Variable("X");
		jpl.Term[] args2 = { gY, x, y };
		jpl.Term fgYXY = new jpl.Compound("f", args2);
		// Construct f(X, g(a), a)
		jpl.Atom a = new jpl.Atom("a");
		jpl.Term[] args3 = { a };
		jpl.Term ga = new jpl.Compound("g", args3);
		jpl.Term[] args4 = { x, ga, a };
		jpl.Term fXgaa = new jpl.Compound("f", args4);

		jpl.Term[] args5 = { gY, x };
		jpl.Term fgYX = new jpl.Compound("f", args5);
		jpl.Term[] args6 = { x, ga };
		jpl.Term fXga = new jpl.Compound("f", args6);

		Hashtable<String, jpl.Term> unifier1 = new Hashtable<String, jpl.Term>();
		unifier1.put(x.name(), gY);

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<String, jpl.Term>();
		unifier2.put(x.name(), ga);
		unifier2.put(y.name(), a);

		assertEquals(unifier1, JPLUtils.mgu(gY, x));
		assertEquals(unifier2, JPLUtils.mgu(fgYX, fXga));
		// f(g(Y), X, Y) = f(X, g(a), a)
		assertEquals(unifier2, JPLUtils.mgu(fgYXY, fXgaa));
	}

	/**
	 * Test case: unification of f(X, Y) and f(Y, X).
	 */
	@Test
	public void test10Mgu() {

		// Construct f(X, Y)
		jpl.Variable x = new jpl.Variable("X");
		jpl.Variable y = new jpl.Variable("Y");
		jpl.Term[] args = { x, y };
		jpl.Term fXY = new jpl.Compound("f", args);
		// Construct f(Y, X)
		jpl.Term[] args2 = { y, x };
		jpl.Term fYX = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier1 = new Hashtable<String, jpl.Term>();
		unifier1.put(x.name(), y);
		Hashtable<String, jpl.Term> unifier2 = new Hashtable<String, jpl.Term>();
		unifier2.put(y.name(), x);

		assertEquals(unifier1, JPLUtils.mgu(fXY, fYX));
		assertEquals(unifier2, JPLUtils.mgu(fYX, fXY));
	}
	
	/**
	 * Test case: unification of f(X) and f(g(X)) (occurs check should kick in). #3470
	 */
	@Test
	public void test11Mgu() {

		// Construct f(X, Y)
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");
		
		assertEquals(x,x1);
		
		// f(x)
		jpl.Term fX = new jpl.Compound("f", new Term[] { x });
		
		
		// Construct f(g(X))
		jpl.Term fgX = new jpl.Compound("f", new Term[] { new jpl.Compound("g", new Term[] { x1 }) });

		Hashtable<String, Term> result = JPLUtils.mgu(fX, fgX);
		
		assertEquals(null,result);
	}

	/**
	 * Test case: X should match with X. #3469
	 */
	@Test
	public void test12Mgu() {

		// Construct f(X, Y)
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");
		
		Hashtable<String, Term> result = JPLUtils.mgu(x, x1);
		
		assertEquals(new Hashtable<String, jpl.Term>(),result);
	}
	
}
