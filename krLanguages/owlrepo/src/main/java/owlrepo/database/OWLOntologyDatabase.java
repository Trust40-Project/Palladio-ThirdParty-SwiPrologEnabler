package owlrepo.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLTranslator;

import com.complexible.common.openrdf.query.BooleanQueryResult;
import com.google.common.base.Optional;

public class OWLOntologyDatabase implements Database {

	private boolean SHARED_MODE = false;
	private String name, baseURI;

	private OWLOntologyManager manager;
	// private OWLOntologyFactory ontofactory;
	private OWLOntology owlontology;
	private OWLDataFactory owlfactory;

	// private OWLReasonerFactory reasonerFactory;
	private OWLReasoner reasoner;

	private SWRLAPIOWLOntology swrlontology;
	// private SWRLRuleEngine ruleEngine;
	// private SQWRLQueryEngine queryEngine;
	// private SWRLAPIFactory swrlfactory;
	// private SWRLDataFactory swrldfactory;

	private RDFRepositoryDatabase localdb = null;
	private RDFRepositoryDatabase shareddb = null;
	private RepositoryConnectionListener local_listener;
	private RepositoryConnectionListener shared_listener;

	// private boolean firstInsert = true;
	private Collection<Statement> baseStm;
	// private SWRLAPIRenderer renderer;
	private DefaultPrefixManager prefixManager;

	private Set<DatabaseFormula> allFormulas = new HashSet<DatabaseFormula>();

	public OWLOntologyDatabase(String name, File file)
			throws KRDatabaseException {
		this.name = name;

		// KB is shared
		if (this.name.equals("KNOWLEDGEBASE"))
			this.SHARED_MODE = true;

		// create owl ontology and its manager
		manager = OWLManager.createOWLOntologyManager();

		try{
			// insert ontology from file into in-memory owlontology object
			if (file != null)
				owlontology = manager.loadOntologyFromOntologyDocument(file);
			else
				owlontology = manager.createOntology(IRI.create(name));

		}catch (OWLOntologyCreationException ex) {
			throw new KRDatabaseException("Could not create OWL DB: "+ex.getMessage(), ex.getCause());
		}
		// reasoner = reasonerFactory.createReasoner(owlontology);
		owlfactory = manager.getOWLDataFactory();

		// create prefix manager
		OWLOntologyID ontoId = owlontology.getOntologyID();
		Optional<IRI> ontoIri = ontoId.getOntologyIRI();
		IRI ontologyIri = ontoIri.get();
		this.prefixManager = new DefaultPrefixManager();
		baseURI = ontologyIri.toString() + "#";
		prefixManager.setDefaultPrefix(baseURI);

		// create swrl ontology
		swrlontology = SWRLAPIFactory
				.createOntology(owlontology, prefixManager);
		try {
			swrlontology.processOntology();
		} catch (SQWRLException e1) {
			throw new KRDatabaseException("Could not create OWL DB: "+e1.getMessage(), e1.getCause());
		}
		prefixManager.setPrefix("", baseURI);
		SWRLAPIFactory.updatePrefixManager(owlontology, prefixManager);
		swrlontology.getPrefixManager().setDefaultPrefix(baseURI);
		swrlontology.getPrefixManager().setPrefix("", baseURI);
		// renderer = SWRLAPIFactory.createSWRLAPIRenderer(swrlontology);

		StatementCollector stc = new StatementCollector();
		RioRenderer render = new RioRenderer(owlontology, stc,
				manager.getOntologyFormat(owlontology), (Resource) null);
		try {
			render.render();
			baseStm = stc.getStatements();
		} catch (IOException e) {
			e.printStackTrace();
			throw new KRDatabaseException(e.getMessage(), e.getCause());
		}

		// OWLRDFConsumer consumer = new OWLRDFConsumer( owlontology, new
		// OWLOntologyLoaderConfiguration() );
		// consumer.statementWithResourceValue(subject, predicate, object);
		// OWLRDFConsumer to get contents of repo

	}

	public Set<DatabaseFormula> getAllDBFormulas() {
		return this.allFormulas;
	}

	public Set<OWLAxiom> getAllOWLAxioms() {
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom axiom : owlontology.getABoxAxioms(null)) {
			allAxioms.add(axiom);
			// System.out.println(axiom);
		}
		// for (OWLAxiom axiom : owlontology.getTBoxAxioms(null)) {
		// allAxioms.add(axiom);
		// // System.out.println(axiom);
		// }
		// for (OWLAxiom axiom : owlontology.getRBoxAxioms(null)) {
		// allAxioms.add(axiom);
		// // System.out.println(axiom);
		// }
		for (SWRLRule rule : swrlontology.getSWRLAPIRules()) {
			allAxioms.add(rule);
			// System.out.println(rule);
		}
		return allAxioms;
	}

	public void setupRepo(URL repoUrl) {
		// set up RDF repository = triple store local + shared

		if (repoUrl != null) {
			System.out.println("Setting up remote repo");

			shared_listener = new RDFRepositoryConnectionListener(this,
					"shared");

			this.shareddb = new RDFRepositoryDatabase(name, owlontology,
					baseURI, repoUrl, shared_listener);
		} else {
			if (this.localdb == null) {
				System.out.println("Setting up local repo");
				local_listener = new RDFRepositoryConnectionListener(this,
						"local");
				this.localdb = new RDFRepositoryDatabase(name, owlontology,
						baseURI, null, local_listener);
			}
		}
	}

	public RDFRepositoryDatabase getRepo() {
		return this.localdb;
	}

	public String getbaseURI() {
		return this.baseURI;
	}

	public OWLOntologyManager getOntologyManager() {
		return this.manager;
	}

	public OWLOntology geOWLOntology() {
		return this.owlontology;
	}

	public SWRLAPIOWLOntology getSWRLOntology() {
		return this.swrlontology;
	}

	@Override
	public String getName() {
		return owlontology.getOntologyID().toString();
	}

	@Override
	public Set<Substitution> query(Query query) throws KRQueryFailedException {
		Set<Substitution> qresult = new HashSet<Substitution>();
		SWRLQuery qr = (SWRLQuery) (query);

		System.out.println("QUERYING:::: "+query.toString());

		SWRLTranslator transl = new SWRLTranslator(this.swrlontology,
				qr.getRule());

		try {
			QueryResult rdfresult = null;
			String querySPARQL = transl.translateToSPARQL();
			if (this.shareddb != null && this.shareddb.isOpen()) {
				System.out.println("Querying shared db:");
				rdfresult = this.shareddb.query(querySPARQL);
			} else if (this.localdb != null && this.localdb.isOpen()) {
				System.out.println("Querying local db: ");
				rdfresult = this.localdb.query(querySPARQL);
			}

			if (rdfresult instanceof TupleQueryResult) {
				TupleQueryResult tupleResult = (TupleQueryResult) rdfresult;

				while (tupleResult.hasNext()) {
					BindingSet bindingSet = tupleResult.next();
					Set<String> bindings = bindingSet.getBindingNames();
					Iterator<String> it = bindings.iterator();
					while (it.hasNext()) {
						String name = it.next();
						Value val = bindingSet.getValue(name);
						SWRLArgument valueArgument = null;

						if (val instanceof Literal) {
							valueArgument = // SWRL.constant(val.stringValue());
									owlfactory.getSWRLLiteralArgument(owlfactory
											.getOWLLiteral(val.stringValue()));
						} else if (val instanceof Resource) {
							valueArgument = // SWRL.individual(val.stringValue());
									owlfactory.getSWRLIndividualArgument(owlfactory
											.getOWLNamedIndividual(IRI.create(val
													.stringValue())));
						}
						System.out.println(name + " : " + val);
						qresult.add(new SWRLSubstitution(
								// SWRL.variable(name)
								owlfactory.getSWRLVariable(IRI.create(name)),
								valueArgument));
					}
				}
			} else if (rdfresult instanceof BooleanQueryResult) {
				BooleanQueryResult booleanResult = (BooleanQueryResult) rdfresult;
				boolean res;
				if (booleanResult.hasNext()) {
					res = booleanResult.next();
					System.out.println("RESULT::: " + res);
					if (res) //only add empty substitution if result is true
						qresult.add(new SWRLSubstitution());
				}
			}
			rdfresult.close();

		} catch (Exception e) {
			throw new KRQueryFailedException(e.getMessage());
		}

		return qresult;
	}

	public boolean isEntailed(Query query) {
		OWLAxiom axiom = ((SWRLQuery) query).getAxiom();
		return reasoner.isEntailed(axiom);
	}

	public void insertAll(Collection<DatabaseFormula> formulas)
			throws KRDatabaseException {
		for (DatabaseFormula formula : formulas)
			insert(formula);
	}

	public void insertAll(Set<OWLAxiom> axioms) throws KRDatabaseException {
		manager.addAxioms(owlontology, axioms);
		for (OWLAxiom axiom : getAllOWLAxioms())
			allFormulas.add(new SWRLDatabaseFormula(axiom));
	}

	@Override
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		this.allFormulas.add(formula);
		Collection<Statement> statements = new HashSet<Statement>();

		SWRLDatabaseFormula form = (SWRLDatabaseFormula) (formula);
		if( form.isArgument()){
			return; //cannot insert argument without predicate
		}else if (form.isTerm()){
			SWRLAtom atom = form.getAtom();
			//if it has no variables // cannot insert smth with variables
			if (form.getFreeVar().isEmpty()){
				ValueFactory vf = getRepo().getValueFactory();
				Collection<SWRLArgument> args = atom.getAllArguments();
				SWRLPredicate predt = atom.getPredicate();

				Resource subj = null;
				URI pred = null;
				Value obj = null;
				//unary
				if (args.size() == 1){
					String subjstring = args.iterator().next().toString();
					subj = vf.createURI(subjstring.substring(1, subjstring.length()-1));
					pred = vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
					obj = vf.createURI(predt.toString());	 
				} 
				//binary
				else if (args.size() == 2) {
					String subjstring = args.iterator().next().toString();
					subj = vf.createURI(subjstring.substring(1, subjstring.length()-1));
					pred = vf.createURI(predt.toString());
					String objstring = args.iterator().next().toString();
					if (objstring.contains("#"))
						obj = vf.createURI(objstring);
					else {
						if (objstring.contains("xsd")){
							//we need correct type creation
				
						}
						obj = vf.createLiteral(objstring);
					}
				}
				Statement st = vf.createStatement(subj, pred, obj);
				statements.add(st);

			} else
				throw new KRDatabaseException("Cannot insert term with variable: "+formula);
		}else if (form.isRule()){

			SWRLRule rule = form.getRule();
			try {

				String ruletext = form.toString();
				// renderer.renderSWRLRule(rule);
				System.out.println("Inserting to db: " + ruletext);

				manager.addAxiom(owlontology, rule);
				swrlontology.processOntology();
				// unfortunately the only way to insert a rule is by creating it
				// again
				// letting swrl parse it from text representation
				rule = swrlontology.createSWRLRule("rulename", ruletext);


				StatementCollector stc = new StatementCollector();
				// org.semanticweb.owlapi.rdf.model.RDFTranslator trans = new
				// RDFTranslator(manager, owlontology, false);
				// trans.visit(form.getRule());
				// RDFGraph graph = trans.getGraph();
				//
				RioRenderer render = new RioRenderer(owlontology, stc,
						manager.getOntologyFormat(owlontology), (Resource) null);
				render.render();
				// Iterator<Statement> stit= stc.getStatements().iterator();
				// while(stit.hasNext())
				// System.out.println(stit.next().toString());
				statements.addAll(stc.getStatements());

				statements.removeAll(baseStm);
			} catch (Exception e) {
				e.printStackTrace();
				throw new KRDatabaseException(e.getMessage());
			}
		}


		if (this.SHARED_MODE) {
			System.out.println("Inserting into shared db: " + formula);
			insertShared(statements);
		} else {
			System.out.println("Inserting into local db: " + formula);
			insertLocal(statements);
		}



	}

	public DatabaseFormula getDBFormula(Term term) throws KRDatabaseException {
		SWRLTerm swt = (SWRLTerm) term;
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();
		Set<SWRLAtom> head = new HashSet<SWRLAtom>();
		body.add(swt.getAtom());
		SWRLRule rule = owlfactory.getSWRLRule(body, head);
		return new SWRLDatabaseFormula(rule);
	}

	public void insert(Collection<Statement> statements) {
		if (SHARED_MODE)
			insertShared(statements);
		else
			insertLocal(statements);
	}

	public void insertLocal(Collection<Statement> statements) {
		// System.out.println("Inserting into local");
		if (localdb != null && localdb.isOpen())
			localdb.insert(statements);
	}

	public void insertShared(Collection<Statement> statements) {
		// System.out.println("Inserting into shared");
		if (shareddb != null && shareddb.isOpen())
			shareddb.insert(statements);
	}

	@Override
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert(formula);
		}
	}

	public void updateDB() {
		if (SHARED_MODE) {
			try {
				RepositoryResult<Statement> res = shareddb.getTriples();
				StatementCollector stc = new StatementCollector(res.asList());
				localdb.insert(stc.getStatements());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		allFormulas.remove(formula);
		System.out.println("DELETING::: "+formula);
		SWRLDatabaseFormula form = (SWRLDatabaseFormula) (formula);
		OWLAxiom axiom = form.getAxiom();
		manager.removeAxiom(owlontology, axiom);
	}

	@Override
	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert(formula);
		}
	}

	public void deleteAll(Set<OWLAxiom> axioms) {
		manager.removeAxioms(owlontology, axioms);
	}

	@Override
	public void destroy() throws KRDatabaseException {
		manager.removeOntology(owlontology);
		if (localdb != null)
			localdb.shutdown();
		//		if (server!=null)
		//			server.stop();
	}

}
