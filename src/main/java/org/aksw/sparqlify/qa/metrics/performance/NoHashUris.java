package org.aksw.sparqlify.qa.metrics.performance;

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
public class NoHashUris extends MetricImpl implements NodeMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException,
			SQLException {
		Node subject = triple.getSubject();
		Set<ViewQuad<ViewDefinition>> viewQuads = null;
		
		if (subject.isURI() && isHashUri((Node_URI) subject)) {
			viewQuads = pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		Node predicate = triple.getPredicate();
		if (isHashUri((Node_URI) predicate)) {
			if (viewQuads == null) viewQuads = pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}
		
		Node object = triple.getObject();
		if (object.isURI() && isHashUri((Node_URI) object)) {
			if (viewQuads == null) viewQuads = pinpointer.getViewCandidates(triple);
			
			writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}


	private boolean isHashUri(Node_URI node) {
		if (node.getURI().indexOf("#") > -1) return true;
		else return false;
	}
}
