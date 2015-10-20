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

package tuprolog.language;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;

import alice.tuprolog.Term;
import krTools.language.Substitution;
import tuprolog.TuPrologInterface;

public class TestUnification2 {
	/**
	 * Returns a substitution built from given alice.tuprolog.Var and term.
	 */
	public Substitution getSubstitution(PrologVar var, alice.tuprolog.Term term) throws Exception {
		TuPrologInterface swi = new TuPrologInterface();
		Substitution unifier = swi.getSubstitution(null);
		unifier.addBinding(var, new PrologTerm(term, null));
		return unifier;
	}

	/**
	 * Test case: unification of basic constants.
	 */
	@Test
	public void test1unify() throws Exception {
		// Construct a (twice)
		alice.tuprolog.Term a1 = new alice.tuprolog.Struct("a");
		// Construct b
		alice.tuprolog.Term b = new alice.tuprolog.Struct("b");

		// TODO
		// assertEquals(SWIPrologInterface.getInstance().getSubstitution(new
		// HashMap<Var, Term>()).getJPLSolution(),
		// JPLUtils.unify(a1,a2));
		assertEquals(null, JPLUtils.mgu(a1, b));
	}

	/**
	 * Test case: unification of single alice.tuprolog.Var X with constant a.
	 */
	@Test
	public void test2unify() {
		// Construct X
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		// Construct a
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(1);
		unifier.put(x.getName(), a);

		assertEquals(unifier, JPLUtils.mgu(x, a));
	}

	/**
	 * Test case: unification of alice.tuprolog.Var X with term f(...) where ...
	 * does not contain X.
	 */
	@Test
	public void test3unify() {
		// Construct X
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");

		// Construct f(a)
		alice.tuprolog.Term a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Term[] args = { a };
		alice.tuprolog.Term fa = new alice.tuprolog.Struct("f", args);

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(1);
		unifier.put(x.getName(), fa);
		assertEquals(unifier, JPLUtils.mgu(x, fa));
		assertEquals(unifier, JPLUtils.mgu(fa, x));

		// Construct f(a, b)
		alice.tuprolog.Term b = new alice.tuprolog.Struct("b");
		alice.tuprolog.Term[] args2 = { a, b };
		alice.tuprolog.Term fab = new alice.tuprolog.Struct("f", args2);

		Hashtable<String, alice.tuprolog.Term> unifier2 = new Hashtable<>(1);
		unifier2.put(x.getName(), fab);
		assertEquals(unifier2, JPLUtils.mgu(x, fab));
		assertEquals(unifier2, JPLUtils.mgu(fab, x));

		// Construct f(Y, b)
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term[] args3 = { y, b };
		alice.tuprolog.Term fYb = new alice.tuprolog.Struct("f", args3);

		Hashtable<String, alice.tuprolog.Term> unifier3 = new Hashtable<>(1);
		unifier3.put(x.getName(), fYb);
		assertEquals(unifier3, JPLUtils.mgu(x, fYb));
		assertEquals(unifier3, JPLUtils.mgu(fYb, x));
	}

	/**
	 * Test case: unification of f(X,Y) and f(a,b) and f(a,b,c).
	 */
	@Test
	public void test4unify() {
		// Construct f(X, Y)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term[] args = { x, y };
		alice.tuprolog.Term fXY = new alice.tuprolog.Struct("f", args);
		// Construct f(a, b)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Struct b = new alice.tuprolog.Struct("b");
		alice.tuprolog.Term[] args2 = { a, b };
		alice.tuprolog.Term fab = new alice.tuprolog.Struct("f", args2);
		alice.tuprolog.Struct c = new alice.tuprolog.Struct("c");
		alice.tuprolog.Term[] args3 = { a, b, c };
		alice.tuprolog.Term fabc = new alice.tuprolog.Struct("f", args3);

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(2);
		unifier.put(x.getName(), a);
		unifier.put(y.getName(), b);

		assertEquals(unifier, JPLUtils.mgu(fXY, fab));
		assertEquals(unifier, JPLUtils.mgu(fab, fXY));
		assertEquals(null, JPLUtils.mgu(fXY, fabc));
		assertEquals(null, JPLUtils.mgu(fabc, fXY));
	}

	/**
	 * Test case: unification of f(a, X) and f(Y, Z).
	 */
	@Test
	public void test5unify() {
		// Construct f(a, X)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Term[] args = { a, x };
		alice.tuprolog.Term faX = new alice.tuprolog.Struct("f", args);
		// Construct f(Y, Z)
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Var z = new alice.tuprolog.Var("Z");
		alice.tuprolog.Term[] args2 = { y, z };
		alice.tuprolog.Term fyz = new alice.tuprolog.Struct("f", args2);

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(2);
		unifier.put(y.getName(), a);
		unifier.put(x.getName(), z);

		assertEquals(unifier, JPLUtils.mgu(faX, fyz));

		Hashtable<String, alice.tuprolog.Term> unifier2 = new Hashtable<>(2);
		unifier2.put(y.getName(), a);
		unifier2.put(z.getName(), x);

		assertEquals(unifier2, JPLUtils.mgu(fyz, faX));
	}

	/**
	 * Test case: unification of f(a, X) and f(X, a).
	 */
	@Test
	public void test6unify() {
		// Construct f(a, X)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Term[] args = { a, x };
		alice.tuprolog.Term faX = new alice.tuprolog.Struct("f", args);
		// Construct f(X, a)
		alice.tuprolog.Term[] args2 = { x, a };
		alice.tuprolog.Term fXa = new alice.tuprolog.Struct("f", args2);

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(1);
		unifier.put(x.getName(), a);

		assertEquals(unifier, JPLUtils.mgu(faX, fXa));
		assertEquals(unifier, JPLUtils.mgu(fXa, faX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, b).
	 */
	@Test
	public void test7unify() {
		// Construct f(a, X)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Term[] args = { x, x };
		alice.tuprolog.Term fXX = new alice.tuprolog.Struct("f", args);
		// Construct f(X, a)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Struct b = new alice.tuprolog.Struct("b");
		alice.tuprolog.Term[] args2 = { a, b };
		alice.tuprolog.Term fab = new alice.tuprolog.Struct("f", args2);

		assertEquals(null, JPLUtils.mgu(fXX, fab));
		assertEquals(null, JPLUtils.mgu(fab, fXX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, Y).
	 */
	@Test
	public void test8unify() {
		// Construct f(a, X)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Term[] args = { x, x };
		alice.tuprolog.Term fXX = new alice.tuprolog.Struct("f", args);
		// Construct f(X, a)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term[] args2 = { a, y };
		alice.tuprolog.Term faY = new alice.tuprolog.Struct("f", args2);

		Hashtable<String, alice.tuprolog.Term> unifier = new Hashtable<>(2);
		unifier.put(x.getName(), a);
		unifier.put(y.getName(), a);

		assertEquals(unifier, JPLUtils.mgu(fXX, faY));
		assertEquals(unifier, JPLUtils.mgu(faY, fXX));
	}

	/**
	 * Test case: f(g(Y), X, Y) = f(X, g(a), a).
	 */
	@Test
	public void test9unify() {
		// Construct f(g(Y), X, Y)
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term[] args = { y };
		alice.tuprolog.Term gY = new alice.tuprolog.Struct("g", args);
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Term[] args2 = { gY, x, y };
		alice.tuprolog.Term fgYXY = new alice.tuprolog.Struct("f", args2);
		// Construct f(X, g(a), a)
		alice.tuprolog.Struct a = new alice.tuprolog.Struct("a");
		alice.tuprolog.Term[] args3 = { a };
		alice.tuprolog.Term ga = new alice.tuprolog.Struct("g", args3);
		alice.tuprolog.Term[] args4 = { x, ga, a };
		alice.tuprolog.Term fXgaa = new alice.tuprolog.Struct("f", args4);

		alice.tuprolog.Term[] args5 = { gY, x };
		alice.tuprolog.Term fgYX = new alice.tuprolog.Struct("f", args5);
		alice.tuprolog.Term[] args6 = { x, ga };
		alice.tuprolog.Term fXga = new alice.tuprolog.Struct("f", args6);

		Hashtable<String, alice.tuprolog.Term> unifier1 = new Hashtable<>(1);
		unifier1.put(x.getName(), gY);

		Hashtable<String, alice.tuprolog.Term> unifier2 = new Hashtable<>(2);
		unifier2.put(x.getName(), gY);
		unifier2.put(y.getName(), a);

		assertEquals(unifier1, JPLUtils.mgu(gY, x));
		assertEquals(unifier2, JPLUtils.mgu(fgYX, fXga));
		// f(g(Y), X, Y) = f(X, g(a), a)
		assertEquals(unifier2, JPLUtils.mgu(fgYXY, fXgaa));
	}

	/**
	 * Test case: unification of f(X, Y) and f(Y, X).
	 */
	@Test
	public void test10unify() {
		// Construct f(X, Y)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term[] args = { x, y };
		alice.tuprolog.Term fXY = new alice.tuprolog.Struct("f", args);
		// Construct f(Y, X)
		alice.tuprolog.Term[] args2 = { y, x };
		alice.tuprolog.Term fYX = new alice.tuprolog.Struct("f", args2);

		Hashtable<String, alice.tuprolog.Term> unifier1 = new Hashtable<>(1);
		unifier1.put(x.getName(), y);
		Hashtable<String, alice.tuprolog.Term> unifier2 = new Hashtable<>(1);
		unifier2.put(y.getName(), x);

		assertEquals(unifier1, JPLUtils.mgu(fXY, fYX));
		assertEquals(unifier2, JPLUtils.mgu(fYX, fXY));
	}

	/**
	 * Test case: unification of f(X) and f(g(X)) (occurs check should kick in).
	 * #3470
	 */
	@Test
	public void test11unify() {
		// Construct f(X, Y)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var x1 = new alice.tuprolog.Var("X");

		assertEquals(x, x1);

		// f(x)
		alice.tuprolog.Term fX = new alice.tuprolog.Struct("f", new Term[] { x });

		// Construct f(g(X))
		alice.tuprolog.Term fgX = new alice.tuprolog.Struct("f",
				new Term[] { new alice.tuprolog.Struct("g", new Term[] { x1 }) });

		Map<String, Term> result = JPLUtils.mgu(fX, fgX);

		assertEquals(null, result);
	}

	/**
	 * Test case: X should match with X. #3469
	 */
	@Test
	public void test12unify() {
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var x1 = new alice.tuprolog.Var("X");

		Map<String, Term> result = JPLUtils.mgu(x, x1);
		assertEquals(new Hashtable<String, alice.tuprolog.Term>(0), result);
	}

	/**
	 * Test case: unification of f(X,X) and f(X,X)
	 */
	@Test
	public void test13unify() {
		// Construct f(X, Y)
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var x1 = new alice.tuprolog.Var("X");

		// f(x,x)
		alice.tuprolog.Term fXX = new alice.tuprolog.Struct("f", new Term[] { x, x1 });

		// Construct f(X, Y)
		alice.tuprolog.Var x3 = new alice.tuprolog.Var("X");
		alice.tuprolog.Var x4 = new alice.tuprolog.Var("X");

		// f(x,x)
		alice.tuprolog.Term FXX = new alice.tuprolog.Struct("f", new Term[] { x3, x4 });

		Map<String, Term> result = JPLUtils.mgu(fXX, FXX);
		assertEquals(new Hashtable<String, alice.tuprolog.Term>(0), result);
	}

	@Test
	public void testOccursCheck() {
		alice.tuprolog.Struct term1 = new alice.tuprolog.Struct("aap", new Term[] { new alice.tuprolog.Var("X") });
		Term term2 = new alice.tuprolog.Var("X");
		Map<String, Term> result = JPLUtils.mgu(term1, term2);
		assertEquals(null, result);
	}

	/**
	 * Test case: aap(1,X) versus aap(2,X)
	 */
	@Test
	public void test14unify() {
		Term one = new alice.tuprolog.Struct("1");
		Term two = new alice.tuprolog.Struct("2");
		alice.tuprolog.Var x = new alice.tuprolog.Var("X");
		alice.tuprolog.Var x1 = new alice.tuprolog.Var("X");

		alice.tuprolog.Struct aap1 = new alice.tuprolog.Struct("aap", new Term[] { one, x });
		alice.tuprolog.Struct aap2 = new alice.tuprolog.Struct("aap", new Term[] { two, x1 });

		Map<String, Term> result = JPLUtils.mgu(aap1, aap2);

		assertEquals(null, result);
	}

	/**
	 * Test case: aap(1,beer(3)) versus aap(2,beer(3))
	 */
	@Test
	public void test15unify() {
		Term one = new alice.tuprolog.Struct("1");
		Term two = new alice.tuprolog.Struct("2");
		alice.tuprolog.Struct three = new alice.tuprolog.Struct("3");
		alice.tuprolog.Struct three2 = new alice.tuprolog.Struct("3");

		alice.tuprolog.Struct aap1 = new alice.tuprolog.Struct("aap", new Term[] { one, three });
		alice.tuprolog.Struct aap2 = new alice.tuprolog.Struct("aap", new Term[] { two, three2 });

		Map<String, Term> result = JPLUtils.mgu(aap1, aap2);

		assertEquals(null, result);
	}

	/**
	 * Test case: '1' versus 1
	 */
	@Test
	public void Struct1Int1() {
		Term one = new alice.tuprolog.Int(1);
		Term one2 = new alice.tuprolog.Struct("1");

		Map<String, Term> result = JPLUtils.mgu(one, one2);

		assertEquals(null, result);
	}

	/**
	 * Test case: 1 versus '1'
	 */
	@Test
	public void Int1Struct1() {
		Term one = new alice.tuprolog.Struct("1");
		Term one2 = new alice.tuprolog.Int(1);

		Map<String, Term> result = JPLUtils.mgu(one, one2);

		assertEquals(null, result);
	}
}
