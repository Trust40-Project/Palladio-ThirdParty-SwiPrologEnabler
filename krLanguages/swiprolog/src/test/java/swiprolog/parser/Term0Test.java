package swiprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class Term0Test {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException
	 *             If the file does not exist.
	 */
	private ErrorStoringProlog4Parser getParser(InputStream textStream)
			throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(textStream);

		Prolog4Lexer lexer = new Prolog4Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		ErrorStoringProlog4Parser parser = new ErrorStoringProlog4Parser(tokens);

		return parser;
	}

	@SuppressWarnings("deprecation")
	private ErrorStoringProlog4Parser getParser(String text) throws IOException {
		return getParser(new StringBufferInputStream(text));
	}

	private void checkParsesAsTerm0(String text) throws IOException {
		ErrorStoringProlog4Parser parser = getParser(text);
		ParseTree tree = parser.term0();
		if (!parser.getErrors().isEmpty()) {
			throw parser.getErrors().get(0);
		}
		System.out.println(text + " -> " + tree.toStringTree(parser));
		assertEquals("(term0 " + text + ")", tree.toStringTree(parser));
	}

	@Test
	public void testFloat() throws IOException {
		checkParsesAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws IOException {
		checkParsesAsTerm0("100.3e13");
	}

	@Test
	public void testFloat3() throws IOException {
		checkParsesAsTerm0("0.3e13");
	}

	@Test
	public void testInteger() throws IOException {
		checkParsesAsTerm0("12345");
	}

	@Test
	public void testBigInteger() throws IOException {
		checkParsesAsTerm0("123456789012345678901234567890123456789012345678901234567890");
	}

	@Test
	public void testVariable() throws IOException {
		checkParsesAsTerm0("X");
	}

	@Test
	public void testVariable2() throws IOException {
		checkParsesAsTerm0("_123");
	}

	@Test
	public void testString() throws IOException {
		checkParsesAsTerm0("'Aap'");
	}

	@Test
	public void testString1() throws IOException {
		checkParsesAsTerm0("\"Aap\"");
	}

	@Test
	public void testString2() throws IOException {
		checkParsesAsTerm0("`Aap`");
	}

	@Test(expected = NoViableAltException.class)
	public void testString3() throws IOException {
		checkParsesAsTerm0("`Aap'");
	}
}
