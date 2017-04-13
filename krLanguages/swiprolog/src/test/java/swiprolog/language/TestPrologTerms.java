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

import org.junit.Before;
import org.junit.Test;

import org.jpl7.Variable;
import krTools.exceptions.KRInitFailedException;
import swiprolog.SwiPrologInterface;

public class TestPrologTerms {

	@Before
	public void init() throws KRInitFailedException {
		new SwiPrologInterface();
	}

	@Test
	public void testToString() {
		org.jpl7.Term term = new org.jpl7.Atom("Aap");
		assertEquals("'Aap'", term.toString());
	}

	/**
	 * JPL lower level test
	 */
	@Test
	public void testEqualVars() {
		Variable X = new Variable("X");
		Variable X1 = new Variable("X");

		assertEquals(X, X1);
	}

	/**
	 * PrologVariable test.
	 */
	@Test
	public void testEqualPrologVars() {
		PrologVar X = new PrologVar(new Variable("X"), null);
		PrologVar X1 = new PrologVar(new Variable("X"), null);

		assertEquals(X, X1);
	}
}
