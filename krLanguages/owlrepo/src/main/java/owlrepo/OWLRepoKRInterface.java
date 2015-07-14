package owlrepo;


import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
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

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SWRLParser;

public class OWLRepoKRInterface implements KRInterface {
	OWLOntologyDatabase database = null;
	File owlfile = null;
	URL repoUrl = null;

	public void initialize(List<URI> uris) throws KRInitFailedException {
		for (URI uri : uris){
			if (uri.toString().startsWith("http")) {
				try {
					this.repoUrl =  uri.toURL();
				} catch (MalformedURLException e) {
					throw new KRInitFailedException(e.getMessage());
				}
			} else if (uri.isAbsolute()){
				//get the ontology file from the module
				this.owlfile = new File(uri);
			} 
		}

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
		//create parser for this database onto and reader source
		return new SWRLParser(database.getSWRLOntology(), source, info);
	}
	
	public File getOwlFile(){
		return this.owlfile;
	}
	
	public URL getRepoUrl(){
		return this.repoUrl;
	}

	public OWLOntologyDatabase getDatabase(){
		return this.database;
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
