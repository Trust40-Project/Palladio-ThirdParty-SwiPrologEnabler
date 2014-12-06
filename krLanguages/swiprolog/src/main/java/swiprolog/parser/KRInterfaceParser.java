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
import org.antlr.runtime.RecognitionException;

import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;

/**
 * Implementation of KR interface parser for SWI Prolog.
 */
public class KRInterfaceParser implements Parser {

	/**
	 * The ANTLR generated parser for Prolog.
	 */
	private PrologParser parser;

	/**
	 * Creates a new KR interface parser that uses the given stream as input.
	 *
	 * @param stream
	 *            The input stream.
	 * @throws ParserException
	 *             If an exception occurred during parsing. See
	 *             {@link ParserException}.
	 */
	public KRInterfaceParser(ANTLRReaderStream stream) throws ParserException {
		try {
			PrologLexer lexer = new PrologLexer(stream);
			lexer.initialize();
			LinkedListTokenSource linker = new LinkedListTokenSource(lexer);
			LinkedListTokenStream tokenStream = new LinkedListTokenStream(
					linker);
			this.parser = new PrologParser(tokenStream);
			this.parser.setInput(lexer, stream);
			this.parser.initialize();
		} catch (Exception e) {
			throw new ParserException("Could not initialize "
					+ "the Prolog parser", e);
		}
	}

	@Override
	public Update parseUpdate(SourceInfo info) {
		return this.parser.ParseUpdateOrEmpty(info);
	}

	@Override
	public List<DatabaseFormula> parseDBFs(SourceInfo info)
			throws ParserException {
		return this.parser.parsePrologProgram(info);
	}

	@Override
	public List<Query> parseQueries(SourceInfo info) throws ParserException {
		return this.parser.parsePrologGoalSection(info);
	}

	/**
	 * Allows empty queries.
	 */
	@Override
	public Query parseQuery(SourceInfo info) {
		return this.parser.ParseQueryOrEmpty(info);
	}

	@Override
	public Var parseVar(SourceInfo info) throws ParserException {
		PrologTerm term;
		try {
			term = this.parser.term0();
		} catch (RecognitionException e) {
			throw new ParserException(e.getMessage(), e);
		}
		if (term.isVar()) {
			return new PrologVar((Variable) term.getTerm(), info);
		} else {
			throw new ParserException(String.format(
					"Expected a Prolog variable but found '%s'",
					term.toString()));
		}
	}

	@Override
	public Term parseTerm(SourceInfo info) {
		return new PrologTerm(this.parser.ParseTerm().getTerm(), info);
	}

	@Override
	public List<Term> parseTerms(SourceInfo info) {
		return this.parser.ParsePrologTerms(info);
	}

	@Override
	public List<SourceInfo> getErrors() {
		List<SourceInfo> errors = new ArrayList<SourceInfo>();
		List<ParserException> exceptions = new ArrayList<ParserException>();
		exceptions.addAll(this.parser.getLexer().getErrors());
		exceptions.addAll(this.parser.getErrors());
		for (ParserException e : exceptions) {
			if (!e.hasSourceInfo()) {
				// try to recover source info from cause
				if (e.getCause() instanceof RecognitionException) {
					int line = ((RecognitionException) e.getCause()).line;
					int charPos = ((RecognitionException) e.getCause()).charPositionInLine;
					SourceInfoObject info = new SourceInfoObject(line, charPos);
					info.setMessage(e.getMessage());
					errors.add(info);
				}
			} else {
				errors.add(e);
			}
		}

		return errors;
	}

	/**
	 *
	 */
	public String[] getTokenNames() {
		return this.parser.getTokenNames();
	}

}