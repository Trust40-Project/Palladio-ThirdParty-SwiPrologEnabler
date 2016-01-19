package swiprolog.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.LexerATNSimulator;

public class Lexer4 extends Lexer {
	private final Prolog4Lexer lexer;

	public Lexer4(CharStream input, ANTLRErrorListener errorlistener) {
		super(input);
		this.lexer = new Prolog4Lexer(input);
		this.lexer.removeErrorListeners();
		this.lexer.addErrorListener(errorlistener);
	}

	@Override
	public Token nextToken() {
		return this.lexer.nextToken();
	}

	@Override
	public String[] getRuleNames() {
		return this.lexer.getRuleNames();
	}

	@Override
	public String getGrammarFileName() {
		return this.lexer.getGrammarFileName();
	}

	@Override
	public ATN getATN() {
		return this.lexer.getATN();
	}

	@Override
	public LexerATNSimulator getInterpreter() {
		return this.lexer.getInterpreter();
	}
}
