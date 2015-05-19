package visitor;

import java.util.List;

import krTools.errors.exceptions.ParserException;
import swiprolog.language.PrologTerm;
import swiprolog.parser.ErrorStoringProlog4Parser;

/**
 * Visitor that takes the parser as start point.
 * 
 * @author W.Pasman 23apr15
 *
 */
public class Prolog4VisitorPlus {

	private ErrorStoringProlog4Parser parser;
	private Prolog4Visitor visitor;

	/**
	 * 
	 * @param source
	 *            used only to make correct getSourceInfo references.
	 */
	public Prolog4VisitorPlus(ErrorStoringProlog4Parser p) {
		parser = p;
		visitor = new Prolog4Visitor(p.getSourceInfo().getSource());
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

}