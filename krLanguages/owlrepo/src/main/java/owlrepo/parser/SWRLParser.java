package owlrepo.parser;

import java.io.IOException;
import java.io.Reader;
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
	List<String> lines = null;
	
	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		parser = new org.swrlapi.parser.SWRLParser(swrlapiOWLOntology);
		errors = new ArrayList<SourceInfo>();
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
	}
	
	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, Reader reader, SourceInfo info) {
		this(swrlapiOWLOntology);

		try {
			lines = IOUtils.readLines(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.info = info;
	}
	
	private boolean parseCurrentLine(){
		//takes the next from the List of parsed lines as Strings
		currentLine = lines.get(lineNr); lineNr++;
		
		//process current line
		currentLine = currentLine.trim();
		if (!currentLine.isEmpty())
			return true;
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
	
	private SWRLArgument parseArgument(String string) {
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
					errors.add(new SWRLParserSourceInfo(info.getSource(), lineNr-1, -1, e.getMessage()));
					//throw new SWRLParseException(e.getMessage());
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
			DatabaseFormula dbf;
			while((dbf = parseDBF()) != null){
				dbfs.add(dbf);
			}
			return dbfs;
		}
	
	public DatabaseFormula parseDBF() throws ParserException {
	//rule to list of dbformula
		if (parseCurrentLine()){
			SWRLRule rule = parseRule(currentLine);
			if (rule!=null)
				return new SWRLDatabaseFormula(rule);
		}
		return null;
	}

	@Override
	public Update parseUpdate() throws ParserException {
		//rule to update
		if (parseCurrentLine()){
			SWRLRule rule = parseRule(currentLine);
			if (rule!=null)
				return new SWRLUpdate(rule);
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
		try {//does not use currentLine, but instead splits the line by commas
			if (lines!=null){
				for (String line : lines){
					String[] termstrings = line.split(",");
					for (String term: termstrings)
						terms.add(parseTerm(term.trim()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParserException(e.getMessage());
		}
		return terms;
	}
	
	private SWRLTerm parseTerm(String termstring) throws SWRLParseException{
		SWRLTerm term = null;
		if (!termstring.isEmpty()){
			//parse rule
			SWRLRule rule = parseRule(termstring);
			//rule to term
			if (rule!=null){
				term = new SWRLTerm(rule);
			}else {
				//variable
				term = new SWRLTerm(parseArgument(termstring));
			}
			System.out.println(term);
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
