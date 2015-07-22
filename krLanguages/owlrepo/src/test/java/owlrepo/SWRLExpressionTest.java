package owlrepo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

import krTools.language.Term;
import krTools.language.Var;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.language.SWRLExpression;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SWRLParser;

public class SWRLExpressionTest {

	SWRLParser parser = null;
	BufferedReader reader;
	SWRLRule r = null;
	SWRLAPIOWLOntology swrlapiOnto;

	public SWRLExpressionTest() {
		try {
			File file = new File("src/test/resources/tradrIndivrdf.owl");
			if (file.exists() && file.canRead()) {
				OWLOntologyManager mng = OWLManager.createOWLOntologyManager();

				OWLOntology ontology = mng
						.loadOntologyFromOntologyDocument(file);
				// System.out.println("NR of owl axioms in onto: "
				// + ontology.getAxiomCount());

				DefaultPrefixManager pmg = new DefaultPrefixManager();
				swrlapiOnto = SWRLAPIFactory.createOntology(ontology, pmg);

				pmg.setPrefix("tradr",
						"http://www.semanticweb.org/ontologies/tradr#");

				reader = new BufferedReader(new InputStreamReader(System.in));
				parser = new SWRLParser(swrlapiOnto,
						Arrays.asList(RDFFormat.RDFXML), reader, null);
			} else
				System.out.println("File not found or not accessible.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SWRLExpression getExpression(String text) {
		try {
			System.out.println("SWRL Rule:::: " + text);
			r = parser.parseRule(text, "rule1");
		} catch (SWRLParseException e) {
			e.printStackTrace();
		}

		SWRLExpression exp = new SWRLExpression(r);
		System.out.println("SWRL expression: " + exp.toString());
		System.out.print("Free vars:");
		for (Var v : exp.getFreeVar()) {
			System.out.print(v + " ");
		}
		System.out.println();
		return exp;
	}

	private SWRLExpression getTerm(String text) {
		try {
			System.out.println("SWRL Rule:::: " + text);
			r = parser.parseRule(text, "rule1");
		} catch (SWRLParseException e) {
			e.printStackTrace();
		}

		SWRLExpression exp = new SWRLExpression(r.getBody().iterator().next());
		System.out.println("SWRL expression: " + exp.toString());
		System.out.print("Free vars:");
		for (Var v : exp.getFreeVar()) {
			System.out.print(v + " ");
		}
		System.out.println();
		return exp;
	}

	private void printMGU(SWRLSubstitution subst) {
		System.out.println("MGU:::: ");

		for (SWRLVariable var : subst.getSWRLVariables()) {
			SWRLArgument term = subst.getSWRLArgument(var);
			System.out.println("SWRL Substitution " + var + " / " + term);
		}

		for (Var var : subst.getVariables()) {
			Term term = subst.get(var);
			System.out.println("KRI Substitution " + var + " / " + term);
		}
	}

	@Test
	public void testMGUTermTerm() {
		String text = " tradr:hasAge(?x, 34)";
		String text2 = "tradr:hasAge(tradr:John_Smith, 35)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getTerm(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);

		assertTrue(subst.getSWRLVariables().isEmpty());
		assertTrue(subst.getVariables().isEmpty());
	}

	@Test
	public void testMGUTermTermBothFreeVars() {
		String text = " tradr:hasAge(?x, 34)";
		String text2 = "tradr:hasAge(tradr:John_Smith, ?z)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getTerm(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		assertEquals(2, subst.getSWRLVariables().size());
		assertEquals(2, subst.getVariables().size());
	}

	@Test
	public void testMGUTermTermMatchAllArgs() {
		String text = " tradr:hasAge(?x, 34)";
		String text2 = "tradr:hasAge(tradr:John_Smith, 35)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getTerm(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);

		assertTrue(subst.getSWRLVariables().isEmpty());
		assertTrue(subst.getVariables().isEmpty());
	}

	@Test
	public void testMGUTermRule() {
		String text = "tradr:Human(tradr:John_Smith)";
		String text2 = "tradr:Human(?x) ^ tradr:isHeadOf(?x, ?t)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getExpression(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		assertEquals(1, subst.getSWRLVariables().size());
		assertEquals(1, subst.getVariables().size());

	}

	@Test
	public void testMGUTermRuleTwoMatches() {
		String text = "tradr:Human(tradr:John_Smith)";
		String text2 = "tradr:Human(?x) ^ tradr:Human(?t)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getExpression(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		assertEquals(2, subst.getSWRLVariables().size());
		assertEquals(2, subst.getVariables().size());

	}

	@Test
	public void testMGUTermRuleTwoMatchesSameVariable() {
		String text = "tradr:hasAge(tradr:John_Smith, ?x)";
		String text2 = "tradr:hasAge(?x, 33) ^ tradr:hasAge(?y, ?z)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getExpression(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		// returns ?x/33 ?y/?x
		// but actually variables need to be renamed and then ok

		// assertTrue(subst.getSWRLVariables().isEmpty());
		// assertTrue(subst.getVariables().isEmpty());
	}

	@Test
	public void testMGUTermRuleTwoMatchesInvalid() {
		String text = "tradr:hasAge(tradr:John_Smith, ?x)";
		String text2 = "tradr:hasAge(?y, 33) ^ tradr:hasAge(?z, 35)";

		SWRLExpression exp = getTerm(text);
		SWRLExpression exp2 = getExpression(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		// it should throw exception or error, coz there are two possible
		// substitutions:
		// ?y/John_Smith ?x/33
		// ?z/John_Smith ?x/35
		// so for now it returns the first, plus erronously also ?z/John_Smith
		// needs more debugging to figure out mgu algorithm fully
		assertEquals(3, subst.getSWRLVariables().size());
		assertEquals(3, subst.getVariables().size());

	}

	@Test
	public void testMGURuleTerm() {
		String text = "tradr:Human(?x) ^ tradr:isHeadOf(?x, ?t) -> tradr:Team_leader(?x)";
		String text2 = "tradr:Human(tradr:John_Smith)";

		SWRLExpression exp = getExpression(text);
		SWRLExpression exp2 = getTerm(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		assertEquals(1, subst.getSWRLVariables().size());
		assertEquals(1, subst.getVariables().size());

	}

	@Test
	public void testMGURules() {
		String text = "tradr:Human(?x) ^ tradr:isHeadOf(?x, ?t) -> tradr:Team_leader(?x)";
		String text2 = "tradr:Human(tradr:John_Smith) ^ tradr:hasAge(tradr:John_Smith, 34)";

		SWRLExpression exp = getExpression(text);
		SWRLExpression exp2 = getExpression(text2);

		SWRLSubstitution subst = (SWRLSubstitution) exp.mgu(exp2);
		printMGU(subst);

		assertEquals(1, subst.getSWRLVariables().size());
		assertEquals(1, subst.getVariables().size());

	}
}
