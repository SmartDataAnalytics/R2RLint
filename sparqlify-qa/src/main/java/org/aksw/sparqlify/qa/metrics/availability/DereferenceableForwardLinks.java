package org.aksw.sparqlify.qa.metrics.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;


public class DereferenceableForwardLinks extends PinpointMetric implements
		NodeMetric {

	private HashMap<String, Boolean> linkBroken;


	public DereferenceableForwardLinks() {
		linkBroken = new HashMap<String, Boolean>();
	}
	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		// FIXME: make this threaded
		
		Node subject = triple.getSubject();
		if (subject.isURI() && !((Node_URI) subject).getURI().startsWith(prefix)
				&& isBroken((Node_URI) subject)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
		}
		
		Node predicate = triple.getPredicate();
		if (!((Node_URI) predicate).getURI().startsWith(prefix)
				&& isBroken((Node_URI) predicate)) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
		}
		
		Node object = triple.getObject();
		if (object.isURI() && !((Node_URI) object).getURI().startsWith(prefix) && isBroken((Node_URI) object)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}


	private boolean isBroken(Node_URI node) {
		
		Boolean broken = linkBroken.get(node.getURI());
		
		if (broken != null) {
			return broken;
		} else {
			URL extUrl;
			try {
				// FIXME: 
				extUrl = new URL(node.getURI());
			} catch (MalformedURLException e) {
				linkBroken.put(node.getURI(), true);
				return true;
			}
			
			HttpURLConnection urlConn;
			
			try {
				urlConn = (HttpURLConnection) extUrl.openConnection();
			} catch (IOException e) {
				linkBroken.put(node.getURI(), true);
				return true;
			}
			
			try {
				urlConn.setRequestMethod("GET");
			} catch (ProtocolException e) {
				linkBroken.put(node.getURI(), true);
				return true;
			}
			
			int responseCode = 0;
			
			try {
				urlConn.connect();
				responseCode = urlConn.getResponseCode();
			} catch (IOException e) {
				linkBroken.put(node.getURI(), true);
				return true;
			}

			if (responseCode >= 200 && responseCode < 400) {
				linkBroken.put(node.getURI(), false);
				return false;
			} else {
				linkBroken.put(node.getURI(), true);
				return true;
			}
		}
	}

}
