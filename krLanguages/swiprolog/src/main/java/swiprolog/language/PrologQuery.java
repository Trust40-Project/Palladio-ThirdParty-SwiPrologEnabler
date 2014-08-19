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

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

/**
 * Implements the GOAL Query object for (SWI) prolog.
 * 
 * Performs no checks on whether Prolog term represents a term that can be
 * queried on a Prolog database for efficiency reasons (this setup avoid checks
 * at run time, as a result e.g. from applying substitutions). Responsibility to
 * do these checks are delegated to the parser/compiler (to perform checks at
 * compile time only).
 */
public class PrologQuery extends PrologExpression implements Query {

	/**
	 * DOC
	 * 
	 * @param term A JPL term that can be used as a query.
	 */
	public PrologQuery(jpl.Term term) {
		super(term);
	}

	/**
	 * DOC
	 * 
	 * ASSUMES the inner prolog term of the query can also be parsed as an
	 * update. If called on (a-)goal literals in the context of a module, this has
	 * already been checked by the parser.
	 */
	public Update toUpdate() { 
		return new PrologUpdate(this.getTerm());
	}

	/**
	 * DOC
	 */
	public Query applySubst(Substitution s) {
		Hashtable<String, jpl.Term> jplSubstitution = ((PrologSubstitution) s).getJPLSolution();
		return new PrologQuery(JPLUtils.applySubst(jplSubstitution, this.getTerm()));
	}

}