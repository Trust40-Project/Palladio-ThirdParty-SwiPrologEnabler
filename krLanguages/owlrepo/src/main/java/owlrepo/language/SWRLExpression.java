package owlrepo.language;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataFactory;
import org.semanticweb.owlapi.model.SWRLRule;

public class SWRLExpression implements Expression{

	OWLObject expression; //when to be used?
	
	//possible values:
	OWLAxiom axiom;
	SWRLRule rule;
	SWRLAtom atom;
	SWRLArgument argument;
	
	SWRLDataFactory df;
	
	public SWRLExpression(OWLAxiom axiom){
		this.axiom = axiom;
		this.expression = axiom;
	}
	
	public SWRLExpression(SWRLRule rule){
		this.rule = rule;
		this.axiom = rule;
		this.expression = rule;
	}
	
	public SWRLExpression(SWRLAtom atom){
		this.rule = df.getSWRLRule((Set<SWRLAtom>) atom, null); //body,head
		this.axiom = rule;
		this.atom = atom;
		this.expression = atom;
	}
	
	public SWRLExpression(SWRLArgument arg){
		this.argument = arg;
		this.expression = arg;
	}
	
	
	
	public String getSignature() {
		return rule.getSignature().toArray().toString();
	}
	
	public SWRLRule getRule(){
		return this.rule;
	}
	
	public OWLAxiom getAxiom(){
		return this.axiom;
	}
	

	public boolean isVar() {
		return false;
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<Var> getFreeVar() {
		// TODO Auto-generated method stub
		return null;
	}

	public Expression applySubst(Substitution substitution) {
		// TODO Auto-generated method stub
		return null;
	}


	public Substitution mgu(Expression expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(){
		if (rule!=null){
			SWRLTranslator trans = new SWRLTranslator();
			return trans.getRuleText(rule);
		}
		return expression.toString();
	}

	@Override
	public SourceInfo getSourceInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
