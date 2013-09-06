package org.aksw.sparqlify.qa.metrics.completeness;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

public class PopulationCompletenessTest {

	private Connection conn1;
	private Connection conn2;
	private Connection conn3;
	private Connection conn4;
	private ValueTestingSink sink;
	private String dumpFilePath = "src/test/resources/dump.ttl";
	private SparqlifyDataset dataset;


	@Before
	public void setUp() throws Exception {
		initDBs();
		initDB1Content(conn1);
		initDB2Content(conn2);
		initDB3Content(conn3);
		initDB4Content(conn4);
		
		sink = new ValueTestingSink();
		dataset = new SparqlifyDataset();
		dataset.readFromDump(dumpFilePath);
	}


	/*
	 * dataset: 16 unique resources
	 * DB: non-M:N, 4 + 9 = 13 entities
	 */
	@Test
	public void test01() throws NotImplementedException {
		PopulationCompleteness metric = new PopulationCompleteness();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerDbConnection(conn1);
		
		metric.assessDataset(dataset);
		float expected = (float) 16/(float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * dataset: 16 unique resources
	 * DB: N:1, 4 + 8 = 12 entities
	 */
	@Test
	public void test02() throws NotImplementedException {
		PopulationCompleteness metric = new PopulationCompleteness();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerDbConnection(conn2);
		
		metric.assessDataset(dataset);
		float expected = (float) 16/(float) 12;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * dataset: 16 unique resources
	 * DB: M:N relation holding tuples that can be considered as own entities;
	 * 4 + 8 + 6 = 18 entities
	 */
	@Test
	public void test03() throws NotImplementedException {
		PopulationCompleteness metric = new PopulationCompleteness();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerDbConnection(conn3);
		
		metric.assessDataset(dataset);
		float expected = (float) 16/(float) 18;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * dataset: 16 unique resources
	 * DB: real M:N relation holding tuples that should not be considered as
	 * own entities; 4 + 8 = 12 entities
	 */
	@Test
	public void test04() throws NotImplementedException {
		PopulationCompleteness metric = new PopulationCompleteness();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerDbConnection(conn4);
		
		metric.assessDataset(dataset);
		float expected = (float) 16/(float) 12;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	private void initDBs() throws SQLException {
		// DB 1
		JdbcDataSource ds1 = new JdbcDataSource();
		ds1.setURL("jdbc:h2:mem:test1;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds1.setUser("test");
		ds1.setPassword("test");
		conn1 = ds1.getConnection();
		
		// DB 2
		JdbcDataSource ds2 = new JdbcDataSource();
		ds2.setURL("jdbc:h2:mem:test2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds2.setUser("test");
		ds2.setPassword("test");
		conn2 = ds2.getConnection();
		
		// DB 3
		JdbcDataSource ds3 = new JdbcDataSource();
		ds3.setURL("jdbc:h2:mem:test3;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds3.setUser("test");
		ds3.setPassword("test");
		conn3 = ds3.getConnection();
		
		// DB 4
		JdbcDataSource ds4 = new JdbcDataSource();
		ds4.setURL("jdbc:h2:mem:test4;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds4.setUser("test");
		ds4.setPassword("test");
		conn4 = ds4.getConnection();
	}

	/*
	 * initialize DB with no M:N relations
	 * 
	 * no. of entities: 4 + 9 = 13
	 */
	private void initDB1Content(Connection conn) throws SQLException {
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("CREATE TABLE a (" +
				"id integer NOT NULL, " +
				"name character varying(400) NOT NULL );");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
				"id integer NOT NULL, " +
				"name character varying(40) NOT NULL, " +
				"date date NOT NULL, " +
				"web_id character varying(355));");

		conn.createStatement().executeUpdate("INSERT INTO a VALUES(1, 'one');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(2, 'two');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(3, 'three');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(4, 'four');");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"1, 'alpha', '1979-01-05', 'http://ex.org/foo/alpha');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"2, 'beta', '1968-02-28', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"3, 'gamma', '1983-09-17', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"4, 'delta', '1990-12-04', 'http://ex.org/foo/delta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"5, 'epsilon', '1960-03-03', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"6, 'zeta', '0975-02-20', 'http://ex.org/foo/zeta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"7, 'eta', '1980-12-14', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"8, 'theta', '1987-01-23', 'http://ex.org/foo/theta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"9, 'iota', '1990-02-16', NULL);");
		
		conn.createStatement().executeUpdate("ALTER TABLE a " +
				"ADD CONSTRAINT a_pkey PRIMARY KEY (id);");

		conn.createStatement().executeUpdate("ALTER TABLE b " +
				"ADD CONSTRAINT b_pkey PRIMARY KEY (id);");
	}


	/*
	 * initialize DB with N:1 relation
	 * 
	 * no. of entities: 4 + 8 = 12
	 */
	private void initDB2Content(Connection conn) throws SQLException {

		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("CREATE TABLE a (" +
				"id integer NOT NULL, " +
				"name character varying(400) NOT NULL );");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
				"id integer NOT NULL, " +
				"name character varying(40) NOT NULL, " +
				"date date NOT NULL, " +
				"web_id character varying(355)," +
				"a_id integer);");

		conn.createStatement().executeUpdate("INSERT INTO a VALUES(1, 'one');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(2, 'two');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(3, 'three');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(4, 'four');");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"1, 'alpha', '1979-01-05', 'http://ex.org/foo/alpha', 4);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"2, 'beta', '1968-02-28', NULL, 3);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"3, 'gamma', '1983-09-17', NULL, 2);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"4, 'delta', '1990-12-04', 'http://ex.org/foo/delta', 1);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"5, 'epsilon', '1960-03-03', NULL, 4);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"6, 'zeta', '0975-02-20', 'http://ex.org/foo/zeta', 3);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"7, 'eta', '1980-12-14', NULL, 2);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"8, 'theta', '1987-01-23', 'http://ex.org/foo/theta', 1);");
		
		conn.createStatement().executeUpdate("ALTER TABLE a " +
				"ADD CONSTRAINT a_pkey PRIMARY KEY (id);");

		conn.createStatement().executeUpdate("ALTER TABLE b " +
				"ADD CONSTRAINT b_pkey PRIMARY KEY (id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE b " +
				"ADD CONSTRAINT a_id_fkey FOREIGN KEY (a_id) REFERENCES a(id);");
	}


	/*
	 * initialize DB with M:N relation holding tuples that can be considered as
	 * own entities
	 * 
	 * no. of entities: 4 + 8 + 6 = 18
	 */
	private void initDB3Content(Connection conn) throws SQLException {
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("CREATE TABLE a (" +
				"id integer NOT NULL, " +
				"name character varying(400) NOT NULL );");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
				"id integer NOT NULL, " +
				"name character varying(40) NOT NULL, " +
				"date date NOT NULL, " +
				"web_id character varying(355));");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS ab;");
		conn.createStatement().executeUpdate("CREATE TABLE ab (" +
				"a_id integer NOT NULL, " +
				"b_id integer NOT NULL, " +
				"date date NOT NULL );");

		conn.createStatement().executeUpdate("INSERT INTO a VALUES(1, 'one');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(2, 'two');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(3, 'three');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(4, 'four');");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"1, 'alpha', '1979-01-05', 'http://ex.org/foo/alpha');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"2, 'beta', '1968-02-28', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"3, 'gamma', '1983-09-17', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"4, 'delta', '1990-12-04', 'http://ex.org/foo/delta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"5, 'epsilon', '1960-03-03', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"6, 'zeta', '0975-02-20', 'http://ex.org/foo/zeta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"7, 'eta', '1980-12-14', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"8, 'theta', '1987-01-23', 'http://ex.org/foo/theta');");
		
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"1, 2, '1999-12-23');");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"1, 2, '1999-12-24');");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"1, 3, '1999-04-12');");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"2, 1, '2002-12-23');");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"2, 3, '2001-06-11');");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(" +
				"4, 2, '1999-12-23');");
		
		conn.createStatement().executeUpdate("ALTER TABLE a " +
				"ADD CONSTRAINT a_pkey PRIMARY KEY (id);");

		conn.createStatement().executeUpdate("ALTER TABLE b " +
				"ADD CONSTRAINT b_pkey PRIMARY KEY (id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT ab_pkey PRIMARY KEY (a_id, b_id, date);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT a_id_fkey FOREIGN KEY (a_id) REFERENCES a(id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT b_id_fkey FOREIGN KEY (b_id) REFERENCES b(id);");
	}


	/*
	 * initialize DB with real M:N relation holding tuples that should not be
	 * considered as own entities
	 * 
	 * no. of entities: 4 + 8 = 12
	 */
	private void initDB4Content(Connection conn) throws SQLException {
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("CREATE TABLE a (" +
				"id integer NOT NULL, " +
				"name character varying(400) NOT NULL );");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
				"id integer NOT NULL, " +
				"name character varying(40) NOT NULL, " +
				"date date NOT NULL, " +
				"web_id character varying(355));");
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS ab;");
		conn.createStatement().executeUpdate("CREATE TABLE ab (" +
				"a_id integer NOT NULL, " +
				"b_id integer NOT NULL);");

		conn.createStatement().executeUpdate("INSERT INTO a VALUES(1, 'one');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(2, 'two');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(3, 'three');");
		conn.createStatement().executeUpdate("INSERT INTO a VALUES(4, 'four');");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"1, 'alpha', '1979-01-05', 'http://ex.org/foo/alpha');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"2, 'beta', '1968-02-28', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"3, 'gamma', '1983-09-17', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"4, 'delta', '1990-12-04', 'http://ex.org/foo/delta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"5, 'epsilon', '1960-03-03', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"6, 'zeta', '0975-02-20', 'http://ex.org/foo/zeta');");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"7, 'eta', '1980-12-14', NULL);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(" +
				"8, 'theta', '1987-01-23', 'http://ex.org/foo/theta');");
		
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(1, 2);");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(1, 3);");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(2, 1);");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(2, 3);");
		conn.createStatement().executeUpdate("INSERT INTO ab VALUES(4, 2);");
		
		conn.createStatement().executeUpdate("ALTER TABLE a " +
				"ADD CONSTRAINT a_pkey PRIMARY KEY (id);");

		conn.createStatement().executeUpdate("ALTER TABLE b " +
				"ADD CONSTRAINT b_pkey PRIMARY KEY (id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT ab_pkey PRIMARY KEY (a_id, b_id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT a_id_fkey FOREIGN KEY (a_id) REFERENCES a(id);");
		
		conn.createStatement().executeUpdate("ALTER TABLE ab " +
				"ADD CONSTRAINT b_id_fkey FOREIGN KEY (b_id) REFERENCES b(id);");
	}
}
