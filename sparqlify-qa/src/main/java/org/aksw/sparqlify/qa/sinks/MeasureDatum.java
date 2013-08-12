package org.aksw.sparqlify.qa.metrics;

public class MeasureDatum {
	
	private float value;
	private String dimension;
	private String measure;
	private String source0;
	private String source1;
	
	
	public MeasureDatum(String dimension, String measure, float value, String source0, String source1) {
		this.dimension = dimension;
		this.measure = measure;
		this.value = value;
		this.source0 = source0;
		this.source1 = source1;
	}
	
	
	public MeasureDatum(String dimension, String measure, float value, String source) {
		this.dimension = dimension;
		this.measure = measure;
		this.value = value;
		this.source0 = source;
	}
	
	
	public float getValue() {
		return value;
	}
	
	
	public String getDimension() {
		return dimension;
	}
	
	
	public String getMeasure() {
		return measure;
	}
	
	
	public String getSource() {
		return source0;
	}
	
	
	public boolean hasAdditionalSource() {
		return (source1 != null) ? true : false;
	}

	
	public String getAdditionalSource() {
		return source1;
	}

}
