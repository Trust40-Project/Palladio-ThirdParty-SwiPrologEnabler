package swiprolog.visitor;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import krTools.exceptions.ParserException;
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
		parser = p;
		visitor = new Visitor4Internal(p.getSourceInfo());
	}

	/**
	 * Re-throw the first error, if there occurred an error during the parsing
	 *
	 * @throws ParserException
	 */
	private void rethrow() throws ParserException {
		if (!getErrors().isEmpty()) {
			throw getErrors().first();
		}
	}

	/********************
	 * build on top of Visitor4Internal but throwing
	 **********/
	public PrologTerm visitPossiblyEmptyConjunct() throws ParserException {
		PrologTerm t = visitor.visitPossiblyEmptyConjunct(parser.possiblyEmptyConjunct());
		rethrow();
		return t;
	}

	public List<PrologTerm> visitPrologtext() throws ParserException {
		List<PrologTerm> t = visitor.visitPrologtext(parser.prologtext());
		rethrow();
		return t;

	}

	public PrologTerm visitPossiblyEmptyDisjunct() throws ParserException {
		PrologTerm t = visitor.visitPossiblyEmptyDisjunct(parser.possiblyEmptyDisjunct());
		rethrow();
		return t;
	}

	public PrologTerm visitTerm0() throws ParserException {
		PrologTerm t = visitor.visitTerm0(parser.term0());
		rethrow();
		return t;
	}

	public PrologTerm visitTerm1000() throws ParserException {
		PrologTerm t = visitor.visitTerm1000(parser.term1000());
		rethrow();
		return t;

	}

	/**
	 * Get all errors from both visitor and parser
	 *
	 * @return all errors from both visitor and parser
	 */
	public SortedSet<ParserException> getErrors() {
		SortedSet<ParserException> allErrors = new TreeSet<ParserException>();

		allErrors.addAll(visitor.getVisitorErrors());
		allErrors.addAll(parser.getErrors());
		return allErrors;
	}
}