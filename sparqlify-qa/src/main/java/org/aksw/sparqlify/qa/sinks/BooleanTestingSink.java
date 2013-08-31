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
	public void initMeasure(String name, Class<? extends MetricImpl> cls,
			String parentDimension) throws NotImplementedException {
		
		List<String> interfaceNames = new ArrayList<String>();
		
		for (Class<?> interfce : cls.getInterfaces()){
			interfaceNames.add(interfce.getName());
		}
		
		// if node metric
		if (interfaceNames.contains(NodeMetric.class.getName())) {
			HashMap<TriplePosition, Boolean> posWrites =
								new HashMap<TriplePosition, Boolean>();
			posWrites.put(TriplePosition.SUBJECT, false);
			posWrites.put(TriplePosition.PREDICATE, false);
			posWrites.put(TriplePosition.OBJECT, false);
			
			nodeWrites.put(name, posWrites);
		
		// if triple metric
		} else if (interfaceNames.contains(TripleMetric.class.getName())) {
			tripleWrites.put(name, false);
			
		// if mapping metric
		} else if (interfaceNames.contains(MappingMetric.class.getName())) {
			mappingWrites.put(name, false);
			
		// else
		} else {
			throw new NotImplementedException();
		}
	}


	@Override
	public void write(MeasureDatum datum) throws NotImplementedException {
		
		// if node measure datum
		if (datum instanceof NodeMeasureDatum) {
			writeNodeMeasure((NodeMeasureDatum) datum);
			
		// if triple measure datum
		} else if (datum instanceof TripleMeasureDatum) {
			writeTripleMeasure((TripleMeasureDatum) datum);
		
		// if mapping measure datum
		} else if (datum instanceof MappingMeasureDatum) {
			writeMappingMeasure(datum);
		
		// if mapping var measure datum
		} else if(datum instanceof MappingVarMeasureDatum) {
			writeMappingMeasure(datum);
		
		// if mapping quad measure datum
		} else if (datum instanceof MappingQuadMeasureDatum) {
			writeMappingMeasure(datum);
		
		// else
		} else {
			throw new NotImplementedException();
		}
	}


	/* triple measure datum methods */
	private void writeTripleMeasure(TripleMeasureDatum datum) {
		tripleWrites.put(datum.getMetric(), true);
	}

	public boolean tripleMeasureWritten(String metricName) {
		return tripleWrites.get(metricName);
	}


	/* node measure datum methods */
	private void writeNodeMeasure(NodeMeasureDatum datum) {
		nodeWrites.get(datum.getMetric()).put(datum.getTriplePosition(), true);
	}

	public boolean nodeMeasureWritten(String metricName, TriplePosition pos) {
		return nodeWrites.get(metricName).get(pos);
	}


	/* mapping measure datum methods */
	public void writeMappingMeasure(MeasureDatum datum) {
		mappingWrites.put(datum.getMetric(), true);
	}

	public boolean mappingMeasureWritten(String metricName) {
		return mappingWrites.get(metricName);
	}
}
