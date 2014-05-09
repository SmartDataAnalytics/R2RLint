package org.aksw.sparqlify.qa.metrics.availability;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;

@Component
public class DereferenceableUris extends MetricImpl implements
		NodeMetric {

	private static Logger logger = LoggerFactory.getLogger(DereferenceableUris.class);
	private HashMap<String, Boolean> linkBroken;

	@Autowired
	Pinpointer pinpointer;


	public DereferenceableUris() {
		linkBroken = new HashMap<String, Boolean>();
	}


	@Override
	public void assessNodes(Triple triple) throws NotImplementedException, SQLException {
		// FIXME: make this threaded
		logger.debug("assessing " + triple);
		
		// check subject, if local and not a blank node
		Node subject = triple.getSubject();
		if (subject.isURI()){
			boolean isLocal = false;
			// check all local prefixes
			for (String prefix : prefixes) {
				if (((Node_URI) subject).getURI().startsWith(prefix)) {
					isLocal = true;
					break;
				}
			}
			
			if (!isLocal && isBroken((Node_URI) subject)) {
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT, triple, viewQuads);
			}
		}
		
		// check predicate if local
		Node predicate = triple.getPredicate();
		{
			boolean isLocal = false;
			
			// check all local prefixes
			for (String prefix : prefixes) {
				if (((Node_URI) predicate).getURI().startsWith(prefix)) {
					isLocal = true;
					break;
				}
			}
			if (!isLocal && isBroken((Node_URI) predicate)) {
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE, triple, viewQuads);
			}
		}
		
		// check object if URI and local
		Node object = triple.getObject();
		
		if (object.isURI()) {
			boolean isLocal = false;
			// check all local prefixes
			for (String prefix : prefixes) {
				if (((Node_URI) object).getURI().startsWith(prefix)) {
					isLocal = true;
				}
			}
			if (!isLocal && isBroken((Node_URI) object)) {
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
			}
		}
	}


	private boolean isBroken(Node_URI node) {
		
		Boolean broken = linkBroken.get(node.getURI());
		if (broken != null) {
			return broken;
		} else {
			URL extUrl;
			try {
				extUrl = new URL(node.getURI());
				logger.debug("Trying to retrieve " + extUrl);
			} catch (MalformedURLException e) {
				linkBroken.put(node.getURI(), true);
				return true;
			}
			
			HttpURLConnection urlConn;
			
			try {
				urlConn = (HttpURLConnection) extUrl.openConnection();
			} catch (IOException e) {
				linkBroken.put(node.getURI(), true);
				logger.debug("Got IO Exception");
				logger.debug("###### Done #####");
				return true;
			} catch (Exception e) {
				// added to handle
				// Exception in thread "main" java.lang.ClassCastException: sun.net.www.protocol.file.FileURLConnection cannot be cast to java.net.HttpURLConnection
				// for URIs like file:/x/y/z
				linkBroken.put(node.getURI(), true);
				logger.debug("Got general Exception");
				logger.debug("###### Done #####");
				return true;
			}
			
			try {
				urlConn.setRequestMethod("HEAD");
			} catch (ProtocolException e) {
				linkBroken.put(node.getURI(), true);
				logger.debug("Got protocol error");
				logger.debug("###### Done #####");
				return true;
			}
			
			int responseCode = 0;
			
			try {
				urlConn.connect();
				responseCode = urlConn.getResponseCode();
			} catch (IOException e) {
				linkBroken.put(node.getURI(), true);
				logger.debug("Could not get response Code");
				logger.debug("###### Done #####");
				return true;
			}

			if (responseCode >= 200 && responseCode < 400) {
				linkBroken.put(node.getURI(), false);
				logger.debug("-- SUCCESS --");
				logger.debug("###### Done #####");
				return false;
			} else {
				linkBroken.put(node.getURI(), true);
				logger.debug("Bad response code");
				logger.debug("###### Done #####");
				return true;
			}
		}
	}

}
