package org.aksw.sparqlify.qa.metrics.consistency;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
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

public class HomogeneousDatatypesTest {

	ValueTestingSink sink;
	ViewDefinitionFactory vdf;


	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
		
		Map<String, String> typeAlias = MapReader.read(
				new File("src/test/resources/type-map.h2.tsv"));
		vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
		
		
	}

	/*
	 * no inhomogeneity
	 */
	@Test
	public void test01() throws NotImplementedException {
		HomogeneousDatatypes metric = new HomogeneousDatatypes();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Pinpointer pinpointer = new Pinpointer(viewDefs01());
		metric.registerPinpointer(pinpointer);
		metric.setThreshold((float) 0.9);
		metric.setOutlierValue((float) 0.5);
		metric.setConflictValue(0);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = (float) -1;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * inhomogeneity (outlier)
	 */
	@Test
	public void test02() throws NotImplementedException {
		HomogeneousDatatypes metric = new HomogeneousDatatypes();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Pinpointer pinpointer = new Pinpointer(viewDefs02());
		metric.registerPinpointer(pinpointer);
		metric.setThreshold((float) 0.8);
		metric.setOutlierValue((float) 0.5);
		metric.setConflictValue(0);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = (float) 0.5;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * inhomogeneity (conflict)
	 */
	@Test
	public void test03() throws NotImplementedException {
		HomogeneousDatatypes metric = new HomogeneousDatatypes();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		Pinpointer pinpointer = new Pinpointer(viewDefs02());
		metric.registerPinpointer(pinpointer);
		metric.setThreshold((float) 0.95);
		metric.setOutlierValue((float) 0.5);
		metric.setConflictValue(0);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		float expected = (float) 0.0;
		assertEquals(
				expected,
				sink.writtenValue(metricName),
				0);
	}


	/*
	 * no inhomogeneity 
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/res/01> <http://ex.org/pred01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/02> <http://ex.org/pred01> \"29\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/03> <http://ex.org/pred01> \"33\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/04> <http://ex.org/pred01> \"42\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/05> <http://ex.org/pred01> \"56\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/06> <http://ex.org/pred01> \"73\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/07> <http://ex.org/pred01> \"99\"^^<http://www.w3.org/2001/XMLSchema#int> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}


	/*
	 * inhomogeneity with outlier (0.8 vs 0.2)
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/res/01> <http://ex.org/pred01> \"07\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/02> <http://ex.org/pred01> \"11\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/03> <http://ex.org/pred01> \"27\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/04> <http://ex.org/pred01> \"37\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/05> <http://ex.org/pred01> \"49\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/06> <http://ex.org/pred01> \"57\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/06> <http://ex.org/pred01> \"Hello! I'm wrong\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/06> <http://ex.org/pred01> \"42\"^^<http://www.w3.org/2001/XMLSchema#int> . " +
			"<http://ex.org/res/06> <http://ex.org/pred01> \"Me, too\"^^<http://www.w3.org/2001/XMLSchema#string> . " +
			"<http://ex.org/res/07> <http://ex.org/pred01> \"23\"^^<http://www.w3.org/2001/XMLSchema#int> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}


	public Collection<ViewDefinition> viewDefs01() {
		String viewDef01Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			"Create View view01 As " +
				"Construct { " +
					"?r ex:pred01 ?v . " +
				"} " +
				"With " +
					"?r = uri(ex:res, '/', ?id) " +
					"?v = typedLiteral(?foo, xsd:int) " +
				"From " +
					"A";
		
		ViewDefinition viewDef01 = vdf.create(viewDef01Str);
		
		return Arrays.asList(viewDef01);
	}


	public Collection<ViewDefinition> viewDefs02() {
		String viewDef01Str =
			"Prefix ex: <http://ex.org/> " +
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			"Create View view01 As " +
				"Construct { " +
					"?r ex:pred01 ?v . " +
				"} " +
				"With " +
					"?r = uri(ex:res, '/', ?id) " +
					"?v = typedLiteral(?foo, xsd:int) " +
				"From " +
					"A";
		
		String viewDef02Str =
				"Prefix ex: <http://ex.org/> " +
				"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"Prefix owl: <http://www.w3.org/2002/07/owl#> " +
				"Prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
				"Create View view02 As " +
					"Construct { " +
						"?r ex:pred01 ?v . " +
					"} " +
					"With " +
						"?r = uri(ex:res, '/', ?id) " +
						"?v = typedLiteral(?foo, xsd:string) " +
					"From " +
						"B";
		ViewDefinition viewDef01 = vdf.create(viewDef01Str);
		ViewDefinition viewDef02 = vdf.create(viewDef02Str);
		
		return Arrays.asList(viewDef01, viewDef02);
	}
}
