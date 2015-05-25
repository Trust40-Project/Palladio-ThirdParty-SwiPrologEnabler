package swiprolog.validator;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

import swiprolog.parser.Prolog4Parser;

public class ErrorStrategy4 extends DefaultErrorStrategy {
	@Override
	public void reportNoViableAlternative(Parser parser, NoViableAltException e)
			throws RecognitionException {
		parser.notifyErrorListeners(e.getOffendingToken(),
				getExpectationTxt((Parser) e.getRecognizer()),
				getException("NoViableAlternative", parser));
	}

	@Override
	public void reportInputMismatch(Parser parser, InputMismatchException e) {
		parser.notifyErrorListeners(e.getOffendingToken(),
				getExpectationTxt((Parser) e.getRecognizer()),
				getException("InputMismatch", parser));
	}

	@Override
	public void reportFailedPredicate(Parser parser, FailedPredicateException e) {
		parser.notifyErrorListeners(e.getOffendingToken(),
				getExpectationTxt((Parser) e.getRecognizer()),
				getException("FailedPredicate", parser));
	}

	/**
	 * This method is called to report a syntax error which requires the removal
	 * of a token from the input stream. At the time this method is called, the
	 * erroneous symbol is current {@code LT(1)} symbol and has not yet been
	 * removed from the input stream. When this method returns, {@code parser}
	 * is in error recovery mode.
	 *
	 * <p>
	 * This method is called when {@link #singleTokenDeletion} identifies
	 * single-token deletion as a viable recovery strategy for a mismatched
	 * input error.
	 * </p>
	 *
	 * <p>
	 * Like the default implementation this method simply returns if the handler
	 * is already in error recovery mode. Otherwise, it calls
	 * {@link #beginErrorCondition} to enter error recovery mode, followed by
	 * calling {@link Parser#notifyErrorListeners}.
	 * </p>
	 *
	 * <p>
	 * The method has been modified to report more readable error messages.
	 * </p>
	 *
	 * @param parser
	 *            the parser instance
	 */
	@Override
	public void reportUnwantedToken(Parser parser) {
		if (!inErrorRecoveryMode(parser)) {
			beginErrorCondition(parser);
			Token t = parser.getCurrentToken();
			parser.notifyErrorListeners(t, getExpectationTxt(parser),
					getException("UnwantedToken", parser));
		}
	}

	/**
	 * This method is called to report a syntax error which requires the
	 * insertion of a missing token into the input stream. At the time this
	 * method is called, the missing token has not yet been inserted. When this
	 * method returns, {@code recognizer} is in error recovery mode.
	 *
	 * <p>
	 * This method is called when {@link #singleTokenInsertion} identifies
	 * single-token insertion as a viable recovery strategy for a mismatched
	 * input error.
	 * </p>
	 *
	 * <p>
	 * The default implementation simply returns if the handler is already in
	 * error recovery mode. Otherwise, it calls {@link #beginErrorCondition} to
	 * enter error recovery mode, followed by calling
	 * {@link Parser#notifyErrorListeners}.
	 * </p>
	 *
	 * @param recognizer
	 *            the parser instance
	 */
	@Override
	public void reportMissingToken(Parser parser) {
		if (!inErrorRecoveryMode(parser)) {
			beginErrorCondition(parser);
			Token t = parser.getCurrentToken();
			parser.notifyErrorListeners(t, getExpectationTxt(parser),
					getException("MissingToken", parser));
		}
	}

	/**
	 * Used to control display of token in an error message.
	 *
	 * During development it may be useful to use t.toString() (which, for
	 * CommonToken, dumps everything about the token).
	 */
	@Override
	public String getTokenErrorDisplay(Token t) {
		if (t == null) {
			return "????";
		}

		// Default is to use token name from list in parser
		String s = getSymbolText(t);
		if (s == null) {
			s = "<" + getSymbolType(t) + ">";
		} else {
			s = escapeWSAndQuote(s.toLowerCase());
		}

		// Handle specific cases to produce more readable output
		String prettyprint = prettyPrintToken(t);
		return (prettyprint != null) ? prettyprint : s;
	}

	/**
	 * Helper method for reporting multiple expected alternatives
	 *
	 * @param tokens
	 *            Set of expected tokens
	 * @return String representation of token set
	 */
	private String getExpectationTxt(Parser parser) {
		IntervalSet tokens = getExpectedTokens(parser);
		if (tokens.size() < 5) { // list all expected tokens if less than 5
			int size = tokens.toList().size();
			String str = (size > 1 ? "either " : "");
			for (int i = 0; i < size; i++) {
				int type = tokens.toList().get(i);
				str += prettyPrintToken(type);
				str += (i < size - 2 ? ", " : "");
				str += (i == size - 2 ? " or " : "");
			}
			return str;
		} else { // otherwise output parser rule context
			return prettyPrintRuleContext(parser.getRuleContext()
					.getRuleIndex());
		}
	}

	/**
	 * We use a general RecognitionException with a particular text to signal to
	 * the error strategy what type of issue we found.
	 *
	 * @param text
	 *            Label to indicate error type
	 * @param parser
	 * @return The recognition exception
	 */
	private RecognitionException getException(String text, Parser parser) {
		return new RecognitionException(text, parser, parser.getInputStream(),
				parser.getRuleContext());
	}

	protected String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case Prolog4Parser.VARIABLE:
			return txt + " '" + t.getText() + "'";
			// TODO: other cases?!
		default:
			return txt;
		}
	}

	protected String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file";
			// TODO: other cases?!
		default:
			// Do not improve, simply return token symbol as is
			if (type < Prolog4Parser.tokenNames.length) {
				return Prolog4Parser.tokenNames[type];
			} else {
				return "something that does not make sense";
			}
		}
	}

	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		// TODO: other cases?!
		default:
			return Prolog4Parser.ruleNames[ruleIndex];
		}
	}
}
