package org.aksw.sparqlify.config.syntax.r2rml;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import mapping.SparqlifyConstants;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.MapReader;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_URI;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;


public class ImporterR2RML {
	
	private static com.hp.hpl.jena.rdf.model.Model r2rmlGraph = ModelFactory.createDefaultModel();
	private static ViewDefinition r2rmlViewDef;
	
	public static com.hp.hpl.jena.rdf.model.Model loadR2rmlGraph(String r2rmlFileName){
		
		//Model model = ModelFactory.createDefaultModel();
		///model.read(url)
		
		
		FileManager.get().readModel(r2rmlGraph, r2rmlFileName);
		return r2rmlGraph;
	}
	public static void main(String[] args)
		throws Exception
	{
		
		RdfViewSystemOld.initSparqlifyFunctions();
		
		
		DatatypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);

		
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		ViewDefinition personView = vdf.create(
				"Prefix ex:<http://ex.org/>							" +
				"Create View person As 								" +
				"	Construct {										" +
				"		?s a ex:Person ; ex:name ?t					" +
				"	}" +
				"With " +
				"	?s = uri(concat('http://ex.org/person/', ?ID) 	" +
				"	?t = plainLiteral(?NAME) From person			");
		ViewDefinition deptView = vdf.create(
				"Prefix ex:<http://ex.org/>" +
				"Create View dept As " +
				"	Construct { " +
				"		?s a ex:Department ; ex:name ?t" +
				"	} " +
				"With " +
				"	?s = uri(concat('http://ex.org/dept/', ?ID) " +
				"	?t = plainLiteral(?NAME) From dept");
		ViewDefinition personToDeptView = vdf.create(
				"Prefix ex:<http://ex.org/>" +
				"Create View person_to_dept " +
				"	As Construct { ?p ex:worksIn ?d } " +
				"With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) " +
				"	?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) " +
				"From person_to_dept");

		
		//System.out.println(personView);
		
		/*
		System.out.println("test");
		*/
		

		{
			ExprList eargs = new ExprList();
			eargs.add(NodeValue.makeString("http://example.org/"));
			eargs.add(new ExprVar(Var.alloc("varName")));
			
			
			Expr subject =
					new E_Function(SparqlifyConstants.uriLabel,
							new ExprList(new E_StrConcatPermissive(eargs))
					);
			
			System.out.println(subject+"*******************************************");
		}
		
		//test R2RML 2 Sparqlify
		{
		
			//FileManager.get().readModel(r2rmlGraph, args[0]);
			loadR2rmlGraph(args[0]);
			
			r2rmlGraph.write(System.out, "TTL");
			
			R2RMLSpec myMapping = new R2RMLSpec(r2rmlGraph);

			Multimap<LogicalTable, TriplesMap> tableToTm = HashMultimap.create();
			
			// Group triple maps by their logical table
			for(TriplesMap tm : myMapping.getTripleMaps()) {
				LogicalTable lt = tm.getLogicalTable();

				tableToTm.put(lt, tm);
			}
			
			Set<ViewDefinition> result = new HashSet<ViewDefinition>();
			
			// Let's create view definitions
			for(Entry<LogicalTable, Collection<TriplesMap>> entry : tableToTm.asMap().entrySet()) {
				
				LogicalTable logicalTable = entry.getKey();
				Collection<TriplesMap> triplesMaps = entry.getValue();
				
				
				String name = logicalTable + "" + triplesMaps;  

				Var var = Var.alloc("myVar");

				// TODO Create the template by extracting the quads from all the triple maps
				QuadPattern template = new QuadPattern();
				template.add(new Quad(Quad.defaultGraphNodeGenerated, var, RDF.type.asNode(), ResourceFactory.createPlainLiteral("hello world").asNode()));
				
				
				
				// TODO Extract the variable definitions from the predicate object maps
				Multimap<Var, RestrictedExpr> varToExprs = HashMultimap.create(); 

				
				
				ExprList a = new ExprList();
				a.add(NodeValue.makeString("http://foo.bar/"));
				a.add(new ExprVar(Var.alloc("nr")));
				
				Expr expr = new E_URI(new E_StrConcatPermissive(a));
				
				//Expr expr = ExprUtils.parse("<" + SparqlifyConstants.uriLabel + ">(?myColumn)");
				RestrictedExpr restExpr = new RestrictedExpr(expr);

				varToExprs.put(var, restExpr);
				
				VarDefinition varDef = new VarDefinition(varToExprs);
				
				
				SqlOp op;
				// Create the table node
				if(logicalTable.isTableName()) {
					String tableName = logicalTable.getTableName();
					
					op = new SqlOpTable(new SchemaImpl(), tableName);
				} else {
					throw new RuntimeException("Not implemented");
				}
				
				
				
				
				
				Mapping mapping = new Mapping(varDef, op);
				
				
				
				ViewDefinition viewDef = new ViewDefinition(name, template, null, mapping, entry);
				
				result.add(viewDef);
			}
			
			
			for(ViewDefinition viewDef : result) {
				System.out.println(viewDef);
			}
			
			if(true) {
				System.exit(0);
			}
			
			
			//r2rmlViewDef.
			
			
			
			String queryString = "Select * where { ?s <http://www.w3.org/ns/r2rml#tableName> ?o} ";
			com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

			System.out.println("----------------------");
			System.out.println("Query Result Sheet");
			System.out.println("----------------------");		   
				  
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, r2rmlGraph);
			com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();

			// Output query results    
			ResultSetFormatter.out(System.out, results, query);

			qe.close();
				    
			System.out.println("----------------------");
				    

		}
		
	}
}

