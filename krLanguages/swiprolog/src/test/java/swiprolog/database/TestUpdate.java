package swiprolog.database;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologAtomImpl;
import swiprolog.language.impl.PrologDBFormulaImpl;
import swiprolog.language.impl.PrologQueryImpl;

public class TestUpdate {
	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;
	// beliefs
	private PrologCompound aap;
	private PrologCompound kat;

	@Before
	public void setUp() throws Exception {
		this.language = new SwiPrologInterface();
		this.knowledgebase = this.language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>(0), true);
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(0), false);
		this.aap = new PrologAtomImpl("aap", null);
		this.kat = new PrologAtomImpl("kat", null);
	}

	@After
	public void tearDown() throws Exception {
		if (this.beliefbase != null) {
			this.beliefbase.destroy();
		}
		if (this.knowledgebase != null) {
			this.knowledgebase.destroy();
		}
	}

	@Test
	public void testInitialQuery1() throws Exception {
		PrologQuery query = new PrologQueryImpl(new PrologAtomImpl("true", null));
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that inserting 'aap' results in 'aap' query to become true and that
	 * there is 1 sentence in beliefbase after the insert.
	 */
	@Test
	public void testInsertFormula() throws Exception {
		DatabaseFormula formula = new PrologDBFormulaImpl(this.aap);
		this.beliefbase.insert(formula);

		PrologQuery query = new PrologQueryImpl(this.aap);
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Delete belief base; create new belief base. Check that there are no
	 * predicates in new belief base.
	 */
	@Test
	public void testDeleteBeliefbase() throws Exception {
		this.beliefbase.destroy();
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(), true);

		// assertEquals(0, beliefbase.getAllSentences().length);
	}

	/**
	 * After creation of new (empty) BB, check that the new BB also works by
	 * inserting something and checking that it gets there.
	 */
	@Test
	public void testUseNewBeliefbase() throws KRQueryFailedException, KRDatabaseException {
		DatabaseFormula formula = new PrologDBFormulaImpl(this.kat);
		this.beliefbase.insert(formula);

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		PrologQuery query = new PrologQueryImpl(this.kat);
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}
}
