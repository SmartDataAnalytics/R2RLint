package org.aksw.sparqlify.qa.metrics.reprconciseness;

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
import com.hp.hpl.jena.graph.Triple;

@Component
public class ShortUri extends MetricImpl implements NodeMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		Node subj = triple.getSubject();
		if (subj.isURI() && resourceTooLong(subj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (resourceTooLong(pred)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && resourceTooLong(obj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}

	private boolean resourceTooLong(Node res) {
		if (res.getURI().length() >= threshold) return true;
		else return false;
	}
}