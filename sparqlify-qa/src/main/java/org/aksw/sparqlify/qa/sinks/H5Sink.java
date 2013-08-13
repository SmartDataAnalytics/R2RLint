package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.metrics.MetricImpl;


public class H5Sink implements MeasureDataSink {

	public H5Sink(String h5FilePath) {
		// TODO: implement
	}
	
	@Override
	public void write(MeasureDatum datum) {
		// TODO Auto-generated method stub
		
	}

	
	private void createOrOpenFile(String h5FilePath) {
		// TODO: implement
	}

	
	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls, String parentDimension) {
		// TODO Auto-generated method stub
	}
}
