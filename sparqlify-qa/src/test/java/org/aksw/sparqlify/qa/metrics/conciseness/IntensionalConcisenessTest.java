package org.aksw.sparqlify.qa.metrics.conciseness;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class IntensionalConcisenessTest {

	@Autowired
	private MeasureDataSink sink;
	@Autowired
	private IntensionalConciseness metric;
	
	ViewDefinition viewDef01;
	ViewDefinition viewDef02;
	ViewDefinition viewDef03;
	ViewDefinition viewDef04;
	ViewDefinition viewDef05;

	@Before
	public void setUp() throws Exception {
		initViewDefinitions();
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
		
		// 2 redundant term constructor
		viewDef05 = vdf.create(
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
					"?dept rdfs:label ?dnme. " +  // dup
					"?dept2 ex:label ?dnme. " +  // dup
					"?dept spy:label ?dnmf. " +  // dup
					"?dept ex:title ?dttl." +
				"} " +
				"With " +
					"?dept = uri('http://ex.org/', ?id) " +
					"?dept2 = uri('http://ex.org/', ?id) " +
					"?dnme = plainLiteral(?default_name) " +
					"?dnmf = plainLiteral(?default_name) " +
					"?dttl = plainLiteral(?name, ?lang) " +
				"From" +
					" foo");
	}
	
	
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef01));
		
		float expected = (float) -1;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef02));
		
		float expected = 1 / (float) 2;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}
	


	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef01, viewDef02));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}

	
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef03));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef04));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessViews(Arrays.asList(viewDef05));
		
		float expected = 1 / (float) 3;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}
}
