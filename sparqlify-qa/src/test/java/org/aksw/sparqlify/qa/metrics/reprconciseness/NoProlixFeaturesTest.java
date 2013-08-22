package org.aksw.sparqlify.qa.metrics.reprconciseness;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.reprconciseness.NoProlixFeatures;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class NoProlixFeaturesTest {

	BooleanTestingSink sink;
	Pinpointer pinpointer;


	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		pinpointer = new Pinpointer(new ArrayList<ViewDefinition>());
		
	}

	/*
	 * no prolix features
	 */
	@Test
	public void test01() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:subject predicate
	 */
	@Test
	public void test02() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/statement/foo");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
		Node obj = Node.createURI("http://ex.org/things/sth");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:subject subject)
	 */
	@Test
	public void test03() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:subject object)
	 */
	@Test
	public void test04() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/properties/fooProp");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:predicate predicate
	 */
	@Test
	public void test05() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/statement/foo");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
		Node obj = Node.createURI("http://ex.org/things/sth");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:predicate subject)
	 */
	@Test
	public void test06() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:predicate object)
	 */
	@Test
	public void test07() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/properties/fooProp");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:object predicate
	 */
	@Test
	public void test08() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/statement/foo");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
		Node obj = Node.createURI("http://ex.org/things/sth");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:object subject)
	 */
	@Test
	public void test09() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:object object)
	 */
	@Test
	public void test10() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/properties/fooProp");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");;

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Statement
	 */
	@Test
	public void test11() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Statement object without rdftype)
	 */
	@Test
	public void test12() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/someProp");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Statement subject)
	 */
	@Test
	public void test13() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:_1 predicate
	 */
	@Test
	public void test14() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/barContainer");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1");
		Node obj = Node.createURI("http://ex.org/foo/first");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:_1 subject)
	 */
	@Test
	public void test15() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:_1 object)
	 */
	@Test
	public void test16() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/containerProperties/_1");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdfs:member predicate
	 */
	@Test
	public void test17() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/member");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#member");
		Node obj = Node.createURI("http://ex.org/foo/barContainer");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:member subject)
	 */
	@Test
	public void test18() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#member");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:member object)
	 */
	@Test
	public void test19() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/containerProperties/member");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#member");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Alt
	 */
	@Test
	public void test20() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test20";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar/alternative");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Alt object without rdf:type)
	 */
	@Test
	public void test21() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test21";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Alt subject)
	 */
	@Test
	public void test22() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test22";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Bag 
	 */
	@Test
	public void test23() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test23";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar/dirtBag");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Bag object without rdf:type)
	 */
	@Test
	public void test24() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test24";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Bag subject)
	 */
	@Test
	public void test25() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test25";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Seq
	 */
	@Test
	public void test26() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test26";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar/sequence");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Seq object without rdf:type)
	 */
	@Test
	public void test27() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test27";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Seq subject)
	 */
	@Test
	public void test28() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test28";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdfs:Container
	 */
	@Test
	public void test29() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test29";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar/container");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Container");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:Container object without rdf:type)
	 */
	@Test
	public void test30() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test30";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Container");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:Container subject)
	 */
	@Test
	public void test31() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test31";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Container");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:first predicate
	 */
	@Test
	public void test32() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test32";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/collection");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
		Node obj = Node.createURI("http://ex.org/foo/members/1");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:first subject)
	 */
	@Test
	public void test33() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test33";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:first object)
	 */
	@Test
	public void test34() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test34";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/collectionProperties/foorst");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:rest predicate
	 */
	@Test
	public void test35() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test35";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/collection");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
		Node obj = Node.createURI("http://ex.org/foo/members/subCollection");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:rest subject)
	 */
	@Test
	public void test36() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test36";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:rest object)
	 */
	@Test
	public void test37() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test37";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/collectionProperties/barst");
		Node pred = Node.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * rdf:type rdf:List
	 */
	@Test
	public void test38() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test38";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/fooLists/bar");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:List object without rdf:type)
	 */
	@Test
	public void test39() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test39";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://ex.org/foo/bar");
		Node pred = Node.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:List subject)
	 */
	@Test
	public void test40() throws NotImplementedException {
		NoProlixFeatures metric = new NoProlixFeatures();
		String metricName = "test40";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerPinpointer(pinpointer);
		metric.registerMeasureDataSink(sink);
		
		Node subj = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
		Node pred = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node obj = Node.createURI("http://www.w3.org/2000/01/rdf-schema#Class");

		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.tripleMeasureWritten(metricName));
	}
}
