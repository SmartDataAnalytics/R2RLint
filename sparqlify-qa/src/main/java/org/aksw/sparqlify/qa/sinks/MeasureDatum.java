package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

public abstract class MeasureDatum {
	
	protected float value;
	protected String dimension;
	protected String metric;
	protected Set<ViewQuad<ViewDefinition>> viewQuads;


	public float getValue() {
		return value;
	}


	public String getDimension() {
		return dimension;
	}


	public String getMetric() {
		return metric;
	}


	public Set<ViewQuad<ViewDefinition>> getViewQuads() {
		return viewQuads;
	}
}
