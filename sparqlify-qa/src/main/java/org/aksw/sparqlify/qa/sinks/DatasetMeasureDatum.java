package org.aksw.sparqlify.qa.sinks;

public class DatasetMeasureDatum extends MeasureDatum {
	
	public DatasetMeasureDatum(String dimension, String metric, float value) {
		super();
		this.dimension = dimension;
		this.metric = metric;
		this.value = value;
	}

}
