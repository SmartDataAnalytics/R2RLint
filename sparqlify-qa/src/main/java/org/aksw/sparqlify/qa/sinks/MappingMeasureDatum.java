package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

public class MappingMeasureDatum extends MeasureDatum {
	
	protected Set<ViewQuad<ViewDefinition>> candidates;


	public MappingMeasureDatum(String dimension, String metric, float value, 
			Set<ViewQuad<ViewDefinition>> candidates) {
		
		super();
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.candidates = candidates;
		
	}


	public Set<ViewQuad<ViewDefinition>> getViewDefs() {
		return candidates;
	}
}
