package jasonkri.language;

import jasonkri.JasonSourceInfo;
import krTools.language.Substitution;
import krTools.language.Term;

public class JasonTerm extends JasonExpression implements Term {

	public JasonTerm(jason.asSyntax.Term s, JasonSourceInfo i) {
		super(s, i);
	}

	@Override
	public Term applySubst(Substitution substitution) {
		return new JasonTerm(substitute(substitution), getJasonSourceInfo());
	}
}