package org.aksw.sparqlify.qa.sinks;

import java.util.List;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

public class MappingMeasureDatum extends MeasureDatum {
	
	protected List<ViewDefinition> viewDefs;


	public MappingMeasureDatum(String dimension, String metric, float value, 
			List<ViewDefinition> viewDefs) {
		
		super();
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.viewDefs = viewDefs;
		
	}


	public List<ViewDefinition> getViewDefs() {
		return viewDefs;
	}
}
