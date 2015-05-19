package swiprolog.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;

import krTools.errors.exceptions.ParserException;
import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.dfa.DFA;

import swiprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import swiprolog.parser.Prolog4Parser.PrologtextContext;
import swiprolog.parser.Prolog4Parser.Term0Context;
import swiprolog.parser.Prolog4Parser.Term1000Context;

/**
 * Prolog4Parser that stores all errors coming from antlr so that we can later
 * report them. Use similar as {@link Prolog4Parser}. This is needed because
 * {@link Prolog4Parser} does not throw when errors occur, instead it "recovers"
 * and never reports us so. Therefore, we may get back trees that contain hidden
 * null objects. The validator then later can crash on these null objects. The
 * only way to check for parser errors is to listen for them and keep a copy.
 * 
 * This parser therefore checks the results and re-throws the first exception so
 * that we can handle problems with the normal throw/catch mechanisms higher up.
 * 
 * @author W.Pasman 23apr15
 *
 */
public class ErrorStoringProlog4Parser implements ANTLRErrorListener {

	private Prolog4Parser parser;
	private ArrayList<RecognitionException> errors = new ArrayList<RecognitionException>();
	private SourceInfo sourceInfo;

	/**
	 * Constructor. Adjusts the tokeniser input stream position matching the
	 * given start position
	 * 
	 * @param reader
	 *            the input text stream to use for parsing.
	 * @param info
	 *            The start position (line number, column etc) for this parse.
	 *            Used if the text received really is part of a bigger file. If
	 *            set to null, we use a default info object starting at line 1
	 *            with a file reference set to null.
	 * @throws IOException
	 */
	public ErrorStoringProlog4Parser(Reader reader, SourceInfo info)
			throws IOException {

		sourceInfo = info;
		if (sourceInfo == null) {
			sourceInfo = new SourceInfoObject(null, 1, 0, 0, 0);
		}
		ANTLRInputStream stream = new ANTLRInputStream(reader);
		stream.name = (sourceInfo.getSource() == null) ? "" : sourceInfo
				.getSource().getPath();

		Prolog4Lexer lexer = new Prolog4Lexer(stream);
		lexer.setLine(sourceInfo.getLineNumber());
		lexer.setCharPositionInLine(sourceInfo.getCharacterPosition());

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		parser = new Prolog4Parser(tokens);
		parser.addErrorListener(this);
	}

	/**
	 * @return the initial source info field of this parser.
	 * 
	 */
	public SourceInfo getSourceInfo() {
		return sourceInfo;
	}

	// private ErrorStoringProlog4Parser(CommonTokenStream tokens) {
	// parser = new Prolog4Parser(tokens);
	// parser.addErrorListener(this);
	// }

	/**
	 * Get the errors that occured during parsing.
	 * 
	 * @return error list.
	 */
	public ArrayList<RecognitionException> getErrors() {
		return errors;
	}

	/**
	 * Check if parse was a success. To be called after parsing.
	 * 
	 * 
	 * @return true if parsing was a success = no errors.
	 */
	public boolean isSuccess() {
		return errors.isEmpty();
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		errors.add(e); // CHECK store more info?
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex,
			int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		throw new IllegalStateException(
				"SWI Prolog parser encountered ambiguity!" + recognizer
						+ " at " + startIndex);

	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, BitSet conflictingAlts,
			ATNConfigSet configs) {
		throw new IllegalStateException(
				"SWI Prolog parser encountered restart at full context!"
						+ recognizer + " at " + startIndex);

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
		throw new IllegalStateException(
				"SWI Prolog parser encountered context sensitivity!"
						+ recognizer + " at " + startIndex);
	}

	public ParserATNSimulator getInterpreter() {
		// TODO Auto-generated method stub
		return parser.getInterpreter();
	}

	public Term0Context term0() throws ParserException {
		Term0Context t = parser.term0();
		rethrow();
		return t;
	}

	/**
	 * Re-throw the first error, if there occurred an error during the parsing
	 * 
	 * @throws ParserException
	 */
	private void rethrow() throws ParserException {
		if (!errors.isEmpty()) {
			RecognitionException exc = errors.get(0);
			Token token = exc.getOffendingToken();
			// FIXME can we get the original file if there is one?
			SourceInfo info = new SourceInfoObject(null, token.getLine(),
					token.getCharPositionInLine(),
					token.getCharPositionInLine(),
					token.getCharPositionInLine());
			throw new ParserException("error(s) occured while parsing", info,
					exc);
		}
	}

	public PossiblyEmptyConjunctContext possiblyEmptyConjunct()
			throws ParserException {
		PossiblyEmptyConjunctContext t = parser.possiblyEmptyConjunct();
		rethrow();
		return t;
	}

	public PrologtextContext prologtext() throws ParserException {
		PrologtextContext t = parser.prologtext();
		rethrow();
		return t;
	}

	public PossiblyEmptyDisjunctContext possiblyEmptyDisjunct()
			throws ParserException {
		PossiblyEmptyDisjunctContext t = parser.possiblyEmptyDisjunct();
		rethrow();
		return t;
	}

	public Term1000Context term1000() throws ParserException {
		Term1000Context t = parser.term1000();
		rethrow();
		return t;
	}

	/**
	 * Renders string from a parse tree result.
	 * 
	 * @param tree
	 *            {@link ParserRuleContext} to render
	 * @return String represetation of tree.
	 */
	public String toStringTree(ParserRuleContext tree) {
		return tree.toStringTree(parser);
	}

}