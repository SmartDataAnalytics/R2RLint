package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class CorrectReificationUseTest {
	
	private float noReificationType = (float) 0;
	private float missingReificationPart = (float) 0.1;
	private float multipleReificationParts= (float) 0.2;
	private float wrongValueType = (float) 0.3;
	private float noViolation = -1;

	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private CorrectReificationUse metric;

	@Before
	public void setUp() throws Exception {
		// initialize dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer.registerViewDefs(viewDefs);
		// init values that are written in different error cases
		metric.setMissingReificationPartVal(missingReificationPart);
		metric.setMultipleReificationPartsVal(multipleReificationParts);
		metric.setNoReificationTypeVal(noReificationType);
		metric.setWrongValueTypeVal(wrongValueType);
	}


	/*
	 * no reification --> no violation
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * #02
	 * 
	 * reification statement without violation
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * #03
	 * 
	 * reification statement without rdf:type rdf:Statement
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertEquals(noReificationType, sink.writtenValue(metricName), 0);
	}


	/*
	 * #04
	 * 
	 * reification statement without rdf:subject part
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertEquals(missingReificationPart, sink.writtenValue(metricName), 0);
	}


	/*
	 * #05
	 * 
	 * reification statement with multiple rdf:subject parts
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/02> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertEquals(multipleReificationParts, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * #06
	 * 
	 * reification statement without rdf:predicate part
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertEquals(missingReificationPart, sink.writtenValue(metricName), 0);
	}


	/*
	 * #07
	 * 
	 * reification statement with multiple rdf:predicate parts
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop02> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertEquals(multipleReificationParts, sink.writtenValue(metricName), 0);
	}


	
	/*
	 * #08
	 * 
	 * reification statement without rdf:object part
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertEquals(missingReificationPart, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * #09
	 * 
	 * reification statement with multiple rdf:object parts
	 */
	public SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"Foo\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset09();
		metric.assessDataset(dataset);
		
		assertEquals(multipleReificationParts, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * #10
	 * 
	 * reification statement with literal value for rdf:subject part
	 */
	public SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> \"resource 01\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * #11
	 * 
	 * reification statement with blank node value for rdf:predicate part
	 */
	public SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> _:b23 ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset11();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}


	/*
	 * #12
	 * 
	 * reification statement with literal value for rdf:predicate part
	 */
	public SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> \"predicate 01\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test12() throws NotImplementedException, SQLException {
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset12();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}


	/*
	 * #13
	 * 
	 * reification statement without any parts (not detected --> no violation)
	 */
	public SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test13() throws NotImplementedException, SQLException {
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset13();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * #14
	 * 
	 * three reification statements; first statement violating
	 */
	public SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> _:b23 ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" ." +
		
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" ." +
			
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test14() throws NotImplementedException, SQLException {
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset14();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}


	/*
	 * #15
	 * 
	 * three reification statements; second statement violating
	 */
	public SparqlifyDataset dataset15() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/res/02> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" ." +
		
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> _:b23 ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" ." +
			
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/prop01> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test15() throws NotImplementedException, SQLException {
		String metricName = "test15";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset15();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}


	/*
	 * #16
	 * 
	 * three reification statements; third statement violating
	 */
	public SparqlifyDataset dataset16() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/pred02> ." +
			"<http://ex.org/res/s01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"23\" ." +
		
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ex.org/pred01> ." +
			"<http://ex.org/res/s02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" ." +
			
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ex.org/res/01> ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> _:b23 ." +
			"<http://ex.org/res/s03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"42\" .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test16() throws NotImplementedException, SQLException {
		String metricName = "test16";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset16();
		metric.assessDataset(dataset);
		
		assertEquals(wrongValueType, sink.writtenValue(metricName), 0);
	}
}
