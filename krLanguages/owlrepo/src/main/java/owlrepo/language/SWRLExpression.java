package owlrepo.language;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import owlrepo.parser.SWRLParserSourceInfo;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class SWRLExpression implements Expression {

	OWLObject expression; // when to be used?

	// possible values:
	OWLAxiom axiom;
	SWRLRule rule;
	SWRLAtom atom;
	SWRLArgument argument;
	int type = -1;

	OWLDataFactory df = new OWLDataFactoryImpl();

	public SWRLExpression(OWLAxiom axiom) {
		this.axiom = axiom;
		this.expression = axiom;
		this.type = 0;
	}

	public SWRLExpression(SWRLRule rule) {
		this.rule = rule;
		this.axiom = rule;
		this.expression = rule;
		this.type = 1;
	}

	public SWRLExpression(SWRLAtom atom) {
		Set<SWRLAtom> atoms = new HashSet<SWRLAtom>();
		atoms.add(atom);
		this.rule = df.getSWRLRule(atoms, null); // body,head
		this.axiom = rule;
		this.atom = atom;
		this.expression = atom;
		this.type = 2;
	}

	public SWRLExpression(SWRLArgument arg) {
		this.argument = arg;
		this.expression = arg;
		this.type = 3;
	}

	/**
	 * used only for creating a query out of an undefined term string that the
	 * parser could not parse (not in ontology) used only by
	 * OWLRepoKRInterface.getUndefined
	 * 
	 * @param string
	 */
	public SWRLExpression(String string) {
		this.argument = df.getSWRLLiteralArgument(df.getOWLLiteral(string));
		this.expression = argument;
		this.type = -1; // error
	}

	public String getSignature() {
		// System.out.println("get signature " + expression.toString());
		if (this.isArgument())
			return argument.getSignature().toString();
		else if (this.isTerm())
			return atom.getSignature().toString();
		else if (this.isRule())
			return rule.getSignature().toString();
		else
			return expression.toString().substring(0,
					expression.toString().indexOf("^^"));
	}

	public SWRLRule getRule() {
		return this.rule;
	}

	public OWLAxiom getAxiom() {
		return this.axiom;
	}

	public boolean isUndefined() {
		return (this.type == -1);
	}

	public boolean isTerm() {
		return (this.type == 2);
	}

	public boolean isArgument() {
		return (this.type == 3);
	}

	public boolean isVar() {
		return (this.argument != null && this.argument instanceof SWRLVariable);
	}

	public boolean isRule() {
		return (this.type == 1);
	}

	public boolean isIndividual() {
		if (this.isArgument() && !this.isVar())
			if (this.argument instanceof SWRLIArgument)
				return true;
		return false;
	}

	public boolean isConstant() {
		if (this.isArgument() && !this.isVar())
			if (this.argument instanceof SWRLDArgument)
				return true;
		return false;
	}

	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	public Set<Var> getFreeVar() {
		HashSet<Var> vars = new HashSet<Var>();
		if (this.isArgument()) {
			if (this.isVar()) {// variable
				vars.add(new SWRLVar((SWRLVariable) this.argument));
			} else
				return vars;
		} else if (this.isTerm()) {// atom
			for (SWRLArgument arg : this.atom.getAllArguments()) {
				Var fvar = getFreeVar(arg);
				if (fvar != null)
					vars.add(fvar);
			}
		} else if (this.isRule()) { // rule
			for (SWRLVariable v : rule.getVariables()) {
				vars.add(new SWRLVar(v));
			}
		}
		return vars;
	}

	private Var getFreeVar(SWRLArgument arg) {
		if (arg instanceof SWRLVariable)
			return new SWRLVar(((SWRLVariable) arg));
		return null;
	}

	@Override
	public Expression applySubst(Substitution substitution) {
		SWRLSubstitution subst = null;
		if (substitution instanceof SWRLSubstitution)
			subst = (SWRLSubstitution) substitution;
		// if it's a swrl substitution
		if (subst != null) {
			Set<SWRLVariable> substVars = subst.getSWRLVariables();
			// if substitution and current expression have both variables
			if (!substVars.isEmpty() && !this.getFreeVar().isEmpty()) {

				// check type and apply accordingly substitution
				if (this.isVar()) {// variable
					return new SWRLTerm(getArgSubst(this.argument, subst));

				} else if (this.isTerm()) { // term (atom - unary or binary =
					// class/ objectproperty/ dataproperty expression)
					return new SWRLTerm(getTermSubst(this.atom, subst));
				} else { // rule
					// body for sure contains something
					Set<SWRLAtom> newbody = getRuleSubst(this.rule.getBody(),
							subst);
					// process head if not empty
					Set<SWRLAtom> newhead = getRuleSubst(this.rule.getHead(),
							subst);
					return new SWRLQuery(df.getSWRLRule(newbody, newhead));
				}
			}
		}
		// if substitution is empty, return itself
		return this;
	}

	private SWRLArgument getArgSubst(SWRLArgument arg, SWRLSubstitution subst) {
		SWRLVariable thisvar = (SWRLVariable) arg;
		if (subst.getSWRLVariables().contains(thisvar))
			// create new term with the given argument
			return subst.getSWRLArgument(thisvar);
		return arg;
	}

	private SWRLAtom getTermSubst(SWRLAtom atom, SWRLSubstitution subst) {
		Collection<SWRLArgument> args = atom.getAllArguments();
		List<SWRLArgument> newArgs = new LinkedList<SWRLArgument>();
		SWRLPredicate predicate = atom.getPredicate();
		for (SWRLArgument arg : args) {
			// if variable
			if (arg instanceof SWRLVariable) {
				SWRLArgument newArg = getArgSubst(arg, subst);
				if (newArg != null) {
					// add it to the list of new arguments
					newArgs.add(newArg); // the substituted argument
				}
			} else
				// add it to the list of new arguments
				newArgs.add(arg); // the non-changing argument
		}

		// construct new term to be returned, because we cannot change the
		// existing one
		SWRLAtom newatom = null;
		if (atom instanceof SWRLClassAtom) // class
			newatom = df.getSWRLClassAtom((OWLClassExpression) predicate,
					(SWRLIArgument) newArgs.get(0));
		else if (atom instanceof SWRLDataPropertyAtom) // data property
			newatom = df.getSWRLDataPropertyAtom(
					(OWLDataPropertyExpression) predicate,
					(SWRLIArgument) newArgs.get(0),
					(SWRLDArgument) newArgs.get(1));
		else if (atom instanceof SWRLObjectPropertyAtom) // object property
			newatom = df.getSWRLObjectPropertyAtom(
					(OWLObjectPropertyExpression) predicate,
					(SWRLIArgument) newArgs.get(0),
					(SWRLIArgument) newArgs.get(1));
		// else if (this.atom instanceof SWRLBuiltInAtom) //builtin
		// newatom = df.getSWRLBuiltInAtom((IRI)predicate,
		// (List<SWRLDArgument>)newArgs);
		else if (atom instanceof SWRLDataRangeAtom) // data range
			newatom = df.getSWRLDataRangeAtom((OWLDataRange) predicate,
					(SWRLDArgument) newArgs.get(0));
		else if (atom instanceof SWRLSameIndividualAtom) // same individuals
			newatom = df.getSWRLSameIndividualAtom(
					(SWRLIArgument) newArgs.get(0),
					(SWRLIArgument) newArgs.get(1));
		else if (atom instanceof SWRLDifferentIndividualsAtom)
			// diff individuals
			newatom = df.getSWRLDifferentIndividualsAtom(
					(SWRLIArgument) newArgs.get(0),
					(SWRLIArgument) newArgs.get(1));

		if (newatom != null)
			return newatom;

		return atom;
	}

	private Set<SWRLAtom> getRuleSubst(Set<SWRLAtom> atoms,
			SWRLSubstitution subst) {
		Set<SWRLAtom> newset = new HashSet<SWRLAtom>();
		if (!atoms.isEmpty()) {
			// process the atoms
			for (SWRLAtom atom : atoms) {
				SWRLAtom newatom = getTermSubst(atom, subst);
				if (newatom != null)
					newset.add(newatom);
			}
		}
		return newset;
	}

	protected SWRLSubstitution mguVar(SWRLExpression exp) {
		SWRLArgument thisVar = this.argument;
		SWRLArgument otherArg = exp.argument;
		if (exp.isVar()) {
			// both variables
			return new SWRLSubstitution((SWRLVariable) thisVar,
					(SWRLVariable) otherArg);
		} else if (exp.isArgument()) {
			// variable to argument (literal)
			return new SWRLSubstitution((SWRLVariable) thisVar, otherArg);
		}
		return new SWRLSubstitution();
	}
	
	protected SWRLSubstitution mguTerm(SWRLExpression exp){
		SWRLSubstitution substitution = new SWRLSubstitution();

		// if both terms (atoms)
		SWRLAtom thisTerm = this.atom;
		SWRLAtom otherTerm = exp.atom;
		if (thisTerm.getPredicate().equals(otherTerm.getPredicate())) {
			// it is the same predicate
			Collection<SWRLArgument> thisArgs = thisTerm.getAllArguments();
			Collection<SWRLArgument> otherArgs =otherTerm.getAllArguments();
			if (thisArgs.size() == otherArgs.size()){
				//if they have the same number of arguments
				Iterator<SWRLArgument> it1 = thisArgs.iterator();
				Iterator<SWRLArgument> it2 = otherArgs.iterator();
				while (it1.hasNext() && it2.hasNext()){
					// extract the arguments from both terms (wrap it in terms,
					// as we cannot pass arguments here)
					SWRLTerm argThis = new SWRLTerm(it1.next());
					SWRLTerm argOther = new SWRLTerm(it2.next());
					// if the two arguments are not the same
					if (!argThis.equals(argOther)) {
						if (argThis.isVariable()) {
							// if the first argument is variable, get mguVar
							// with other
							// add the mgu of two swrl arguments
							substitution.addSWRLSubstitution(argThis
									.mguVar(argOther));
						} else {
							// if this argument is not variable, it is literal
							// argument
							// check if other argument is variable it is fine
							// if not, then other argument is equal literal
							// argument
							if (!argOther.isVariable()) {
								// if not equal literal, than terms are not
								// matching and break
								if (!argThis.equals(argOther))
									break;
							}

						}
					}
				}
			}
		}
		return substitution;

	}
	
	protected SWRLSubstitution mguRule(SWRLExpression exp) {
		SWRLSubstitution substitution = new SWRLSubstitution();

		// if both rules (atoms)
		SWRLRule thisRule = this.rule;

		// if other expression is a term (cannot be var or arg)
		if (exp.isTerm()) {
			SWRLAtom otherTerm = exp.atom;

		} else if (exp.isRule()) {

		}

		return substitution;
	}

	public Substitution mgu(Expression expression) {
		SWRLSubstitution substitution = new SWRLSubstitution();
		SWRLExpression exp = (SWRLExpression) expression;
		// if expressions are equal, return empty substitution
		if (!this.equals(expression)) {
			// otherwise treat by case
			if (this.isVar()) {
				// get mgu of var/var or var/arg (literal)
				substitution.addSWRLSubstitution(this.mguVar(exp));
				
			} else if (this.isTerm() && !this.getFreeVar().isEmpty()
					&& exp.isTerm()) {
				// both terms and this has variables
				// get mgu for arguments of first with second - use mguVar
				substitution.addSWRLSubstitution(this.mguTerm(exp));

			} else if (this.isRule() && !this.getFreeVar().isEmpty()) {
				// both rules and this has variables
				// get mgu for each term - argument - use mguTerm
				substitution.addSWRLSubstitution(this.mguRule(exp));
				
			} else if (this.isUndefined() || exp.isUndefined()) {
				// do nothing, return empty
			}
		}
		return substitution;

	}

	public Set<SWRLTerm> getTermSet() {
		Set<SWRLTerm> terms = new HashSet<SWRLTerm>();
		if (this.isArgument() || (this.isTerm()))
			terms.add((SWRLTerm) this);
		else {
			Set<SWRLAtom> atoms = rule.getBody();
			atoms.addAll(rule.getHead());
			for (SWRLAtom atom : atoms) {
				terms.add(new SWRLTerm(atom));
			}
		}
		return terms;
	}

	@Override
	public String toString() {
		if (this.isRule()) {
			SWRLTranslator trans = new SWRLTranslator();
			return trans.getRuleText(rule);
		}
		return expression.toString();
	}

	@Override
	public SourceInfo getSourceInfo() {
		// TODO Auto-generated method stub
		return new SWRLParserSourceInfo();
	}

	@Override
	public int hashCode() {
		switch (type) {
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
		if (this.type != other.type)
			return false;

		boolean equal = false;
		switch (type) {
		case 1:
			equal = this.rule.equals(other.rule);
			break;
		case 2:
			equal = this.atom.equals(other.atom);
			break;
		case 3:
			equal = this.argument.equals(other.argument);
			break;
		}
		return equal;
	}

}
