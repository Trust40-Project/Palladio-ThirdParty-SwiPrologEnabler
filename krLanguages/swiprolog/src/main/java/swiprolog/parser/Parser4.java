package swiprolog.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.dfa.DFA;

import swiprolog.parser.Prolog4Parser.ListtermContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import swiprolog.parser.Prolog4Parser.PrologtextContext;
import swiprolog.parser.Prolog4Parser.Term0Context;
import swiprolog.parser.Prolog4Parser.Term1000Context;
import antlr.build.ANTLR;

/**
 * {@link Prolog4Parser} but stores all errors coming from {@link ANTLR} so that
 * we can later report them. All (unchecked) {@link RecognitionException}s are
 * shielded (actually, never thrown directly from the {@link Prolog4Parser}) and
 * changed to {@link ParserException}s.Also it re-throws if there was an error
 * after parsing. This is needed because {@link Prolog4Parser} does not throw
 * when errors occur, instead it "recovers" and never reports us so. <br>
 * This parser therefore checks the results and re-throws the first exception so
 * that we can handle problems with the normal throw/catch mechanisms higher up.
 * 
 * @author W.Pasman 23apr15
 *
 */
public class Parser4 implements ANTLRErrorListener {

	private Prolog4Parser parser;
	private List<ParserException> errors = new ArrayList<ParserException>();
	private SourceInfo sourceInfo;
	private ANTLRInputStream stream;
	private Prolog4Lexer lexer;

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
	public Parser4(Reader reader, SourceInfo info) throws IOException {
		sourceInfo = info;
		if (sourceInfo == null) {
			sourceInfo = new SourceInfoObject(null, 1, 1, 0, 0);
		}
		stream = new ANTLRInputStream(reader);
		stream.name = (sourceInfo.getSource() == null) ? "" : sourceInfo
				.getSource().getPath();

		lexer = new Prolog4Lexer(stream);
		lexer.setLine(sourceInfo.getLineNumber());
		lexer.setCharPositionInLine(sourceInfo.getCharacterPosition());

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		parser = new Prolog4Parser(tokens);

		lexer.addErrorListener(this);
		parser.addErrorListener(this);
	}

	/**
	 * @return the initial source info field of this parser.
	 * 
	 */
	public SourceInfo getSourceInfo() {
		return sourceInfo;
	}

	/**
	 * Check if we processed the whole stream we were given
	 */
	private void checkEndOfInputReached() {
		if (stream.index() < stream.size()) {
			final SourceInfoObject info = new SourceInfoObject(
					sourceInfo.getSource(), lexer.getLine(),
					lexer.getCharPositionInLine(), 0, 0);
			errors.add(new ParserException("Unrecognized spurious input", info));
		}

	}

	/**
	 * Get the errors that occured during parsing.
	 * 
	 * @return error list.
	 */
	public List<ParserException> getErrors() {
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

	public ParserATNSimulator getInterpreter() {
		return parser.getInterpreter();
	}

	/**
	 * Re-throw the first error, if there occurred an error during the parsing
	 * 
	 * @throws ParserException
	 */
	private void rethrow() throws ParserException {
		if (!errors.isEmpty()) {
			throw errors.get(0);
		}
	}

	/**
	 * Check all input was read and no errors occured.
	 * 
	 * @throws ParserException
	 */
	private void finalChecks() throws ParserException {
		checkEndOfInputReached();
		rethrow();

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

	/*************** Implements {@link ANTLRErrorListener} *******************/
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		SourceInfoObject info = new SourceInfoObject(sourceInfo.getSource(),
				line, charPositionInLine, charPositionInLine,
				charPositionInLine);
		errors.add(new ParserException(msg, info, e));
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex,
			int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		// THIS IS A BUG SITUATION
		throw new IllegalStateException(
				"SWI Prolog parser encountered ambiguity!" + recognizer
						+ " at " + startIndex);

	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, BitSet conflictingAlts,
			ATNConfigSet configs) {
		// THIS IS A BUG SITUATION
		throw new IllegalStateException(
				"SWI Prolog parser encountered restart at full context!"
						+ recognizer + " at " + startIndex);

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
		// THIS IS A BUG SITUATION
		throw new IllegalStateException(
				"SWI Prolog parser encountered context sensitivity!"
						+ recognizer + " at " + startIndex);
	}

	/************** Actual public Parser functionality *******************/
	public Term0Context term0() throws ParserException {
		Term0Context t = parser.term0();
		finalChecks();
		return t;
	}

	public PossiblyEmptyConjunctContext possiblyEmptyConjunct()
			throws ParserException {
		PossiblyEmptyConjunctContext t = parser.possiblyEmptyConjunct();
		finalChecks();
		return t;
	}

	public PrologtextContext prologtext() throws ParserException {
		PrologtextContext t = parser.prologtext();
		finalChecks();
		return t;
	}

	public PossiblyEmptyDisjunctContext possiblyEmptyDisjunct()
			throws ParserException {
		PossiblyEmptyDisjunctContext t = parser.possiblyEmptyDisjunct();
		finalChecks();
		return t;
	}

	public Term1000Context term1000() throws ParserException {
		Term1000Context t = parser.term1000();
		finalChecks();
		return t;
	}

	public ListtermContext listterm() throws ParserException {
		ListtermContext t = parser.listterm();
		finalChecks();
		return t;
	}

}