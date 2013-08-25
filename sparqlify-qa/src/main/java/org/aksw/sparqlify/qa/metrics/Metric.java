package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;


public interface Metric {
	
	public void setParentDimension(String parentDimension);
	
	public void setThreshold(float threshold);
	
	public String getParentDimension();

	public void registerMeasureDataSink(MeasureDataSink sink) throws NotImplementedException;
		
	public void setName(String name);

	public String getName();
	
	public void setPrefix(String domain);
}