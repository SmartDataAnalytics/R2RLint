package org.aksw.sparqlify.qa.metrics.completeness;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointDbMetric;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * The population completeness metric for LOD sources should measure the ratio
 * between the number of real-world objects are represented in the data source
 * and the total number of real-world objects.
 * 
 * In the RDB2RDF case the 'real-world' objects are all entities represented in
 * the underlying database tables. So to measure the RDB2RDF population
 * completeness one first has to count
 * - all primary key values
 * - but only if they are not consisting entirely of foreign keys .
 * 
 * So m:n relation tables would not be counted since (at least
 * normally) these have a compound primary key consisting of foreign keys.
 * 
 * On the RDF side all entities should be counted that are no classes or
 * properties, which means
 * - resources on predicate position are not considered
 * - resources on object position are not considered if it is assigned via an
 *   rdf:type predicate
 * - all remaining (non-literal) resources are counted
 * 
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class PopulationCompleteness extends PinpointDbMetric implements
		DatasetMetric {

	private final String numSubjQueryStr =
			"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			
			"SELECT (COUNT(*) AS ?count) {" +
				"SELECT DISTINCT ?s {" +
					"?s ?p ?o." +
				"}" +
			"}";
	
	private final String numObjQueryStr =
			"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			
			"SELECT (COUNT(*) AS ?count) {" +
				"SELECT DISTINCT ?o {" +
					"?s ?p ?o. " +
					"OPTIONAL{?o ?p2 ?o2}. " +
					"FILTER(isURI(?o) && !BOUND(?o2) && !isBlank(?o) && ?p != rdf:type)" +
				"}" +
			"}";


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
	
		int numDbEntities = getNumDbEntities(); 
		int numResources = getNumResources(dataset);
		float ratio = (float) numResources / (float) numDbEntities;
		
		if (threshold == 0 || ratio < threshold) {
			writeDatasetMeasureToDisk(ratio);
		}
	}


	private int getNumDbEntities() {
		int numEntities = 0;
		List<String> tableNames = getTableNames();
		
		for (String tableName : tableNames) {
			numEntities += getNumTableEntities(tableName);
		}
		return numEntities;
	}


	private int getNumTableEntities(String tableName) {
		int numEntities = 0;
		
		String query = "SELECT count(*) AS count FROM " + tableName + ";";
		
		try {
			Statement stmt = conn.createStatement();
			java.sql.ResultSet res = stmt.executeQuery(query);
			while (res.next()) {
				numEntities = res.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return numEntities;
	}

	/**
	 * This method finds all table names that contain actual entities. Tables
	 * that do not fall in this category are tables used to express N:M
	 * relations, since these do not contain entities but express relations
	 * between entities.
	 * The characteristic feature used for the distinction are primary and
	 * foreign keys: in M:N tables the primary keys consist entirely of foreign
	 * keys, e.g. (*attr* --> primary key):
	 * A               M:N                  B
	 * 
	 *  *id* | ...      *A.id* | *B.id*      *id* | ...
	 * ------+-----    --------+--------    ----- +-----
	 *       |                 |                  |
	 * 
	 * If the primary key of such an M:N table consists not entirely of foreign
	 * keys, the corresponding tuples can be considered as entities, e.g. in the
	 * scenario of a library:
	 * 
	 * Reader:          Lending:                    Book:
	 * 
	 *  *rid* | ...      *rid* | *bid* | *date*      *bid* | ...
	 * -------+-----    -------+-------+--------    -------+-----
	 *        |                |       |                   |
	 * 
	 * So the actual lendings could be modeled as entities in the LOD world.
	 * 
	 * @return a list of table names that are considered as entity tables, not
	 * 		just M:N relation tables
	 */
	private List<String> getTableNames() {
		
		List<String> names = new ArrayList<String>();
		
		try {
			DatabaseMetaData meta = conn.getMetaData();
			java.sql.ResultSet tablesRes = meta.getTables(null, null, "%",
					new String[] { "TABLE" });
			
			// iterate over table names
			while (tablesRes.next()) {
				
				List<String> primaryKeys = new ArrayList<String>();
				
				String tableName = tablesRes.getString(3);
				
				// query primary keys
				java.sql.ResultSet primaryKeysRes =
						meta.getPrimaryKeys(conn.getCatalog(), null, tableName);
				
				// fill the primary key list
				while (primaryKeysRes.next()) {
					primaryKeys.add(primaryKeysRes.getString(4));
				}
				
				// query foreign keys
				java.sql.ResultSet foreignKeysRes =
						meta.getImportedKeys(conn.getCatalog(), null, tableName);
				
				// pop all primary keys, that are also foreign keys
				while (foreignKeysRes.next()) {
					String fKeyName = foreignKeysRes.getString(8);
					
					if (primaryKeys.contains(fKeyName)) {
						primaryKeys.remove(fKeyName);
					}
				}
				
				// check if there are remaining non-foreign primary keys
				// --> yes: table is considered to hold entities
				// --> no: table is considered to be a pure M:N table
				if (!primaryKeys.isEmpty()) names.add(tableName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return names;
	}


	private int getNumResources(SparqlifyDataset dataset) {
		int numResources = 0;
		
		numResources += getCountResult(numSubjQueryStr, dataset);
		numResources += getCountResult(numObjQueryStr, dataset);
		
		return numResources;
	}


	private int getCountResult(String queryStr, SparqlifyDataset dataset) {
		int count = 0;
		Query query = QueryFactory.create(queryStr);
		
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		ResultSet res = qe.execSelect();
		
		while(res.hasNext())
		{
			QuerySolution solution = res.nextSolution();
			RDFNode solNode = solution.get("count");
			count += solNode.asLiteral().getInt();
		}
		qe.close(); 
		
		return count;
	}
}
