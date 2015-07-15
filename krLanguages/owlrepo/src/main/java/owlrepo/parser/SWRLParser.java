package owlrepo.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.apache.commons.io.IOUtils;
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
	SWRLAPIOWLOntology onto ;
	SWRLParserSupport swrlParserSupport;
	BufferedReader reader = null;
	List<RDFFormat> formats = null;
	SWRLParserUtil parserUtil;

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		parser = new org.swrlapi.parser.SWRLParser(swrlapiOWLOntology);
		errors = new ArrayList<SourceInfo>();
		this.onto = swrlapiOWLOntology;
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
		this.parserUtil = new SWRLParserUtil(parser, onto.getPrefixManager());
	}

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, List<RDFFormat> formats, Reader reader, SourceInfo info) {
		this(swrlapiOWLOntology);
		this.reader = new BufferedReader(reader);
		this.formats = formats;
		
		this.info = info;
	}

	private boolean parseCurrentLine(){
		//takes the next from the List of parsed lines as Strings
		try {
			currentLine = reader.readLine();
			//System.out.println("current line: "+currentLine);
			lineNr++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//lines.get(lineNr); lineNr++;
		if (currentLine!= null){
			if (currentLine.isEmpty())
				return parseCurrentLine();
			//process current line
			currentLine = currentLine.trim();
			return true;
		}
		return false;
	}

	private SWRLRule parseRule(String string){
		SWRLRule rule =  null;
		try { 

			if (!string.isEmpty()){
				//TODO: to avoid second pass parse KR file -- find better alternative/add all possibilities
				if (string.startsWith("<?") || string.startsWith("<!")) 
					return rule;
				//call SWRL parser
				if (parser.isSWRLRuleCorrectAndComplete(string))
					rule = parseRule(string, "rule"+lineNr);

			}
			//			if (isSWRLRuleCorrectAndComplete(currentLine)) {
			//				//parse the line
			//				parse(currentLine, String.valueOf(line));
			//			} else {
			//				System.out.println("Incomplete rule: "+currentLine+" :: "+isSWRLRuleCorrectButPossiblyIncomplete(currentLine));
			//				errors.add(new SQWRLParserSourceInfo(null, line, -1, "Incomplete rule: "+currentLine));
			//			}

		} catch (SWRLParseException e) {
			e.printStackTrace(); 
			errors.add(new SWRLParserSourceInfo(info.getSource(), lineNr-1, -1, e.getMessage()));
		}
		return rule;
	}

	private SWRLArgument parseArgument(String string) throws ParserException {
		SWRLArgument arg = null;
		//it's not a swrl rule or contains undefined iri-s

		if (!string.isEmpty()){
			boolean isInHead = false;
			//get private method of swrlparser we need - swrldargument
			try {
				SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
				Method parseArgumentList = org.swrlapi.parser.SWRLParser.class.getDeclaredMethod("parseSWRLDArgument", SWRLTokenizer.class, boolean.class, boolean.class);
				parseArgumentList.setAccessible(true);// Abracadabra 
				SWRLDArgument parsedArg = (SWRLDArgument) parseArgumentList.invoke(parser, tokenizer, isInHead, isInHead);// now its OK
				arg = parsedArg;
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | SWRLParseException ex) {
				try{
					//try parsing swrliargument
					SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
					Method parseArgumentList = org.swrlapi.parser.SWRLParser.class.getDeclaredMethod("parseSWRLIArgument", SWRLTokenizer.class, boolean.class);
					parseArgumentList.setAccessible(true);// Abracadabra 
					SWRLIArgument parsedArg = (SWRLIArgument) parseArgumentList.invoke(parser, tokenizer, isInHead);// now its OK
					arg = parsedArg;

				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException | SWRLParseException e) {
					e.printStackTrace();
					//errors.add(new SWRLParserSourceInfo(info.getSource(), lineNr-1, -1, e.getMessage()));
					throw new ParserException(e.getMessage());
				}
			}
			//				System.out.println(arg);
		}
		return arg;
	}


	public SWRLRule parseRule(String line, String name) throws SWRLParseException{
		//		public SWRLRule parseSWRLRule(String ruleText, boolean interactiveParseOnly, String ruleName, String comment)
		//		throws SWRLParseException	
		return parser.parseSWRLRule(line, false, name, "nocomment");
	}

	@Override
	public List<DatabaseFormula> parseDBFs() throws ParserException {
		//rule to list of dbformula
		List<DatabaseFormula> dbfs = new LinkedList<DatabaseFormula>();
		List<SWRLRule> parsed = parseRDF();
		//TODO!!
		if (parsed != null)
			for (SWRLRule triple: parsed){
				dbfs.add(new SWRLDatabaseFormula(triple));
			}
		return dbfs;
	}

	private List<SWRLRule> parseRDF() throws ParserException{
		List<SWRLRule> triples = new LinkedList<SWRLRule>();
		String text = "";
		while (parseCurrentLine()){
			if (currentLine.startsWith("<?") || currentLine.startsWith("<!")) 
				return null;
			text+=currentLine+"\n";
		}
		//System.out.println(text);
		for (int i=1; i<this.formats.size(); i++){
			StringReader sreader = new StringReader(text);
			//first is always the owl file
			RDFFormat format = this.formats.get(i);
			RDFParser rdfParser = Rio.createParser(format);
			List<Statement> statements = new ArrayList<Statement>();
			StatementCollector collector = new StatementCollector(statements);
			rdfParser.setRDFHandler(collector);
			try {
				if (!sreader.ready()) System.out.println("Reader not ready");
				sreader.mark(1);
				rdfParser.parse(sreader, onto.getPrefixManager().getDefaultPrefix());
				for (Statement stm : statements){
				//	System.out.println(stm);
					//convert it into a SWRLRule or DBFormula somehow	
					triples.add(parserUtil.getSWRLRule(stm));
				}
				sreader.reset();
			} catch (RDFParseException | RDFHandlerException | IOException | SWRLParseException e) {
				e.printStackTrace();
				throw new ParserException(e.getMessage());
			}
		}

		return null;
	}

	@Override
	public Update parseUpdate() throws ParserException {
		//rule to update
		SWRLRule rule = null;

		if (parseCurrentLine()){
			//parse rule
			rule = parseRule(currentLine);
			//rule to query
			if (rule!=null)
				return new SWRLUpdate(rule);
			else {
				//parse argument
				SWRLArgument arg = parseArgument(currentLine);
				if (arg != null)
					//check if it was an argument or a rule
					return new SWRLUpdate(arg);
			}
		}
		return null;
	}


	@Override
	public List<Query> parseQueries() throws ParserException {
		List<Query> queries = new LinkedList<Query>();
		Query q;
		while((q=parseQuery())!=null){
			queries.add(q);
		}
		return queries;
	}


	@Override
	public Query parseQuery() throws ParserException {
		SWRLRule rule = null;

		if (parseCurrentLine()){
			//parse rule
			rule = parseRule(currentLine);
			//rule to query
			if (rule!=null)
				return new SWRLQuery(rule);
			else {
				//parse argument
				SWRLArgument arg = parseArgument(currentLine);
				if (arg != null)
					//check if it was an argument or a rule
					return new SWRLQuery(arg);
			}
		}

		return null;
	}


	@Override
	public List<Term> parseTerms() throws ParserException {
		List<Term> terms = new LinkedList<Term>();
		//does not use currentLine, but instead splits the line by commas
			while (parseCurrentLine()){
				String[] termstrings = currentLine.split(",");
				for (String term: termstrings)
					terms.add(parseTerm(term.trim()));
			}
		return terms;
	}

	private SWRLTerm parseTerm(String termstring) throws ParserException{
		SWRLTerm term = null;
		if (!termstring.isEmpty()){
			//parse rule
			SWRLRule rule = parseRule(termstring);
			//rule to term
			if (rule!=null){
				term = new SWRLTerm(rule);
			}else {
				//variable
				SWRLArgument arg = parseArgument(termstring);
				if (arg instanceof SWRLVariable)
					term = new SWRLVar((SWRLVariable)arg);
				else
					term = new SWRLTerm(arg);
			}
			//System.out.println(term);
		}
		return term;
	}

	//	private SWRLVar parseVariable(String string) throws SWRLParseException{
	//		return new SWRLVar(swrlParserSupport.getSWRLVariable(string.substring(1,string.length())));
	//	}

	@Override
	public Term parseTerm() throws ParserException {
		//rule to term
		if (parseCurrentLine()){
			SWRLRule rule = parseRule(currentLine);
			if (rule!=null)
				return new SWRLTerm(rule);
		}
		return null;
	}

	@Override
	public Var parseVar() throws ParserException {
		//rule to var
		if (parseCurrentLine()){
			SWRLArgument arg= parseArgument(currentLine);
			if (arg instanceof SWRLVariable)
				return new SWRLVar((SWRLVariable)arg);
		}
		return null;
	}


	public List<SourceInfo> getErrors() {
		return errors;
	}	



}
