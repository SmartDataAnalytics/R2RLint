package org.aksw.sparqlify.qa.metrics.conciseness;

import static org.junit.Assert.assertEquals;

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
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
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
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class NoDuplicateStatementsTest {

	@Autowired
	private DataSource rdb;
	private Connection conn;
	@Autowired
	private MeasureDataSink sink;
	@Autowired
	private NoDuplicateStatements metric;
	private ViewDefinitionFactory vdf;

	@Before
	public void setUp() throws Exception {
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
	}
	
	@After
	public void tearDown() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
	}

	@PostConstruct
	private void init() throws SQLException {
		conn = rdb.getConnection();
	}
	
	@PreDestroy
	private void cleanUp() throws SQLException {
		conn.close();
	}
	
	/*
	 * no duplications
	 */
	private void initData01() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
						"id integer NOT NULL, " +
						"word varchar(30), " +
						"num integer);");
		
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
					"A");
		
		return viewDef;
	}

	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();

		initData01();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01()));
		float expected = (float) -1;
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * one duplication ('dup', 23) out of 22 entities
	 */
	private void initData02() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
						"id integer NOT NULL, " +
						"word varchar(30), " +
						"num integer);");
		
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
		
		conn.createStatement().executeUpdate("CREATE TABLE b (" +
				"id integer NOT NULL, " +
				"word varchar(30), " +
				"num integer);");
		
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
					"B");
		
		return viewDef;
	}
	
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();

		initData02();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01(), viewDef02()));
		float expected = 1 - ( 1 / (float) 22);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * two duplications ('dup', 23) out of 33 entities
	 */
	private void initData03() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
					"C ");
		
		return viewDef;
	}

	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData03();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01(), viewDef02(), viewDef03()));
		float expected = 1 - ( 2 / (float) 33);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * one duplicate ('two', 2) out of 10 entities
	 */
	private void initData04() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
	}

	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData04();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01()));
		float expected = 1 - ( 1 / (float) 10);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * two duplicates ('two', 2) out of 10 entities
	 */
	private void initData05() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
	}

	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData05();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01()));
		float expected = 1 - ( 2 / (float) 10);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * two duplicates ('two', 2) out of 10 entities (sink write overridden by
	 * the following write);
	 * one duplicate ('two', 2) out of 18 entities (20 entities - 2 duplicates
	 * already found above)
	 * 
	 * --> intention for the underlying logic is not to report duplicates more
	 * than once for different levels:
	 * - table level duplicates reported once (two duplicates)
	 * - database level duplicates reported once (one duplicate: a('two', 2),
	 *   b('two',2))
	 */
	private void initData06() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE a (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
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
	}

	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData06();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01(), viewDef02()));
		// here first  2 duplicates are reported as above; afterwards the
		// distinct values of a and b are checked --> just one duplicate
		//                                       a              +  b
		//                                  = 10 - 2 duplicates + 10
		float expected = 1 - ( 1 / (float) 18);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * 10 DB entities used twice: 2 x 10 entities total, 10 duplicates
	 */
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
					"A ");
		
		return viewDef;
	}

	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData01();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef04()));
		
		float expected = 1 - ( 10 / (float) 20);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}


	/*
	 * SELECT C.num AS num, B.word AS word, C.checked FROM B JOIN C ON B.num = C.num
	 * 
	 * join hits: 10
	 * (555, 'ten', 10, 444, 10, false)
	 * (556, 'eleven', 11, 445, 11, true)
	 * (557, 'twelve', 12, 446, 12, true)
	 * (558, 'thirteen', 13, 447, 13, false)
	 * (559, 'fourteen', 14, 448, 14, true)
	 * (560, 'fifteen', 15, 449, 15, false)
	 * (561, 'sixteen', 16, 450, 16, false)
	 * (562, 'two', 2, 451, 2, true)
	 * (563, 'eighteen', 18, 452, 18, false)
	 * (564, 'nineteen', 19, 453, 19, true)
	 * 
	 * all 10 hits are used twice --> 20 entities involved, 10 duplicates
	 * 
	 *  +
	 *  
	 * another 10 entities from table A adding one duplicate ('two', 2)
	 * 
	 * ----
	 * 
	 * 30 entities, 11 duplicates
	 */
	private void initData07() throws SQLException {
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS a;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS b;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS c;");
		
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
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE b (" +
					"id integer NOT NULL, " +
					"word varchar(30), " +
					"num integer);");
		
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(555, 'ten', 10);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(556, 'eleven', 11);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(557, 'twelve', 12);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(558, 'thirteen', 13);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(559, 'fourteen', 14);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(560, 'fifteen', 15);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(561, 'sixteen', 16);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(668, 'two', 2);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(563, 'eighteen', 18);");
		conn.createStatement().executeUpdate("INSERT INTO b VALUES(564, 'nineteen', 19);");
		
		conn.createStatement().executeUpdate(
				"CREATE TABLE c (" +
					"id integer NOT NULL, " +
					"num integer, " +
					"checked boolean);");
		
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
	}
	
	/*
	 * viewDef01:
	 * 
	 *  Prefix ex: <http://ex.org/>
	 *  Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	 *  Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
	 *  
	 *  Create View view1 As
	 *  	Construct {
	 *  		?n a ex:Sth .
	 *  		?n rdfs:label ?l .
	 *  	}
	 *  	With
	 *  		?n = uri(ex:number, '/', ?num)
	 *  		?l = plainLiteral(?word)
	 *  	From
	 *  		A
	 */
	private ViewDefinition viewDef05() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view5 As " +
				"Construct { " +
					"?n rdfs:label ?l . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?numval) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"[[" +
						"SELECT C.num AS numval, B.word AS word, C.checked " +
						"FROM " +
							"B " +
						"JOIN " +
							"C " +
						"ON B.num = C.num" +
					"]]"
		);
		
		return viewDef;
	}
	
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData07();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef01(), viewDef05()));
		float expected = 1 - ( 11 / (float) 30);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}
	
	
	/*
	 * viewDef01:
	 * 
	 *  Prefix ex: <http://ex.org/>
	 *  Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	 *  Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
	 *  
	 *  Create View view1 As
	 *  	Construct {
	 *  		?n a ex:Sth .
	 *  		?n rdfs:label ?l .
	 *  	}
	 *  	With
	 *  		?n = uri(ex:number, '/', ?num)
	 *  		?l = plainLiteral(?word)
	 *  	From
	 *  		A
	 */
	private ViewDefinition viewDef06() {
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
						"?n = uri(ex:number, '/', ?id, '-', ?num) " +
						"?l = plainLiteral(?word) " +
					"From " +
						"A"
		);
		
		return viewDef;
	}
	
	private ViewDefinition viewDef07() {
		ViewDefinition viewDef = vdf.create(
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			
			"Create View view5 As " +
				"Construct { " +
					"?n rdfs:label ?l . " +
					"?n rdfs:label ?l . " +
				"} " +
				"With " +
					"?n = uri(ex:number, '/', ?id, '-', ?numval) " +
					"?l = plainLiteral(?word) " +
				"From " +
					"[[" +
						"SELECT B.id AS id, C.num AS numval, B.word AS word " +
						"FROM " +
							"B " +
						"JOIN " +
							"C " +
						"ON B.num = C.num" +
					"]]"
		);
		
		return viewDef;
	}
	
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		initData07();
		metric.flushCaches();
		metric.assessViews(Arrays.asList(viewDef06(), viewDef07()));
		float expected = 1 - ( 11 / (float) 30);
		assertEquals(expected, ((ValueTestingSink) sink).writtenValue(metricName), 0);
	}
}
