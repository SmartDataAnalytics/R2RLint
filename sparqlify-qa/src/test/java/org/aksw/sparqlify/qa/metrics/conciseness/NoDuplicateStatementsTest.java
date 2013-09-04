package org.aksw.sparqlify.qa.metrics.conciseness;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

public class NoDuplicateStatementsTest {
	
	private int dbNum = 1;
	private ValueTestingSink sink;
	private ViewDefinitionFactory vdf;


	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
	}


	@Test
	public void test01() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db1();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01()));
		float expected = (float) -1;
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test02() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db2();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01(), viewDef02()));
		float expected = 1 - ( 1 / (float) 22);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test03() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db3();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01(), viewDef02(), viewDef03()));
		float expected = 1 - ( 2 / (float) 33);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test04() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db4();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01()));
		float expected = 1 - ( 1 / (float) 10);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test05() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db5();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01()));
		float expected = 1 - ( 2 / (float) 10);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test06() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db6();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01(), viewDef02()));
		// here first  3 duplicates are reported as above; afterwards the
		// distinct values of a and b are checked --> just one duplicate
		//                                       a              +  b
		//                                  = 10 - 2 duplicates + 10
		float expected = 1 - ( 1 / (float) 18);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	@Test
	public void test07() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db1();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef04()));
		float expected = 1 - ( 10 / (float) 20);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}
	
	@Test
	public void test08() throws NotImplementedException {
		NoDuplicateStatements metric = new NoDuplicateStatements();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Connection conn = null;
		try {
			conn = db7();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		metric.registerDbConnection(conn);

		metric.assessMappings(Arrays.asList(viewDef01(), viewDef05()));
		float expected = 1 - ( 1 / (float) 20);
		assertEquals(expected, sink.mappingMeasureValue(metricName), 0);
	}


	private ViewDefinition viewDef01() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view1 As " +
				"Construct { " +
					"?n a ex:Sth . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?num) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"A"
		);
		
		return viewDef;
	}


	private ViewDefinition viewDef02() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view2 As " +
				"Construct { " +
					"?n a ex:Sth . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?num) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"B"
		);
		
		return viewDef;
	}


	private ViewDefinition viewDef03() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view3 As " +
				"Construct { " +
					"?n a ex:Sth . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?num) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"c "
		);
		
		return viewDef;
	}


	private ViewDefinition viewDef04() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view4 As " +
				"Construct { " +
					"?n a ex:Sth . " +
					"?n rdfs:label ?l . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?num) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"A "
		);
		
		return viewDef;
	}


	private ViewDefinition viewDef05() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view5 As " +
				"Construct { " +
					"?n a ex:Sth . " +
					"?n rdfs:label ?l . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?num) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"[[" +
						"SELECT C.num AS num, B.word AS word, C.checked " +
						"FROM " +
							"B " +
						"JOIN " +
							"C " +
						"ON B.num = C.num" +
					"]]"
		);
		
		return viewDef;
	}

	private Connection db1() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'six', 6);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'seven', 7);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		
		return conn;
	}


	private Connection db2() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'six', 6);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'seven', 7);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(676, 'dup', 23);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(556, 'ten', 10);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(557, 'eleven', 11);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(558, 'twelve', 12);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(559, 'thirteen', 13);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(560, 'fourteen', 14);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(561, 'fifteen', 15);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(562, 'sixteen', 16);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(563, 'seventeen', 17);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(564, 'eighteen', 18);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(565, 'nineteen', 19);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(566, 'dup', 23);");
		
		return conn;
	}


	private Connection db3() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'six', 6);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'seven', 7);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(676, 'dup', 23);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(555, 'ten', 10);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(556, 'eleven', 11);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(557, 'twelve', 12);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(558, 'thirteen', 13);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(559, 'fourteen', 14);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(510, 'fifteen', 15);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(511, 'sixteen', 16);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(512, 'seventeen', 17);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(513, 'eighteen', 18);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(514, 'nineteen', 19);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(515, 'dup', 23);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(444, 'thirty', 30);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(445, 'thirtyone', 31);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(446, 'thirtytwo', 32);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(447, 'thirtythree', 33);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(448, 'thirtyfout', 34);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(449, 'thirtyfive', 35);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(450, 'thirtysix', 36);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(451, 'thirtyseven', 37);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(452, 'thirtyeight', 38);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(453, 'thirtynine', 39);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(454, 'dup', 23);");
		return conn;
	}


	private Connection db4() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'seven', 7);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		
		return conn;
	}


	private Connection db5() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		
		return conn;
	}


	private Connection db6() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(555, 'ten', 10);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(556, 'eleven', 11);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(557, 'twelve', 12);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(558, 'thirteen', 13);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(559, 'fourteen', 14);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(510, 'fifteen', 15);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(511, 'sixteen', 16);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(512, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(513, 'eighteen', 18);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(514, 'nineteen', 19);");
		
		return conn;
	}


	private Connection db7() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test" + dbNum++ + ";MODE=PostgreSQL;" +
				"DB_CLOSE_DELAY=-1");
		
		ds.setUser("test");
		ds.setPassword("test");
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(666, 'zero', 0);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(667, 'one', 1);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(669, 'three', 3);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(670, 'four', 4);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(671, 'five', 5);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(672, 'six', 6);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(673, 'seven', 7);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(674, 'eight', 8);");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(675, 'nine', 9);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer" +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(555, 'ten', 10);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(556, 'eleven', 11);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(557, 'twelve', 12);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(558, 'thirteen', 13);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(559, 'fourteen', 14);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(510, 'fifteen', 15);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(511, 'sixteen', 16);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(512, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(513, 'eighteen', 18);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(514, 'nineteen', 19);");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"id integer NOT NULL, " +
					"num integer, " +
					"checked boolean " +
				");");
		
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(444, 10, false);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(445, 11, true);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(446, 12, true);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(447, 13, false);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(448, 14, true);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(449, 15, false);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(450, 16, false);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(451, 2, true);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(452, 18, false);");
		conn.createStatement().executeUpdate("INSERT INTO c VALUES(453, 19, true);");
		
		
		return conn;
	}
}
