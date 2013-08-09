package org.aksw.sparqlify.qa.metrics;

import java.sql.Connection;

import com.hp.hpl.jena.graph.Triple;

public class NoProlixFeatures implements TripleMetric {
	
	private String parentDimension = null;
	private String name;
	private float threshold;
	private MeasureDataSink sink;
	private Connection conn;
	private Pinpointer pinpointer;
	
	
	@Override
	public void assessTriple(Triple triple) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public String getParentDimension() {
		return parentDimension;
	}


	@Override
	public void setParentDimension(String parentDimension) {
		this.parentDimension = parentDimension;
		
	}


	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}


	@Override
	public void registerMeasureDataSink(MeasureDataSink sink) {
		this.sink = sink;
	}


	@Override
	public void registerDbConnection(Connection conn) {
		this.conn = conn;
	}


	@Override
	public void registerPinpointer(Pinpointer pinpointer) {
		this.pinpointer = pinpointer;
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}

}
