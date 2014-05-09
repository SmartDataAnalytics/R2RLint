package org.aksw.sparqlify.qa.metrics.interoperability;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Arrays;

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
public class TermReUseTest {

	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private TermReUse metric;

	@Before
	public void setUp() throws Exception {
	}

	// FIXME: uses real dereferenceability test; this should be mocked away
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/dataset> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/ns/void#Dataset> ." +
			"<http://ex.org/dataset> <http://purl.org/dc/elements/1.1/creator> \"Anthony Author\" . " +
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/Class02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.registerDump(new StringReader(content));
		dataset.read(new StringReader(content), null, "TTL");
		dataset.setPrefixes(Arrays.asList("http://ex.org/"));
		
		return dataset;
	}
	
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		float expected = 5 / (float) 11;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}

}
