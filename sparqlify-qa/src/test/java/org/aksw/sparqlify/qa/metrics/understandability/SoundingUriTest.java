package org.aksw.sparqlify.qa.metrics.understandability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
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
public class SoundingUriTest {
	
	private String wordlistFilePath = "src/main/resources/uwords_all.txt";
	@Autowired
	BooleanTestingSink sink;
	private float threshold = (float) 0.095;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private SoundingUri metric;
	
	@Before
	public void setUp() throws Exception {
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
		metric.setWordListFilePath(wordlistFilePath);
	}

	@Test
	public synchronized void test01() throws IOException, NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.initMeasureDataSink();
		
		// not sounding
		Node subj = NodeFactory.createURI("http://ex.org/1");
		// pain threshold (sounding)
		Node pred = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		// should be ignored
		Node obj = NodeFactory.createLiteral("foo");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	@Test
	public synchronized void test02() throws IOException, NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.initMeasureDataSink();
		
		// not sounding
		Node subj = NodeFactory.createURI("http://193.239.40.138/path/23");
		// sounding
		Node pred = NodeFactory.createURI("http://ex.org/properties/hasValue");
		// should be ignored
		Node obj = NodeFactory.createLiteral("foo");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	@Test
	public synchronized void test03() throws IOException, NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.setThreshold(threshold);
		metric.initMeasureDataSink();
		
		// sounding
		Node subj = NodeFactory.createURI("http://example.org/sounding/resource/path");
		// should fail
		Node pred = NodeFactory.createURI("http://ex.org/prp/hsvl");
		// should fail
		Node obj = NodeFactory.createURI("http://193.239.40.138/path/23");
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessNodes(triple);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
