package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.sinks.MeasureDataSink;

public abstract class MetricImpl implements Metric {

	String name;
	String parentDimension;
	float threshold = 0;
	MeasureDataSink sink;

	
	@Override
	public void setParentDimension(String parentDimension) {
		this.parentDimension = parentDimension;
	}

	
	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	
	@Override
	public String getParentDimension() {
		return parentDimension;
	}

	
	@Override
	public void registerMeasureDataSink(MeasureDataSink sink) {
		this.sink = sink;
	}

	
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
