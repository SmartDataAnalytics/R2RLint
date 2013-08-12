package org.aksw.sparqlify.qa.metrics;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class ShortUri extends PinpointMetric implements NodeMetric {

	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		Node subj = triple.getSubject();
		if (subj.isURI() && resourceTooLong(subj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
			
			writeToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (resourceTooLong(pred)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && resourceTooLong(obj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}

	private boolean resourceTooLong(Node res) {
		if (res.getURI().length() > threshold) return true;
		else return false;
	}
}