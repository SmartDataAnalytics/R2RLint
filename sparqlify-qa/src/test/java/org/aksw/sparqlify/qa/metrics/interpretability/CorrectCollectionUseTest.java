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
public class CorrectCollectionUseTest {
	
	private float listNodeHasNoRdfFirst = 0;
	private float listNodeHasMultipleRdfFirstStmnts = (float) 0.1;
	private float rdfRestIsLiteral= (float) 0.2;
	private float multiplePredecessors = (float) 0.3;
	private float multipleSuccessors = (float) 0.4;
	private float listEndedWithoutRdfNil = (float) 0.5;
	private float nilHasSuccessor = (float) 0.6;
	private float noViolation = -1;
	
	@Autowired
	private ValueTestingSink sink;
	@Autowired
	private Pinpointer pinpointer;
	@Autowired
	private CorrectCollectionUse metric;

	@Before
	public void setUp() throws Exception {
		// set up dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer.registerViewDefs(viewDefs);
		
		// init values that are written in different error cases
		metric.setListNodeHasNoRdfFirstVal(listNodeHasNoRdfFirst);
		metric.setListNodeHasMultipleRdfFirstStmntsVal(listNodeHasMultipleRdfFirstStmnts);
		metric.setRdfRestIsLiteralVal(rdfRestIsLiteral);
		metric.setMultiplePredecessorsVal(multiplePredecessors);
		metric.setMultipleSuccessorsVal(multipleSuccessors);
		metric.setListEndedWithoutRdfNilVal(listEndedWithoutRdfNil);
		metric.setNilHasSuccessorVal(nilHasSuccessor);
	}


	/*
	 * no collection used --> no violations
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> ." +
			"_:23 <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
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
	 * list with no violations
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
	 * list with no violations (first statement == "sth rdf:rest rdf:nil"; Jena
	 * model reader reads in reverse order)
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(noViolation, sink.writtenValue(metricName), 0);
	}


	/*
	 * list with multiple predecessors (headwards)
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m71> . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public void test04() throws NotImplementedException, SQLException {
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertEquals(multiplePredecessors, sink.writtenValue(metricName), 0);
	}


	/*
	 * list with multiple predecessors (tailwards)
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m71> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		
		assertEquals(multiplePredecessors, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * list with multiple successors (headwards)
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m71> . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l71 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(multipleSuccessors, sink.writtenValue(metricName), 0);
	}


	/*
	 * list with multiple successors (tailwards)
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . "+
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l71 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l71 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m71> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		
		assertEquals(multipleSuccessors, sink.writtenValue(metricName), 0);
	}


	/*
	 * list node with no rdf:first node (headwards)
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(listNodeHasNoRdfFirst, sink.writtenValue(metricName), 0);
	}


	/*
	 * list node with no rdf:first node (tailwards)
	 */
	public SparqlifyDataset dataset09() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		
		assertEquals(listNodeHasNoRdfFirst, sink.writtenValue(metricName), 0);
	}


	/*
	 * list node with multiple rdf:first nodes (headwards)
	 */
	public SparqlifyDataset dataset10() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m72> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(listNodeHasMultipleRdfFirstStmnts, sink.writtenValue(metricName), 0);
	}


	/*
	 * list node with multiple rdf:first nodes (tailwards)
	 */
	public SparqlifyDataset dataset11() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . "+
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m72> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		
		assertEquals(listNodeHasMultipleRdfFirstStmnts, sink.writtenValue(metricName), 0);
	}


	/*
	 * list has literal in rdf:rest (headwards)
	 */
	public SparqlifyDataset dataset12() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> \"l04\" . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(rdfRestIsLiteral, sink.writtenValue(metricName), 0);
	}


	/*
	 * list has literal in rdf:rest (tailwards)
	 */
	public SparqlifyDataset dataset13() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> \"l04\" . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		
		assertEquals(rdfRestIsLiteral, sink.writtenValue(metricName), 0);
	}

	/*
	 * list ended without rdf:nil (headwards)
	 */
	public SparqlifyDataset dataset14() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
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
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset14();
		metric.assessDataset(dataset);
		
		assertEquals(listEndedWithoutRdfNil, sink.writtenValue(metricName), 0);
	}


	/*
	 * list ended without rdf:nil (tailwards)
	 */
	public SparqlifyDataset dataset15() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l03 . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l03 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . ";
		
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
		
		assertEquals(listEndedWithoutRdfNil, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * rdf:nil node has successor (headwards)
	 */
	public SparqlifyDataset dataset16() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . ";
		
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
		
		assertEquals(nilHasSuccessor, sink.writtenValue(metricName), 0);
	}
	
	
	/*
	 * rdf:nil node has successor (tailwards)
	 */
	public SparqlifyDataset dataset17() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/l01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l05 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m05> . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l05 . " +
			"_:l04 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m04> . " +
			"<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l04 . " +
			"<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m03> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . " +
			"_:l02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m02> . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> _:l02 . " +
			"_:l01 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://ex.org/res/m01> . " +
			"<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		
		return dataset;
	}
	
	@Test
	public synchronized void test17() throws NotImplementedException, SQLException {
		String metricName = "test17";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.initMeasureDataSink();
		metric.clearCaches();
		
		SparqlifyDataset dataset = dataset17();
		metric.assessDataset(dataset);
		
		assertEquals(nilHasSuccessor, sink.writtenValue(metricName), 0);
	}
}
