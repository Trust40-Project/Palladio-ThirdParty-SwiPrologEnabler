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

package tuprolog.database;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tuprolog.TuPrologInterface;
import tuprolog.language.JPLUtils;

public class TestUtils {
	@Test
	public void testJPLAtom() throws Exception {
		alice.tuprolog.Struct constant = new alice.tuprolog.Struct("Aap");
		alice.tuprolog.Term term = JPLUtils.createCompound("var", constant);
		assertTrue(new PrologDatabase("", null, new TuPrologInterface()).rawquery(term).isEmpty());
	}
}
