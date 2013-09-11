package org.aksw.sparqlify.qa.metrics.interlinking;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class ExternalSameAsLinksTest {

	ValueTestingSink sink;

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	/*
	 * no interlinks
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test01() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * no external links (<local> owl:sameAs <local>)
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.org/res/23> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test02() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = 0 / (float) 5;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * one external link (<local> owl:sameAs <external>)
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.com/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test03() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset03();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = 1 / (float) 5;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * one external link (<external> owl:sameAs <local>)
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.com/res/01> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.org/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test04() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset04();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = 1 / (float) 5;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * no external links (<external> owl:sameAs <external>)
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.com/res/01> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.com/res/01> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test05() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset05();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = 0 / (float) 5;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * two external links
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.com/res/01> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.org/res/01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/2002/07/owl#sameAs> <http://ex.com/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}

	@Test
	public void test06() throws NotImplementedException {
		ExternalSameAsLinks metric = new ExternalSameAsLinks();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset06();
		dataset.setPrefix("http://ex.org/");
		metric.assessDataset(dataset);
		
		float expected = 2 / (float) 6;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
