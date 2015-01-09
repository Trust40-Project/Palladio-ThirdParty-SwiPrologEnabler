package owlrepo.language;

import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

import org.semanticweb.owlapi.model.SWRLRule;


public class SWRLUpdate extends SWRLExpression implements Update {

	public SWRLUpdate(SWRLRule axiom) {
		super(axiom);
	}

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	public Update applySubst(Substitution substitution){
		return null;
	}

	public List<DatabaseFormula> getAddList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<DatabaseFormula> getDeleteList() {
		// TODO Auto-generated method stub
		return null;
	}

	public Query toQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

}