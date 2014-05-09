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
public class VocabularyClassCompletenessTest {
	
	@Autowired
	private ValueTestingSink sink;
	
	@Autowired
	private VocabularyClassCompleteness metric;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	private String datasetContent01 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 13\" . ";
	
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
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 0 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}

	
	private String datasetContent02 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test02() throws NotImplementedException, SQLException {
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent02);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 1 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent03 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test03() throws NotImplementedException, SQLException {
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent03);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 3 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent04 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Project> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Project 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"PersonalProfileDocument 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent04);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 5 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent05 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Document> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Document 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Person 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Project> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Project 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"PersonalProfileDocument 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test05() throws NotImplementedException, SQLException {
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent05);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 7 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent06 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/LabelProperty> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"LabelProperty 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Organization 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Document> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Document 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Person 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Project> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Project 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"PersonalProfileDocument 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test06() throws NotImplementedException, SQLException {
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent06);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 9 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent07 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineAccount> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineAccount 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineChatAccount> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"OnLineChatAccount 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/LabelProperty> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"LabelProperty 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Organization 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Document> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Document 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Person 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Project> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Project 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"PersonalProfileDocument 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test07() throws NotImplementedException, SQLException {
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent07);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 11 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
	
	
	private String datasetContent08 = 
	"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Group> . "
		+ "<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Group 01\" . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Image> . "
		+ "<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Image 02\" . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineAccount> . "
		+ "<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineAccount 03\" . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineChatAccount> . "
		+ "<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"OnLineChatAccount 04\" . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/LabelProperty> . "
		+ "<http://ex.org/sth/05> <http://www.w3.org/2000/01/rdf-schema#label> \"LabelProperty 05\" . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Organization> . "
		+ "<http://ex.org/sth/06> <http://www.w3.org/2000/01/rdf-schema#label> \"Organization 06\" . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Document> . "
		+ "<http://ex.org/sth/07> <http://www.w3.org/2000/01/rdf-schema#label> \"Document 07\" . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
		+ "<http://ex.org/sth/08> <http://www.w3.org/2000/01/rdf-schema#label> \"Person 08\" . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Project> . "
		+ "<http://ex.org/sth/09> <http://www.w3.org/2000/01/rdf-schema#label> \"Project 09\" . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . "
		+ "<http://ex.org/sth/10> <http://www.w3.org/2000/01/rdf-schema#label> \"PersonalProfileDocument 10\" . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineEcommerceAccount> . "
		+ "<http://ex.org/sth/11> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineEcommerceAccount 11\" . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/OnlineGamingAccount> . "
		+ "<http://ex.org/sth/12> <http://www.w3.org/2000/01/rdf-schema#label> \"OnlineGamingAccount 12\" . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Agent> . "
		+ "<http://ex.org/sth/13> <http://www.w3.org/2000/01/rdf-schema#label> \"Agent 13\" . ";
	
	@Test
	public void test08() throws NotImplementedException, SQLException {
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent08);
		dataset.read(reader, null, "TTL");
		dataset.setUsedPrefixes(new ArrayList<String>(Arrays.asList("foaf")));
		
		metric.assessDataset(dataset);
		/* foaf has 13 classes:
		 * - http://xmlns.com/foaf/0.1/Agent
		 * - http://xmlns.com/foaf/0.1/OnlineGamingAccount
		 * - http://xmlns.com/foaf/0.1/OnlineEcommerceAccount
		 * - http://xmlns.com/foaf/0.1/PersonalProfileDocument
		 * - http://xmlns.com/foaf/0.1/Project
		 * - http://xmlns.com/foaf/0.1/Person
		 * - http://xmlns.com/foaf/0.1/Document
		 * - http://xmlns.com/foaf/0.1/Organization
		 * - http://xmlns.com/foaf/0.1/LabelProperty
		 * - http://xmlns.com/foaf/0.1/OnlineChatAccount
		 * - http://xmlns.com/foaf/0.1/OnlineAccount
		 * - http://xmlns.com/foaf/0.1/Image
		 * - http://xmlns.com/foaf/0.1/Group
		 */
		float expected = (float) 13 / (float) 13;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
