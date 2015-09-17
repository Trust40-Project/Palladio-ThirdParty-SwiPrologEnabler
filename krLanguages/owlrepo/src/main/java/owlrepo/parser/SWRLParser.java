package owlrepo.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.parser.SWRLParserSupport;
import org.swrlapi.parser.SWRLTokenizer;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import owlrepo.language.SWRLVar;

public class SWRLParser implements Parser {
	org.swrlapi.parser.SWRLParser parser;
	SourceInfo info;
	String currentLine = "";
	int lineNr = 0;
	List<SourceInfo> errors;
	SWRLAPIOWLOntology onto;
	SWRLParserSupport swrlParserSupport;
	BufferedReader reader = null;
	List<RDFFormat> formats = null;
	SWRLParserUtil parserUtil;
	Set<String> undefined = new HashSet<String>();

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology) {
		this.parser = new org.swrlapi.parser.SWRLParser(swrlapiOWLOntology);
		this.errors = new ArrayList<SourceInfo>();
		this.onto = swrlapiOWLOntology;
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
		this.parserUtil = new SWRLParserUtil(this.parser, this.onto.getPrefixManager());
	}

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, List<RDFFormat> formats, Reader reader, SourceInfo info) {
		this(swrlapiOWLOntology);
		this.reader = new BufferedReader(reader);
		this.formats = formats;

		this.info = info;
	}

	public void parse(List<RDFFormat> formats, Reader reader, SourceInfo info) {
		this.reader = new BufferedReader(reader);
		this.formats = formats;
		this.info = info;
		this.errors.clear();
		this.undefined.clear();
	}

	private boolean parseCurrentLine() {
		// takes the next from the List of parsed lines as Strings
		try {
			this.currentLine = this.reader.readLine();
			this.lineNr++;
		} catch (IOException e) {
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
			return false;
		}

		// lines.get(lineNr); lineNr++;
		if (this.currentLine != null) {
			if (this.currentLine.isEmpty()) {
				return parseCurrentLine();
			}
			// process current line
			this.currentLine = this.currentLine.trim();
			if (this.currentLine.startsWith("(") && this.currentLine.endsWith(")")) {
				this.currentLine = this.currentLine.substring(1, this.currentLine.length() - 1);
			}
			return true;
		}
		return false;
	}

	private SWRLRule parseRule(String string) {
		SWRLRule rule = null;
		try {

			if (!string.isEmpty()) {
				// TODO: to avoid second pass parse KR file -- find better
				// alternative/add all possibilities
				if (string.startsWith("<?") || string.startsWith("<!")) {
					return rule;
				}
				// call SWRL parser
				if (string.contains("(")) {// we know it's a term, not an
					// argument
					// if (parser.isSWRLRuleCorrectAndComplete(string))
					rule = parseRule(string, "rule" + this.lineNr);
				}
			}
		} catch (SWRLParseException e) {
			this.undefined.add(string);
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
		}
		return rule;
	}

	public SourceInfo getInfo() {
		return this.info;
	}

	public Set<String> getUndefined() {
		return this.undefined;
	}

	private SWRLArgument parseArgument(String string) {
		SWRLArgument arg = null;
		// it's not a swrl rule or contains undefined iri-s

		if (!string.isEmpty()) {
			boolean isInHead = false;
			// get private method of swrlparser we need - swrldargument
			try {
				SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
				Method parseArgumentList = org.swrlapi.parser.SWRLParser.class.getDeclaredMethod("parseSWRLDArgument",
						SWRLTokenizer.class, boolean.class, boolean.class);
				parseArgumentList.setAccessible(true);// Abracadabra
				SWRLDArgument parsedArg = (SWRLDArgument) parseArgumentList.invoke(this.parser, tokenizer, isInHead,
						isInHead);// now
				// its
				// OK
				arg = parsedArg;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | SWRLParseException ex) {
				try {
					// try parsing swrliargument
					SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
					Method parseArgumentList = org.swrlapi.parser.SWRLParser.class
							.getDeclaredMethod("parseSWRLIArgument", SWRLTokenizer.class, boolean.class);
					parseArgumentList.setAccessible(true);// Abracadabra
					SWRLIArgument parsedArg = (SWRLIArgument) parseArgumentList.invoke(this.parser, tokenizer,
							isInHead);// now its OK
					arg = parsedArg;

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | SWRLParseException e) {
					return this.onto.getOWLDataFactory()
							.getSWRLLiteralArgument(this.onto.getOWLDataFactory().getOWLLiteral(string));
				}
			}
		}
		return arg;
	}

	public SWRLRule parseRule(String line, String name) throws SWRLParseException {
		// public SWRLRule parseSWRLRule(String ruleText, boolean
		// interactiveParseOnly, String ruleName, String comment)
		// throws SWRLParseException
		return this.parser.parseSWRLRule(line, false, name, "nocomment");
	}

	@Override
	public List<DatabaseFormula> parseDBFs() {
		// rule to list of dbformula
		List<DatabaseFormula> dbfs = new LinkedList<DatabaseFormula>();
		List<SWRLRule> parsed = parseRDF();
		// TODO!!
		if (parsed != null) {
			for (SWRLRule triple : parsed) {
				dbfs.add(new SWRLDatabaseFormula(triple));
			}
		}
		return dbfs;
	}

	private List<SWRLRule> parseRDF() {
		List<SWRLRule> triples = new LinkedList<SWRLRule>();
		String text = "";
		while (parseCurrentLine()) {
			if (this.currentLine.startsWith("<?") || this.currentLine.startsWith("<!")) {
				return null;
			}
			text += this.currentLine + "\n";
		}
		for (int i = 1; i < this.formats.size(); i++) {
			StringReader sreader = new StringReader(text);
			// first is always the owl file
			RDFFormat format = this.formats.get(i);
			if (format != null) {
				RDFParser rdfParser = Rio.createParser(format);
				List<Statement> statements = new ArrayList<Statement>();
				StatementCollector collector = new StatementCollector(statements);
				rdfParser.setRDFHandler(collector);
				try {
					if (!sreader.ready()) {
						throw new IOException("Reader not ready");
					}
					sreader.mark(1);
					rdfParser.parse(sreader, this.onto.getPrefixManager().getDefaultPrefix());
					for (Statement stm : statements) {
						// convert it into a SWRLRule or DBFormula somehow
						triples.add(this.parserUtil.getSWRLRule(stm));
					}
					sreader.reset();
				} catch (RDFParseException | RDFHandlerException | IOException | SWRLParseException e) {
					this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
				}
			}
		}

		return null;
	}

	@Override
	public Update parseUpdate() {
		// rule to update
		SWRLRule rule = null;

		if (parseCurrentLine()) {
			// parse rule
			rule = parseRule(this.currentLine);
			// rule to query
			if (rule != null) {
				return new SWRLUpdate(rule);
			} else if (this.errors.isEmpty()) {
				// parse argument
				SWRLArgument arg = parseArgument(this.currentLine);
				if (arg != null) {
					// check if it was an argument or a rule
					return new SWRLUpdate(arg);
				} else {
					return new SWRLUpdate(this.currentLine);
				}
			}
		}
		return new SWRLUpdate(this.onto.getOWLDataFactory()
				.getSWRLIndividualArgument(this.onto.getOWLDataFactory().getOWLAnonymousIndividual()));
		// throw new ParserException("Cannot create update from: " +
		// currentLine);
	}

	@Override
	public List<Query> parseQueries() {
		List<Query> queries = new LinkedList<Query>();
		Query q;
		while ((q = parseQuery()) != null) {
			queries.add(q);
		}
		return queries;
	}

	@Override
	public Query parseQuery() {
		SWRLRule rule = null;

		if (parseCurrentLine()) {
			// parse rule
			rule = parseRule(this.currentLine);
			// rule to query
			if (rule != null) {
				return new SWRLQuery(rule);
			} else if (this.errors.isEmpty()) {
				// parse argument
				SWRLArgument arg = parseArgument(this.currentLine);
				if (arg != null && arg instanceof SWRLVariable) {
					// check if it was an argument or a rule
					return new SWRLQuery(arg);
				} else {
					this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1,
							"Could not create query from: " + this.currentLine));
				}
				return new SWRLQuery(arg);
			}
		}
		this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1,
				"Could not create query: parsing is finished (returns null)"));
		return null;
	}

	/**
	 * parses the reader given to the parser on creation, tries to create a list
	 * of terms
	 *
	 * @return list of terms parsed, or empty list
	 */
	@Override
	public List<Term> parseTerms() {
		List<Term> terms = new LinkedList<Term>();
		// does not use currentLine, but instead splits the line by commas
		while (parseCurrentLine()) {
			String[] termstrings = this.currentLine.split(",");
			for (String term : termstrings) {
				Term pterm = parseTerm(term.trim());
				if (pterm != null) {
					terms.add(pterm);
				}
			}
		}
		return terms;
	}

	private SWRLTerm parseTerm(String termstring) {
		SWRLTerm term = null;
		if (!termstring.isEmpty()) {
			// parse rule
			SWRLRule rule = parseRule(termstring);
			// rule to term
			if (rule != null) {
				term = new SWRLTerm(rule);
			} else if (this.errors.isEmpty()) {
				// variable
				SWRLArgument arg = parseArgument(termstring);
				if (arg != null) {
					if (arg instanceof SWRLVariable) {
						term = new SWRLVar((SWRLVariable) arg);
					} else {
						term = new SWRLTerm(arg);
					}
				} else {
					term = new SWRLTerm(termstring);
				}
			}
		}
		return term;
	}

	// private SWRLVar parseVariable(String string) throws SWRLParseException{
	// return new
	// SWRLVar(swrlParserSupport.getSWRLVariable(string.substring(1,string.length())));
	// }

	/**
	 * parses the reader given to the parser on creation, tries to create a term
	 *
	 * @return a Term parsed, or null
	 */
	@Override
	public Term parseTerm() {
		// rule to term
		if (parseCurrentLine()) {
			return parseTerm(this.currentLine);
		}
		return null;
	}

	@Override
	public Var parseVar() {
		// rule to var
		if (parseCurrentLine()) {
			SWRLArgument arg = parseArgument(this.currentLine);
			if (arg != null) {
				if (arg instanceof SWRLVariable) {
					return new SWRLVar((SWRLVariable) arg);
				}
			} else {
				return new SWRLVar(this.currentLine);
			}
		}
		return null;
	}

	@Override
	public List<SourceInfo> getErrors() {
		return this.errors;
	}

	@Override
	public List<SourceInfo> getWarnings() {
		return new ArrayList<>(0); // TODO
	}

}
