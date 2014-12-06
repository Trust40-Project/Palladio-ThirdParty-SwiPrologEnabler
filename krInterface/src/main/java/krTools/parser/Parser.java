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

package krTools.parser;

import java.util.List;

import krTools.KRInterface;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;

/**
 * Interface for a parser for the KR language.
 *
 * <p>
 * Also see: {@link KRInterface#getParser(java.io.Reader)}.
 */
public interface Parser {

	/**
	 * Parses the input. Returns the (database) formulas found during parsing.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	List<DatabaseFormula> parseDBFs(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns the queries found during parsing.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	List<Query> parseQueries(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns the query found during parsing.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	Query parseQuery(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns the update found during parsing.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	Update parseUpdate(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns a variable obtained by parsing the input.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	Var parseVar(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns a term obtained by parsing the input.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	Term parseTerm(SourceInfo info) throws ParserException;

	/**
	 * Parses the input. Returns a list of terms obtained by parsing the input.
	 *
	 * @param info
	 *            A source info object.
	 * @throws ParserException
	 *             If parsing was interrupted.
	 */
	List<Term> parseTerms(SourceInfo info) throws ParserException;

	/**
	 * @return The list of all parsing errors that occurred while parsing.
	 */
	List<SourceInfo> getErrors();

}