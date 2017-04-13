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

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import org.jpl7.Compound;
import org.jpl7.Term;
import org.jpl7.Variable;
import swiprolog.SwiInstaller;

public class TestPrologSubstitution {
	static {
		SwiInstaller.init();
	}

	/**
	 * Check that variables are substituted only once.
	 */
	@Test
	public void testSubstitution() {
		SortedMap<String, Term> solution = new TreeMap<>();

		Variable Y = new Variable("Y");
		Variable Z = new Variable("Z");
		solution.put("X", Y);
		solution.put("Y", Z);

		Term[] args = new Term[2];
		args[0] = new Variable("X");
		args[1] = new Variable("Y");
		Term term = new org.jpl7.Compound("aap", args);

		Term result = JPLUtils.applySubst(solution, term);
		assertTrue(result instanceof Compound);
		Compound compound = (Compound) result;
		assertEquals(Y, compound.arg(1));
		assertEquals(Z, compound.arg(2));
	}

	@Test
	public void testToString() {
		SortedMap<String, Term> solution = new TreeMap<>();
		PrologSubstitution substitution1 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertTrue(substitution1.getJPLSolution().isEmpty());

		org.jpl7.Variable var = new org.jpl7.Variable("X");
		org.jpl7.Term term = new org.jpl7.Atom("a");
		solution.put(var.name(), term);
		PrologSubstitution substitution2 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(1, substitution2.getJPLSolution().size());
		assertEquals(term, substitution2.getJPLSolution().get(var.name()));

		org.jpl7.Variable var1 = new org.jpl7.Variable("Y");
		org.jpl7.Term term1 = new org.jpl7.Atom("b");
		solution.put(var1.name(), term1);
		PrologSubstitution substitution3 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(2, substitution3.getJPLSolution().size());
		assertEquals(term, substitution3.getJPLSolution().get(var.name()));
		assertEquals(term1, substitution3.getJPLSolution().get(var1.name()));

		org.jpl7.Variable var2 = new org.jpl7.Variable("Z");
		org.jpl7.Variable var3 = new org.jpl7.Variable("V");
		solution.put(var2.name(), var3);
		PrologSubstitution substitution4 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(3, substitution4.getJPLSolution().size());
		assertEquals(term, substitution4.getJPLSolution().get(var.name()));
		assertEquals(term1, substitution4.getJPLSolution().get(var1.name()));
		assertEquals(var3, substitution4.getJPLSolution().get(var2.name()));
	}
}
