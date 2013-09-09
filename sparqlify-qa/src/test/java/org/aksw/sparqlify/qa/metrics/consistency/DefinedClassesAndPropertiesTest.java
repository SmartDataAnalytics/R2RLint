package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class DefinedClassesAndPropertiesTest {
	
	ValueTestingSink sink;


	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	/*
	 * no violations, but no prefix set for dataset --> all not defined
	 * properties are reported even if they are not local
	 */
	@Test
	public void test01() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * no violations
	 */
	@Test
	public void test02() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = (float) -1;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * no violations, but no prefix set for dataset --> all not defined
	 * properties are reported even if they are not local
	 */
	@Test
	public void test03() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * no violations
	 */
	@Test
	public void test04() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = (float) -1;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * undefined property
	 */
	@Test
	public void test05() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset03();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * undefined class
	 */
	@Test
	public void test06() throws NotImplementedException {
		DefinedClassesAndProperties metric = new DefinedClassesAndProperties();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset04();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * dataset with no violations (rdfs:Class, rdfs:Property)
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}


	/*
	 * dataset with no violations (owl:Class, rdfs:Property)
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}


	/*
	 * dataset with undefined property 
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}


	/*
	 * dataset with undefinded class
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
}
