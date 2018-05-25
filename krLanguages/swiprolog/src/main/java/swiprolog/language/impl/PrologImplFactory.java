package swiprolog.language.impl;

import krTools.language.Term;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologUpdate;
import swiprolog.language.PrologVar;

public class PrologImplFactory {
	private PrologImplFactory() {

	}

	public static PrologCompound getAtom(String name, SourceInfo info) {
		return new PrologAtomImpl(name, info);
	}

	public static PrologCompound getCompound(String name, Term[] args, SourceInfo info) {
		return new PrologCompoundImpl(name, args, info);
	}

	public static PrologDBFormula getDBFormula(PrologCompound compound) {
		return new PrologDBFormulaImpl(compound);
	}

	public static PrologTerm getNumber(double value, SourceInfo info) {
		return new PrologFloatImpl(value, info);
	}

	public static PrologTerm getNumber(long value, SourceInfo info) {
		return new PrologIntImpl(value, info);
	}

	public static PrologTerm getNumber(int value, SourceInfo info) {
		return new PrologIntImpl(value, info);
	}

	public static PrologQuery getQuery(PrologCompound compound) {
		return new PrologQueryImpl(compound);
	}

	public static PrologUpdate getUpdate(PrologCompound compound) {
		return new PrologUpdateImpl(compound);
	}

	public static PrologVar getVar(String name, SourceInfo info) {
		return new PrologVarImpl(name, info);
	}
}
