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

package krTools.language;

/**
 * A database formula is an expression that can be inserted into a {@link Database}.
 * 
 * <p>It is assumed that a DatabaseFormula does not consist of parts that
 * themselves are DatabaseFormulas again. That is, a DatabaseFormula must be an
 * expression that itself may be inserted into a database but does not have
 * constituent parts that may be inserted into a database.</p>
 * 
 * <p>Examples of database formulas differ from one kr language to another. In
 * Prolog, for example, clauses of the form p :- q and p where p and q are
 * literals may be part of a Prolog database. A negative literal not(p),
 * however, cannot be part of a Prolog database. Also note that a Prolog
 * conjunction p, q is not considered a database formula, as both p and q
 * separately can be inserted into a database.</p>
 * 
 * <p>Make sure to also implement {@code @Override boolean equals(Object obj)} and
 * {@code @Override int hashCode()}, which will be needed to implement the method
 * {@link Expression#mgu(Expression)}.</p>
 */
public interface DatabaseFormula extends Expression {

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	DatabaseFormula applySubst(Substitution substitution);

}
