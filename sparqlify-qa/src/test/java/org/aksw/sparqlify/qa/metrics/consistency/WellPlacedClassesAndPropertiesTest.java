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

public class WellPlacedClassesAndPropertiesTest {
	
	Pinpointer pinpointer;
	BooleanTestingSink sink;

	@Before
	public void setUp() throws Exception {
		// init dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
		
		sink = new BooleanTestingSink();
	}


	/*
	 * no violations (with explicitly typed property and class)
	 */
	private SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test01() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * no violations (with no explicitly typed property and class)
	 */
	private SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test02() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * property (not explicitly typed) on subject position, no violation:
	 * whitelisted predicate
	 */
	private SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test03() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/*
	 * property (explicitly typed) on subject position, no violation:
	 * whitelisted predicate
	 */
	private SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test04() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/*
	 * property (not explicitly typed) on subject position, violation
	 */
	private SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred01> <http://ex.org/pred02> \"Sth else\"^^<http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test05() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * property (explicitly typed) on subject position, violation
	 */
	private SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred01> <http://ex.org/pred02> \"Sth else\"^^<http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test06() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * property (not explicitly typed) in object position, no violation:
	 * whitelisted predicate
	 */
	private SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test07() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * property (explicitly typed) in object position, no violation:
	 * whitelisted predicate
	 */
	private SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/pred02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test08() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}


	/*
	 * property (not explicitly typed) in object position, violation
	 */
	private SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/01> <http://ex.org/pred02> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test09() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * property (explicitly typed) in object position, violation
	 */
	private SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/01> <http://ex.org/pred02> <http://ex.org/pred01> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test10() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
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
	 * class (not explicitly typed) in predicate postion, violation
	 */
	private SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/Cls01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test11() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset11();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}


	/*
	 * class (explicitly typed) in predicate postion, violation
	 */
	private SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/Cls01> \"Sth\"^^<http://www.w3.org/2001/XMLSchema#string> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test12() throws NotImplementedException {
		WellPlacedClassesAndProperties metric = new WellPlacedClassesAndProperties();
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset12();
		metric.assessDataset(dataset);
		
		assertTrue(sink.measureWritten(metricName));
	}
}
