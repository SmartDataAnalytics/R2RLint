package org.aksw.sparqlify.qa.metrics.reprconsistency;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class VocabularyReUseTest {
	
	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private VocabularyReUse metric;

	@Before
	public void setUp() throws Exception {
	}


	/*
	 * explicitly defined classes
	 * 
	 * 		(established:not established)
	 * subj pos: 0:3 (--> ex:Class01, ex:Class02, ex:prop01)
	 * pred pos: 2:0 (--> rdf:type, dc:creator)
	 * obj  pos: 3:0 (--> void:Dataset, rdfs:Class, rdf:Property)
	 * ---------------
	 *           5:3   --> ratio: 5 / (5 + 3) = 5/8
	 * 
	 */
	/*
	 * Hint: ex232.org is used here since example.com and example.org are
	 * already under the top 100 prefixes retrieved from prefix.cc. So if
	 * someday ex.org will be also that famous all the tests would break.
	 * That's why I used ex232.org instead of ex.org
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex232.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex232.org/dataset> <http://purl.org/dc/elements/1.1/creator> \"Anthony Author\" . " +
			"<http://ex232.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex232.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex232.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex232.org/Class01> ." +
			"<http://ex232.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex232.org/Class02> ." +
			"<http://ex232.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex232.org/res/01> <http://ex232.org/prop01> \"23\" ." +
			"_:23 <http://ex232.org/prop01> \"42\" . ";
		
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
		
		float expected = 5 / (float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	/*
	 * not explicitly defined classes
	 * 
	 * 		(established:not established)
	 * subj pos: 0:1 (--> ex:prop01)
	 * pred pos: 2:0 (--> rdf:type, dc:creator)
	 * obj  pos: 2:2 (--> void:Dataset, ex:Class01, ex:Class02, rdf:Property)
	 * ---------------
	 *           4:3   --> ratio: 4 / (4 + 3) = 4/7
	 * 
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex232.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex232.org/dataset> <http://purl.org/dc/elements/1.1/creator> \"Anthony Author\" . " +
			"<http://ex232.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex232.org/Class01> ." +
			"<http://ex232.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex232.org/Class02> ." +
			"<http://ex232.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex232.org/res/01> <http://ex232.org/prop01> \"23\" ." +
			"_:23 <http://ex232.org/prop01> \"42\" . ";
		
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
		
		float expected = 4 / (float) 7;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
