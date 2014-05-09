package org.aksw.sparqlify.qa.sinks;


public abstract class MeasureDatum {
	
	protected float value;
	protected String dimension;
	protected String metric;


	public float getValue() {
		return value;
	}


	public String getDimension() {
		return dimension;
	}


	public String getMetric() {
		return metric;
	}
}
