package jasonkri.language;

import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;
import jasonkri.Utils;

import java.util.ArrayList;
import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;

/**
 * A JasonUpdate contains either a {@link LogicalFormula} (the 'atom') or a
 * {@link LogExpr} (a conjunct, or negation). See also
 * {@link Utils#getConjuncts(Term)}) and {@link Utils#isUpdate(Term)}.
 * 
 * @author W.Pasman
 *
 */
public class JasonUpdate extends JasonExpression implements Update {

	// lazy cache.
	private List<DatabaseFormula> addList, deleteList;

	public JasonUpdate(Term s, SourceInfo i) {
		super(s, i);
		if (!isUpdate()) {
			throw new IllegalArgumentException("structure " + s
					+ " is not an update"); // bug? Or can this be user err?
		}
	}

	@Override
	public Update applySubst(Substitution substitution) {
		return new JasonUpdate(substitute(substitution), getSourceInfo());
	}

	@Override
	public List<DatabaseFormula> getAddList() {
		if (addList == null) {
			addList = new ArrayList<DatabaseFormula>();
			for (Term t : Utils.getConjuncts(getJasonTerm())) {
				if (!Utils.isNegation(t)) {
					addList.add(new JasonDatabaseFormula(t, getSourceInfo()));
				}
			}
		}
		return addList;
	}

	@Override
	public List<DatabaseFormula> getDeleteList() {

		if (deleteList == null) {
			deleteList = new ArrayList<DatabaseFormula>();
			for (Term t : Utils.getConjuncts(getJasonTerm())) {
				if (Utils.isNegation(t)) {
					deleteList.add(new JasonDatabaseFormula(((LogExpr) t)
							.getLHS(), getSourceInfo()));
				}
			}
		}
		return deleteList;
	}

	@Override
	public Query toQuery() {
		return new JasonQuery(getJasonTerm(), getSourceInfo());
	}

}
