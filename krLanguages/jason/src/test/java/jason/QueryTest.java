package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;

import org.junit.Test;

/**
 * Test jason inference system
 * 
 * @author W.Pasman 10jun15
 *
 */
public class QueryTest {

	@Test
	public void queryNotP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("~p");
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("not p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
		assertEquals("{}", res.next().toString());
	}

	@Test
	public void queryP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("~p");
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertFalse(res.hasNext());
	}

}