package swiprolog.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.misc.Interval;

public class Lexer4 extends Lexer {
	private final Prolog4Lexer lexer;

	public Lexer4(CharStream input, ANTLRErrorListener errorlistener) {
		super(input);
		lexer = getNewLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(errorlistener);
	}

	@Override
	public Token nextToken() {
		return lexer.nextToken();
	}

	@Override
	public String[] getRuleNames() {
		return lexer.getRuleNames();
	}

	@Override
	public String getGrammarFileName() {
		return lexer.getGrammarFileName();
	}

	@Override
	public ATN getATN() {
		return lexer.getATN();
	}

	@Override
	public LexerATNSimulator getInterpreter() {
		return lexer.getInterpreter();
	}

	protected Prolog4Lexer getNewLexer(CharStream input) {
		return new Prolog4Lexer(input) {
			@Override
			public void notifyListeners(LexerNoViableAltException e) {
				String text = _input.getText(Interval.of(_tokenStartCharIndex, _input.index()));
				String msg = this.getErrorDisplay(text);
				ANTLRErrorListener listener = getErrorListenerDispatch();
				listener.syntaxError(this, null, _tokenStartLine, _tokenStartCharPositionInLine, msg, e);
			}
		};
	}
}
