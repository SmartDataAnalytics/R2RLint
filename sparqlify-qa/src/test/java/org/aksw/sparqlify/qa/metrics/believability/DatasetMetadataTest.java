package org.aksw.sparqlify.qa.metrics.believability;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class DatasetMetadataTest {
	
	ValueTestingSink sink;
	float noDatasetStatementsValue = 0;
	float noSuggestedPropertiesUsedValue = (float) 0.5;
	float noValueWritten = -1;

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	/* 
	 * #01
	 * 
	 * no dataset resource given --> 0
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test01() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = noDatasetStatementsValue;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * #02
	 * 
	 * dataset resource given but no further statements about the dataset made
	 * --> 0
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test02() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = noDatasetStatementsValue;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * #03
	 * 
	 * datset resource given and statements made, but none of the suggested
	 * properties used --> 0.5
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/dataset> <http://ex.org/prop01> \"23\" . " +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test03() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		float expected = noSuggestedPropertiesUsedValue;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * #04
	 * 
	 * dataset resource given and at least one of the suggested statements used
	 * (datasetTitleProperties) --> 1, no write
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/dataset> <http://purl.org/dc/terms/title> \"I am a dataset\" . " +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test04() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		float expected = noValueWritten;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * #05
	 * 
	 * dataset resource given and at least one of the suggested statements used
	 * (datasetContentProperties) --> 1, no write
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/dataset> <http://rdfs.org/ns/void#entities> \"42\"^^<http://www.w3.org/2001/XMLSchema#> . " +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test05() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		float expected = noValueWritten;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * #06
	 * 
	 * dataset resource given and at least one of the suggested statements used
	 * (datasetCreatorProperties) --> 1, no write
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/dataset> <http://purl.org/dc/elements/1.1/creator> \"Anthony Author\" . " +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test06() throws NotImplementedException {
		DatasetMetadata metric = new DatasetMetadata();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		float expected = noValueWritten;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
