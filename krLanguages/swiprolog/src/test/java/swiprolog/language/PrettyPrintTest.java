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
import jpl.Compound;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the pretty printing of terms.
 */
public class PrettyPrintTest {

	@Before
	public void setUp() throws Exception {
		swiprolog.SWIPrologInterface.getInstance();
	}

	@Test
	public void printConjunct() {

		jpl.Term[] args = new jpl.Term[] { new jpl.Atom("aap"),
				new jpl.Atom("beer") };
		Compound term = new jpl.Compound(",", args);
		System.out.println("var1 toString=" + term);
		assertEquals("aap , beer", JPLUtils.toString(term));

	}

	@Test
	public void printConjunct3() {

		jpl.Term[] args = new jpl.Term[] { new jpl.Atom("b"), new jpl.Atom("c") };
		jpl.Term[] args2 = new jpl.Term[] { new jpl.Atom("a"),
				new jpl.Compound(",", args) };

		Compound term = new jpl.Compound(",", args2);
		assertEquals("a , b , c", JPLUtils.toString(term));
	}

	@Test
	public void printClause1() {
		jpl.Term[] clauseargs = new jpl.Term[] { new jpl.Atom("head"),
				new jpl.Atom("body") };

		Compound clause = new jpl.Compound(":-", clauseargs);
		assertEquals("head :- body", JPLUtils.toString(clause));
	}

	@Test
	public void printClause3() {

		jpl.Term[] args = new jpl.Term[] { new jpl.Atom("b"), new jpl.Atom("c") };
		jpl.Compound body = new jpl.Compound(",", new jpl.Term[] {
				new jpl.Atom("a"), new jpl.Compound(",", args) });

		jpl.Term[] clauseargs = new jpl.Term[] { new jpl.Atom("head"), body };

		Compound clause = new jpl.Compound(":-", clauseargs);
		assertEquals("head :- a , b , c", JPLUtils.toString(clause));
	}

	@Test
	public void printList() {

		jpl.Term[] args = new jpl.Term[] { new jpl.Atom("aap"),
				new jpl.Atom("[]") };
		Compound term = new jpl.Compound(".", args);
		assertEquals("[aap]", JPLUtils.toString(term));

	}
}