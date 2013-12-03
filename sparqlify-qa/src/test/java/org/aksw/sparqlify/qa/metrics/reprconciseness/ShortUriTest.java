package org.aksw.sparqlify.qa.metrics.reprconciseness;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.reprconciseness.ShortUri;
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
public class ShortUriTest {

	@Autowired
	private BooleanTestingSink sink;
	private float threshold = (float) 50;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private ShortUri metric;
	
	@Before
	public void setUp() throws Exception {
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
		metric.setThreshold(threshold);
	}

	@Test
	public synchronized void testAssessNodes1() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
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
	public synchronized void testAssessNodes2() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
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
