package org.aksw.sparqlify.qa.metrics.relevancy;

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

/**
 * This metric should calculate the coverage of a dataset referring to the
 * covered scope. This covered scope is expressed as the number of 'instances'
 * statements are made about.
 * 'instance' is specified as follows: "The members of a class are known as
 * instances of the class." [0]
 * Another definition made there is, that "All things described by RDF are
 * called resources, and are instances of the class rdfs:Resource." [1]
 * So, as a consequence, everything is an instance and the scope, defined as
 * 
 *       number of instances
 *      ---------------------
 *        number of triples
 * 
 * would always be > 1.
 * To avoid this, a more restrictive definition of the term 'instance' would be
 * preferred here.
 * 
 * An artifact defined in the OWL specs, that is in some way similar to
 * an instance, is the term 'individual'. To be an individual a resource has to
 * have a type that is an owl:Class. Since in the most RDB2RDF real world
 * examples, I have seen, OWL wasn't used at all, calculating the scope based
 * on individuals wouldn't be very meaningful. In that sense, using the
 * individuals as instances would be too restrictive.
 * 
 * The definition of an instance used here is as follows:
 * 
 *     instance rdf:type someClass && someClass rdf:type rdfs:Class
 * 
 * i.e. an instance (as used here) is a resource, that is of a certain type that
 * is an rdfs:Class (or an owl:Class).
 * 
 * 
 * [0] http://www.w3.org/TR/rdf-schema/#ch_classes
 * [1] http://www.w3.org/TR/rdf-schema/#ch_resource
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CoverageScope extends MetricImpl implements DatasetMetric {

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		long numTriples = dataset.size();
		long numInstances = 0;
		
		Query query = QueryFactory.create(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
				
				+ "SELECT (count(*) AS ?count) { "
					+ "SELECT distinct ?s {"
						+ " { ?s a ?o . ?o a rdfs:Class }"
						+ " UNION { ?s a ?o . ?o a owl:Class }"
					+ "}"
				+ "}");
			
		QueryExecution qe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri() != null) {
			qe = QueryExecutionFactory.sparqlService(dataset.getSparqlServiceUri(), query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
		
		ResultSet res = qe.execSelect();
		while(res.hasNext())
		{
			QuerySolution resNode = res.nextSolution();
			numInstances = resNode.getLiteral("count").asLiteral().getLong();
		}
		qe.close(); 
		
		float val;
		if (numTriples == 0) val = 0;
		else val = numInstances / (float) numTriples;

		writeDatasetMeasureToSink(val);
	}
}
