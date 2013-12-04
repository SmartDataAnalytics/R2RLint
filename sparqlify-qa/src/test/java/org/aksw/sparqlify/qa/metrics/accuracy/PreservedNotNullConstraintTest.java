package org.aksw.sparqlify.qa.metrics.accuracy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class PreservedNotNullConstraintTest {
	
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private DataSource rdb;
	private Connection conn;
	private ViewDefinitionFactory vdf;
	
	@Autowired
	private PreservedNotNullConstraint metric;
	
	
	@PostConstruct
	private void init() throws SQLException {
		conn = rdb.getConnection();
	}
	
	@PreDestroy
	private void cleanUp() throws SQLException {
		conn.close();
	}
	
	@Before
	public void setUp() throws Exception {
		initDBContent();
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
	}
	
	@After
	public void tearDown() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS test;");
	}
	
	
	private void initDBContent() throws SQLException {
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS test;");
		conn.createStatement().executeUpdate("CREATE TABLE test ("
					+ "id integer NOT NULL, "
					+ "default_name varchar(400), "
					+ "name varchar(400), "
					+ "age integer NOT NULL"
				+ ");");
		conn.commit();
	}
	
	
	private ViewDefinition viewDef01() throws IOException {
		return vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View view01 As " +
				"Construct { " +
					"?r rdfs:label ?name . " +
					"?r ex:prop1 ?dflt . " +
					"?r ex:prop2 ?age . " +
				"} " +
				"With " +
					"?r = uri(ex:resource, '/', ?ID) " +
					"?name = plainLiteral(?NAME) " +
					"?dflt = plainLiteral(?DEFAULT_NAME) " +
					"?age  = typedLiteral(?AGE, xsd:int) " +
				"From " +
					"TEST");
	}
	
	/*
	 * since ex:prop2 is not constrained, view01 does not preserve the NOT NULL
	 * constraint --> violation
	 */
	@Test
	public synchronized void test01() throws NotImplementedException,
			SQLException, IOException {
		
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessMappings(Arrays.asList(viewDef01()));
		
		assertTrue(sink.measureWritten(metricName));
	}
	
	
	private ViewDefinition viewDef02() throws IOException {

		return vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View view02 As " +
				"Construct { " +
					"?r rdfs:label ?name . " +
					"?r ex:prop1 ?dflt . " +
					"?r ex:prop2 ?age . " +
					"_:23 a owl:Restriction . " +
					"_:23 owl:onProperty ex:prop1 . " +
					"_:23 owl:cardinality \"1\"^^xsd:int ." +
				"} " +
				"With " +
					"?r = uri(ex:resource, '/', ?ID) " +
					"?name = plainLiteral(?NAME) " +
					"?dflt = plainLiteral(?DEFAULT_NAME) " +
					"?age  = typedLiteral(?AGE, xsd:int) " +
				"From " +
					"TEST");
	}
	
	/*
	 * since ex:prop2 is not constrained but ex:prop1, view02 does not preserve
	 * the NOT NULL constraint --> violation
	 */
	@Test
	public synchronized void test02() throws NotImplementedException,
			SQLException, IOException {
		
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessMappings(Arrays.asList(viewDef02()));
		
		assertTrue(sink.measureWritten(metricName));
	}
	
	
	private ViewDefinition viewDef03() throws IOException {

		return vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View view03 As " +
				"Construct { " +
					"?r rdfs:label ?name . " +
					"?r ex:prop1 ?dflt . " +
					"?r ex:prop2 ?age . " +
					"_:23 a owl:Restriction . " +
					"_:23 owl:onProperty ex:prop2 . " +
					"_:23 owl:cardinality \"1\"^^xsd:int ." +
				"} " +
				"With " +
					"?r = uri(ex:resource, '/', ?ID) " +
					"?name = plainLiteral(?NAME) " +
					"?dflt = plainLiteral(?DEFAULT_NAME) " +
					"?age  = typedLiteral(?AGE, xsd:int) " +
				"From " +
					"TEST");
	}
	
	/*
	 * since ex:prop2 is constrained here, view03 preserves the NOT NULL
	 * constraint
	 */
	@Test
	public synchronized void test03() throws NotImplementedException,
			SQLException, IOException {
		
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessMappings(Arrays.asList(viewDef03()));
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	private ViewDefinition viewDef04() throws IOException {

		return vdf.create(
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"Prefix foaf:<http://xmlns.com/foaf/0.1/> " +
				"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
				"Prefix owl:<http://www.w3.org/2002/07/owl#> " +
				"Prefix spy:<http://aksw.org/sparqlify/> " +
				"Create View view04 As " +
				"Construct { " +
					"?r rdfs:label ?name . " +
					"?r ex:prop1 ?dflt . " +
					"?r ex:prop2 ?age . " +
					"_:23 a owl:Restriction . " +
					"_:23 owl:onProperty ex:prop2 . " +
					"_:23 owl:minCardinality \"1\"^^xsd:int ." +
				"} " +
				"With " +
					"?r = uri(ex:resource, '/', ?ID) " +
					"?name = plainLiteral(?NAME) " +
					"?dflt = plainLiteral(?DEFAULT_NAME) " +
					"?age  = typedLiteral(?AGE, xsd:int) " +
				"From " +
					"TEST");
	}
	
	/*
	 * since ex:prop2 is constrained here, view04 preserves the NOT NULL
	 * constraint
	 */
	@Test
	public synchronized void test04() throws NotImplementedException,
			SQLException, IOException {
		
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.assessMappings(Arrays.asList(viewDef04()));
		
		assertFalse(sink.measureWritten(metricName));
	}
}
