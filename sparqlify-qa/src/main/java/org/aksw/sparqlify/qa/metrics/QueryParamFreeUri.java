package org.aksw.sparqlify.qa.metrics;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;

public class QueryParamFreeUri extends PinpointMetric implements NodeMetric {

	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		/* assess subject */
		Node subj = triple.getSubject();
		
		if (subj.isURI() && hasQueryString((Node_URI) subj)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		/* assess predicate */
		Node pred = triple.getPredicate();
		
		if (hasQueryString((Node_URI) pred)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}

		/* assess object */
		Node obj = triple.getObject();
		
		if (obj.isURI() && hasQueryString((Node_URI) obj)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
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
