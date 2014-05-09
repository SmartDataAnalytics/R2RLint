package org.aksw.sparqlify.qa.sinks;

import java.util.List;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node_Variable;



public class MappingVarMeasureDatum extends MeasureDatum {
	
	private List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs;

	public MappingVarMeasureDatum(String dimension, String metric, float value, 
			List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs) {

		super();
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.nodeViewDefs = nodeViewDefs;
	}


	public List<Pair<Node_Variable, ViewDefinition>> getNodeViewDefs() {
		return nodeViewDefs;
	}
}
