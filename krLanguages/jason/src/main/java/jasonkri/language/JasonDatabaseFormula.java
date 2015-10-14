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

package jasonkri.language;

import jason.asSyntax.Term;
import jasonkri.Utils;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;

public class JasonDatabaseFormula extends JasonExpression implements
		DatabaseFormula {

	public JasonDatabaseFormula(Term t, SourceInfo i) {
		super(t, i); // must be done first.
		if (!Utils.isDatabaseFormula(t)) {
			throw new IllegalArgumentException(t.toString()
					+ " is not a database formula"); // bug?
		}
	}

	@Override
	public DatabaseFormula applySubst(Substitution substitution) {
		return new JasonDatabaseFormula(substitute(substitution),
				getSourceInfo());
	}

	/**
	 * Converts a database formula to a query. May not perform any check whether
	 * a conversion can be successfully performed. Use {@link #isQuery()} to
	 * check this.
	 *
	 * @return A {@link Query}.
	 */
	@Override
	public Query toQuery() {
		return new JasonQuery(getJasonTerm(), getSourceInfo());
	}

}
