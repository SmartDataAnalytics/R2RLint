package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;


public interface MeasureDataSink {

	public void initMeasure(String name, String type, String parentDimension);
	
	public void write(MeasureDatum datum) throws NotImplementedException;

}