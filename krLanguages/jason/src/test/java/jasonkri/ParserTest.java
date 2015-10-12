package jasonkri;

import static org.junit.Assert.assertEquals;
import jason.asSyntax.parser.ParseException;

import java.util.List;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.parser.Parser;

import org.junit.Test;

public class ParserTest {

	@Test
	public void constructorSmokeTest2WithParser() {
		Parser parser = new JasonParser("p:-q. q.", new mySourceInfo());
		List<DatabaseFormula> result = parser.parseDBFs();
		if (!parser.getErrors().isEmpty()) {
			System.out.println("parsing has problems: " + parser.getErrors());
		}

		assertEquals(2, result.size());
	}

	@Test
	public void ParseUpdate() throws ParseException, ParserException {
		JasonParser parser = new JasonParser("not (on(X,Z)) & on(X,Y)",
				new mySourceInfo());
		parser.parseUpdate();
	}
}