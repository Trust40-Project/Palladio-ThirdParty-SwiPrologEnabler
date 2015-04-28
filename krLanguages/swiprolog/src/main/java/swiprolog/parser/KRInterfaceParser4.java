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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Implementation of KR interface parser for SWI Prolog that is based on antlr4.
 */
public class KRInterfaceParser4 implements Parser {
	private final ANTLRInputStream stream;
	/**
	 * The ANTLR4 generated parser for Prolog.
	 */
	private final ErrorStoringProlog4Parser parser;

	/**
	 * Creates a new KR interface parser that uses the given stream as input.
	 *
	 * @param r
	 *            The input stream.
	 * @param info
	 *            the {@link SourceInfo} for the fragment to be parsed
	 * @throws IOException
	 * @throws ParserException
	 *             If an exception occurred during parsing. See
	 *             {@link ParserException}.
	 */
	public KRInterfaceParser4(Reader r, SourceInfo info) throws IOException {
		stream = new ANTLRInputStream(r);
		stream.name = (info.getSource() == null) ? "" : info.getSource()
				.getPath();
		// this.start = info.getStartIndex();
		Prolog4Lexer lexer = new Prolog4Lexer(stream);
		lexer.setLine(info.getLineNumber());
		lexer.setCharPositionInLine(info.getCharacterPosition());
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		this.parser = new ErrorStoringProlog4Parser(tokens);
	}

	@Override
	public Update parseUpdate() {
		return null;
		// ParseTree tree = parser.possiblyEmptyConjunct();
		// if (!parser.isSuccess()) {
		// return null;
		// }
		// parser.visit(tree);
		// return this.parser.ParseUpdateOrEmpty();
	}

	@Override
	public List<DatabaseFormula> parseDBFs() throws ParserException {
		return null;
		// return this.parser.parsePrologProgram();
	}

	@Override
	public List<Query> parseQueries() throws ParserException {
		return null;
		// return this.parser.parsePrologGoalSection();
	}

	/**
	 * Allows empty queries.
	 */
	@Override
	public Query parseQuery() {
		return null;
		// return this.parser.ParseQueryOrEmpty();
	}

	@Override
	public Var parseVar() throws ParserException {
		return null;
		// PrologTerm term;
		// try {
		// term = this.parser.term0();
		// } catch (RecognitionException e) {
		// int start = this.start + e.index;
		// int end = (e.token == null || e.token.getText() == null) ? start
		// : (start + e.token.getText().length());
		// final SourceInfoObject source = new SourceInfoObject(
		// this.parser.getSource(), e.line, e.charPositionInLine,
		// start, end);
		// throw new ParserException(
		// "data could not be parsed as a SWI term0", source, e);
		// }
		// if (term.isVar()) {
		// return new PrologVar((Variable) term.getTerm(),
		// term.getSourceInfo());
		// } else {
		// throw new ParserException(String.format(
		// "expected a SWI prolog variable but found '%s'",
		// term.toString()), term.getSourceInfo());
		// }
	}

	@Override
	public Term parseTerm() {
		return null;
		// return this.parser.ParseTerm();
	}

	@Override
	public List<Term> parseTerms() {
		return null;
		// return this.parser.ParsePrologTerms();
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

	public String[] getTokenNames() {
		return null;
		// return this.parser.getTokenNames();
	}
}