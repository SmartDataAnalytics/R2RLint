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
import org.junit.Before;
import org.junit.Test;

public class NoResourceNameClashesTest {
	
	Pinpointer pinpointer;
	BooleanTestingSink sink;
	

	@Before
	public void setUp() throws Exception {
		// init dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs );
		
		sink = new BooleanTestingSink();
	}


	/* 
	 * #01
	 * 
	 * no violation
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test01() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/* 
	 * #02
	 * 
	 * class (<http://ex.org/Class01>) used as property
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/Class01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test02() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #03
	 * 
	 * class (<http://ex.org/Class01>) used as subject of a datatype property
	 * explicitly defined
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ." +
			"<http://ex.org/Class01> <http://ex.org/prop01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test03() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #04
	 * 
	 * class (<http://ex.org/Class01>) used as subject of a datatype property
	 * not explicitly defined
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/Class01> <http://ex.org/prop01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test04() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #05
	 * 
	 * class (<http://ex.org/Class01>) used as subject of an object property
	 * explicitly defined
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/Class01> <http://ex.org/prop01> <http://ex.org/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test05() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #06
	 * 
	 * class (<http://ex.org/Class01>) used as subject of an object property
	 * not explicitly defined
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/Class01> <http://ex.org/prop01> <http://ex.org/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test06() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #07
	 * 
	 * class (<http://ex.org/Class01>) used as subject of a property being
	 * neither a datatype- nor an object property (e.g. rdfs:subClassOf, no
	 * violation)
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/Class01> <http://ex.org/prop01> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test07() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/* 
	 * #08
	 * 
	 * class (<http://ex.org/Class02>) used as object of datatype property
	 * explicitly defined
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test08() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #09
	 * 
	 * class (<http://ex.org/Class02>) used as object of object property
	 * explicitly defined
	 */
	public SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test09() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset09();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #10
	 * 
	 * class (<http://ex.org/Class01>) used as object of not whitelisted
	 * property
	 */
	public SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test10() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #11
	 * 
	 * class (<http://ex.org/Class01>) used as object of whitelisted property
	 * (no violation)
	 */
	public SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test11() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset11();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/* 
	 * #12
	 * 
	 * known property (<http://ex.org/prop02>) used as subject of a datatype
	 * property
	 */
	public SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/prop02> <http://ex.org/prop01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test12() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset12();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #13
	 * 
	 * known property (<http://ex.org/prop02>) used as subject of an object
	 * property
	 */
	public SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/prop02> <http://ex.org/prop01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test13() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset13();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #14
	 * 
	 * known property (<http://ex.org/prop02>) used as subject of not
	 * whitelisted property
	 */
	public SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/prop02> <http://ex.org/prop01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test14() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset14();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #15
	 * 
	 * known (<http://ex.org/prop02>) property used as subject of whitelisted
	 * property (no violation)
	 */
	public SparqlifyDataset dataset15() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/prop02> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#int> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test15() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset15();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/* 
	 * #16
	 * 
	 * known property (<http://ex.org/prop02>) used as object of a datatype
	 * property
	 */
	public SparqlifyDataset dataset16() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/prop02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test16() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset16();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #17
	 * 
	 * known property (<http://ex.org/prop02>) used as object of an object
	 * property
	 */
	public SparqlifyDataset dataset17() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/prop02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test17() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset17();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #18
	 * 
	 * known property (<http://ex.org/prop02>) used as object of a not
	 * whitelisted property
	 */
	public SparqlifyDataset dataset18() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
//			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/prop02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test18() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test18";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset18();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #19
	 * 
	 * known property (<http://ex.org/prop02>) used as object of a whitelisted
	 * property (no violation)
	 */
	public SparqlifyDataset dataset19() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
//			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/prop01> <http://www.w3.org/2002/07/owl#inverseOf> <http://ex.org/prop02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test19() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test19";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset19();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/* #20
	 * individual (<http://ex.org/res/02>) used as predicate
	 */
	public SparqlifyDataset dataset20() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/res/02> <http://ex.org/sth> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test20() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test20";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset20();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* #21
	 * individual (<http://ex.org/res/02>) used as subject of certain
	 * (blacklisted) ontology defining properties
	 */
	public SparqlifyDataset dataset21() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test21() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test21";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset21();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* #22
	 * individual (<http://ex.org/res/01>) used as object of certain
	 * (blacklisted) ontology defining properties
	 */
	public SparqlifyDataset dataset22() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2002/07/owl#equivalentClass> <http://ex.org/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test22() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test22";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset22();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/* 
	 * #23
	 * 
	 * blank node used as subject of certain (blacklisted) ontology defining
	 * properties
	 */
	public SparqlifyDataset dataset23() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"_:23 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"_:23 <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
//	@Test
	public void test23() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test23";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset23();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}
	/* #24
	 * blank node used as object of certain (blacklisted) ontology defining
	 * properties
	 */
	public SparqlifyDataset dataset24() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"_:23 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2002/07/owl#equivalentClass> _:23 .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test24() throws NotImplementedException {
		NoResourceNameClashes metric = new NoResourceNameClashes();
		String metricName = "test24";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset24();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}
}
