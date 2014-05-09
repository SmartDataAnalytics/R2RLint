package org.aksw.sparqlify.qa.metrics.interlinking;

import java.sql.SQLException;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

@Component
public class ExternalSameAsLinks extends MetricImpl implements DatasetMetric {

	private Logger logger = LoggerFactory.getLogger(ExternalSameAsLinks.class);
	
	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		List<String> prefixes = dataset.getPrefixes();
		StmtIterator sameAsIt = dataset.listStatements(null, OWL.sameAs, (RDFNode) null);
		
		int sameAsCount = 0;
		int dbgCount = 0;
		while (sameAsIt.hasNext()) {
			dbgCount++;
			if(dbgCount % 10000 == 0) {
				logger.debug("assessed " + dbgCount + " for sameAs links");
			}
			Statement statement = sameAsIt.next();
			String subjectUri = statement.getSubject().getURI();
			
			RDFNode object = statement.getObject();
			if (!object.isURIResource()) continue;
			String objectUri = object.asResource().getURI();
			
			// if <local> owl:sameAs <external> or <external> owl:sameAs <local>
			// subject
			boolean subjectIsLocal = false;
			for (String prefix : prefixes) {
				if (subjectUri.startsWith(prefix)){
					subjectIsLocal = true;
					break;
				}
			}
			
			// object
			boolean objectIsExternal = true;
			for (String prefix : prefixes) {
				if (objectUri.startsWith(prefix)) {
					objectIsExternal = false;
					break;
				}
			}
			if ((subjectIsLocal && objectIsExternal)
					|| (!subjectIsLocal && !objectIsExternal)) {
				sameAsCount ++;
			}
		}
		
		
		long wholeCount = getWholeCount(dataset);
		float ratio;
		if (wholeCount == 0) ratio = 0;
		else ratio = sameAsCount / (float) wholeCount;
		writeDatasetMeasureToSink(ratio);
	}
	
	private long getWholeCount(SparqlifyDataset dataset) {
		long count = 0;
		Query query = QueryFactory.create("SELECT (count(*) AS ?count) { ?s ?p ?o }");
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
			count += solNode.asLiteral().getLong();
		}
		qe.close(); 
		
		return count;
	}

}
