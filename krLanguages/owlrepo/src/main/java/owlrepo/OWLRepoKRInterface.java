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

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SQWRLParser;

public class OWLRepoKRInterface implements KRInterface {

	private static OWLRepoKRInterface instance = null;
	OWLOntologyDatabase database = null;


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

	public Parser getParser(Reader source) throws ParserException {
		BufferedReader reader = new BufferedReader(source);
		File owlfile = null;
		try {
			String firstline = reader.readLine();
			if (firstline.endsWith(".owl")){
				owlfile = new File(firstline);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		if (database == null)
			try {
				
				if (owlfile==null){
					database = (OWLOntologyDatabase) getDatabase(new HashSet<DatabaseFormula>());
				}else {
					database = new OWLOntologyDatabase("kb", owlfile);
				}
			} catch (KRDatabaseException e) {
				e.printStackTrace();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		//create parser for this database onto and reader source
		return new SQWRLParser(database.getSWRLOntology(), reader);
	}

	public Substitution getSubstitution(Map<Var, Term> map) {
		return new SWRLSubstitution(map);
	}
	
	
	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		return queries;
	}
	
	
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		return dbfs;
	}
	

}
