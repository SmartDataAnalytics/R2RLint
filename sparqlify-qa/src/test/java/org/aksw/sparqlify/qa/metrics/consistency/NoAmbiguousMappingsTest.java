package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

public class NoAmbiguousMappingsTest {
	
	int dbNum = 0;
	
	private BooleanTestingSink sink;
	private ViewDefinitionFactory vdf;

	@Before
	public void setUp() throws Exception {
		sink = new BooleanTestingSink();
		
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
	private Connection db01() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS A;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE A (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS B;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE B (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL" +
				");");
		return conn;
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
	public void test01() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db01();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef01(), viewDef02()));

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
	private Connection db02() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS A;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE A (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS B;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE B (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL REFERENCES A(a3)" +
				");");
		return conn;
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
	public void test02() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db02();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef03(), viewDef04()));

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
	public void test03() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db01();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef05(), viewDef06()));

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
	public void test04() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db01();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef03(), viewDef04()));

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
	public void test05() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db01();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));

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
	public void test06() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db02();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
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
	private Connection db03() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS A;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE A (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS C;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE C (" +
					"c_id integer NOT NULL, " +
					"c2 varchar(30) NOT NULL, " +
					"c3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS B;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE B (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL, " +
					"b3 integer NOT NULL REFERENCES C(c3)" +
				");");
		return conn;
	}

	@Test
	public void test07() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db03();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));

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
	private Connection db04() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS A;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE A (" +
					"a_id integer NOT NULL, " +
					"a2 varchar(30) NOT NULL, " +
					"a3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS C;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE C (" +
					"c_id integer NOT NULL, " +
					"c2 varchar(30) NOT NULL, " +
					"c3 integer NOT NULL" +
				");");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS B;");
		conn.createStatement().executeUpdate(
				"CREATE TABLE B (" +
					"b_id integer NOT NULL, " +
					"b2 varchar(30) NOT NULL REFERENCES A(a2), " +
					"b3 integer NOT NULL REFERENCES C(c3)" +
				");");
		return conn;
	}
	
	@Test
	public void test08() throws NotImplementedException {
		NoAmbiguousMappings metric = new NoAmbiguousMappings();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db04();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);
		metric.assessMappings(Arrays.asList(viewDef07(), viewDef08()));

		assertTrue(sink.measureWritten(metricName));
	}
}
