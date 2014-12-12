package owlrepo.database;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLAPIRenderer;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.language.SWRLTranslator;

import com.google.common.base.Optional;

public class OWLOntologyDatabase implements Database {
	
	private boolean SHARED_MODE = false;
	private String name, baseURI;
	
	 private OWLOntologyManager manager;
//	private  OWLOntologyFactory ontofactory;
   private OWLOntology owlontology;
   private OWLDataFactory owlfactory ;

//   private OWLReasonerFactory reasonerFactory;
  private  OWLReasoner reasoner;
    
   private SWRLAPIOWLOntology swrlontology;
   private SWRLRuleEngine ruleEngine;
   private SQWRLQueryEngine queryEngine;
 // private SWRLAPIFactory swrlfactory;
//  private SWRLDataFactory swrldfactory;
	
    private RDFRepositoryDatabase localdb;
    private RDFRepositoryDatabase shareddb;

    private SWRLAPIRenderer renderer;
    
    public OWLOntologyDatabase(String name, File file, String repoUrl) throws OWLOntologyCreationException{
    	this.name = name;
		
		//create owl ontology and its manager
		  manager = OWLManager.createOWLOntologyManager();
		  
		  //insert ontology from file into in-memory owlontology object
	      if (file != null)
	    	  owlontology = manager.loadOntologyFromOntologyDocument(file);
	      else
		      owlontology = manager.createOntology(IRI.create(name));
	      
//	      reasoner = reasonerFactory.createReasoner(owlontology); 
	      owlfactory = manager.getOWLDataFactory(); 
	      
	      //create prefix manager 
	      OWLOntologyID ontoId = owlontology.getOntologyID();
	      Optional<IRI> ontoIri = ontoId.getOntologyIRI();
	      IRI ontologyIri = ontoIri.get();
	      DefaultPrefixManager prefixManager = new DefaultPrefixManager();
	      baseURI = ontologyIri.toString()+"#";
	      prefixManager.setPrefix("onto", baseURI);

	    
	      //create swrl ontology
	      swrlontology = SWRLAPIFactory.createOntology(owlontology, prefixManager);
	      SWRLAPIFactory.updatePrefixManager(owlontology, prefixManager);
	      //and our factory
	      renderer = SWRLAPIFactory.createSWRLAPIRenderer(swrlontology);  

	      //create owlapi reasoner pellet
	//      PelletOWLAPIReasoner pellet =  PelletOWLAPIReasonerFactory.getInstance().createReasoner(owlontology);
//	      SPARQLBuilder builder = SPARQLBuilder.createInstance();
//	      builder.allProperties(new com.complexible.jsonld.types.IRI(prefixManager.getPrefix("onto")));
//	      builder.limit(5);
//	      try {
//			System.out.println(builder.renderQuery());
//		} catch (CollectionsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	     // pellet.getKB().explain(axiom);
	      
	      
	      //create rule engine creator Drools
	//	    TargetSWRLRuleEngineCreator recreator =   DroolsFactory.getSWRLRuleEngineCreator();
//		//   create sqwrl query engine
	//	    queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(swrlontology, recreator);
//	    //  create swrl rule engine
		    
//			SWRLRuleEngineFactory swrlRuleEngineFactory = SWRLAPIFactory.createSWRLRuleEngineFactory();
//			 ruleEngine = swrlRuleEngineFactory.createSWRLRuleEngine(swrlontology);
//			 ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(swrlontology, recreator);
			
			
	      //   OWLRDFConsumer consumer = new OWLRDFConsumer( owlontology, new OWLOntologyLoaderConfiguration() );
//  		consumer.statementWithResourceValue(subject, predicate, object);
  		//OWLRDFConsumer to get contents of repo
  	
	     
	      //set up RDF repository = triple store local + shared
	      if (this.localdb == null){
	    		this.localdb = new RDFRepositoryDatabase(name, owlontology, baseURI, repoUrl);
	         
	      if (repoUrl!=null){
	    	  this.SHARED_MODE = true;
	    	  this.shareddb = new RDFRepositoryDatabase(name, owlontology, baseURI, repoUrl);
	      }}
		
    }
   
    public RDFRepositoryDatabase getRepo(){
    	return this.localdb;
    }
    
    public String getbaseURI(){
    	return this.baseURI;
    }
    
    
	public OWLOntologyDatabase(String name) throws OWLOntologyCreationException{
		this(name, (File)null, (String)null);
	}
	
	public OWLOntologyDatabase(String name, Collection<DatabaseFormula> content) throws OWLOntologyCreationException{
		this(name);
		Iterator<DatabaseFormula> iterator = content.iterator();
		while (iterator.hasNext()){
			SWRLDatabaseFormula formula = (SWRLDatabaseFormula)(iterator.next());
			manager.addAxiom(owlontology, formula.getRule());
		}
	}
	
	public OWLOntologyManager getOntologyManager(){
		return this.manager;
	}
	
	public OWLOntology geOWLOntology(){
		return this.owlontology;
	}
	public SWRLAPIOWLOntology getSWRLOntology(){
		return this.swrlontology;
	}
	
	public String getName() {
		return owlontology.getOntologyID().toString();
	}

	public Set<Substitution> query(Query query) throws KRQueryFailedException {
		Set<Substitution> qresult = new HashSet<Substitution>();
		SWRLQuery qr = (SWRLQuery)(query);

//		try {
//			System.out.println("Drools Query Engine results:");
//			SQWRLResult result = queryEngine.runSQWRLQuery(qr.getQueryName(), qr.toString());
//			for (String col: result.getColumnNames()){
//				System.out.println(col);
//				for(SQWRLResultValue val : result.getColumn(col)){
//					if (val.isIndividual())
//						System.out.println(val.asEntityResult().getIRI());
//					else if (val.isLiteral())
//						System.out.println(val.asLiteralResult().getValue());
//					else if (val.isEntity())
//						System.out.println(val.asEntityResult().getIRI());
//				}
//			}
//			
//		} catch (SQWRLException e1) {
//			e1.printStackTrace();
//			//throw new KRQueryFailedException(e1.getMessage());
//		}
		
		SWRLTranslator transl = new SWRLTranslator(this.swrlontology, qr.getRule());
		
		try {
			
		System.out.println("Local Stardog Repo results: ");
		TupleQueryResult rdfresult = this.localdb.query(transl.translateToSPARQL());
	    
		while (rdfresult.hasNext()) {
            BindingSet bindingSet = rdfresult.next();
            Set<String> bindings = bindingSet.getBindingNames();
            Iterator<String> it = bindings.iterator();
            while (it.hasNext()){
          	  String name = it.next();
          	  Value val = bindingSet.getValue(name);
          	  SWRLArgument valueArgument = null;
          	  
          	  if (val instanceof Literal){
          		valueArgument = //SWRL.constant(val.stringValue());
          				owlfactory.getSWRLLiteralArgument(owlfactory.getOWLLiteral(val.stringValue()));
          	  }else if (val instanceof Resource){
            	valueArgument = //SWRL.individual(val.stringValue());
            			owlfactory.getSWRLIndividualArgument(owlfactory.getOWLNamedIndividual(IRI.create(val.stringValue())));
          	  }
          	//  System.out.println(name + " : "+val);
          	  qresult.add(new SWRLSubstitution(//SWRL.variable(name)
          			 owlfactory.getSWRLVariable(IRI.create(name))
          			  , valueArgument));
            }
        }

	
//			System.out.println("Query: "+qr.getRule().toString());
//			//run query name, text
//			SQWRLResult result = queryEngine.runSQWRLQuery(qr.getQueryName());
//			if (!result.isEmpty()){
//				for (String columnName : result.getColumnNames()){
//					System.out.println(columnName+": "+result.getColumn(columnName).toString());
//					List<SQWRLResultValue> values =  result.getColumn(columnName);
//					for (int i=0; i<values.size();i++){
//						qresult.add(new SWRLSubstitution(owlfactory.getSWRLVariable(IRI.create(columnName)), 
//							values.get(i)));}
//				}
//				
//			}
		} catch (Exception e) {
			throw new KRQueryFailedException(e.getMessage());
		}
		
		return qresult;
	}
	
	public boolean isEntailed(Query query){
		OWLAxiom axiom = ((SWRLQuery)query).getAxiom();
		return reasoner.isEntailed(axiom);
	}

	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		SWRLDatabaseFormula form = (SWRLDatabaseFormula)(formula);
		SWRLRule rule = form.getRule();
		try {
			
			String ruletext = form.toString();
					//renderer.renderSWRLRule(rule);
			System.out.println("Inserting to db: "+ruletext);
			swrlontology.createSWRLRule("rulename", ruletext);
			
			manager.addAxiom(owlontology, rule);
			
			StatementCollector stc = new StatementCollector();
//			org.semanticweb.owlapi.rdf.model.RDFTranslator trans = new RDFTranslator(manager, owlontology, false);
//			trans.visit(form.getRule());
//			RDFGraph graph = trans.getGraph();
//			
			RioRenderer render = new RioRenderer(owlontology, stc, manager.getOntologyFormat(owlontology), (Resource)null);
			render.render();
//			Iterator<Statement> stit= stc.getStatements().iterator();
//			while(stit.hasNext())
//				System.out.println(stit.next().toString());
			localdb.insert(stc.getStatements());
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new KRDatabaseException(e.getMessage());
		}
	}

	
	public void insert(Update update) throws KRDatabaseException {
		OWLOntologyChange ch = null;
		manager.applyChange(ch);
	}
	
	public void updateDB(){
		if (SHARED_MODE){
			try{
			RepositoryResult<Statement> res = shareddb.getTriples();
			StatementCollector stc = new StatementCollector(res.asList());
			localdb.insert(stc.getStatements());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		SWRLDatabaseFormula form = (SWRLDatabaseFormula)(formula);
		OWLAxiom axiom = form.getAxiom();
		manager.removeAxiom(owlontology, axiom);
	}

	public void delete(Update update) throws KRDatabaseException {
		
		
	}

	public void destroy() throws KRDatabaseException {
		manager.removeOntology(owlontology);
		if (localdb != null)
			localdb.shutdown();
		//reasoner.dispose();
	}

}
