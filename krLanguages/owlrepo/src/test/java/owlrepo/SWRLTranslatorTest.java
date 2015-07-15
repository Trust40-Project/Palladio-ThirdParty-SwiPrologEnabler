package owlrepo;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLTranslator;


public class SWRLTranslatorTest {
	SWRLTranslator trans;
	OWLOntologyDatabase  db = null;

	
	public SWRLTranslatorTest(){
		try {
		File file = new File("src/test/resources/tradrIndivrdf.owl");
		  db = new OWLOntologyDatabase("myonto", file);

		}catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() throws SWRLParseException {
	 SWRLRule rule = db.getSWRLOntology().createSWRLRule( "onto:Team_leader(?x)",
			 "onto:Human(?x) ^ onto:isHeadOf(?x, ?t)");

	 trans = new SWRLTranslator(db.getSWRLOntology(), rule);
	 System.out.println("SPARQL:::: \n"+trans.translateToSPARQL());
	}
}