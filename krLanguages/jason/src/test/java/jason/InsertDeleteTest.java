package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
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
public class InsertDeleteTest {

	@Test
	public void testTermAap() {
		LiteralImpl f1 = aap(3);
		assertEquals("aap", f1.getFunctor());
		assertEquals(1, f1.getArity());
		Term term = f1.getTerm(0);

		assertTrue(term.isNumeric());
		assertEquals(3, ((NumberTermImpl) term).solve(), 10 ^ -12);
	}

	/**
	 * @param value
	 * @return aap(value)
	 */
	public LiteralImpl aap(double value) {
		LiteralImpl f1 = new LiteralImpl("aap");
		f1.addTerm(new NumberTermImpl(value));
		return f1;
	}

	/**
	 * 
	 * @return aap(X)
	 */
	public LiteralImpl makeBasicTermAapX() {
		LiteralImpl f1 = new LiteralImpl("aap");
		f1.addTerm(new VarTerm("X"));
		return f1;
	}

	/**
	 * Test insert sequence.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void InsertTest1() throws ParseException {
		// set up database
		
		BeliefBase bb = new DefaultBeliefBase();
		

		checkQueryAap(bb);

		bb.add(aap(4.5));
		checkQueryAap(bb, "{X=4.5}");

	}

	/**
	 * Test insert sequence of length 3.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void InsertTest2() throws ParseException {
		// set up database
		
		BeliefBase bb = new DefaultBeliefBase();

		checkQueryAap(bb);

		bb.add(aap(4.5));
		checkQueryAap(bb, "{X=4.5}");

		// note, addBel is asserta()
		bb.add(aap(6.3));
		checkQueryAap(bb, "{X=6.3}", "{X=4.5}");

		// note, addBel is asserta()
		bb.add(aap(1.6));
		checkQueryAap(bb, "{X=1.6}", "{X=6.3}", "{X=4.5}");
	}

	/**
	 * Delete test.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void DeleteTest() throws ParseException {
		// set up database
		BeliefBase bb = new DefaultBeliefBase();

		checkQueryAap(bb);

		bb.add(aap(4.5));
		bb.add(aap(6.3));
		bb.add(aap(1.6));
		checkQueryAap(bb, "{X=1.6}", "{X=6.3}", "{X=4.5}");

		
		((DefaultBeliefBase)bb).abolish(aap(6.3), new Unifier());
		checkQueryAap(bb, "{X=1.6}", "{X=4.5}");

	}

	/**
	 * Check if query aap(X) returns given values in given order
	 */
	private void checkQueryAap(BeliefBase bb, String... values) {
		LiteralImpl query = makeBasicTermAapX();
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		for (String value : values) {
			if (!result.hasNext()) {
				throw new IndexOutOfBoundsException(
						"less results than expected");
			}
			assertEquals(value, result.next().toString());
		}
		if (result.hasNext()) {
			throw new IndexOutOfBoundsException("more results than expected");
		}
	}
}