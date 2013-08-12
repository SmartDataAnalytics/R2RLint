package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class NodeMeasureDatum extends MeasureDatum {
	
	TriplePosition pos;
	Triple triple;

	public NodeMeasureDatum(String dimension, String metric, float value,
			TriplePosition pos, Triple triple,
			Set<ViewQuad<ViewDefinition>> viewQuads) {
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.pos = pos;
		this.triple = triple;
		this.viewQuads = viewQuads;
		
	}


	public TriplePosition getTriplePosition() {
		return pos;
	}


	public Triple getTriple() {
		return triple;
	}


	public Node getNode() {
		switch (pos) {
		case SUBJECT:
			return triple.getSubject();
		case PREDICATE:
			return triple.getPredicate();
		case OBJECT:
			return triple.getObject();

		default:
			return null;
		}
	}
}
