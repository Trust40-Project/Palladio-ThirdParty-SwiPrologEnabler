/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package swiprolog.validator;

import java.util.ArrayList;
import java.util.LinkedList;
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
import swiprolog.language.PrologUpdate;
import swiprolog.language.PrologVar;
import visitor.Visitor4;

/**
 * Parse, visit and validate. All errors occurring during parse or validation
 * are thrown. Normally you use Validator4 which also stores errors as they
 * occur.
 *
 * Usage example to parse string as prolog term:
 * <code>validator = new Validator4(new Prolog4VisitorPlus(
				new ErrorStoringProlog4Parser(new StringReader(in), null)));
				PrologTerm term = validator.term();
				</code>
 *
 * @author W.Pasman 18may15
 */
public class Validator4 {
	private final Visitor4 visitor;

	/**
	 * @param visitor
	 *            the {@link Visitor4} (that contains the parser)
	 */
	public Validator4(Visitor4 vis) {
		this.visitor = vis;
	}

	/**
	 * Validate an update or empty term.
	 *
	 * @return {@link Update} or null if there is error.
	 */
	public Update updateOrEmpty() throws ParserException {
		PrologTerm conj = this.visitor.visitPossiblyEmptyConjunct();
		if (conj.toString().equals("true")) { // special case.
			return new PrologUpdate(conj.getTerm(), conj.getSourceInfo());
		} else {
			return SemanticTools.conj2Update(conj);
		}
	}

	/**
	 * Validate a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> program() throws ParserException {
		List<PrologTerm> prologTerms = this.visitor.visitPrologtext();
		List<DatabaseFormula> dbfs = new ArrayList<DatabaseFormula>(
				prologTerms.size());
		for (PrologTerm t : prologTerms) {
			dbfs.add(SemanticTools.DBFormula(t));
		}
		return dbfs;
	}

	/**
	 * Validate a section that should contain Prolog goals, i.e., queries.
	 *
	 * @return List<Query>, or {@code null} if a parser error occurs.
	 */
	public List<Query> goalSection() throws ParserException {
		List<Query> goals = new LinkedList<Query>();
		for (PrologTerm t : this.visitor.visitPrologtext()) {
			// check that each term is a valid Prolog goal / query
			goals.add(new PrologQuery(SemanticTools.toGoal(t.getTerm(),
					t.getSourceInfo()), t.getSourceInfo()));
		}
		return goals;
	}

	/**
	 * Validate a (possibly empty) query.
	 *
	 * @return A {@link PrologQuery}, or {@code null} if an error occurred.
	 */
	public PrologQuery queryOrEmpty() throws ParserException {
		return SemanticTools.toQuery(this.visitor.visitPossiblyEmptyDisjunct());
	}

	/**
	 * Validate the variable. Returns a variable obtained by parsing the input.
	 *
	 * @return {@link Var} or null if error occurred.
	 */
	public Var var() throws ParserException {
		PrologTerm term;
		term = this.visitor.visitTerm0();
		if (term.isVar()) {
			return (PrologVar) term;
		} else {
			throw new ParserException(String.format(
					"expected a SWI prolog variable but found '%s'",
					term.toString()), term.getSourceInfo());
		}
	}

	/**
	 * Validate a term
	 *
	 * @return term, or null if error occurs
	 * @throws ParserException
	 */
	public PrologTerm term() throws ParserException {
		return this.visitor.visitTerm0();
	}

	/**
	 * Validate a set of parameters.
	 *
	 * @return A list of {@link Term}s. Or null if an error occured.
	 */
	public List<Term> terms() throws ParserException {
		PrologTerm t = this.visitor.visitTerm1000();
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

	/**
	 * Get all errors that occurred, both in validator and in visitor.
	 *
	 * @return all errors that occurred
	 */
	public List<ParserException> getErrors() {
		return this.visitor.getErrors();
	}

	/**
	 * @return true iff parsing was successfull which means {@link #getErrors()}
	 *         returns empty list.
	 */
	public boolean isSuccess() {
		return getErrors().isEmpty();
	}
}