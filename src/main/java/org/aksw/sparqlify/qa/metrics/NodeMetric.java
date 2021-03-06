package org.aksw.sparqlify.qa.metrics;

import java.sql.SQLException;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;

import com.hp.hpl.jena.graph.Triple;

public interface NodeMetric extends Metric {
	
	public void assessNodes(Triple triple) throws NotImplementedException, SQLException;
	
}
