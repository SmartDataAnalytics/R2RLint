package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;

public interface DatasetMetric extends Metric {
	 
	public void assessDataset(SparqlifyDataset dataset) throws NotImplementedException;

}
