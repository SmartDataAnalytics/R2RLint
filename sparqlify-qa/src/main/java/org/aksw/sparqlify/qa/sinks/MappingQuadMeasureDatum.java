package org.aksw.sparqlify.qa.sinks;

import java.util.List;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.sparql.core.Quad;


public class MappingQuadMeasureDatum extends MeasureDatum {
	
	private List<Pair<Quad, ViewDefinition>> quadViewDefs;


	public MappingQuadMeasureDatum(String dimension, String metric, float value, 
			List<Pair<Quad, ViewDefinition>> quadViewDefs) {
		
		super();
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.quadViewDefs = quadViewDefs;
	}


	public List<Pair<Quad, ViewDefinition>> getQuadViewDefs() {
		return quadViewDefs;
	}
}
