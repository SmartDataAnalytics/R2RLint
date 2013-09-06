package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;


public interface MeasureDataSink {

	public void initMeasure(String name, Class<? extends MetricImpl> class1, String parentDimension) throws NotImplementedException;
	
	public void write(MeasureDatum datum);

}