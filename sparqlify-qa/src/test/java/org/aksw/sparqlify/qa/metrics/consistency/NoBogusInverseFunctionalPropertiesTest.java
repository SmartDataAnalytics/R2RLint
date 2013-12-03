package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
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
public class NoBogusInverseFunctionalPropertiesTest {

	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private NoBogusInverseFunctionalProperties metric;


	@Before
	public void setUp() throws Exception {
		// dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer.registerViewDefs(viewDefs);
	}


	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/01");
		Node predicate = NodeFactory.createURI("http://ex.org/pred01");
		Node object = NodeFactory.createURI("http://ex.org/res/02");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}


	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/01");
		Node predicate = NodeFactory.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = NodeFactory.createLiteral("08445a31a78661b5c746feff39a9db6e4e2cc5cf");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/02");
		Node predicate = NodeFactory.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = NodeFactory.createLiteral("da39a33ee5e6b4b0d3255bfef95601890afd80709");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/02");
		Node predicate = NodeFactory.createURI("http://xmlns.com/foaf/0.1/homepage");
		Node object = NodeFactory.createURI("http://");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/02");
		Node predicate = NodeFactory.createURI("http://xmlns.com/foaf/0.1/mbox_sha1sum");
		Node object = NodeFactory.createLiteral("");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}


	@Test
	public void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subject = NodeFactory.createURI("http://ex.org/res/02");
		Node predicate = NodeFactory.createURI("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
		Node object = NodeFactory.createURI("http://");
		Triple triple = new Triple(subject, predicate, object);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}
}
