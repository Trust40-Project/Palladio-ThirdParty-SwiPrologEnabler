package owlrepo.language;

import java.util.HashSet;
import java.util.Set;

import krTools.language.Var;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLVariable;

public class SWRLVar extends SWRLTerm implements Var {
	
	SWRLVariable var;
	OWLDataFactory df;
	
	public SWRLVar(SWRLVariable var){
		super(var);
		this.var = var;
	}
	
	public SWRLVariable getVar(){
		return this.var;
	}
	
	@Override
	public boolean isVar() {
		return true;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		return super.getFreeVar();
	}
	
	@Override
	public String toString(){
		return var.getIRI().toString();
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		// TODO Auto-generated method stub
		return this;
	}
	
}
