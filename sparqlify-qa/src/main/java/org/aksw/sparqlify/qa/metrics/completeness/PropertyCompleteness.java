package org.aksw.sparqlify.qa.metrics.completeness;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DbMetric;
import org.aksw.sparqlify.qa.metrics.MappingMetric;

public class PropertyCompleteness extends DbMetric implements MappingMetric {
	
	private final String whereStr = "where";

	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {
		for (ViewDefinition viewDef : viewDefs) {
			SqlOpBase logicalTbl = (SqlOpBase) viewDef.getMapping().getSqlOp();
			
			if (logicalTbl instanceof SqlOpTable) {
				/*
				 * The whole table is considered which means a maximal
				 * completeness.  
				 */
				if (threshold == 0) writeMappingMeasureToDisk(1, viewDef);

			} else if (logicalTbl instanceof SqlOpQuery) {
				float completeness = getTupleCompleteness((SqlOpQuery) logicalTbl);
				if (threshold == 0) {
					writeMappingMeasureToDisk(completeness, viewDef);
				} else if (completeness < threshold) {
					writeMappingMeasureToDisk(completeness, viewDef);
				}
			} else {
				// should not happen...
			}
		}
	}


	private float getTupleCompleteness(SqlOpQuery query) {
		final String queryStr = query.getQueryString().toLowerCase();
		int whereIndex = queryStr.indexOf(whereStr);
		
		// query contains no WHERE clause which means that there is no
		// restriction for the number of retrievable tuples which in turn means
		// that the completeness equals 1
		if (whereIndex == -1) return 1;
		
		// there is a WHERE clause --> completeness has to be calculated as
		// no of tuples with WHERE clause / no of tuples without WHERE clause
		else {
			String unrestrictedQueryStr = buildUnrestrictedQuery(queryStr);
			int numTuplesUnrestricted = getNumTuples(unrestrictedQueryStr);
			int numTuplesRestricted = getNumTuples(queryStr);
			
			return (float) numTuplesRestricted/(float) numTuplesUnrestricted;
		}
	}


	private String buildUnrestrictedQuery(String query) {
		// SELECT foo FROM bar WHERE baz=true;
		// SELECT foo FROM (SELECT foo, b FROM c WHERE d>23) AS bar WHERE b<42;
		int firstWhereIndex = query.indexOf(whereStr);
		
		// query contains no WHERE clause anymore
		if (firstWhereIndex == -1) return query;
		
		/*
		 * Assumptions:
		 * - there is at least one WHERE clause
		 * - the current WHERE restriction is the sub-string
		 *   - starting at firstWhereIndex
		 *   - ending at either the next closing brace ')' or at the string end 
		 */
		int nextClosingBraceindex = query.indexOf(")", firstWhereIndex);
		
		if (nextClosingBraceindex < 0 ) {
			// the WHERE restriction ends with the string end
			return query.substring(0, firstWhereIndex-1);
		} else {
			// the WHERE restriction ends with the next closing brace
			String queryWoCurrentWhereClause =
					query.substring(0, firstWhereIndex-1) +
					query.substring(nextClosingBraceindex);
			return buildUnrestrictedQuery(queryWoCurrentWhereClause);
		}
	}


	private int getNumTuples(String query) {
		String countQuery = "SELECT COUNT(*) as count FROM (" + query + ") foo";
		ResultSet res = null;
		try {
			res = conn.createStatement().executeQuery(countQuery);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int numTuples = 0;
		try {
			while (res.next()) {
				numTuples = res.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return numTuples;
	}
}
