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

import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;

import org.junit.Before;
import org.junit.Test;

import swiprolog.SWIPrologInterface;
import swiprolog.errors.ParserErrorMessages;
import swiprolog.parser.Parser4;
import swiprolog.validator.Validator4;
import swiprolog.visitor.Visitor4;

/**
 * Tests for {@link Validator4Internal} to see if pipeline
 * parser->visitor->validator works ok. This is an end-to-end test.
 *
 */
public class ProgramTest {

	@Before
	public void init() throws KRInitFailedException {
		SWIPrologInterface.getInstance();
	}

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
	public void testValidateBasicUpdate() throws IOException,
	KRInitFailedException, ParserException {
		try {
			validator("1").queryOrEmpty();
			throw new IllegalStateException("parse of wrong query succeeded");
		} catch (ParserException e) {
			assertEquals(
					ParserErrorMessages.NUMBER_NOT_AS_GOAL
							.toReadableString("1"),
					e.getMessage());
		}
	}

	@Test
	public void testVarAsGoal() throws IOException, KRInitFailedException,
			ParserException {
		try {
			validator("X").queryOrEmpty();
			throw new IllegalStateException("parse of wrong query succeeded");
		} catch (ParserException e) {
			assertEquals(
					ParserErrorMessages.VARIABLES_NOT_AS_GOAL
							.toReadableString("X"),
					e.getMessage());
		}
	}

}
