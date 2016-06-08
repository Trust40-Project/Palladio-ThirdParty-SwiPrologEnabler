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
import static org.junit.Assert.assertTrue;
import jpl.Float;

import org.junit.Test;

import swiprolog.SwiInstaller;
import swiprolog.database.PrologDatabase;

public class TestUtils {
	static {
		SwiInstaller.init();
	}

	@Test
	public void testJPLAtom() throws Exception {
		jpl.Atom constant = new jpl.Atom("Aap");
		jpl.Term term = JPLUtils.createCompound("var", constant);
		assertTrue(PrologDatabase.rawquery(term).isEmpty());
	}

	@Test
	public void testJPLFloat() throws Exception {
		Float constant = new jpl.Float(-1.2);
		// bit weird but let's check anyway...
		assertEquals("-1.2/0", JPLUtils.getSignature(constant));
	}

}
