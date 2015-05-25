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

package visitor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;

import org.junit.Test;

import swiprolog.parser.Parser4;
import swiprolog.visitor.Visitor4;

/**
 * Tests for Prolog4Parser term0 to see if pipeline parser->visitor works ok.
 * This is already a kind of end-to-end test as we do not stub the parser and
 * also we hook in SWI prolog.
 */
public class FailingTermsTest {

	/**
	 * Test that the parsing the given term throws a {@link ParserException}
	 * exception and RETURNS the {@link ParserException}. This throws an
	 * exception if somethign else happens.
	 *
	 * @param in
	 *            the string to parse
	 * @param out
	 *            the expected result
	 * @throws IOException
	 * @throws KRInitFailedException
	 * @throws ParserException
	 */
	private ParserException checkFailsAsTerm1000(String in) throws IOException {
		Visitor4 visitor = new Visitor4(new Parser4(new StringReader(in), null));
		try {
			visitor.visitTerm1000();
		} catch (ParserException e) {
			return e;
		}
		throw new IllegalStateException("parsing of " + in
				+ " succeeds unexpectedly");
	}

	@Test
	// this fails, I think correctly as {} has 1 parameter: see ISO p. 14. SWI
	// does accept it though?
	public void testEmptyCurlyList() throws IOException, KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000("{}");
		// assertEquals("no viable alternative at input '}'", exc.getMessage());
		assertEquals(1, exc.getLineNumber());
		assertEquals(2, exc.getCharacterPosition());
	}

	@Test
	// :- is term1200 and paramlist holds term1000
	public void testTerm1200InList() throws IOException, KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000("[asserta(bar(X) :- X), clause(bar(X), B)), [[B , call(X)]]]");
		// assertEquals(
		// "mismatched input ':-' expecting {',', ')', '=', '\\=', '==', '\\==', '@<', '@=<', '@>', '@>=', '=@=', '=..', 'is', '/\\', '\\/', '=:=', '=\\=', '<', '<<', '=<', '>', '>>', '>=', '><', '+', '-', '*', '/', '//', 'rem', 'mod', 'xor', 'rdiv'}",
		// exc.getMessage());
	}

	@Test
	// :- is term1200 and paramlist holds term1000
	public void testTerm1200InListB() throws IOException, KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000("assert(a:-b,c)");
		// assertEquals(
		// "mismatched input ':-' expecting {',', ')', '=', '\\=', '==', '\\==', '@<', '@=<', '@>', '@>=', '=@=', '=..', 'is', '/\\', '\\/', '=:=', '=\\=', '<', '<<', '=<', '>', '>>', '>=', '><', '+', '-', '*', '/', '//', 'rem', 'mod', 'xor', 'rdiv'}",
		// exc.getMessage());
	}

	@Test
	// -- does not parse and results in 'extraneous input' message.
	public void testUnknownOperator() throws IOException, KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000(">>> (1)");
		// assertEquals("extraneous input '>' expecting '('", exc.getMessage());
	}

	@Test
	// . % can not accept , as operator.
	public void testListWithoutFirstArgument() throws IOException,
			KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000("[,(var(X), X=1), [[X ]]]");
		// assertEquals("missing ']' at ','", exc.getMessage());
	}

	@Test
	// :- is fx operator, so can have only lower-prio ops on the right.
	public void testDoubleImplication() throws IOException,
			KRInitFailedException {
		ParserException exc = checkFailsAsTerm1000(":- :- a");
		// assertEquals("no viable alternative at input ':-'",
		// exc.getMessage());
		assertEquals(1, exc.getCharacterPosition());
		// CHECK Why isn't this position 3?
	}
}