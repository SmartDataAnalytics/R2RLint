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

public class CoverageDetailTest {
	
	ValueTestingSink sink;

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	/*
	 * 2 different properties / 3 triples
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
		CoverageDetail metric = new CoverageDetail();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = 2 / (float) 3;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * 3 different properties / 3 triples
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/pred01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> . " +
			"<http://ex.org/res/01> <http://ex.org/pred01> \"Sth\" . " +
			"<http://ex.org/res/02> <http://ex.org/pred02> \"Sth else\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test02() throws NotImplementedException {
		CoverageDetail metric = new CoverageDetail();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = 3 / (float) 3;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * 2 differnt properties / 6 triples
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
		CoverageDetail metric = new CoverageDetail();
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
