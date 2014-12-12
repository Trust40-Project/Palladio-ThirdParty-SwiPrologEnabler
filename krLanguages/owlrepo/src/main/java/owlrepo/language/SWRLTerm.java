package owlrepo.language;

import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

public class SWRLTerm extends SWRLExpression implements Term {
	
	SWRLAtom atom;
	SWRLArgument argument;
	
	public SWRLTerm(SWRLRule rule){
		super(rule);
		Set<SWRLAtom> body = super.getRule().getBody();
		//assume there's only one atom in the body
		this.atom = body.iterator().next();
		
	}
	public SWRLTerm(SWRLAtom atom){
		super(atom);
		this.atom = atom;
		this.argument = null;
		
	}
	
	public SWRLTerm(SWRLArgument arg){
		super(arg);
		this.argument = arg;
		this.atom = null;
	}
	
	
	public SWRLAtom getAtom(){
		return this.atom;
	}
	
	public SWRLArgument getArgument(){
		return this.argument;
	}

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables that are
	 * bound to a term in the substitution by that term (or, only renames in case the
	 * substitution binds a variable to another one).
	 */
	public Term applySubst(Substitution substitution){
		return null;
	}

	@Override 
	public String toString(){
		if (atom != null)
			return atom.toString();
		if (argument != null)
			return argument.toString();
		return this.toString();
	}
}
