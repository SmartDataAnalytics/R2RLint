package org.aksw.sparqlify.qa.sinks;

import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Triple;

public class TriplesMeasureDatum extends MeasureDatum {
	
	List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> tripleViewQuads;
	
	public TriplesMeasureDatum(String dimension, String metric, float value,
			List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> tripleViewQuads) {
		super();
		
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
		this.tripleViewQuads = tripleViewQuads;
	}

	public List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> getPinpoinInfos() {
		return tripleViewQuads;
	}
}
