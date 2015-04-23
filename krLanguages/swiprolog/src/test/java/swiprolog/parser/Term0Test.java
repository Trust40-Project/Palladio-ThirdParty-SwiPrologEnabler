package swiprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
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
	private MyProlog4Parser getParser(InputStream textStream)
			throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(textStream);

		Prolog4Lexer lexer = new Prolog4Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		MyProlog4Parser parser = new MyProlog4Parser(tokens);

		return parser;
	}

	@SuppressWarnings("deprecation")
	private MyProlog4Parser getParser(String text) throws IOException {
		return getParser(new StringBufferInputStream(text));
	}

	private void checkParsesAsTerm0(String text) throws IOException {
		MyProlog4Parser parser = getParser(text);
		ParseTree tree = parser.term0();
		parser.checkErrors();
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

class MyProlog4Parser extends Prolog4Parser implements ANTLRErrorListener {

	ArrayList<RecognitionException> exceptions = new ArrayList<RecognitionException>();

	public MyProlog4Parser(CommonTokenStream tokens) {
		super(tokens);
		removeErrorListeners();
		addErrorListener(this);
	}

	public void checkErrors() {
		if (!exceptions.isEmpty()) {
			throw exceptions.get(0);
		}
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		exceptions.add(e);

	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex,
			int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		exceptions.add(new RecognitionException("ambiguity at " + startIndex,
				recognizer, null, null));

	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, BitSet conflictingAlts,
			ATNConfigSet configs) {
		exceptions.add(new RecognitionException("attempting full context at "
				+ startIndex, recognizer, null, null));

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
		exceptions.add(new RecognitionException("context sensitivity at "
				+ startIndex, recognizer, null, null));

	}

}