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

import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologUpdate;
import swiprolog.parser.PrologOperators;

/**
 * Tools for semantic checking of {@link PrologTerm}s and conversion to
 * {@link Update} etc.
 *
 * @author W.Pasman 18may15
 */
public class SemanticTools {

	/**
	 * <p>
	 * Performs additional checks on top of {@link #basicUpdateCheck}, to check
	 * that conjunct is good update.
	 * </p>
	 *
	 * @param {@link PrologTerm} that is supposedly an update (ie conjunct of
	 *        [not] dbFormula)
	 * @returns the original term (no conversion is performed)
	 * @throws ParserException
	 *             if term is no good Update.
	 * @see #checkDBFormula
	 */
	public static PrologUpdate conj2Update(PrologTerm conjunct)
			throws ParserException {
		return new PrologUpdate(basicUpdateCheck(conjunct),
				conjunct.getSourceInfo());
	}

	/**
	 * <p>
	 * Checks if {@link PrologTerm} may be used as update, i.e. a conjunct of
	 * either a {@link DatabaseFormula} or not(DatabaseFormula).
	 *
	 * @param conjunct
	 *            is the PrologTerm to be checked which is a zipped conjunct.
	 *            (see {@link PrologTerm#getConjuncts})
	 * @throws ParserException
	 *             if term not acceptable as DatabaseFormula.
	 *             </p>
	 * @returns original conjunct (if term is good update)
	 */
	private static jpl.Term basicUpdateCheck(PrologTerm conjunct)
			throws ParserException {
		List<jpl.Term> terms = JPLUtils.getOperands(",", conjunct.getTerm());
		for (jpl.Term term : terms) {
			if (JPLUtils.getSignature(term).equals("not/1")) {
				DBFormula(new PrologTerm(term.arg(1), conjunct.getSourceInfo()));
			} else {
				DBFormula(new PrologTerm(term, conjunct.getSourceInfo()));
			}
		}
		return conjunct.getTerm();
	}

	/**
	 * <p>
	 * Converts given {@Link PrologTerm} into a {@link DatabaseFormula}
	 * object made of it. Basic idea is that it checks that assert() will work
	 * (not throw exceptions). This function checks only SINGLE formulas, not
	 * conjunctions. Use toDBFormulaList for that.
	 * </p>
	 * <p>
	 * Check ISO section 8.9.1.3. If term not of form "head:-body" then head is
	 * to be taken the term itself and body to be "true" We fail if<br>
	 * 1. Head is a variable<br>
	 * 2. head can not be converted to a predication (@see D-is-a-precication in
	 * ISO p.132- )<br>
	 * 3. body can not be converted to a goal<br>
	 * CHECK 4.
	 * "The predicate indicator Pred of Head is not that of a dynamic procedure"
	 * . What does that mean and should we do something to prevent this?
	 * </p>
	 * <p>
	 * ISO section 6.2 also deals with this. Basically it defines directive
	 * terms and clause terms, and combined they allow every term that can be
	 * part of the database.
	 * </p>
	 * <p>
	 * ISO section 7.4 defines the way they are used when loading a database.
	 * Particularly the operators that are Directives are treated specially. In
	 * GOAL we do not want to support these and need exclusion. this is done via
	 * PrologOperators.goalProtected()
	 * </p>
	 *
	 * @param conjunction
	 *            is a PrologTerm containing a conjunction
	 * @see PrologTerm#getConjuncts
	 * @see PrologTerm#useNotAllowed useNotAllowed
	 * @see toDBFormulaList
	 * @returns DatabaseFormula object made from conjunction
	 * @throws ParserException
	 *             If Prolog term is not a valid clause.
	 */
	public static DatabaseFormula DBFormula(PrologTerm term)
			throws ParserException {
		jpl.Term head, body;

		if (term.getSignature().equals(":-/2")) {
			head = term.getTerm().arg(1);
			body = term.getTerm().arg(2);
		} else {
			head = term.getTerm();
			body = new jpl.Atom("true");
		}

		if (head.isVariable()) {
			throw new ParserException(
					"The head of a Prolog rule cannot be a variable "
							+ term.toString(), term.getSourceInfo());
		} else if (!JPLUtils.isPredication(head)) {
			throw new ParserException("The head of a Prolog rule cannot be "
					+ term.toString() + " but should be a Prolog clause",
					term.getSourceInfo());
		}

		String signature = JPLUtils.getSignature(head);
		if (PrologOperators.prologBuiltin(signature)) {
			throw new ParserException(
					"Cannot redefine the Prolog built-in predicate "
							+ term.toString(), term.getSourceInfo());
		}

		// check for special directives, and refuse those.
		String name = signature.substring(0, signature.indexOf('/'));
		if (PrologOperators.goalProtected(name)) {
			throw new ParserException("Some predicates, like "
					+ head.toString() + ", are protected "
					+ "and are not allowed in the head of a Prolog clause: "
					+ term.toString(), term.getSourceInfo());
		}

		// try to convert, it will throw if it fails.
		toGoal(body, term.getSourceInfo());
		return new PrologDBFormula(term.getTerm(), term.getSourceInfo());
	}

	/**
	 * Checks that term is a well formed Prolog goal.
	 * <p>
	 * ISO requires rebuild of the term but in our case we do not allow
	 * variables and hence a real rebuild is not necessary. Instead, we simply
	 * return the original term after checking.
	 * </p>
	 *
	 * @return the term "rewritten" as a Prolog goal according to ISO.
	 * @throws ParserException
	 *             If t is not a well formed Prolog goal.
	 */
	public static jpl.Term toGoal(jpl.Term t, SourceInfo source)
			throws ParserException {
		// 7.6.2.a use article 7.8.3
		if (t.isVariable()) {
			throw new ParserException("Variables cannot be used as goals: "
					+ t.toString(), source);
		}
		// 7.6.2.b
		String sig = JPLUtils.getSignature(t);
		if (PrologOperators.goalProtected(t.name())) {
			throw new ParserException("The use of predicate " + t.toString()
					+ ": " + t.toString() + " is not supported", source);
		} else if (sig.equals(":-/2")) {
			throw new ParserException("Cannot use a clause " + t.toString()
					+ " as a goal", source);
		} else if (sig.equals(",/2") || sig.equals(";/2") || sig.equals("->/2")) {
			toGoal(t.arg(1), source);
			toGoal(t.arg(2), source);
		}
		// 7.6.2.c
		// no action required.
		return t;
	}

	/**
	 * <p>
	 * Checks that conjunction is a Prolog query and returns {@link PrologQuery}
	 * object made of it. This means that, if successful, the result can be
	 * queried to the (SWI) prolog engine.
	 * </p>
	 * <p>
	 * ISO section 7.6.2 on p.27 specifies how to convert a term to a goal.
	 * </p>
	 *
	 * @param conjunction
	 *            is a PrologTerm containing a conjunction
	 * @see PrologTerm#getConjuncts
	 * @see PrologTerm#useNotAllowed useNotAllowed
	 * @returns Query object made from conjunction
	 * @throws ParserException
	 *             if prologTerm is not a good Query.
	 */
	public static PrologQuery toQuery(PrologTerm conjunction)
			throws ParserException {
		return new PrologQuery(toGoal(conjunction.getTerm(),
				conjunction.getSourceInfo()), conjunction.getSourceInfo());
	}
}