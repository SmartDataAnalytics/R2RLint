package org.aksw.sparqlify.qa.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.TripleMetric;


public class BooleanTestingSink implements MeasureDataSink {

	private HashMap<String, HashMap<TriplePosition, Boolean>> nodeWrites;
	private HashMap<String, Boolean> tripleWrites;
	private HashMap<String, Boolean> mappingWrites;


	public BooleanTestingSink() {
		nodeWrites = new HashMap<String, HashMap<TriplePosition, Boolean>>();
		tripleWrites = new HashMap<String, Boolean>();
		mappingWrites = new HashMap<String, Boolean>();
	}


	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls, String parentDimension) throws NotImplementedException {
		List<String> interfaceNames = new ArrayList<String>();
		
		for (Class<?> interfce : cls.getInterfaces()){
			interfaceNames.add(interfce.getName());
		}
		
		if (interfaceNames.contains(NodeMetric.class.getName())) {
			HashMap<TriplePosition, Boolean> posWrites =
								new HashMap<TriplePosition, Boolean>();
			posWrites.put(TriplePosition.SUBJECT, false);
			posWrites.put(TriplePosition.PREDICATE, false);
			posWrites.put(TriplePosition.OBJECT, false);
			
			nodeWrites.put(name, posWrites);
		} else if (interfaceNames.contains(TripleMetric.class.getName())) {
			tripleWrites.put(name, false);
		} else if (interfaceNames.contains(MappingMetric.class.getName())) {
			mappingWrites.put(name, false);
		} else {
			throw new NotImplementedException();
		}
	}


	@Override
	public void write(MeasureDatum datum) throws NotImplementedException {
		if (datum.getClass().getName().equals(NodeMeasureDatum.class.getName())) {
			writeNodeMeasure((NodeMeasureDatum) datum);
		} else if (datum.getClass().getName().equals(TripleMeasureDatum.class.getName())) {
			writeTripleMeasure((TripleMeasureDatum) datum);
		} else if (datum.getClass().getName().equals(MappingMeasureDatum.class.getName())) {
			writeMappingMeasure((MappingMeasureDatum) datum);
		} else {
			throw new NotImplementedException();
		}
	}


	private void writeTripleMeasure(TripleMeasureDatum datum) {
		tripleWrites.put(datum.metric, true);
	}


	public boolean tripleMeasureWritten(String metricName) {
		return tripleWrites.get(metricName);
	}


	private void writeNodeMeasure(NodeMeasureDatum datum) {
		nodeWrites.get(datum.metric).put(datum.pos, true);
	}


	public boolean nodeMeasureWritten(String metricName, TriplePosition pos) {
		return nodeWrites.get(metricName).get(pos);
	}


	public void writeMappingMeasure(MappingMeasureDatum datum) {
		mappingWrites.put(datum.getMetric(), true);
	}
	public boolean mappingMeasureWritten(String metricName) {
		return mappingWrites.get(metricName);
	}
}
