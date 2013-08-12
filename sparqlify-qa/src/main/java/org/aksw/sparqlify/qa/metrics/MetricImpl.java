package org.aksw.sparqlify.qa.metrics;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.MeasureDatum;
import org.aksw.sparqlify.qa.sinks.NodeMeasureDatum;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Triple;

public abstract class MetricImpl implements Metric {

	String name;
	String parentDimension;
	float threshold = 0;
	MeasureDataSink sink;


	@Override
	public String getParentDimension() {
		return parentDimension;
	}


	@Override
	public void setParentDimension(String parentDimension) {
		this.parentDimension = parentDimension;
	}


	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}


	@Override
	public void registerMeasureDataSink(MeasureDataSink sink) {
		this.sink = sink;
		sink.initMeasure(name, getClass().getName(), parentDimension);
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	// TODO: fix this first shot approach method signature
	protected void writeToSink(float val, String note1, String note2,
			Set<ViewQuad<ViewDefinition>> viewQuads) {
		
		MeasureDatum datum = new NodeMeasureDatum(parentDimension, name, val,
				pos, triple, viewQuads);
		
		sink.write(datum);
	}

}
