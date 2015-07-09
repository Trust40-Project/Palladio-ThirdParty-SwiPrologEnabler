package owlrepo;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.errors.exceptions.KRDatabaseException;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;
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

	private static OWLRepoKRInterface instance = null;
	OWLOntologyDatabase database = null;
	File owlfile = null;
	String repoUrl = null;


	private OWLRepoKRInterface() throws KRInitFailedException {
	try {
			
		} catch (Exception e) {
			throw new KRInitFailedException(e.getMessage(), e);
		}		
	}
	public static synchronized OWLRepoKRInterface getInstance()
			throws KRInitFailedException {
		if (OWLRepoKRInterface.instance == null) {
			OWLRepoKRInterface.instance = new OWLRepoKRInterface();
		}
		return OWLRepoKRInterface.instance;
	}
	
	public String getName() {
		return "owlrepo";
	}

	public void initialize() throws KRInitFailedException {

		
	}

	public void release() throws KRDatabaseException {
		database.destroy();		
	}

	public Database getDatabase(Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		try {
			database = new OWLOntologyDatabase("knowledge",content);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
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
		
		if (database == null){
			try {
				
				if (owlfile==null){
					database = (OWLOntologyDatabase) getDatabase(new HashSet<DatabaseFormula>());
				}else {
					database = new OWLOntologyDatabase(owlfile.getName(), owlfile);
				}
			} catch (KRDatabaseException e) {
				e.printStackTrace();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		
		} 
		
		//create parser for this database onto and reader source
		return new SWRLParser(database.getSWRLOntology(), source, info);
	}
	
	public File getOwlFile(){
		return this.owlfile;
	}
	
	public String getRepoUrl(){
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
	
	@Override
	public void setVocabularyFile(File file){
		//get the ontology file from the module
		//use it for the parser
		this.owlfile = file;
	}
	
	

}
