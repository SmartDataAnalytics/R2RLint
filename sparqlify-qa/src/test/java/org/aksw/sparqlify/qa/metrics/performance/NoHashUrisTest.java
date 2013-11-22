package org.aksw.sparqlify.qa.metrics.performance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class NoHashUrisTest {

	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private NoHashUris metric;


	@Before
	public void setUp() throws Exception {
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
	}


	/*
	 * no hash URIs
	 */
	@Test
	public synchronized void test01() throws NotImplementedException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * hash URI on subject position
	 */
	@Test
	public synchronized void test02() throws NotImplementedException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo#bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * hash URI on predicate position
	 */
	@Test
	public synchronized void test03() throws NotImplementedException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties#fooProp");
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * hash URI on object position
	 */
	@Test
	public synchronized void test04() throws NotImplementedException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node obj = NodeFactory.createURI("http://ex.org#Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
