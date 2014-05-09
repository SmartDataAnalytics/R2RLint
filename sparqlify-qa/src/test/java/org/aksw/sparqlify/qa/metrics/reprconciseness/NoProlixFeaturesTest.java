package org.aksw.sparqlify.qa.metrics.reprconciseness;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.reprconciseness.NoProlixFeatures;
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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class NoProlixFeaturesTest {

	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private NoProlixFeatures metric;


	@Before
	public void setUp() throws Exception {
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
	}

	/*
	 * no prolix features
	 */
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = RDF.type.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/Foo");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:subject predicate
	 */
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/statement/foo");
		Node pred = RDF.subject.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/things/sth");
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:subject subject)
	 */
	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.subject.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:subject object)
	 */
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.subject.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:predicate predicate
	 */
	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/statement/foo");
		Node pred = RDF.predicate.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/things/sth");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:predicate subject)
	 */
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.predicate.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:predicate object)
	 */
	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.predicate.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:object predicate
	 */
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/statement/foo");
		Node pred = RDF.object.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/things/sth");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:object subject)
	 */
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.object.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:object object)
	 */
	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/properties/fooProp");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.object.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Statement
	 */
	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = RDF.type.asNode();
		Node obj = RDF.Statement.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Statement object without rdftype)
	 */
	@Test
	public synchronized void test12() throws NotImplementedException, SQLException {
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/someProp");
		Node obj = RDF.Statement.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Statement subject)
	 */
	@Test
	public synchronized void test13() throws NotImplementedException, SQLException {
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.Statement.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:_1 predicate
	 */
	@Test
	public synchronized void test14() throws NotImplementedException, SQLException {
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/barContainer");
		Node pred = RDF.li(1).asNode();
		Node obj = NodeFactory.createURI("http://ex.org/foo/first");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:_1 subject)
	 */
	@Test
	public synchronized void test15() throws NotImplementedException, SQLException {
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.li(1).asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:_1 object)
	 */
	@Test
	public synchronized void test16() throws NotImplementedException, SQLException {
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/containerProperties/_1");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.li(1).asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdfs:member predicate
	 */
	@Test
	public synchronized void test17() throws NotImplementedException, SQLException {
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/member");
		Node pred = RDFS.member.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/foo/barContainer");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:member subject)
	 */
	@Test
	public synchronized void test18() throws NotImplementedException, SQLException {
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDFS.member.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:member object)
	 */
	@Test
	public synchronized void test19() throws NotImplementedException, SQLException {
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/containerProperties/member");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDFS.member.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Alt
	 */
	@Test
	public synchronized void test20() throws NotImplementedException, SQLException {
		String metricName = "test20";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar/alternative");
		Node pred = RDF.type.asNode();
		Node obj = RDF.Alt.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Alt object without rdf:type)
	 */
	@Test
	public synchronized void test21() throws NotImplementedException, SQLException {
		String metricName = "test21";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = RDF.Alt.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Alt subject)
	 */
	@Test
	public synchronized void test22() throws NotImplementedException, SQLException {
		String metricName = "test22";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.Alt.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Bag 
	 */
	@Test
	public synchronized void test23() throws NotImplementedException, SQLException {
		String metricName = "test23";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar/dirtBag");
		Node pred = RDF.type.asNode();
		Node obj = RDF.Bag.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Bag object without rdf:type)
	 */
	@Test
	public synchronized void test24() throws NotImplementedException, SQLException {
		String metricName = "test24";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = RDF.Bag.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Bag subject)
	 */
	@Test
	public synchronized void test25() throws NotImplementedException, SQLException {
		String metricName = "test25";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.Bag.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdf:Seq
	 */
	@Test
	public synchronized void test26() throws NotImplementedException, SQLException {
		String metricName = "test26";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar/sequence");
		Node pred = RDF.type.asNode();
		Node obj = RDF.Seq.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Seq object without rdf:type)
	 */
	@Test
	public synchronized void test27() throws NotImplementedException, SQLException {
		String metricName = "test27";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = RDF.Seq.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:Seq subject)
	 */
	@Test
	public synchronized void test28() throws NotImplementedException, SQLException {
		String metricName = "test28";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.Seq.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdfs:Container
	 */
	@Test
	public synchronized void test29() throws NotImplementedException, SQLException {
		String metricName = "test29";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar/container");
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Container.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:Container object without rdf:type)
	 */
	@Test
	public synchronized void test30() throws NotImplementedException, SQLException {
		String metricName = "test30";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = RDFS.Container.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdfs:Container subject)
	 */
	@Test
	public synchronized void test31() throws NotImplementedException, SQLException {
		String metricName = "test31";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDFS.Container.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:first predicate
	 */
	@Test
	public synchronized void test32() throws NotImplementedException, SQLException {
		String metricName = "test32";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/collection");
		Node pred = RDF.first.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/foo/members/1");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:first subject)
	 */
	@Test
	public synchronized void test33() throws NotImplementedException, SQLException {
		String metricName = "test33";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.first.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:first object)
	 */
	@Test
	public synchronized void test34() throws NotImplementedException, SQLException {
		String metricName = "test34";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/collectionProperties/foorst");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.first.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:rest predicate
	 */
	@Test
	public synchronized void test35() throws NotImplementedException, SQLException {
		String metricName = "test35";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/collection");
		Node pred = RDF.rest.asNode();
		Node obj = NodeFactory.createURI("http://ex.org/foo/members/subCollection");

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:rest subject)
	 */
	@Test
	public synchronized void test36() throws NotImplementedException, SQLException {
		String metricName = "test36";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.rest.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDF.Property.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:rest object)
	 */
	@Test
	public synchronized void test37() throws NotImplementedException, SQLException {
		String metricName = "test37";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/collectionProperties/barst");
		Node pred = RDFS.subPropertyOf.asNode();
		Node obj = RDF.rest.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * rdf:type rdf:List
	 */
	@Test
	public synchronized void test38() throws NotImplementedException, SQLException {
		String metricName = "test38";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/fooLists/bar");
		Node pred = RDF.type.asNode();
		Node obj = RDF.List.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertTrue(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:List object without rdf:type)
	 */
	@Test
	public synchronized void test39() throws NotImplementedException, SQLException {
		String metricName = "test39";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = NodeFactory.createURI("http://ex.org/foo/bar");
		Node pred = NodeFactory.createURI("http://ex.org/properties/evenBetterThan");
		Node obj = RDF.List.asNode();

		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}

	/*
	 * no prolix feature (rdf:List subject)
	 */
	@Test
	public synchronized void test40() throws NotImplementedException, SQLException {
		String metricName = "test40";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		Node subj = RDF.List.asNode();
		Node pred = RDF.type.asNode();
		Node obj = RDFS.Class.asNode();
		
		Triple triple = new Triple(subj, pred, obj);
		metric.assessTriple(triple);
		
		assertFalse(sink.measureWritten(metricName));
	}
}
