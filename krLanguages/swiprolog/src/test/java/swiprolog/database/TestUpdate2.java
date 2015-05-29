package swiprolog.database;

import java.util.LinkedHashSet;
import java.util.Set;

import jpl.Atom;
import krTools.KRInterface;
import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swiprolog.language.PrologDBFormula;

/**
 * Test the {@link UpdateEngine} KNOWLEDGEBASE part. Unfortunately we need also
 * the {@link InferenceEngine} to test if the update really worked so we're
 * partially testing {@link InferenceEngine} as well.
 * <p>
 * See also the comments in {@link TestUpdate}.
 *
 * @author W.Pasman 12mar2012
 *
 */

public class TestUpdate2 {

	// components enabling us to run the tests...
	private KRInterface language;

	// basic knowledge
	private final Atom k1 = new Atom("aap");
	private final Atom k2 = new Atom("beer");
	private final Atom k3 = new Atom("kat");

	private Database knowledgebase;
	private Database beliefbase;

	@Before
	public void setUp() throws Exception {
		this.language = swiprolog.SWIPrologInterface.getInstance();

		fillKB();
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

	/**
	 * Make standard kb and bb where KB holds two knowledge items
	 *
	 * @throws KRDatabaseException
	 */
	private void fillKB() throws KRDatabaseException {
		Set<DatabaseFormula> kbtheory = new LinkedHashSet<DatabaseFormula>();
		kbtheory.add(new PrologDBFormula(this.k1, null));
		kbtheory.add(new PrologDBFormula(this.k2, null));
		this.knowledgebase = this.language.getDatabase(kbtheory);

		this.beliefbase = this.language
				.getDatabase(new LinkedHashSet<DatabaseFormula>());

	}

	/**
	 * alternative fill for KB.
	 *
	 * @throws KRDatabaseException
	 */
	private void fillKB2() throws KRDatabaseException {

		Set<DatabaseFormula> kbtheory2 = new LinkedHashSet<DatabaseFormula>();
		kbtheory2.add(new PrologDBFormula(this.k3, null));
		this.knowledgebase = this.language.getDatabase(kbtheory2);

		this.beliefbase = this.language
				.getDatabase(new LinkedHashSet<DatabaseFormula>());
	}

	/**
	 * Test initial size of the new databases
	 *
	 * @throws KRInitFailedException
	 */
	// @Test
	// public void testInitialBeliefs() throws KRInitFailedException {
	// assertEquals(2, beliefbase.getAllSentences().length);
	// assertEquals(0, knowledgebase.getAllSentences().length);
	// }

	/**
	 * Test that the knowledge made it into the BB
	 *
	 * @throws KRQueryFailedException
	 * @throws KRInitFailedException
	 */
	// TODO FIXME
	// @Test
	// public void testInitialQuery1() throws KRQueryFailedException,
	// KRInitFailedException {
	// PrologQuery query = new PrologQuery(k1);
	// Set<Substitution> sol = beliefbase.query(query);
	// assertEquals(1, sol.size());
	// }

	/**
	 * Delete all databases. Just a smoke test, as we can't do anything with
	 * deleted databases.
	 *
	 * @throws KRQueryFailedException
	 * @throws KRDatabaseException
	 */
	@Test
	public void testDeleteAll() throws KRQueryFailedException,
	KRDatabaseException {
		this.beliefbase.destroy();
		this.beliefbase = null;
		this.knowledgebase.destroy();
		this.knowledgebase = null;
	}

	/**
	 * Create new Kb and Bb for SAME AGENT NAME, and check that the new BB has
	 * the new Kb content.
	 *
	 * @throws KRQueryFailedException
	 * @throws KRDatabaseException
	 */
	@Test
	public void testRecreateKbAndBb() throws KRQueryFailedException,
	KRDatabaseException {
		this.beliefbase.destroy();
		this.knowledgebase.destroy();

		// ok, now we can recreate the KB. But this time different.
		fillKB2();

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		// TODO FIXME
		// PrologQuery query = new PrologQuery(k3);
		// Set<Substitution> sol = beliefbase.query(query);
		// assertEquals(1, sol.size());

	}
}
