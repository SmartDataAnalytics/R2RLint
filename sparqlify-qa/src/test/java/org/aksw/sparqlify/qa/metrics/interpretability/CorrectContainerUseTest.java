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

public class CorrectContainerUseTest {
	
	float noTypeAssigned = (float) 0;
	float multipleTypesAssigned = (float) 0.1;
	float duplicateContMembProps= (float) 0.2;
	float noConsecutiveNumbering = (float) 0.3;
	float leadingZero = (float) 0.4;
	float memberIsLiteral = (float) 0.5;
	float noViolation = (float) -1;
	
	ValueTestingSink sink;
	Pinpointer pinpointer;

	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
		// initialize dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
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
	public void test01() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test02() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test03() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test04() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test05() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test06() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test07() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test08() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test09() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
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
	public void test10() throws NotImplementedException {
		CorrectContainerUse metric = new CorrectContainerUse();
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		// init values that are written in different error cases
		metric.setDuplicateContMembPropsVal(duplicateContMembProps);
		metric.setLeadingZeroVal(leadingZero);
		metric.setMemberIsLiteralVal(memberIsLiteral);
		metric.setMultipleTypesAssignedVal(multipleTypesAssigned);
		metric.setNoConsecutiveNumberingVal(noConsecutiveNumbering);
		metric.setNoTypeAssignedVal(noTypeAssigned);
		
		SparqlifyDataset dataset = dataset10();
		metric.assessDataset(dataset);
		
		assertEquals(leadingZero, sink.writtenValue(metricName), 0);
	}
}
