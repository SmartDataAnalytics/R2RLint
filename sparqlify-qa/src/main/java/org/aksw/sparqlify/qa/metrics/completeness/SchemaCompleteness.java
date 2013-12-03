package org.aksw.sparqlify.qa.metrics.completeness;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
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

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * This metric measures the ratio between the properties introduced in the
 * view definitions and possible properties that could be extracted from the
 * database.
 * The number of possible properties is the sum of the numbers of columns of
 * all tables in the considered database omitting foreign key columns (of the
 * child tables).
 * The number of properties introduced via the RDB2RDF mapping equals the
 * number of triple patterns in the view definitions having at least a variable
 * subject and object. Constants in the subject or object position are
 * considered as additional information and so should not contribute to the
 * completeness. On the other hand mappings having a variable predicate are
 * only counted once, independent of the actual created predicates to avoid
 * over-complete mappings.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class SchemaCompleteness extends MetricImpl implements MappingMetric {
	
	@Autowired
	private DataSource rdb;
	private Connection conn;
	
	private boolean countVariablePredicates = true;
	
	@PostConstruct
	private void init() throws SQLException {
		conn = rdb.getConnection();
	}
	
	@PreDestroy
	private void cleanUp() throws SQLException {
		conn.close();
	}
	
	/**
	 * mainly for testing purposes
	 * 
	 * @param countVariablePredicates: controls if all possible bindings of a
	 *           variable predicate should be counted or if the variable
	 *           predicate should only count once
	 */
	public void setCountVariablePredicates(boolean countVariablePredicates) {
		this.countVariablePredicates = countVariablePredicates;
	}


	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException {

		int numColumns = getNumColumns();
		int numMappingPattens = getNumMappingPatterns(viewDefs);
		float ratio = (float) numMappingPattens / (float) numColumns;
		
		if (threshold == 0 || ratio < threshold) {
			writeMappingMeasureToSink(ratio, null);
		}
		
	}


	private int getNumColumns() {
		int numColumns = 0;
		
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, "%" ,new String[] {"TABLE"} );
			
			// iterate over table names
			while (res.next()) {
				String tableName = res.getString(3);
				
				ResultSet colRes = meta.getColumns(null, null, tableName, "%");
				
				// iterate over column names (adding count)
				while(colRes.next()) {
					numColumns ++;
				}
				
				ResultSet foreignKeys =
						meta.getImportedKeys(conn.getCatalog(), null, tableName);
				
				// iterate over foreign keys of that table (subtracting count)
				while (foreignKeys.next()) {
					numColumns --;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return numColumns;
	}


	private int getNumMappingPatterns(Collection<ViewDefinition> viewDefs) {
		int numMappingPatterns = 0;
		
		for (ViewDefinition viewDef : viewDefs) {
			List<Quad> patterns = (List<Quad>) viewDef.getTemplate().getList();
			VarDefinition varDefs = viewDef.getVarDefinition();
			
			
			for (Quad pattern : patterns) {
				if (pattern.getSubject().isVariable()
						&& pattern.getObject().isVariable()) {

					// predicate is also variable
					if (pattern.getPredicate().isVariable()) {
						if (!countVariablePredicates) {
							numMappingPatterns++;
							continue;
						}
						
						SqlOpBase relation =
								(SqlOpBase) viewDef.getMapping().getSqlOp();
						Node_Variable predicate =
								(Node_Variable) pattern.getPredicate();
						Collection<RestrictedExpr> predVarDefs =
								varDefs.getDefinitions((Var) predicate);
						
						numMappingPatterns +=
								getVariablePredicateCount(predVarDefs, relation);
						
					// predicate is a constant
					} else {
						numMappingPatterns++;
					}
					
				}
			}
		}
		
		return numMappingPatterns;
	}


	private int getVariablePredicateCount(Collection<RestrictedExpr> varDefs,
			SqlOpBase relation) {
	
		int count = 0;
		
		String innerFromClause = " FROM ";
		
		// get table name or expression of logical table

		// querying a table
		if (relation instanceof SqlOpTable) {
			String tableName = ((SqlOpTable) relation).getTableName();
			innerFromClause += tableName;
		
		// querying a logical table based on a sub-query
		} else if (relation instanceof SqlOpQuery) {
			String subQuery = ((SqlOpQuery) relation).getQueryString();
			innerFromClause += "(" + subQuery + ")";
		} else {
			// should not happen...
			return 1;
		}
		
		// get used column
		String selectedColums = "SELECT DISTINCT ";
		
		for (RestrictedExpr predVarDef : varDefs) {
			Set<Var> columns = predVarDef.getExpr().getVarsMentioned();
			for (Var col : columns) {
				selectedColums += col.getName() + ", ";
			}
		}
		
		int selColStrLen = selectedColums.length();
		String innerQuery = selectedColums.substring(0, selColStrLen - 2)
				+ innerFromClause;
		/*
		 * quick fix:
		 * In queries like 
		 * SELECT DISTINCT sequence_id FROM (SELECT * FROM relation_members WHERE member_type = 'N')
		 * there needs to be an alias postfixed, otherwise queries like the
		 * following will result, which are invalid:
		 * SELECT COUNT(*) AS count FROM ( SELECT DISTINCT sequence_id FROM (SELECT * FROM relation_members WHERE member_type = 'N')) foo;
		 * 
		 * The quick fix for now is to check if the query ends with a closing
		 * parenthesis. If so, a dummy alias is appended.
		 */
		if (innerQuery.trim().endsWith(")")) {
			innerQuery += " bar";
		}
		
		String query = "SELECT COUNT(*) AS count FROM ( " + innerQuery +
				") foo;";
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(query);
			while (res.next()) {
				count = res.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return count;
	}
}
