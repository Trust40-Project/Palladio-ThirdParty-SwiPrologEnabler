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

import java.util.Set;

import org.junit.Test;

import jpl.Variable;
import krTools.language.Term;
import krTools.language.Var;

public class Terms {

	@Test
	public void atomtostring() {
		// Check that toString works OK.
		// TODO FIXME
		// Term atom = new PrologTerm(new jpl.Atom("'/tmp/pl_tmp_1089_0'"));
		// assertEquals("'/tmp/pl_tmp_1089_0'", atom.toString());
	}

	@Test
	public void equality() {
		Term var1 = new PrologVar(new jpl.Variable("X"), null);
		Term var2 = new PrologVar(new jpl.Variable("X"), null);
		assertEquals(true, var1.equals(var2));
	}

	/**
	 * Check if we get the vars from term q(p,X).
	 */
	@Test
	public void testFreeVars() {
		Variable X = new Variable("X");
		jpl.Term term = new jpl.Compound("q", new jpl.Term[] { new jpl.Atom("p"), X });
		Term t = new PrologTerm(term, null);
		Set<Var> vars = t.getFreeVar();
		assertEquals(1, vars.size());
		assertEquals(X, ((PrologVar) (vars.iterator().next())).getTerm());
	}

	/**
	 * Check if the X in the term "X=1" is considered a "free" var.
	 */
	@Test
	public void testFreeVarsInIs() {
		Variable X = new Variable("X");
		jpl.Term term = new jpl.Compound("=", new jpl.Term[] { X, new jpl.Integer(1), });
		Term t = new PrologTerm(term, null);
		Set<Var> vars = t.getFreeVar();
		assertEquals(1, vars.size());
		assertEquals(X, ((PrologVar) (vars.iterator().next())).getTerm());
	}

}