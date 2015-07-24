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

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SWRLParser;

public class OWLRepoKRInterface implements KRInterface {
	OWLOntologyDatabase database = null;
	File owlfile = null;
	URL repoUrl = null;
	List<RDFFormat> formats = null;
	SWRLParser parser = null;
	List<String> undefined = new LinkedList<String>();

	public void initialize(List<URI> uris) throws KRInitFailedException {
		this.formats = new LinkedList<RDFFormat>();
		for (URI uri : uris) {
			RDFFormat format = RDFFormat.forFileName(uri.getPath());
			formats.add(format);
			if (uri.toString().startsWith("http")) {
				try {
					this.repoUrl = uri.toURL();
				} catch (MalformedURLException e) {
					throw new KRInitFailedException(e.getMessage());
				}
			} else if (uri.isAbsolute() && format.hasFileExtension("owl")) {
				// get the ontology file from the module
				this.owlfile = new File(uri);
			}
		}

		getDatabase("parser");

	}

	private OWLOntologyDatabase getDatabase(String name)
			throws KRInitFailedException {
		// create a database with the initializing ontology
		// if (database == null) {
			try {
				if (owlfile == null) {
					// database = (OWLOntologyDatabase) getDatabase(new
					// HashSet<DatabaseFormula>());
					throw new KRDatabaseException(
							"Creation of OWL parser needs an OWL file set to the Interface");
				} else {
					database = new OWLOntologyDatabase(name,
							owlfile);
					if (!name.equals("parser")) //in case it is needed for parser, do not set up real triplestores
						database.setupRepo(repoUrl);
				}
			} catch (KRDatabaseException e) {
				throw new KRInitFailedException(e.getMessage(), e.getCause());
			}
		// }
		return database;
	}

	public void release() throws KRDatabaseException {
		database.destroy();
	}

	public Database getDatabase(String name, Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		try {
			database = getDatabase(name);
			database.insertAll(content);
		} catch (KRInitFailedException e) {
			throw new KRDatabaseException(
					"Failed to create OWL Ontology Database", e);
		}
		// Add database to list of databases maintained by OWL Repo and
		// associated with name.
		// databases.put(database.getName(), database);

		// Return new database.
		return database;
	}

	public Database getDatabase(String name, File onto) throws KRDatabaseException {
		return new OWLOntologyDatabase(name, onto);
		// Return new database.
		// used for message base, that does NOT need to know the KR terms.
	}

	@Override
	public Parser getParser(Reader source, SourceInfo info)
			throws ParserException {
		// BufferedReader reader = new BufferedReader(source);
		// System.out.println("OWLRepo parser created");
		if (database == null)
			throw new ParserException(
					"OWLREPO KR Interface was not correctly initialized with an owl file!");

		// create parser for this database onto and reader source
		if (this.parser == null) {
			this.parser = new SWRLParser(database.getSWRLOntology(), formats,
					source, info);
		} else
			this.parser.parse(formats, source, info);
		return parser;
	}

	public List<RDFFormat> getAllFormats() {
		return this.formats;
	}

	public File getOwlFile() {
		return this.owlfile;
	}

	public URL getRepoUrl() {
		return this.repoUrl;
	}

	public Substitution getSubstitution(Map<Var, Term> map) {
		return new SWRLSubstitution(map);
	}

	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		Set<Query> undef = new HashSet<Query>();
		if (this.parser != null)
			for (String s : parser.getUndefined()) {
				// System.out.println("undefined " + s);
				undef.add(new SWRLQuery(s));
				// parser.getErrors().add(
				// new SWRLParserSourceInfo(parser.getInfo().getSource(),
				// 1, -1, "Invalid SWRL atom predicate in" + s));
			}
		// System.out.println(parser.getErrors().size());
		// keep parser errors only until getundefined is called
		// parser.getErrors().clear();
		parser.getUndefined().clear();
		for (Query query : queries) {
			// System.out.println("query " + query.toString());
			if (((SWRLQuery) query).isUndefined()) {
				// System.out.println("undefined " + query.toString());
				undef.add(query);
			}
		}
		return undef;
	}

	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		// TODO:check terms in db
		return new HashSet<DatabaseFormula>();
	}

	@Override
	public boolean supportsSerialization() {
		// TODO Auto-generated method stub
		return false;
	}
}
