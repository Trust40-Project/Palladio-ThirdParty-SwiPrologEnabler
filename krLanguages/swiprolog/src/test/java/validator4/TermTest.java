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

package validator4;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import jpl.Term;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;

import org.junit.Test;

import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import swiprolog.parser.Parser4;
import swiprolog.validator.Validator4;
import swiprolog.validator.Validator4Internal;
import visitor.Visitor4;

/**
 * Tests for {@link Validator4Internal} to see if pipeline
 * parser->visitor->validator works ok. This is an end-to-end test.
 *
 */
public class TermTest {

	/**
	 * Create a new prolog4 validator for the test, using given string as input
	 * stream.
	 * 
	 * @param in
	 *            the input string for the validator.
	 * @return {@link Validator4Internal}
	 * @throws IOException
	 */
	public Validator4 validator(String in) throws IOException {
		return new Validator4(new Visitor4(new Parser4(new StringReader(in),
				null)));
	}

	@Test
	public void testValidateFloat() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("100.4").term();
		assertEquals(term, new PrologTerm(new jpl.Float(100.4), null));
	}

	@Test
	public void testValidateAtom() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("aap").term();
		assertEquals(term, new PrologTerm(new jpl.Atom("aap"), null));
	}

	@Test
	public void testValidate1arg() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("aap(1)").term();
		assertEquals(term, new PrologTerm(new jpl.Compound("aap",
				new Term[] { new jpl.Integer(1) }), null));
	}

	@Test
	public void testValidate2arg() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("aap(1,2)").term();
		assertEquals(term, new PrologTerm(new jpl.Compound("aap", new Term[] {
				new jpl.Integer(1), new jpl.Integer(2) }), null));
	}

	@Test
	public void testInteger() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("33").term();
		assertEquals(term, new PrologTerm(new jpl.Integer(33), null));
	}

	@Test
	public void testVariable() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("X").term();
		assertEquals(term, new PrologVar(new jpl.Variable("X"), null));
	}

	@Test
	public void testVariable2() throws IOException, KRInitFailedException,
			ParserException {
		PrologTerm term = validator("_123").term();
		assertEquals(term, new PrologVar(new jpl.Variable("_123"), null));

	}

}
