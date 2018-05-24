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

package swiprolog.parser;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologDBFormulaImpl;
import swiprolog.language.impl.PrologQueryImpl;
import swiprolog.language.impl.PrologVarImpl;

/**
 * Analyzer to identify unused and undefined predicates.
 */
public class Analyzer {
	private static final Term ANON_VAR = new PrologVarImpl("_", null);
	/**
	 * Map of definitions.
	 */
	private final Map<String, List<PrologDBFormula>> definitions = new LinkedHashMap<>();
	/**
	 * Map of queries.
	 */
	private final Map<String, List<PrologQuery>> used = new LinkedHashMap<>();
	/**
	 * Input
	 */
	private final Set<DatabaseFormula> dbfs;
	private final Set<Query> queries;
	/**
	 * Output
	 */
	private final Set<String> undefined = new LinkedHashSet<>();
	private final Set<String> unused = new LinkedHashSet<>();

	/**
	 * Creates an analyzer.
	 */
	public Analyzer(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		this.dbfs = dbfs;
		this.queries = queries;
	}

	public void analyze() {
		for (DatabaseFormula dbf : this.dbfs) {
			addDefinition(dbf);
		}
		for (Query query : this.queries) {
			addQuery(query);
		}
		this.undefined.addAll(this.used.keySet());
		this.undefined.removeAll(this.definitions.keySet());
		this.unused.addAll(this.definitions.keySet());
		this.unused.removeAll(this.used.keySet());
	}

	public Set<Query> getUndefined() {
		Set<Query> undefined = new LinkedHashSet<>();
		for (String undf : this.undefined) {
			undefined.addAll(this.used.get(undf));
		}
		return undefined;
	}

	public Set<DatabaseFormula> getUnused() {
		Set<DatabaseFormula> unused = new LinkedHashSet<>();
		for (String df : this.unused) {
			unused.addAll(this.definitions.get(df));
		}
		return unused;
	}

	/**
	 * Assumes the given DatabaseFormula is either a single term, or the :-/2
	 * function.
	 */
	private void addDefinition(DatabaseFormula formula) {
		PrologCompound plFormula = ((PrologDBFormula) formula).getCompound();
		PrologCompound headTerm = plFormula;
		if (plFormula.getName().equals(":-")) {
			if (plFormula.getArity() == 1 && plFormula.getArg(0) instanceof PrologCompound) {
				// Directive: the first argument is a query.
				addQuery(new PrologQueryImpl((PrologCompound) plFormula.getArg(0)));
			} else if (plFormula.getArity() == 2 && plFormula.getArg(0) instanceof PrologCompound
					&& plFormula.getArg(1) instanceof PrologCompound) {
				// The first argument is the only defined term.
				headTerm = (PrologCompound) plFormula.getArg(0);
				// The other argument is a conjunction of queried terms.
				addQuery(new PrologQueryImpl((PrologCompound) plFormula.getArg(1)));
			}
		}

		String headSig = headTerm.getSignature();
		// Ignore built-in operators.
		if (!PrologOperators.prologBuiltin(headSig)) {
			// Add a new definition node
			List<PrologDBFormula> formulas;
			if (this.definitions.containsKey(headSig)) {
				formulas = this.definitions.get(headSig);
			} else {
				formulas = new LinkedList<>();
			}
			formulas.add(new PrologDBFormulaImpl(headTerm));
			this.definitions.put(headSig, formulas);
		}
	}

	/**
	 * Add a query (a use of predicates).
	 */
	public void addQuery(Query query) {
		addQuery(((PrologQuery) query).getCompound(), query.getSourceInfo());
	}

	/**
	 * Add predicates used in a clause as queries.
	 */
	public void addQuery(DatabaseFormula formula) {
		// we may assume the formula is a single term, so we can just
		// as well handle the inner term as a general term.
		addQuery(((PrologDBFormula) formula).getCompound(), formula.getSourceInfo());
	}

	private void addQuery(Term term, SourceInfo info) {
		// check if the term needs to be unpacked
		if (!(term instanceof PrologCompound)) {
			return;
		}
		PrologCompound plTerm = (PrologCompound) term;
		String termSig = plTerm.getSignature();
		// there is only one /1 operator we need to unpack: not/1
		if (termSig.equals("not/1") || termSig.equals("+/1") || termSig.equals("include/3")
				|| termSig.equals("exclude/3") || termSig.startsWith("partition/") || termSig.startsWith("maplist/")
				|| termSig.equals("convlist/3") || termSig.startsWith("foldl/") || termSig.startsWith("scanl/")
				|| termSig.equals("free_variables/4")) {
			addQuery(plTerm.getArg(0), info);
		} else if (termSig.equals(";/2") || termSig.equals("|/2") || termSig.equals(",/2") || termSig.equals("->/2")
				|| termSig.equals("*->/2") || termSig.equals("forall/2") || termSig.equals("foreach/2")) {
			// unpack the conjunction, disjunction and forall /2-operators
			addQuery(plTerm.getArg(0), info);
			addQuery(plTerm.getArg(1), info);
		} else if (termSig.startsWith("findall/") || termSig.equals("setof/3") || termSig.equals("bagof/3")
				|| termSig.equals("aggregate/3") || termSig.equals("aggregate_all/3")) {
			// findall, setof aggregate and aggregate_all /3-operators only
			// have a query in the second argument.
			addQuery(plTerm.getArg(1), info);
		} else if (termSig.equals("aggregate/4") || termSig.equals("aggregate_all/4")
				|| termSig.startsWith("findnsols/")) {
			// aggregate and aggregate_all /4-operators have the query in
			// the third argument.
			addQuery(plTerm.getArg(2), info);
		} else if (termSig.equals("predsort/3") && plTerm.getArg(0) instanceof PrologCompound) {
			// first argument is name that will be called as name/3
			Term stubfunc = new PrologCompoundImpl(((PrologCompound) plTerm.getArg(0)).getName(),
					new Term[] { ANON_VAR, ANON_VAR, ANON_VAR }, plTerm.getSourceInfo());
			addQuery(stubfunc, info);
		} else if (termSig.equals("dynamic/1") && plTerm.getArg(0) instanceof PrologCompound) {
			// recognize predicate declaration(s).
			PrologCompound compound = (PrologCompound) plTerm.getArg(0);
			for (Term dynamicPred : compound.getOperands(",")) {
				addDefinition(new PrologDBFormulaImpl((PrologCompound) dynamicPred));
			}
		} else if (!PrologOperators.prologBuiltin(termSig)) {
			// if we get here, the term should not be unpacked
			// but needs to be added as a query node
			// Add a new definition node
			List<PrologQuery> formulas;
			if (this.used.containsKey(termSig)) {
				formulas = this.used.get(termSig);
			} else {
				formulas = new LinkedList<>();
			}
			formulas.add(new PrologQueryImpl(plTerm));
			this.used.put(termSig, formulas);
		}
	}
}
