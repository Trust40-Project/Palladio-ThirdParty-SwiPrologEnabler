package owlrepo.language;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataFactory;
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

	SWRLDataFactory df = new OWLDataFactoryImpl();

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

	public String getSignature() {
		return rule.getSignature().toArray().toString();
	}

	public SWRLRule getRule() {
		return this.rule;
	}

	public OWLAxiom getAxiom() {
		return this.axiom;
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
		} else { // rule
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

	public Substitution mgu(Expression expression) {
		SWRLExpression exp = (SWRLExpression) expression;
		if (this.isVar() && expression.isVar()) {
			if (!this.equals(expression))
				return new SWRLSubstitution((SWRLVariable) this.argument,
						(SWRLVariable) exp.argument);
		} else if (this.isTerm() && exp.isTerm()) {

		}
		return new SWRLSubstitution();

	}

	@Override
	public String toString() {
		if (rule != null) {
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
