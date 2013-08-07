package org.aksw.sparqlify.qa.metrics;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;


public interface MappingMetric extends Metric {
	
	public void assessMappings(Collection<ViewDefinition> viewDefs);

}
