package org.aksw.sparqlify.export.r2rml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.domain.input.RestrictedExpr;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;


/**
 * Class used to convert variable definitions (varDefs) to term map values. So
 * this class mainly provides means to resolve (possibly nested) term
 * constructors and return a suitable R2RML representation. E.g.
 * 
 *   uri(concat(ex:foo, '/', ?bar))
 *   
 * will be resolved to a certain representation of
 * 
 *   ... [
 *   	rr:template "http://example.org/foo/{bar};
 *   	rr:termType rr:IRI;
 *  ]
 *   
 * assuming ex: <http://example.org/> and ?bar being a column reference of the
 * considered logical table.
 *
 */
public abstract class TermConstructorExporter {
	// final attributes
	private final static String plainLiteralFunctionIRI = "http://aksw.org/sparqlify/plainLiteral";
	private final static String typedLiteralFunctionIRI = "http://aksw.org/sparqlify/typedLiteral";
	private final static String blankNodeFunctionIRI = "http://aksw.org/sparqlify/blankNode";
	private final static String uriFunctionIRI = "http://aksw.org/sparqlify/uri";
	private final static FunctionLabel concatLabel = new FunctionLabel("concat");
	private final static FunctionLabel functionLabel = new FunctionLabel("function");
	private final static FunctionLabel strLabel = new FunctionLabel("str");
	private final static String notImplemented = "NOT IMPLEMENTED!!!";
	

	/**
	 * Builds R2RML representations (so called term map values) of a given
	 * collection of SML variable restrictions (varResrictions).
	 * 
	 * @param varRestrictions
	 * 		A collection of variable restrictions.
	 * @return
	 * 		A List of term map values
	 */
	public static List<TermMapValue> varDef2TermMapValues(
			Collection<RestrictedExpr> varRestrictions) {
		/*
		 * TODO: cases like uri(uri(...)) aren't covered, yet. The problem there
		 * is that the outer uri function restriction has no args when arriving
		 * here.
		 */

		List<TermMapValue> termMapValues = new ArrayList<TermMapValue>();
		
		// iterate over restrictions
		for (RestrictedExpr restriction : varRestrictions) {
			// result of this loop run
			TermMapValue termMapValue;
			
			Expr expr = restriction.getExpr();
			// column names (if any column references are used)
			Set<Var> varsMentioned = expr.getVarsMentioned();
			
			
			/*
			 * Since the considered expressions here are the outer most
			 * expressions they can only be of one of the following function
			 * forms.  
			 */	
			if (expr.isFunction()) {
				/*
				 * If the restriction expression is a function one has to
				 * differentiate between the following cases:
				 * - plainLiteral
				 * - typedLiteral
				 * - blankNode
				 * - URI
				 */
				ExprFunction func = expr.getFunction();
				
				/* plain literal */
				if (func.getFunctionIRI().equals(plainLiteralFunctionIRI)) {
					String literal = buildPlainLiteralTemplate(expr);
					List<Expr> args = func.getArgs();
					
					// decide, if rr:column or rr:template should be used
					String property;
					
					if (args.get(0).isVariable()) {
						property = Constants.rrColumn;
						// in the rr:column case the surrounding curly braces
						// have to be stripped off, i.e. "{foo}" will become
						// "foo"
						int strlength = literal.length();
						literal = literal.substring(1, strlength-1);
					} else if (args.get(0).isConstant()) {
						property = Constants.rrConstant;
					} else property = Constants.rrTemplate;
					
					termMapValue = new TermMapValue(property, literal);
					
					// In the literal case the term type is optional.
					// So the next line could also be commented out.
					termMapValue.setTermType(Constants.rrLiteral);
					
					// check if language is set
					if (args.size() == 2) {
						String lang = args.get(1).toString();
						termMapValue.setLanguage(lang);
					}
					
					
				/* typed literal */
				} else if (func.getFunctionIRI().equals(typedLiteralFunctionIRI)) {
					String literal = buildTypedLiteralTemplate(expr);
					List<Expr> args = func.getArgs();
					
					// decide, if rr:column or rr:template should be used
					String property;
					
					if (args.get(0).isVariable()) {
						property = Constants.rrColumn;
						// in the rr:column case the surrounding curly braces
						// have to be stripped off, i.e. "{foo}" will become
						// "foo"  
						int strlength = literal.length();
						literal = literal.substring(1, strlength - 1);
					} else if (args.get(0).isConstant()) {
						property = Constants.rrConstant;
					} else property = Constants.rrTemplate;
					
					termMapValue = new TermMapValue(property, literal);
					
					// typedLiteral term constructor has to have two arguments
					// (so no check needed here)
					String datatype = args.get(1).toString();
					
					// strip off surrounding angle braces
					if (datatype.startsWith("<")) {
						int strLength = datatype.length();
						datatype = datatype.substring(1, strLength-1);
					}
					termMapValue.setDatatype(datatype);
					
					// In the literal case the term type is optional.
					// So the next line could also be commented out.
					termMapValue.setTermType(Constants.rrLiteral);
				
				/* blank node */
				} else if (func.getFunctionIRI().equals(blankNodeFunctionIRI)) {
					String literal = buildBlankNodeTemplate(expr);
					List<Expr> args = func.getArgs();
					
					// decide, if rr:column or rr:template should be used
					String property;
					
					if (args.get(0).isVariable()) {
						property = Constants.rrColumn;
						// in the rr:column case the surrounding curly braces
						// have to be stripped off, i.e. "{foo}" will become
						// "foo"
						int strlength = literal.length();
						literal = literal.substring(1, strlength-1);
					} else if (args.get(0).isConstant()) {
						property = Constants.rrConstant;
					} else property = Constants.rrTemplate;
					
					termMapValue = new TermMapValue(property, literal);
					
					termMapValue.setTermType(Constants.rrBlankNode);

				/* uri */
				} else if (func.getFunctionIRI().equals(uriFunctionIRI)) {
					String uri = buildUriTemplate(expr);
					List<Expr> args = func.getArgs();
					
					// decide, if rr:column or rr:template should be used
					String property;
					
					if (args.size()>0 && args.get(0).isVariable()) {
						property = Constants.rrColumn;
						// in the rr:column case the surrounding curly braces
						// have to be stripped off, i.e. "{foo}" will become
						// "foo"
						int strlength = uri.length();
						uri= uri.substring(1, strlength-1);
					} else if (args.size()>0 && args.get(0).isConstant()) {
						property = Constants.rrConstant;
					} else property = Constants.rrTemplate;
					
					termMapValue = new TermMapValue(property, uri);
					termMapValue.setTermType(Constants.rrIRI);
					
				/* fallback */
				} else {
					// TODO: find fallback that makes sense
					// FIXME: dummy
					termMapValue =
						new TermMapValue(Constants.rrConstant, notImplemented);
				}
				
			/* fallback */
			} else { // should never happen (TM)
				// TODO: throw reasonable error
				// FIXME: dummy
				termMapValue =
						new TermMapValue(Constants.rrConstant, notImplemented);
			}
			
			if (varsMentioned.size()>0) {
				// FIXME: throw error if there is more than one Var
				termMapValue.setColumnReference(varsMentioned.iterator().next());
			}
			
			termMapValues.add(termMapValue);
		}
		
		return termMapValues;
	}
	
	
	private static String buildPlainLiteralTemplate(Expr expr) {
		Expr arg = expr.getFunction().getArgs().get(0);
		return constructTerm(arg);
	}
	
	
	private static String buildTypedLiteralTemplate(Expr expr) {
		Expr arg = expr.getFunction().getArgs().get(0);
		return constructTerm(arg);
	}
	
	
	private static String buildBlankNodeTemplate(Expr expr) {
		Expr arg = expr.getFunction().getArgs().get(0);
		return constructTerm(arg);
	}
	
	
	private static String buildUriTemplate(Expr expr) {
		String uriStr = new String("");
		
		for (Expr arg : expr.getFunction().getArgs()) {
			// TODO: check if this works as expected
			uriStr += constructTerm(arg);
		}
				
		return uriStr;
	}
	
	
	/**
	 * Resolves the actual value of a term constructor expression by calling
	 * more primitive functions. The primitive functions supported so far are
	 * - str
	 * - concat 
	 * 
	 * @param expr
	 * 		the expression its value has to be constructed
	 * @return
	 * 		A String containing the actual value of the expression suitable for
	 * 		an R2RML Triples Map 
	 */
	private static String constructTerm(Expr expr) {
		String result = "";

		/* constants */
		if (expr.isConstant()) {
			String val = expr.getConstant().asString();
			result += val;
		
		/* functions */
		} else if (expr.isFunction()) {
			ExprFunction func = expr.getFunction();
			
			/* concat */
			if (func.getFunctionSymbol().equals(concatLabel)) {
				result += concat(func.getArgs());
			/* str */
			} else if (func.getFunctionSymbol().equals(strLabel)) {
				// assuming str(...) is always used with just one variable
				result += str(func.getArgs().get((0)));
			} else {
				// TODO: implement
			}
		} else if (expr.isVariable()) {
			String varStr = expr.getVarName();
			result += "{" + varStr + "}";
		}
		
		return result;
	}

	
	private static String concat(List<Expr> exprs) {
		String result = "";
		for (Expr expr : exprs) {
			result += constructTerm(expr);
		}
		
		return result;
	}
	
	
	private static String str(Expr expr) {
		return constructTerm(expr);
	}
}
