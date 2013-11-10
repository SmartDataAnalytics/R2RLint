package org.aksw.sparqlify.qa.metrics.completeness;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * This metric measures the property completeness giving a score of the
 * completeness of the available values of a property. In the RDB2RDF case this
 * means the ratio of the number of property values to the number of attribute
 * values in the considered (logical) table.
 * To be totally accurate one would have to use the number of *distinct* values
 * (of a considered property and the corresponding attribute). But since this
 * would mean that the property would have to be computed per attribute used
 * in the mapping things would become more complex. So this metric can be
 * considered as an approximation of the per attribute property completeness.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class PropertyCompleteness extends MetricImpl implements MappingMetric {
	
	@Autowired
	private DataSource rdb;
	private Connection conn;
	
	private final String whereStr = "where";
	
	@PostConstruct
	public void init() throws SQLException {
		conn = rdb.getConnection();
	}

	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {
		for (ViewDefinition viewDef : viewDefs) {
			SqlOpBase logicalTbl = (SqlOpBase) viewDef.getMapping().getSqlOp();
			List<Pair<Quad, ViewDefinition>> quadViewDefs =
					new ArrayList<Pair<Quad, ViewDefinition>>();
			
			if (logicalTbl instanceof SqlOpTable) {
				/*
				 * The whole table is considered which means a maximal
				 * completeness.  
				 */
				if (threshold == 0) {
					quadViewDefs.add(new Pair<Quad, ViewDefinition>(null, viewDef));
					writeMappingQuadMeasureToSink(1, quadViewDefs);
				}

			} else if (logicalTbl instanceof SqlOpQuery) {
				float completeness = getTupleCompleteness((SqlOpQuery) logicalTbl);
				
				if (threshold == 0) {
					quadViewDefs.add(new Pair<Quad, ViewDefinition>(null, viewDef));
					
					writeMappingQuadMeasureToSink(completeness, quadViewDefs);
					
				} else if (completeness < threshold) {
					quadViewDefs.add(new Pair<Quad, ViewDefinition>(null, viewDef));
					writeMappingQuadMeasureToSink(completeness, quadViewDefs);
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
		 *   - ending at either the next outer closing parenthesis ')' or at the
		 *     string end
		 */
		
		/*
		 * Determining the next outer parenthesis:
		 * - start at the WHERE position and iterate over the characters
		 *   following
		 * - a parenthesis counter is used, initialized with 0 
		 * - if a '(' is found the parenthesis counter is incremented
		 * - if a ')' is found the parenthesis counter is decremented
		 * - if the parenthesis counter becomes a negative number, we're under
		 *   the outer closing parenthesis
		 * - if the parenthesis counter didn't become negative, there is no
		 *   outer closing parenthesis and the WHERE expression ends with the
		 *   string end
		 */
		int parCounter = 0;
		for (int i=firstWhereIndex; i<query.length(); i++) {
			if (query.charAt(i) == '(') parCounter++;
			else if(query.charAt(i) == ')') parCounter--;
			else continue;
			
			if (parCounter<0) {
				String queryWoCurrentWhereClause =
						query.substring(0, firstWhereIndex-1) +
						query.substring(i);
				return buildUnrestrictedQuery(queryWoCurrentWhereClause);
			}
		}
		return query.substring(0, firstWhereIndex-1);
	}


	private int getNumTuples(String query) {
		String countQuery = "SELECT COUNT(*) as count FROM (" + query + ") foo";
		ResultSet res = null;
		try {
			res = conn.createStatement().executeQuery(countQuery);
		} catch (SQLException e) {
			System.out.println(countQuery);
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
