package visitor;

import java.util.List;

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
 *
 */
public class Visitor4 {

	private Parser4 parser;
	private Visitor4Internal visitor;

	/**
	 * 
	 * @param source
	 *            used only to make correct getSourceInfo references.
	 */
	public Visitor4(Parser4 p) {
		parser = p;
		visitor = new Visitor4Internal(p.getSourceInfo().getSource());
	}

	public PrologTerm visitPossiblyEmptyConjunct() throws ParserException {
		return visitor.visitPossiblyEmptyConjunct(parser
				.possiblyEmptyConjunct());
	}

	public List<PrologTerm> visitPrologtext() throws ParserException {
		return visitor.visitPrologtext(parser.prologtext());

	}

	public PrologTerm visitPossiblyEmptyDisjunct() throws ParserException {
		return visitor.visitPossiblyEmptyDisjunct(parser
				.possiblyEmptyDisjunct());
	}

	public PrologTerm visitTerm0() throws ParserException {
		return visitor.visitTerm0(parser.term0());
	}

	public PrologTerm visitTerm1000() throws ParserException {
		return visitor.visitTerm1000(parser.term1000());
	}

	public List<ParserException> getErrors() {
		return parser.getErrors();
	}

}