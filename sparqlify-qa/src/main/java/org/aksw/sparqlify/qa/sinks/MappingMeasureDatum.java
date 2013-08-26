package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Triple;

public class MappingMeasureDatum extends MeasureDatum {
	
	protected ViewDefinition viewDef;


	public MappingMeasureDatum(String dimension, String metric, float value, 
			ViewDefinition viewDef) {
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.viewDef = viewDef;
		
	}


	public ViewDefinition getViewDef() {
		return viewDef;
	}
}
