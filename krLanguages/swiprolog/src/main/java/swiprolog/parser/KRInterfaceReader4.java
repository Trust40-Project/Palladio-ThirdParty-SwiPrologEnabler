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

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import swiprolog.validator.Prolog4Validator;
import visitor.Prolog4VisitorPlus;

/**
 * Implementation of KR interface parser, visitor and validator for SWI Prolog
 * that is based on antlr4.
 */
public class KRInterfaceReader4 implements Parser {
	private final Prolog4Validator validator;

	/**
	 * Creates a new KR interface parser that uses the given stream as input.
	 *
	 * @param r
	 *            The input stream.
	 * @param info
	 *            the {@link SourceInfo} for the fragment to be parsed. If set
	 *            to null, we use a default info object starting at line 1 with
	 *            a file reference set to null.
	 * @throws IOException
	 * @throws ParserException
	 *             If an exception occurred during parsing. See
	 *             {@link ParserException}.
	 */
	public KRInterfaceReader4(Reader r, SourceInfo info) throws IOException {
		validator = new Prolog4Validator(new Prolog4VisitorPlus(
				new ErrorStoringProlog4Parser(r, info)));
	}

	@Override
	public Update parseUpdate() throws ParserException {
		return validator.parseUpdateOrEmpty();
	}

	@Override
	public List<DatabaseFormula> parseDBFs() throws ParserException {
		return validator.parsePrologProgram();
	}

	@Override
	public List<Query> parseQueries() throws ParserException {
		return validator.parsePrologGoalSection();
	}

	/**
	 * Allows empty queries.
	 * 
	 * @throws ParserException
	 */
	@Override
	public Query parseQuery() throws ParserException {
		return validator.ParseQueryOrEmpty();
	}

	@Override
	public Var parseVar() throws ParserException {
		return validator.parseVar();
	}

	@Override
	public Term parseTerm() throws ParserException {
		return validator.ParseTerm();
	}

	@Override
	public List<Term> parseTerms() throws ParserException {
		return validator.ParsePrologTerms();
	}

	@Override
	public List<SourceInfo> getErrors() {
		return null;
		// // Get all (syntax)errors from the lexer or the parser
		// List<SourceInfo> exceptions = new ArrayList<SourceInfo>();
		// exceptions.addAll(this.parser.getLexer().getErrors());
		// exceptions.addAll(this.parser.getErrors());
		//
		// // Check if we processed the whole stream we were given
		// final int index = this.stream.index();
		// final int size = this.stream.size();
		// if (size - index > 0) {
		// final SourceInfoObject error = new SourceInfoObject(
		// this.parser.getSource(), this.parser.getLexer().getLine(),
		// this.parser.getLexer().getCharPositionInLine(), this.start
		// + index, this.start + (size - 1));
		// exceptions.add(new ParserException("Unrecognized spurious input",
		// error));
		// }
		//
		// // Return
		// return exceptions;
	}

}