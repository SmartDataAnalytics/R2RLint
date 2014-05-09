package org.aksw.sparqlify.qa.dimensions;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.metrics.Metric;

public class Dimension {
	
	private List<Metric> metrics;
	private String name;
	
	public Dimension(String name) {
		this.name = name;
		metrics = new ArrayList<Metric>();
	}

	public List<Metric> getMetrics() {
		return metrics;
	}
	
	public void addMetric(Metric metric) {
		metrics.add(metric);
	}
	
	public String getName() {
		return name;
	}
}
