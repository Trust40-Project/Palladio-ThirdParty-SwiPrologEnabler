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

import swiprolog.language.PrologTerm;
import swiprolog.parser.Parser4;

/**
 * Tests for Prolog4Parser term0 to see if pipeline parser->visitor works ok.
 * This is already a kind of end-to-end test as we do not stub the parser and
 * also we hook in SWI prolog.
 *
 */
public class Term0Test {

	/**
	 * Default version of {@link #checkVisitesAsTerm0(String, String)} where
	 * input and output are exptected identical.
	 * 
	 * @param text
	 *            the text to parse.
	 * @throws IOException
	 * @throws KRInitFailedException
	 * @throws ParserException
	 */
	private void checkVisitsAsTerm0(String text) throws IOException,
			KRInitFailedException, ParserException {
		checkVisitesAsTerm0(text, text);
	}

	/**
	 * Test that the parsed(in) {@link PrologTerm}.toString == out
	 * 
	 * @param in
	 *            the string to parse
	 * @param out
	 *            the expected result
	 * @throws IOException
	 * @throws KRInitFailedException
	 * @throws ParserException
	 */
	private void checkVisitesAsTerm0(String in, String out)
			throws KRInitFailedException, IOException, ParserException {
		Visitor4 visitor = new Visitor4(
				new Parser4(new StringReader(in), null));
		PrologTerm term = visitor.visitTerm0();

		System.out.println(in + " -> " + term);
		assertEquals(out, term.toString());
	}

	@Test
	public void testFloat() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitesAsTerm0("100.3e13", "1.00300002E15");
	}

	@Test
	public void testFloat3() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitesAsTerm0("0.3e13", "3.00000005E12");
	}

	@Test
	public void testInteger() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("12345");
	}

	@Test(expected = NumberFormatException.class)
	public void testBigInteger() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("123456789012345678901234567890123456789012345678901234567890");
	}

	@Test
	public void testVariable() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("X");
	}

	@Test
	public void testVariable2() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("_123");
	}

	@Test
	public void testString() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("'Aap'");
	}

	@Test
	public void testString1() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitesAsTerm0("\"Aap\"", "'Aap'");
	}

	@Test
	public void testAtom() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("aap");
	}

	@Test
	public void testString2() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitesAsTerm0("`Aap`", "'Aap'");
	}

	@Test(expected = ParserException.class)
	public void testString3() throws IOException, KRInitFailedException,
			ParserException {
		checkVisitsAsTerm0("`Aap'");
	}
}
