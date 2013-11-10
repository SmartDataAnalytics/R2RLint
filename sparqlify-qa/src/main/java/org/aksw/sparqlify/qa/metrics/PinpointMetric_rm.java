package org.aksw.sparqlify.qa.metrics;

import org.aksw.sparqlify.qa.pinpointing.Pinpointer;

public abstract class PinpointMetric_rm extends MetricImpl implements Metric {
	
	protected Pinpointer pinpointer;


	public void registerPinpointer(Pinpointer pinpointer) {
		this.pinpointer = pinpointer;

	}
}
