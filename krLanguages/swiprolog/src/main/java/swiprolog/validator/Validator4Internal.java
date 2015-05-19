package swiprolog.validator;

import java.util.ArrayList;
import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import visitor.Visitor4;

/**
 * Parse, visit and validate. All errors occuring during parse or validation are
 * thrown. Normally you use Validator4 which also stores errors as they occur.
 * 
 * Usage example to parse string as prolog term:
 * <code>validator = new Validator4Internal(new Prolog4VisitorPlus(
				new ErrorStoringProlog4Parser(new StringReader(in), null)));
				PrologTerm term = validator.term();
				</code>
 * 
 * @author W.Pasman 18may15
 *
 */
public class Validator4Internal {

	private Visitor4 visitor;

	private List<ParserException> errors = new ArrayList<ParserException>();

	/**
	 * 
	 * @param visitor
	 *            the {@link Visitor4} (that contains the parser)
	 */
	public Validator4Internal(Visitor4 vis) {
		visitor = vis;
	}

	/**
	 * Parses an update or empty term.
	 * 
	 * @return {@link Update} or null if there is error.
	 */
	public Update updateOrEmpty() throws ParserException {
		try {
			return SemanticTools.conj2Update(visitor
					.visitPossiblyEmptyConjunct());
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * Parses a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> program() {
		try {
			List<PrologTerm> prologTerms = visitor.visitPrologtext();

			List<DatabaseFormula> dbfs = new ArrayList<DatabaseFormula>(
					prologTerms.size());
			for (PrologTerm t : prologTerms) {
				dbfs.add(SemanticTools.DBFormula(t));
			}
			return dbfs;
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * Parse a section that should contain Prolog goals, i.e., queries.
	 *
	 * @return List<Query>, or {@code null} if a parser error occurs.
	 */
	public List<Query> goalSection() {
		try {
			List<Query> goals = new ArrayList<Query>();
			for (PrologTerm t : visitor.visitPrologtext()) {
				// check that each term is a valid Prolog goal / query
				goals.add(new PrologQuery(SemanticTools.toGoal(t.getTerm(),
						t.getSourceInfo()), t.getSourceInfo()));
			}
			return goals;
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * Parses a (possibly empty) query.
	 *
	 * @return A {@link PrologQuery}, or {@code null} if an error occurred.
	 */
	public PrologQuery queryOrEmpty() {
		try {
			return SemanticTools.toQuery(visitor.visitPossiblyEmptyDisjunct());
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * Parses the input. Returns a variable obtained by parsing the input.
	 * 
	 * @return {@link Var} or null if error occured.
	 */
	public Var var() {
		try {
			PrologTerm term;
			term = visitor.visitTerm0();

			if (!term.isVar()) {
				throw new ParserException(String.format(
						"expected a SWI prolog variable but found '%s'",
						term.toString()), term.getSourceInfo());
			}

			return (PrologVar) term;
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * try parse a term
	 * 
	 * @return term, or null if error occurs
	 * @throws ParserException
	 */
	public PrologTerm term() {
		try {
			return visitor.visitTerm0();
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;
	}

	/**
	 * Parse a set of parameters.
	 *
	 * @return A list of {@link Term}s. Or null if an error occured.
	 */
	public List<Term> terms() {
		try {
			PrologTerm t = visitor.visitTerm1000();

			List<jpl.Term> original = JPLUtils.getOperands(",", t.getTerm());
			List<Term> terms = new ArrayList<Term>(original.size());
			for (jpl.Term term : original) {
				if (term instanceof jpl.Variable) {
					terms.add(new PrologVar((jpl.Variable) term, t
							.getSourceInfo()));
				} else {
					terms.add(new PrologTerm(term, t.getSourceInfo()));
				}
			}
			return terms;
		} catch (ParserException e) {
			errors.add(e);
		}
		return null;

	}

	/**
	 * Convert all errors that occured in the validator. Excludes the errors in
	 * the visitor.
	 * 
	 * @return list of validator errors that occured in the validator.
	 */
	public List<ParserException> getValidatorErrors() {
		return errors;
	}

	/**
	 * Get all errors that occured, both in validator and in visitor.
	 * 
	 * @return all errors that occured
	 */
	public List<ParserException> getErrors() {
		List<ParserException> list = new ArrayList<ParserException>();
		list.addAll(visitor.getErrors());
		list.addAll(getValidatorErrors());
		return list;
	}

	/**
	 * @return true iff parsing was successfull which means {@link #getErrors()}
	 *         returns empty list.
	 */
	public boolean isSuccess() {
		return getErrors().isEmpty();
	}
}