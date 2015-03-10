/*
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

grammar Prolog;
 
/*
 * This is an ANTLR 3 grammar for the Prolog language.
 *
 * <p>We have followed the ISO/IEC 13211-1 International Standard for Prolog in most cases, but not always.
 * Existing Prolog implementations do not completely conform to the Standard, and, in practice, some
 * compromises have to be made (see below). Other reasons for deviating have been more pragmatically
 * motivated: we wanted to keep our grammar simple (and accordingly choose simple solutions to resolve
 * certain issues) and we did not want our grammar to support certain options that quickly lead to
 * unreadable code (e.g., using graphic tokens as predicate names). For a list of deviations, please see
 * the end of this file. For those who would like to conform more closely to the Standard, this grammar
 * may provide a starting point.</p>
 *
 * <p>All references below of the form "6.3.4" refer to clauses of the International Standard for Prolog.</p>
 *
 * <p>Compile with ANTLR v3.2 from the command line using: java org.antlr.Tool Prolog.g.</p>
 */

options {
  k = 1;
}

@parser::header {
    package swiprolog.parser;

    import krTools.errors.exceptions.ParserException;
    import krTools.language.DatabaseFormula;
    import krTools.language.Query;
    import krTools.language.Term;
    import krTools.parser.SourceInfo;
    
    import java.io.File;
	
	import swiprolog.language.PrologDBFormula;
	import swiprolog.language.PrologQuery;
	import swiprolog.language.PrologTerm;
	import swiprolog.language.PrologUpdate;
	import swiprolog.language.PrologVar;
	import swiprolog.language.JPLUtils;
}
@lexer::header {
    package swiprolog.parser;
    
    import krTools.errors.exceptions.ParserException;
    
    import java.io.File;
}

@lexer::members {

    /**
     * The list of errors that were found during parsing.
     */
    private ArrayList<ParserException> errors;
    
    public void initialize() {
        errors = new ArrayList<ParserException>();
    }
    
    public File getSource() {
    	return new File(getSourceName());
    }
    
    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    	SourceInfoObject info = new SourceInfoObject(getSource(), e.line, e.charPositionInLine);
    	ParserException newErr;
    	if (e instanceof MismatchedTokenException && e.token != null) {
           	newErr = new ParserException("Found " + e.token.getText() + " where I was expecting "
           		+ PrologParser.tokenNames[((MismatchedTokenException)e).expecting], info);
        } else if (e instanceof MissingTokenException) {
          	newErr = new ParserException(PrologParser.tokenNames[((MissingTokenException)e).expecting] + " is missing here", info);
        } else if (e instanceof NoViableAltException) { 
           	newErr = new ParserException("Cannot use " + Character.toString((char)e.input.LA(1)) + " here", info);
        } else if (e instanceof UnwantedTokenException && e.token != null) {
    		newErr = new ParserException("Syntax error on '" + e.token.getText() + "', delete this", info);
    	} else if (e.getCause() instanceof ParserException) { // embedded parser exception we should use
        	ParserException cause = ((ParserException)e.getCause());
        	if (cause.hasSourceInfo()) {
        		info = new SourceInfoObject(getSource(), cause.getLineNumber(), cause.getCharacterPosition());
        	}
            newErr = new ParserException(e.getCause().getMessage(), info);
        } else {
        	newErr = new ParserException("Sorry, cannot make anything out of this", e);
        }
        errors.add(newErr);
    }
    
    public ArrayList<ParserException> getErrors() {
        return this.errors;
    }
    
    /**
     * @return {@code true} iff the parser found a syntax error.
     */
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
    
    /**
     * @return the last error generated while parsing.
     */
    public ParserException getLastError() {
        if(!this.hasErrors()) {
            return null;
        } else {
            return this.errors.get(this.errors.size()-1);
        }
    }
}

@parser::members {
    private PrologLexer lexer;
    private CharStream cs;
	private ArrayList<ParserException> errors;
  
    public void initialize() {
		errors = new ArrayList<ParserException>();
    }
    
    public File getSource() {
    	return new File(getSourceName());
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    	SourceInfoObject info = new SourceInfoObject(getSource(), e.line, e.charPositionInLine);
    	ParserException newErr = null;
    	if (e.token != null && e.token.getText() != null) {
    		if (e instanceof MismatchedTokenException) {
           		newErr = new ParserException("Found " + e.token.getText() + " where I was expecting " + tokenNames[((MismatchedTokenException)e).expecting], info);
        	} else if (e instanceof MissingTokenException && e.token.getText() != null) {
          		newErr = new ParserException(tokenNames[((MissingTokenException)e).expecting] + " is missing here", info);
        	} else if (e instanceof NoViableAltException && e.token.getText() != null) { 
           		newErr = new ParserException("Cannot use " + e.token.getText() + " here", info);
        	} else if (e instanceof UnwantedTokenException && e.token.getText() != null) {
    			newErr = new ParserException("Syntax error on '" + e.token.getText() + "', delete this", info);
    		} else if (e.getCause() instanceof ParserException) { // embedded parser exception we should use
        		ParserException cause = ((ParserException)e.getCause());
        		if (cause.hasSourceInfo()) {
        			info = new SourceInfoObject(getSource(), cause.getLineNumber(), cause.getCharacterPosition());
        		}
            	newErr = new ParserException(cause.getMessage(), info);
        	} else {
        		newErr = new ParserException("Sorry, cannot make anything out of this", e);
        	}
        } else {
        	newErr = new ParserException("Sorry, cannot make anything out of this", e);
        }
        errors.add(newErr);
    }
    
    public ArrayList<ParserException> getErrors() {
        return this.errors;
    }
    
    /**
     * @return {@code true} iff the parser found a syntax error.
     */
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
    
    /**
     * @return the last error generated while parsing.
     */
    public ParserException getLastError() {
        if(!this.hasErrors()) {
            return null;
        } else {
            return this.errors.get(this.errors.size()-1);
        }
    }

  // Disable standard error handling by ANTLR; be strict ; see the definitive ANTLR ref manual, p.252.
  protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
  	throw new MismatchedTokenException(ttype, input);
  }

  /**
  * Prolog parser fails on error.
  */
  @Override
  public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) 
    throws RecognitionException {
    throw e;
  }

  public void setInput(PrologLexer lexer, CharStream cs) {
    this.lexer = lexer;
    this.cs = cs;
  }
  
  public PrologLexer getLexer() {
      return this.lexer;
  }
  
    /**
     * Parses a Prolog program. Assumes that the parser has been set up properly.
     *
     * @return ArrayList<DatabaseFormula>, or {@code null} if a parser error occurs.
     */
	public ArrayList<DatabaseFormula> parsePrologProgram() throws ParserException {
		try {
			ArrayList<PrologTerm> prologTerms = prologtextWithImports();

            // Parser does not check if Prolog terms are correct database objects, do this next
            ArrayList<DatabaseFormula> dbfs = new ArrayList<DatabaseFormula>();
            for (PrologTerm t: prologTerms) {
            	PrologTerm updatedSourceInfo = new PrologTerm(t.getTerm(), t.getSourceInfo());
				dbfs.add(DBFormula(updatedSourceInfo));
			}
            return dbfs;
		} catch (RecognitionException e) {
          reportError(e);
          return null;
        }
    }
    
    /**
     * Check that all terms are formula that can be inserted into a Prolog database.
     *
     * @return ArrayList<DatabaseFormula> List of database formulas derived from input Prolog terms.
     * @throws ParserException If an input Prolog term cannot be converted to a database formula.
     */
    private ArrayList<DatabaseFormula> toDBFormulaList(ArrayList<PrologTerm> prologTerms) 
        throws RecognitionException {
        ArrayList<DatabaseFormula> DBFormulaList = new ArrayList<DatabaseFormula>();
        for (PrologTerm t: prologTerms)
            try {
				DBFormulaList.add(DBFormula(t));
			} catch (ParserException e) {
				RecognitionException err = new RecognitionException();
    			err.initCause(e);
				throw err;
			}
        return DBFormulaList;
    }
    
    /**
     * <p>Converts given {@Link PrologTerm} into a {@link DatabaseFormula} object made of it.
     * Basic idea is that it checks that assert() will work (not throw exceptions). 
     * This function checks only SINGLE formulas, not conjunctions. Use toDBFormulaList for that.
     * </p>
     * <p>
     * Check ISO section 8.9.1.3. If term not of form "head:-body" then head is
     * to be taken the term itself and body to be "true" We fail if<br>
     * 1. Head is a variable<br>
     * 2. head can not be converted to a predication (@see D-is-a-precication in
     * ISO p.132- )<br>
     * 3. body can not be converted to a goal<br>
     * CHECK 4.
     * "The predicate indicator Pred of Head is not that of a dynamic procedure"
     * . What does that mean and should we do something to prevent this?
     * </p>
     * <p>
     * ISO section 6.2 also deals with this. Basically it defines directive
     * terms and clause terms, and combined they allow every term that can be
     * part of the database.
     * </p>
     * <p>
     * ISO section 7.4 defines the way they are used when loading a database.
     * Particularly the operators that are Directives are treated specially. In
     * GOAL we do not want to support these and need exclusion. this is done via
     * PrologOperators.goalProtected()
     * </p>
     * 
     * @param conjunction is a PrologTerm containing a conjunction
     * @see PrologTerm#getConjuncts
     * @see PrologTerm#useNotAllowed useNotAllowed
     * @see toDBFormulaList
     * @returns DatabaseFormula object made from conjunction
     * @throws ParserException If Prolog term is not a valid clause.
     */
    private DatabaseFormula DBFormula(PrologTerm term) throws ParserException {
        jpl.Term head, body;

        if (term.getSignature().equals(":-/2")) {
            head = term.getTerm().arg(1);
            body = term.getTerm().arg(2);
        } else {
            head = term.getTerm();
            body = new jpl.Atom("true");
        }
        
        if (head.isVariable()) {
            throw new ParserException(
                "The head of a Prolog rule cannot be a variable " + term.toString(),
                term.getSourceInfo());
        }
        
        if (!JPLUtils.isPredication(head)) {
            throw new ParserException( 
                "The head of a Prolog rule cannot be " + term.toString() + 
                " but should be a Prolog clause",
                term.getSourceInfo());
        }
        
        String signature = JPLUtils.getSignature(head);
        if (PrologOperators.prologBuiltin(signature)) {
            throw new ParserException( 
                "Cannot redefine the Prolog built-in predicate " + term.toString(),
                term.getSourceInfo());
        }
        
        // check for special directives, and refuse those.
        String name = signature.substring(0, signature.indexOf('/'));
        if (PrologOperators.goalProtected(name)) {
            throw new ParserException( 
                "Some predicates, like " + head.toString() + ", are protected " +
                "and are not allowed in the head of a Prolog clause: " +
                term.toString(),
                term.getSourceInfo());
        }
        
        toGoal(body); // try to convert, it will throw if it fails.
        
        return new PrologDBFormula(term.getTerm(), term.getSourceInfo());
    }
    
    /**
     * Extract source info from token.
     *
     * @return Source info object.
     */
    private SourceInfo getSourceInfo(Token token) {
    	return new SourceInfoObject(getSource(), token.getLine(), token.getCharPositionInLine());
    }
    

	/**
	 * Unquote a quoted string. The enclosing quotes determine how quotes inside the string are handled.
	 */
	private String unquote(String quotedstring) {
		char quote = quotedstring.charAt(0);
		String unquoted = quotedstring.substring(1, quotedstring.length()-1);
		// CHECK SWI does first replaceQuotes, then escape. Try '\''''. Is that ISO?
		return unescape(replaceQuotes(unquoted,quote));
	}
	
	/**
	 * Double quotes in quoted string indicate just that quote one time. eg, """" means '"'.  
	 */
	private String replaceQuotes(String string, char quote) {
		return string.replaceAll(""+quote+quote,""+quote);
	}
	
	/**
	 * Unescape string according to ISO standard. Not implemented #2917
	 */
	private String unescape(String string) {
		return string;
	}
	
       /**
         * Parse a section that should contain Prolog goals, i.e., queries.
         *
         * @return ArrayList<Query>, or {@code null} if a parser error occurs.
         * @throws ParserException 
         */
        public ArrayList<Query> parsePrologGoalSection() throws ParserException { 
            ArrayList<Query> goals = new ArrayList<Query>();
            
            try {
                ArrayList<PrologTerm> prologTerms = prologtext();

                for (PrologTerm t: prologTerms) {
                	// check that each term is a valid Prolog goal / query
                    goals.add(new PrologQuery(toGoal(t.getTerm()), t.getSourceInfo()));
                }
                return goals;
            } catch (RecognitionException e) {
                reportError(e);
                return null;
            }
        }
	
	/**
     * Checks that term is a well formed Prolog goal.
     * <p>
     * ISO requires rebuild of the term but in our case we do not allow
     * variables and hence a real rebuild is not necessary.
     * Instead, we simply return the original term after checking.
     * </p>
     * 
     * @return the term "rewritten" as a Prolog goal according to ISO.
     * @throws ParserException If t is not a well formed Prolog goal.
     */
    private jpl.Term toGoal(jpl.Term t) throws ParserException {
        // 7.6.2.a use article 7.8.3
        if (t.isVariable()) {
            throw new ParserException(
                 "Variables cannot be used as goals: " + t.toString());
        }
        // 7.6.2.b
        String sig = JPLUtils.getSignature(t);
        if (PrologOperators.goalProtected(t.name())) {
            throw new ParserException(
                 "The use of predicate " + t.toString() + ": " + 
                 t.toString() + " is not supported");
        }
        if (sig.equals(":-/2")) {
        	throw new ParserException("Cannot use a clause " + t.toString() + " as a goal");
        }
        if (sig.equals(",/2") || sig.equals(";/2") || sig.equals("->/2")) {
            toGoal( t.arg(1));
            toGoal( t.arg(2));
        }
        // 7.6.2.c
        // no action required. 
        return t;
    }
  
  /**
   * Parse a Prolog conjunction and check it's a proper update.
   *
   * @return PrologUpdate, or {@code null} in case of a parser error.
   */
  public PrologUpdate ParseGOALUpdate() {
    try {
		return conj2Update(term1000());
    } catch(RecognitionException e) {
        reportError(e);
        return null;
	} catch (ParserException e) {
        RecognitionException err = new RecognitionException();
    	err.initCause(e);
        reportError(err);
        return null;
	}
  }
  
  /**
  * Parse a Prolog conjunction and check it's a proper update.
  * @return PrologUpdate, or null if parser error occurs.
  */
  public PrologUpdate ParseUpdate() {
    try {
      return conj2Update(term1000());
     } catch(RecognitionException e) {
        	reportError(e);
            return null;
    } catch (ParserException e) {
      RecognitionException err = new RecognitionException();
   	  err.initCause(e);
      reportError(err);
      return null;
    }
  }
  
  /**
   * Parses a (possibly empty) Prolog conjunction and checks whether it's a valid update.
   *
   * @param A source info object.
   * @return PrologUpdate, or {@code null} if parser error occurs.
   */
  public PrologUpdate ParseUpdateOrEmpty()  {
    try {
		PrologTerm conj = possiblyEmptyConjunct();
		if (conj.toString().equals("true")) {
			return new PrologUpdate(conj.getTerm(), conj.getSourceInfo());
		} else {
			return conj2Update(conj);
		}
	} catch(RecognitionException e) {
		reportError(e);
		return null;
	} catch(ParserException e) {
		RecognitionException err = new RecognitionException();
    	err.initCause(e);
        reportError(err);
		return null;
	}    
  }
      
    /**
     * <p>
     * Checks if {@link PrologTerm} may be used as update, i.e. a conjunct of
     * either a {@link DatabaseFormula} or not(DatabaseFormula).
     * @param conjunct is the PrologTerm to be checked which is a zipped conjunct.
     * (see {@link PrologTerm#getConjuncts}) 
     * @throws ParserException if term not acceptable as DatabaseFormula.</p>
     * @returns original conjunct (if term is good update)
     */
    private jpl.Term basicUpdateCheck(PrologTerm conjunct) throws ParserException {
        List<jpl.Term> terms = JPLUtils.getOperands(",", conjunct.getTerm());
        
        for (jpl.Term term : terms) {
            if (JPLUtils.getSignature(term).equals("not/1")) {
                DBFormula(new PrologTerm(term.arg(1), conjunct.getSourceInfo()));
            } else {
                DBFormula(new PrologTerm(term, conjunct.getSourceInfo()));
            }
        }
        return conjunct.getTerm();
    }
          
    /**
     * <p>
     * Performs additional checks on top of {@link #basicUpdateCheck}, to check
     * that conjunct is good update.</p>
     * @param {@link PrologTerm} that is supposedly an update (ie conjunct of [not] dbFormula)
     * @returns the original term (no conversion is performed)
     * @throws ParserException if term is no good Update.
     * @see #checkDBFormula
     */
    private PrologUpdate conj2Update(PrologTerm conjunct) throws ParserException {
		PrologUpdate update = new PrologUpdate(basicUpdateCheck(conjunct), conjunct.getSourceInfo());

        return update;
    }

    /**
    * <p>
    * Checks that conjunction is a Prolog query and returns {@link PrologQuery} object made of it.
    * This means that, if successful, the result can be queried to the (SWI) prolog engine.
    * </p>
    * <p>
    * ISO section 7.6.2 on p.27 specifies how to convert a term to a goal.
    * </p>
    * @param conjunction is a PrologTerm containing a conjunction
    * @see PrologTerm#getConjuncts
    * @see PrologTerm#useNotAllowed useNotAllowed
    * @returns Query object made from conjunction
    * @throws ParserException if prologTerm is not a good Query.
    */
    public PrologQuery toQuery(PrologTerm conjunction) throws ParserException {
        return new PrologQuery(toGoal(conjunction.getTerm()), conjunction.getSourceInfo());
    }
     
   /**
    * Parses a query.
    *
    * @return A {@link PrologQuery}, or {@code null} if an error occurred.
    */   
    public PrologQuery ParseQuery() {
		try {
			return toQuery(term1100());
		} catch(RecognitionException e) {
        	reportError(e);
            return null;
        } catch (ParserException e) {
			RecognitionException err = new RecognitionException();
			err.initCause(e);
			reportError(err);
			return null;
        }
	}
    
   /**
    * Parses a (possibly empty) query.
    *
    * @return A {@link PrologQuery}, or {@code null} if an error occurred.
    */
    public PrologQuery ParseQueryOrEmpty() {
		try {
			return toQuery(possiblyEmptyDisjunct());
		} catch(RecognitionException e) {
			reportError(e);
			return null;
		} catch (ParserException e) {
          RecognitionException err = new RecognitionException();
    	  err.initCause(e);
          reportError(err);
          return null;
        }
    }
    
   /**
    * Parse a set of parameters.
    *
    * @return A list of {@link Term}s.
    */
    public List<Term> ParsePrologTerms() {
      try {
        PrologTerm t = term1000();
        ArrayList<Term> terms = new ArrayList<Term>();
        for (jpl.Term term : JPLUtils.getOperands(",", t.getTerm())) {
          if (term instanceof jpl.Variable) {
            terms.add(new PrologVar((jpl.Variable)term, t.getSourceInfo()));
          } else {
            terms.add(new PrologTerm(term, t.getSourceInfo()));
          }
        }
        return terms;
      } catch(RecognitionException e) {
          reportError(e);
          return null;
      }    
    }
    
   /**
    * try parse a term
    * @return term, or null if error occurs
   */   
    public PrologTerm ParseTerm() {
       try {
            return term0();
       } catch (RecognitionException e) {
            reportError(e);
            return null;
       }  
    }
}

@rulecatch {
	catch (RecognitionException e) {
		throw e;
	}
}

/*---------------------------------------------------------------------
 * PARSER RULES
 *---------------------------------------------------------------------*/
 
/**
 * Parses a Prolog file.
 * <p>
 * Expects Prolog program with common Prolog syntax,
 * but requires the EOF token after the last clause or directive.
 * </p> 
 */
prologfile returns [ArrayList<PrologTerm> clauses]
    :
        { clauses = new ArrayList<PrologTerm>(); }
        (
            (
              term = directive
            | term = clause
            )
            { clauses.add(term); }
        )*
        EOF
    ;

/**
 * The same rule as #prologtext
 */ 
prologtextWithImports returns [ArrayList<PrologTerm> clauses]
    :
        { clauses = new ArrayList<PrologTerm>(); }
        ( (
            term = directive
          | term = clause
          )
        { clauses.add(term); }
        )*
    ;
    
prologtext returns [ArrayList<PrologTerm> clauses] // 6.2.1
  :
    { clauses = new ArrayList<PrologTerm>(); }
    ( (
        term = directive
      | term = clause
      )
    { clauses.add(term); }
    )*
  ;

directive returns [PrologTerm term] // 6.2.1.1
  :
     ':-' t = term1200 ENDTOKEN
     { jpl.Term[] args = { t.getTerm() };
       term = new PrologTerm(new jpl.Compound(":-", args), t.getSourceInfo());
     }
  ;

clause returns [PrologTerm term] // 6.2.1.2
  :
      t = term1200 ENDTOKEN { term = t; }
  ;

arglist returns [ArrayList<PrologTerm> arguments] // 6.3.3
  :
      { ArrayList<PrologTerm> argList = new ArrayList<PrologTerm>(); }
        exp = expression
      { argList.add(exp); }
        ( ',' argTail = arglist { argList.addAll(argTail); } )?
      { arguments = argList; }
  ;

possiblyEmptyConjunct returns [PrologTerm conjunct]
  :
        conj = term1000?
     { conjunct = (conj != null)? conj : new PrologTerm(new jpl.Atom("true"), null); }
  ;
  
possiblyEmptyDisjunct returns [PrologTerm conjunct]
  :
        conj = term1100?
     { conjunct = (conj != null)? conj : new PrologTerm(new jpl.Atom("true"), null); }
  ;
  
expression returns [PrologTerm term] // 6.3.3.1
  :
      t = term900 { term = t; }
  ;

listterm returns [PrologTerm term] // 6.3.5
  :
        '[' i = items? ']'
      { if (i==null) {
           term = new PrologTerm(new jpl.Atom("[]"), null);
        } else {
           term=i;
        }
      }
  ;

items returns [PrologTerm term] // 6.3.5 ; we use the prolog "." functor to build items list.
  :
  	  { int index = this.cs.index(); }
        l = expression
      { jpl.Term[] args = { l.getTerm() };
        term = new PrologTerm(new jpl.Compound(".", args), l.getSourceInfo()); }
      (
        (',' r=items) 
        | ('|' (
              r=listterm  
            | (
                r1 = VARIABLE 
              { r = new PrologVar(new jpl.Variable(r1.getText()), getSourceInfo(r1)); }
              )
            )
        )
       )?
       { if (r==null) {
           jpl.Term[] args1 = { l.getTerm(), new jpl.Atom("[]") };
           term = new PrologTerm(new jpl.Compound(".", args1), l.getSourceInfo());
       } else {
           jpl.Term[] args2 = { l.getTerm(), r.getTerm() };
           term = new PrologTerm(new jpl.Compound(".", args2), l.getSourceInfo());
       }
    }
  ;

prefixoperator returns [PrologTerm term] 
  // compensates for absence of names derived from graphic token.
  // Note that ',' cannot be used as prefix operator since the comma character is not a graphic token
  // char (cf. 6.4.2, 6.5.1).
  // In addition, we do not allow ':-' as prefix operator to avoid ambiguity and allow '-' 
  // only as unary prefix operator (see term200 below for the 'infix' version.)
  : 
      f=( '-->' | ';' | '->' | '=' | '\\=' | '==' | '\\==' | '@<' | '@=<' | '@>' | '@>=' | '=..' | 'is' |
    '=:=' | '=\\=' | '<' | '=<' | '>' | '>=' | '+' | '/\\' | '\\/' | '*' | '/' | '//' | 'rem' |
    'mod' | '<<' | '>>' | '**' | '^'  ) '(' e1=expression ',' e2=expression ')'
    { jpl.Term[] args = { e1.getTerm(), e2.getTerm() };
      term = new PrologTerm(new jpl.Compound(f.getText(), args), getSourceInfo(f)); }
  ;

/* 
 * Prolog terms have been defined using the grammar design pattern in Terence Parr, 2007, The Definitive ANTLR
 * Reference p. 61, 275. Below the numbers after 'term' indicate the operator precedence (see the ISO Standard
 * for more information).
 */
term0 returns [PrologTerm term]
  :
  	{ int index = this.cs.index(); }
      (
        tk = NUMBER  
        // 6.3.1.1 (The Standard supports negative numbers explicitly; 
        // using the '-/1' operator these are covered here as well; see term200 below). 
    {
      if (tk.getText().matches("[0-9]+") || tk.getText().matches("0[box].*")) {
		Long val = Long.valueOf(tk.getText());
        term = new PrologTerm(JPLUtils.createIntegerNumber(val), getSourceInfo(tk)); // int, octal, hex, etc.
      } else { // float
        term = new PrologTerm(new jpl.Float(Double.valueOf(tk.getText())), getSourceInfo(tk)); // float
      }
    }
    | tk = NAME ( '(' a=arglist ')' )? // 6.3.1.3 and 6.3.3;
    { if (a==null) {
         term = new PrologTerm(new jpl.Atom(tk.getText()), getSourceInfo(tk));
      } else { 
         List<jpl.Term> terms = new ArrayList<jpl.Term>();
         for (PrologTerm pterm: a) {
            terms.add(pterm.getTerm());
         }
         term = new PrologTerm(new jpl.Compound(tk.getText(), terms.toArray(new jpl.Term[0])), getSourceInfo(tk));
      } 
    } 
    | tk = VARIABLE // 6.3.2
    { term = new PrologVar(new jpl.Variable(tk.getText()), getSourceInfo(tk)); }
    | tk = STRING // Compare 6.4.2.1
      { String str = tk.getText();
        term = new PrologTerm(new jpl.Atom(unquote(str)), getSourceInfo(tk));
      } 
    | '(' t = term1200 ')' // 6.3.4.1
    { term = t; }
    | '{' t = term1200 '}' // 6.3.6
    { jpl.Term[] args = { t.getTerm() };
      term = new PrologTerm(new jpl.Compound("{}", args), t.getSourceInfo()); } 
    | t = listterm
    { term = t; }
    | t = prefixoperator
    { term = t; }
    )
  ;

term50 returns [PrologTerm term]
  :
      t1 = term0 
      (':' t2 = term0)?
    { if (t2==null) {
         term = t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(":", args), t1.getSourceInfo());
      }
    }
  ;

term100 returns [PrologTerm term]
  :
      t1 = term50
      ('@' t2 = term50)?
    { if (t2==null) {
         term = t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound("@", args), t1.getSourceInfo());
      }
    } 
  ;

term200 returns [PrologTerm term]
  :
      (
        (op='-' | op='\\' ) t=term200 
      { jpl.Term[] args = { t.getTerm() };
        term = new PrologTerm(new jpl.Compound(op.getText(), args), t.getSourceInfo());
        if (op.getText().equals("-")) {
          // minus sign, check special case of numeric constant. See ISO 6.3.1.2 footnote
          // Note, we interpret this footnote RECURSIVELY, eg --1 == 1.
          // Note that this notation is not SWI prolog compatible, SWI seems to fail ISO 
          // compliance here.
            if (t.getTerm().isFloat()) {
               term = new PrologTerm(new jpl.Float(-1 * ((PrologTerm)t).getTerm().floatValue()), t.getSourceInfo());
            } else { // integer
               term = new PrologTerm(new jpl.Integer(-1 * ((PrologTerm) t).getTerm().intValue()), t.getSourceInfo());
            }
        }
      }
      | t1 = term100
        ( (op='^' t2=term200) | (op='**' t2=term100) )?
      { if (t2==null) {
           term = t1;
        } else {
           jpl.Term[] args = { t1.getTerm(), t2.getTerm() }; 
           term = new PrologTerm(new jpl.Compound(op.getText(), args), getSourceInfo(op));
        }
      }
    )
  ;

term400 returns [PrologTerm term] 
// Operators *, /, //, rem, mod, <<, and >> are left-associative
// rdiv is SWI prolog specific.
  : 
      t = term200
    (
      (op='*'  | op='/' | op='//' | op='rem' | op='mod' | op='rdiv' | op='<<' | op='>>') 
      t1 = term200
    { jpl.Term[] args = { t.getTerm(), t1.getTerm() };
      t = new PrologTerm(new jpl.Compound(op.getText(), args), getSourceInfo(op)); }
    )*
    { term = t; }
  ;

term500 returns [PrologTerm term] // Operators +, -, /\, and \/ are left-associative
// 'xor' and '><' are SWI specific.
  :
      t = term400
    (
      op=('+' | '-' | '/\\' | '\\/' | 'xor' | '><') 
      t1 = term400
    { jpl.Term[] args = { t.getTerm(), t1.getTerm() };
      t = new PrologTerm(new jpl.Compound(op.getText(), args), getSourceInfo(op)); }
    )*
    { term = t; }   
  ;

term700 returns [PrologTerm term]
  : 
      t1 = term500
              ( 
      op=('=' | '\\=' | '==' | '\\==' | '@<' | '@=<' | 
        '@>' | '@>=' | '=@='| '=..' | 'is' | '=:=' | '=\\=' |
        '<' | '=<' | '>' | '>=') t2=term500 )?
    { if (t2==null) {
         term = t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(op.getText(), args), getSourceInfo(op));
      }
    }
  ;

term900 returns [PrologTerm term] // CHECK UNKNOWN OPERATOR, NOT IN TABLE ON ISO-SPEC p.13 ?
  :
    (
      t = term700
    { term = t; }
    | '\\+' t = term900
    { jpl.Term[] args = { t.getTerm() };
      term = new PrologTerm(new jpl.Compound("\\+", args), t.getSourceInfo()); }
    )
  ;

term1000 returns [PrologTerm term]
  : 
      t1 = term900
      (',' t2 = term1000)?
    { if (t2 == null) {
         term = t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(",", args), t1.getSourceInfo());
      }
    }
  ;

term1050 returns [PrologTerm term]
  : 
      t1 = term1000
    (
      op=('*->' | '->')
      t2 = term1050
    )?
    { if (t2==null) {
         term=t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(op.getText(), args), t1.getSourceInfo());
      }
    }
  ;

term1100 returns [PrologTerm term]
  : 
      t1 = term1050
      (';' t2 = term1100)?
    { if (t2==null) {
         term = t1;
      } else { 
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(";", args), t1.getSourceInfo());
      }
    }
  ;

term1105 returns [PrologTerm term] 
  : 
      t1 = term1100
      ('|' t2 = term1105)?
    { if (t2==null) {
         term=t1;
      } else { 
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound("|", args), t1.getSourceInfo());
      }
    }
  ; 
  
term1200 returns [PrologTerm term]
  : 
    (
      t1 = term1105
      ( ( op=':-' | op='-->') t2 = term1105)?
    { if (t2==null) {
         term=t1;
      } else {
         jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
         term = new PrologTerm(new jpl.Compound(op.getText(), args), t1.getSourceInfo());
      }
    }
    | '?-' t = term1105
    { jpl.Term[] args = { t.getTerm() };
      term = new PrologTerm(new jpl.Compound("?-", args), t.getSourceInfo()); }
    )
  ;


/*---------------------------------------------------------------------
 * LEXER RULES
 *---------------------------------------------------------------------*/

/*  We have a NUMBER token, while the ISO spec has a FLOAT and a NUMBER token.
  However antlr can not determine properly whether a DOT is a decimal dot or a float number dot if
  we follow the ISO.
  If necessary, we will in post processing determine whether the number is float or integer. 
*/ 
NUMBER  // 6.4.4. + 6.4.5
    // We follow the ISO convention: you must use the dot and at least a digit if you want to use E notation.
  :   // HACK to get around ANTLR issues. 
      // ANTLR will incorrectly determine for itself whether REST_OF_FLOAT is following
      // if we would write "REST_OF_FLOAT?", using only the "." as the trigger.
      // the break will stop the parser right in its tracks and return the INTEGERCONSTANT parsed so far  
      // We still need to put REST_OF_FLOAT optional, otherwise a plain "0" will not parse, because
      // the ANT predictor predicts that the first rule will *not* fit (because there is NO '.')
      // and it will try only the 0x 0b etc cases which of course fail also.    
    INTEGERCONSTANT { if (input.LA(1)!='.' || input.LA(2)<'0' || input.LA(2)>'9') break; } REST_OF_FLOAT?  // accept either FLOAT or INTEGER
  | BINARYCONSTANT
  | OCTALCONSTANT
  | HEXADECIMALCONSTANT
  ;

fragment REST_OF_FLOAT
  :  '.' INTEGERCONSTANT EXPONENT?
  ;

fragment EXPONENT// 6.4.5
  :   ('e' | 'E') ('+' | '-')? INTEGERCONSTANT
  ;

NAME  // 6.4.2 (graphic tokens, semicolon token nor quoted tokens (of the form '...') have been included; see also below).
  : ('a'..'z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*  // 6.4.2 (identifier)
  | '!'           // 6.4.2 (cut token)
  ;

VARIABLE// 6.4.3
  : ('A'..'Z' | '_') (ALPHACHAR | DIGIT |'_')*
  ;


fragment INTEGERCONSTANT  // 6.4.4
  :     (DIGIT)+
  ;
  
fragment BINARYCONSTANT   // 6.4.4
  : '0b' ('0' | '1')+
  ;

fragment OCTALCONSTANT    // 6.4.4
  : '0o' ('0'..'7')+
  ;

fragment HEXADECIMALCONSTANT  // 6.4.4
  : '0x' ('0'..'9' | 'a'..'f' | 'A'..'F')+
  ;

fragment CHAR // 6.5. many characters but not quotes. 
  : ALPHACHAR | DIGIT | '_'   // alpha numeric char
  | '#' | '$' | '&' | '*' | '+' | '-' | '.' | '/' | ':' | '<' | '=' | '>' | '?' | '@' | '^' | '~' // graphic char
  | '!' | '(' | ')' | ',' | ';' | '[' | ']' | '{' | '}' | '|' | '%' // solo char
  |  WHITESPACECHAR // layout characters. See my comments at WHITESPACE.
  | '\\' // meta char; quote chars are explicitly handled in definition of string token
  | ESCAPE_SEQUENCE
  ;

fragment ALPHACHAR
  : 'A'..'Z' | 'a'..'z'
  ;

fragment DIGIT
  :     '0'..'9'
  ;
  
STRING  // Compare 6.4.2 (quoted char); in contrast with 6.5.4 new line chars are allowed in strings
  : '\'' (CHAR | '\'\'' | '"' | '`' )* '\'' // single quoted string
  | '"' (CHAR | '""' | '\'' | '`' )* '"'    // double quoted string
  | '`' (CHAR | '``' | '\'' | '"' )* '`'    // back quoted string
  ; 

fragment ESCAPE_SEQUENCE: '\\' (META_CHAR | SYMBOLIC_CONTROL_CHAR) ;  

fragment META_CHAR: '\\' | '\'' | '"' | '`'; // 6.5.5

fragment SYMBOLIC_CONTROL_CHAR: 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | 'x'; // 6.4.2.1


ENDTOKEN // Cf. 6.4.8: The end char '.' must be followed by a layout text; 
    // The footnote there also allows comments; we are more generous also allowing /* there¬
    // @modified Nick 27mar2011: to be able to import files, we now also allow the endtoken to be followed by the end of the file (EOF)
  : '.' ( WHITESPACECHAR | COMMENTCHARS | EOF );

// Layout text. Note that comments and whitespace are handled slightly differently than the Standard does.
// See also remark (1) in list of deviations below.
COMMENT   : COMMENTCHARS { $channel=HIDDEN; } ;
    // skip() means that COMMENT can be placed anywhere.
    // I think that's not conform the standard?
   
fragment COMMENTCHARS:  '%'(~('\n'|'\r'))*                // 6.4.1 (single line comment)
        | '/*' (options { greedy=false; } : .)* '*/'  // 6.4.1 (bracketed comment)
        ;

WHITESPACE  : WHITESPACECHAR+  { $channel=HIDDEN; }; // 6.4.1 (layout text)
    // because skip() is called, WHITESPACE never will appear as a token.
    // hence you can not refer to WHITESPACE in the parser

fragment WHITESPACECHAR: (' '|'\t'|'\f'|'\r'|'\n') ; // non ISO: see comments.

/* Deviations from the ISO/IEC 13211-1 International Standard for Prolog.
 1. White space and comments can be used more freely than the Standard allows.
  Since comments are "layout text" (6.4.1) and in the Standard layout text is used to resolve two
  ambiguities [(i) whether a dot is a graphic or end token; and (ii) whether an atom followed by an open
  token is a functor or an operator] these ambiguities have been resolved somewhat differently:
  - by explicitly defining an end token to include at least one white space character (though we
    would have preferred to drop this requirement, existing Prolog implementations would not accept
    Prolog texts accepted by this grammar)
  - by restricting name tokens: graphic tokens are not allowed as names; see also (2) below.

  @modified W.Pasman, 4june2010: We did not accept a % directly behind a ENDTOKEN. 
  We now also allow the /* directly behind an ENDTOKEN, in line with the more freely use of layout text
  mentioned above.
 2. Graphic tokens are not allowed as atoms (graphic chars are only allowed as part of strings).
  The Standard allows the use of graphic tokens such as '/*^' as names (which serve as identifiers 7.1.4,
  e.g., for predicates, functors, and constants). This leads to certain obvious issues that have to be
  settled more or less ad hoc, even in the Standard itself (e.g. by disallowing the use of ':-/1' as
  principal functor in a Prolog clause (6.2.1.2) or the use of '/*' at the beginning of a graphic token
  (since it is also used to indicate the start of a comment; cf. 6.4.2; see also (1) above). We also
  believe that the use of graphic tokens as names quickly leads to unreadable code.
 3. Operators are not allowed as atoms (they are allowed as part of strings).
  The standard allows operators as atoms (which then are terms with priority 1201; cf. 6.3.1.3). Among
  others this allows expressions such as 'f(:-, ;, [:-, :-|:-])' (cf. 6.3.3.1). Since atoms are supposed to
  serve as identifiers (7.1.4), remarks like those made above (2) regarding the liberal use of graphic
  characters apply to this case as well.
  Instead, in this grammar, operators with associated priorities have been handled explicitly ("hard coded")
  using the precedence design pattern discussed in Terence Parr, 2007, The Definitive ANTLR Reference, The
  Pragmatic Bookshelf, p. 61, 275.
  As a result, different from the Standard 6.3.4.4 note 4, it is not possible to redefine operator
  precedences by means of the operator 'op/3'.
 4. '[]' and '{}' are not allowed as atoms.
  In this grammar '[]' denotes the empty list which can be used as an argument in a compound term. '[]'
  cannot be used otherwise, and the 'empty list' is covered by the grammar rule for lists (see nonterminal
  'listterm').
  The Standard introduces a notational inconsistency, since it declares that whereever the phrase
  '{}(...)' is used one can equivalently use '{...}' but '[](...)' cannot be replaced equivalently with
  '[...]' (instead one has to use '.(...,...)' notation (cf. 6.3.3, 6.3.5 and 6.3.6).
  Note that '[]' nor '{}' can be treated as operators according to the Standard (6.3.4.3 note 3).
 5. Quoted tokens are not allowed as atoms (instead they are used to define a string token).
  The ISO Standard 6.4.2 states that if the (allowed) character sequence appearing within the quoted token
  is a valid atom without quotes "the quoted token shall denote that atom", i.e. can equivalently be
  replaced by the atom without quotes. E.g. "'abc'" and "abc" denote the same atom.
  Because of (2-5) this grammar does not explicitly introduce a non-terminal to denote atoms (6.3.1.3).
 6. Predefined unary operator ':-' (cf. ISO Standard 6.3.4.4).
  The unary operator ':-' is only allowed in a directive term (see nonterminal 'directive' above).
 7. After a head tail separator '|' a listterm is required.
  Different from the Standard 6.3.5 the grammar does not allow an arbitrary expression after a head tail
  separator but requires a listterm at that position instead. The Standard does not recognize a list as a
  type (cf. 7.1) but the restriction in the grammar on list tails makes sense and seems useful.
  The liberal approach which allows an arbitrary term after a head tail separator leads to issues which
  are not easily solved. In the Standard it appears that the head tail separator is only used as a syntactic
  device (cf. 7.10.6). In existing Prolog implementations, the built-in member function handles a "list" of
  the form '[a,b|c]' as the list '[a]', whereas the append function returns 'no' when attempting to append
  any list to '[a,b|c]' which suggests lists of the form '[a,b|c]' have limited use.
  Note that the setup used here facilitates identifying various basic types including integers, floats,
  strings, as well as lists. This allows for a limited implementation of type checking at compile time.
  TODO insert text about variables. 
 8. Character code lists have not been included in this grammar (cf. Standard 6.3.7).
 9. ANTLR precludes the use of (automatically) generated lexer tokens such as 'is', 'rem' and 'mod' as
  e.g. constant names. They are treated as 'reserved keywords' and cannot be (re)used as identifiers.
10. Strings have only limited support. Newlines are allowed within strings. No support for back quoted strings (`...`).
  escape sequence chars limited support, eg for \" \` \n \r etc.
  But no support for exotic number notations.
11. It is not clear from the ISO standard how to  parse and print curly bracket notation (eg {1,2,3}). 
  It is not even clear to me whether {} is a good term, I think it is not but SWI still accepts it as term. 

 * Additional remarks.
 - As noted by others, existing Prolog implementations do not completely conform to the ISO/IEC 13211-1 standard.
   See for a discussion of the standard e.g. Roberto Bagnara, Is the ISO Prolog Standard Taken Seriously?, in:
   The Association for Logic Programming Newsletter 12, 1 (February 1999), pp. 10-12.
   A few examples to illustrate some of the deviations in practice:
   - Existing Prolog implementations allow clauses such as 'p :- !, q' whereas the Standard requires brackets
     around the cut operator (i.e. only 'p :- (!), q' is valid; cf. 6.3.3 and [Bagnara, 1999] above).
   - SWI Prolog (http://www.swi-prolog.org/) allows compound terms with argument terms that have priority>=999
     (e.g. a term1200 can be an argument (the Standard only allows terms with priority<999; cf. 6.3.3.1).
   - The predefined operator table (Figure 9.2) in: Pierre Deransart, AbdelAli Ed-Dbali, Laurent Cervoni, 1996,
     Prolog: The Standard - Reference Manual, Springer-Verlag differs from the ISO Standard Table 5, p. 16.
   - Parsing priority issues in SWI Prolog parses. For instance -4**4 parses as (-4)**4. This is incorrect,
    because - is an operator of priority 200 and ** (which is xfx of prio 200) can only take operands of
     priority less than 200. The only correct way to parse -4**4 is as -(4**4) which would give -256.
   SWI will parse as expected if you write "- 4 ** 4" (with whitespace before 4).
   Also note that SWI will PRINT OUT (-4)**4 as -4**4 which does in turn NOT parse as (-4)**4 !!
  In TUProlog similar issues play but the details are different; it always incorrectly parses -4**4 as (-4)**4, 
  irrespective of spaces after the '-'. 
   - gprolog (http://www.gprolog.org/manual/html_node/gprolog037.html)
   seems to have a number of non-iso operators:
    ':' as a priority 600 xfy operator, 
    '+' priority 200 yfx 
    '\+' fy at priority 900.
*/
