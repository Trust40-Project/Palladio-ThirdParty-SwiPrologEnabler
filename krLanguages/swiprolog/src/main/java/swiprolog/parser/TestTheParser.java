/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
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

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;

import swiprolog.language.PrologTerm;

/**
 * run this using 1 argument: the file to be parsed
 *
 * @author W.Pasman
 * */

public class TestTheParser {
	public static void main(String args[]) throws Exception {
		java.net.URL u = TestTheParser.class.getClassLoader().getResource(
				"goal/kr/language/prolog/" + args[0]);

		PrologLexer lex = new PrologLexer(new ANTLRFileStream(u.getFile()));
		CommonTokenStream tokens = new CommonTokenStream(lex);

		PrologParser parser = new PrologParser(tokens);
		ArrayList<PrologTerm> results = parser.prologtext(); // launch parsing
		// ArrayList<PrologTerm> results = new ArrayList<PrologTerm>();
		// results.add(parser.term1200()); // parsing
		// print tree if building trees
		if (results == null) {
			System.out.println("parse failed");
		} else {
			for (PrologTerm t : results) {
				try {
					System.out.println(">" + t + " with main operator "
							+ t.getSignature());
				} catch (Exception e) {
					System.out.println("print failed:" + e);
				}
			}
		}
	}
}
