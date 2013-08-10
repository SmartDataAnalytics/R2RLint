package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.pinpointing.Pinpointer;

public abstract class PinpointMetric extends MetricImpl implements Metric {
	
	Pinpointer pinpointer;


	public void registerPinpointer(Pinpointer pinpointer) {
		this.pinpointer = pinpointer;

	}
}
