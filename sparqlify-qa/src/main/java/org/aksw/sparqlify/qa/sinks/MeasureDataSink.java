package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.metrics.MeasureDatum;

public interface MeasureDataSink {

	public void initMeasure(String name, String type, String parentDimension);
	
	public void write(MeasureDatum datum);

}