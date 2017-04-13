/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package swiprolog.visitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import krTools.exceptions.ParserException;
import krTools.parser.SourceInfo;
import swiprolog.errors.ParserErrorMessages;
import swiprolog.language.JPLUtils;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;
import swiprolog.parser.Prolog4Parser;
import swiprolog.parser.Prolog4Parser.ArglistContext;
import swiprolog.parser.Prolog4Parser.ClauseContext;
import swiprolog.parser.Prolog4Parser.DirectiveContext;
import swiprolog.parser.Prolog4Parser.DirectiveorclauseContext;
import swiprolog.parser.Prolog4Parser.ExpressionContext;
import swiprolog.parser.Prolog4Parser.ItemsContext;
import swiprolog.parser.Prolog4Parser.ListtermContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import swiprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import swiprolog.parser.Prolog4Parser.PrefixoperatorContext;
import swiprolog.parser.Prolog4Parser.PrologfileContext;
import swiprolog.parser.Prolog4Parser.PrologtextContext;
import swiprolog.parser.Prolog4Parser.Term0Context;
import swiprolog.parser.Prolog4Parser.Term1000Context;
import swiprolog.parser.Prolog4Parser.Term100Context;
import swiprolog.parser.Prolog4Parser.Term1050Context;
import swiprolog.parser.Prolog4Parser.Term1100Context;
import swiprolog.parser.Prolog4Parser.Term1105Context;
import swiprolog.parser.Prolog4Parser.Term1150Context;
import swiprolog.parser.Prolog4Parser.Term1200Context;
import swiprolog.parser.Prolog4Parser.Term200Context;
import swiprolog.parser.Prolog4Parser.Term400Context;
import swiprolog.parser.Prolog4Parser.Term400bContext;
import swiprolog.parser.Prolog4Parser.Term500Context;
import swiprolog.parser.Prolog4Parser.Term500bContext;
import swiprolog.parser.Prolog4Parser.Term50Context;
import swiprolog.parser.Prolog4Parser.Term700Context;
import swiprolog.parser.Prolog4Parser.Term900Context;
import swiprolog.parser.Prolog4ParserBaseVisitor;
import swiprolog.parser.SourceInfoObject;

/**
 * Implements the basic antlr {@link Prolog4ParserBaseVisitor} interface and
 * creates the proper objects from the parsed tree. This returns
 * {@link PrologTerm}s but they are not yet validated. This is for internal use
 * only, as you normally need an error listener. See also {@link Visitor4}.<br>
 */
public class Visitor4Internal extends Prolog4ParserBaseVisitor<Object> {
	private final SourceInfo source;
	private final List<ParserException> errors = new LinkedList<>();

	/**
	 * @param source
	 */
	public Visitor4Internal(SourceInfo source) {
		this.source = source;
	}

	/**
	 * Create {@link SourceInfoObject} for given context.
	 *
	 * @param ctx
	 *            the {@link DirectiveContext} from the parsed object
	 * @return {@link SourceInfoObject}
	 */
	private SourceInfo getSourceInfo(ParserRuleContext ctx) {
		Token start = (ctx == null) ? null : ctx.getStart();
		Token stop = (ctx == null) ? null : ctx.getStop();
		if (stop == null) {
			// happens if we are at EOF...
			stop = start;
		}
		return (start == null) ? null
				: new SourceInfoObject(this.source.getSource(), start.getLine(), start.getCharPositionInLine(),
						this.source.getStartIndex() + start.getStartIndex() + 1,
						this.source.getStartIndex() + stop.getStopIndex() + 1);
	}

	private SourceInfo getSourceInfo(TerminalNode leaf) {
		Token symbol = (leaf == null) ? null : leaf.getSymbol();
		return (symbol == null) ? null
				: new SourceInfoObject(this.source.getSource(), symbol.getLine(), symbol.getCharPositionInLine(),
						this.source.getStartIndex() + symbol.getStartIndex() + 1,
						this.source.getStartIndex() + symbol.getStopIndex() + 1);
	}

	/**
	 * create new {@link org.jpl7.Compound} using functor name, args and
	 * {@link ParserRuleContext}
	 *
	 * @param name
	 *            the functor name
	 * @param args
	 *            the arguments for the compound
	 * @param ctx
	 *            the {@link ParserRuleContext}. Used to create the
	 *            {@link SourceInfo}.
	 * @return
	 */
	private PrologTerm compound(String name, org.jpl7.Term[] args, ParserRuleContext ctx) {
		return compound(name, args, getSourceInfo(ctx));
	}

	/**
	 * create new {@link org.jpl7.Compound} using functor name, args and
	 * {@link SourceInfo}
	 *
	 * @param name
	 *            the functor name
	 * @param args
	 *            the arguments for the compound
	 * @param ctx
	 *            the {@link SourceInfo}
	 * @return new compound
	 */
	private PrologTerm compound(String name, org.jpl7.Term[] args, SourceInfo info) {
		return new PrologTerm(new org.jpl7.Compound(name, args), info);
	}

	/**
	 * craete new {@link org.jpl7.Atom} with given name
	 *
	 * @param name
	 *            of new atom
	 * @param ctx
	 *            the {@link ParserRuleContext}
	 * @return new atom
	 */
	private PrologTerm atom(String name, ParserRuleContext ctx) {
		return new PrologTerm(new org.jpl7.Atom(name), getSourceInfo(ctx));
	}

	/**
	 * Unquote a quoted string. The enclosing quotes determine how quotes inside
	 * the string are handled.
	 */
	private String unquote(String quotedstring) {
		char quote = quotedstring.charAt(0);
		String unquoted = quotedstring.substring(1, quotedstring.length() - 1);
		// CHECK SWI does first replaceQuotes, then escape. Try '\''''. Is that
		// ISO?
		return unescape(replaceQuotes(unquoted, quote));
	}

	/**
	 * Double quotes in quoted string indicate just that quote one time. eg,
	 * """" means '"'.
	 */
	private String replaceQuotes(String string, char quote) {
		return string.replaceAll("" + quote + quote, "" + quote);
	}

	/**
	 * Unescape string according to ISO standard. Not implemented #2917
	 */
	private String unescape(String string) {
		return string;
	}

	/*****************************************************************/

	/************ implements Prolog4ParserBaseVisitor ****************/
	/*****************************************************************/
	@Override
	public List<PrologTerm> visitPrologfile(PrologfileContext ctx) {
		return visitPrologtext(ctx.prologtext());
	}

	@Override
	public List<PrologTerm> visitPrologtext(PrologtextContext ctx) {
		List<PrologTerm> clauses = new ArrayList<>(ctx.directiveorclause().size());
		for (DirectiveorclauseContext d : ctx.directiveorclause()) {
			clauses.add(visitDirectiveorclause(d));
		}
		return clauses;
	}

	@Override
	public PrologTerm visitDirectiveorclause(Prolog4Parser.DirectiveorclauseContext ctx) {
		if (ctx.directive() != null) {
			return visitDirective(ctx.directive());
		} else { // ctx.clause() // CHECK null?
			return visitClause(ctx.clause());
		}
	}

	@Override
	public PrologTerm visitDirective(DirectiveContext ctx) {
		PrologTerm t = visitTerm1200(ctx.term1200());
		org.jpl7.Term[] args = { t.getTerm() };
		return compound(":-", args, ctx);
	}

	@Override
	public PrologTerm visitClause(ClauseContext ctx) {
		return visitTerm1200(ctx.term1200());
	}

	@Override
	public List<PrologTerm> visitArglist(ArglistContext ctx) {
		List<PrologTerm> arglist = new LinkedList<>();
		arglist.add(visitExpression(ctx.expression()));
		if (ctx.arglist() != null) {
			// we DO have a comma and more arguments
			arglist.addAll(visitArglist(ctx.arglist()));
		}
		return arglist;
	}

	@Override
	public PrologTerm visitPossiblyEmptyConjunct(PossiblyEmptyConjunctContext ctx) {
		if (ctx.term1000() != null) {
			return visitTerm1000(ctx.term1000());
		} else {
			return atom("true", ctx);
		}
	}

	@Override
	public PrologTerm visitPossiblyEmptyDisjunct(PossiblyEmptyDisjunctContext ctx) {
		if (ctx.term1100() != null) {
			return visitTerm1100(ctx.term1100());
		} else {
			return atom("true", ctx);
		}
	}

	@Override
	public PrologTerm visitExpression(ExpressionContext ctx) {
		return visitTerm900(ctx.term900());
	}

	@Override
	public PrologTerm visitListterm(ListtermContext ctx) {
		if (ctx.items() != null) {
			return visitItems(ctx.items());
		} else {
			return atom("[]", ctx);
		}
	}

	@Override
	public PrologTerm visitItems(ItemsContext ctx) {
		// 6.3.5 ; we use the prolog "." functor to build items list.
		PrologTerm head = visitExpression(ctx.expression());

		PrologTerm tail = null;
		if (ctx.items() != null) {
			tail = visitItems(ctx.items());
		} else if (ctx.listterm() != null) {
			tail = visitListterm(ctx.listterm());
		} else if (ctx.VARIABLE() != null) {
			tail = new PrologVar(new org.jpl7.Variable(ctx.VARIABLE().getText()), getSourceInfo(ctx.VARIABLE()));
		}

		if (tail == null) {
			org.jpl7.Term[] args1 = { head.getTerm(), new org.jpl7.Atom("[]") };
			return compound(".", args1, ctx);
		} else {
			org.jpl7.Term[] args2 = { head.getTerm(), tail.getTerm() };
			return compound(".", args2, ctx);
		}
	}

	@Override
	public PrologTerm visitPrefixoperator(PrefixoperatorContext ctx) {
		org.jpl7.Term[] args = { visitExpression(ctx.expression(0)).getTerm(),
				visitExpression(ctx.expression(1)).getTerm() };
		return compound(ctx.prefixop().getText(), args, ctx);
	}

	/**
	 * Parse number as term.
	 *
	 * @param num
	 *            number to parse
	 * @param info
	 *            the {@link SourceInfo}
	 * @return the parsed term. If failure, this returns a term '1' and reports
	 *         the error in the {@link #errors} list.
	 */
	private PrologTerm parseNumber(String num, SourceInfo info) {
		if (num.matches("[0-9]+") || num.matches("0[box].*")) {
			// integer string
			try {
				Long val = Long.valueOf(num);
				// int, octal, hex, etc.
				return new PrologTerm(JPLUtils.createIntegerNumber(val), info);
			} catch (NumberFormatException e) {
				System.out.println(ParserErrorMessages.NUMBER_TOO_LARGE_CONVERTING.toReadableString(num));
			}
		}
		// float
		try {
			Double val = Double.valueOf(num);
			if (val.isNaN()) {
				throw new NumberFormatException(ParserErrorMessages.NUMBER_NAN.toReadableString(num));
			}
			if (val.isInfinite()) {
				throw new NumberFormatException(ParserErrorMessages.NUMBER_INFINITY.toReadableString(num));
			}
			return new PrologTerm(new org.jpl7.Float(val), info);
		} catch (NumberFormatException e) {
			this.errors.add(new ParserException(
					ParserErrorMessages.NUMBER_NOT_PARSED.toReadableString() + ":" + e.getMessage(), info));
		}
		// never return null as others may post process our output.
		return new PrologTerm(new org.jpl7.Integer(1), info);
	}

	@Override
	public PrologTerm visitTerm0(Term0Context ctx) {
		if (ctx.NUMBER() != null) {
			return parseNumber(ctx.NUMBER().getText(), getSourceInfo(ctx));
		} else if (ctx.NAME() != null) {
			String name = ctx.NAME().getText();
			ArglistContext args = ctx.arglist();
			if (args == null) {
				return atom(name, ctx);
			} else {
				List<PrologTerm> a = visitArglist(args);
				// functor with arguments
				List<org.jpl7.Term> terms = new ArrayList<>(a.size());
				for (PrologTerm pterm : a) {
					terms.add(pterm.getTerm());
				}
				return compound(name, terms.toArray(new org.jpl7.Term[0]), ctx);
			}
		} else if (ctx.VARIABLE() != null) {
			return new PrologVar(new org.jpl7.Variable(ctx.VARIABLE().getText()), getSourceInfo(ctx));
		} else if (ctx.STRING() != null) {
			return atom(unquote(ctx.STRING().getText()), ctx);
		} else if (ctx.LBR() != null || ctx.CLBR() != null) {
			return visitTerm1200(ctx.term1200());
		} else if (ctx.listterm() != null) {
			return visitListterm(ctx.listterm());
		} else if (ctx.prefixoperator() != null) {
			return visitPrefixoperator(ctx.prefixoperator());
		} else {
			return null; // should never be reached
		}
	}

	@Override
	public PrologTerm visitTerm50(Term50Context ctx) {
		PrologTerm t1 = visitTerm0(ctx.term0(0));
		if (ctx.term0(1) == null) {
			return t1;
		} else {
			PrologTerm t2 = visitTerm0(ctx.term0(1));
			org.jpl7.Term[] args = { t1.getTerm(), t2.getTerm() };
			return compound(":", args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm100(Term100Context ctx) {
		PrologTerm t1 = visitTerm50(ctx.term50(0));
		if (ctx.term50(1) == null) {
			return t1;
		} else {
			PrologTerm t2 = visitTerm50(ctx.term50(1));
			org.jpl7.Term[] args = { t1.getTerm(), t2.getTerm() };
			return compound("@", args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm200(Term200Context ctx) {
		String op = null;
		SourceInfo info = getSourceInfo(ctx);
		/**
		 * (op = '-' | op= '\\' ) term200 <br>
		 * | term100 ( (op= '^' term200) | (op='**' term100) )?
		 */
		if (ctx.op != null) {
			op = ctx.op.getText();
		}
		PrologTerm term;

		if ("-".equals(op) || "\\".equals(op)) {
			// (op = '-' | op= '\\' ) term200
			org.jpl7.Term t = visitTerm200(ctx.term200()).getTerm();
			org.jpl7.Term[] args = { t };
			term = compound(op, args, ctx);
			if (op.equals("-")) {
				// minus sign, check special case of numeric constant. See ISO
				// 6.3.1.2 footnote
				// Note, we interpret this footnote RECURSIVELY, eg --1 == 1.
				// Note that this notation is not SWI prolog compatible, SWI
				// seems to fail ISO
				// compliance here.
				if (t.isFloat()) {
					term = new PrologTerm(new org.jpl7.Float(-1 * t.floatValue()), info);
				} else if (t.isInteger()) {
					term = new PrologTerm(new org.jpl7.Integer(-1 * t.intValue()), info);
				}
			}
		} else {
			// term100 ( (op= '^' term200) | (op= '**' term100) )?
			org.jpl7.Term t1 = visitTerm100(ctx.term100(0)).getTerm();
			if (op == null) { // only term100.
				term = new PrologTerm(t1, info);
			} else {
				org.jpl7.Term t2;
				if ("^".equals(op)) {
					t2 = visitTerm200(ctx.term200()).getTerm();
				} else {
					t2 = visitTerm100(ctx.term100(1)).getTerm();
				}
				org.jpl7.Term[] args = { t1, t2 };
				term = compound(op, args, ctx);
			}
		}
		return term;
	}

	@Override
	public PrologTerm visitTerm400(Term400Context ctx) {
		/*
		 * term200 ( ('*' | '/' | '//' | 'rem' | 'mod' | 'rdiv' | '<<' | '>>')
		 * term200 )*
		 */
		PrologTerm term = visitTerm200(ctx.term200());
		for (Term400bContext t : ctx.term400b()) {
			org.jpl7.Term t1 = visitTerm400b(t);
			org.jpl7.Term[] args = { term.getTerm(), t1 };
			term = compound(t.op.getText(), args, ctx);
		}
		return term;
	}

	@Override
	public org.jpl7.Term visitTerm400b(Term400bContext ctx) {
		return visitTerm200(ctx.term200()).getTerm();
	}

	@Override
	public PrologTerm visitTerm500(Term500Context ctx) {
		/*
		 * term400 term500b*
		 */
		PrologTerm term = visitTerm400(ctx.term400());
		for (Term500bContext t : ctx.term500b()) {
			org.jpl7.Term t1 = visitTerm500b(t);
			org.jpl7.Term[] args = { term.getTerm(), t1 };
			term = compound(t.op.getText(), args, ctx);
		}
		return term;
	}

	@Override
	public org.jpl7.Term visitTerm500b(Term500bContext ctx) {
		return visitTerm400(ctx.term400()).getTerm();
	}

	@Override
	public PrologTerm visitTerm700(Term700Context ctx) {
		/**
		 * term500 ( ( '=' | '\\=' | '==' | '\\==' | '@<' | '@=<' | '@>' | '@>='
		 * | '=@='| '=..' | 'is' | '=:=' | '=\\=' | '<' | '=<' | '>' | '>=')
		 * term500 )?
		 */
		PrologTerm lhs = visitTerm500(ctx.term500(0));
		if (ctx.term500().size() == 1) {
			// only term500
			return lhs;
		} else {
			// we DO have the optional RHS term. Make a compound.
			PrologTerm rhs = visitTerm500(ctx.term500(1));
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm900(Term900Context ctx) {
		/**
		 * : term700 | '\\+' term900
		 */
		if (ctx.term700() != null) {
			return visitTerm700(ctx.term700());
		} else {
			org.jpl7.Term[] args = { visitTerm900(ctx.term900()).getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm1000(Term1000Context ctx) {
		/**
		 * : term900 (',' term1000)?
		 */
		PrologTerm lhs = visitTerm900(ctx.term900());
		if (ctx.term1000() == null) {
			return lhs;
		} else {
			PrologTerm rhs = visitTerm1000(ctx.term1000());
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}

	}

	@Override
	public PrologTerm visitTerm1050(Term1050Context ctx) {
		/**
		 * term1000 ( ('*->' | '->') term1050 )?
		 */
		PrologTerm lhs = visitTerm1000(ctx.term1000());
		if (ctx.term1050() == null) {
			return lhs;
		} else {
			PrologTerm rhs = visitTerm1050(ctx.term1050());
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm1100(Term1100Context ctx) {
		/**
		 * term1050 (';' term1100)?
		 */
		PrologTerm lhs = visitTerm1050(ctx.term1050());
		if (ctx.term1100() == null) {
			return lhs;
		} else {
			PrologTerm rhs = visitTerm1100(ctx.term1100());
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm1105(Term1105Context ctx) {
		/**
		 * term1100 ('|' term1105)?
		 */
		PrologTerm lhs = visitTerm1100(ctx.term1100());
		if (ctx.term1105() == null) {
			return lhs;
		} else {
			PrologTerm rhs = visitTerm1105(ctx.term1105());
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm1150(Term1150Context ctx) {
		// term1105 | op=DYNAMIC term1000
		if (ctx.term1000() == null) {
			return visitTerm1105(ctx.term1105());
		} else {
			org.jpl7.Term[] args = { visitTerm1000(ctx.term1000()).getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	@Override
	public PrologTerm visitTerm1200(Term1200Context ctx) {
		/**
		 * term1105 ( ( op=':-' | op='-->') term1105)? | op='?-' term1105
		 */
		PrologTerm lhs = visitTerm1150(ctx.term1150(0));
		if (ctx.op == null) {
			return lhs;
		} else if ("?-".equals(ctx.op.getText())) {
			org.jpl7.Term[] args = { lhs.getTerm() };
			return compound("?-", args, ctx);
		} else {
			// op=':-' | op='-->' and we have a 2nd arg
			PrologTerm rhs = visitTerm1150(ctx.term1150(1));
			org.jpl7.Term[] args = { lhs.getTerm(), rhs.getTerm() };
			return compound(ctx.op.getText(), args, ctx);
		}
	}

	/**
	 * Get all errors that occured in the visiting phase (excluding the parsing
	 * errors).
	 *
	 * @return
	 */
	public List<ParserException> getVisitorErrors() {
		return this.errors;
	}
}