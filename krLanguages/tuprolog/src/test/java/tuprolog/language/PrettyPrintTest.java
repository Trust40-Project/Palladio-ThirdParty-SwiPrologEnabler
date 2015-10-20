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

import org.junit.Test;

/**
 * Test the pretty printing of terms.
 */
public class PrettyPrintTest {
	@Test
	public void printConjunct() {
		alice.tuprolog.Term[] args = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("aap"),
				new alice.tuprolog.Struct("beer") };
		alice.tuprolog.Struct term = new alice.tuprolog.Struct(",", args);
		System.out.println("var1 toString=" + term);
		assertEquals("aap , beer", JPLUtils.toString(term));

	}

	@Test
	public void printConjunct3() {
		alice.tuprolog.Term[] args = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("b"),
				new alice.tuprolog.Struct("c") };
		alice.tuprolog.Term[] args2 = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("a"),
				new alice.tuprolog.Struct(",", args) };

		alice.tuprolog.Struct term = new alice.tuprolog.Struct(",", args2);
		assertEquals("a , b , c", JPLUtils.toString(term));
	}

	@Test
	public void printClause1() {
		alice.tuprolog.Term[] clauseargs = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("head"),
				new alice.tuprolog.Struct("body") };

		alice.tuprolog.Struct clause = new alice.tuprolog.Struct(":-", clauseargs);
		assertEquals("head :- body", JPLUtils.toString(clause));
	}

	@Test
	public void printClause3() {
		alice.tuprolog.Term[] args = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("b"),
				new alice.tuprolog.Struct("c") };
		alice.tuprolog.Struct body = new alice.tuprolog.Struct(",",
				new alice.tuprolog.Term[] { new alice.tuprolog.Struct("a"), new alice.tuprolog.Struct(",", args) });

		alice.tuprolog.Term[] clauseargs = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("head"), body };

		alice.tuprolog.Struct clause = new alice.tuprolog.Struct(":-", clauseargs);
		assertEquals("head :- a , b , c", JPLUtils.toString(clause));
	}

	@Test
	public void printList() {
		alice.tuprolog.Term[] args = new alice.tuprolog.Term[] { new alice.tuprolog.Struct("aap"),
				new alice.tuprolog.Struct("[]") };
		alice.tuprolog.Struct term = new alice.tuprolog.Struct(".", args);
		assertEquals("[aap]", JPLUtils.toString(term));
	}
}