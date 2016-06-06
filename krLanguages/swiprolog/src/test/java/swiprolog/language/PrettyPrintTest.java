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
import jpl.Atom;
import jpl.Compound;
import jpl.Term;
import jpl.Util;

import org.junit.Test;

import swiprolog.SwiInstaller;

/**
 * Test the pretty printing of terms.
 */
public class PrettyPrintTest {

	static {
		SwiInstaller.init();
	}

	private final static Atom EMPTYLIST = new Atom("[]");

	@Test
	public void printConjunct() {
		Term[] args = new Term[] { new Atom("aap"), new Atom("beer") };
		Compound term = new Compound(",", args);
		System.out.println("var1 toString=" + term);
		assertEquals("aap , beer", JPLUtils.toString(term));

	}

	@Test
	public void printConjunct3() {
		Term[] args = new Term[] { new Atom("b"), new Atom("c") };
		Term[] args2 = new Term[] { new Atom("a"), new Compound(",", args) };

		Compound term = new Compound(",", args2);
		assertEquals("a , b , c", JPLUtils.toString(term));
	}

	@Test
	public void printClause1() {
		Term[] clauseargs = new Term[] { new Atom("head"), new Atom("body") };

		Compound clause = new Compound(":-", clauseargs);
		assertEquals("head :- body", JPLUtils.toString(clause));
	}

	@Test
	public void printClause3() {
		Term[] args = new Term[] { new Atom("b"), new Atom("c") };
		Compound body = new Compound(",", new Term[] { new Atom("a"),
				new Compound(",", args) });

		Term[] clauseargs = new Term[] { new Atom("head"), body };

		Compound clause = new Compound(":-", clauseargs);
		assertEquals("head :- a , b , c", JPLUtils.toString(clause));
	}

	@Test
	public void printList() {
		Term[] args = new Term[] { new Atom("p"), new Atom("[]") };
		Compound term = new Compound(".", args);
		assertEquals("[p]", JPLUtils.toString(term));
	}

	@Test
	public void printLongerList() {
		Term list = Util.textToTerm("[a,b,c]");
		assertEquals("[a,b,c]", JPLUtils.toString(list));
	}

	@Test
	public void printListOfList() {
		String input = "[[a,b,c],[d,e,f]]";
		Term list = Util.textToTerm(input);
		assertEquals(input, JPLUtils.toString(list));
	}

}