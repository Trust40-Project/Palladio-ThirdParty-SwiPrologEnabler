package owlrepo.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.apache.commons.io.IOUtils;
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
	SourceInfo info;
	String currentLine = "";
	int lineNr = 0;
	List<SourceInfo> errors;
	SWRLAPIOWLOntology onto ;
	SWRLParserSupport swrlParserSupport;
	List<String> lines = null;
	
	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology){
		super(swrlapiOWLOntology);
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
	
	private SWRLRule parseLine(){
		SWRLRule rule =  null;
		try { 

			//takes the next from the List of parsed lines as Strings
			currentLine = lines.get(lineNr); lineNr++;
			
			//process current line
			currentLine = currentLine.trim();
			if (!currentLine.isEmpty()){
				//TODO: to avoid second pass parse KR file -- find better alternative/add all possibilities
				if (currentLine.startsWith("<?") || currentLine.startsWith("<!")) 
					return rule;
				//call SWRL parser
				rule = parseRule(currentLine, "rule"+lineNr);
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
	
			//it's not a swrl rule or contains undefined iri-s
//			//use owl parser
////			OWLParserFactory pf = new OWLParserFactoryImpl();
////			OWLParser parser =  pf.createParser();
////			parser.parse(null, null);


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
		SWRLRule rule = parseLine();
		if (rule!=null)
		return new SWRLDatabaseFormula(rule);
		else return null;
	}

	@Override
	public Update parseUpdate() throws ParserException {
		//rule to update
		SWRLRule rule = parseLine();
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
		SWRLRule rule = null;
		
		//parse term
		String line = lines.get(lineNr); lineNr++;
		SWRLTerm term;
		try {
			term = parseTerm(line);
		} catch (SWRLParseException e) {
			throw new ParserException(e.getMessage());
		}
		//check if it was a term or a rule
		if (term!=null)
			rule = term.getRule();
		else
			rule = parseLine();
		//rule to query
		if (rule!=null)
			return new SWRLQuery(rule);
		return null;
	}

	
	@Override
	public List<Term> parseTerms() throws ParserException {
		List<Term> terms = new LinkedList<Term>();
		try {
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
		if (termstring.startsWith("?")) {
			//variable
			term = parseVariable(termstring);
		} else{
			term = new SWRLTerm(parseLine());
		}
		System.out.println(term);
		return term;
	}

	private SWRLVar parseVariable(String string) throws SWRLParseException{
		return new SWRLVar(swrlParserSupport.getSWRLVariable(string.substring(1,string.length())));
	}
	
	@Override
	public Term parseTerm() throws ParserException {
		//rule to term
			SWRLRule rule = parseLine();
			if (rule!=null)
				return new SWRLTerm(rule);
		return null;
	}

	@Override
	public Var parseVar() throws ParserException {
			//rule to var
			SWRLRule rule = parseLine();
			Set<SWRLVariable> vars = rule.getVariables();
			return new SWRLVar(vars.iterator().next());
	}
		
	
	public List<SourceInfo> getErrors() {
		return errors;
	}	
	
	

}
