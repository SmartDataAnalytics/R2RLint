package org.aksw.sparqlify.qa.metrics;

public interface MeasureDataSink {

	public void initMeasure(String name, String type, String parentDimension);
	
	public void write(MeasureDatum datum);

}
