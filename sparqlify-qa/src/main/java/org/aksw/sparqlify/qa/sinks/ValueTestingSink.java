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
	
	public ValueTestingSink() {
		mappingWrites = new HashMap<String, Float>();
	}

	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls,
			String parentDimension) throws NotImplementedException {
		
		List<String> interfaceNames = new ArrayList<String>();
		
		for (Class<?> interfce : cls.getInterfaces()){
			interfaceNames.add(interfce.getName());
		}
		
		if (interfaceNames.contains(NodeMetric.class.getName())) {
			throw new NotImplementedException();
		} else if (interfaceNames.contains(TripleMetric.class.getName())) {
			throw new NotImplementedException();
		} else if (interfaceNames.contains(DatasetMetric.class.getName())) {
			throw new NotImplementedException();
		} else if (interfaceNames.contains(MappingMetric.class.getName())) {
			mappingWrites.put(name, null);
		} else {
			throw new NotImplementedException();
		}
	}


	@Override
	public void write(MeasureDatum datum) throws NotImplementedException {
		if (datum.getClass().getName().equals(NodeMeasureDatum.class.getName())) {
			throw new NotImplementedException();
		} else if (datum.getClass().getName().equals(TripleMeasureDatum.class.getName())) {
			throw new NotImplementedException();
		} else if (datum.getClass().getName().equals(DatasetMeasureDatum.class.getName())){
			throw new NotImplementedException();
		} else if (datum.getClass().getName().equals(MappingMeasureDatum.class.getName())) {
			writeMappingMeasure((MappingMeasureDatum) datum);
		} else {
			throw new NotImplementedException();
		}
	}


	private void writeMappingMeasure(MappingMeasureDatum datum) {
		mappingWrites.put(datum.getMetric(), datum.getValue());
	}


	public float mappingMeasureValue(String metricName) {
		return mappingWrites.get(metricName);
	}
}
