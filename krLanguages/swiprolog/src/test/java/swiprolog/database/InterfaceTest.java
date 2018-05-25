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
import swiprolog.language.PrologCompound;
import swiprolog.language.impl.PrologImplFactory;

public class InterfaceTest {
	private SwiPrologInterface swi;
	// we're being lazy, maybe we should mock these?
	private final PrologCompound a = PrologImplFactory.getAtom("a", null);
	private DatabaseFormula aformula = PrologImplFactory.getDBFormula(this.a);

	@Before
	public void before() {
		this.swi = new SwiPrologInterface();
	}

	@After
	public void after() throws KRDatabaseException {
		this.swi.release();
	}

	@Test
	public void testCache() throws KRDatabaseException {
		assertEquals(this.aformula, this.aformula);

		List<DatabaseFormula> content = new ArrayList<>();
		content.add(this.aformula);
		Database db1 = this.swi.getDatabase("db1", content, true);
		Database db2 = this.swi.getDatabase("db2", content, true);

		assertTrue("databases should be identical objects, cache seems failing", db1 == db2);

	}
}
