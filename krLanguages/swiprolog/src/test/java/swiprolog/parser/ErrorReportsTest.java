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
import java.io.StringReader;

import org.junit.Test;

import krTools.exceptions.ParserException;
import swiprolog.errors.ParserErrorMessages;

/**
 * Tests the error reports coming from the {@link Parser4}.
 */
public class ErrorReportsTest {
	/**
	 * Creates parser to parse given text.
	 *
	 * @return The {@link Parser4} for the text.
	 */
	private Parser4 getParser(String text) throws IOException {
		return new Parser4(new StringReader(text), null);
	}

	/**
	 * Check that we have implemented all prettyprint rules in
	 * prettyPrintRuleContext.
	 */
	@Test
	public void checkAllTokensTranslated() {
		ErrorStrategy4 strat = new ErrorStrategy4();
		for (int n = 0; n < Prolog4Parser.ruleNames.length; n++) {
			strat.prettyPrintRuleContext(n);
		}
	}

	@Test
	public void testSpuriousText() throws IOException, ParserException {
		// term0 will not eat the second number.
		try {
			getParser("100.3 200 ").term0();
		} catch (ParserException e) {
			assertEquals(1, e.getLineNumber());
			assertEquals(10, e.getCharacterPosition()); // end of next token
		}
	}

	@Test
	public void testSpuriousText2() throws IOException {
		try {
			getParser("\n\n\n100.3 200 ").term0();
		} catch (ParserException e) {
			assertEquals(4, e.getLineNumber());
		}
	}

	@Test
	public void testBadText() throws IOException {
		Parser4 parser = getParser("ç");
		try {
			parser.term0();
		} catch (ParserException e) {
			System.out.println("errors:" + parser.getErrors());
			assertEquals(ParserErrorMessages.CANNOT_BE_USED.toReadableString("ç"), e.getMessage());
		}
	}
}
