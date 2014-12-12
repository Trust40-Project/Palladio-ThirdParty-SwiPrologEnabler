package owlrepo.language;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class SWRLDatabaseFormula extends SWRLExpression implements DatabaseFormula {


	public SWRLDatabaseFormula(OWLAxiom axiom){
		super(axiom);
	}
	
	public SWRLDatabaseFormula(SWRLRule axiom) {
		super(axiom);
	}

	
	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	public DatabaseFormula applySubst(Substitution substitution){
		return null;
	}


	//@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}


	//@Override
	public Query toQuery() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
