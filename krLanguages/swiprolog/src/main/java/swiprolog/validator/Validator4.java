package swiprolog.validator;

import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import visitor.Visitor4;

/**
 * Parse, visit and validate. All errors are collected internally, but also
 * after an attempt to validate a string the first error is thrown if there was
 * an error.
 * 
 * Usage example to parse string as prolog term:
 * <code>validator = new Validator4(new Prolog4VisitorPlus(
				new ErrorStoringProlog4Parser(new StringReader(in), null)));
				PrologTerm term = validator.term();
				</code>
 * 
 * @author W.Pasman 18may15
 *
 */
public class Validator4 {

	private Validator4Internal validator;

	private void rethrow() throws ParserException {
		if (!validator.isSuccess()) {
			throw validator.getErrors().get(0);
		}
	}

	/**
	 * 
	 * @param visitor
	 *            the {@link Visitor4} (that contains the parser)
	 */
	public Validator4(Visitor4 vis) {
		validator = new Validator4Internal(vis);
	}

	/**
	 * Parses an update or empty term.
	 * 
	 * @return {@link Update} or null if there is error.
	 */
	public Update updateOrEmpty() throws ParserException {
		Update t = validator.updateOrEmpty();
		rethrow();
		return t;
	}

	/**
	 * Parses a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>.
	 * @throws ParserException
	 */
	public List<DatabaseFormula> program() throws ParserException {
		List<DatabaseFormula> t = validator.program();
		rethrow();
		return t;
	}

	/**
	 * Parse a section that should contain Prolog goals, i.e., queries.
	 *
	 * @return List<Query>
	 * @throws ParserException
	 */
	public List<Query> goalSection() throws ParserException {
		List<Query> t = validator.goalSection();
		rethrow();
		return t;
	}

	/**
	 * Parses a (possibly empty) query.
	 *
	 * @return A {@link PrologQuery}.
	 * @throws ParserException
	 */
	public PrologQuery queryOrEmpty() throws ParserException {
		PrologQuery t = validator.queryOrEmpty();
		rethrow();
		return t;
	}

	/**
	 * Parses the input. Returns a variable obtained by parsing the input.
	 * 
	 * @return {@link Var}.
	 * @throws ParserException
	 */
	public Var var() throws ParserException {
		Var t = validator.var();
		rethrow();
		return t;
	}

	/**
	 * try parse a term
	 * 
	 * @return term
	 * @throws ParserException
	 */
	public PrologTerm term() throws ParserException {
		PrologTerm t = validator.term();
		rethrow();
		return t;
	}

	/**
	 * Parse a set of parameters.
	 *
	 * @return A list of {@link Term}s.
	 * @throws ParserException
	 */
	public List<Term> terms() throws ParserException {
		List<Term> t = validator.terms();
		rethrow();
		return t;

	}

	/**
	 * Get all errors that occured, both in validator and in visitor.
	 * 
	 * @return all errors that occured
	 */
	public List<ParserException> getErrors() {
		return validator.getErrors();
	}

	/**
	 * @return true iff parsing was successfull which means {@link #getErrors()}
	 *         returns empty list.
	 */
	public boolean isSuccess() {
		return getErrors().isEmpty();
	}
}