package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;

import com.hp.hpl.jena.graph.Triple;

public interface TripleMetric extends Metric {
	
	public void assessTriple(Triple triple) throws NotImplementedException;

}
