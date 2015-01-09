package owlrepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.language.SWRLTranslator;
import owlrepo.parser.SQWRLParser;


public class ParserTest {
	
	SQWRLParser parser = null;
	 BufferedReader reader;
	 SWRLRule r = null;
	 SWRLAPIOWLOntology swrlapiOnto;
	
	public ParserTest(){
		try{
		    File file = new File("src/test/resources/tradrIndivrdf.owl");
if (file.exists() && file.canRead()){
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		
	    OWLOntology  ontology = mng.loadOntologyFromOntologyDocument(file);
	    System.out.println("NR of owl axioms in onto: "+ontology.getAxiomCount());

//	    ontology.setOWLOntologyManager(mng);
//		mng.setOntologyFormat(ontology, mng.getOntologyFormat(ontology));
		
	     swrlapiOnto = SWRLAPIFactory.createOntology(file);
	    
	    System.out.println("NR of swrl rules in onto: "+swrlapiOnto.getNumberOfSWRLRules());
	    
	    PrefixManager pmg = swrlapiOnto.getPrefixManager();
	    pmg.setPrefix("tradr", "http://www.semanticweb.org/timi/ontologies/2014/3/untitled-ontology-10#");
	   
	    reader = new BufferedReader(new InputStreamReader(System.in));
		parser= new SQWRLParser(swrlapiOnto, reader);
}else
	System.out.println("File not found or not accessible.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		String text= "tradr:Human(?x) ^ tradr:isHeadOf(?x, ?t) -> tradr:Team_leader(?x)";
		//text = "tradr:Human(P1)";
		try {
			System.out.println("SWRL Rule:::: "+ text);
			 r = parser.parseRule(text, "rule1");
			 
		} catch (SWRLParseException e) {
			e.printStackTrace();
		}
		System.out.println("Read:::: "+r.toString());
		System.out.println("Axiom type:::: "+r.getAxiomType());
		//System.out.println("NNF: "+r.getNNF());
		System.out.println("Signature:::: "+r.getSignature());
		System.out.println("Variables:::: "+r.getVariables());

		SWRLTranslator transl = new SWRLTranslator(swrlapiOnto,r);
		System.out.println("SPARQL translation::::: \n"+transl.translateToSPARQL());

	
	}

}