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

import java.io.StringReader;

import org.junit.Test;

import krTools.language.Term;
import swiprolog.SwiPrologInterface;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologCompoundImpl;
import swiprolog.language.impl.PrologFloatImpl;
import swiprolog.language.impl.PrologIntImpl;
import swiprolog.language.impl.PrologVarImpl;
import swiprolog.parser.Parser4;
import swiprolog.validator.Validator4;
import swiprolog.visitor.Visitor4;

/**
 * Tests for {@link Validator4Internal} to see if pipeline
 * parser->visitor->validator works ok. This is an end-to-end test.
 */
public class TermTest {
	/**
	 * Create a new prolog4 validator for the test, using given string as input
	 * stream.
	 *
	 * @param in
	 *            the input string for the validator.
	 * @return {@link Validator4Internal}
	 */
	public Validator4 validator(String in) throws Exception {
		new SwiPrologInterface();
		return new Validator4(new Visitor4(new Parser4(new StringReader(in), null)));
	}

	@Test
	public void testValidateFloat() throws Exception {
		Term term = validator("100.4").term();
		assertEquals(term, new PrologFloatImpl(100.4, null));
	}

	@Test
	public void testValidateAtom() throws Exception {
		Term term = validator("aap").term();
		assertEquals(term, new PrologAtomImpl("aap", null));
	}

	@Test
	public void testValidate1arg() throws Exception {
		Term term = validator("aap(1)").term();
		assertEquals(term, new PrologCompoundImpl("aap", new Term[] { new PrologIntImpl(1, null) }, null));
	}

	@Test
	public void testValidate2arg() throws Exception {
		Term term = validator("aap(1,2)").term();
		assertEquals(term, new PrologCompoundImpl("aap",
				new Term[] { new PrologIntImpl(1, null), new PrologIntImpl(2, null) }, null));
	}

	@Test
	public void testInteger() throws Exception {
		Term term = validator("33").term();
		assertEquals(term, new PrologIntImpl(33, null));
	}

	@Test
	public void testVariable() throws Exception {
		Term term = validator("X").term();
		assertEquals(term, new PrologVarImpl("X", null));
	}

	@Test
	public void testVariable2() throws Exception {
		Term term = validator("_123").term();
		assertEquals(term, new PrologVarImpl("_123", null));
	}
}
