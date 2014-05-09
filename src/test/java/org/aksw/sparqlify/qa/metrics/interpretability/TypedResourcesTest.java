package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Arrays;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class TypedResourcesTest {
	
	@Autowired
	private BooleanTestingSink sink;
	@Autowired
	private TypedResources metric;

	@Before
	public void setUp() throws Exception {
	}
	
	
	/* 
	 * #01
	 * 
	 * no violation / no prefixes set
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		return dataset;
	}
	
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset01();
		metric.clearCaches();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/* 
	 * #02
	 * 
	 * no violation / prefixes set
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset02();
		metric.clearCaches();
		metric.assessDataset(dataset);
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #03
	 * 
	 * <http://ex.org/Class01> not typed, but not considered local since no
	 * dataset prefixes are set
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset03());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #04
	 * 
	 * <http://ex.org/Class01> not typed / prefixes set
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset04());
		
		assertTrue(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #05
	 * 
	 * <http://ex.org/res/01> not typed, but no considered local, since no
	 * prefixes set
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset05());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #06
	 * 
	 * <http://ex.org/res/01> not typed / prefixes set
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset06());
		
		assertTrue(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #07
	 * 
	 * typed via rdfs:subClassOf / no prefixes set
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset07());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #08
	 * 
	 * typed via rdfs:subClassOf / prefixes set
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset08());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #09
	 * 
	 * typed via rdfs:subPropertyOf / no prefixes set
	 */
	public SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/prop02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/prop01> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset09());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #10
	 * 
	 * typed via rdfs:subPropertyOf / prefixes set
	 */
	public SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/prop02> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://ex.org/prop01> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset10());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #11
	 * 
	 * typed via owl:equivalentClass / no prefixes set
	 */
	public SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2002/07/owl#equivalentClass> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset11());
		
		assertFalse(sink.measureWritten(metricName));
	}

	
	/*
	 * #12
	 * 
	 * typed via owl:equivalentClass / prefixes set
	 */
	public SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/2002/07/owl#equivalentClass> <http://ex.org/Class01> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test12() throws NotImplementedException, SQLException {
		String metricName = "test12";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset12());
		
		assertFalse(sink.measureWritten(metricName));
	}


	

	/*
	 * #13
	 * 
	 * typed via owl:equivalentProperty / no prefixes set
	 */
	public SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/prop02> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://ex.org/prop01> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test13() throws NotImplementedException, SQLException {
		String metricName = "test13";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset13());
		
		assertFalse(sink.measureWritten(metricName));
	}
	
	
	/*
	 * #14
	 * 
	 * typed via owl:equivalentProperty / prefixes set
	 */
	public SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/prop02> <http://www.w3.org/2002/07/owl#equivalentProperty> <http://ex.org/prop01> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> .";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test14() throws NotImplementedException, SQLException {
		String metricName = "test14";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		metric.clearCaches();
		metric.assessDataset(dataset14());
		
		assertFalse(sink.measureWritten(metricName));
	}
}
