package owlrepo.language;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.sqwrl.values.SQWRLEntityResultValue;
import org.swrlapi.sqwrl.values.SQWRLLiteralResultValue;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class SWRLSubstitution implements Substitution {
	HashMap<SWRLVariable, SWRLArgument> substitutions;
	Set<SWRLVariable> variables;
	OWLDataFactory df;

	public SWRLSubstitution() {
		substitutions = new HashMap<SWRLVariable, SWRLArgument>();
		variables = new HashSet<SWRLVariable>();
		df = new OWLDataFactoryImpl();
	}

	public SWRLSubstitution(Map<Var, Term> map) {
		this();
		if (map!=null){
		Set<Var> vars = map.keySet();
		Iterator<Var> it = vars.iterator();
		while (it.hasNext()) {
			Var v = it.next();
			SWRLVariable var = ((SWRLVar) v).getVar();
			SWRLArgument atom = ((SWRLTerm) map.get(v)).getArgument();
			substitutions.put(var, atom);
		}
		variables = substitutions.keySet();
		}
	}


	public SWRLSubstitution(SWRLVariable var, SWRLArgument value){
		this();
			variables.add(var);
		substitutions.put(var, value);
			
	}
			
	public SWRLSubstitution(SWRLVariable var, SQWRLResultValue resultvalue){
		this();
		try{
			variables.add(var);
		
			if (resultvalue.isLiteral()) {
				SQWRLLiteralResultValue literalvalue = resultvalue.asLiteralResult();
				OWLLiteral lit = literalvalue.getOWLLiteral();
				//boolean value = literalvalue.getBoolean();
				substitutions.put(var, df.getSWRLLiteralArgument(lit));
			} else if (resultvalue.isEntity()) {
				SQWRLEntityResultValue entityvalue = resultvalue.asEntityResult();
				IRI value = entityvalue.getIRI();
				substitutions.put(var, df.getSWRLIndividualArgument(df.getOWLNamedIndividual(value)));
			}
//			{
//				SQWRLClassResultValue classvalue = value.asClassResult();
//				classvalue.getIRI();
//			} else if (value.isDataProperty()){
//				SQWRLDataPropertyResultValue datavalue = value.asDataPropertyResult();
//				datavalue.getIRI();
//			} else if (value.isObjectProperty()){
//				SQWRLObjectPropertyResultValue objectvalue = value.asObjectPropertyResult();
//				objectvalue.getIRI();
//			} else  else if (value.isIndividual()){
//				SQWRLIndividualResultValue indivalue = value.asIndividualResult();
//				indivalue.getIRI()
//			} else if (value.isAnnotationProperty()) {
//				SQWRLAnnotationPropertyResultValue annotvalue = value.asAnnotationPropertyResult();
//				annotvalue.getIRI()
//			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public void addSWRLSubstitution(SWRLVariable var, SWRLArgument atom) {
		substitutions.put(var, atom);
		variables = substitutions.keySet();
	}
	
	public SWRLArgument getSWRLArgument(SWRLVariable var){
		return substitutions.get(var);
	}

	//@Override
	public Set<Var> getVariables() {
		HashSet<Var> newvars = new HashSet<Var>();
		Iterator<SWRLVariable> it = variables.iterator();
		while (it.hasNext()) {
			newvars.add(new SWRLVar(it.next()));
		}
		return newvars;
	}
	

	//@Override
	public Term get(Var var) {
		// check if needs an if and return null or does automatically
		SWRLVariable v = ((SWRLVar) var).getVar();
		SWRLArgument arg = substitutions.get(v);
		if (arg != null)
			return new SWRLTerm(arg);
		return null;
	}

	//@Override
	public void addBinding(Var var, Term term) {
		SWRLVar nvar = (SWRLVar) var;
		SWRLTerm nterm = (SWRLTerm) term;
		if (!substitutions.containsKey(nvar) && (nterm instanceof SWRLArgument))
			substitutions.put(nvar.getVar(), nterm.getArgument());
	}

//	@Override
	public Substitution combine(Substitution substitution) {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public boolean remove(Var var) {
		SWRLVariable nvar = ((SWRLVar) var).getVar();
		if (substitutions.containsKey(nvar)) {
			substitutions.remove(nvar);
			return true;
		}
		return false;
	}

//	@Override
	// Removes all bindings in this Substitution that do not bind any of the
	// given Vars.
	public boolean retainAll(Collection<Var> variables) {
		Set<Var> removevars = getVariables();
		removevars.removeAll(variables);
		if (removevars.isEmpty())
			return false;
		else
			variables.removeAll(removevars);

		Iterator<Var> iterator = removevars.iterator();
		while (iterator.hasNext()) {
			SWRLVar v = (SWRLVar) iterator.next();
			substitutions.remove(v.getVar());
		}
		return true;
	}

	@Override
	public Substitution clone() {
		return this;
	}

}
