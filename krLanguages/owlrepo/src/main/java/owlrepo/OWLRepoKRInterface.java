package owlrepo;


import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SWRLParser;

public class OWLRepoKRInterface implements KRInterface {
	OWLOntologyDatabase database = null;
	File owlfile = null;
	URL repoUrl = null;
	List<RDFFormat> formats = null;

	public void initialize(List<URI> uris) throws KRInitFailedException {
		this.formats = new LinkedList<RDFFormat>();
		for (URI uri : uris){
			RDFFormat format = RDFFormat.forFileName(uri.getPath());
			formats.add(format);
			if (uri.toString().startsWith("http")) {
				try {
					this.repoUrl =  uri.toURL();
				} catch (MalformedURLException e) {
					throw new KRInitFailedException(e.getMessage());
				}
			} else if (uri.isAbsolute() && format.hasFileExtension("owl")){
				//get the ontology file from the module
				this.owlfile = new File(uri);
			} 
		}

		getDatabase();
		
	}

	private OWLOntologyDatabase getDatabase() throws KRInitFailedException{
		//create a database to create a parser
		if (database == null){
			try {
				if (owlfile==null){
					//database = (OWLOntologyDatabase) getDatabase(new HashSet<DatabaseFormula>());
					throw new KRDatabaseException("Creation of OWL parser needs an OWL file set to the Interface");
				} else {
					database = new OWLOntologyDatabase(owlfile.getName(), owlfile);
				}
			} catch (KRDatabaseException e) {
				throw new KRInitFailedException(e.getMessage());
			} catch (OWLOntologyCreationException e) {
				throw new KRInitFailedException(e.getMessage());
			}
		}
		return database;
	}

	public void release() throws KRDatabaseException {
		database.destroy();		
	}

	public Database getDatabase(Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		try {
			database = new OWLOntologyDatabase("knowledge",content);
		} catch (OWLOntologyCreationException e) {
			throw new KRDatabaseException("Failed to create OWL Ontology Database",e);
		}
		// Add database to list of databases maintained by OWL Repo and
		// associated with name.
		//databases.put(database.getName(), database);

		// Return new database.
		return database;
	}

	@Override
	public Parser getParser(Reader source, SourceInfo info)
			throws ParserException {
		//BufferedReader reader = new BufferedReader(source);
		if (database == null)
			throw new ParserException("OWLREPO KR Interface was not correctly initialized with an owl file!");

		try {
			//create parser for this database onto and reader source
			return new SWRLParser(getDatabase().getSWRLOntology(), formats, source, info);
		
		} catch (KRInitFailedException e){
			e.printStackTrace();
			throw new ParserException(e.getMessage());
		}
	}
	
	public List<RDFFormat> getAllFormats(){
		return this.formats;
	}

	public File getOwlFile(){
		return this.owlfile;
	}

	public URL getRepoUrl(){
		return this.repoUrl;
	}

	public Substitution getSubstitution(Map<Var, Term> map) {
		return new SWRLSubstitution(map);
	}

	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		//TODO:check terms in db
		return new HashSet<Query>();
	}

	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		//TODO:check terms in db
		return new HashSet<DatabaseFormula>();
	}

	@Override
	public boolean supportsSerialization() {
		// TODO Auto-generated method stub
		return false;
	}
}