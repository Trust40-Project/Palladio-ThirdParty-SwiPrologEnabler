package swiprolog.validator;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import jpl.Variable;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

import swiprolog.language.JPLUtils;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import swiprolog.parser.ErrorStoringProlog4Parser;
import swiprolog.parser.Prolog4Lexer;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import swiprolog.parser.Prolog4Parser.PrologtextContext;
import swiprolog.parser.Prolog4Parser.Term0Context;
import swiprolog.parser.Prolog4Parser.Term1000Context;
import visitor.Prolog4Visitor;

/**
 * Prolog4Validator that parses and validates. All errors occuring during parse
 * or validation are thrown.
 * 
 * @author W.Pasman 18may15
 *
 */
public class Prolog4Validator {

	private ErrorStoringProlog4Parser parser;
	private Prolog4Visitor visitor;

	/**
	 * @param reader
	 *            the data to be parsed and validated.
	 * 
	 * @param file
	 *            a file reference for use in error reports. Not used for the
	 *            input stream.
	 * @throws IOException
	 */
	public Prolog4Validator(Reader reader, SourceInfo info) throws IOException {

		ANTLRInputStream stream = new ANTLRInputStream(reader);
		stream.name = (info.getSource() == null) ? "" : info.getSource()
				.getPath();

		Prolog4Lexer lexer = new Prolog4Lexer(stream);
		lexer.setLine(info.getLineNumber());
		lexer.setCharPositionInLine(info.getCharacterPosition());

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		parser = new ErrorStoringProlog4Parser(tokens);
		visitor = new Prolog4Visitor(info.getSource());
	}

	/**
	 * Parses an update or empty term. Or null if there is parser error.
	 * 
	 * @return {@link Update} or null if there is error.
	 * @throws RecognitionException
	 *             , ParserException
	 */
	public Update parseUpdateOrEmpty() throws RecognitionException,
			ParserException {
		PossiblyEmptyConjunctContext tree = parser.possiblyEmptyConjunct();
		PrologTerm term = visitor.visitPossiblyEmptyConjunct(tree);
		return SemanticTools.conj2Update(term);

	}

	/**
	 * Parses a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> parsePrologProgram()
			throws RecognitionException, ParserException {
		PrologtextContext tree = parser.prologtext();
		List<PrologTerm> prologTerms = visitor.visitPrologtext(tree);

		// Parser does not check if Prolog terms are correct database
		// objects, do this next
		List<DatabaseFormula> dbfs = new ArrayList<DatabaseFormula>(
				prologTerms.size());
		for (PrologTerm t : prologTerms) {
			PrologTerm updatedSourceInfo = new PrologTerm(t.getTerm(),
					t.getSourceInfo());
			dbfs.add(SemanticTools.DBFormula(updatedSourceInfo));
		}
		return dbfs;
	}

	/**
	 * Parse a section that should contain Prolog goals, i.e., queries.
	 *
	 * @return List<Query>, or {@code null} if a parser error occurs.
	 * @throws ParserException
	 */
	public List<Query> parsePrologGoalSection() throws ParserException,
			RecognitionException {
		PrologtextContext tree = parser.prologtext();
		List<PrologTerm> prologTerms = visitor.visitPrologtext(tree);
		List<Query> goals = new ArrayList<Query>(prologTerms.size());
		for (PrologTerm t : prologTerms) {
			// check that each term is a valid Prolog goal / query
			goals.add(new PrologQuery(SemanticTools.toGoal(t.getTerm(),
					t.getSourceInfo()), t.getSourceInfo()));
		}
		return goals;
	}

	/**
	 * Parses a (possibly empty) query.
	 *
	 * @return A {@link PrologQuery}, or {@code null} if an error occurred.
	 * @throws ParserException
	 */
	public PrologQuery ParseQueryOrEmpty() throws ParserException,
			RecognitionException {
		PossiblyEmptyDisjunctContext tree = parser.possiblyEmptyDisjunct();
		PrologTerm term = visitor.visitPossiblyEmptyDisjunct(tree);
		return SemanticTools.toQuery(term);
	}

	/**
	 * Parses the input. Returns a variable obtained by parsing the input.
	 * 
	 * @return {@link Var}
	 * @throws ParserException
	 * @throws RecognitionException
	 */
	public Var parseVar() throws ParserException, RecognitionException {
		PrologTerm term = ParseTerm();

		if (!term.isVar()) {
			throw new ParserException(String.format(
					"expected a SWI prolog variable but found '%s'",
					term.toString()), term.getSourceInfo());
		}

		return new PrologVar((Variable) term.getTerm(), term.getSourceInfo());
	}

	/**
	 * try parse a term
	 * 
	 * @return term, or null if error occurs
	 */
	public PrologTerm ParseTerm() {
		Term0Context tree = parser.term0();
		return visitor.visitTerm0(tree);
	}

	/**
	 * Parse a set of parameters.
	 *
	 * @return A list of {@link Term}s.
	 */
	public List<Term> ParsePrologTerms() {
		Term1000Context tree = parser.term1000();
		PrologTerm t = visitor.visitTerm1000(tree);

		List<jpl.Term> original = JPLUtils.getOperands(",", t.getTerm());
		List<Term> terms = new ArrayList<Term>(original.size());
		for (jpl.Term term : original) {
			if (term instanceof jpl.Variable) {
				terms.add(new PrologVar((jpl.Variable) term, t.getSourceInfo()));
			} else {
				terms.add(new PrologTerm(term, t.getSourceInfo()));
			}
		}
		return terms;
	}
}