package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class NoBogusInverseFunctionalPropertiesTest {

	Pinpointer pinpointer;
	BooleanTestingSink sink;


	@Before
	public void setUp() throws Exception {
		// dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
		sink = new BooleanTestingSink();
	}


	@Test
	public void test01() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/01");
		Node predicate = Node.createURI("http://ex.org/pred01");
		Node object = Node.createURI("http://ex.org/res/02");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}


	@Test
	public void test02() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/01");
		Node predicate = Node.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = Node.createLiteral("08445a31a78661b5c746feff39a9db6e4e2cc5cf");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public void test03() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/02");
		Node predicate = Node.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = Node.createLiteral("da39a33ee5e6b4b0d3255bfef95601890afd80709");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public void test04() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/02");
		Node predicate = Node.createURI("http://xmlns.com/foaf/0.1/homepage");
		Node object = Node.createURI("http://");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public void test05() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/02");
		Node predicate = Node.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = Node.createLiteral("");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public void test06() throws NotImplementedException {
		NoBogusInverseFunctionalProperties metric = new NoBogusInverseFunctionalProperties();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		Node subject = Node.createURI("http://ex.org/res/02");
		Node predicate = Node.createURI("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
		Node object = Node.createURI("http://");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}
}
