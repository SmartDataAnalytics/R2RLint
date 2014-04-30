package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
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

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * TODO: This tests do not cover the case where the Jena reasoner adds
 * deprecation statements given in an external vocabulary/ontology. The problem
 * of testing this currently is that there are no deprecated classes or
 * properties in OWL 1.0 and it seems, that OWL 2 is not used by the reasoner.
 * So I don't know any standard vocabulary that is "built in" and has
 * deprecated classes or properties.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class NoDeprecatedClassesOrPropertiesTest {
	
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private NoDeprecatedClassesOrProperties metric;

	@Before
	public void setUp() throws Exception {
		// dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer.registerViewDefs(viewDefs);
	}


	/*
	 * no deprecated classes
	 */
	private SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <"+ RDF.type.getURI() +"> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated class declared via rdf:type but not used
	 */
	private SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	
	/*
	 * deprecated class declared via rdf:type, used on subject position
	 */
	private SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/Cls01> <" + RDFS.label.getURI() + "> \"deprecated class\" . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	
	/*
	 * deprecated class assigned via rdf:type, used on object position
	 */
	private SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class declared via owl:equivalentClass (subject), but not used 
	 */
	private SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Cls01> <" + OWL.equivalentClass.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	
	/*
	 * deprecated class declared via owl:equivalentClass (subject), used on
	 * subject position 
	 */
	private SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Cls01> <" + OWL.equivalentClass.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/Cls01> <" + RDFS.label.getURI() + "> \"deprecated class\" . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	
	
	/*
	 * deprecated class declared via owl:equivalentClass (subject), used on
	 * object position
	 */
	private SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Cls01> <" + OWL.equivalentClass.getURI() + "> <" + OWL.DeprecatedClass.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class declared via owl:equivalentClass (object), but not used
	 */
	private SparqlifyDataset dataset08() {
		String content = 
			"<" + OWL.DeprecatedClass.getURI() + "> <" + OWL.equivalentClass.getURI() + "> <http://ex.org/Cls01> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	
	/*
	 * deprecated class declared via owl:equivalentClass (object), used on 
	 * subject position
	 */
	private SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<" + OWL.DeprecatedClass.getURI() + "> <" + OWL.equivalentClass.getURI() + "> <http://ex.org/Cls01> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset09();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	
	/*
	 * deprecated class declared via owl:equivalentClass (object), used on
	 * object position
	 */
	private SparqlifyDataset dataset10() {
		String content = 
			"<" + OWL.DeprecatedClass.getURI() + "> <" + OWL.equivalentClass.getURI() + "> <http://ex.org/Cls01> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	
	/*
	 * deprecated property declared via rdf:type, but not used 
	 */
	private SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset11();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated property declared via rdf:type, used on subject position
	 */
	private SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDFS.label.getURI() + "> \"deprecated property\" . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test12() throws NotImplementedException, SQLException {
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset12();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via rdf:type, used on predicate position
	 */
	private SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test13() throws NotImplementedException, SQLException {
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset13();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via rdf:type; used on object position
	 */
	private SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " +
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test14() throws NotImplementedException, SQLException {
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset14();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via owl:equivalentProperty (subject), but
	 * not used
	 */
	private SparqlifyDataset dataset15() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + OWL.equivalentProperty.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test15() throws NotImplementedException, SQLException {
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset15();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	
	/*
	 * deprecated property declared via owl:equivalentProperty (subject), used
	 * on subject position
	 */
	private SparqlifyDataset dataset16() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDFS.label.getURI() + "> \"deprecated property\" . " +
			"<http://ex.org/pred01> <" + OWL.equivalentProperty.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test16() throws NotImplementedException, SQLException {
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset16();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated property declared via owl:equivalentProperty (subject), used
	 * on predicate position
	 */
	private SparqlifyDataset dataset17() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + OWL.equivalentProperty.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test17() throws NotImplementedException, SQLException {
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset17();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via owl:equivalentProperty (subject), used
	 * on object position
	 */
	private SparqlifyDataset dataset18() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + OWL.equivalentProperty.getURI() + "> <" + OWL.DeprecatedProperty.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " +
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test18() throws NotImplementedException, SQLException {
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset18();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via owl:equivalentProperty (object), but
	 * not used
	 */
	private SparqlifyDataset dataset19() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<" + OWL.DeprecatedProperty.getURI() + "> <" + OWL.equivalentProperty.getURI() + "> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test19() throws NotImplementedException, SQLException {
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset19();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	
	/*
	 * deprecated property declared via owl:equivalentProperty (object), used
	 * on subject position
	 */
	private SparqlifyDataset dataset20() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<" + OWL.DeprecatedProperty.getURI() + "> <" + OWL.equivalentProperty.getURI() + "> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/pred01> <" + RDFS.range.getURI() + "> <"+ XSD.xstring.getURI() + "> . " +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test20() throws NotImplementedException, SQLException {
		String metricName = "test20";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset20();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property declared via owl:equivalentProperty (object)), used
	 * on predicate position
	 */
	private SparqlifyDataset dataset21() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<" + OWL.DeprecatedProperty.getURI() + "> <" + OWL.equivalentProperty.getURI() + "> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test21() throws NotImplementedException, SQLException {
		String metricName = "test21";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset21();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated property declared via owl:equivalentProperty (object)), used
	 * on object position
	 */
	private SparqlifyDataset dataset22() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<" + OWL.DeprecatedProperty.getURI() + "> <" + OWL.equivalentProperty.getURI() + "> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " + 
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test22() throws NotImplementedException, SQLException {
		String metricName = "test22";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset22();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated resource declared via owl:deprecated "true"^^xsd:boolean,
	 * not used
	 */
	private SparqlifyDataset dataset23() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDFS.range.getURI() + "> <" + XSD.xstring.getURI() + "> . " +
			"<http://ex.org/res/01> <" + OWL2.deprecated.getURI() + "> \"true\"^^<" + XSD.xboolean.getURI() + "> ." +
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test23() throws NotImplementedException, SQLException {
		String metricName = "test23";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset23();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated resource declared via owl:deprecated "true"^^xsd:boolean,
	 * used on subject position
	 */
	private SparqlifyDataset dataset24() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDFS.range.getURI() + "> <" + XSD.xstring.getURI() + "> . " +
			"<http://ex.org/res/01> <" + OWL2.deprecated.getURI() + "> \"true\"^^<" + XSD.xboolean.getURI() + "> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " +
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test24() throws NotImplementedException, SQLException {
		String metricName = "test24";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset24();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
	
	
	/*
	 * deprecated resource (assigned via owl:deprecated "true"^^xsd:boolean);
	 * predicate position
	 */
	private SparqlifyDataset dataset25() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . " +
			"<http://ex.org/pred01> <" + OWL2.deprecated.getURI() + "> \"true\"^^<" + XSD.xboolean.getURI() + "> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test25() throws NotImplementedException, SQLException {
		String metricName = "test25";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset25();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated resource (assigned via owl:deprecated "true"^^xsd:boolean);
	 * object position
	 */
	private SparqlifyDataset dataset26() {
		String content = 
			"<http://ex.org/Cls01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> . " +
			"<http://ex.org/pred01> <" + RDFS.range.getURI() + "> <" + XSD.xstring.getURI() + "> . " +
			"<http://ex.org/res/02> <http://ex.org/pred02> <http://ex.org/res/01> . " +
			"<http://ex.org/res/01> <" + OWL2.deprecated.getURI() + "> \"true\"^^<" + XSD.xboolean.getURI() + "> ." +
			"<http://ex.org/pred02> <" + RDFS.subPropertyOf.getURI() + "> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public synchronized void test26() throws NotImplementedException, SQLException {
		String metricName = "test26";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset26();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
