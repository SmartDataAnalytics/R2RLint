package org.aksw.sparqlify.config.syntax.r2rml;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
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

	private static com.hp.hpl.jena.rdf.model.Model r2rmlGraph = ModelFactory
			.createDefaultModel();
	private static ViewDefinition r2rmlViewDef;

	public static com.hp.hpl.jena.rdf.model.Model loadR2rmlGraph(
			String r2rmlFileName) {

		// Model model = ModelFactory.createDefaultModel();
		// /model.read(url)

		FileManager.get().readModel(r2rmlGraph, r2rmlFileName);
		return r2rmlGraph;
	}

	public static void main(String[] args) throws Exception {

		RdfViewSystemOld.initSparqlifyFunctions();

//		TypeSystem datatypeSystem = SparqlifyUtils
//				.createDefaultDatatypeSystem();
		//SqlTranslator sqlTranslator = SparqlifyUtils.createSqlRewriter(); //new SqlTranslatorImpl(datatypeSystem);

		DataSource dataSource = SparqlifyUtils.createTestDatabase();
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File(
				"src/main/resources/type-map.h2.tsv"));

		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(
				conn, typeAlias);

		ViewDefinition personView = vdf
				.create("Prefix ex:<http://ex.org/>							"
						+ "Create View person As 								"
						+ "	Construct {										"
						+ "		?s a ex:Person ; ex:name ?t					" + "	}" + "With "
						+ "	?s = uri(concat('http://ex.org/person/', ?ID) 	"
						+ "	?t = plainLiteral(?NAME) From person			");
		ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/>"
				+ "Create View dept As " + "	Construct { "
				+ "		?s a ex:Department ; ex:name ?t" + "	} " + "With "
				+ "	?s = uri(concat('http://ex.org/dept/', ?ID) "
				+ "	?t = plainLiteral(?NAME) From dept");
		ViewDefinition personToDeptView = vdf
				.create("Prefix ex:<http://ex.org/>"
						+ "Create View person_to_dept "
						+ "	As Construct { ?p ex:worksIn ?d } "
						+ "With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) "
						+ "	?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) "
						+ "From person_to_dept");



		// FileManager.get().readModel(r2rmlGraph, args[0]);
		loadR2rmlGraph(args[0]);

		r2rmlGraph.write(System.out, "TTL");

		R2RMLSpec myMapping = new R2RMLSpec(r2rmlGraph);

		Multimap<LogicalTable, TriplesMap> tableToTm = HashMultimap
				.create();

		// Group triple maps by their logical table
		for (TriplesMap tm : myMapping.getTripleMaps()) {
			LogicalTable lt = tm.getLogicalTable();

			tableToTm.put(lt, tm);
		}

		Set<ViewDefinition> result = new HashSet<ViewDefinition>();

		// Let's create view definitions
		for (Entry<LogicalTable, Collection<TriplesMap>> entry : tableToTm.asMap().entrySet()) {

			LogicalTable logicalTable = entry.getKey();
			Collection<TriplesMap> triplesMaps = entry.getValue();
			String name = logicalTable + "" + triplesMaps;
			QuadPattern template = new QuadPattern();

			for (TriplesMap tm : triplesMaps) {
				SubjectMap sm = tm.getSubjectMap();
				RDFNode rrClass = sm.getRrClass();
				Multimap<Var, RestrictedExpr> varToExprs = HashMultimap.create();
				String templateString = sm.getTemplate();
				E_StrConcatPermissive e = RRUtils.parseTemplate(templateString);
				Expr pkExpr = new E_URI(e);
				RestrictedExpr restExpr = new RestrictedExpr(pkExpr);
				Generator genS = Gensym.create("S");
				Var varS = Var.alloc(genS.next());
				Var subjectVar = varS;
				varToExprs.put(varS, restExpr);
				VarDefinition varDef = new VarDefinition(varToExprs);
				template.add(new Quad(Quad.defaultGraphNodeGenerated, varS, RDF.type.asNode(), rrClass.asNode()));
				Generator genO = Gensym.create("O");

				for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
					for (ObjectMap om : pom.getObjectMap()) {
						
						varDef = getVarDefinition(template, varToExprs, subjectVar,	varDef, pom, om, genO);
					}
				}
				SqlOp op;
				// Create the table node
				if (logicalTable.isTableName()) {
					String tableName = logicalTable.getTableName();
					op = new SqlOpTable(new SchemaImpl(), tableName);
				} else {
					throw new RuntimeException("Not implemented");
				}
				Mapping mapping = new Mapping(varDef, op);

				ViewDefinition viewDef = new ViewDefinition(name,template, null, mapping, entry);

				result.add(viewDef);

			}
		}
		for (ViewDefinition viewDef : result) {
			System.out.println(viewDef);
		}

		if (true) {
			System.exit(0);
		}


	}



	/**
	 * @param template
	 * @param varToExprs
	 * @param subjectVar
	 * @param varDef
	 * @param genO
	 * @param pom
	 * @param tableExprList
	 * @param om
	 * @return
	 * @author sherif
	 */
	public static VarDefinition getVarDefinition(QuadPattern template,
			Multimap<Var, RestrictedExpr> varToExprs, Var subjectVar,
			VarDefinition varDef, PredicateObjectMap pom, ObjectMap om, Generator genO) {
		
		ExprList tableExprList = new ExprList();
		E_Function ef;
		RDFNode datatype = om.getDatatype();
		RDFNode languageTag = om.getLanguageTag();
		String term = getObjectMapTerm(om);
		if (term != null){

			tableExprList.add(new ExprVar(Var.alloc(term)));
			if (datatype != null) {
				tableExprList.add(NodeValue.makeNode(datatype.asNode())); // NodeValue.makeNode(Node.createURI(datatype.toString())));
				ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);
			} else if (languageTag != null) {
				tableExprList.add(NodeValue.makeNode(languageTag.asNode())); 
				ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);
			} else {
				ef = new E_Function(SparqlifyConstants.plainLiteralLabel,tableExprList);
			}
			RestrictedExpr tableRestExpr = new RestrictedExpr(ef);
			Var varO = Var.alloc(genO.next());
			varToExprs.put(varO, tableRestExpr);
			varDef = new VarDefinition(varToExprs);
			template.add(new Quad(Quad.defaultGraphNodeGenerated , subjectVar,pom.getPredicate().asNode() , varO));
		}
		return varDef;
	}

	public static String getObjectMapTerm(ObjectMap om) {

		switch (om.getTermSpec()) {
		case COLUMN: {
			return om.getColumnName();
		}
		case CONSTANT: {
			return om.getConstant();
		}
		case TEMPLATE: {
			return om.getTemplate();
		}
		case JOIN: {
			return null;
		} // NOT SUPPORTED YET
		default: {
			//			return null;
			throw new RuntimeException("Not supported TermSpec");
		}
		}

	}
	Map<String, ViewDefinition> load(InputStream in) {
	
		Map<String, ViewDefinition> actuals= new HashMap<String, ViewDefinition>();
		return actuals;
	}
}
