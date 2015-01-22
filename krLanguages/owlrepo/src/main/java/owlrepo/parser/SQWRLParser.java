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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.parser.SWRLParser;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLExpression;
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
	
	public SQWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		super(swrlapiOWLOntology);
		errors = new ArrayList<SourceInfo>();
	}
	
	public SQWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, BufferedReader reader) {
		this(swrlapiOWLOntology);
		this.reader = reader;
	}
	
	private SWRLRule parse(){
		SWRLRule rule =  null;
		//until end of file
		if (currentLine != null){

		try {
			//advance with the line
			currentLine = reader.readLine();
			System.out.println("Parsing: "+currentLine);
			line++;
			if (currentLine!=null){
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
	public Update parseUpdate(SourceInfo info) throws ParserException {
		//rule to update
		SWRLRule rule = parse();
		return new SWRLUpdate(rule);
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
		SWRLRule rule = parse();
		return new SWRLQuery(rule);
	}

	
	@Override
	public List<Term> parseTerms(SourceInfo info) throws ParserException {
			List<Term> terms = new LinkedList<Term>();
		Term t;
		while ((t=parseTerm(info))!=null){
			terms.add(t);
		}
		return terms;
	}

	@Override
	public Term parseTerm(SourceInfo info) throws ParserException {
		//rule to term
		SWRLRule rule = parse();

		return new SWRLTerm(rule);
	}

	@Override
	public Var parseVar(SourceInfo info) throws ParserException {
			//rule to var
			SWRLRule rule = parse();
			Set<SWRLVariable> vars = rule.getVariables();
			return new SWRLVar(vars.iterator().next());
	}
		
	
	public List<SourceInfo> getErrors() {
		return errors;
	}

	
	
	

}
