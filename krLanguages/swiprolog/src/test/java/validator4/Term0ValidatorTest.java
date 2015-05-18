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

package validator4;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.Test;

import swiprolog.SWIPrologInterface;
import swiprolog.language.PrologTerm;
import swiprolog.parser.ErrorStoringProlog4Parser;
import swiprolog.parser.Prolog4Lexer;
import swiprolog.parser.Prolog4Parser.Term0Context;
import visitor.Prolog4Visitor;

/**
 * Tests for Prolog4Parser term0. This is already a kind of end-to-end test as
 * SWI prolog is being used from this point.
 *
 */
public class Term0ValidatorTest {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException
	 *             If the file does not exist.
	 * @throws KRInitFailedException
	 */
	private ErrorStoringProlog4Parser getParser(InputStream textStream)
			throws IOException, KRInitFailedException {

		SWIPrologInterface.getInstance();
		ANTLRInputStream input = new ANTLRInputStream(textStream);

		Prolog4Lexer lexer = new Prolog4Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		ErrorStoringProlog4Parser parser = new ErrorStoringProlog4Parser(tokens);

		// parser report all ambiguities for testing.
		parser.getInterpreter().setPredictionMode(
				PredictionMode.LL_EXACT_AMBIG_DETECTION);
		return parser;
	}

	@SuppressWarnings("deprecation")
	private ErrorStoringProlog4Parser getParser(String text)
			throws IOException, KRInitFailedException {
		return getParser(new StringBufferInputStream(text));
	}

	/**
	 * Default version of {@link #checkValidatesAsTerm0(String, String)} where
	 * input and output are exptected identical.
	 * 
	 * @param text
	 *            the text to parse.
	 * @throws IOException
	 * @throws KRInitFailedException
	 * @throws ParserException
	 */
	private void checkValidatesAsTerm0(String text) throws IOException,
			KRInitFailedException, ParserException {
		checkValidatesAsTerm0(text, text);
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
	private void checkValidatesAsTerm0(String in, String out)
			throws KRInitFailedException, IOException, ParserException {
		ErrorStoringProlog4Parser parser = getParser(in);
		Term0Context tree = parser.term0();
		Prolog4Visitor visitor = new Prolog4Visitor(null);
		PrologTerm term = visitor.visitTerm0(tree);

		System.out.println(in + " -> " + term);
		assertEquals(out, term.toString());
	}

	@Test
	public void testFloat() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("100.3e13", "1.00300002E15");
	}

	@Test
	public void testFloat3() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("0.3e13", "3.00000005E12");
	}

	@Test
	public void testInteger() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("12345");
	}

	@Test(expected = NumberFormatException.class)
	public void testBigInteger() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("123456789012345678901234567890123456789012345678901234567890");
	}

	@Test
	public void testVariable() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("X");
	}

	@Test
	public void testVariable2() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("_123");
	}

	@Test
	public void testString() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("'Aap'");
	}

	@Test
	public void testString1() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("\"Aap\"", "'Aap'");
	}

	@Test
	public void testAtom() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("aap");
	}

	@Test
	public void testString2() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("`Aap`", "'Aap'");
	}

	@Test(expected = NoViableAltException.class)
	public void testString3() throws IOException, KRInitFailedException,
			ParserException {
		checkValidatesAsTerm0("`Aap'");
	}
}
