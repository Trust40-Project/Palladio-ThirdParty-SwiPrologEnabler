package swiprolog.database;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jpl.Atom;
import jpl.Term;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologUpdate;
import krTools.KRInterface;
import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.*;

/**
 * Test the {@link UpdateEngine} BELIEFBASE part. Unfortunately we need also the
 * {@link InferenceEngine} to test if the update really worked so we're
 * partially testing {@link InferenceEngine} as well.
 * <p>
 * We should test ALL inference engine implementations here. The idea is that we
 * can feed all inference engines with generic tests and they should all behave
 * the same. Unfortunately this may not be possible since the databases all have
 * their own data types, eg SWI Prolog accepts only SWI Prolog terms, while PDDL
 * only accepts PDDL terms etc. So this approach may not be feasible in the end.
 * <p>
 * TODO we need to reach a good coverage in this test. Currently we do just a
 * bit of basic testing only. To create a good test, we should make a state
 * diagram of the UpdateEngine first.
 * 
 * @author W.Pasman 12mar2012
 * 
 */

public class TestUpdate {

	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;

	private Atom aap = new jpl.Atom("aap");
	private Atom beer = new jpl.Atom("beer");
	private Atom kat = new jpl.Atom("kat");

	@Before
	public void setUp() throws Exception {
		language = swiprolog.SWIPrologInterface.getInstance();

		knowledgebase = language.getDatabase(
				new LinkedHashSet<DatabaseFormula>());

		beliefbase = language.getDatabase(
				new LinkedHashSet<DatabaseFormula>());
	}

	@After
	public void tearDown() throws Exception {
		if (beliefbase != null) {
			beliefbase.destroy();
		}
		if (knowledgebase != null) {
			knowledgebase.destroy();
		}
	}

	@Test
	public void testInitialQuery1() throws KRQueryFailedException {
		PrologQuery query = new PrologQuery(new jpl.Atom("true"));
		Set<Substitution> sol = beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that inserting 'aap' results in 'aap' query to become true and that
	 * there is 1 sentence in beliefbase after the insert.
	 * 
	 * @throws KRQueryFailedException
	 * @throws KRInitFailedException 
	 */
	@Test
	public void testInsertFormula() throws KRQueryFailedException, KRDatabaseException {
		DatabaseFormula formula = new PrologDBFormula(aap);
		beliefbase.insert(formula);

		PrologQuery query = new PrologQuery(aap);
		Set<Substitution> sol = beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that after updating with (not(aap),beer) that there is 1 sentence
	 * in beliefbase and that 'beer' is true now and 'aap' false.
	 * 
	 * @throws KRQueryFailedException
	 * @throws KRInitFailedException 
	 */
	@Test
	public void testUpdate() throws KRQueryFailedException, KRDatabaseException {
		Update update = new PrologUpdate(new jpl.Compound(",",
						new Term[] {new jpl.Compound("not", new Term[] { aap }), beer }));
		beliefbase.insert(update);

//		assertEquals(1, beliefbase.getAllSentences().length);
//		assertEquals(0, knowledgebase.getAllSentences().length);

// TODO FIXME
//		PrologQuery query = new PrologQuery(aap);
//		Set<Substitution> sol = beliefbase.query(query);
//		assertEquals(sol.size(), 0);

		PrologQuery query2 = new PrologQuery(beer);
		Set<Substitution> sol2 = beliefbase.query(query2);
		assertEquals(1, sol2.size());
	}

	/**
	 * Delete belief base; create new belief base. Check that there are no
	 * predicates in new belief base.
	 * 
	 * @throws KRQueryFailedException
	 * @throws KRInitFailedException
	 * @throws KRDatabaseException 
	 */
	@Test
	public void testDeleteBeliefbase() throws KRQueryFailedException,
			KRDatabaseException {
		beliefbase.destroy();
		beliefbase = language.getDatabase(
				new LinkedHashSet<DatabaseFormula>());

//		assertEquals(0, beliefbase.getAllSentences().length);
	}

	/**
	 * After creation of new (empty) BB, check that the new BB also works by
	 * inserting something and checking that it gets there.
	 * 
	 * @throws KRQueryFailedException
	 * @throws KRInitFailedException
	 * @throws KRDatabaseException 
	 */
	@Test
	public void testUseNewBeliefbase() throws KRQueryFailedException, KRDatabaseException {
		DatabaseFormula formula = new PrologDBFormula(kat);
		beliefbase.insert(formula);

//		assertEquals(1, beliefbase.getAllSentences().length);
//		assertEquals(0, knowledgebase.getAllSentences().length);

		PrologQuery query = new PrologQuery(kat);
		Set<Substitution> sol = beliefbase.query(query);
		assertEquals(1, sol.size());

	}
}
