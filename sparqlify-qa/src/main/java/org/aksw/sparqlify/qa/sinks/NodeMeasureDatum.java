package org.aksw.sparqlify.qa.sinks;

import com.hp.hpl.jena.graph.Node;

public class NodeMeasureDatum extends MeasureDatum {
	
	private Node node;
	
	public NodeMeasureDatum(String dimension, String metric, float value, Node node) {
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.node = node;
	}


	public Node getNode() {
		return node;
	}
}
