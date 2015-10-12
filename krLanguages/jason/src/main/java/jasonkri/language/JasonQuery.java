package jasonkri.language;

import jason.asSyntax.Term;
import jasonkri.JasonSourceInfo;
import jasonkri.Utils;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

public class JasonQuery extends JasonExpression implements Query {

	public JasonQuery(Term s, JasonSourceInfo i) {
		super(s, i);
		if (!isQuery()) {
			throw new IllegalArgumentException("Structure " + s
					+ " is not a valid query"); // bug? Or can this be user err?
		}
	}

	@Override
	public Query applySubst(Substitution substitution) {
		return new JasonQuery(substitute(substitution), getJasonSourceInfo());
	}

	@Override
	public boolean isUpdate() {
		return Utils.isUpdate(getJasonTerm());
	}

	@Override
	public Update toUpdate() {
		return new JasonUpdate(getJasonTerm(), getJasonSourceInfo());
	}

}
