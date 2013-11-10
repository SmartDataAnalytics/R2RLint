package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class BasicOntologyConformanceTest {

	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private BasicOntologyConformance metric;
	@Autowired
	private Pinpointer pinpointer;
	ViewDefinitionFactory vdf;


	@Before
	public void setUp() throws Exception {
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
		// this pinpointer is the same as registered inside the metric, so
		// initializing this.pinpointer also initializes also the pinpointer
		// inside the metric
		pinpointer.registerViewDefs(new ArrayList<ViewDefinition>());
	}
	
	
	/*
	 * should violate distinct classes statement 
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	public Collection<ViewDefinition> viewDefs01() {
		String viewDef01Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#>" +
			"Create View view01 As " +
				"Construct { " +
					"?c1 a owl:Class . " +
					"?c2 a owl:Class . " +
					"?c1 owl:disjointWith ?c2 . " +
					"?r1 a ?c1 . " +
					"?r1 a ?c2 . " +
				"} " +
				"With " +
					"?c1 = uri(ex:Class, ?c1_id) " +
					"?c2 = Uri(ex:Class, ?c2_id) " +
					"?r1 = uri(ex:res, '/', ?id) " +
				"From " +
					"A";
		
		ViewDefinition viewDef01 = vdf.create(viewDef01Str);
		
		return Arrays.asList(viewDef01);
	}
	
	/*
	 * disjointClass
	 */
	@Test
	public synchronized void test01() throws NotImplementedException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		pinpointer.registerViewDefs(viewDefs01());
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(BasicOntologyConformance.disjointClassesConformance),
				0);
	}

	/*
	 * should violate range restriction
	 */
	public SparqlifyDataset dataset02() {
		String content =
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2001/XMLSchema#int> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"Not an int\" .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	public Collection<ViewDefinition> viewDefs02() {
		String viewDef02Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#>" +
			"Create View view02 As " +
				"Construct { " +
					"?r1 ex:prop01 ?v . " +
				"} " +
				"With " +
					"?r1 = uri(ex:res, '/', ?id) " +
					"?v = plainLiteral(?foo) " +
				"From " +
					"A";
		
		String viewDef03Str = 
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			"Create View view03 As " +
				"Construct { " +
					"ex:prop1 rdfs:range xsd:int" +
				"} ";
		
		ViewDefinition viewDef02 = vdf.create(viewDef02Str);
		ViewDefinition viewDef03 = vdf.create(viewDef03Str);
		
		return Arrays.asList(viewDef02, viewDef03);
	}
	
	/*
	 * rdfs:range
	 */
	@Test
	public synchronized void test02() throws NotImplementedException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		pinpointer.registerViewDefs(viewDefs02());
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(BasicOntologyConformance.validRange),
				0);
	}
	
	
	/*
	 * should violate domain restriction --> reported as disjointClass violation 
	 */
	public SparqlifyDataset dataset03() {
		String content =
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#domain> <http://ex.org/Class02> . " +
			"<http://ex.org/Class01> <http://www.w3.org/2002/07/owl#disjointWith> <http://ex.org/Class02> . " +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	public Collection<ViewDefinition> viewDefs03() {
		String viewDef04Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Create View view04 As " +
				"Construct { " +
					"?r a ex:Class01 . " +
					"?r ex:prop01 ?v . " +
				"} " +
				"With " +
					"?r = uri(ex:res, '/', ?id) " +
					"?v = plainLiteral(?foo) " +
				"From " +
					"A";
		
		String viewDef05Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"Create View view05 As " +
				"Construct {" +
					"ex:prop01 rdfs:domain ex:Class02 . " +
					"ex:Class01 owl:disjointWith ex:Class02 . " +
				"}";
		
		ViewDefinition viewDef04 = vdf.create(viewDef04Str);
		ViewDefinition viewDef05 = vdf.create(viewDef05Str);
		
		return Arrays.asList(viewDef04, viewDef05);
	}
	
	/*
	 * rdfs:domain violation; reported as disjointClass violation
	 */
	@Test
	public synchronized void test03() throws NotImplementedException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		pinpointer.registerViewDefs(viewDefs03());
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(BasicOntologyConformance.disjointClassesConformance),
				0);
	}


	/*
	 * should violate objectProperty restriction
	 */
	public SparqlifyDataset dataset04() {
		String conten =
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"42\"";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(conten);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	public Collection<ViewDefinition> viewDefs04() {
		String viewDef06Str =
			"Prefix ex: <http://ex.org/> " +
			"Create View view06 As " +
				"Construct { " +
					"?r ex:prop01 ?v . " +
				"} " +
				"With " +
					"?r = uri(ex:res, '/', ?id) " +
					"?v = plainLiteral(?foo) " +
				"From " +
					"A";
		
		String viewDef07Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"Create View view07 As " +
				"Construct {" +
					"ex:prop a owl:ObjectProperty . " +
				"}";
		
		ViewDefinition viewDef06 = vdf.create(viewDef06Str);
		ViewDefinition viewdef07 = vdf.create(viewDef07Str);
		
		return Arrays.asList(viewDef06, viewdef07);
	}
	
	/*
	 * owl:ObjectProperty violation
	 */
	@Test
	public synchronized void test04() throws NotImplementedException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		pinpointer.registerViewDefs(viewDefs04());
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		// this is just a warning --> 0.5 instead of 0
		float expected = (float) 0.5;
		assertEquals(
				expected,
				sink.writtenValue(BasicOntologyConformance.correctObjPropValue),
				0);
	}


	/*
	 * should violate datatype property restriction
	 */
	public SparqlifyDataset dataset05() {
		String content =
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . " +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . " +
			"<http://ex.org/prop02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> . " +
			"<http://ex.org/Class01> <http://ex.org/prop02> <http://ex.org/Class02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	public Collection<ViewDefinition> viewDefs05() {
		String viewDef08Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"Create View view08 As " +
				"Construct { " +
					"ex:Class01 a owl:Class . " +
					"ex:Class02 a owl:Class . " +
					"ex:prop02 a owl:DatatypeProperty . " +
					"ex:Class01 ex:prop02 ex:Class02 . " +
				"} ";
		
		ViewDefinition viewDef08 = vdf.create(viewDef08Str);
		
		return Arrays.asList(viewDef08);
	}
	
	/*
	 * owl:DatatypeProperty violation
	 */
	@Test
	public synchronized void test05() throws NotImplementedException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		pinpointer.registerViewDefs(viewDefs05());
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		float expected = (float) 0;
		assertEquals(
				expected,
				sink.writtenValue(BasicOntologyConformance.correctDtPropValue),
				0);
	}
}
