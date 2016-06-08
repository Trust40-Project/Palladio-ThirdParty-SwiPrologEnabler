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

package swiprolog.language;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import jpl.Term;
import jpl.Util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import swiprolog.SwiInstaller;

/**
 * Test the pretty printing of terms.
 */
@RunWith(Parameterized.class)
public class PrettyPrintTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "a, b" }, { "a, b , c" },
				{ "head :- body" }, { "head :- a , b , c" }, { "[p]" },
				{ "[a,b,c]" }, { "between(-1,1,X)" }, { "[[a,b,c],[d,e,f]]" },
				{ "[1,2|4]" }, { "[1,2|3]" }, { "'.'" }, { "3.1415" },
				{ "- p" }, { ":- p" }, { "'.'(1)" } });
	}

	static {
		SwiInstaller.init();
	}

	private String input;

	public PrettyPrintTest(String input) {
		this.input = input;
	}

	@Test
	public void test() {
		Term list = Util.textToTerm(input);
		/*
		 * With some terms like "between(-1,1,X), JPLUtils is playing some weird
		 * tricks: it inserts whitespaces where they are not in the original
		 * term, and removes them where they are in the original term.
		 */
		assertEquals(input.replaceAll(" ", ""), JPLUtils.toString(list)
				.replaceAll(" ", ""));
	}

}