package org.aksw.sparqlify.qa.metrics;

import java.util.List;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;


public interface Metric {
	
	public void setParentDimension(String parentDimension);
	
	public void setThreshold(float threshold);
	
	public String getParentDimension();

	public void initMeasureDataSink() throws NotImplementedException;
		
	public void setName(String name);

	public String getName();
	
	public void setPrefixes(List<String> domain);
}