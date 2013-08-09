package org.aksw.sparqlify.qa.metrics;

import com.hp.hpl.jena.graph.Triple;

public interface NodeMetric extends Metric {
	
	public void assessNodes(Triple triple);
	
}
