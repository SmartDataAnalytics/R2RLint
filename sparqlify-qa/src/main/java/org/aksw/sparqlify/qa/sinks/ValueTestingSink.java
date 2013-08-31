package org.aksw.sparqlify.qa.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.TripleMetric;

public class ValueTestingSink implements MeasureDataSink {
	
	private HashMap<String, Float> mappingWrites;
	private HashMap<String, Float> datasetWrites;
	private HashMap<String, Float> tripleWrites;
	private HashMap<String, Float> nodeWrites;


	public ValueTestingSink() {
		mappingWrites = new HashMap<String, Float>();
		datasetWrites = new HashMap<String, Float>();
		tripleWrites = new HashMap<String, Float>();
		nodeWrites = new HashMap<String, Float>();
	}


	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls,
			String parentDimension) throws NotImplementedException {
		
		List<String> interfaceNames = new ArrayList<String>();
		
		for (Class<?> interfce : cls.getInterfaces()){
			interfaceNames.add(interfce.getName());
		}
		
		// if node metric
		if (interfaceNames.contains(NodeMetric.class.getName())) {
			nodeWrites.put(name, (float) -1);
		
		// if triple metric
		} else if (interfaceNames.contains(TripleMetric.class.getName())) {
			tripleWrites.put(name, (float) -1);
		
		// if dataset metric
		} else if (interfaceNames.contains(DatasetMetric.class.getName())) {
			datasetWrites.put(name, (float) -1);
		
		// if mapping metric
		} else if (interfaceNames.contains(MappingMetric.class.getName())) {
			mappingWrites.put(name, (float) -1);
		
		// else
		} else {
			throw new NotImplementedException();
		}
	}


	@Override
	public void write(MeasureDatum datum) throws NotImplementedException {
		
		// if node measure
		if (datum instanceof NodeMeasureDatum) {
			writeNodeMeasure((NodeMeasureDatum) datum);
		
		// if triple measure
		} else if (datum instanceof TripleMeasureDatum) {
			writeTripleMeasure((TripleMeasureDatum) datum);
		
		// if dataset measure
		} else if (datum instanceof DatasetMeasureDatum){
			writeDatasetMeasure((DatasetMeasureDatum) datum);
		
		// if mapping measure
		} else if (datum instanceof MappingMeasureDatum) {
			writeMappingMeasure(datum);
		
		// if mapping var measure
		} else if (datum instanceof MappingVarMeasureDatum) {
			writeMappingMeasure(datum);
		
		// if mapping quad measure
		} else if (datum instanceof MappingQuadMeasureDatum) {
			writeMappingMeasure(datum);
		} else {
			throw new NotImplementedException();
		}
	}


	/* dataset measure methods */
	private void writeDatasetMeasure(DatasetMeasureDatum datum) {
		datasetWrites.put(datum.getMetric(), datum.getValue());
	}

	public float datasetMeasureValue(String metricName) {
		return datasetWrites.get(metricName);
	}


	/* mapping (var/quad) measure methods */ 
	private void writeMappingMeasure(MeasureDatum datum) {
		mappingWrites.put(datum.getMetric(), datum.getValue());
	}

	public float mappingMeasureValue(String metricName) {
		return mappingWrites.get(metricName);
	}


	/* triple measure methods */
	private void writeTripleMeasure(TripleMeasureDatum datum) {
		tripleWrites.put(datum.getMetric(), datum.getValue());
	}

	public float tripleMeasureValue(String metricName) {
		return tripleWrites.get(metricName);
	}


	/* node measure methods */
	private void writeNodeMeasure(NodeMeasureDatum datum) {
		nodeWrites.put(datum.getMetric(), datum.getValue());
	}

	public float nodeMeasureWritten(String metricName) {
		return nodeWrites.get(metricName);
	}
}
