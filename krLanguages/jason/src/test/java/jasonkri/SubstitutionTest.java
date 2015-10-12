package jasonkri;

import static org.junit.Assert.assertEquals;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonExpression;
import jasonkri.language.JasonSubstitution;
import krTools.language.Expression;
import krTools.language.Substitution;

import org.junit.Test;

public class SubstitutionTest {
	@Test
	public void testEmptySubsti() throws ParseException {
		Unifier unifier = new Unifier();
		assertEquals(0, unifier.size());
	}

	@Test
	public void testSubsti() throws ParseException {
		Unifier unifier = bindX2p();
		assertEquals(1, unifier.size());
	}

	private Unifier bindX2pX() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(ASSyntax.parseVar("X"), ASSyntax.parseStructure("p(X)"));
		return unifier;
	}

	private Unifier bindXY() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(ASSyntax.parseVar("X"), ASSyntax.parseVar("Y"));
		return unifier;
	}

	private Unifier bindXYZ() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(ASSyntax.parseVar("X"), ASSyntax.parseVar("Y"));
		unifier.bind(ASSyntax.parseVar("Y"), ASSyntax.parseVar("Z"));
		return unifier;
	}

	private Unifier bindX2p() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(ASSyntax.parseVar("X"), ASSyntax.parseStructure("p(1)"));
		return unifier;
	}

	/**
	 * test substi with infinite recursion. Surprisingly works fine
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testSubstiInfinite() throws ParseException {
		Unifier unifier = bindX2pX();
		assertEquals(1, unifier.size());
	}

	@Test
	public void testApplySubst() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("p(X)"), null);
		Substitution subst = new JasonSubstitution(bindX2p());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
	}

	/**
	 * Smoke test. It does not even bulge.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testApplySubstInfinity() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("p(X)"), null);
		Substitution subst = new JasonSubstitution(bindX2pX());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
	}

	/**
	 * chain substitution renaming vars. This is NOT working as expected.
	 */
	@Test
	public void testApplySubstXY() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("p(X)"), null);
		Substitution subst = new JasonSubstitution(bindXY());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
		assertEquals("p(Y)", result.toString());
	}

	/**
	 * chain substitution renaming vars. This is NOT working as expected.
	 */
	@Test
	public void testApplySubstXYZ() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("p(X)"), null);
		Substitution subst = new JasonSubstitution(bindXYZ());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
		assertEquals("p(Z)", result.toString());
	}
}