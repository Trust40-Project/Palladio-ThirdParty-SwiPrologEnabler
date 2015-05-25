package swiprolog.visitor;

import java.util.List;
import java.util.SortedSet;

import krTools.errors.exceptions.ParserException;
import swiprolog.language.PrologTerm;
import swiprolog.parser.Parser4;

/**
 * Visitor that converts a parse tree coming from an {@link Parser4} into a
 * {@link PrologTerm}.<br>
 * <h1>example</h1> example parsing a term0<br>
 * * <code>
		visitor = new Visitor4(
				new ErrorStoringProlog4Parser(new StringReader("term"), null));<br>
		PrologTerm term = visitor.visitTerm0();<br>

 * </code>
 *
 * @author W.Pasman 23apr15
 */
public class Visitor4 {
	private final Parser4 parser;
	private final Visitor4Internal visitor;

	/**
	 * @param parser
	 *            a {@link Parser4}
	 */
	public Visitor4(Parser4 p) {
		this.parser = p;
		this.visitor = new Visitor4Internal(p.getSourceInfo());
	}

	public PrologTerm visitPossiblyEmptyConjunct() throws ParserException {
		return this.visitor.visitPossiblyEmptyConjunct(this.parser
				.possiblyEmptyConjunct());
	}

	public List<PrologTerm> visitPrologtext() throws ParserException {
		return this.visitor.visitPrologtext(this.parser.prologtext());

	}

	public PrologTerm visitPossiblyEmptyDisjunct() throws ParserException {
		return this.visitor.visitPossiblyEmptyDisjunct(this.parser
				.possiblyEmptyDisjunct());
	}

	public PrologTerm visitTerm0() throws ParserException {
		return this.visitor.visitTerm0(this.parser.term0());
	}

	public PrologTerm visitTerm1000() throws ParserException {
		return this.visitor.visitTerm1000(this.parser.term1000());
	}

	public SortedSet<ParserException> getErrors() {
		return this.parser.getErrors();
	}
}