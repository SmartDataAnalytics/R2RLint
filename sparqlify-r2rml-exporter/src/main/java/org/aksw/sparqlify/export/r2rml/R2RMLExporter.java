package org.aksw.sparqlify.export.r2rml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;


public class R2RMLExporter {
	
	Collection<ViewDefinition> viewDefs;
	int idCounter;
	String rrNamespace = "http://www.w3.org/ns/r2rml#";
	String rrPrefix = "rr";
	FunctionLabel concatLabel = new FunctionLabel("concat");
	
	// just needed because I know no better way to let a method return multiple
	// values
	private class PredicateAndObject {
		Property predicate;
		Literal object;
		
		PredicateAndObject(Property predicate, Literal object) {
			this.predicate = predicate;
			this.object = object;
		}
		
		Property getPrediacte() {
			return predicate;
		}
		
		Literal getObject() {
			return object;
		}
	}

	/**
	 * @param viewDefs: a Collection<ViewDefinition> containing view definition data
	 */
	public R2RMLExporter(Collection<ViewDefinition> viewDefs) {
		this.viewDefs = viewDefs;
		idCounter = 1;
	}
	
	/**
	 * To actual export method returning an RDF model
	 * @return a com.hp.hpl.jena.rdf.model.Model representing the R2RML structure
	 */
	public Model export() {
		Model r2rml = ModelFactory.createDefaultModel();
		
		for (ViewDefinition viewDef : viewDefs) {
			exportViewDef(viewDef, r2rml);
		}
		return r2rml;
	}
	
	/**
	 * Derives an R2RML graph from the given Sparqlify-ML view definition input 
	 * @param viewDef: a Sparqlify-ML view definition (org.aksw.sparqlify.core.domain.input.ViewDefinition)
	 * @param r2rml: a com.hp.hpl.jena.rdf.model.Model representing the R2RML structure that will be built up
	 */
	private void exportViewDef(ViewDefinition viewDef, Model r2rml) {
		SqlOpBase relation = (SqlOpBase) viewDef.getMapping().getSqlOp();
		List<Quad> patterns = (List<Quad>) viewDef.getTemplate().getList();
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		// TODO: force non-null values of varRestrictions and viewReferences
		for (Quad pattern : patterns) {
			exportPattern(r2rml, pattern, relation, varDefs);
		}
	}
	
	/**
	 * Builds up the RDF model "r2ml" representing the R2RML structure of the given Sparqlify-ML definitions.
	 * At this point only parts of the Sparqlify-ML view definitions are considered.
	 * These are so called patterns representing quads (graph, subject, predicate object) that can be
	 * valid resources or variables with certain restrictions derived from Sparqlify-ML expressions like
	 * "uri(concat("http://panlex.org/plx/", ?li))" meaning that the actual value is constructed by creating a
	 * URI based on the concatenation of "http://panlex.org/plx/" and the value of the "li" column in the current
	 * line of the table and database at hand.
	 * @param r2rml: the target com.hp.hpl.jena.rdf.model.Model
	 * @param pattern: a quad that may contain variables in the subject, predicate or object position
	 * @param relation: the considered database table or constructed logical relation defined by a query or view 
	 * @param varDefs: the construction definition of the target value based in the actual database value and
	 * some additional data like prefix strings or certain functions like uri( ... )
	 */
	private void exportPattern(Model r2rml, Quad pattern, SqlOpBase relation, VarDefinition varDefs) {
		/*
		 * Just some hints concerning the variable names: I will give my best
		 * to be as consistent as possible obeying the following rules:
		 * * there is always a scope of a triple that defines the current
		 *   subject predicate and object
		 * * since triples can be nested it may be ambiguous what the current
		 *   subject, predicate or object is
		 * * since triples in R2RML refer to a subject ("rr:subjectMap"),
		 *   predicate or object ("predicateObjectMap") things become even more
		 *   unclear
		 * * I will determine a scope based on the its subject, so if the
		 *   subject is "foo", "fooSubject" is the subject in the "foo" scope.
		 *   "fooPredicate" is the predicate in the "foo" scope and so on.
		 * * since there may be several predicates and objects I will prefix
		 *   a hint stating the special use of the considered part of a triple,
		 *   so "fooPredicate_bar" is the predicate in the "foo" scope to define
		 *   "bar"
		 * * statements are named the same way: <scope>Statement_<use>
		 */

		// create the triples map subject
		String triplesMapId = String.format("#TriplesMap%d", idCounter++); 
		Resource triplesMapSubject = ResourceFactory.createResource(triplesMapId);
		
		// create logical table triple
		Property triplesMapPredicate_logicalTable =
				ResourceFactory.createProperty(rrNamespace + "logicalTable");
		
		Statement logicalTblStatement_tblDefinition =
					buildLogicalTableTriple(r2rml, relation);
		r2rml.add(logicalTblStatement_tblDefinition);
		
		Statement triplesMapStatement_logicalTable = r2rml.createStatement(
								triplesMapSubject,
								triplesMapPredicate_logicalTable,
								logicalTblStatement_tblDefinition.getSubject());
		r2rml.add(triplesMapStatement_logicalTable);
		
		/*
		 * subject map
		 * ===========
		 */
		Node subjectMapObject_templColOrConst = pattern.getSubject();
		List<Statement> triplesMapStatement_subjectMaps =
					buildMapStatements(
								r2rml,
								subjectMapObject_templColOrConst,
								varDefs);
		
		// there may be more than one entry, e.g.
		// [] rr:template "http://data.example.com/department/{DEPTNO}" and
		// [] rr:class ex:Department
		for (Statement statement : triplesMapStatement_subjectMaps) {
			r2rml.add(statement);
			//debugGraph.add(statement.asTriple());
		}
		
		// build up the subject map triple that looks sth. like this: 
		// <#TriplesMap2> rr:subjectMap []
		if (triplesMapStatement_subjectMaps.size() > 0) {
			// rr:subjectMap
			Property triplesMapPredicate_subjectMap =
					ResourceFactory.createProperty(rrNamespace, "subjectMap");
			// []
			RDFNode triplesMapObject_subjectMap =
					(RDFNode) triplesMapStatement_subjectMaps.get(0).getSubject();
			// <#TriplesMap2> rr:subjectMap []
			Statement subjectMapTriple = r2rml.createStatement(
								triplesMapSubject,
								triplesMapPredicate_subjectMap,
								triplesMapObject_subjectMap);
			r2rml.add(subjectMapTriple);
		}
		
		/*
		 * predicate map
		 * =============
		 */
		Node predicateMap_templColOrConst = pattern.getPredicate();
		List<Statement> prediacteMapStatements =
					buildMapStatements(
								r2rml,
								predicateMap_templColOrConst,
								varDefs);
		
		// there may be more than one entry, e.g.
		// [] rr:template "http://data.example.com/department/{DEPTNO}" and
		// [] rr:class ex:Department
		for (Statement statement : prediacteMapStatements) {
			r2rml.add(statement);
		}

		/*
		 * object map
		 * ==========
		 */
		Node objectMap_templColOrConst = pattern.getObject();
		List<Statement> objectMapStatements =
					buildMapStatements(
								r2rml,
								objectMap_templColOrConst,
								varDefs);
		
		// there may be more than one entry, e.g.
		// [] rr:template "http://data.example.com/department/{DEPTNO}" and
		// [] rr:class ex:Department
		for (Statement statement : objectMapStatements) {
			r2rml.add(statement);
		}

		/*
		 * predicate object map
		 * ====================
		 */
		// build the predicate-object map triple that may look like this:
		// <#TriplesMap2> rr:prediacteObjectMap [
		// 			rr:predicateMap [
		// 					rr:constant ex:name ] ;
		//			rr:objectMap [
		//					rr:column "ENAME" ];
		// ]

		// [#1]
		Resource triplesMapObject_predicateObjectMap = ResourceFactory.createResource(); 

		// 1) the statement for [#1] rr:predicateMap [#2]
		Property predicateObjectMapPredicate_predicateMap = 
					ResourceFactory.createProperty(rrNamespace, "predicateMap");
		// [#2]
		RDFNode predicateObjectMapObject_predicateMap =
				(RDFNode) prediacteMapStatements.get(0).getSubject();
		Statement predicateObjectMapStatement_predicateMap =
					r2rml.createStatement(
							triplesMapObject_predicateObjectMap,
							predicateObjectMapPredicate_predicateMap,
							predicateObjectMapObject_predicateMap);
		r2rml.add(predicateObjectMapStatement_predicateMap);
		
		// 2) the statement for [#1] rr:objectMap [#3]
		Property prediacteObjectMapPrediacte_objectMap = 
					ResourceFactory.createProperty(rrNamespace, "objectMap");
		// [#3]
		RDFNode prediacteObjectMapObject_objectMap = 
				(RDFNode) objectMapStatements.get(0).getSubject();
		Statement predicateObjectMapStatement_objectMap =
					r2rml.createStatement(
							triplesMapObject_predicateObjectMap,
							prediacteObjectMapPrediacte_objectMap,
							prediacteObjectMapObject_objectMap);
		r2rml.add(predicateObjectMapStatement_objectMap);
		
		// 3) the statement for <#TriplesMap2> rr:prediacteObjectMap [#1]
		Property triplesMapPredicate_predicateObjectMap =
				ResourceFactory.createProperty(rrNamespace, "predicateObjectMap");
		Statement triplesMapStatement_predicateObjectMap =
					r2rml.createStatement(
							triplesMapSubject,
							triplesMapPredicate_predicateObjectMap,
							triplesMapObject_predicateObjectMap);
		r2rml.add(triplesMapStatement_predicateObjectMap);
	}
	
	/**
	 * Builds up the one triple that states where the actual data for the target
	 * mapping comes from. Such a source can be simply a database table or an
	 * SQL query.
	 * @param r2rml: the target com.hp.hpl.jena.rdf.model.Model
	 * @param relation: the data source (table name or SQL query)
	 * @return the whole Statement stating where the data comes from, e.g. '[] rr:tableName "EMP"' 
	 */
	private Statement buildLogicalTableTriple(Model r2rml, SqlOpBase relation) {
		// subject (a blank node [])
		Resource logicalTableSubject = ResourceFactory.createResource();
		// predicate (rr:tableName or rr:sqlQuery)
		Property logicalTablePredicate;
		// object (a Literal like "SELECT DEPTNO FROM DEPT WHERE DEPTNO > 23" or
		// simply a table name like "DEPTNO" 
		Literal logicalTableObject;
		
		// it's a table
		if (relation instanceof SqlOpTable) {
			SqlOpTable tbl = (SqlOpTable) relation;
			logicalTablePredicate =
					ResourceFactory.createProperty(rrNamespace, "tableName");
			logicalTableObject =
					ResourceFactory.createPlainLiteral(tbl.getTableName());
		// it's a query
		} else if (relation instanceof SqlOpQuery) {
			SqlOpQuery query = (SqlOpQuery) relation;
			logicalTablePredicate =
					ResourceFactory.createProperty(rrNamespace, "sqlQuery");
			logicalTableObject =
					ResourceFactory.createPlainLiteral(query.getQueryString());
		// it's not possible
		} else {
			// should never be called since a relation can either be a table
			// or a query
			logicalTablePredicate =
					ResourceFactory.createProperty("");
			logicalTableObject =
					ResourceFactory.createPlainLiteral("");
		}
		
		Statement logicalTblStatement = r2rml.createStatement(
				logicalTableSubject,
				logicalTablePredicate,
				logicalTableObject);
		
		return logicalTblStatement;
	}
	
	/**
	 * Builds up statements like
	 * [] rr:template "http://data.example.com/department/{DEPTNO}" or
	 * [] rr:class ex:Department
	 * and returns them as a Statement List.
	 * @param r2rml: the target com.hp.hpl.jena.rdf.model.Model
	 * @param mappingData: the target that should be mapped to relational structures (subject, predicate or object) 
	 * @param varDefs: the construction definition of the target value based in the actual database value and
	 * some additional data like prefix strings or certain functions like uri( ... )
	 * @return a List<Statement> containing all the subject map statements
	 */
	private List<Statement> buildMapStatements(Model r2rml, Node mappingData, VarDefinition varDefs) {
		List<Statement> results = new ArrayList<Statement>();
		
		// a blank node []
		Resource mapSubject = ResourceFactory.createResource();
		// rr:template or rr:column or rr:constant
		Property mapPredicate;
		// a literal like "http://data.example.com/department/{DEPTNO}" or
		// simply "DEPTNO" (column name) or a constant "Foo bar!!"
		// (or in rare cases a URI, which is handled separately)
		Literal mapObject;
		
		// template or column
		if (mappingData.isVariable()) {
			Collection<RestrictedExpr> restrictions =
							varDefs.getDefinitions((Var) mappingData);
			List<PredicateAndObject> mapPredicateAndObjects =
						processRestrictions(restrictions);
			
			for (PredicateAndObject result: mapPredicateAndObjects) {
				mapObject = result.getObject();
				mapPredicate = result.getPrediacte();
				Statement resultStatement =
						r2rml.createStatement(
									mapSubject,
									mapPredicate,
									mapObject);
				results.add(resultStatement);
			}
			
		// everything that is not a variable is handled as a constant
		} else if (mappingData.isConcrete()){
			// URIs and Literals have to be handled separately since the methods
			// to retrieve the respective value are different
			
			Statement resultStatement;
			
			// URI
			if (mappingData.isURI()) {
				/*
				 * This case is somewhat special since the mapObject is not a
				 * Literal. So, this needs some special handling:
				 * * the Literal mapObject will not be used and only dummy initialized
				 * * a special mapObject_uri Resource will be created
				 * * the result will be created, appended to the List and
				 *   returned to not go through any further ordinary processing
				 */
				mapObject = null;
				
				Resource mapObject_uri =
						ResourceFactory.createResource(mappingData.getURI());
				mapPredicate = ResourceFactory.createProperty(rrNamespace, "constant");
				
				resultStatement = r2rml.createStatement(
									mapSubject,
									mapPredicate,
									mapObject_uri);
				results.add(resultStatement);
				
				return results;
				
			// Literal
			} else if (mappingData.isLiteral()) {
				
				mapObject = ResourceFactory.createPlainLiteral(
								mappingData.getLiteral().toString(false));
			
			// else (e.g. blank node)
			} else { // mapSubject.isBlank() == true
				// Hmm... I think this violates the standard. So lean back and
				// enjoy the trace...
				mapObject = null;
			}
			
			mapPredicate = ResourceFactory.createProperty(rrPrefix, "constant");
			
			resultStatement = r2rml.createStatement(
									mapSubject,
									mapPredicate,
									mapObject);
			results.add(resultStatement);
		}
		
		return results;
	}
	
	/**
	 * Builds up an R2RML literal string -- the actual mapping value that can be
	 * constructed like this: "http://data.example.com/department/{DEPTNO}" .
	 * This would correspond to the following Sparqlify-ML expression:
	 * uri(concat("http://data.example.com/department/", ?DEPTNO))
	 * So the given restrictions Collection encompass the following nested
	 * expressions (from outer to inner):
	 * * uri function
	 * * concat function
	 * * String "http://data.example.com/department/", variable DEPTNO
	 * 
	 * This method can be called for all kinds of mappings: rr:subjectMap,
	 * rr:predicateMap and rr:objectMap (but not for rr:predicateObjectMap which
	 * is kind of a container for the rr:predicateMap and rr:objectMap).
	 * @param restrictions: Collection<RestrictedExpr> containing restriction expressions
	 * @return a list of predicates and objects encapsulated in a List<PredicateAndObject>
	 */
	private List<PredicateAndObject> processRestrictions(Collection<RestrictedExpr> restrictions) {
		String exprStr = "";
		List<PredicateAndObject> results = new ArrayList<PredicateAndObject>();
		
		for (int i=0; i<restrictions.size(); i++) {
			Property mapPredicate;
			
			RestrictedExpr restriction = (RestrictedExpr) restrictions.toArray()[i];
			Expr expression = restriction.getExpr();
			
			// Build up the expression string recursively processing all
			// functions involved. In this case the mapPredicate can be
			// rr:template or rr:column. 
			if (expression.isFunction()) {
				exprStr += processRestrExpr(expression);

				// get first argument of the function
				Expr firstArg = expression.getFunction().getArgs().get(0);
				
				// rr:column
				if (expression.getFunction().getFunctionIRI() == 
							"http://aksw.org/sparqlify/plainLiteral" && 
							firstArg.isVariable()) {
					// if the outermost function is plainLiteral( ... ) with
					// a variable as first argument
					mapPredicate =
						ResourceFactory.createProperty(rrNamespace, "column");
					
					// Yes this is goofy, but I have to strip off the curly
					// braces added in the processRestrExpr method before. This
					// is necessary because down there I could not check if the
					// variable would end up in a rr:template or rr:column
					// literal
					int strlength = exprStr.length();
					exprStr = exprStr.substring(1, strlength-1);
				
				// rr:template
				} else {
					mapPredicate =
						ResourceFactory.createProperty(rrNamespace, "template");
				}
				
			// There is just a variable given that is interpreted as the target
			// column name. So the mapPredicate must be rr:column
			// FIXME: Since such expressions (e.g. ?variable_name=?COUMN) would
			// violate Sparqlify-ML, this branch should never be reached. 
			} else if (expression.isVariable()) {
				exprStr += expression.getVarName();
				mapPredicate = ResourceFactory.createProperty(rrNamespace, "column");
				
			// since the case of constant values was handled earlier there can't
			// be an else here and this branch will never never never ever be
			// reached ;)
			} else {
				mapPredicate = ResourceFactory.createProperty("None");
			}
			
			Literal mapObject = ResourceFactory.createPlainLiteral(exprStr);
			PredicateAndObject result =
							new PredicateAndObject(mapPredicate, mapObject);
			
			results.add(result);
		}
		
		return results;
	}

	/**
	 * Processes (at the time of writing some) known functions available in the
	 * Sparqlify-ML, variables and constant strings. Since arguments of the
	 * considered function can be functions, variables or constant strings as
	 * well, this method is called recursively to get to the most inner
	 * expression and build up the rest based on that.
	 * @param expr: a restriction expression (com.hp.hpl.jena.sparql.expr.Expr)
	 * like a function (concat( ... ), uri( ... ), ...) or a variable
	 * @return a String containing the R2RML counterpart of these Sparqlify-ML expressions
	 */
	private String processRestrExpr(Expr expr) {
		String exprStr = "";
		
		/*
		 *  functions
		 */
		if (expr.isFunction()) {
			ExprFunction func = expr.getFunction();
			
			// uri( ... )
			if (func.getFunctionIRI() == "http://aksw.org/sparqlify/uri") {
				// there should be just one argument here
				Expr subExpr = func.getArgs().get(0);
				exprStr += processRestrExpr(subExpr);
			
			// concat( ... )
			} else if (func.getFunctionSymbol().equals(concatLabel)) {
				List<Expr> args = func.getArgs();
				for (Expr arg : args) {
					exprStr += processRestrExpr(arg);
				}
				
			// plainLiteral
			} else if (func.getFunctionIRI() ==
							"http://aksw.org/sparqlify/plainLiteral") {
				// there should be just one argument here
				Expr subExpr = func.getArgs().get(0);
				exprStr += processRestrExpr(subExpr);
			}
			
		/*
		 *  variables and strings
		 */
		} else {
			
			// strings
			if (expr.isConstant()) {
				String constStr = expr.toString();
				if (constStr.startsWith("\"")) {
					// strip off the leading and trailing quote
					constStr = constStr.substring(1);
					
					if (constStr.endsWith("\"")) {
						int strLength = constStr.length();
						constStr = constStr.substring(0, strLength-1);
					}
				}
				exprStr += constStr;

			// variables
			} else if (expr.isVariable()) {
				String varStr = expr.toString();
				// strip off the leading question mark...
				varStr = varStr.substring(1);
				// ...and put the value in curly braces
				varStr = "{" + varStr + "}";
				exprStr += varStr;
			}
		}
			
		return exprStr;
	}
}
