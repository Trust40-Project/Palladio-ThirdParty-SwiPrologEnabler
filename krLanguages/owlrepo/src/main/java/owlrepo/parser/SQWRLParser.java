package owlrepo.parser;

import java.io.BufferedReader;
import java.io.IOException;
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
import org.swrlapi.parser.SWRLParser;
import org.swrlapi.parser.SWRLParserSupport;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import owlrepo.language.SWRLVar;

public class SQWRLParser extends SWRLParser implements Parser {

	SWRLParser parser;
	BufferedReader reader;
	String currentLine = "";
	int line = -1;
	List<SourceInfo> errors;
	SWRLAPIOWLOntology onto ;
	SWRLParserSupport swrlParserSupport;
	
	public SQWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		super(swrlapiOWLOntology);
		errors = new ArrayList<SourceInfo>();
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
	}
	
	public SQWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, BufferedReader reader) {
		this(swrlapiOWLOntology);
		this.reader = reader;
	}
	
	private SWRLRule parse(int line, int pos){
		SWRLRule rule =  null;
		//until end of file
		if (currentLine != null){

		try { 
			//currentLine = reader.readLine();
			//System.out.println("Parsing: "+currentLine);
			while ((currentLine = reader.readLine()) != null){
			    System.out.println("<"+currentLine+">");
			line++;
		//	if (currentLine!=null){
				rule = (SWRLRule) parse(currentLine, String.valueOf(line));
			
//			if (isSWRLRuleCorrectAndComplete(currentLine)) {
//				//parse the line
//				parse(currentLine, String.valueOf(line));
//			} else {
//				System.out.println("Incomplete rule: "+currentLine+" :: "+isSWRLRuleCorrectButPossiblyIncomplete(currentLine));
//				errors.add(new SQWRLParserSourceInfo(null, line, -1, "Incomplete rule: "+currentLine));
//			}
				break;
			}
		} catch (SWRLParseException e) {
			e.printStackTrace(); 
			errors.add(new SQWRLParserSourceInfo(null, line, -1, e.getMessage()));
		} catch (IOException e) {
			e.printStackTrace();
			errors.add(new SQWRLParserSourceInfo(null, line, -1, e.getMessage()));
		}
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
	public List<DatabaseFormula> parseDBFs(SourceInfo info) throws ParserException {
		//rule to list of dbformula
			List<DatabaseFormula> dbfs = new LinkedList<DatabaseFormula>();
			DatabaseFormula dbf;
			while((dbf = parseDBF(info)) != null){
				dbfs.add(dbf);
			}
			return dbfs;
		}
	
	public DatabaseFormula parseDBF(SourceInfo info) throws ParserException {
	//rule to list of dbformula
		SWRLRule rule = parse(info.getLineNumber(), info.getCharacterPosition());
		if (rule!=null)
		return new SWRLDatabaseFormula(rule);
		else return null;
	}

	@Override
	public Update parseUpdate(SourceInfo info) throws ParserException {
		//rule to update
		SWRLRule rule = parse(info.getLineNumber(), info.getCharacterPosition());
		if (rule!=null)
			return new SWRLUpdate(rule);
		return null;
	}
	
	
	@Override
	public List<Query> parseQueries(SourceInfo info) throws ParserException {
			List<Query> queries = new LinkedList<Query>();
		Query q;
		while((q=parseQuery(info))!=null){
			queries.add(q);
		}
		return queries;
	}


	@Override
	public Query parseQuery(SourceInfo info) throws ParserException {
		//rule to query
		SWRLRule rule = parse(info.getLineNumber(), info.getCharacterPosition());
		if (rule!=null)
			return new SWRLQuery(rule);
		return null;
	}

	
	@Override
	public List<Term> parseTerms(SourceInfo info) throws ParserException {
		List<Term> terms = new LinkedList<Term>();
		try {
			String line = reader.readLine();
			String[] termstrings = line.split(",");
			for (String term: termstrings)
				terms.add(parseTerm(term));
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
			SWRLVariable var = swrlParserSupport.getSWRLVariable(termstring);
			term = new SWRLVar(var);
		}
		System.out.println(term);
		return term;
	}

	@Override
	public Term parseTerm(SourceInfo info) throws ParserException {
		//rule to term
			SWRLRule rule = parse(info.getLineNumber(), info.getCharacterPosition());
			if (rule!=null)
				return new SWRLTerm(rule);
		return null;
	}

	@Override
	public Var parseVar(SourceInfo info) throws ParserException {
			//rule to var
			SWRLRule rule = parse(info.getLineNumber(), info.getCharacterPosition());
			Set<SWRLVariable> vars = rule.getVariables();
			return new SWRLVar(vars.iterator().next());
	}
		
	
	public List<SourceInfo> getErrors() {
		return errors;
	}

	
	
	

}
