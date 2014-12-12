package owlrepo.language;

import java.util.HashSet;
import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;

import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.sqwrl.SQWRLQuery;

import uk.ac.manchester.cs.owl.owlapi.SWRLRuleImpl;

//import org.openrdf.query.Query;


public class SWRLQuery extends SWRLExpression implements Query {

	private String queryName;
	
	public SWRLQuery(SQWRLQuery query){
		super(new SWRLRuleImpl(new HashSet<SWRLAtom>(query.getBodyAtoms()), 
							   new HashSet<SWRLAtom>(query.getHeadAtoms())));
		this.queryName = query.getQueryName();
	}
	
	public SWRLQuery(SWRLRule query){
		super(query);
	}
	
	public String getQueryName(){
		return this.queryName;
	}
	
	
	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	public Query applySubst(Substitution substitution){
		Set<Var> vars = substitution.getVariables();
		for (SWRLAtom atom: this.rule.getBody()){
		//	if (atom contains var)
			//replace atom where var is replaced with term of substituion
		}
		
		return null;
	}

	/**
	 * Converts a {@link SWRLQuery} to an {@link SWRLUpdate}.
	 * 
	 * TODO:
	 * <p>All Mental Literals contain Queries. However goals are represented by
	 * Updates. In order to convert a mental literal into a goal, as is
	 * necessary when instantiating a module, a way to convert a Query into an
	 * Update is needed.</p>
	 * 
	 * @return An Update with an empty delete list and an add list with the
	 *         content of this {@link SWRLQuery}.
	 */
	public Update toUpdate(){
		return null;
	}



	//@Override
	public boolean isUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

}
