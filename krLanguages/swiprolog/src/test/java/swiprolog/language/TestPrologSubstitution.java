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

import static org.junit.Assert.*;

import java.util.Hashtable;

import jpl.Term;

import org.junit.Test;

import swiprolog.language.PrologSubstitution;

public class TestPrologSubstitution {

	@Test
	public void testToString() {
		Hashtable<String, Term> solution = new Hashtable<String, Term>();
		PrologSubstitution substitution1 = new PrologSubstitution(solution);
		
		assertEquals("[]", substitution1.toString());
		
		jpl.Variable var = new jpl.Variable("X");
		jpl.Term term = new jpl.Atom("a");
		solution.put(var.name(), term);
		PrologSubstitution substitution2 = new PrologSubstitution(solution);
		
		assertEquals("[X/a]", substitution2.toString());
		
		jpl.Variable var1 = new jpl.Variable("Y");
		jpl.Term term1 = new jpl.Atom("b");
		solution.put(var1.name(), term1);
		
		PrologSubstitution substitution3 = new PrologSubstitution(solution);
		
		assertEquals("[Y/b, X/a]", substitution3.toString());
		
		jpl.Variable var2 = new jpl.Variable("Z");
		jpl.Variable var3 = new jpl.Variable("V");
		solution.put(var2.name(), var3);
		
		PrologSubstitution substitution4 = new PrologSubstitution(solution);
		
		assertEquals("[Z/V, Y/b, X/a]", substitution4.toString());
	}

}
