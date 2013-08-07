package org.aksw.sparqlify.qa.metrics;

import com.hp.hpl.jena.rdf.model.Statement;

public interface TripleMetric extends Metric {
	
	public void assessTriple(Statement triple);

}
