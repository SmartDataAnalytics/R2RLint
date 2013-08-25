package org.aksw.sparqlify.qa.metrics;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;


public interface MappingMetric extends Metric {
	
	public void assessMappings(Collection<ViewDefinition> viewDefs) throws NotImplementedException;

}
