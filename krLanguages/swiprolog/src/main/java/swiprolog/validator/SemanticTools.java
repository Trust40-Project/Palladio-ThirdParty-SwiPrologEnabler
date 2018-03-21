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

import java.util.LinkedHashSet;
import java.util.Set;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import swiprolog.errors.ParserErrorMessages;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologExpression;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologUpdate;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologDBFormulaImpl;
import swiprolog.language.impl.PrologQueryImpl;
import swiprolog.language.impl.PrologUpdateImpl;
import swiprolog.parser.PrologOperators;

/**
 * Tools for semantic checking of {@link Term}s and conversion to {@link Update}
 * etc.
 */
public class SemanticTools {
	/**
	 * <p>
	 * Performs additional checks on top of {@link #basicUpdateCheck}, to check that
	 * conjunct is good update.
	 * </p>
	 *
	 * @param {@link
	 * 			PrologCompound} that is supposedly an update (ie conjunct of [not]
	 *            dbFormula)
	 * @returns the original term (no conversion is performed)
	 * @throws ParserException
	 *             if the conjunction is not a good Update.
	 */
	public static PrologUpdate conj2Update(PrologCompound conjunct) throws ParserException {
		return new PrologUpdateImpl(basicUpdateCheck(conjunct));
	}

	/**
	 * <p>
	 * Checks if {@link PrologCompound} may be used as update, i.e. a conjunct of
	 * either a {@link DatabaseFormula} or not(DatabaseFormula).
	 *
	 * @param conjunct
	 *            is the PrologCompound to be checked which is a zipped conjunct.
	 *            (see {@link PrologCompound#getConjuncts})
	 * @throws ParserException
	 *             if term not acceptable as DatabaseFormula.
	 *             </p>
	 * @returns original conjunct (if term is good update)
	 */
	private static PrologCompound basicUpdateCheck(PrologCompound conjunct) throws ParserException {
		for (Term term : conjunct.getOperands(",")) {
			if (term instanceof PrologCompound) {
				PrologCompound compound = (PrologCompound) term;
				if ((compound.getArity() == 1) && "not".equals(compound.getName())
						&& compound.getArg(0) instanceof PrologCompound) {
					PrologCompound content = (PrologCompound) compound.getArg(0);
					DBFormula(content);
				} else {
					DBFormula(compound);
				}
			} else {
				throw new ParserException("expected compound but found '" + term + "'.", conjunct.getSourceInfo());
			}
		}
		return conjunct;
	}

	/**
	 * <p>
	 * Converts given {@link PrologCompound} into a {@link DatabaseFormula} object
	 * made of it. Basic idea is that it checks that assert() will work (not throw
	 * exceptions). This function checks only SINGLE formulas, not conjunctions. Use
	 * toDBFormulaList for that.
	 * </p>
	 * <p>
	 * Check ISO section 8.9.1.3. If term not of form "head:-body" then head is to
	 * be taken the term itself and body to be "true" We fail if<br>
	 * 1. Head is a variable<br>
	 * 2. head can not be converted to a predication (@see D-is-a-precication in ISO
	 * p.132- )<br>
	 * 3. body can not be converted to a goal<br>
	 * CHECK 4. "The predicate indicator Pred of Head is not that of a dynamic
	 * procedure" . What does that mean and should we do something to prevent this?
	 * </p>
	 * <p>
	 * ISO section 6.2 also deals with this. Basically it defines directive terms
	 * and clause terms, and combined they allow every term that can be part of the
	 * database.
	 * </p>
	 * <p>
	 * ISO section 7.4 defines the way they are used when loading a database.
	 * Particularly the operators that are Directives are treated specially. In GOAL
	 * we do not want to support these and need exclusion. this is done via
	 * PrologOperators.goalProtected()
	 * </p>
	 *
	 * @param conjunction
	 * @returns DatabaseFormula object made from conjunction
	 * @throws ParserException
	 *             If the conjunction is not a valid clause.
	 */
	public static DatabaseFormula DBFormula(PrologCompound term) throws ParserException {
		PrologCompound head, body;
		if ((term.getArity() == 2) && term.getName().equals(":-") && (term.getArg(0) instanceof PrologCompound)
				&& (term.getArg(1) instanceof PrologCompound)) {
			head = (PrologCompound) term.getArg(0);
			body = (PrologCompound) term.getArg(1);
		} else {
			head = term;
			body = new PrologAtomImpl("true", term.getSourceInfo());
		}

		/*
		 * if (head.isVariable()) { throw new
		 * ParserException(ParserErrorMessages.HEAD_CANT_BE_VAR.toReadableString
		 * (term.toString()), term.getSourceInfo()); } else
		 */

		if (!head.isPredication()) {
			throw new ParserException(ParserErrorMessages.HEAD_MUST_BE_CLAUSE.toReadableString(term),
					term.getSourceInfo());
		}

		String signature = head.getSignature();
		if (head.isDirective()) {
			PrologCompound directive = (PrologCompound) term.getArg(0);
			signature = directive.getSignature();
			if (signature.equals("dynamic/1") && directive.getArg(0) instanceof PrologCompound) {
				PrologCompound dynamicPreds = (PrologCompound) directive.getArg(0);
				for (Term headTerm : dynamicPreds.getOperands(",")) {
					signature = headTerm.getSignature();
					if (PrologOperators.prologBuiltin(signature)) {
						throw new ParserException(
								ParserErrorMessages.CANNOT_REDEFINE_BUILT_IN.toReadableString(signature),
								term.getSourceInfo());
					}
				}
			} else {
				throw new ParserException(ParserErrorMessages.NOT_SUPPORTED_DIRECTIVE.toReadableString(signature),
						term.getSourceInfo());
			}
		} else if (PrologOperators.prologBuiltin(signature)) {
			throw new ParserException(ParserErrorMessages.CANNOT_REDEFINE_BUILT_IN.toReadableString(signature),
					term.getSourceInfo());
		}

		// try to convert, it will throw if it fails.
		toGoal(body);
		return new PrologDBFormulaImpl(term);
	}

	/**
	 * Checks that compound is a well formed Prolog goal.
	 * <p>
	 * ISO requires rebuild of the term but in our case we do not allow variables
	 * and hence a real rebuild is not necessary. Instead, we simply return the
	 * original term after checking.
	 * </p>
	 *
	 * @return the compound "rewritten" as a Prolog goal according to ISO.
	 * @throws ParserException
	 *             If t is not a well formed Prolog goal.
	 */
	public static PrologCompound toGoal(Term t) throws ParserException {
		if (t instanceof PrologCompoundImpl) {
			PrologCompound c = (PrologCompound) t;
			if (c.getName().equals(":-")) {
				if (c.getArity() == 1) {
					throw new ParserException(ParserErrorMessages.DIRECTIVE_NOT_AS_GOAL.toReadableString(t),
							t.getSourceInfo());
				} else if (c.getArity() == 2) {
					throw new ParserException(ParserErrorMessages.CLAUSE_NOT_AS_GOAL.toReadableString(t),
							t.getSourceInfo());
				}
			}
			if ((c.getArity() == 2)
					&& (c.getName().equals(",") || c.getName().equals(";") || c.getName().equals("->"))) {
				toGoal(c.getArg(0));
				toGoal(c.getArg(1));
			}
			return c;
		} else {
			throw new ParserException(ParserErrorMessages.EXPECTED_COMPOUND.toReadableString(t), t.getSourceInfo());
		}
	}

	/**
	 * <p>
	 * Checks that conjunction is a Prolog query and returns {@link PrologQuery}
	 * object made of it. This means that, if successful, the result can be queried
	 * to the (SWI) prolog engine.
	 * </p>
	 * <p>
	 * ISO section 7.6.2 on p.27 specifies how to convert a term to a goal.
	 * </p>
	 *
	 * @param conjunction
	 * @returns Query object made from conjunction
	 * @throws ParserException
	 *             if the conjunction is not a good Query.
	 */
	public static PrologQuery toQuery(PrologCompound conjunction) throws ParserException {
		return new PrologQueryImpl(toGoal(conjunction));
	}

	/**
	 * Extract the defined signature(s) from the compound. A signature is defined if
	 * the databaseformula defines a predicate of that signature (as fact or as
	 * following from inference.
	 *
	 * @param term
	 * @param info
	 *            the source info of the term. Used when an error message needs to
	 *            be thrown.
	 * @return signatures of defined terms.
	 * @throws ParserException
	 */
	public static Set<String> getDefinedSignatures(Term term, SourceInfo info) throws ParserException {
		Set<String> signatures = new LinkedHashSet<>();
		if (term instanceof PrologAtomImpl) {
			signatures.add(term.getSignature());
		} else if (term instanceof PrologCompound) {
			PrologCompound compound = (PrologCompound) term;
			if (compound.getName().equals(":-")) {
				switch (compound.getArity()) {
				case 1:
					break;
				case 2:
					signatures.add(compound.getArg(0).getSignature());
					break;
				default:
					// ':-' has prolog meaning only with 1 or 2 terms. ignore
					break;
				}
			} else {
				// if not :-, it must be a defined predicate.
				signatures.add(term.getSignature());
			}
		} else {
			throw new ParserException("expected atom or definition but found '" + term + "'.", info);
		}
		return signatures;
	}

	/**
	 * Extract the signatures of dynamic declarations from the compound.
	 *
	 * @param term
	 * @param info
	 * @return declared but undefined signatures
	 * @throws ParserException
	 */
	public static Set<String> getDeclaredSignatures(PrologCompound term, SourceInfo info) throws ParserException {
		Set<String> signatures = new LinkedHashSet<>();
		if (term.isDirective()) {
			PrologCompound directive = (PrologCompound) term.getArg(0);
			if ((directive.getArity() != 1) || !directive.getName().equals("dynamic")
					|| !(directive.getArg(0) instanceof PrologCompound)) {
				throw new ParserException("only the 'dynamic/1' directive is supported, found " + directive, info);
			}
			PrologCompound content = (PrologCompound) directive.getArg(0);
			for (Term signatureterm : content.getOperands(",")) {
				PrologCompound signature = (signatureterm instanceof PrologCompound) ? (PrologCompound) signatureterm
						: null;
				if (signature != null && signature.isPredicateIndicator()) {
					signatures.add(signature.getArg(0) + "/" + signature.getArg(1));
				} else {
					throw new ParserException("term '" + signatureterm + "' is not a predicate indicator", info);
				}
			}
		}
		return signatures;
	}

	/**
	 * Extract the non-system defined used signature(s) from the {@link Expression}.
	 * A signature is used if the expression is or contains a predicate of that
	 * signature. 'contains' means that one of the arguments of this predicate uses
	 * the signature (recursive definition).
	 *
	 * @param expression
	 *            the {@link DatabaseFormula} to extract the defined signatures
	 *            from.
	 * @return signature(s) that are used in the expression
	 */
	public static Set<String> getUsedSignatures(PrologExpression term) {
		PrologCompound compound;
		if (term instanceof PrologDBFormulaImpl) {
			compound = ((PrologDBFormulaImpl) term).getCompound();
		} else if (term instanceof PrologQueryImpl) {
			compound = ((PrologQueryImpl) term).getCompound();
		} else if (term instanceof PrologUpdateImpl) {
			compound = ((PrologUpdateImpl) term).getCompound();
		} else if (term instanceof PrologCompoundImpl) {
			compound = (PrologCompoundImpl) term;
		} else {
			return new LinkedHashSet<>(0);
		}

		Set<String> signatures = new LinkedHashSet<>();
		if (compound.getArity() == 1 && "dynamic".equals(compound.getName())
				&& compound.getArg(0) instanceof PrologCompound) {
			// special case. dynamic contains list of //2 predicates that the
			// user is explictly declaring.
			PrologCompound dyamic = (PrologCompound) (compound).getArg(0);
			for (Term dyndecl : dyamic.getOperands(",")) {
				PrologCompound declaration = (dyndecl instanceof PrologCompound) ? (PrologCompound) dyndecl : null;
				if (declaration != null && declaration.isPredicateIndicator()) {
					signatures.add(declaration.getArg(0) + "/" + declaration.getArg(1));
				}
			}
			return signatures;
		}

		String signature = compound.getSignature();
		if (!PrologOperators.prologBuiltin(signature)) {
			signatures.add(signature);
		}

		for (int i = 0; i < compound.getArity(); ++i) {
			signatures.addAll(getUsedSignatures((PrologExpression) compound.getArg(i)));
		}

		return signatures;
	}
}