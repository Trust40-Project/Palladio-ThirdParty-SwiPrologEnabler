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

package swiprolog.language.impl;

import java.util.List;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologSubstitution;
import swiprolog.language.PrologVar;

/**
 * Utility class for JPL objects.
 */
public class JPLUtils {
	/**
	 * Returns a (possibly empty) Prolog list with the given terms as elements
	 * of the list.
	 *
	 * @param terms
	 *            The elements to be included in the list.
	 * @return A Prolog list using the "." and "[]" list constructors.
	 */
	public static Term termsToList(List<Term> terms, SourceInfo info) {
		// Start with element in list, since innermost term of Prolog list is
		// the last term.
		Term list = new PrologAtomImpl("[]", info);
		for (int i = terms.size() - 1; i >= 0; i--) {
			list = new PrologCompoundImpl(".", new Term[] { terms.get(i), list }, info);
		}
		return list;
	}

	/**
	 * Returns a (possibly empty) conjunct containing the given terms.
	 *
	 * @param terms
	 * @param info
	 *            source info
	 * @return possibly empty conjunct containing the given terms
	 */
	public static PrologCompound termsToConjunct(List<Term> terms, SourceInfo info) {
		if (terms.isEmpty()) {
			return new PrologAtomImpl("true", info);
		} else {
			// build up list last to first.
			PrologCompound list = (PrologCompound) terms.get(terms.size() - 1); // last
			for (int i = terms.size() - 2; i >= 0; i--) {
				list = new PrologCompoundImpl(",", new Term[] { terms.get(i), list }, info);
			}
			return list;
		}
	}

	/**
	 * Create most general unifier (mgu) of two terms. The returned most general
	 * unifier is a substitution which, if applied to both terms, will make the
	 * terms equal. This algorithm currently will first try to set variables in
	 * x. So it has a bias towards filling in the x variables over filling in y
	 * variables. <br>
	 * The map that is returned contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 *
	 * @param x
	 *            first term
	 * @param y
	 *            second term
	 * @return mgu, or null if no mgu exists.
	 */
	public static Substitution mgu(Term x, Term y) {
		return unify(x, y, new PrologSubstitution());
	}

	/**
	 * Textbook implementation of Unify algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace. <br>
	 * This textbook implementation has a bias towards assigning variables in
	 * the left hand term. <br>
	 * The returned map contains String as key objects, because jpl.Variable can
	 * not be used for key (as it does not implement hashCode).
	 *
	 * @param x
	 *            the first term.
	 * @param y
	 *            the second term.
	 * @param s
	 *            the substitutions used so far.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	public static Substitution unify(Term x, Term y, Substitution s) {
		if (s == null) {
			return null;
		} else if (x.equals(y)) {
			return s;
		} else if (x instanceof PrologVar) {
			return unifyVar((PrologVar) x, y, s);
		} else if (y instanceof PrologVar) {
			return unifyVar((PrologVar) y, x, s);
		} else if (x instanceof PrologCompound && y instanceof PrologCompound) {
			return unifyCompounds((PrologCompound) x, (PrologCompound) y, s);
		} else {
			return null;
		}
	}

	/**
	 * Unify 2 {@link PrologCompound}s. Implements the bit vague element in the
	 * textbook UNIFY(x.ARGS, y.ARGS, UNIFY(x.OP, y.OP,s)).
	 *
	 * @param x
	 *            the first {@link PrologCompound}
	 * @param y
	 *            The second {@link PrologCompound}
	 * @param s
	 *            the substitutions used so far. s must not be null.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	private static Substitution unifyCompounds(PrologCompound x, PrologCompound y, Substitution s) {
		if (x.getArity() != y.getArity()) {
			return null;
		} else if (!x.getName().equals(y.getName())) {
			return null;
		} else {
			for (int i = 0; i < x.getArity(); ++i) {
				s = unify(x.getArg(i), y.getArg(i), s);
			}
			return s;
		}
	}

	/**
	 * Textbook implementation of Unify-Var algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition. unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace.
	 *
	 * @param var
	 *            the {@link Var}.
	 * @param y
	 *            the {@link Term}.
	 * @param s
	 *            the substitutions used so far. Must not be null. This set can
	 *            be modified by this function.
	 *
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */

	private static Substitution unifyVar(Var var, Term x, Substitution s) {
		if (s.get(var) != null) {
			return unify(s.get(var), x, s);
		} else if (x instanceof Var && s.get((Var) x) != null) {
			return unify(var, s.get((Var) x), s);
		} else if (x.getFreeVar().contains(var)) {
			return null;
		} else {
			s.addBinding(var, x);
			return s;
		}
	}
}
