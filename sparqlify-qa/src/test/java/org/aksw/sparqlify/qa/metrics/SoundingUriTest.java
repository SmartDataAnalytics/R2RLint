package org.aksw.sparqlify.qa.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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

public class SoundingUriTest {
	
	String wordlistFilePath = "src/main/resources/uwords_all.txt";
	BooleanTestingSink sink;
	float threshold = (float) 0.095;
	Pinpointer pinpointer;
	
	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
	}

	@Test
	public void test01() throws IOException, NotImplementedException {
		SoundingUri metric = new SoundingUri(wordlistFilePath);
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		// not sounding
		Node subj = Node.createURI("http://ex.org/1");
		// pain threshold (sounding)
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		// should be ignored
		Node obj = Node.createLiteral("foo");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	@Test
	public void test02() throws IOException, NotImplementedException {
		SoundingUri metric = new SoundingUri(wordlistFilePath);
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		// not sounding
		Node subj = Node.createURI("http://193.239.40.138/path/23");
		// sounding
		Node pred = Node.createURI("http://ex.org/properties/hasValue");
		// should be ignored
		Node obj = Node.createLiteral("foo");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	@Test
	public void test03() throws IOException, NotImplementedException {
		SoundingUri metric = new SoundingUri(wordlistFilePath);
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		// sounding
		Node subj = Node.createURI("http://example.org/sounding/resource/path");
		// should fail
		Node pred = Node.createURI("http://ex.org/prp/hsvl");
		// should fail
		Node obj = Node.createURI("http://193.239.40.138/path/23");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
