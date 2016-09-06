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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jpl.Float;
import jpl.PrologException;
import jpl.Util;
import swiprolog.SwiInstaller;
import swiprolog.database.PrologDatabase;

public class TestUtils {
	static {
		SwiInstaller.init();
	}

	@Test
	public void testJPLAtom() throws Exception {
		jpl.Atom constant = new jpl.Atom("Aap");
		jpl.Term term = JPLUtils.createCompound("var", constant);
		assertTrue(PrologDatabase.rawquery(term).isEmpty());
	}

	@Test
	public void testJPLFloat() throws Exception {
		Float constant = new jpl.Float(-1.2);
		// bit weird but let's check anyway...
		assertEquals("-1.2/0", JPLUtils.getSignature(constant));
	}

	@Test
	public void testPredicateIndicator1() throws Exception {
		assertTrue(JPLUtils.isPredicateIndicator(Util.textToTerm("p/1")));
	}

	@Test
	public void testPredicateIndicator2() throws Exception {
		// numbers are not atoms.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("12/1")));
	}

	@Test
	public void testPredicateIndicator3() throws Exception {
		// protected keywords are not checked against.
		assertTrue(JPLUtils.isPredicateIndicator(Util.textToTerm("asserta/1")));
	}

	@Test
	public void testPredicateIndicator4() throws Exception {
		// vars are not ok
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("X/2")));
	}

	@Test(expected = PrologException.class)
	public void testPredicateIndicator5() throws Exception {
		// 2nd term must be positive number. Actually the parser fails on this.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("p/-2")));
	}

	@Test
	public void testPredicateIndicator6() throws Exception {
		// 2nd term must be number.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("p/q")));
	}

	@Test
	public void testPredicateIndicator7() throws Exception {
		// the '/' is obligatory.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("p")));
	}

	@Test
	public void testPredicateIndicator8() throws Exception {
		// the first argument must be atom, not predicate.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("p(1) / 3")));
	}

	@Test
	public void testPredicateIndicator9() throws Exception {
		// / takes 2 arguments, not 3.
		assertFalse(JPLUtils.isPredicateIndicator(Util.textToTerm("/(1,2,3)")));
	}

}
