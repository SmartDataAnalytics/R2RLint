package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class NoAmbiguousMappingsTest {
	
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private DataSource rdb;
	private Connection conn;
	@Autowired
	private NoAmbiguousMappings metric;
	private ViewDefinitionFactory vdf;
	
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
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
	}

	/* #01
	 * no ambiguities
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:a2, '/', ?a2)  ]
	 *   a3           [ uri(ex:a3, '/', ?a3)  ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:b2, '/', ?b2)  ]
	 *   b3           [ uri(ex:b3, '/', ?b3)  ]
	 */
	private void initData01() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL" +
				");");
	}
	
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

	private ViewDefinition viewDef02() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view02 As " +
				"Construct { " +
					"?b a ex:Sth . " +
					"?b ex:prop1 ?b2 . " +
					"?b ex:prop2 ?b3 . " +
				"} " +
				"With " +
					"?b  = uri(ex:b, '/', ?b_id) " +
					"?b2 = uri(ex:b2, '/', ?b2) " +
					"?b3 = uri(ex:b3, '/', ?b3) " +
				"From " +
					"B "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		initData01();
		metric.assessMappings(Arrays.asList(viewDef01(), viewDef02()));
		metric.cleanCaches();

		assertFalse(sink.measureWritten(metricName));
	}


	/* #02
	 * no ambiguity, but foreign key situation
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:a2, '/', ?a2)  ]
	 *   a3           [ uri(ex:key, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:b2, '/', ?b2 ) ]
	 *   b3 --> A.a3  [ uri(ex:key, '/', ?b3) ]
	 */
	private void initData02() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL REFERENCES A(a3)" +
				");");
	}

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
					"?a3 = uri(ex:key, '/', ?a3) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}
	private ViewDefinition viewDef04() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view04 As " +
				"Construct { " +
					"?b a ex:Sth . " +
					"?b ex:prop1 ?b2 . " +
					"?b ex:prop2 ?b3 . " +
				"} " +
				"With " +
					"?b  = uri(ex:b, '/', ?b_id) " +
					"?b2 = uri(ex:b2, '/', ?b2) " +
					"?b3 = uri(ex:key, '/', ?b3) " +
				"From " +
					"B "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData02();
		metric.assessMappings(Arrays.asList(viewDef03(), viewDef04()));
		metric.cleanCaches();

		assertFalse(sink.measureWritten(metricName));
	}


	/* #03
	 * no ambiguity (literals)
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ plainLiteral(?a2)     ]
	 *   a3           [ typedLiteral(?a3, xsd:int) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ plainLiteral(?b2)     ]
	 *   b3           [ typedLiteral(?b3, xsd:int) ]
	 */
	private ViewDefinition viewDef05() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			
			"Create View view05 As " +
				"Construct { " +
					"?a a ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?a ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = plainLiteral(?a2) " +
					"?a3 = typedLiteral(?a3, xsd:int) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}
	
	private ViewDefinition viewDef06() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			
			"Create View view06 As " +
				"Construct { " +
					"?b a ex:Sth . " +
					"?b ex:prop1 ?b2 . " +
					"?b ex:prop2 ?b3 . " +
				"} " +
				"With " +
					"?b  = uri(ex:b, '/', ?b_id) " +
					"?b2 = plainLiteral(?b2) " +
					"?b3 = typedLiteral(?b3, xsd:int) " +
				"From " +
					"B "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData01();
		metric.assessMappings(Arrays.asList(viewDef05(), viewDef06()));
		metric.cleanCaches();

		assertFalse(sink.measureWritten(metricName));
	}


	/* #04
	 * simple ambiguity
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:a2, '/', ?a2)  ]
	 *   a3           [ uri(ex:key, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:b2, '/', ?b2)  ]
	 *   b3           [ uri(ex:key, '/', ?b3) ]
	 */
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData01();
		metric.assessMappings(Arrays.asList(viewDef03(), viewDef04()));
		metric.cleanCaches();

		assertTrue(sink.measureWritten(metricName));
	}


	/* #05
	 * ambiguity (two columns)
	 * 	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:key1, '/', ?a2) ]
	 *   a3           [ uri(ex:key2, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:key1, '/', ?b2) ]
	 *   b3           [ uri(ex:key2, '/', ?b3) ]
	 */
	private ViewDefinition viewDef07() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view07 As " +
				"Construct { " +
					"?a a ex:Sth . " +
					"?a ex:prop1 ?a2 . " +
					"?a ex:prop2 ?a3 . " +
				"} " +
				"With " +
					"?a  = uri(ex:a, '/', ?a_id) " +
					"?a2 = uri(ex:key1, '/', ?a2) " +
					"?a3 = uri(ex:key2, '/', ?a3) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}
	private ViewDefinition viewDef08() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view08 As " +
				"Construct { " +
					"?b a ex:Sth . " +
					"?b ex:prop1 ?b2 . " +
					"?b ex:prop2 ?b3 . " +
				"} " +
				"With " +
					"?b  = uri(ex:b, '/', ?b_id) " +
					"?b2 = uri(ex:key1, '/', ?b2) " +
					"?b3 = uri(ex:key2, '/', ?b3) " +
				"From " +
					"B "
		);
		
		return viewDef;
	}

	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData01();
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));
		metric.cleanCaches();

		assertTrue(sink.measureWritten(metricName));
	}


	/* #06
	 * ambiguity (one column foreign key, other not)
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:key1, '/', ?a2) ]
	 *   a3           [ uri(ex:key2, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:key1, '/', ?b2) ]
	 *   b3 --> A.a3  [ uri(ex:key2, '/', ?b3) ]
	 */
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData02();
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));

		assertTrue(sink.measureWritten(metricName));
	}


	/* #07
	 * ambiguity (one column foreign key to third table)
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:key1, '/', ?a2) ]
	 *   a3           [ uri(ex:key2, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2           [ uri(ex:key1, '/', ?b2) ]
	 *   b3 --> C.c3  [ uri(ex:key2, '/', ?b3) ]
	 * 
	 * table C: not mapped
	 */
	private void initData03() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"c_id integer NOT NULL, " +
					"c2 varchar(30) NOT NULL, " +
					"c3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL REFERENCES C(c3)" +
				");");
	}

	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData03();
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));
		metric.cleanCaches();

		assertTrue(sink.measureWritten(metricName));
	}


	/* #08
	 * ambiguity (one column foreign key to third table, another to original table)
	 * table A:
	 *   a_id         [ uri(ex:a, '/', ?a_id) ]
	 *   a2           [ uri(ex:key1, '/', ?a2) ]
	 *   a3           [ uri(ex:key2, '/', ?a3) ]
	 * table B:
	 *   b_id         [ uri(ex:b, '/', ?b_id) ]
	 *   b2 --> A.a2  [ uri(ex:key1, '/', ?b2) ]
	 *   b3 --> C.c3  [ uri(ex:key2, '/', ?b3) ]
	 * 
	 * table C: not mapped
	 */
	private void initData04() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"c_id integer NOT NULL, " +
					"c2 varchar(30) NOT NULL, " +
					"c3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL REFERENCES A(a2), " +
					"b3 integer NOT NULL REFERENCES C(c3)" +
				");");
	}
	
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();

		initData04();
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));
		metric.cleanCaches();

		assertTrue(sink.measureWritten(metricName));
	}
}
