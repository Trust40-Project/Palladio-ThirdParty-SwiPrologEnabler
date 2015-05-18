package swiprolog.validator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jpl.Term;
import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

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
 * Implements the antlr ParserVisitor and creates the proper objects from the
 * parsed tree
 * 
 * @author W.Pasman 23apr15
 *
 */
public class Prolog4Visitor extends Prolog4ParserBaseVisitor {

	private final File sourcefile;

	Prolog4Visitor(File source) {
		sourcefile = source;
	}

	/**
	 * Create {@link SourceInfoObject} for given context.
	 * 
	 * @param ctx
	 *            the {@link DirectiveContext} from the parsed object
	 * @return {@link SourceInfoObject}
	 */
	private SourceInfo getSourceInfo(ParserRuleContext ctx) {
		return new SourceInfoObject(sourcefile, ctx.getStart().getLine(), ctx
				.getStart().getCharPositionInLine(), ctx.getStart()
				.getStartIndex(), ctx.getStop().getStopIndex());
	}

	private SourceInfo getSourceInfo(TerminalNode leaf) {
		Token symbol = leaf.getSymbol();
		return new SourceInfoObject(sourcefile, symbol.getLine(),
				symbol.getCharPositionInLine(), symbol.getStartIndex(),
				symbol.getStopIndex());
	}

	/**
	 * create new {@link jpl.Compound} using functor name, args and
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
	private PrologTerm compound(String name, jpl.Term[] args,
			ParserRuleContext ctx) {
		return compound(name, args, getSourceInfo(ctx));
	}

	/**
	 * create new {@link jpl.Compound} using functor name, args and
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
	private PrologTerm compound(String name, jpl.Term[] args, SourceInfo info) {
		return new PrologTerm(new jpl.Compound(name, args), info);
	}

	/**
	 * craete new {@link jpl.Atom} with given name
	 * 
	 * @param name
	 *            of new atom
	 * @param ctx
	 *            the {@link ParserRuleContext}
	 * @return new atom
	 */
	private PrologTerm atom(String name, ParserRuleContext ctx) {
		return new PrologTerm(new jpl.Atom(name), getSourceInfo(ctx));
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
		List<PrologTerm> clauses = new LinkedList<PrologTerm>();
		for (DirectiveorclauseContext d : ctx.directiveorclause()) {

			clauses.add(visitDirectiveorclause(d));
		}
		return clauses;
	}

	@Override
	public PrologTerm visitDirectiveorclause(
			Prolog4Parser.DirectiveorclauseContext ctx) {
		if (ctx.directive() != null) {
			return visitDirective(ctx.directive());
		} else { // ctx.clause() // CHECK null?
			return visitClause(ctx.clause());
		}
	}

	@Override
	public PrologTerm visitDirective(DirectiveContext ctx) {
		PrologTerm t = visitTerm1200(ctx.term1200());
		jpl.Term[] args = { t.getTerm() };
		return compound(":-", args, ctx);
	}

	@Override
	public PrologTerm visitClause(ClauseContext ctx) {
		return visitTerm1200(ctx.term1200());
	}

	@Override
	public List<PrologTerm> visitArglist(ArglistContext ctx) {
		List<PrologTerm> argList = new LinkedList<PrologTerm>();
		argList.add(visitExpression(ctx.expression()));
		argList.addAll(visitArglist(ctx.arglist()));
		return argList;
	}

	@Override
	public PrologTerm visitPossiblyEmptyConjunct(
			PossiblyEmptyConjunctContext ctx) {
		if (ctx.term1000() != null) {
			return visitTerm1000(ctx.term1000());
		}
		return atom("true", ctx);
	}

	@Override
	public PrologTerm visitPossiblyEmptyDisjunct(
			PossiblyEmptyDisjunctContext ctx) {
		if (ctx.term1100() != null) {
			return visitTerm1100(ctx.term1100());
		}
		return atom("true", ctx);
	}

	@Override
	public PrologTerm visitExpression(ExpressionContext ctx) {
		return visitTerm900(ctx.term900());
	}

	@Override
	public PrologTerm visitListterm(ListtermContext ctx) {
		if (ctx.items() == null) {
			return atom("[]", ctx);
		}
		return visitItems(ctx.items());
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
			tail = new PrologVar(new jpl.Variable(ctx.VARIABLE().getText()),
					getSourceInfo(ctx.VARIABLE()));
		}

		if (tail == null) {
			jpl.Term[] args1 = { head.getTerm(), new jpl.Atom("[]") };
			return compound(".", args1, ctx);
		} else {
			jpl.Term[] args2 = { head.getTerm(), tail.getTerm() };
			return compound(".", args2, ctx);
		}

	}

	@Override
	public PrologTerm visitPrefixoperator(PrefixoperatorContext ctx) {
		jpl.Term[] args = { visitExpression(ctx.expression(0)).getTerm(),
				visitExpression(ctx.expression(1)).getTerm() };
		return compound(ctx.prefixop().getText(), args, ctx);
	}

	@Override
	public PrologTerm visitTerm0(Term0Context ctx) {
		if (ctx.NUMBER() != null) {
			String text = ctx.NUMBER().getText();
			if (text.matches("[0-9]+") || text.matches("0[box].*")) {
				Long val = Long.valueOf(text);
				return new PrologTerm(JPLUtils.createIntegerNumber(val),
						getSourceInfo(ctx)); // int, octal, hex, etc.
			}
			// float
			return new PrologTerm(new jpl.Float(Double.valueOf(text)),
					getSourceInfo(ctx));

		}
		if (ctx.NAME() != null) {
			String name = ctx.NAME().getText();
			ArglistContext args = ctx.arglist();
			if (args == null) {
				return atom(name, ctx);
			}
			List<PrologTerm> a = visitArglist(args);

			// functor with arguments
			List<jpl.Term> terms = new ArrayList<jpl.Term>(a.size());
			for (PrologTerm pterm : a) {
				terms.add(pterm.getTerm());
			}
			return compound(name, terms.toArray(new jpl.Term[0]), ctx);
		}
		if (ctx.VARIABLE() != null) {
			return new PrologVar(new jpl.Variable(ctx.VARIABLE().getText()),
					getSourceInfo(ctx));
		}
		if (ctx.STRING() != null) {
			return atom(unquote(ctx.STRING().getText()), ctx);
		}
		if (ctx.LBR() != null || ctx.CLBR() != null) {
			return visitTerm1200(ctx.term1200());
		}
		if (ctx.listterm() != null) {
			return visitListterm(ctx.listterm());
		}
		if (ctx.prefixoperator() != null) {
			return visitPrefixoperator(ctx.prefixoperator());
		}
		return null; // should never be reached
	}

	@Override
	public PrologTerm visitTerm50(Term50Context ctx) {
		PrologTerm t1 = visitTerm0(ctx.term0(0));
		if (ctx.term0(1) == null) {
			return t1;
		}
		PrologTerm t2 = visitTerm0(ctx.term0(1));
		jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
		return compound(":", args, ctx);
	}

	@Override
	public PrologTerm visitTerm100(Term100Context ctx) {
		PrologTerm t1 = visitTerm50(ctx.term50(0));
		if (ctx.term50(1) == null) {
			return t1;
		}
		PrologTerm t2 = visitTerm50(ctx.term50(1));
		jpl.Term[] args = { t1.getTerm(), t2.getTerm() };
		return compound("@", args, ctx);
	}

	@Override
	public PrologTerm visitTerm200(Term200Context ctx) {
		String op = ctx.op.getText();
		PrologTerm term;

		if (op.equals("-") || op.equals("\\")) {
			// (op = '-' | op= '\\' ) term200

			Term t = visitTerm200(ctx.term200()).getTerm();
			jpl.Term[] args = { t };
			term = compound(op, args, ctx);
			if (op.equals("-")) {
				// minus sign, check special case of numeric constant. See ISO
				// 6.3.1.2 footnote
				// Note, we interpret this footnote RECURSIVELY, eg --1 == 1.
				// Note that this notation is not SWI prolog compatible, SWI
				// seems to fail ISO
				// compliance here.
				if (t.isFloat()) {
					term = new PrologTerm(new jpl.Float(-1 * t.floatValue()),
							getSourceInfo(ctx));
				} else { // integer
					term = new PrologTerm(new jpl.Integer(-1 * t.intValue()),
							getSourceInfo(ctx));
				}
			}
		} else {
			// term100 ( (op= '^' term200) | (op= '**' term100) )?

			Term t1 = visitTerm100(ctx.term100(0)).getTerm();
			Term t2;
			if (op.equals("^")) {
				t2 = visitTerm200(ctx.term200()).getTerm();
			} else {
				t2 = visitTerm100(ctx.term100(1)).getTerm();
			}
			jpl.Term[] args = { t1, t2 };
			term = compound(op, args, ctx);
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
			Term t1 = visitTerm400b(t);
			jpl.Term[] args = { term.getTerm(), t1 };
			term = compound(t.op.getText(), args, ctx);
		}
		return term;
	}

	@Override
	public Term visitTerm400b(Term400bContext ctx) {
		return visitTerm200(ctx.term200()).getTerm();
	}

	@Override
	public PrologTerm visitTerm500(Term500Context ctx) {
		/*
		 * term400 ( ('+' | '-' | '/\\' | '\\/' | 'xor' | '><') term400 )*
		 */
		PrologTerm term = visitTerm400(ctx.term400());
		for (Term500bContext t : ctx.term500b()) {
			Term t1 = visitTerm500b(t);
			jpl.Term[] args = { term.getTerm(), t1 };
			term = compound(t.op.getText(), args, ctx);
		}

		return term;
	}

	@Override
	public Term visitTerm500b(Term500bContext ctx) {
		return visitTerm400(ctx.term400()).getTerm();
	}

	@Override
	public PrologTerm visitTerm700(Term700Context ctx) {
/**
		        term500
       ( 
         (	'=' | '\\=' | '==' | '\\==' | '@<' | '@=<' | 
			'@>' | '@>=' | '=@='| '=..' | 'is' | '=:=' | '=\\=' |
        	'<' | '=<' | '>' | '>=') 
         term500 
       )?
		 */
		PrologTerm lhs = visitTerm500(ctx.term500(0));
		PrologTerm rhs = visitTerm500(ctx.term500(1));
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };

		return compound(ctx.op.getText(), args, ctx);
	}

	@Override
	public PrologTerm visitTerm900(Term900Context ctx) {

		/**
		 * : term700 | '\\+' term900
		 */
		if (ctx.term700() != null) {
			return visitTerm700(ctx.term700());
		}

		jpl.Term[] args = { visitTerm900(ctx.term900()).getTerm() };

		return compound(ctx.op.getText(), args, ctx);
	}

	@Override
	public PrologTerm visitTerm1000(Term1000Context ctx) {
		/**
		 * : term900 (',' term1000)?
		 */
		PrologTerm lhs = visitTerm900(ctx.term900());
		if (ctx.term1000() == null) {
			return lhs;
		}

		PrologTerm rhs = visitTerm1000(ctx.term1000());
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };

		return compound(ctx.op.getText(), args, ctx);

	}

	@Override
	public PrologTerm visitTerm1050(Term1050Context ctx) {
		/**
		 * term1000 ( ('*->' | '->') term1050 )?
		 */
		PrologTerm lhs = visitTerm1000(ctx.term1000());
		if (ctx.term1050() == null) {
			return lhs;
		}

		PrologTerm rhs = visitTerm1050(ctx.term1050());
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };

		return compound(ctx.op.getText(), args, ctx);
	}

	@Override
	public PrologTerm visitTerm1100(Term1100Context ctx) {
		/**
		 * term1050 (';' term1100)?
		 */
		PrologTerm lhs = visitTerm1050(ctx.term1050());
		if (ctx.term1100() == null) {
			return lhs;
		}

		PrologTerm rhs = visitTerm1100(ctx.term1100());
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };

		return compound(ctx.op.getText(), args, ctx);

	}

	@Override
	public PrologTerm visitTerm1105(Term1105Context ctx) {
		/**
		 * term1100 ('|' term1105)?
		 */
		PrologTerm lhs = visitTerm1100(ctx.term1100());
		if (ctx.term1105() == null) {
			return lhs;
		}

		PrologTerm rhs = visitTerm1105(ctx.term1105());
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };

		return compound(ctx.op.getText(), args, ctx);

	}

	@Override
	public PrologTerm visitTerm1200(Term1200Context ctx) {
		/**
		 * term1105 ( ( op=':-' | op='-->') term1105)? | op='?-' term1105
		 */
		PrologTerm lhs = visitTerm1105(ctx.term1105(0));

		if (ctx.op == null) {
			return lhs;
		}

		if ("?-".equals(ctx.op.getText())) {
			jpl.Term[] args = { lhs.getTerm() };
			return compound("?-", args, ctx);
		}

		// op=':-' | op='-->' and we have a 2nd arg
		PrologTerm rhs = visitTerm1105(ctx.term1105(1));
		jpl.Term[] args = { lhs.getTerm(), rhs.getTerm() };
		return compound(ctx.op.getText(), args, ctx);

	}

}