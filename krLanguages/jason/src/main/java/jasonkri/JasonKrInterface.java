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

package jasonkri;

import jason.asSemantics.Unifier;
import jason.asSyntax.VarTerm;
import jasonkri.language.JasonSubstitution;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

/**
 * Implementation of a knowledge representation interface for the proprietary
 * support for an agent's belief base and associated querying of the Jason agent
 * programming language.
 */
public final class JasonKrInterface implements KRInterface {

	@Override
	public void initialize(List<URI> uris) throws KRInitFailedException {
	}

	@Override
	public void release() throws KRDatabaseException {
	}

	@Override
	public Database getDatabase(String name, Collection<DatabaseFormula> content)
			throws krTools.exceptions.KRDatabaseException {
		// FIXME do something with name
		JasonDatabase dbase = new JasonDatabase(content);
		return dbase;
	}

	@Override
	public Parser getParser(Reader source, SourceInfo info)
			throws ParserException {
		return new JasonParser(source, info);
	}

	@Override
	public Substitution getSubstitution(Map<Var, Term> map) {
		Unifier unifier = new Unifier();
		for (Var v : map.keySet()) {
			unifier.bind((VarTerm) ((JasonVar) v).getJasonTerm(),
					((JasonTerm) map.get(v)).getJasonTerm());
		}
		return new JasonSubstitution(unifier);
	}

	@Override
	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		return new HashSet<Query>(); // FIXME
	}

	@Override
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		return new HashSet<DatabaseFormula>(); // FIXME

	}

	@Override
	public boolean supportsSerialization() {
		return false; // Can be fixed
	}

}