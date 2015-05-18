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
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

/**
 * Tests for Prolog4Parser term0
 *
 */
public class ListTest {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException
	 *             If the file does not exist.
	 */
	private Prolog4Parser getParser(InputStream textStream) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(textStream);

		Prolog4Lexer lexer = new Prolog4Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		Prolog4Parser parser = new Prolog4Parser(tokens);

		// parser report all ambiguities for testing.
		parser.getInterpreter().setPredictionMode(
				PredictionMode.LL_EXACT_AMBIG_DETECTION);
		return parser;
	}

	@SuppressWarnings("deprecation")
	private Prolog4Parser getParser(String text) throws IOException {
		return getParser(new StringBufferInputStream(text));
	}

	/**
	 * Turn list of items into a string, using given separator.
	 * 
	 * @param separator
	 *            separator to use.
	 * @param items
	 *            items for in the list.
	 */
	private String list2String(String separator, String... items) {
		String listtext = "";
		for (String item : items) {
			if (!listtext.isEmpty()) {
				listtext += separator;
			}
			listtext += item;
		}
		return listtext;
	}

	private void checkParsesAsList(String... items) throws IOException,
			RecognitionException {
		String text = "[" + list2String(",", items) + "]";
		Prolog4Parser parser = getParser(text);
		ParseTree tree = parser.listterm();
		System.out.println(text + " -> " + tree.toStringTree(parser));
		assertEquals(parsedList2String(items), tree.toStringTree(parser));
	}

	/**
	 * We should mock this, as now it is really testing too much.
	 * 
	 * @param term
	 * @return
	 */
	private String item2String(String term) {
		return "(expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 "
				+ term + ")))))))))";
	}

	/**
	 * convert a set of items to the expected expression
	 * 
	 * @param items
	 *            items that have been put in list and parsed.
	 */
	private String parsedList2String(String... items) {
		String itemstring = "";
		if (items.length > 0) {
			ArrayList<String> array = new ArrayList<String>();
			for (String item : items) {
				array.add(item);
			}
			itemstring = items2String(array) + " ";
		}

		return "(listterm [ " + itemstring + "])";
	}

	private String items2String(List<String> items) {
		if (items.size() == 1) {
			return "(items " + item2String(items.get(0)) + ")";
		}
		return "(items " + item2String(items.get(0)) + " , "
				+ items2String(items.subList(1, items.size())) + ")";
	}

	@Test
	public void testLisEmptyList() throws IOException {
		checkParsesAsList();
	}

	@Test
	public void testLista() throws IOException {
		checkParsesAsList("a");
	}

	@Test
	public void testListab() throws IOException {
		checkParsesAsList("a", "b");
	}

	@Test
	public void testListabc() throws IOException {
		checkParsesAsList("a", "b", "c");
	}

	// TODO also test lists containing lists and the BAR notation eg [1,2|3]
}
