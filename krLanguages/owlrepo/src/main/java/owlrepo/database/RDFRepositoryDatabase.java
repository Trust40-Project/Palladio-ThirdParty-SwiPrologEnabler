package owlrepo.database;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.rio.RioRenderer;

import com.complexible.common.protocols.server.Server;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;
import com.complexible.stardog.reasoning.api.ReasoningType;
import com.complexible.stardog.sesame.StardogRepository;


public class RDFRepositoryDatabase {

	private Server server;
	private Repository repo;
	private RepositoryConnection conn;
	private NotifyingRepositoryConnection nconn;
	private NotifyingRepositoryWrapper nrepo ;
	private RepositoryConnectionListener listener;
	private String baseURI;
	
//	private OWLOntologyDatabase ontology;
	private  String repo_url ;//= "http://localhost:8080/openrdf-sesame/tradr";
	private  String username = "admin";
	private  String password = "admin";
	
	public RDFRepositoryDatabase(String name, OWLOntology ontology, String baseURI, String url) {
		this.repo_url = url;

		try{
	/*		//AllegroGraph server and repo setup
			AGServer server = new AGServer(repo_url, username, password);
		    System.out.println("Available catalogs: " + server.listCatalogs());	
	        AGCatalog catalog = server.getRootCatalog();
		     repo = catalog.createRepository("tradr");
		    System.out.println("Got repository "+ "tradr");
	    //    AGValueFactory vf = conn.getRepository().getValueFactory();
	*/


			 // Create a Sesame Repository from a Stardog ConnectionConfiguration. 
			//The configuration will be used  when creating new RepositoryConnections
			//StarDog repo
		if (repo_url == null){
			//start local server
			server = Stardog
	                 .buildServer()
	                 .bind(SNARLProtocolConstants.EMBEDDED_ADDRESS)
	                 .start();
			
			 // first create a temporary database to use (if there is one already, drop it first)
			 AdminConnection aAdminConnection = AdminConnectionConfiguration.toEmbeddedServer().credentials(username, password).connect();
			 if (aAdminConnection.list().contains(name)) {
				 aAdminConnection.drop(name);
			 }
			 //create repo databse with given name in memory
			 aAdminConnection.createMemory(name);
			 aAdminConnection.close();
			 
			 //create Stardog Sesame Repository with SL reasoning
			 repo = new StardogRepository(ConnectionConfiguration
	                                         .to(name)
	                                         .reasoning(ReasoningType.SL)
	                                         .credentials(username, password));
			 
		} else{
			  //create Stardog Sesame Repository with SL reasoning
			repo = new StardogRepository(ConnectionConfiguration
					.to(name)
					.reasoning(ReasoningType.SL)
					.server(repo_url)
					);
		}
	        
		//start the repo up
		repo.initialize();
		conn = repo.getConnection();
		conn.begin();
		
		//wrap it in notifying connection
		 nrepo = new NotifyingRepositoryWrapper(repo);
		 listener = new RDFRepositoryConnectionListener();
		nrepo.addRepositoryConnectionListener(listener);
		 nconn = nrepo.getConnection();

		//add contents of file (ontology) to repo
		//repositoryConnection.add(File, String baseUri, RDFFormat, ResourcE)
		StatementCollector stc = new StatementCollector();
		RioRenderer render = new RioRenderer(ontology, stc, ontology.getOWLOntologyManager().getOntologyFormat(ontology), (Resource)null);
		render.render();
		nconn.add(stc.getStatements()); 
		nconn.commit();
		
		
		}catch(Exception e){
			e.printStackTrace();
			shutdown();
		}

	}
	
	
	public RepositoryResult<Statement> getTriples() throws RepositoryException{
		return nconn.getStatements(null, null, null, false, null);
	}
	
	public ValueFactory getValueFactory(){
		return nrepo.getValueFactory();
	}
	
	public TupleQueryResult query(String queryString){
		 TupleQueryResult result = null;
		 try { 
			 System.out.println("QUERYING...");
		  TupleQuery tupleQuery = nconn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
           result = tupleQuery.evaluate();
          System.out.println("RESULT: "+result.hasNext());
          
          }catch(Exception e){
        	  e.printStackTrace();
          }
		 return result;
	}
	
	public void insert(Collection<Statement> stms){
		try {
			nconn.add(stms, (Resource) null);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public void delete(Collection<Statement> stms){
		
		try {
			nconn.remove(stms, (Resource) null);
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public boolean ask(String queryString){
	     boolean truth= false;
	     try {
			 BooleanQuery booleanQuery = nconn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
			 truth = booleanQuery.evaluate();
			 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	     return truth;
	}
	
	public void construct(String queryString){
		 GraphQuery describeQuery;
		try {
			describeQuery = nconn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		    GraphQueryResult gresult = describeQuery.evaluate(); 
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void update(String updateString){
		Update update;
		try {
			update = nconn.prepareUpdate(QueryLanguage.SPARQL, updateString);
			update.execute();

		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
	
	public void shutdown(){
		try {
			conn.close();
			nconn.close();
			repo.shutDown();
			//nrepo.shutDown();
			if (server!=null)
				server.stop();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
}
