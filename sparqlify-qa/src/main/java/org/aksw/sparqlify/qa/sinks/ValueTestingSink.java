package org.aksw.sparqlify.qa.sinks;

import java.util.HashMap;

import org.aksw.sparqlify.qa.metrics.MetricImpl;

public class ValueTestingSink implements MeasureDataSink {
	
	private HashMap<String, Float> writes;


	public ValueTestingSink() {
		writes = new HashMap<String, Float>();
	}


	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls,
			String parentDimension) {
		
		writes.put(name, (float) -1);
	}


	@Override
	public void write(MeasureDatum datum) {
		writes.put(datum.getMetric(), datum.getValue());
	}


	public float writtenValue(String metricName) {
		return writes.get(metricName);
	}
}
