package swiprolog.parser;

import java.util.ArrayList;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

/**
 * Prolog4Parser that stores all errors coming from antlr so that we can later
 * report them.
 * 
 * @author W.Pasman 23apr15
 *
 */
public class ErrorStoringProlog4Parser extends Prolog4Parser implements
		ANTLRErrorListener {

	ArrayList<RecognitionException> errors = new ArrayList<RecognitionException>();

	public ErrorStoringProlog4Parser(CommonTokenStream tokens) {
		super(tokens);
		removeErrorListeners();
		addErrorListener(this);
	}

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

}