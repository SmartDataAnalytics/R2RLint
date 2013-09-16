package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;

public class ResourceInterpretabilityTest {
	
	BooleanTestingSink sink;
	Pinpointer pinpointer;

	@Before
	public void setUp() throws Exception {
		// dummy pinpointer
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
		sink = new BooleanTestingSink();
	}


	/*
	 * no violations: all resources have at least a type assigned via rdf:type
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org");
		
		return dataset;
	}
	
	@Test
	public void test01() throws NotImplementedException {
		ResourceInterpretability metric = new ResourceInterpretability();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset01();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * missing ontological description on subject position (ex:res/01)
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
//			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org");
		
		return dataset;
	}
	
	@Test
	public void test02() throws NotImplementedException {
		ResourceInterpretability metric = new ResourceInterpretability();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset02();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * missing ontological description on predicate position (ex:prop01)
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
//			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org");
		
		return dataset;
	}
	
	@Test
	public void test03() throws NotImplementedException {
		ResourceInterpretability metric = new ResourceInterpretability();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset03();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * missing ontological description on object position (ex:res/02)
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/Class01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> ." +
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> ." +
			"_:23 <http://ex.org/prop01> <http://ex.org/res/02> . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org");
		
		return dataset;
	}
	
	@Test
	public void test04() throws NotImplementedException {
		ResourceInterpretability metric = new ResourceInterpretability();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset04();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
