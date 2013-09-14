package org.aksw.sparqlify.qa.sinks;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class NodeTripleMeasureDatum extends MeasureDatum {
	
	protected TriplePosition pos;
	protected Triple triple;
	protected Set<ViewQuad<ViewDefinition>> viewQuads;

	public NodeTripleMeasureDatum(String dimension, String metric, float value,
			TriplePosition pos, Triple triple,
			Set<ViewQuad<ViewDefinition>> viewQuads) {
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.pos = pos;
		this.triple = triple;
		this.viewQuads = viewQuads;
		
	}


	public Set<ViewQuad<ViewDefinition>> getViewQuads() {
		return viewQuads;
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
