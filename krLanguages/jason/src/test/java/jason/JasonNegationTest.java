package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;

import org.junit.Test;

/**
 * Test some basic functionality of JSON that we need.
 * 
 * @author W.Pasman 4jun15
 *
 */
public class JasonNegationTest {

	/**
	 * Tests basic query, with an Atom in the beliefbase.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void basicNotTest() throws ParseException {
		// set up database

		// add beer clause beer():-aap()... and aap() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(new LiteralImpl("beer"));

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = ASSyntax.parseStructure("not aap");
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");

		assertTrue(result.hasNext());
		assertEquals("{}", result.next().toString());
	}

}