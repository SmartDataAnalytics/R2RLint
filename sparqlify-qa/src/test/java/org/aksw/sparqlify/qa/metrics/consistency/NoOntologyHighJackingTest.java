package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class NoOntologyHighJackingTest {

	private final float noViolation = -1;
	private final float badSmell = (float) 0.5;
	private final float error = 0;
	
	private ValueTestingSink sink;
	private Pinpointer pinpointer;


	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
		// initialize dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
	}


	/*
	 * no violation
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"res 1\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"res 2\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		dataset.setUsedPrefixes(Arrays.asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		
		return dataset;
	}
	
	@Test
	public void test01() throws NotImplementedException {
		NoOntologyHighJacking metric = new NoOntologyHighJacking();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * bad smell
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://www.w3.org/2002/07/owl#sameAs> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"res 1\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"res 2\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		dataset.setUsedPrefixes(Arrays.asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		
		return dataset;
	}
	
	@Test
	public void test02() throws NotImplementedException {
		NoOntologyHighJacking metric = new NoOntologyHighJacking();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertEquals(badSmell, sink.writtenValue(metricName), 0);
	}


	/*
	 * violation
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"res 1\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"res 2\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		dataset.setUsedPrefixes(Arrays.asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		
		return dataset;
	}
	
	@Test
	public void test03() throws NotImplementedException {
		NoOntologyHighJacking metric = new NoOntologyHighJacking();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertEquals(error, sink.writtenValue(metricName), 0);
	}


	/*
	 * re-definition that conforms with the original specs --> no violation
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"res 1\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"res 2\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		dataset.setUsedPrefixes(Arrays.asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		
		return dataset;
	}
	
	@Test
	public void test04() throws NotImplementedException {
		NoOntologyHighJacking metric = new NoOntologyHighJacking();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * re-definitions that conform with the original specs --> no violation
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"res 1\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"res 2\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		dataset.setUsedPrefixes(Arrays.asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		
		return dataset;
	}
	
	@Test
	public void test05() throws NotImplementedException {
		NoOntologyHighJacking metric = new NoOntologyHighJacking();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}
}
