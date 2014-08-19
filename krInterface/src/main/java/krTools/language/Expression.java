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

import java.util.Set;

/**
 * An expression is any grammatically correct string of symbols of a knowledge
 * representation (KR) language. As KR languages are logical languages,
 * expressions typically refer to objects, or to propositions. For example, an
 * expression may be a constant 'a', term '1+2', or formula 'on(a,b),
 * clear(table)'. This class does not make any assumptions about the actual
 * syntax of terms or formulas, and the examples are provided only for purposes
 * of clarification.
 * 
 * The assumptions that are made are the following: - an expression is part of a
 * specific KR language. - each expression has a 'main operator': this may
 * either be the expression itself (e.g. in case of a constant or positive
 * literal) or a function such as '+' or logical operator such as 'not'. - the
 * language contains variables (though even this is not really enforced; the
 * methods isClosed, isVar, getFreeVar, applySubst, and mgu only make sense if
 * variables are part of the language.
 */
/**
 * TODO determines if two terms are equal.
 * <p>
 * Important implementation detail: {@link Expression}s and particularly
 * {@link Var}s are used in {@link Set}s and therefore must implement
 * hashCode and equals. The interface specifies these but since they are
 * already implemented by the default object they will not appear if you ask
 * Eclipse to add the missing function to a derived class.
 * 
 * @return true if all fields of Expression are literally equal, including
 *         the names of variables.
 */
//boolean equals(Object obj);
/**
 * Calculates a hash code for this Expression.
 * 
 * @return An Integer with the following property: if two objects are equal
 *         to each other (using {@link #equals(Object)}), the hash code is
 *         the same.<br>
 *         It is not necessarily the case that two objects with the same
 *         hash code are equal.
 */
//int hashCode();
public interface Expression {

	/**
	 * @return String of the form "{operator name}/{arity}" where arity is
	 * number of arguments associated with the operator.
	 */
	String getSignature();

	/**
	 * @return true if expression is a variable.
	 */
	boolean isVar();

	/**
	 * @return true if expression does not contain any free variables.
	 */
	boolean isClosed();

	/**
	 * @return all free variables in Expression.
	 */
	Set<Var> getFreeVar();

	/**
	 * TODO 
	 * @param substitution
	 * @return
	 */
	Expression applySubst(Substitution substitution);
	
	/**
	 * @return most general substitution (mgu) that makes this
	 *         {@link Expression} equal to the parameter expression expression
	 *         (unifies), or null if unification is impossible.
	 */
	Substitution mgu(Expression expression);

}
