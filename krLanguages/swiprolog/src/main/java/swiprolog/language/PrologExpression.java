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

import krTools.language.Expression;
import swiprolog.parser.PrologOperators;

public interface PrologExpression extends Expression {
	@Override
	public default boolean isVar() {
		return (this instanceof PrologVar);
	}

	/**
	 * Returns true iff the signature of the expression is true/0.
	 */
	public default boolean isEmpty() {
		return getSignature().equals("true/0");
	}

	/**
	 * @return The F-ixity of the term: returns NOT_OPERATOR for non-operator
	 *         terms. See ISO 12311, table 5.
	 * @see PrologOperators.Fixity for a list of f-ixities.
	 */
	public default PrologOperators.Fixity getFixity() {
		PrologOperators.Fixity spec = PrologOperators.getFixity(getSignature());
		return (spec == null) ? PrologOperators.Fixity.NOT_OPERATOR : spec;
	}

	/**
	 * Returns the priority of the main operator of the term. See ISO 12311,
	 * table 5.
	 *
	 * @return The priority of the term's operator. Default is 0.
	 */
	public default int getPriority() {
		Integer prio = PrologOperators.getPriority(getSignature());
		return (prio == null) ? 0 : prio;
	}
}