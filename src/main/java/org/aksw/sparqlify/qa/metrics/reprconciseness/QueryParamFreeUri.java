package org.aksw.sparqlify.qa.metrics.reprconciseness;

import java.sql.SQLException;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;

@Component
public class QueryParamFreeUri extends MetricImpl implements NodeMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException, SQLException {
		/* assess subject */
		Node subj = triple.getSubject();
		
		if (subj.isURI() && hasQueryString((Node_URI) subj)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		/* assess predicate */
		Node pred = triple.getPredicate();
		
		if (hasQueryString((Node_URI) pred)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}

		/* assess object */
		Node obj = triple.getObject();
		
		if (obj.isURI() && hasQueryString((Node_URI) obj)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}
	
	private boolean hasQueryString(Node_URI resource) {
		String uri = resource.getURI();
		int qMarkIndex = uri.indexOf("?");
		int hashTagIndex = uri.indexOf("#");
		if (qMarkIndex > -1 && (hashTagIndex == -1 || qMarkIndex < hashTagIndex))
			return true;
		else return false;
	}

}
