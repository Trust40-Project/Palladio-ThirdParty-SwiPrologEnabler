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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpl.Compound;
import jpl.Term;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.parser.SourceInfo;
import swiprolog.parser.PrologOperators;

/**
 * Analyzer to identify unused and undefined predicates.
 */
public class Analyzer {

	private static final Term ANON_VAR = new jpl.Variable("_");

	/**
	 * Map of definitions.
	 */
	private final Map<String, List<PrologDBFormula>> definitions = new LinkedHashMap<String, List<PrologDBFormula>>();
	/**
	 * Map of queries.
	 */
	private final Map<String, List<PrologQuery>> used = new LinkedHashMap<String, List<PrologQuery>>();

	/**
	 * Input
	 */
	private final Set<DatabaseFormula> dbfs;
	private final Set<Query> queries;

	/**
	 * Output
	 */
	private final Set<String> undefined = new HashSet<>();
	private final Set<String> unused = new HashSet<>();

	/**
	 * Creates an analyzer.
	 */
	public Analyzer(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		this.dbfs = dbfs;
		this.queries = queries;
	}

	public void analyze() {
		for (DatabaseFormula dbf : dbfs) {
			addDefinition(dbf);
		}
		for (Query query : queries) {
			addQuery(query);
		}
		undefined.addAll(used.keySet());
		undefined.removeAll(definitions.keySet());
		unused.addAll(definitions.keySet());
		unused.removeAll(used.keySet());
	}

	public Set<Query> getUndefined() {
		Set<Query> undefined = new HashSet<>();
		for (String undf : this.undefined) {
			undefined.addAll(used.get(undf));
		}

		return undefined;
	}

	public Set<DatabaseFormula> getUnused() {
		Set<DatabaseFormula> unused = new HashSet<>();
		for (String df : this.unused) {
			unused.addAll(definitions.get(df));
		}

		return unused;
	}

	/**
	 * Assumes the given DatabaseFormula is either a single term, or the :-/2
	 * function.
	 */
	private void addDefinition(DatabaseFormula formula) {
		PrologDBFormula plFormula = (PrologDBFormula) formula;
		jpl.Term term = plFormula.getTerm();
		jpl.Term headTerm = term;
		// The :- function needs special attention.
		if (term.name().equals(":-") && term.arity() == 2) {
			// The first argument is the only defined term.
			headTerm = term.arg(1);
			// The other argument is a conjunction of queried terms.
			this.addQuery(new PrologQuery(term.arg(2), formula.getSourceInfo()));
		}

		String headSig = headTerm.name() + "/" + headTerm.arity();
		// Ignore built-in operators.
		if (PrologOperators.prologBuiltin(headSig)) {
			return;
		}
		// Add a new definition node
		List<PrologDBFormula> formulas;
		if (definitions.containsKey(headSig)) {
			formulas = definitions.get(headSig);
		} else {
			formulas = new ArrayList<>();
		}
		formulas.add(new PrologDBFormula(headTerm, formula.getSourceInfo()));
		definitions.put(headSig, formulas);
	}

	/**
	 * Add a query (a use of predicates).
	 */
	public void addQuery(Query query) {
		addQuery(((PrologQuery) query).getTerm(), query.getSourceInfo());
	}

	/**
	 * Add predicates used in a clause as queries.
	 */
	public void addQuery(DatabaseFormula formula) {
		// we may assume the formula is a single term, so we can just
		// as well handle the inner term as a general term.
		this.addQuery(((PrologDBFormula) formula).getTerm(), formula.getSourceInfo());
	}

	private void addQuery(jpl.Term plTerm, SourceInfo info) {
		// check if the term needs to be unpacked
		String termSig = plTerm.name() + "/" + plTerm.arity();

		// there is only one /1 operator we need to unpack: not/1
		if (termSig.equals("not/1")) {
			this.addQuery(plTerm.arg(1), info);
			// unpack the conjunction, disjunction and forall /2-operators
		} else if (termSig.equals(";/2") || termSig.equals(",/2") || termSig.equals("forall/2")) {
			this.addQuery(plTerm.arg(1), info);
			this.addQuery(plTerm.arg(2), info);
			// findall, setof aggregate and aggregate_all /3-operators only
			// have a query in the second argument.
		} else if (termSig.equals("findall/3") || termSig.equals("setof/3") || termSig.equals("aggregate/3")
				|| termSig.equals("aggregate_all/3")) {
			this.addQuery(plTerm.arg(2), info);
			// aggregate and aggregate_all /4-operators have the query in
			// the third argument.
		} else if (termSig.equals("aggregate/4") || termSig.equals("aggregate_all/4")) {
			this.addQuery(plTerm.arg(3), info);
		} else if (termSig.equals("predsort/3")) {
			// first argument is name that will be called as name/3
			jpl.Term stubfunc = new Compound(plTerm.arg(1).name(), new jpl.Term[] { ANON_VAR, ANON_VAR, ANON_VAR });
			this.addQuery(stubfunc, info);
		} else {
			// do nothing if the term is a built-in prolog operator
			if (PrologOperators.prologBuiltin(termSig)) {
				return;
			}
			// if we get here, the term should not be unpacked
			// but needs to be added as a query node
			// Add a new definition node
			List<PrologQuery> formulas;
			if (used.containsKey(termSig)) {
				formulas = used.get(termSig);
			} else {
				formulas = new ArrayList<>();
			}
			formulas.add(new PrologQuery(plTerm, info));
			used.put(termSig, formulas);
		}
	}

}
