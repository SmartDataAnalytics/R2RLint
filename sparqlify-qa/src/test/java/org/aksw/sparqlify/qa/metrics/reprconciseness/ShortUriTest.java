package org.aksw.sparqlify.qa.metrics.reprconciseness;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.reprconciseness.ShortUri;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

public class ShortUriTest {

	BooleanTestingSink sink;
	float threshold = (float) 50;
	Pinpointer pinpointer;
	
	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
	}

	@Test
	public void testAssessNodes1() throws NotImplementedException {
		ShortUri metric = new ShortUri();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		// too long
		Node subj = NodeFactory.createURI("http://example.org/too/long/resource/identifier/aaa");
		// too long
		Node pred = NodeFactory.createURI("http://example.org/too/long/resource/identifier/aa");
		// should be ignored
		Node obj = NodeFactory.createLiteral("foo");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	@Test
	public void testAssessNodes2() throws NotImplementedException {
		ShortUri metric = new ShortUri();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		// should be ignored
		Node subj = NodeFactory.createAnon();
		// too long
		Node pred = NodeFactory.createURI("http://example.org/too/long/resource/identifier/aa");
		// not too long
		Node obj = NodeFactory.createURI("http://example.org/not/too/long/resource/id/aaaaa");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
