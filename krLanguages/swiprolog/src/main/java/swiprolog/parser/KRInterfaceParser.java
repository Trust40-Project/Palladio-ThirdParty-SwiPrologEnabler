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

import java.util.ArrayList;
import java.util.List;

import jpl.Variable;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;

/**
 * Implementation of KR interface parser for SWI Prolog.
 */
public class KRInterfaceParser implements Parser {
	private final IntStream stream;
	/**
	 * The ANTLR generated parser for Prolog.
	 */
	private final PrologParser parser;

	/**
	 * Creates a new KR interface parser that uses the given stream as input.
	 *
	 * @param stream
	 *            The input stream.
	 * @throws ParserException
	 *             If an exception occurred during parsing. See
	 *             {@link ParserException}.
	 */
	public KRInterfaceParser(ANTLRReaderStream stream) {
		this.stream = stream;
		PrologLexer lexer = new PrologLexer(stream);
		lexer.initialize();
		LinkedListTokenSource linker = new LinkedListTokenSource(lexer);
		LinkedListTokenStream tokenStream = new LinkedListTokenStream(linker);
		this.parser = new PrologParser(tokenStream);
		this.parser.setInput(lexer, stream);
		this.parser.initialize();
	}

	@Override
	public Update parseUpdate() {
		return this.parser.ParseUpdateOrEmpty();
	}

	@Override
	public List<DatabaseFormula> parseDBFs() throws ParserException {
		return this.parser.parsePrologProgram();
	}

	@Override
	public List<Query> parseQueries() throws ParserException {
		return this.parser.parsePrologGoalSection();
	}

	/**
	 * Allows empty queries.
	 */
	@Override
	public Query parseQuery() {
		return this.parser.ParseQueryOrEmpty();
	}

	@Override
	public Var parseVar() throws ParserException {
		PrologTerm term;
		try {
			term = this.parser.term0();
		} catch (RecognitionException e) {
			int start = e.index;
			int end = (e.token == null || e.token.getText() == null) ? start
					: (start + e.token.getText().length());
			final SourceInfoObject source = new SourceInfoObject(
					this.parser.getSource(), e.line, e.charPositionInLine,
					start, end);
			throw new ParserException(
					"data could not be parsed as a SWI term0", source, e);
		}
		if (term.isVar()) {
			return new PrologVar((Variable) term.getTerm(),
					term.getSourceInfo());
		} else {
			throw new ParserException(String.format(
					"expected a SWI prolog variable but found '%s'",
					term.toString()), term.getSourceInfo());
		}
	}

	@Override
	public Term parseTerm() {
		return this.parser.ParseTerm();
	}

	@Override
	public List<Term> parseTerms() {
		return this.parser.ParsePrologTerms();
	}

	@Override
	public List<SourceInfo> getErrors() {
		// Get all (syntax)errors from the lexer or the parser
		List<SourceInfo> exceptions = new ArrayList<SourceInfo>();
		exceptions.addAll(this.parser.getLexer().getErrors());
		exceptions.addAll(this.parser.getErrors());

		// Check if we processed the whole stream we were given
		final int index = this.stream.index();
		final int size = this.stream.size();
		if (size - index > 1) {
			final SourceInfoObject error = new SourceInfoObject(
					this.parser.getSource(), this.parser.getLexer().getLine(),
					this.parser.getLexer().getCharPositionInLine(), index,
					size - 1);
			exceptions.add(new ParserException("Unrecognized spurious input",
					error));
		}

		// Return
		return exceptions;
	}

	public String[] getTokenNames() {
		return this.parser.getTokenNames();
	}
}