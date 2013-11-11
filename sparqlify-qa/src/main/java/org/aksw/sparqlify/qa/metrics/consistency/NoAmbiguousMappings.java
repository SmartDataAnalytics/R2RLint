package org.aksw.sparqlify.qa.metrics.consistency;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 * This metric reports quad patterns in SML view definitions that may lead to
 * ambiguous RDF resources.
 * 'Ambiguous' here means that a certain resource may be created based on
 * different database entities. So e.g. if there is a table A having the columns
 * (a_id, a2, a3) and a table B with the columns (b_id, b2, b3) the following
 * view definitions may lead to ambiguous RDF resources, e.g.
 * <http://ex.org/amb/23>:
 * 
 * Create View a_view As
 *   Construct {
 *     ?a a ex:ClassA .
 *   }
 *   With
 *     ?a = uri(ex:amb, '/', ?a_id)
 *   From
 *     A
 * 
 * Create View a_view As
 *   Construct {
 *     ?b a ex:ClassB
 *   }
 *   With
 *     ?b = uri(ex:amb, '/', ?b_id)
 *   From
 *     B
 * 
 * To find such ambiguities a hash map is built like this:
 * { <normalized term constructor> :
 *     { <table key> : [
 *           (<quad>, <view definition>),
 *           (<quad>, <view definition>),
 *           ...
 *         ]
 *       <table key> : [
 *           ...
 *         ]
 *     }
 * }
 * 
 * (where the terms in angle brackets are surrogates of concrete values and
 * should not be misunderstood as URI resources).
 * 
 * The normalized term constructor is the string serialization of a term
 * constructor expression where the actual column variable names are replaced
 * by neutral markers. So e.g. "?column_xyz" would be replaced with "?VAR" and
 * a term constructor like
 * <http://aksw.org/sparqlify/uri>(concat("http://ex.org/", str(?id)))
 * would end up in 
 * <http://aksw.org/sparqlify/uri>(concat("http://ex.org/", str(?VAR)))
 * After this normalization step one can look up cases where the normalized
 * term constructors are the same, but the actual values come from different
 * (logical) tables. In such cases different database entities are mapped to the
 * same URI.
 * 
 * Special emphasis has to be put on foreign key situations: Although there the
 * normalized term constructors are the same and the values come from different
 * tables this does not result in ambiguity since both tables refer to the same
 * entities. So instead of using the actual (logical) table for distinction, a
 * 'table key' is used. In most cases the table key is the (logical) table name
 * except for cases, where the considered column is the (referencing) child of
 * a foreign key relation pointing to a unique value of the (referenced) parent
 * table. There, instead of the actual (logical) table name, the table name of
 * the parent table is used as table key.
 * 
 * Nevertheless this approach has some limitations that could only be
 * eliminated by parsing the considered SQL queries (if used) and looking up
 * the actual values from the database:
 * - This approach won't find ambiguous triples that were built using different
 *   term constructors ( e.g. uri(ex:foo, ?bar) vs uri(?foo_bar_uri))
 * - This approach my declare duplicates as ambiguous if SQL queries are
 *   involved, e.g.: A.a_id vs. a_id in "SELECT * FROM A JOIN B ON A.a2=B.b2"
 *   when using the same term constructors, e.g. uri(ex:foo, '/', a_id) ;
 *   since in this approach A != "SELECT * FROM A JOIN B ON A.a2=B.b2" the
 *   logical tables are considered different an so the contained entities are
 *   considered different.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class NoAmbiguousMappings extends MetricImpl implements MappingMetric {

	// term constructor map holding all normalized term constructors (column
	// names removed) and where they can stem from (table, quad, viewDef)
	HashMap<String, HashMap<String, List<Pair<Quad, ViewDefinition>>>> tcMap;

	@Autowired
	private DataSource rdb;
	private Connection conn;
	
	@PostConstruct
	private void init() throws SQLException {
		conn = rdb.getConnection();
	}
	@PreDestroy
	private void cleanUp() throws SQLException {
		conn.close();
	}
	
	// for testing
	protected void cleanCaches() {
		tcMap = new HashMap<String, HashMap<String, List<Pair<Quad, ViewDefinition>>>>();
	}
	
	public NoAmbiguousMappings() {
		super();
		tcMap = new HashMap<String, HashMap<String, List<Pair<Quad, ViewDefinition>>>>();
	}


	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {
		
		// gather term constructor informations
		for (ViewDefinition viewDef : viewDefs) {
			readTermConstructorInfos(viewDef);
		}
		
		// evaluate the collected informations
		for (HashMap<String, List<Pair<Quad, ViewDefinition>>> relationMap : tcMap.values()) {
			
			if (relationMap.keySet().size() > 1) reportAmbiguousMappings(relationMap);
		}
	}


	private void reportAmbiguousMappings(
			HashMap<String, List<Pair<Quad, ViewDefinition>>> relationMap)
			throws NotImplementedException {
		
		List<Pair<Quad, ViewDefinition>> quadViewDefs =
				new ArrayList<Pair<Quad, ViewDefinition>>();
		
		for (List<Pair<Quad, ViewDefinition>> relQuadViewDefs : relationMap.values()) {
			quadViewDefs.addAll(relQuadViewDefs);
		}
		
		writeMappingQuadMeasureToSink(0, quadViewDefs);
	}


	/*
	 * collect information for subject, predicate and object, if they are
	 * variable
	 */
	private void readTermConstructorInfos(ViewDefinition viewDef) {
		VarDefinition varDefs = viewDef.getVarDefinition();
		
		for (Quad quad : viewDef.getTemplate()) {
			Node subject = quad.getSubject();
			if (subject.isVariable())
				readTcsInfo(varDefs.getDefinitions(Var.alloc(subject)), quad, viewDef);
			
			Node predicate = quad.getPredicate();
			if (predicate.isVariable())
				readTcsInfo(varDefs.getDefinitions(Var.alloc(predicate)), quad, viewDef);
			
			Node object = quad.getObject();
			if (object.isVariable())
				readTcsInfo(varDefs.getDefinitions(Var.alloc(object)), quad, viewDef);
				
		}
	}


	private void readTcsInfo(Collection<RestrictedExpr> termConstructors, Quad quad, ViewDefinition viewDef) {
		// dummy loop since there is always just one term constructor
		// a.k.a RestrictedExpr
		for (RestrictedExpr tc : termConstructors) {
			Expr expr = tc.getExpr();
		
			// skip plainLiteral and typedLiteral term constructors
			ExprFunction func = expr.getFunction();
			// -1 = var, 0 = bNode, 1 = uri, 2 = plainLiteral, 3 = typedLiteral
			double type = func.getArg(1).getConstant().getDouble();
			if (type < 2) { 
				
				// get term constructor string serialization
				String tcString = expr.toString();
				
				// get the names of the referenced colunms
				List<String> colNames = new ArrayList<String>();
				Set<Var> varsMentioned = expr.getVarsMentioned();
				
				// if there are no, skip to avoid reporting constants
				if (varsMentioned.isEmpty()) continue;
				
				// normalize term constructor string (remove concrete variable
				// names with placeholders)
				for (Var colVar : varsMentioned) {
					String colVarName = colVar.getVarName().toLowerCase();
					colNames.add(colVarName);
					tcString = tcString.replaceAll("\\?" + colVarName, "VAR");
				}
				
				String relationName =
						getTableKey(colNames, viewDef.getMapping().getSqlOp());
				
				Pair<Quad, ViewDefinition> quadViewDef =
						new Pair<Quad, ViewDefinition>(quad, viewDef);
				
				addToTcMap(tcString, relationName, quadViewDef);
			}
		}
	}


	/**
	 * This method determines a table key used to store the term constructor
	 * informations in the tcMap hash map. In most cases the name of the table
	 * is used or the actual query serialization. Only in the case, that all
	 * considered columns are referencing columns of a foreign key relation the
	 * table name of the referenced table is used.
	 * If not *all* columns are the child part of a foreign key situation, the
	 * semantic is assumed to be as follows:
	 * There is a resource built up referring to entities of the parent table
	 * of the foreign key constraint *and* additional entities of another table.
	 * (This other table can be the original relation given as call parameter
	 * or a table that is involved in another foreign key relation.) So, since
	 * the actual database entities stem from different tables, this situation
	 * is considered a table join and the the table key is built putting all
	 * these tables together. A simple and reproducible schema is to concatenate
	 * all entities of the sorted set of involved table or query names.
	 * 
	 * @param columnNames
	 *         The column names used in one term constructor
	 * @param relation
	 *         The relation (a table or SQL query) that is used in a view
	 *         definition
	 * @return
	 *         A string containing the table key (built as described above)
	 */
	private String getTableKey (List<String> columnNames, SqlOp relation) {
		
		// since SQL queries are not parsed there isn't anything that can be
		// done in this case except stupidly returning the actual query string
		// to be used as table key
		if (!(relation instanceof SqlOpTable)) return getRelationName(relation);
		
		Set<String> involvedTables = new TreeSet<String>();
		
		String tableName = getRelationName(relation);
		try {
			DatabaseMetaData meta = conn.getMetaData();
			
			// get the tables that are referenced (if there are any)
			ResultSet foreignKeysRes = meta.getImportedKeys(conn.getCatalog(),
					null, tableName);
			
			while (foreignKeysRes.next()) {
				/*
				 * data based on test02 in the unit tests:
				 * 
				 * (java.lang.String) TEST0  // foreignKeysRes.getString(1)
				 * (java.lang.String) PUBLIC  // foreignKeysRes.getString(2)
				 * (java.lang.String) A  // foreignKeysRes.getString(3)
				 * (java.lang.String) A3  // foreignKeysRes.getString(4)
				 * (java.lang.String) TEST0  // foreignKeysRes.getString(5)
				 * (java.lang.String) PUBLIC  // foreignKeysRes.getString(6)
				 * (java.lang.String) B  // foreignKeysRes.getString(7)
				 * (java.lang.String) B3  // foreignKeysRes.getString(8)
				 */
				// get name of the referencing column
				String fKeyName = foreignKeysRes.getString(8).toLowerCase();
				
				// get the name of the referenced table
				String referencedTableName = foreignKeysRes.getString(3);
				
				// add table name to involved tables list if such a referencing
				// column is used in the input columnNames list
				if (columnNames.contains(fKeyName)) {
					columnNames.remove(fKeyName);
					involvedTables.add(referencedTableName);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// if there are still column names left, these aren't referencing
		// columns and the input relation is also involved (i.e. columnNames
		// would be empty if all the columns in the given term constructor were
		// referencing columns)
		if (!columnNames.isEmpty()) involvedTables.add(tableName);
		
		// build table key
		String tableKey = "";
		for (String tblName : involvedTables) {
			tableKey += tblName + "_";
		}
		tableKey = tableKey.substring(0, tableKey.length()-1);
		
		return tableKey;
	}


	/**
	 * This method adds the term constructor information collected to the tcMap
	 * hash map.
	 * 
	 * @param tcString
	 *         The serialized and normalized term constructor
	 * @param tableKey
	 *         Table key generated in the getTableKey method
	 * @param quadViewDef
	 *         Quad ViewDefinition pairs
	 */
	private void addToTcMap(String tcString, String tableKey,
			Pair<Quad, ViewDefinition> quadViewDef) {
		
		if (tcMap.containsKey(tcString)) {
			if (tcMap.get(tcString).containsKey(tableKey)) {
				// 1) term constructor already known and table key already known
				//    --> just add quadViewDef to the existing list
				tcMap.get(tcString).get(tableKey).add(quadViewDef);
				
			} else {
				// 2) term constructor already known and but table key unknown
				//    --> create new Quad ViewDefinition list
				//        [(Quad, ViewDefinition)] and put(tableKey, list)
				List<Pair<Quad, ViewDefinition>> quadViewDefs =
						new ArrayList<Pair<Quad, ViewDefinition>>();
				quadViewDefs.add(quadViewDef);
				
				tcMap.get(tcString).put(tableKey, quadViewDefs);
			}
		} else {
			// 3) neither the term constructor serialization nor the table key
			//    is known
			//    --> create new relation hash map
			//        {String: [(Quad, ViewDefinition)]} and put(tcString, map)
			List<Pair<Quad, ViewDefinition>> quadViewDefs =
					new ArrayList<Pair<Quad, ViewDefinition>>();
			quadViewDefs.add(quadViewDef);
			
			HashMap<String, List<Pair<Quad, ViewDefinition>>> relMap =
					new HashMap<String, List<Pair<Quad, ViewDefinition>>>();
			relMap.put(tableKey, quadViewDefs);
			
			tcMap.put(tcString, relMap);
		}
	}


	private String getRelationName(SqlOp relation) {
		
		if (relation instanceof SqlOpTable) {
			return ((SqlOpTable) relation).getTableName();
		} else if (relation instanceof SqlOpQuery) {
			return ((SqlOpQuery) relation).getQueryString();
		} else {
			return relation.toString();
		}
	}

}
