package org.aksw.sparqlify.qa.metrics.completeness;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.DbMetric;

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
public class InterlinkingCompleteness extends DbMetric implements DatasetMetric {

	private String numInterlinksQueryStr;
	private String numSubjQueryStr;
	private String numObjQueryStr;


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
	
		numSubjQueryStr =
				"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
						
				"SELECT (COUNT(*) AS ?count) {" +
					"SELECT DISTINCT ?s {" +
						"?s ?p ?o." +
						"OPTIONAL {?i rdf:type ?s}. " +
						"FILTER (" +
							// must not be a class
							"!BOUND(?i) && " +
							// must be a local resource 
							"regex(str(?s), \"^" + dataset.getPrefix() + "\") &&" +
							// must not be a class
							"?p != rdfs:subClassOf" +
						")" +
					"}" +
				"}";
		
		numObjQueryStr =
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
						
			"SELECT (COUNT(*) AS ?count) {" +
				"SELECT DISTINCT ?o {" +
					"?s ?p ?o. " +
					"OPTIONAL{?o ?p2 ?o2}. " +
					"OPTIONAL {?i rdf:type ?o}. " +

					"FILTER(" +
						"isURI(?o) && " +  // must not be a literal
						// must not be a duplicate of an already captured subject
						"!BOUND(?o2) && " +  
						// must not be a class
						"!BOUND(?i) && " +
						// must be a local resource 
						"regex(str(?o), \"^" + dataset.getPrefix() + "\") && " +
						// must not be a class (assigned via rdf:type)
						"?p != rdf:type && " +
						// must not be a class
						"?p != rdfs:subClassOf" +
					")" +
				"}" +
			"}";
		
		numInterlinksQueryStr = 
				"Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
					
				"SELECT (COUNT(*) AS ?count) {" +
					"{" +
						// <external instance> <some predicate> <local instance>
						"?s ?p ?o." +
						"OPTIONAL { ?i rdf:type ?s }. " +
						"FILTER (" +
							// ?o must not be a literal
							"isURI(?o) && " +
							// ?s must not be a class
							"!BOUND(?i) && " +
							// ?o must not be a class
							"?p != rdf:type && " +
							"?p != rdfs:subClassOf && " +
							// ?s must be an external
							"!regex(str(?s), \"^" + dataset.getPrefix() + "\") && " +
							// ?o must be a local
							"regex(str(?o), \"^" + dataset.getPrefix() + "\") " +
						")" +
					"} UNION {" +
						// <local instance> <some predicate> <external instance>
						"?s ?p ?o. " +
						"OPTIONAL { ?i rdf:type ?s }. " +
						"FILTER(" +
							// ?o must not be a literal 
							"isURI(?o) && " +
							// ?s must not be a class
							"!BOUND(?i) && " +
							// ?o must not be a class
							"?p != rdf:type && " +
							// ?s and ?o must not be classes
							"?p != rdfs:subClassOf && " +
							// ?s must be local
							"regex(str(?s), \"^" + dataset.getPrefix() + "\") && " +
							// ?o must be external
							"!regex(str(?o), \"^" + dataset.getPrefix() + "\") " +
						")" +
					"}" +
				"}";
		
		
		int numResources = getNumResources(dataset);
		int numInterlinkedResources = getCountResult(numInterlinksQueryStr, dataset);

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
