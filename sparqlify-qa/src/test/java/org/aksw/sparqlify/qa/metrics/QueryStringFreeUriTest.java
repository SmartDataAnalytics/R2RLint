package org.aksw.sparqlify.qa.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class QueryStringFreeUriTest {

	BooleanTestingSink sink;
	Pinpointer pinpointer;


	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
	}


	@Test
	public void test01() throws NotImplementedException {
		QueryStringFreeUri metric = new QueryStringFreeUri();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo?bar");
		Node pred = Node.createURI("http://ex.org/foo");
		Node obj = Node.createURI("http://ex.org/foo/?bar");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.written(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.written(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.written(metricName, TriplePosition.OBJECT));
	}


	@Test
	public void test02() throws NotImplementedException {
		QueryStringFreeUri metric = new QueryStringFreeUri();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo?bar#baz");
		Node pred = Node.createURI("http://ex.org/foo/?bar#baz");
		Node obj = Node.createURI("http://ex.org/foo/#bar");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.written(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.written(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.written(metricName, TriplePosition.OBJECT));
	}


	@Test
	public void test03() throws NotImplementedException {
		QueryStringFreeUri metric = new QueryStringFreeUri();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo#bar?baz");
		Node pred = Node.createURI("http://ex.org/foo/#bar?baz");
		Node obj = Node.createURI("http://ex.org/foo/bar?");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.written(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.written(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.written(metricName, TriplePosition.OBJECT));
	}
}
