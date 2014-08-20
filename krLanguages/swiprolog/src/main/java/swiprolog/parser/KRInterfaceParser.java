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
	 * @param stream The input stream.
	 * @throws ParserException If an exception occurred during parsing. See {@link ParserException}.
	 */
	public KRInterfaceParser(ANTLRReaderStream stream) throws ParserException {
		try {
			PrologLexer lexer = new PrologLexer(stream);
			lexer.initialize();
			LinkedListTokenSource linker = new LinkedListTokenSource(lexer);
			LinkedListTokenStream tokenStream = new LinkedListTokenStream(linker);
			parser = new PrologParser(tokenStream);
			parser.setInput(lexer, stream);
			parser.initialize();
		} catch (Exception e) {
			throw new ParserException("Could not initialize "
					+ "the Prolog parser", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SourceInfo> getErrors() {
		List<SourceInfo> errors = new ArrayList<SourceInfo>();
		errors.addAll(parser.getLexer().getErrors());
		for (ParserException e : parser.getErrors())
			errors.add((SourceInfo) e.getCause().getCause());
		return errors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Update parseUpdate() {
		return parser.ParseUpdateOrEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DatabaseFormula> parseDBFs() {
		return parser.ParsePrologProgram();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query parseQuery() {
		return parser.ParseQuery();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Term parseTerm() {
		return parser.ParseTerm();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Var parseVar() throws ParserException {
		PrologTerm term;
		try {
			term = parser.term0();
		} catch (RecognitionException e) {
			throw new ParserException(e.getMessage(), e);
		}
		if (term.isVar()) {
			return new PrologVar((Variable) term.getTerm());
		} else {
			throw new ParserException(String.format("Expected a Prolog variable but found '%s'", term.toString()));
		}
	}
	
	/**
	 * 
	 */
	public String[] getTokenNames() {
		return parser.getTokenNames();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Term> parseTerms() {
		return parser.ParsePrologTerms();
	}

}