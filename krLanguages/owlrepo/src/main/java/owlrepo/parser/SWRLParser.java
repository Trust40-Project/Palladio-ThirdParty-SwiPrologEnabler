package owlrepo.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.parser.SWRLParserSupport;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import owlrepo.language.SWRLVar;

public class SWRLParser extends org.swrlapi.parser.SWRLParser implements Parser {

	org.swrlapi.parser.SWRLParser parser;
	StringReader reader;
	SourceInfo info;
	String currentLine = "";
	int line = -1;
	List<SourceInfo> errors;
	SWRLAPIOWLOntology onto ;
	SWRLParserSupport swrlParserSupport;
	
	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		super(swrlapiOWLOntology);
		errors = new ArrayList<SourceInfo>();
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
	}
	
	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, Reader reader, SourceInfo info) {
		this(swrlapiOWLOntology);
		this.reader = (StringReader)reader;
		this.info = info;
	}
	
	private SWRLRule parse(){
		SWRLRule rule =  null;
		//until end of file
		//if (currentLine != null){

		try { 
			//currentLine = reader.readLine();
			//System.out.println("Parsing: "+currentLine);
			 StreamTokenizer tokenizer = new StreamTokenizer(reader);
//			  while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
//			if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
//					System.out.println(tokenizer.sval);
//						
//		}}         

			//do{
			//	currentLine = reader.read();
			//	line++;
			   // System.out.println("<"+currentLine+">");
			//}
			//while (currentLine != "null");
			if (currentLine!="null"){
				rule = (SWRLRule) parse(currentLine, String.valueOf(line));
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
			errors.add(new SWRLParserSourceInfo(info.getSource(), line, -1, e.getMessage()));
		}
		
//		 else
//			System.out.println("EOF - by Parser -- by Reader");
		return rule;
	}
	
//	public SWRLExpression parse(String line, String name) throws SWRLParseException{
////		public SWRLRule parseSWRLRule(String ruleText, boolean interactiveParseOnly, String ruleName, String comment)
//				//		throws SWRLParseException
//		try{
//			SWRLRule rule =  parseRule(line, name);
//			return new SWRLExpression(rule);
//		}catch(SWRLParseException e){
//			//it's not a swrl rule or contains undefined iri-s
//			//use owl parser
////			OWLParserFactory pf = new OWLParserFactoryImpl();
////			OWLParser parser =  pf.createParser();
////			parser.parse(null, null);
//			throw new SWRLParseException(e.getMessage());
//		}
//	}
	public SWRLRule parse(String line, String name) throws SWRLParseException {
		return parseRule(line, name);
	}
	
	public SWRLRule parseRule(String line, String name) throws SWRLParseException{
//		public SWRLRule parseSWRLRule(String ruleText, boolean interactiveParseOnly, String ruleName, String comment)
				//		throws SWRLParseException	
		return parseSWRLRule(line, false, name, "nocomment");
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
		SWRLRule rule = parse();
		if (rule!=null)
		return new SWRLDatabaseFormula(rule);
		else return null;
	}

	@Override
	public Update parseUpdate() throws ParserException {
		//rule to update
		SWRLRule rule = parse();
		if (rule!=null)
			return new SWRLUpdate(rule);
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
		//rule to query
		SWRLRule rule = parse();
		if (rule!=null)
			return new SWRLQuery(rule);
		return null;
	}

	
	@Override
	public List<Term> parseTerms() throws ParserException {
		List<Term> terms = new LinkedList<Term>();
		try {
			parse();
		//	String line = reader.readLine();
		//	String[] termstrings = line.split(",");
		//	for (String term: termstrings)
		//		terms.add(parseTerm(term.trim()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParserException(e.getMessage());
		}
		return terms;
	}
	
	private Term parseTerm(String termstring) throws SWRLParseException{
		Term term = null;
		if (termstring.startsWith("?")) {
			//variable
		//	swrlParserSupport.checkThatSWRLVariableNameIsValid(termstring);
			SWRLVariable var = swrlParserSupport.getSWRLVariable(termstring.substring(1,termstring.length()));
			term = new SWRLVar(var);
		}
		System.out.println(term);
		return term;
	}

	@Override
	public Term parseTerm() throws ParserException {
		//rule to term
			SWRLRule rule = parse();
			if (rule!=null)
				return new SWRLTerm(rule);
		return null;
	}

	@Override
	public Var parseVar() throws ParserException {
			//rule to var
			SWRLRule rule = parse();
			Set<SWRLVariable> vars = rule.getVariables();
			return new SWRLVar(vars.iterator().next());
	}
		
	
	public List<SourceInfo> getErrors() {
		return errors;
	}	
	
	

}
