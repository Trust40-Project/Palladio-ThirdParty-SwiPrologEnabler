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
import swiprolog.parser.Prolog4Parser.Term1000Context;

/**
 * Tests for Prolog4Parser term900. predicates are term0 but the argument list
 * inside the predicate are term900 elements.
 *
 */
public class Term500Test {
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

	@SuppressWarnings("deprecation")
	private Parser4 getParser(String text) throws IOException {
		return getParser(new StringReader(text));
	}

	/**
	 * Checks that two ':' separated texts (which should be term0 parse-able
	 * texts) are parsed properly.
	 * 
	 * @throws ParserException
	 */
	private void checkParsesAsTerm1000(String text1, String text2)
			throws IOException, ParserException {
		String text = text1 + ":" + text2;
		Parser4 parser = getParser(text1);
		Term1000Context tree = parser.term1000();
		System.out.println(text + " -> " + parser.toStringTree(tree));
		assertEquals(text2, parser.toStringTree(tree));
	}

	@Test
	public void testNotTerm500() throws IOException, ParserException {
		try {
			checkParsesAsTerm1000("X=Y=Z", "");

			throw new IllegalStateException("Unexpected success");
		} catch (ParserException e) {
			assertEquals("Found '=' where we need term with '*', '/' or similar",e.getMessage());
		}
	}
	
	@Test
	public void testGoodTerm500() throws IOException, ParserException {
		// the term500 is after the '=' sign.
			checkParsesAsTerm1000("kat = aap + beer", "(term1000 (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 kat)))))) = (term500 (term400 (term200 (term100 (term50 (term0 aap))))) (term500b + (term400 (term200 (term100 (term50 (term0 beer))))))))))");
	}
}
