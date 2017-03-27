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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
	 * TODO: Check!!!
	 *
	 * Substitution stored as a {@link Map} with tuples {@link String},
	 * {@link jpl.Term} indicating a substitution of variable for term. We do
	 * not use {@link jpl.Variable} as keys because {@link jpl.Variable} has no
	 * implementation for hashCode and therefore putting these in a map will
	 * fail #2211. Using String brings us closest to what JPL is doing
	 * internally.
	 */

	/**
	 * Create empty JPL substitution.
	 */
	private SortedMap<String, jpl.Term> jplSubstitution = new TreeMap<String, jpl.Term>() {
		private static final long serialVersionUID = 2720402569508083187L;

		@Override
		public jpl.Term put(String varname, jpl.Term term) {
			if (varname != null && !varname.isEmpty() && !varname.equals("_")) {
				return super.put(varname, term);
			} else {
				return null;
			}
		}
	};

	/**
	 * Creates an empty {@link Substitution}.
	 */
	public PrologSubstitution() {
	}

	/**
	 * Create {@link PrologSubstitution} from JPL substitution.
	 *
	 * @param solutions
	 *            JPL substitution.
	 */
	private PrologSubstitution(SortedMap<String, jpl.Term> solution) {
		for (String var : solution.keySet()) {
			this.jplSubstitution.put(var, solution.get(var));
		}
	}

	public static PrologSubstitution getSubstitutionOrNull(SortedMap<String, jpl.Term> solution) {
		if (solution == null) {
			return null;
		} else {
			return new PrologSubstitution(solution);
		}
	}

	/**
	 * @return A JPL substitution.
	 */
	public SortedMap<String, jpl.Term> getJPLSolution() {
		return this.jplSubstitution;
	}

	/**
	 * Returns the list of {@link Var}iables bound by this
	 * {@link PrologSubstitution}.
	 *
	 * <p>
	 * Source information, if available, is lost.
	 * </p>
	 *
	 * @return The variables in the domain of this substitution.
	 */
	@Override
	public List<Var> getVariables() {
		List<Var> variables = new ArrayList<>(this.jplSubstitution.size());
		// Build VariableTerm from jpl.Variable.
		for (String varname : this.jplSubstitution.keySet()) {
			jpl.Variable var = new Variable(varname);
			variables.add(new PrologVar(var, null));
		}
		return variables;
	}

	@Override
	public Term get(Var variable) {
		jpl.Variable jplvar = (jpl.Variable) ((PrologVar) variable).getTerm();
		if (this.jplSubstitution.containsKey(jplvar.name())) {
			return new PrologTerm(this.jplSubstitution.get(jplvar.name()), null);
		} else {
			return null;
		}
	}

	@Override
	public void addBinding(Var v, Term term) {
		jpl.Variable var = (jpl.Variable) ((PrologVar) v).getTerm();
		if (this.jplSubstitution.containsKey(var.name())) {
			throw new RuntimeException(
					"attempt to add '" + v + "' to substitution " + this + " that already binds the variable.");
		}
		this.jplSubstitution.put(var.name(), ((PrologTerm) term).getTerm());
	}

	@Override
	public Substitution combine(Substitution substitution) {
		SortedMap<String, jpl.Term> combined = null;
		if (substitution != null) {
			combined = JPLUtils.combineSubstitutions(this.jplSubstitution,
					((PrologSubstitution) substitution).getJPLSolution());
		}
		return getSubstitutionOrNull(combined);
	}

	@Override
	public boolean remove(Var variable) {
		jpl.Variable var = (jpl.Variable) ((PrologVar) variable).getTerm();
		if (this.jplSubstitution.containsKey(var.name())) {
			return this.jplSubstitution.remove(var.name()) != null;
		} else {
			return false;
		}
	}

	@Override
	public boolean retainAll(Collection<Var> varsToRetain) {
		Set<String> varnamesToRetain = new HashSet<>(varsToRetain.size());
		for (Var v : varsToRetain) {
			varnamesToRetain.add(((PrologVar) v).getVariable().name());
		}
		Set<String> currentVars = new HashSet<>(this.jplSubstitution.keySet());

		boolean removed = false;
		for (String varname : currentVars) {
			if (!varnamesToRetain.contains(varname)) {
				this.jplSubstitution.remove(varname);
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public PrologSubstitution clone() {
		return new PrologSubstitution(new TreeMap<>(this.jplSubstitution));
	}

	/**
	 * Returns a string representation of this {@link PrologSubstitution}.
	 *
	 * @return The string representation of this substitution.
	 */
	@Override
	public String toString() {
		Set<String> variables = this.jplSubstitution.keySet();

		StringBuilder builder = new StringBuilder();

		builder.append("[");
		boolean addComma = false;

		for (String var : variables) {
			if (addComma) {
				builder.append(", ");
			}
			builder.append(var).append("/");
			PrologTerm term = new PrologTerm(this.jplSubstitution.get(var), null);
			builder.append(term.toString());
			addComma = true;
		}
		builder.append("]");

		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int h = 1;
		Iterator<Entry<String, jpl.Term>> i = this.jplSubstitution.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, jpl.Term> e = i.next();
			h = prime * h + e.getKey().hashCode();
			h = prime * h + JPLUtils.hashCode(e.getValue());
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof PrologSubstitution)) {
			return false;
		}
		PrologSubstitution other = (PrologSubstitution) obj;
		if (this.jplSubstitution == null) {
			if (other.jplSubstitution != null) {
				return false;
			}
		} else if (this.jplSubstitution.size() != other.jplSubstitution.size()) {
			return false;
		} else {
			Iterator<Entry<String, jpl.Term>> i = this.jplSubstitution.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, jpl.Term> e = i.next();
				if (e.getValue() == null) {
					if (!(other.jplSubstitution.get(e.getKey()) == null
							&& other.jplSubstitution.containsKey(e.getKey()))) {
						return false;
					}
				} else {
					if (!JPLUtils.equals(e.getValue(), other.jplSubstitution.get(e.getKey()))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
