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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologVarImpl;

public class TestPrologSubstitution {
	@Test
	public void testSubstitution() {
		Substitution solution = new PrologSubstitution();
		Var X = new PrologVarImpl("X", null);
		Var Y = new PrologVarImpl("Y", null);
		Var Z = new PrologVarImpl("Z", null);
		solution.addBinding(X, Y);
		solution.addBinding(Y, Z);

		Term term = new PrologCompoundImpl("aap", new Term[] { X, Y }, null);

		Term result = term.applySubst(solution);
		assertTrue(result instanceof PrologCompound);
		PrologCompound compound = (PrologCompound) result;
		assertEquals(Y, compound.getArg(0));
		assertEquals(Z, compound.getArg(1));
	}

	@Test
	public void testToString() {
		Substitution solution = new PrologSubstitution();
		assertTrue(solution.getVariables().isEmpty());

		Var X = new PrologVarImpl("X", null);
		Term a = new PrologAtomImpl("a", null);
		solution.addBinding(X, a);
		assertEquals(1, solution.getVariables().size());
		assertEquals(a, solution.get(X));

		Var Y = new PrologVarImpl("Y", null);
		Term b = new PrologAtomImpl("b", null);
		solution.addBinding(Y, b);
		assertEquals(2, solution.getVariables().size());
		assertEquals(a, solution.get(X));
		assertEquals(b, solution.get(Y));

		Var Z = new PrologVarImpl("Z", null);
		Term V = new PrologVarImpl("V", null);
		solution.addBinding(Z, V);
		assertEquals(3, solution.getVariables().size());
		assertEquals(a, solution.get(X));
		assertEquals(b, solution.get(Y));
		assertEquals(V, solution.get(Z));
	}
}
