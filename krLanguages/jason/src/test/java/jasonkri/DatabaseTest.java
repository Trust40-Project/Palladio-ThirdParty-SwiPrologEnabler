package jasonkri;

import static org.junit.Assert.assertEquals;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonUpdate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.parser.Parser;

import org.junit.Test;

/**
 * Test {@link JasonUpdate}
 * 
 * @author W.Pasman 10jun15
 *
 */
public class DatabaseTest {

	@Test
	public void constructorSmokeTest1() throws ParseException,
			KRDatabaseException {
		Collection<DatabaseFormula> formulas = new HashSet<DatabaseFormula>();
		formulas.add(new JasonDatabaseFormula(ASSyntax.parseLiteral("p"), null));
		new JasonDatabase(formulas);
	}

	@Test
	public void constructorSmokeTest2() throws KRDatabaseException,
			ParseException, ParserException {
		Parser parser = new JasonParser("p:-q. q.", new mySourceInfo());
		new JasonDatabase(parser.parseDBFs());
	}

	@Test
	public void queryTest1() throws KRDatabaseException, ParseException,
			ParserException, KRQueryFailedException {
		Parser parser = new JasonParser("p:-q. q.", new mySourceInfo());
		JasonDatabase db = new JasonDatabase(parser.parseDBFs());

		JasonQuery query = new JasonQuery(ASSyntax.parseFormula("q"),
				new JasonSourceInfo(new mySourceInfo()));
		Set<Substitution> results = db.query(query);
		System.out.println("result=" + results);
		assertEquals("[{}]", results.toString());// YES
	}

	@Test
	public void queryTest2() throws KRDatabaseException, ParseException,
			ParserException, KRQueryFailedException {
		Parser parser = new JasonParser("p :- q. q.", new mySourceInfo());
		JasonDatabase db = new JasonDatabase(parser.parseDBFs());

		JasonQuery query = new JasonQuery(ASSyntax.parseFormula("p"),
				new JasonSourceInfo(new mySourceInfo()));
		Set<Substitution> results = db.query(query);
		System.out.println("result=" + results);
		assertEquals("[{}]", results.toString());// YES
	}
}
