package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
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
public class NoDeprecatedClassesOrPropertiesTest {
	
	Pinpointer pinpointer;
	BooleanTestingSink sink;

	@Before
	public void setUp() throws Exception {
		// dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
		sink = new BooleanTestingSink();
	}


	/*
	 * no deprecated classes
	 */
	private SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test01() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}

	/*
	 * deprecated class (assigned via rdf:type); subject position
	 */
	private SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DeprecatedClass> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test02() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class (assigned via rdf:type); object position
	 */
	private SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DeprecatedClass> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test03() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class (assigned via owl:equivalentClass (subject)); subject
	 * position
	 */
	private SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/2002/07/owl#equivalentClass> <http://www.w3.org/2002/07/owl#DeprecatedClass> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test04() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class (assigned via owl:equivalentClass (subject)); object
	 * position
	 */
	private SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/2002/07/owl#equivalentClass> <http://www.w3.org/2002/07/owl#DeprecatedClass> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test05() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class (assigned via owl:equivalentClass (object)); subject
	 * position
	 */
	private SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://www.w3.org/2002/07/owl#DeprecatedClass> <http://www.w3.org/2002/07/owl#equivalentClass> <http://ex.org/Cls01> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test06() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated class (assigned via owl:equivalentClass (object)); object
	 * position
	 */
	private SparqlifyDataset dataset07() {
		String content = 
			"<http://www.w3.org/2002/07/owl#DeprecatedClass> <http://www.w3.org/2002/07/owl#equivalentClass> <http://ex.org/Cls01> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test07() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	
	/*
	 * deprecated property (assigned via rdf:type); subject position
	 */
	private SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test08() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via rdf:type); predicate position
	 */
	private SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test09() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset09();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via rdf:type); object position
	 */
	private SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test10() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via owl:equivalentProperty (subject));
	 * subject position
	 */
	private SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test11() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset11();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via owl:equivalentProperty (subject));
	 * predicate position
	 */
	private SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test12() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset12();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via owl:equivalentProperty (subject)); object
	 * position
	 */
	private SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://www.w3.org/2002/07/owl#DeprecatedProperty> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . " +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test13() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset13();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via owl:equivalentProperty (object));
	 * subject position
	 */
	private SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://www.w3.org/2002/07/owl#DeprecatedProperty> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/pred01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test14() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset14();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (assigned via owl:equivalentClass (object));
	 * predicate position
	 */
	private SparqlifyDataset dataset15() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://www.w3.org/2002/07/owl#DeprecatedProperty> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test15() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset15();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated property (directly assigned via owl:equivalentClass (object));
	 * object position
	 */
	private SparqlifyDataset dataset16() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://www.w3.org/2002/07/owl#DeprecatedProperty> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://ex.org/pred01> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred02> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test16() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset16();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated resource (assigned via owl:deprecated "true"^^xsd:boolean);
	 * subject position
	 */
	private SparqlifyDataset dataset17() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/01> <http://www.w3.org/2002/07/owl#deprecated> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test17() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset17();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated resource (assigned via owl:deprecated "true"^^xsd:boolean);
	 * predicate position
	 */
	private SparqlifyDataset dataset18() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . " +
			"<http://ex.org/pred01> <http://www.w3.org/2002/07/owl#deprecated> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test18() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset18();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * deprecated resource (assigned via owl:deprecated "true"^^xsd:boolean);
	 * object position
	 */
	private SparqlifyDataset dataset19() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/02> <http://ex.org/pred02> <http://ex.org/res/01> . " +
			"<http://ex.org/res/01> <http://www.w3.org/2002/07/owl#deprecated> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ." +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test19() throws NotImplementedException {
		NoDeprecatedClassesOrProperties metric = new NoDeprecatedClassesOrProperties();
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset19();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
