package org.aksw.sparqlify.qa.metrics.conciseness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntensionalConcisenessTest {

	ValueTestingSink valSink;
	BooleanTestingSink boolSink;
	ViewDefinition viewDef01;
	ViewDefinition viewDef02;
	ViewDefinition viewDef03;
	ViewDefinition viewDef04;


	@Before
	public void setUp() throws Exception {
		valSink = new ValueTestingSink();
		boolSink = new BooleanTestingSink();
		initViewDefinitions();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test01() throws NotImplementedException {
		IntensionalConciseness metric = new IntensionalConciseness();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(boolSink);
		
		metric.assessMappings(Arrays.asList(viewDef01));
		
		assertFalse(boolSink.mappingMeasureWritten(metricName));
	}


	@Test
	public void test02() throws NotImplementedException {
		IntensionalConciseness metric = new IntensionalConciseness();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(valSink);
		
		metric.assessMappings(Arrays.asList(viewDef02));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, valSink.mappingMeasureValue(metricName), 0);
	}
	


	@Test
	public void test03() throws NotImplementedException {
		IntensionalConciseness metric = new IntensionalConciseness();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(valSink);
		
		metric.assessMappings(Arrays.asList(viewDef01, viewDef02));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, valSink.mappingMeasureValue(metricName), 0);
	}

	
	@Test
	public void test04() throws NotImplementedException {
		IntensionalConciseness metric = new IntensionalConciseness();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(valSink);
		
		metric.assessMappings(Arrays.asList(viewDef03));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, valSink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test05() throws NotImplementedException {
		IntensionalConciseness metric = new IntensionalConciseness();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(valSink);
		
		metric.assessMappings(Arrays.asList(viewDef04));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, valSink.mappingMeasureValue(metricName), 0);
	}


	private void initViewDefinitions() throws IOException {
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		ViewDefinitionFactory vdf =
				SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
		
		// no redundant quad patterns
		viewDef01 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View departments As " +
						"Construct { " +
							"?dept a ex:Department. " +
							"?dept rdfs:label ?dnme. " +
							"?dept ex:title ?dttl." +
							"?foo  ex:fooBar ?bar. " +
						"} " +
						"With " +
							"?dept = uri('http://ex.org/oo', ?id) " +
							"?dnme = plainLiteral(?default_name) " +
							"?foo = uri('http://ex.org/', ?id) " +
							"?bar = plainLiteral(?default_name) " +
							"?dttl = plainLiteral(?name, ?lang) " +
						"From" +
							" foo");
		
		// 1 redundant quad pattern
		viewDef02 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View departments As " +
				"Construct { " +
					"?dept a ex:Department. " +
					"?dept rdfs:label ?dnme. " +
					"?dept ex:label ?dnme. " +
					"?dept ex:title ?dttl." +
				"} " +
				"With " +
					"?dept = uri('http://ex.org/', ?id) " +
					"?dnme = plainLiteral(?default_name) " +
					"?dttl = plainLiteral(?name, ?lang) " +
				"From" +
					" foo");
		
		// 2 redundant quad patterns
		viewDef03 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View departments As " +
				"Construct { " +
					"?dept a ex:Department. " +
					"?dept rdfs:label ?dnme. " +
					"?dept ex:label ?dnme. " +
					"?dept spy:label ?dnme. " +
					"?dept ex:title ?dttl." +
				"} " +
				"With " +
					"?dept = uri('http://ex.org/', ?id) " +
					"?dnme = plainLiteral(?default_name) " +
					"?dttl = plainLiteral(?name, ?lang) " +
				"From" +
					" foo");
		
		// 1 redundant quad patterns + 1 redundant term constructor
		viewDef04 = vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View departments As " +
				"Construct { " +
					"?dept a ex:Department. " +
					"?dept rdfs:label ?dnme. " +
					"?dept ex:label ?dnme. " +
					"?dept spy:label ?dnmf. " +
					"?dept ex:title ?dttl." +
				"} " +
				"With " +
					"?dept = uri('http://ex.org/', ?id) " +
					"?dnme = plainLiteral(?default_name) " +
					"?dnmf = plainLiteral(?default_name) " +
					"?dttl = plainLiteral(?name, ?lang) " +
				"From" +
					" foo");
	}
}
