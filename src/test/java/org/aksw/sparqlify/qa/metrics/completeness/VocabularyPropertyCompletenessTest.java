package org.aksw.sparqlify.qa.metrics.completeness;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class VocabularyPropertyCompletenessTest {
	
	
	@Autowired
	private ValueTestingSink sink;
	
	@Autowired
	private VocabularyPropertyCompleteness metric;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}



	private String datasetContent01 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/14> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/16> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/17> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/19> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test01() throws NotImplementedException, SQLException {
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent01);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		
		float expected = (float) 0 / (float) 64;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent02 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/14> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/16> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/17> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/19> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent02);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 1 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	private String datasetContent03 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/14> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/16> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/17> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/19> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent03);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 5 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent04 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/14> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/16> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/17> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/19> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent04);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 10 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent05 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/17> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/19> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent05);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 15 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent06 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/22> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/24> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/25> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent06);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 20 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent07 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://xmlns.com/foaf/0.1/surname> \"Test\" . " +
	"<http://ex.org/sth/22> <http://xmlns.com/foaf/0.1/family_name> \"Test\" . " +
	"<http://ex.org/sth/23> <http://xmlns.com/foaf/0.1/familyName> \"Test\" . " +
	"<http://ex.org/sth/24> <http://xmlns.com/foaf/0.1/phone> <tel:456-7890> . " +
	"<http://ex.org/sth/25> <http://xmlns.com/foaf/0.1/homepage> <http://ex.org/> . " +
	"<http://ex.org/sth/26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/27> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/29> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/30> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent07);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 25 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent08 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://xmlns.com/foaf/0.1/surname> \"Test\" . " +
	"<http://ex.org/sth/22> <http://xmlns.com/foaf/0.1/family_name> \"Test\" . " +
	"<http://ex.org/sth/23> <http://xmlns.com/foaf/0.1/familyName> \"Test\" . " +
	"<http://ex.org/sth/24> <http://xmlns.com/foaf/0.1/phone> <tel:456-7890> . " +
	"<http://ex.org/sth/25> <http://xmlns.com/foaf/0.1/homepage> <http://ex.org/> . " +
	"<http://ex.org/sth/26> <http://xmlns.com/foaf/0.1/weblog> <http://ex.org/blog> . " +
	"<http://ex.org/sth/27> <http://xmlns.com/foaf/0.1/openid> <http://ex.org/me> . " +
	"<http://ex.org/sth/28> <http://xmlns.com/foaf/0.1/tipjar> <http://ex.org/tipjar> . " +
	"<http://ex.org/sth/29> <http://xmlns.com/foaf/0.1/plan> \"whatever\" . " +
	"<http://ex.org/sth/30> <http://xmlns.com/foaf/0.1/made> <http://ex.org/mess> . " +
	"<http://ex.org/sth/31> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/32> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/34> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/35> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/36> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/37> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/39> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent08);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 30 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent09 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://xmlns.com/foaf/0.1/surname> \"Test\" . " +
	"<http://ex.org/sth/22> <http://xmlns.com/foaf/0.1/family_name> \"Test\" . " +
	"<http://ex.org/sth/23> <http://xmlns.com/foaf/0.1/familyName> \"Test\" . " +
	"<http://ex.org/sth/24> <http://xmlns.com/foaf/0.1/phone> <tel:456-7890> . " +
	"<http://ex.org/sth/25> <http://xmlns.com/foaf/0.1/homepage> <http://ex.org/> . " +
	"<http://ex.org/sth/26> <http://xmlns.com/foaf/0.1/weblog> <http://ex.org/blog> . " +
	"<http://ex.org/sth/27> <http://xmlns.com/foaf/0.1/openid> <http://ex.org/me> . " +
	"<http://ex.org/sth/28> <http://xmlns.com/foaf/0.1/tipjar> <http://ex.org/tipjar> . " +
	"<http://ex.org/sth/29> <http://xmlns.com/foaf/0.1/plan> \"whatever\" . " +
	"<http://ex.org/sth/30> <http://xmlns.com/foaf/0.1/made> <http://ex.org/mess> . " +
	"<http://ex.org/sth/31> <http://xmlns.com/foaf/0.1/maker> <http://other.org/agents/01> . " +
	"<http://ex.org/sth/32> <http://xmlns.com/foaf/0.1/img> <http://other.org/imgs/01> . " +
	"<http://ex.org/sth/33> <http://xmlns.com/foaf/0.1/depiction> <http://other.org/imgs/02> . " +
	"<http://ex.org/sth/34> <http://xmlns.com/foaf/0.1/depicts> <http://other.org/things/01> . " +
	"<http://ex.org/sth/35> <http://xmlns.com/foaf/0.1/thumbnail> <http://other.org/imgs/03> . " +
	"<http://ex.org/sth/36> <http://xmlns.com/foaf/0.1/myersBriggs> \"Myers brigg\" . " +
	"<http://ex.org/sth/37> <http://xmlns.com/foaf/0.1/workplaceHomepage> <http://ex.com/> . " +
	"<http://ex.org/sth/38> <http://xmlns.com/foaf/0.1/workInfoHomepage> <http://ex.com/info> . " +
	"<http://ex.org/sth/39> <http://xmlns.com/foaf/0.1/schoolHomepage> <http://ex.edu> . " +
	"<http://ex.org/sth/40> <http://xmlns.com/foaf/0.1/knows> <http://other.org/ppl/01> . " +
	"<http://ex.org/sth/41> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/43> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/45> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/47> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/50> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test09() throws NotImplementedException, SQLException {
		String metricName = "test09";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent09);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 40 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent10 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://xmlns.com/foaf/0.1/surname> \"Test\" . " +
	"<http://ex.org/sth/22> <http://xmlns.com/foaf/0.1/family_name> \"Test\" . " +
	"<http://ex.org/sth/23> <http://xmlns.com/foaf/0.1/familyName> \"Test\" . " +
	"<http://ex.org/sth/24> <http://xmlns.com/foaf/0.1/phone> <tel:456-7890> . " +
	"<http://ex.org/sth/25> <http://xmlns.com/foaf/0.1/homepage> <http://ex.org/> . " +
	"<http://ex.org/sth/26> <http://xmlns.com/foaf/0.1/weblog> <http://ex.org/blog> . " +
	"<http://ex.org/sth/27> <http://xmlns.com/foaf/0.1/openid> <http://ex.org/me> . " +
	"<http://ex.org/sth/28> <http://xmlns.com/foaf/0.1/tipjar> <http://ex.org/tipjar> . " +
	"<http://ex.org/sth/29> <http://xmlns.com/foaf/0.1/plan> \"whatever\" . " +
	"<http://ex.org/sth/30> <http://xmlns.com/foaf/0.1/made> <http://ex.org/mess> . " +
	"<http://ex.org/sth/31> <http://xmlns.com/foaf/0.1/maker> <http://other.org/agents/01> . " +
	"<http://ex.org/sth/32> <http://xmlns.com/foaf/0.1/img> <http://other.org/imgs/01> . " +
	"<http://ex.org/sth/33> <http://xmlns.com/foaf/0.1/depiction> <http://other.org/imgs/02> . " +
	"<http://ex.org/sth/34> <http://xmlns.com/foaf/0.1/depicts> <http://other.org/things/01> . " +
	"<http://ex.org/sth/35> <http://xmlns.com/foaf/0.1/thumbnail> <http://other.org/imgs/03> . " +
	"<http://ex.org/sth/36> <http://xmlns.com/foaf/0.1/myersBriggs> \"Myers brigg\" . " +
	"<http://ex.org/sth/37> <http://xmlns.com/foaf/0.1/workplaceHomepage> <http://ex.com/> . " +
	"<http://ex.org/sth/38> <http://xmlns.com/foaf/0.1/workInfoHomepage> <http://ex.com/info> . " +
	"<http://ex.org/sth/39> <http://xmlns.com/foaf/0.1/schoolHomepage> <http://ex.edu> . " +
	"<http://ex.org/sth/40> <http://xmlns.com/foaf/0.1/knows> <http://other.org/ppl/01> . " +
	"<http://ex.org/sth/41> <http://xmlns.com/foaf/0.1/interest> <http://other.org/things/02> . " +
	"<http://ex.org/sth/42> <http://xmlns.com/foaf/0.1/topic_interest> <http://other.org/things/03> . " +
	"<http://ex.org/sth/43> <http://xmlns.com/foaf/0.1/publications> <http://other.org/docs/01> . " +
	"<http://ex.org/sth/44> <http://xmlns.com/foaf/0.1/currentProject> <http://other.org/prjs/01> . " +
	"<http://ex.org/sth/45> <http://xmlns.com/foaf/0.1/pastProject> <http://other.org/prjs/02> . " +
	"<http://ex.org/sth/46> <http://xmlns.com/foaf/0.1/fundedBy> <http://ex.com/> . " +
	"<http://ex.org/sth/47> <http://xmlns.com/foaf/0.1/logo> <http://other.org/imgs/04> . " +
	"<http://ex.org/sth/48> <http://xmlns.com/foaf/0.1/topic> <http://other.org/things/03> . " +
	"<http://ex.org/sth/49> <http://xmlns.com/foaf/0.1/primaryTopic> <http://other.org/things/04> . " +
	"<http://ex.org/sth/50> <http://xmlns.com/foaf/0.1/focus> <http://other.org/things/05> . " +
	"<http://ex.org/sth/51> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/56> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/59> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/62> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . ";
	
	@Test
	public synchronized void test10() throws NotImplementedException, SQLException {
		String metricName = "test10";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent10);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);

		float expected = (float) 50 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent11 = 
	"<http://ex.org/sth/01> <http://xmlns.com/foaf/0.1/mbox> <foo@bar> . " +
	"<http://ex.org/sth/02> <http://xmlns.com/foaf/0.1/mbox_sha1sum> \"01234567\" . " +
	"<http://ex.org/sth/03> <http://xmlns.com/foaf/0.1/gender> \"female\" . " +
	"<http://ex.org/sth/04> <http://xmlns.com/foaf/0.1/geekcode> \"1207 6331<\" . " +
	"<http://ex.org/sth/05> <http://xmlns.com/foaf/0.1/dnaChecksum> \"9876543210\" . " +
	"<http://ex.org/sth/06> <http://xmlns.com/foaf/0.1/sha1> \"9876543210\" . " +
	"<http://ex.org/sth/07> <http://xmlns.com/foaf/0.1/based_near> <http://ex.org/sthSpatial/01> . " +
	"<http://ex.org/sth/08> <http://xmlns.com/foaf/0.1/title> \"Mr\" . " +
	"<http://ex.org/sth/09> <http://xmlns.com/foaf/0.1/nick> \"mlml\" . " +
	"<http://ex.org/sth/10> <http://xmlns.com/foaf/0.1/jabberID> \"mlml@jabber.org\" . " +
	"<http://ex.org/sth/11> <http://xmlns.com/foaf/0.1/aimChatID> \"798798762351\" . " +
	"<http://ex.org/sth/12> <http://xmlns.com/foaf/0.1/skypeID> \"mlml\" . " +
	"<http://ex.org/sth/13> <http://xmlns.com/foaf/0.1/icqChatID> \"798798762352\" . " +
	"<http://ex.org/sth/14> <http://xmlns.com/foaf/0.1/yahooChatID> \"798798762353\" . " +
	"<http://ex.org/sth/15> <http://xmlns.com/foaf/0.1/msnChatID> \"mlml\" . " +
	"<http://ex.org/sth/16> <http://xmlns.com/foaf/0.1/name> \"Test Test\" . " +
	"<http://ex.org/sth/17> <http://xmlns.com/foaf/0.1/firstName> \"Test\" . " +
	"<http://ex.org/sth/18> <http://xmlns.com/foaf/0.1/lastName> \"Test\" . " +
	"<http://ex.org/sth/19> <http://xmlns.com/foaf/0.1/givenName> \"Test\" . " +
	"<http://ex.org/sth/20> <http://xmlns.com/foaf/0.1/givenname> \"Test\" . " +
	"<http://ex.org/sth/21> <http://xmlns.com/foaf/0.1/surname> \"Test\" . " +
	"<http://ex.org/sth/22> <http://xmlns.com/foaf/0.1/family_name> \"Test\" . " +
	"<http://ex.org/sth/23> <http://xmlns.com/foaf/0.1/familyName> \"Test\" . " +
	"<http://ex.org/sth/24> <http://xmlns.com/foaf/0.1/phone> <tel:456-7890> . " +
	"<http://ex.org/sth/25> <http://xmlns.com/foaf/0.1/homepage> <http://ex.org/> . " +
	"<http://ex.org/sth/26> <http://xmlns.com/foaf/0.1/weblog> <http://ex.org/blog> . " +
	"<http://ex.org/sth/27> <http://xmlns.com/foaf/0.1/openid> <http://ex.org/me> . " +
	"<http://ex.org/sth/28> <http://xmlns.com/foaf/0.1/tipjar> <http://ex.org/tipjar> . " +
	"<http://ex.org/sth/29> <http://xmlns.com/foaf/0.1/plan> \"whatever\" . " +
	"<http://ex.org/sth/30> <http://xmlns.com/foaf/0.1/made> <http://ex.org/mess> . " +
	"<http://ex.org/sth/31> <http://xmlns.com/foaf/0.1/maker> <http://other.org/agents/01> . " +
	"<http://ex.org/sth/32> <http://xmlns.com/foaf/0.1/img> <http://other.org/imgs/01> . " +
	"<http://ex.org/sth/33> <http://xmlns.com/foaf/0.1/depiction> <http://other.org/imgs/02> . " +
	"<http://ex.org/sth/34> <http://xmlns.com/foaf/0.1/depicts> <http://other.org/things/01> . " +
	"<http://ex.org/sth/35> <http://xmlns.com/foaf/0.1/thumbnail> <http://other.org/imgs/03> . " +
	"<http://ex.org/sth/36> <http://xmlns.com/foaf/0.1/myersBriggs> \"Myers brigg\" . " +
	"<http://ex.org/sth/37> <http://xmlns.com/foaf/0.1/workplaceHomepage> <http://ex.com/> . " +
	"<http://ex.org/sth/38> <http://xmlns.com/foaf/0.1/workInfoHomepage> <http://ex.com/info> . " +
	"<http://ex.org/sth/39> <http://xmlns.com/foaf/0.1/schoolHomepage> <http://ex.edu> . " +
	"<http://ex.org/sth/40> <http://xmlns.com/foaf/0.1/knows> <http://other.org/ppl/01> . " +
	"<http://ex.org/sth/41> <http://xmlns.com/foaf/0.1/interest> <http://other.org/things/02> . " +
	"<http://ex.org/sth/42> <http://xmlns.com/foaf/0.1/topic_interest> <http://other.org/things/03> . " +
	"<http://ex.org/sth/43> <http://xmlns.com/foaf/0.1/publications> <http://other.org/docs/01> . " +
	"<http://ex.org/sth/44> <http://xmlns.com/foaf/0.1/currentProject> <http://other.org/prjs/01> . " +
	"<http://ex.org/sth/45> <http://xmlns.com/foaf/0.1/pastProject> <http://other.org/prjs/02> . " +
	"<http://ex.org/sth/46> <http://xmlns.com/foaf/0.1/fundedBy> <http://ex.com/> . " +
	"<http://ex.org/sth/47> <http://xmlns.com/foaf/0.1/logo> <http://other.org/imgs/04> . " +
	"<http://ex.org/sth/48> <http://xmlns.com/foaf/0.1/topic> <http://other.org/things/03> . " +
	"<http://ex.org/sth/49> <http://xmlns.com/foaf/0.1/primaryTopic> <http://other.org/things/04> . " +
	"<http://ex.org/sth/50> <http://xmlns.com/foaf/0.1/focus> <http://other.org/things/05> . " +
	"<http://ex.org/sth/51> <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> <http://other.org/docs/02> . " +
	"<http://ex.org/sth/52> <http://xmlns.com/foaf/0.1/page> <http://other.org/> . " +
	"<http://ex.org/sth/53> <http://xmlns.com/foaf/0.1/theme> <http://other.org/things/06> . " +
	"<http://ex.org/sth/54> <http://xmlns.com/foaf/0.1/account> <http://other.org/accounts/01> . " +
	"<http://ex.org/sth/55> <http://xmlns.com/foaf/0.1/holdsAccount> <http://other.org/ppl/02> . " +
	"<http://ex.org/sth/56> <http://xmlns.com/foaf/0.1/accountServiceHomepage> <http://other.org/accounts> . " +
	"<http://ex.org/sth/57> <http://xmlns.com/foaf/0.1/accountName> \"Acc\" . " +
	"<http://ex.org/sth/58> <http://xmlns.com/foaf/0.1/member> <http://other.org/ppl/03> . " +
	"<http://ex.org/sth/59> <http://xmlns.com/foaf/0.1/membershipClass> <http://other.org/classes/Sth> . " +
	"<http://ex.org/sth/60> <http://xmlns.com/foaf/0.1/birthday> \"2000-01-01\" . " +
	"<http://ex.org/sth/61> <http://xmlns.com/foaf/0.1/age> \"23\" . " +
	"<http://ex.org/sth/62> <http://xmlns.com/foaf/0.1/status> \"off\" . ";
	
	@Test
	public synchronized void test11() throws NotImplementedException, SQLException {
		String metricName = "test11";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent11);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		
		float expected = (float) 62 / (float) 62;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
