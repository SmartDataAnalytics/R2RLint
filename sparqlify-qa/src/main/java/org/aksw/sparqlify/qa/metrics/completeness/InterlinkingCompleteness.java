package org.aksw.sparqlify.qa.metrics.completeness;

import java.sql.SQLException;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * This metric measures the interlinking completeness. Since any resource of a
 * dataset can be interlinked with another resource of a foreign dataset this
 * metric makes a statement about the ratio of interlinked resources to
 * resources that could potentially be interlinked.
 * 
 * An interlink here is assumed to be a statement like
 * 
 *   <local resource> <some predicate> <external resource>
 * 
 * or
 * 
 *   <external resource> <some predicate> <local resource>
 * 
 * Local resources are those that share the same URI prefix of the considered
 * dataset, external resources are those that don't.
 * 
 * Zaveri et. al [http://www.semantic-web-journal.net/system/files/swj414.pdf]
 * further restrict the interlinking completeness metric to consider only
 * instance resources.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class InterlinkingCompleteness extends MetricImpl implements DatasetMetric {


	private String numInterlinkedResQueryStr;
	private String numSubjQueryStr;
	private String numObjQueryStr;


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		/*
		 *  build string to check if a resource ?r is local
		 */
		String localPrefixesRegexTest =
				"  ( ";
		for (String prefix : dataset.getPrefixes()) {
			localPrefixesRegexTest +=
						"regex(str(?r), \"^" + prefix + "\") || ";
		}
		// strip off the last four characters: either ' || ' or '  ( '
		localPrefixesRegexTest = localPrefixesRegexTest.substring(0, localPrefixesRegexTest.length() - 4);
		// close parenthesis (if there were any local prefixes to consider)
		if (dataset.getPrefixes().size() > 0) {
			localPrefixesRegexTest +=
				"  ) && ";
		}
		
		/*
		 *  build string to check if resource ?n is external
		 */
		String notLocalPrefixesRegexTest = 
				"  ( ";
		for (String prefix : dataset.getPrefixes()) {
			notLocalPrefixesRegexTest +=
						"!regex(str(?n), \"^" + prefix + "\") && ";
		}
		// strip off the last four characters: either '  ( ' or ' && '
		notLocalPrefixesRegexTest = notLocalPrefixesRegexTest.substring(0, notLocalPrefixesRegexTest.length() - 4);
		if (dataset.getPrefixes().size() > 0) {
			notLocalPrefixesRegexTest +=
				"  ) && ";
		}
		
		
		numSubjQueryStr =
			"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
					
			"SELECT (COUNT(*) AS ?count) {" +
				"SELECT DISTINCT ?r {" +
					"?r ?p ?o. " +
					"OPTIONAL {?i rdf:type ?r}. " +
					"FILTER (" +
						// must not be a class
						"!BOUND(?i) && " +
						
						// must be a local resource 
						localPrefixesRegexTest +
						
						// must not be a class
						" ?p != rdfs:subClassOf " +
					")" +
				"}" +
			"}";
		
		numObjQueryStr =
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
						
			"SELECT (COUNT(*) AS ?count) { " +
				"SELECT DISTINCT ?r {" +
					"?s ?p ?r. " +
					"OPTIONAL{?r ?p2 ?o}. " +
					"OPTIONAL {?i rdf:type ?r}. " +

					"FILTER(" +
						// must not be a literal
						"isURI(?r) && " +
					
						// must not be a duplicate of an already captured subject
						"!BOUND(?o) && " +  
						
						// must not be a class
						"!BOUND(?i) && " +
						
						// must be a local resource
						localPrefixesRegexTest +
						
						// must not be a class (assigned via rdf:type)
						"?p != rdf:type && " +
						
						// must not be a class
						"?p != rdfs:subClassOf" +
					")" +
				"}" +
			"}";
		
		numInterlinkedResQueryStr = 
			"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				
			"SELECT (COUNT(*) AS ?count) { " +
				"SELECT DISTINCT ?n { " +
					"{" +
						// <external instance> <some predicate> <local instance>
						"?n ?p ?r. " +
						"OPTIONAL { ?i rdf:type ?n }. " +
						"FILTER (" +
							// ?r must not be a literal
							"isURI(?r) && " +
							
							// ?n must not be a class
							"!BOUND(?i) && " +
							
							// ?r must not be a class
							"?p != rdf:type && " +
							
							// ?e must be external
							notLocalPrefixesRegexTest +
							
							// ?r must be a local
							localPrefixesRegexTest +
							
							// ?n and ?r must not be classes
							"?p != rdfs:subClassOf " +
							
						")" +
					"} UNION {" +
						// <local instance> <some predicate> <external instance>
						"?r ?p ?n. " +
						"OPTIONAL { ?i rdf:type ?r }. " +
						"FILTER (" +
							// ?n must not be a literal 
							"isURI(?n) && " +
							
							// ?r must not be a class
							"!BOUND(?i) && " +
							
							// ?n must not be a class
							"?p != rdf:type && " +
							
							// ?r must be local
							localPrefixesRegexTest +
							
							// ?n must be external
							notLocalPrefixesRegexTest +
							
							// ?r and ?n must not be classes
							"?p != rdfs:subClassOf " +
						")" +
					"} " +
				"} " +
			"}";
		
		
		int numResources = getNumResources(dataset);
		int numInterlinkedResources = getCountResult(numInterlinkedResQueryStr, dataset);

		float value = (float) numInterlinkedResources / (float) numResources;
		if (threshold == 0 || value < threshold ) {
			writeDatasetMeasureToSink(value);
		}
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
		QueryExecution qe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri() != null) {
			qe = QueryExecutionFactory.sparqlService(dataset.getSparqlServiceUri(), query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
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
