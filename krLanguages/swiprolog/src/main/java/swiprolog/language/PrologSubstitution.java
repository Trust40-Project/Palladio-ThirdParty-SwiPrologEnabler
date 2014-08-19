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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

import jpl.Variable;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * A substitution is a mapping of distinct variables to terms. A substitution is
 * said to bind the term to the variable if it maps the variable to the term. A
 * substitution may be empty.
 */
public class PrologSubstitution implements Substitution {
	
	/**
	 * Create empty JPL substitution.
	 */
	private Hashtable<String, jpl.Term> jplSubstitution = new Hashtable<String, jpl.Term>();

	/**
	 * TODO: Check!!!
	 * 
	 * Substitution stored as a {@link Map} with tuples {@link String},
	 * {@link jpl.Term} indicating a substitution of variable for term. We do
	 * not use {@link jpl.Variable} as keys because {@link jpl.Variable} has no
	 * implementation for hashCode and therefore putting these in a map will
	 * fail #2211. Using String brings us closest to what JPL is doing
	 * internally.
	 */
	// private Map<String, jpl.Term> substitution = new LinkedHashMap<String, jpl.Term>();

	/**
	 * Creates an empty {@link Substitution}.
	 */
	public PrologSubstitution() {
	}

	/**
	 * Creates a substitution from a single variable and term.
	 * 
	 * @param var Variable that is bound.
	 * @param term Term that is bound to variable.
	 */
	public PrologSubstitution(jpl.Variable var, jpl.Term term) {		
		jplSubstitution.put(var.name(), term);
	}
	
	/**
	 * Create {@link PrologSubstitution} from JPL substitution.
	 * 
	 * @param solutions JPL substitution.
	 */
	public PrologSubstitution(Hashtable<String, jpl.Term> solution) {
		this.jplSubstitution = solution;
	}
	
	/**
	 * Returns JPL substitution.
	 * 
	 * @return
	 */
	public Hashtable<String, jpl.Term> getJPLSolution() {
		return this.jplSubstitution;
	}

	/**
	 * Returns the set of {@link Var}iables bound by this
	 * {@link PrologSubstitution}.
	 * 
	 * @return The variables in the domain of this substitution.
	 */
	public Set<Var> getVariables() {
		ArrayList<String> jplvarnames = new ArrayList<String>(jplSubstitution.keySet());
		Set<Var> variables = new LinkedHashSet<Var>();
		
		// Build VariableTerm from jpl.Variable.
		for (String varname : jplvarnames) {
			jpl.Variable var = new Variable(varname);
			variables.add(new PrologVar(var));
		}
		
		return variables;
	}

	public Term get(Var variable) {
		jpl.Variable jplvar = (jpl.Variable)((PrologVar)variable).getTerm();
		if (jplSubstitution.containsKey(jplvar.name())) {
			return new PrologTerm((jpl.Term)jplSubstitution.get(jplvar.name()));
		} else {
			return null;
		}
	}

	public void addBinding(Var v, Term term) {
		jpl.Variable var = (jpl.Variable)((PrologVar)v).getTerm();
		if (this.jplSubstitution.containsKey(var.name())) {
			throw new RuntimeException("Attempt to add variable " + v
					+ " to substitution " + this
					+ " that already binds the variable.");
		}
		this.jplSubstitution.put(var.name(), ((PrologTerm) term).getTerm());
	}
	
	/**
	 * 
	 */
	public Substitution combine(Substitution substitution) {
		Hashtable<String, jpl.Term> combined = new Hashtable<String, jpl.Term>();
		combined = JPLUtils.combineSubstitutions(jplSubstitution, ((PrologSubstitution)substitution).getJPLSolution());
		return new PrologSubstitution(combined);
	}

	/**
	 * DOC Only used by ListallDoRule...
	 */
	public boolean remove(Var variable) {
		jpl.Variable var = (jpl.Variable)((PrologVar)variable).getTerm();

		if (this.jplSubstitution.containsKey(var)) {
			return this.jplSubstitution.remove(var) != null;
		}
		return false;
	}

	/**
	 * DOC Only used by Macro...
	 */
	public boolean retainAll(Collection<Var> variables) {
		Set<String> vars = this.jplSubstitution.keySet();
		boolean removed = false;
		
		for (Var var : variables) {
			jpl.Variable v = (jpl.Variable)((PrologVar)var).getTerm();
			if (!vars.contains(v)) {
				this.jplSubstitution.remove(v);
				removed = true;
			}
		}

		return removed;
	}

	/**
	 * DOC
	 */
	@SuppressWarnings("unchecked")
	public PrologSubstitution clone() {
		return new PrologSubstitution((Hashtable<String, jpl.Term>) this.jplSubstitution.clone());
	}

	/**
	 * Returns a string representation of this {@link PrologSubstitution}.
	 * 
	 * @return The string representation of this substitution.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public String toString() {	
		Set variables = this.jplSubstitution.keySet();
		
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		boolean addComma = false;
		
		for (Object var : variables) {
			if (addComma) {
				builder.append(", ");
			}
			builder.append(var.toString()).append("/");
			PrologTerm term = new PrologTerm(this.jplSubstitution.get(var));
			builder.append(term.toString());
			addComma = true;
		}
		builder.append("]");

		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jplSubstitution == null) ? 0 : jplSubstitution.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrologSubstitution other = (PrologSubstitution) obj;
		if (jplSubstitution == null) {
			if (other.jplSubstitution != null)
				return false;
		} else if (!jplSubstitution.equals(other.jplSubstitution))
			return false;
		return true;
	}

}
