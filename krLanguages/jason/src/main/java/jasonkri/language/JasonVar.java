package jasonkri.language;

import jason.asSyntax.VarTerm;
import jasonkri.JasonSourceInfo;

import java.util.Set;

import krTools.language.Var;

public class JasonVar extends JasonTerm implements Var {

	public JasonVar(VarTerm var, JasonSourceInfo info) {
		super(var, info);
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		String name = getName();
		JasonSourceInfo info = getJasonSourceInfo();

		int n = 1;
		Var newVar;
		do {
			newVar = new JasonVar(new VarTerm(name + "_" + n), info);
			n++;
		} while (usedNames.contains(newVar));

		return newVar;
	}

	@Override
	public boolean isVar() {
		return true;
	}
}
