package owlrepo;

import goalhub.krTools.KRFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.KRInterfaceNotSupportedException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLRule;

import owlrepo.OWLRepoKRInterface;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


public class OWLRepoKRInterfaceTest {

	private Database db;
	
	public OWLRepoKRInterfaceTest() {
		try {
			
			OWLRepoKRInterface interf = (OWLRepoKRInterface) KRFactory.getInterface("owl_repo");
			System.out.println("initializing "+interf.getName());
			interf.initialize();
			
			Set<DatabaseFormula> formulas = readFormulas(); 
			
			System.out.println("inserting formulas to db");
			db = interf.getDatabase(formulas);
			
			System.out.println("starting querying");
			db.query(getQuery());
			
			interf.release();
			
		} catch (KRInitFailedException e) {
			e.printStackTrace();
		} catch (KRDatabaseException e) {
			e.printStackTrace();
		} catch (KRInterfaceNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KRQueryFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Set<DatabaseFormula> readFormulas(){
		Set<DatabaseFormula> formulas = new HashSet<DatabaseFormula>();
		
		OWLDataFactory df = new OWLDataFactoryImpl();
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();
		body.add(df.getSWRLClassAtom(df.getOWLClass(IRI.create("http://example.org/Agent")), 
										df.getSWRLIndividualArgument(df.getOWLNamedIndividual(IRI.create("http://example.org/John_Smith")))));
		
		Set<SWRLAtom> head = new HashSet<SWRLAtom>();

		SWRLRule rule = df.getSWRLRule(body, head);
		
		DatabaseFormula formula = new SWRLDatabaseFormula(rule);
		formulas.add(formula);
		System.out.println(formula);
		return formulas;
	}
	
	private Query getQuery(){
		OWLDataFactory df = new OWLDataFactoryImpl();
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();
		body.add(df.getSWRLClassAtom(df.getOWLClass(IRI.create("http://example.org/Agent")), 
										df.getSWRLVariable(IRI.create("http://example.org/x"))));
		
		Set<SWRLAtom> head = new HashSet<SWRLAtom>();
		ArrayList<SWRLDArgument> arg = new ArrayList<SWRLDArgument>();
		arg.add(df.getSWRLVariable(IRI.create("http://example.org/x")));
		head.add(df.getSWRLBuiltInAtom(IRI.create("http://sqwrl.stanford.edu/ontologies/built-ins/3.4/sqwrl.owl#select"), 
											arg));
		
		SWRLRule query = df.getSWRLRule(body, head);
		System.out.println(query);
		return new SWRLQuery(query);
		
	}
	@Test
	public void test() {
	}

}
