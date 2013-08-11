package org.aksw.sparqlify.qa.metrics;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class ShortUri extends PinpointMetric implements NodeMetric {

	
	@Override
	public void assessNodes(Triple triple) {
		Node subj = triple.getSubject();
		if (subj.isURI() && resourceTooLong(subj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
			
			String note1 = "subject position of " + triple.toString();
			String note2 = "";
//			for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
//				note2 += viewQuad.getQuad() + "\n"
//						+ "of the following view definition:\n"
//						+ viewQuad.getView() + "\n";
//			}
//	
//			note2 += "\n\n";
			writeToSink(0, note1, note2, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (resourceTooLong(pred)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			String note1 = "predicate position of " + triple.toString();
			String note2 = "";
			writeToSink(0, note1, note2, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && resourceTooLong(obj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			String note1 = "object position of " + triple.toString();
			String note2 = "";
			writeToSink(0, note1, note2, viewQuads);
		}
	}

	private boolean resourceTooLong(Node res) {
		if (res.getURI().length() > threshold) return true;
		else return false;
	}
}