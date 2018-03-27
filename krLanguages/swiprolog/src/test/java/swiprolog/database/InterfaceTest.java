package swiprolog.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import swiprolog.SwiPrologInterface;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologDBFormulaImpl;

public class InterfaceTest {
	private SwiPrologInterface swi;
	// we're being lazy, maybe we should mock these?
	private final PrologAtomImpl a = new PrologAtomImpl("a", null);
	private DatabaseFormula aformula = new PrologDBFormulaImpl(a);

	@Before
	public void before() {
		swi = new SwiPrologInterface();
	}

	@After
	public void after() throws KRDatabaseException {
		swi.release();
	}

	@Test
	public void testCache() throws KRDatabaseException {
		assertEquals(aformula, aformula);

		List<DatabaseFormula> content = new ArrayList<>();
		content.add(aformula);
		Database db1 = swi.getDatabase("db1", content, true);
		Database db2 = swi.getDatabase("db2", content, true);

		assertTrue("databases should be identical objects, cache seems failing", db1 == db2);

	}
}
