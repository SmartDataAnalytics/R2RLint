package org.aksw.sparqlify.qa.metrics.reprconciseness;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.reprconciseness.QueryParamFreeUri;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

public class QueryParamFreeUriTest {

	BooleanTestingSink sink;
	Pinpointer pinpointer;


	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
	}


	@Test
	public void test01() throws NotImplementedException {
		QueryParamFreeUri metric = new QueryParamFreeUri();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo?bar");
		Node pred = NodeFactory.createURI("http://ex.org/foo");
		Node obj = NodeFactory.createURI("http://ex.org/foo/?bar");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	@Test
	public void test02() throws NotImplementedException {
		QueryParamFreeUri metric = new QueryParamFreeUri();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo?bar#baz");
		Node pred = NodeFactory.createURI("http://ex.org/foo/?bar#baz");
		Node obj = NodeFactory.createURI("http://ex.org/foo/#bar");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	@Test
	public void test03() throws NotImplementedException {
		QueryParamFreeUri metric = new QueryParamFreeUri();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = NodeFactory.createURI("http://ex.org/foo#bar?baz");
		Node pred = NodeFactory.createURI("http://ex.org/foo/#bar?baz");
		Node obj = NodeFactory.createURI("http://ex.org/foo/bar?");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
