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

import java.util.Map;

import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;

/**
 * Represents a Prolog term.
 */
public class PrologTerm extends PrologExpression implements Term {
	/**
	 * Creates a {@link PrologTerm} from a JPL term.
	 *
	 * @param term
	 *            A JPL term.
	 * @param info
	 *            A source info object.
	 */
	public PrologTerm(jpl.Term term, SourceInfo info) {
		super(term, info);
	}

	/**
	 * A term is an anonymous variable if it is a variable and anonymous.
	 */
	public boolean isAnonymousVar() {
		return getTerm().isVariable() && ((PrologVar) this).isAnonymous();
	}

	@Override
	public PrologTerm applySubst(Substitution s) {
		Map<String, jpl.Term> jplSubstitution = (s == null) ? null
				: ((PrologSubstitution) s).getJPLSolution();
		jpl.Term term = JPLUtils.applySubst(jplSubstitution, getTerm());
		return new PrologTerm(term, getSourceInfo());
	}
	
	public DatabaseFormula toFormula() {
		if(isClosed()){
			return new PrologDBFormula(getTerm(), getSourceInfo());
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		return 0; // JPL does not implement Term.hashCode...
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof PrologTerm)
				&& getTerm().equals(((PrologTerm) obj).getTerm());
	}
}
