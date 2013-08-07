package org.aksw.sparqlify.qa.metrics;

import java.sql.Connection;

import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;


public interface Metric {
	
	public void setParentDimension(String parentDimension);
	
	public void setThreshold(float threshold);
	
	public String getParentDimension();

	public void registerMeasureDataSink(MeasureDataSink sink);
	
	public void registerDbConnection(Connection conn);
	
	public void registerPinpointer(Pinpointer pinpointer);
	
	public void setName(String name);
}