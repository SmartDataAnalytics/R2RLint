package org.aksw.sparqlify.qa.metrics;

import com.hp.hpl.jena.rdf.model.Statement;

public interface NodeMetric extends Metric {
	
	public void assessNodes(Statement triple);
	
}
