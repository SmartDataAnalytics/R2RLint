package org.aksw.sparqlify.qa.metrics.conciseness;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.Before;
import org.junit.Test;

public class ExtensionalConcisenessTest {
	
	ValueTestingSink sink;
	ViewDefinition viewDef01;
	ViewDefinition viewDef02;
	ViewDefinition viewDef03;
	ViewDefinition viewDef04;
	ViewDefinition viewDef05;
	ViewDefinition viewDef06;
	ViewDefinition viewDef07;
	

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
		initViewDefinitions();
	}


	@Test
	public void test01() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef01));
		
		float expected = (float) -1;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test02() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef02));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test03() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef01, viewDef02));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test04() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef03));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test05() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef04));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test06() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef03, viewDef04));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}

	@Test
	public void test07() throws NotImplementedException {
		ExtensionalConciseness metric = new ExtensionalConciseness();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		metric.assessMappings(Arrays.asList(viewDef01, viewDef02, viewDef05));
		
		float expected = 1 / (float) 4;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	private void initViewDefinitions() throws IOException {
		
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		ViewDefinitionFactory vdf =
				SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
		
		// no redundant resources
		viewDef01 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Create View departments As " +
						"Construct { " +
							"?dept a ex:Department. " +
							"?dept rdfs:label ?dnme. " +
							"?dept ex:title ?dttl." +
						"} " +
						"With " +
							"?dept = uri('http://ex.org/oo', ?id) " +
							"?dnme = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
		
		// 1 redundant resource
		viewDef02 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Create View departments As " +
						"Construct { " +
							"?dept2 a ex:Department. " +
							"?dept3 a ex:Department. " +
							"?dept2 rdfs:label ?dnme. " +
							"?dept2 ex:title ?dttl." +
						"} " +
						"With " +
							"?dept2 = uri('http://ex.org/depts/', ?id) " +
							"?dept3 = uri('http://ex.org/dpt', ?id) " +
							"?dnme = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
		
		// 1 redundant resource
		viewDef03 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Create View departments As " +
						"Construct { " +
							"?dept4 a ex:Department. " +
							"?dept4 rdfs:label ?dnme. " +
							"?dept4 ex:Name ?name. " +
							"?dept4 ex:title ?dttl." +
						"} " +
						"With " +
							"?dept4 = uri('http://ex.org/depts/dpt', ?id) " +
							"?name = uri('http://ex.org/names/', ?default_name) " +
							"?dnme = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
		
		// 1 redundant resource
		viewDef04 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Create View departments As " +
						"Construct { " +
							"?dept4 a ex:Department. " +
							"?name a ex:Name. " +
							"?dept4 rdfs:label ?dnme. " +
							"?dept4 ex:title ?dttl." +
						"} " +
						"With " +
							"?dept4 = uri('http://ex.org/depts/dpt', ?id) " +
							"?name = uri('http://ex.org/names/', ?default_name) " +
							"?dnme = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
		
		// no redundant resource
		viewDef05 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Create View departments As " +
						"Construct { " +
							"?dept4 a ex:Department. " +
							"?dept4 rdfs:label ?dnme. " +
							"?dept4 ex:title ?dttl." +
						"} " +
						"With " +
							"?dept4 = uri('http://ex.org/depts/dpt', ?id) " +
							"?dnme = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
	}
}
