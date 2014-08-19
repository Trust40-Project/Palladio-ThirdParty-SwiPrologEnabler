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
 * Terms may occur in {@link DatabaseFormula}, {@Link Query}, and {@Link Update}.
 * 
 * <p>Make sure to also implement {@code @Override boolean equals(Object obj)} and
 * {@code @Override int hashCode()}, which will be needed to implement the method
 * {@link Expression#mgu(Expression)}.</p>
 */
public interface Term extends Expression {

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 * 
	 * TODO: why is this not in Expression? Because we want a Term back here...
	 */
	Term applySubst(Substitution substitution);

}
