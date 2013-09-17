package org.aksw.sparqlify.qa.metrics.amountofdata;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.amountofdata.CoverageScope;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class CoverageScopeTest {
	
	ValueTestingSink sink;

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	/*
	 * 0 instances since no class assigned as type
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/02> <http://ex.org/pred01> \"Sth\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test01() throws NotImplementedException {
		CoverageScope metric = new CoverageScope();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = 0 / (float) 4;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * 2 individuals / 6 statements = 0.3333...
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . " +
			"<http://ex.org/res/02> <http://ex.org/pred01> \"Sth else\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test02() throws NotImplementedException {
		CoverageScope metric = new CoverageScope();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = 2 / (float) 6;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * 2 individuals / 6 statements = 0.3333...
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/Cls01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Cls01> ." +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . " +
			"<http://ex.org/res/02> <http://ex.org/pred01> \"Sth else\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test03() throws NotImplementedException {
		CoverageScope metric = new CoverageScope();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		float expected = 2 / (float) 6;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
