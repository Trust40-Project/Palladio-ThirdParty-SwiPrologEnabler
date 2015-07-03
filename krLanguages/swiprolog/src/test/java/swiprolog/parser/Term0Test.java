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

package swiprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import krTools.errors.exceptions.ParserException;

import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.Test;

import swiprolog.parser.Prolog4Parser.Term0Context;

/**
 * Tests for Prolog4Parser term0
 */
public class Term0Test {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException
	 *             If the file does not exist.
	 */
	private Parser4 getParser(Reader textStream) throws IOException {
		Parser4 parser = new Parser4(textStream, null);
		parser.getInterpreter().setPredictionMode(
				PredictionMode.LL_EXACT_AMBIG_DETECTION);
		return parser;
	}

	private Parser4 getParser(String text) throws IOException {
		return getParser(new StringReader(text));
	}

	private void checkParsesAsTerm0(String text) throws IOException,
			ParserException {
		Parser4 parser = getParser(text);
		Term0Context tree = parser.term0();
		System.out.println(text + " -> " + parser.toStringTree(tree));
		assertEquals("(term0 " + text + ")", parser.toStringTree(tree));
	}

	@Test(expected = ParserException.class)
	public void testLexerError() throws IOException, ParserException {
		checkParsesAsTerm0("");
	}

	@Test
	public void testFloat() throws IOException, ParserException {
		checkParsesAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws IOException, ParserException {
		checkParsesAsTerm0("100.3e13");
	}

	@Test
	public void testFloat3() throws IOException, ParserException {
		checkParsesAsTerm0("0.3e13");
	}

	@Test
	public void testInteger() throws IOException, ParserException {
		checkParsesAsTerm0("12345");
	}

	@Test
	public void testBigInteger() throws IOException, ParserException {
		checkParsesAsTerm0("123456789012345678901234567890123456789012345678901234567890");
	}

	@Test
	public void testVariable() throws IOException, ParserException {
		checkParsesAsTerm0("X");
	}

	@Test
	public void testVariable2() throws IOException, ParserException {
		checkParsesAsTerm0("_123");
	}

	@Test
	public void testString() throws IOException, ParserException {
		checkParsesAsTerm0("'Aap'");
	}

	@Test
	public void testString1() throws IOException, ParserException {
		checkParsesAsTerm0("\"Aap\"");
	}

	@Test
	public void testAtom() throws IOException, ParserException {
		checkParsesAsTerm0("aap");
	}

	@Test
	public void testString2() throws IOException, ParserException {
		checkParsesAsTerm0("`Aap`");
	}

	@Test(expected = ParserException.class)
	public void testString3() throws IOException, ParserException {
		checkParsesAsTerm0("`Aap'");
	}
}
