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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import swiprolog.SwiInstaller;

/**
 * Test the parsing implementation of KRInterfaceParser4. e2e test.
 */
@RunWith(Parameterized.class)
public class ParserTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "p(a, b)" }, { "p(a, b , c)" },
				{ "head :- body" }, { "head :- a , b , c" }, { "[p]" },
				{ "[a,b,c]" }, { "bet(-1,1,X)" },
				{ "[[a,b,c],[d,e,f]]" },
				// WE CAN NOT PARSE THIS { "[1,2|4]" },
				{ "head :- X is 1- -1" }, { "head :- X*(-1 , 2)" },
				{ "head :- X is 1+ -(1+2)" }, { "[p(1,2),3]" }, { "p" },
				{ "'.'" }
		// We apparently can't parse terms like "'bla.'" , "p('.'(1,2,3))"
				});
	}

	static {
		SwiInstaller.init();
	}

	private String input;

	public ParserTest(String input) {
		this.input = input;
	}

	@Test
	public void test() throws IOException, ParserException {
		Reader r = new StringReader(input + ".");
		KRInterfaceParser4 parser = new KRInterfaceParser4(r, null);
		List<DatabaseFormula> terms = parser.parseDBFs();
		if (!parser.getErrors().isEmpty()) {
			throw new ParserException("parser error:"
					+ parser.getErrors().get(0));
		}
		if (!parser.getWarnings().isEmpty()) {
			throw new ParserException("parser warning:"
					+ parser.getWarnings().get(0));
		}

		// check the outcome. We glue the terms together again
		String result = "";
		for (DatabaseFormula term : terms) {
			result += term;
		}

		// Term list = Util.textToTerm(input);
		/*
		 * With some terms like "between(-1,1,X), JPLUtils is playing some weird
		 * tricks: it inserts whitespaces where they are not in the original
		 * term, and removes them where they are in the original term.
		 */
		assertEquals(input.replaceAll(" ", ""), result.replaceAll(" ", ""));
	}
}