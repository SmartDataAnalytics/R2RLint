package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.dataset.SparqlifyDump;

public interface DatasetMetric extends Metric {
	
	public void assessDataset(SparqlifyDump dataset);

}
