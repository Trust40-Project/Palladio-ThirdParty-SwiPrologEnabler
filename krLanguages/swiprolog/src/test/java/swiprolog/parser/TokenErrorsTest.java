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

import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.Test;

import krTools.exceptions.ParserException;
import swiprolog.errors.ParserErrorMessages;

/**
 * Tests for token errors.
 */
public class TokenErrorsTest {
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

	/**
	 * Check that given parse as prolog text throws the given error message.
	 *
	 * @param text
	 * @param errormess
	 * @throws IOException
	 * @throws ParserException
	 */
	private void failsParseAsProlog(String text, String errormess)
			throws IOException, ParserException {
		Parser4 parser = getParser(text);
		try {
			parser.prologtext();
			throw new IllegalStateException("Parse of " + text
					+ " should have failed");
		} catch (ParserException e) {
			assertEquals(errormess, e.getMessage());
		}
	}

	@Test
	public void testNoEnd() throws IOException, ParserException {
		failsParseAsProlog("kata",
				ParserErrorMessages.TOKEN_MISSING.toReadableString("'.'"));
	}

	@Test
	public void testTwoNames() throws IOException, ParserException {
		failsParseAsProlog("kata kata.",
				ParserErrorMessages.FOUND_BUT_NEED.toReadableString(
						"an atom 'kata'",
						ParserErrorMessages.TERM200.toReadableString()));
	}

	@Test
	public void testExtraNumber() throws IOException, ParserException {
		failsParseAsProlog("kata 1.",
				ParserErrorMessages.FOUND_BUT_NEED.toReadableString(
						"a number '1'",
						ParserErrorMessages.TERM200.toReadableString()));
	}

	@Test
	public void testExtraVariable() throws IOException, ParserException {
		failsParseAsProlog("kata X.",
				ParserErrorMessages.FOUND_BUT_NEED.toReadableString(
						"a variable 'X'",
						ParserErrorMessages.TERM200.toReadableString()));

	}

	@Test
	public void testExtraString() throws IOException, ParserException {
		failsParseAsProlog("kata \"X\".",
				ParserErrorMessages.FOUND_BUT_NEED.toReadableString(
						"a string \"X\"",
						ParserErrorMessages.TERM200.toReadableString()));
	}
}
