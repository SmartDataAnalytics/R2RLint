package org.aksw.sparqlify.qa.metrics.amountofdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * This metric measures the coverage of a dataset referring to the number of
 * details that are described. This number of details is expressed with the
 * number of different properties that are used in the dataset.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CoverageDetail extends MetricImpl implements DatasetMetric {
	
	List<Node_URI> seenProperties;
	long numProperties;
	long numTriples;

	protected void clearCaches() {
		seenProperties = new ArrayList<Node_URI>();
		numProperties = 0;
		numTriples = 0;
	}

	public CoverageDetail() {
		super();
		seenProperties = new ArrayList<Node_URI>();
		numProperties = 0;
		numTriples = 0;
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		numTriples = dataset.size();
		
		Query query;
//		if (dataset.getGraphIri() != null) {
//			query = QueryFactory.create(
//					"SELECT distinct ?p { "
//					+ "GRAPH <" + dataset.getGraphIri() + "> { ?s ?p ?o }}");
//		} else {
			query = QueryFactory.create("SELECT distinct ?p { ?s ?p ?o }");
//		}
			
		QueryExecution qe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri() != null) {
			qe = QueryExecutionFactory.sparqlService(dataset.getSparqlServiceUri(), query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
		
		ResultSet res = qe.execSelect();
		
		long count = 0;
		while(res.hasNext())
		{
			QuerySolution solution = res.nextSolution();
			RDFNode solNode = solution.get("p");
			Node_URI predicate = (Node_URI) solNode.asNode();
			if (!seenProperties.contains(predicate)) {
				numProperties++;
				seenProperties.add(predicate);
			}
			count++;
			if (count%1000==0) {
				System.out.println(count);
			}
		}
		qe.close(); 
		
		float ratio;
		if (numTriples == 0) ratio = 0;
		else ratio = numProperties / (float) numTriples;
		
		writeDatasetMeasureToSink(ratio);
	}

}
