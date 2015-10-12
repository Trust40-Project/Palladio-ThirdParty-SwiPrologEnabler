package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.RelExpr;
import jason.asSyntax.RelExpr.RelationalOp;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
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
public class BasicJasonTest {

	@Test
	public void testTermAap() {
		LiteralImpl f1 = aapZ();
		assertEquals("aap", f1.getFunctor());
		assertEquals(1, f1.getArity());

	}

	@Test
	public void testTermHead() {
		LiteralImpl head = makeBasicTermBeer();
		assertEquals("beer", head.getFunctor());
		assertEquals(1, head.getArity());

	}

	@Test
	public void testArithTerm() {
		RelExpr f2 = makeArithTerm();
		assertEquals(" = ", f2.getFunctor());
		assertEquals(2, f2.getArity());
	}

	@Test
	public void testRuleParser() throws ParseException {
		Rule clause = ASSyntax.parseRule("beer(Y) :- aap(Z) & Y = Z*3.");
		assertEquals("beer", clause.getFunctor());
		assertTrue(clause.isRule());
		assertTrue(clause.getBody().isStructure());
		assertEquals(" & ", ((Structure) clause.getBody()).getFunctor());
	}

	@Test
	public void testClause() {
		Rule clause = makeClause();
		assertTrue(clause.isRule());
		assertTrue(!clause.isGround());
	}

	@Test
	public void testClause2() {
		Rule clause = makeClause2();
		assertTrue(clause.isRule());
		// jason only checks heads of rules.
		assertEquals("beer", clause.getFunctor());
		assertTrue(clause.isGround());
	}

	public LiteralImpl aapZ() {
		// f1: aap(Z)
		LiteralImpl f1 = new LiteralImpl("aap");
		f1.addTerm(new VarTerm("Z"));

		return f1;
	}

	/**
	 * @return beer(Y)
	 */
	public LiteralImpl makeBasicTermBeer() {
		LiteralImpl head = new LiteralImpl("beer");
		head.addTerm(new VarTerm("Y"));

		return head;
	}

	/**
	 * @return beer(Y) :- aap(Z) & Y = Z * 2.
	 */
	public Rule makeClause() {
		LiteralImpl head = makeBasicTermBeer();
		LiteralImpl f1 = aapZ();
		RelExpr f2 = makeArithTerm();

		LogExpr body = new LogExpr(f1, LogicalOp.and, f2);
		Rule initbel = new Rule(head, body);
		return initbel;
	}

	/**
	 * @return beer :- aap(Z) & Y = Z * 2. Notice, no var in head.
	 */
	public Rule makeClause2() {
		LiteralImpl head = new LiteralImpl("beer");
		LiteralImpl f1 = aapZ();
		RelExpr f2 = makeArithTerm();

		LogExpr body = new LogExpr(f1, LogicalOp.and, f2);
		Rule initbel = new Rule(head, body);
		return initbel;
	}

	public RelExpr makeArithTerm() {
		// f2: Y=Z*2
		NumberTerm t1 = new VarTerm("Z");
		NumberTerm t2 = new NumberTermImpl(2);
		ArithExpr ztimes2 = new ArithExpr(t1, ArithmeticOp.times, t2);
		RelExpr f2 = new RelExpr(new VarTerm("Y"), RelationalOp.unify, ztimes2);
		return f2;
	}

	public LiteralImpl makeAtomAap() {
		LiteralImpl belief = new LiteralImpl("aap");
		belief.addTerm(new NumberTermImpl(3.2));
		return belief;
	}

	/**
	 * returns aap(aap(aap(.......()))..)
	 * 
	 * @return
	 */
	private Literal cyclicTermAap() {
		Literal query = new RelExpr(new VarTerm("Z"), RelationalOp.unify,
				aapZ());
		BeliefBase bb = new DefaultBeliefBase();
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		return (Literal) result.next().get("Z");
	}

	/**
	 * Tests basic query, with an Atom in the beliefbase.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void basicQueryTest() throws ParseException {
		// set up database

		// add beer clause beer():-aap()... and aap() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(new LiteralImpl("aap"));

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = new Structure("aap");
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");

		assertTrue(result.hasNext());
		assertEquals("{}", result.next().toString());
	}

	/**
	 * We can not insert {@link Atom}s into the BB.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void noAtomInBbTest() throws ParseException {
		// set up database

		// add beer clause beer():-aap()... and aap() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(new Atom("aap"));
		// this will only PRINT to stdout
		// SEVERE: Error: 'aap' can not be added in the belief base.

		assertEquals(0, bb.size());
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void complexQueryTest() throws ParseException {
		// set up database

		// add beer clause beer():-aap()... and aap() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(makeClause());
		bb.add(makeAtomAap());

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = new Structure("beer");
		query.addTerm(new VarTerm("X"));
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());

		assertTrue(result.hasNext());
		assertEquals("{X=6.4}", result.next().toString());
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testQueryOfRule() throws ParseException {
		// set up database
		// add beer clause beer():-aap()... and aap() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(makeClause());

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Rule query = makeClause();

		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");

		assertFalse(result.hasNext()); // apparently, clause query returns NO.
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testQueryOfCyclicTerm() throws ParseException {
		// set up database
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(cyclicTermAap());
		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = cyclicTermAap();
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");
		assertTrue(result.hasNext()); // the query with cyclic term succeeds
	}
}