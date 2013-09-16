package org.aksw.sparqlify.qa.metrics.understandability;

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

public class LabeledResourcesTest {
	
	Pinpointer pinpointer;
	BooleanTestingSink sink;


	@Before
	public void setUp() throws Exception {
		Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		pinpointer = new Pinpointer(viewDefs);
		sink = new BooleanTestingSink();
	}


	/*
	 * no violations: everything's labeled
	 */
	public SparqlifyDataset dataset01() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#label> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test01() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
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
	 * not labeled subject
	 */
	public SparqlifyDataset dataset02() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 02\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#label> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test02() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
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
	 * not labeled predicate
	 */
	public SparqlifyDataset dataset03() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 02\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test03() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
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
	 * not labeled object
	 */
	public SparqlifyDataset dataset04() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#label> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#label> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/03> ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test04() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
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


	/*
	 * no violations: everything's commented
	 */
	public SparqlifyDataset dataset05() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test05() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset05();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * not commented subject
	 */
	public SparqlifyDataset dataset06() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 02\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test06() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset06();
		metric.assessDataset(dataset);
		
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * not commented predicate
	 */
	public SparqlifyDataset dataset07() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 02\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test07() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset07();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}


	/*
	 * not commented object
	 */
	public SparqlifyDataset dataset08() {
		String content = 
			"<http://ex.org/res/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class01> ." +
			"<http://ex.org/res/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/Class02> ." +
			"<http://ex.org/prop01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ." +
			"<http://ex.org/Class01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 01\" . " +
			"<http://ex.org/Class02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Class 02\" . " +
			"<http://ex.org/res/01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 01\" . " +
			"<http://ex.org/res/02> <http://www.w3.org/2000/01/rdf-schema#comment> \"Resource 02\" . " +
			"<http://ex.org/prop01> <http://www.w3.org/2000/01/rdf-schema#comment> \"Property 01\" . " +
			"<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/03> ." +
			"_:23 <http://ex.org/prop01> \"42\" . ";
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		Reader reader = new StringReader(content);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix("http://ex.org/");
		
		return dataset;
	}
	
	@Test
	public void test08() throws NotImplementedException {
		LabeledResources metric = new LabeledResources();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		metric.registerPinpointer(pinpointer);
		
		SparqlifyDataset dataset = dataset08();
		metric.assessDataset(dataset);
		
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
		assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
		assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
	}
}
