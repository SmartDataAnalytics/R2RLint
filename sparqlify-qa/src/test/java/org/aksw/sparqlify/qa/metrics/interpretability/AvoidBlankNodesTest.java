package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class AvoidBlankNodesTest {
	
	private ViewDefinitionFactory vdf;
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private AvoidBlankNodes metric;

	
	@Before
	public void setUp() throws Exception {
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
	}


	/*
	 * #01
	 * 
	 * no violations
	 */
	private ViewDefinition viewDef01() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view01 As " +
				"Construct { " +
					"?a a ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?a ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = uri(ex:a2, '/', ?a2) " +
					"?a3 = uri(ex:a3, '/', ?a3) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.assessMappings(Arrays.asList(viewDef01()));

		assertFalse(sink.measureWritten(metricName));
	}


	/*
	 * #02
	 * 
	 * blank node on subject position
	 */
	private ViewDefinition viewDef02() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view02 As " +
				"Construct { " +
					"?a a ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?b ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = uri(ex:a2, '/', ?a2) " +
					"?a3 = uri(ex:a3, '/', ?a3) " +
					"?b  = bNode(?a_id) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.assessMappings(Arrays.asList(viewDef02()));

		assertTrue(sink.measureWritten(metricName));
	}


	/*
	 * #03
	 * 
	 * blank node on subject position
	 */
	private ViewDefinition viewDef03() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view03 As " +
				"Construct { " +
					"?a a ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?a ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = uri(ex:a2, '/', ?a2) " +
					"?a3 = bNode(?a3) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.assessMappings(Arrays.asList(viewDef03()));

		assertTrue(sink.measureWritten(metricName));
	}


	/*
	 * #04
	 * 
	 * even though it is not allowed: blank node on predicate position
	 */
	private ViewDefinition viewDef04() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view04 As " +
				"Construct { " +
					"?a ?b ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?a ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?b = bNode(?a_id) " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = uri(ex:a2, '/', ?a2) " +
					"?a3 = uri(ex:a3, '/', ?a3) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.assessMappings(Arrays.asList(viewDef04()));

		assertTrue(sink.measureWritten(metricName));
	}
}
