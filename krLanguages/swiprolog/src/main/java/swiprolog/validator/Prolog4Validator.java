package swiprolog.validator;

import java.util.ArrayList;
import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;

import org.antlr.v4.runtime.RecognitionException;

import swiprolog.language.JPLUtils;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import visitor.Prolog4VisitorPlus;

/**
 * Prolog4Validator that parses and validates. All errors occuring during parse
 * or validation are thrown.
 * 
 * Usage example:
 * <code>PrologTerm term = validator("aap(1)").ParseTerm();</code>
 * 
 * @author W.Pasman 18may15
 *
 */
public class Prolog4Validator {

	private Prolog4VisitorPlus visitor;

	/**
	 * @param reader
	 *            the data to be parsed and validated.
	 * 
	 * @param file
	 *            a file reference for use in error reports. Not used for the
	 *            input stream.
	 * @throws IOException
	 */
	// public Prolog4Validator(Reader reader, SourceInfo info) throws
	// IOException {
	//
	// ANTLRInputStream stream = new ANTLRInputStream(reader);
	// stream.name = (info.getSource() == null) ? "" : info.getSource()
	// .getPath();
	//
	// Prolog4Lexer lexer = new Prolog4Lexer(stream);
	// lexer.setLine(info.getLineNumber());
	// lexer.setCharPositionInLine(info.getCharacterPosition());
	//
	// CommonTokenStream tokens = new CommonTokenStream(lexer);
	//
	// parser = new ErrorStoringProlog4Parser(tokens);
	// visitor = new Prolog4Visitor(info.getSource());
	// }

	/**
	 * 
	 * @param visitor
	 *            the {@link Prolog4VisitorPlus} (that contains the parser)
	 */
	public Prolog4Validator(Prolog4VisitorPlus vis) {
		visitor = vis;
	}

	/**
	 * Parses an update or empty term. Or null if there is parser error.
	 * 
	 * @return {@link Update} or null if there is error.
	 * @throws RecognitionException
	 *             , ParserException
	 */
	public Update parseUpdateOrEmpty() throws ParserException {
		return SemanticTools.conj2Update(visitor.visitPossiblyEmptyConjunct());
	}

	public Update parseUpdateOrEmpty(String text) throws RecognitionException,
			ParserException {
		return SemanticTools.conj2Update(visitor.visitPossiblyEmptyConjunct());
	}

	/**
	 * Parses a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> parsePrologProgram()
			throws RecognitionException, ParserException {

		List<PrologTerm> prologTerms = visitor.visitPrologtext();

		List<DatabaseFormula> dbfs = new ArrayList<DatabaseFormula>(
				prologTerms.size());
		for (PrologTerm t : prologTerms) {
			dbfs.add(SemanticTools.DBFormula(t));
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
		List<Query> goals = new ArrayList<Query>();
		for (PrologTerm t : visitor.visitPrologtext()) {
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
		return SemanticTools.toQuery(visitor.visitPossiblyEmptyDisjunct());
	}

	/**
	 * Parses the input. Returns a variable obtained by parsing the input.
	 * 
	 * @return {@link Var}
	 * @throws ParserException
	 * @throws RecognitionException
	 */
	public Var parseVar() throws ParserException, RecognitionException {
		PrologTerm term = visitor.visitTerm0();

		if (!term.isVar()) {
			throw new ParserException(String.format(
					"expected a SWI prolog variable but found '%s'",
					term.toString()), term.getSourceInfo());
		}

		return (PrologVar) term;
	}

	/**
	 * try parse a term
	 * 
	 * @return term, or null if error occurs
	 * @throws ParserException
	 */
	public PrologTerm ParseTerm() throws ParserException {
		return visitor.visitTerm0();
	}

	/**
	 * Parse a set of parameters.
	 *
	 * @return A list of {@link Term}s.
	 * @throws ParserException
	 */
	public List<Term> ParsePrologTerms() throws ParserException {
		PrologTerm t = visitor.visitTerm1000();

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