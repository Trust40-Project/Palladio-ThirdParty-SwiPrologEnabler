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

import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;

import jpl.Atom;
import jpl.Compound;
import jpl.Term;
import jpl.Variable;
import krTools.exceptions.KRInitFailedException;
import krTools.language.Substitution;
import swiprolog.SwiPrologInterface;

public class TestUnification2 {
	/**
	 * Returns a substitution built from given variable and term.
	 *
	 * @param var
	 * @param term
	 * @return
	 * @throws KRInitFailedException
	 */
	public Substitution getSubstitution(PrologVar var, jpl.Term term) throws KRInitFailedException {
		SwiPrologInterface swi = new SwiPrologInterface();
		Substitution unifier = swi.getSubstitution(null);
		unifier.addBinding(var, new PrologTerm(term, null));
		return unifier;
	}

	/**
	 * Test case: unification of basic constants.
	 *
	 * @throws KRInitFailedException
	 */
	@Test
	public void test1unify() throws KRInitFailedException {
		// Construct a (twice)
		jpl.Term a1 = new Atom("a");
		// jpl.Term a2 = new Atom("a");
		// Construct b
		jpl.Term b = new Atom("b");

		// TODO
		// assertEquals(SWIPrologInterface.getInstance().getSubstitution(new
		// HashMap<Var, Term>()).getJPLSolution(),
		// JPLUtils.unify(a1,a2));
		assertEquals(null, JPLUtils.mgu(a1, b));
	}

	/**
	 * Test case: unification of single variable X with constant a.
	 */
	@Test
	public void test2unify() {
		// Construct X
		Variable x = new Variable("X");
		// Construct a
		Atom a = new Atom("a");

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(1);
		unifier.put(x.name(), a);

		assertEquals(unifier, JPLUtils.mgu(x, a));
	}

	/**
	 * Test case: unification of variable X with term f(...) where ... does not
	 * contain X.
	 */
	@Test
	public void test3unify() {
		// Construct X
		Variable x = new Variable("X");

		// Construct f(a)
		jpl.Term a = new Atom("a");
		jpl.Term[] args = { a };
		jpl.Term fa = new jpl.Compound("f", args);

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(1);
		unifier.put(x.name(), fa);
		assertEquals(unifier, JPLUtils.mgu(x, fa));
		assertEquals(unifier, JPLUtils.mgu(fa, x));

		// Construct f(a, b)
		jpl.Term b = new Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<>(1);
		unifier2.put(x.name(), fab);
		assertEquals(unifier2, JPLUtils.mgu(x, fab));
		assertEquals(unifier2, JPLUtils.mgu(fab, x));

		// Construct f(Y, b)
		Variable y = new Variable("Y");
		jpl.Term[] args3 = { y, b };
		jpl.Term fYb = new jpl.Compound("f", args3);

		Hashtable<String, jpl.Term> unifier3 = new Hashtable<>(1);
		unifier3.put(x.name(), fYb);
		assertEquals(unifier3, JPLUtils.mgu(x, fYb));
		assertEquals(unifier3, JPLUtils.mgu(fYb, x));
	}

	/**
	 * Test case: unification of f(X,Y) and f(a,b) and f(a,b,c).
	 */
	@Test
	public void test4unify() {
		// Construct f(X, Y)
		Variable x = new Variable("X");
		Variable y = new Variable("Y");
		jpl.Term[] args = { x, y };
		jpl.Term fXY = new jpl.Compound("f", args);
		// Construct f(a, b)
		Atom a = new Atom("a");
		Atom b = new Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);
		Atom c = new Atom("c");
		jpl.Term[] args3 = { a, b, c };
		jpl.Term fabc = new jpl.Compound("f", args3);

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(2);
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
	public void test5unify() {
		// Construct f(a, X)
		Atom a = new Atom("a");
		Variable x = new Variable("X");
		jpl.Term[] args = { a, x };
		jpl.Term faX = new jpl.Compound("f", args);
		// Construct f(Y, Z)
		Variable y = new Variable("Y");
		Variable z = new Variable("Z");
		jpl.Term[] args2 = { y, z };
		jpl.Term fyz = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(2);
		unifier.put(y.name(), a);
		unifier.put(x.name(), z);

		assertEquals(unifier, JPLUtils.mgu(faX, fyz));

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<>(2);
		unifier2.put(y.name(), a);
		unifier2.put(z.name(), x);

		assertEquals(unifier2, JPLUtils.mgu(fyz, faX));
	}

	/**
	 * Test case: unification of f(a, X) and f(X, a).
	 */
	@Test
	public void test6unify() {
		// Construct f(a, X)
		Atom a = new Atom("a");
		Variable x = new Variable("X");
		jpl.Term[] args = { a, x };
		jpl.Term faX = new jpl.Compound("f", args);
		// Construct f(X, a)
		jpl.Term[] args2 = { x, a };
		jpl.Term fXa = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(1);
		unifier.put(x.name(), a);

		assertEquals(unifier, JPLUtils.mgu(faX, fXa));
		assertEquals(unifier, JPLUtils.mgu(fXa, faX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, b).
	 */
	@Test
	public void test7unify() {
		// Construct f(a, X)
		Variable x = new Variable("X");
		jpl.Term[] args = { x, x };
		jpl.Term fXX = new jpl.Compound("f", args);
		// Construct f(X, a)
		Atom a = new Atom("a");
		Atom b = new Atom("b");
		jpl.Term[] args2 = { a, b };
		jpl.Term fab = new jpl.Compound("f", args2);

		assertEquals(null, JPLUtils.mgu(fXX, fab));
		assertEquals(null, JPLUtils.mgu(fab, fXX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, Y).
	 */
	@Test
	public void test8unify() {
		// Construct f(a, X)
		Variable x = new Variable("X");
		jpl.Term[] args = { x, x };
		jpl.Term fXX = new jpl.Compound("f", args);
		// Construct f(X, a)
		Atom a = new Atom("a");
		Variable y = new Variable("Y");
		jpl.Term[] args2 = { a, y };
		jpl.Term faY = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier = new Hashtable<>(2);
		unifier.put(x.name(), a);
		unifier.put(y.name(), a);

		assertEquals(unifier, JPLUtils.mgu(fXX, faY));
		assertEquals(unifier, JPLUtils.mgu(faY, fXX));
	}

	/**
	 * Test case: f(g(Y), X, Y) = f(X, g(a), a).
	 */
	@Test
	public void test9unify() {
		// Construct f(g(Y), X, Y)
		Variable y = new Variable("Y");
		jpl.Term[] args = { y };
		jpl.Term gY = new jpl.Compound("g", args);
		Variable x = new Variable("X");
		jpl.Term[] args2 = { gY, x, y };
		jpl.Term fgYXY = new jpl.Compound("f", args2);
		// Construct f(X, g(a), a)
		Atom a = new Atom("a");
		jpl.Term[] args3 = { a };
		jpl.Term ga = new jpl.Compound("g", args3);
		jpl.Term[] args4 = { x, ga, a };
		jpl.Term fXgaa = new jpl.Compound("f", args4);

		jpl.Term[] args5 = { gY, x };
		jpl.Term fgYX = new jpl.Compound("f", args5);
		jpl.Term[] args6 = { x, ga };
		jpl.Term fXga = new jpl.Compound("f", args6);

		Hashtable<String, jpl.Term> unifier1 = new Hashtable<>(1);
		unifier1.put(x.name(), gY);

		Hashtable<String, jpl.Term> unifier2 = new Hashtable<>(2);
		unifier2.put(x.name(), gY);
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
	public void test10unify() {
		// Construct f(X, Y)
		Variable x = new Variable("X");
		Variable y = new Variable("Y");
		jpl.Term[] args = { x, y };
		jpl.Term fXY = new jpl.Compound("f", args);
		// Construct f(Y, X)
		jpl.Term[] args2 = { y, x };
		jpl.Term fYX = new jpl.Compound("f", args2);

		Hashtable<String, jpl.Term> unifier1 = new Hashtable<>(1);
		unifier1.put(x.name(), y);
		Hashtable<String, jpl.Term> unifier2 = new Hashtable<>(1);
		unifier2.put(y.name(), x);

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
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");

		assertEquals(x, x1);

		// f(x)
		jpl.Term fX = new jpl.Compound("f", new Term[] { x });

		// Construct f(g(X))
		jpl.Term fgX = new jpl.Compound("f", new Term[] { new jpl.Compound("g", new Term[] { x1 }) });

		Map<String, Term> result = JPLUtils.mgu(fX, fgX);

		assertEquals(null, result);
	}

	/**
	 * Test case: X should match with X. #3469
	 */
	@Test
	public void test12unify() {
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");

		Map<String, Term> result = JPLUtils.mgu(x, x1);
		assertEquals(new Hashtable<String, jpl.Term>(0), result);
	}

	/**
	 * Test case: unification of f(X,X) and f(X,X)
	 */
	@Test
	public void test13unify() {
		// Construct f(X, Y)
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");

		// f(x,x)
		jpl.Term fXX = new jpl.Compound("f", new Term[] { x, x1 });

		// Construct f(X, Y)
		Variable x3 = new Variable("X");
		Variable x4 = new Variable("X");

		// f(x,x)
		jpl.Term FXX = new jpl.Compound("f", new Term[] { x3, x4 });

		Map<String, Term> result = JPLUtils.mgu(fXX, FXX);
		assertEquals(new Hashtable<String, jpl.Term>(0), result);
	}

	@Test
	public void testOccursCheck() {
		Compound term1 = new Compound("aap", new Term[] { new Variable("X") });
		Term term2 = new Variable("X");
		Map<String, Term> result = JPLUtils.mgu(term1, term2);
		assertEquals(null, result);
	}

	/**
	 * Test case: aap(1,X) versus aap(2,X)
	 */
	@Test
	public void test14unify() {
		Term one = new Atom("1");
		Term two = new Atom("2");
		Variable x = new Variable("X");
		Variable x1 = new Variable("X");

		Compound aap1 = new Compound("aap", new Term[] { one, x });
		Compound aap2 = new Compound("aap", new Term[] { two, x1 });

		Map<String, Term> result = JPLUtils.mgu(aap1, aap2);

		assertEquals(null, result);
	}

	/**
	 * Test case: aap(1,beer(3)) versus aap(2,beer(3))
	 */
	@Test
	public void test15unify() {
		Term one = new Atom("1");
		Term two = new Atom("2");
		Compound three = new Atom("3");
		Compound three2 = new Atom("3");

		Compound aap1 = new Compound("aap", new Term[] { one, three });
		Compound aap2 = new Compound("aap", new Term[] { two, three2 });

		Map<String, Term> result = JPLUtils.mgu(aap1, aap2);

		assertEquals(null, result);
	}

	/**
	 * Test case: '1' versus 1
	 */
	@Test
	public void testAtom1Int1() {
		Term one = new jpl.Integer(1);
		Term one2 = new Atom("1");

		Map<String, Term> result = JPLUtils.mgu(one, one2);

		assertEquals(null, result);
	}

	/**
	 * Test case: 1 versus '1'
	 */
	@Test
	public void test2Atom1Int1() {
		Term one = new Atom("1");
		Term one2 = new jpl.Integer(1);

		Map<String, Term> result = JPLUtils.mgu(one, one2);

		assertEquals(null, result);
	}
}
