package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
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
public class CorrectContainerUseTest {
	
	float noTypeAssigned = (float) 0;
	float multipleTypesAssigned = (float) 0.1;
	float duplicateContMembProps= (float) 0.2;
	float noConsecutiveNumbering = (float) 0.3;
	float leadingZero = (float) 0.4;
	float memberIsLiteral = (float) 0.5;
	float noViolation = (float) -1;
	
	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private CorrectContainerUse metric;

	@Before
	public void setUp() throws Exception {
		// initialize dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer.registerViewDefs(viewDefs);
		
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
	}


	/*
	 * no container --> no violation
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> \"23\" ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test01() throws NotImplementedException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * container with no violation
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test02() throws NotImplementedException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * container without type
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test03() throws NotImplementedException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertEquals(noTypeAssigned, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with multiple types
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test04() throws NotImplementedException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertEquals(multipleTypesAssigned, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with duplicate member numbers
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m73> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test05() throws NotImplementedException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertEquals(duplicateContMembProps, sink.writtenValue(metricName), 0);
	}

	
	/*
	 * container with literal member
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> \"m05\" ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test06() throws NotImplementedException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertEquals(memberIsLiteral, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with a gap of 1 (detected)
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test07() throws NotImplementedException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertEquals(noConsecutiveNumbering, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with a gap of 2 (not detected)
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test08() throws NotImplementedException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		// "noViolation" since the actual violation is not detected
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with numbering starting with 0
	 */
	public SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_0> <http://ex.org/res/m00> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_1> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_2> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_3> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_4> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_5> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_6> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_7> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_8> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_9> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test09() throws NotImplementedException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset09();
		metric.assessDataset(dataset);
		
		assertEquals(leadingZero, sink.writtenValue(metricName), 0);
	}


	/*
	 * container with numbering with leading zeros
	 */
	public SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_01> <http://ex.org/res/m01> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_02> <http://ex.org/res/m02> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_03> <http://ex.org/res/m03> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_04> <http://ex.org/res/m04> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_05> <http://ex.org/res/m05> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_06> <http://ex.org/res/m06> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_07> <http://ex.org/res/m07> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_08> <http://ex.org/res/m08> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_09> <http://ex.org/res/m09> ." +
			"<http://ex.org/res/c01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#_10> <http://ex.org/res/m10> .";

		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test10() throws NotImplementedException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertEquals(leadingZero, sink.writtenValue(metricName), 0);
	}
}
