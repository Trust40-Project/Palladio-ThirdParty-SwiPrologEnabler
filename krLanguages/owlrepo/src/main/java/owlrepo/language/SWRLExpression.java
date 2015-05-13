package owlrepo.language;

import java.util.HashSet;
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
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.core.SWRLAPIFactory;

import owlrepo.parser.SQWRLParserSourceInfo;
import uk.ac.manchester.cs.owl.owlapi.SWRLVariableImpl;

public class SWRLExpression implements Expression{

	OWLObject expression; //when to be used?
	
	//possible values:
	OWLAxiom axiom;
	SWRLRule rule;
	SWRLAtom atom;
	SWRLArgument argument;
	int type = -1;
	
	SWRLDataFactory df;
	
	public SWRLExpression(OWLAxiom axiom){
		this.axiom = axiom;
		this.expression = axiom;
		this.type = 0;
	}
	
	public SWRLExpression(SWRLRule rule){
		this.rule = rule;
		this.axiom = rule;
		this.expression = rule;
		this.type = 1;
	}
	
	public SWRLExpression(SWRLAtom atom){
		this.rule = df.getSWRLRule((Set<SWRLAtom>) atom, null); //body,head
		this.axiom = rule;
		this.atom = atom;
		this.expression = atom;
		this.type = 2;
	}
	
	public SWRLExpression(SWRLArgument arg){
		this.argument = arg;
		this.expression = arg;
		this.type = 3;
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
	private SWRLVar getSWRLVar(){
			return new SWRLVar((SWRLVariable)this.argument);
	}
	

	public boolean isTerm(){
		return (this.type==2);
	}
	
	public boolean isVar() {
		return (this.argument!=null && this.argument instanceof SWRLVariable);
	}

	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	public Set<Var> getFreeVar() {
		HashSet<Var> vars = new HashSet<Var>();
		if (this.type==3){//argument
			vars.add(getFreeVar(this.argument));			
		}else if (this.type == 2 ){//atom
			for (SWRLArgument arg: this.atom.getAllArguments()){
				vars.add(getFreeVar(arg));
			}
		}else { //rule
			for (SWRLVariable v: rule.getVariables()){
				vars.add(new SWRLVar(v));
			}
		}
		return vars;
	}
	
	private Var getFreeVar(SWRLArgument arg){
		if (arg instanceof SWRLVariable)
			return new SWRLVar(((SWRLVariable)arg));
		return null;
	}

	public Expression applySubst(Substitution substitution) {
		Set<Var> substVars = substitution.getVariables();
		if (this.isVar() && substVars.contains(this.getSWRLVar())){
			return (SWRLTerm)substitution.get(this.getSWRLVar());
		}
		else if (this.isTerm()){
			for (SWRLArgument arg: this.atom.getAllArguments()){
				SWRLExpression exp = new SWRLExpression(arg);
				if (exp.isVar() && substVars.contains(exp)){
					
				}
			}
		}else {
			Iterator<SWRLVariable> vars = this.rule.getVariables().iterator();
			while (vars.hasNext()){
				if (substVars.contains(vars.next())){
					
				}
			}
		}
		return null;
	}


	public Substitution mgu(Expression expression) {
		SWRLExpression exp = (SWRLExpression)expression;
		if (this.isVar() && expression.isVar()){
			if (!this.equals(expression))
				return new SWRLSubstitution((SWRLVariable)this.argument,(SWRLVariable)exp.argument);
		} else if (this.isTerm() && exp.isTerm()){
			
		}
		return new SWRLSubstitution();

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
		return new SQWRLParserSourceInfo();
	}
	
	@Override
	public int hashCode() {
		switch(type){
		case 1:
			 return rule.hashCode();
		case 2: 
			return atom.hashCode();
		case 3:
			return argument.hashCode();
		}
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SWRLExpression other = (SWRLExpression) obj;
		if (this.type!=other.type)
			return false;
		
		boolean equal = false;
		switch(type){
		case 1:
			 equal = this.rule.equals(other.rule);
		case 2: 
			equal =  this.atom.equals(other.atom);
		case 3:
			equal =  this.argument.equals(other.argument);
		}
		return equal;
	}
	
}
