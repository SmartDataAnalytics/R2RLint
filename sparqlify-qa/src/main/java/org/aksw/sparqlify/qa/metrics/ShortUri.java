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
			writeBack("subject", triple, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (resourceTooLong(pred)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("subject", triple, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && resourceTooLong(obj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("subject", triple, viewQuads);
		}
	}

	private boolean resourceTooLong(Node res) {
		if (res.getURI().length() > 50) return true;
		else return false;
	}

	
	private void writeBack(String position, Triple triple,
			Set<ViewQuad<ViewDefinition>> viewQuads) {
		
		// FIXME: just a debug dummy
//		String reason = "";
//		for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
//			reason += viewQuad.getQuad() + "\n"
//					+ "of the following view definition:\n"
//					+ viewQuad.getView() + "\n";
//		}
//
//		reason += "\n\n";
		MeasureDatum datum = new MeasureDatum(parentDimension, name, 0,
				position + " position of " + triple.toString(), ""); // reason);
		sink.write(datum);
	}
}