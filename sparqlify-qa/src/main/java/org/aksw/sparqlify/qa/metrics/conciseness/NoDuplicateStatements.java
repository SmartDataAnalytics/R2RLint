package org.aksw.sparqlify.qa.metrics.conciseness;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DbMetric;
import org.aksw.sparqlify.qa.metrics.MappingMetric;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

public class NoDuplicateStatements extends DbMetric implements MappingMetric {

	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {

		for (ViewDefinition viewDef : viewDefs) {
			assessViewDefinition(viewDef);
		}
	}
	
	private void assessViewDefinition(ViewDefinition viewDef) throws NotImplementedException {
		String relation = readRelation(viewDef);
		
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		
		for (Quad quadPattern : viewDef.getTemplate()) {
			
			List<String> referencedColumns = readReferencedColumns(quadPattern, varDefs);
			
			String countDistinctQuery =  buildDistinctQuery(relation, referencedColumns);
			String countAllQuery = buildAllQuery(relation, referencedColumns);

			int distinctCount = -1;
			int allCount = -1;
			ResultSet res = null;
			
			try {
				res = conn.createStatement().executeQuery(countDistinctQuery);
				res.next();
				distinctCount = res.getInt("count");
				
				res = conn.createStatement().executeQuery(countAllQuery);
				res.next();
				allCount = res.getInt("count");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			int diff = allCount - distinctCount;
			
			if (diff > 0 && threshold != 0) {	
				float metricValue = 1 - (diff / (float) allCount);
				if (metricValue < threshold) {
					List<Pair<Quad, ViewDefinition>> quadViewDefs =
							new ArrayList<Pair<Quad, ViewDefinition>>();
					
					quadViewDefs.add(new Pair<Quad, ViewDefinition>(quadPattern, viewDef));
					
					writeMappingQuadMeasureToSink(metricValue, quadViewDefs);
				}
			}
		}
	}


	private List<String> readReferencedColumns(Quad quad, VarDefinition varDefs) {
		List<String> referencedColumns = new ArrayList<String>();
		
		for (Node node : Arrays.asList(quad.getSubject(), quad.getPredicate(), quad.getObject())) {
			if (node.isVariable())
				addNodeReferencedColumns((Node_Variable) node,
						referencedColumns, varDefs);
		}
		
		return referencedColumns;
	}

	private void addNodeReferencedColumns(Node_Variable node, List<String> referencedColumns, VarDefinition varDefs) {
		Collection<RestrictedExpr> tcs =
				varDefs.getDefinitions(Var.alloc(node));
		
		for (RestrictedExpr termConstructor : tcs) {
			Set<Var> vars = termConstructor.getExpr().getVarsMentioned();
			for (Var var : vars) {
				referencedColumns.add(var.getName());
			}
		}
	}

	private String buildDistinctQuery(String relation, List<String> referencedColumns) {
		String query =
			"SELECT count(*) AS count FROM (" +
				"SELECT DISTINCT " + colsString(referencedColumns) + " " +
				"FROM (" + relation + ") AS inner_query " +
				"WHERE " + colsNotNullString(referencedColumns) +
			") AS outer_query";
		
		return query;
	}


	private String buildAllQuery(String relation, List<String> referencedColumns) {
		String query =
			"SELECT count(*) AS count FROM (" +
				"SELECT " + colsString(referencedColumns) + " " +
				"FROM (" + relation + ") AS inner_query " +
				"WHERE " + colsNotNullString(referencedColumns) +
			") AS outer_query";
		
		return query;
	}


	private String colsString(List<String> columns) {
		String colsStr = "";
		for (String column : columns) {
			colsStr += column + ", ";
		}

		int strLen = colsStr.length();
		// assuming that columns will never be empty
		return colsStr.substring(0, strLen - 2);
	}


	private String colsNotNullString(List<String> columns) {
		String colsNotNullStr = "";
		
		for (String column : columns) {
			colsNotNullStr += column + " is not NULL AND ";
		}
		
		int strLen = colsNotNullStr.length();
		// assuming that columns will never be empty
		return colsNotNullStr.substring(0, strLen - 4) ;
	}


	private String readRelation(ViewDefinition viewDef) {
		SqlOp relation = viewDef.getMapping().getSqlOp();
		if (relation instanceof SqlOpTable) {
			return "SELECT * FROM " + ((SqlOpTable) relation).getTableName();
		} else if (relation instanceof SqlOpQuery) {
			return ((SqlOpQuery) relation).getQueryString();
		} else {
			// should never happen
			return "";
		}
	}
}
