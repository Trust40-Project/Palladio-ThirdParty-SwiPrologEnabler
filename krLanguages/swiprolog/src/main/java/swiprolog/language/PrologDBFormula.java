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

package swiprolog.language;

import java.util.Hashtable;

import krTools.language.DatabaseFormula;
import krTools.language.Substitution;

/**
 * <p>
 * Represents all expressions that (may) have a truth value (after instantiating
 * variables with ground terms). Should not be used for expressions that do not
 * have a truth value.
 * </p>
 * <p>
 * Performs no checks on whether Prolog term represents a term that can be
 * inserted into a Prolog database for efficiency reasons (this setup avoid
 * checks at run time, as a result e.g. from applying substitutions).
 * Responsibility to do these checks are delegated to the parser (to perform
 * checks at compile time only).
 * </p>
 * Technical note. I would really have liked to write
 * <tt>PrologDBFormula extends PrologTerm</tt>. However we need a constructor of
 * the form <tt>PrologDBFormula(PrologTerm x)</tt> which would require
 * constructor of PrologTerm which does not exist (since PrologTerm is abstract
 * class). Anyway the idea is that PrologTerm already exists so calling its
 * constructor kind of defeats the purpose. And it is not possible in Java to
 * cast from one subclass of PrologTerm to another. Eg we can not cast from
 * FuncTerm to PrologDBFormula because PrologTerm is abstract class. Having a
 * constructor new PrologTerm(other PrologTerm) would effectively clone the
 * PrologTerm which would be expensive. </p>
 */
public class PrologDBFormula extends PrologExpression implements DatabaseFormula {

	/**
	 * Creates a Prolog formula that can be part of a Prolog database.
	 * 
	 * @param term A JPL term.
	 * @param source
	 *            The source code location of this action, if available;
	 *            {@code null} otherwise.
	 */
	public PrologDBFormula(jpl.Term term) {
		super(term);
		/*
		 * TODO CHECK should exclude all expressions that do not have a truth
		 * value --> check builtIn()
		 * TODO CHECK ISO manual what can be inserted
		 * into Prolog database. Basically, P(X) can be inserted only if P is
		 * not a built-in and P:-Q can be inserted if P is not a built-in and Q
		 * *does not contain* protecteds.
		 */
	}

	public PrologDBFormula applySubst(Substitution substitution) {
		Hashtable<String, jpl.Term> jplSubstitution =
				((PrologSubstitution) substitution).getJPLSolution();
		return new PrologDBFormula(JPLUtils.applySubst(jplSubstitution, this.getTerm()));
	}

}