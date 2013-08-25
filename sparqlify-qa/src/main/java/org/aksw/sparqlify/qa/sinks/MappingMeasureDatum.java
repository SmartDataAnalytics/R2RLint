package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Triple;

public class MappingMeasureDatum extends MeasureDatum {
	
	protected Set<ViewQuad<ViewDefinition>> viewQuads;


	public MappingMeasureDatum(String dimension, String metric, float value, 
			Set<ViewQuad<ViewDefinition>> viewQuads) {
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.viewQuads = viewQuads;
		
	}


	public Set<ViewQuad<ViewDefinition>> getViewQuads() {
		return viewQuads;
	}
}
