package org.aksw.sparqlify.qa.metrics.completeness;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;

public class InterlinkingCompletenessTest {

	private ValueTestingSink sink;
	private final String prefix = "http://ex.org/";
	
	// 0 interlinks; 7 subj + 1 obj = 8 instances
	private final String datasetContent01 =
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://ex.org/sthElse/01> ." +
"<http://ex.org/sth/04> <http://ex.org/properties/foo> <http://ex.org/sthElse/03> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 0 interlinks; 7 subj + 1 obj = 8 instances
	private final String datasetContent02 =
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://ex.org/sthElse/01> ." +
"<http://ex.org/sth/04> <http://ex.org/properties/foo> <http://ex.org/sthElse/03> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 0 interlinks; 7 subj + 1 obj = 8 instances
	private final String datasetContent03 =
"<http://other.org/classes/Sth> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://ex.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://ex.org/sthElse/01> ." +
"<http://ex.org/sth/04> <http://ex.org/properties/foo> <http://ex.org/sthElse/03> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 0 interlinks; 5 subj + 3 obj = instances
	private final String datasetContent04 =
"<http://other.org/classes/Sth> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://other.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://other.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://other.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://ex.org/sthElse/01> ." +
"<http://ex.org/sth/04> <http://ex.org/properties/foo> <http://ex.org/sthElse/03> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 2 interlinks; 5 subj + 1 obj = 6 instances
	private final String datasetContent05 =
"<http://other.org/classes/Sth> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://other.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://other.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://other.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://other.org/sthElse/01> ." +
"<http://ex.org/sth/04> <http://ex.org/properties/foo> <http://other.org/sthElse/03> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 2 interlinks; 5 subj + 1 obj = 6 instances
	private final String datasetContent06 =
"<http://other.org/classes/Sth> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://other.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://other.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://other.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://other.org/sthElse/01> ." +
"<http://other.org/sthElse/03> <http://ex.org/properties/foo> <http://ex.org/sth/04> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 4 interlinks; 5 subj + 3 obj = 8 instances
	private final String datasetContent07 =
"<http://other.org/classes/Sth2> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://other.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://other.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://other.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://other.org/sthElse/01> ." +
"<http://other.org/sthElse/03> <http://ex.org/properties/foo> <http://ex.org/sth/04> ." +
"<http://other.org/sthElse/09> <http://ex.org/properties/foo> <http://ex.org/sth/07> ." +
"<http://other.org/sthElse/10> <http://ex.org/properties/foo> <http://ex.org/sth/08> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> .";

	// 3 interlinks; 5 subj + 1 obj = 7 instances
	private final String datasetContent08 =
"<http://ex.org/classes/Sth2>  <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://other.org/classes/Sth2> ." +
"<http://ex.org/sth/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/Sth> ." +
"<http://ex.org/sth/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 01\" ." +
"<http://ex.org/sth/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 02\" ." +
"<http://ex.org/sth/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 03\" ." +
"<http://ex.org/sth/04> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://other.org/classes/Sth> ." +
"<http://ex.org/sth/04> <http://www.w3.org/2000/01/rdf-schema#label> \"Something 04\" ." +
"<http://other.org/sthElse/01> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/01> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 01\" ." +
"<http://other.org/sthElse/02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/02> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 02\" ." +
"<http://other.org/sthElse/03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ex.org/classes/SthElse> ." +
"<http://other.org/sthElse/03> <http://www.w3.org/2000/01/rdf-schema#label> \"Something Else 03\" ." +
"<http://ex.org/sth/02> <http://ex.org/properties/foo> <http://other.org/sthElse/01> ." +
"<http://other.org/sthElse/01> <http://ex.org/properties/foo> <http://ex.org/sth/02> ." +
"<http://other.org/sthElse/03> <http://ex.org/properties/foo> <http://ex.org/sth/04> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/23> ." +
"<http://ex.org/sthElse/02> <http://ex.org/properties/bar> <http://ex.org/sthCompletelyDifferent/42> .";


	@Before
	public void setUp() throws Exception {
		sink = new ValueTestingSink();
	}


	@Test
	public void test01() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test01";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent01);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 0/(float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test02() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test02";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);

		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent02);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 0/(float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test03() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test03";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent03);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 0/(float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test04() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test04";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent04);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 0/(float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test05() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test05";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent05);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 2/(float) 6;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test06() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test06";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent06);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 2/(float) 6;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test07() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test07";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent07);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 4/(float) 8;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}


	@Test
	public void test08() throws NotImplementedException {
		InterlinkingCompleteness metric = new InterlinkingCompleteness();
		String metricName = "test08";
		metric.setName(metricName);
		metric.setParentDimension("parent");
		metric.registerMeasureDataSink(sink);
		
		SparqlifyDataset dataset = new SparqlifyDataset();
		StringReader reader = new StringReader(datasetContent08);
		dataset.read(reader, null, "TTL");
		dataset.setPrefix(prefix);
		
		metric.assessDataset(dataset);
		float expected = (float) 3/(float) 7;
		assertEquals(expected, sink.writtenValue(metricName), 0);
	}
}
