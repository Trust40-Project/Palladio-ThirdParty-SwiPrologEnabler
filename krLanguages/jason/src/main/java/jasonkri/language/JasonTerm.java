package jasonkri.language;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;

public class JasonTerm extends JasonExpression implements Term {

	public JasonTerm(jason.asSyntax.Term s, SourceInfo i) {
		super(s, i);
	}

	@Override
	public Term applySubst(Substitution substitution) {
		return new JasonTerm(substitute(substitution), getSourceInfo());
	}
}