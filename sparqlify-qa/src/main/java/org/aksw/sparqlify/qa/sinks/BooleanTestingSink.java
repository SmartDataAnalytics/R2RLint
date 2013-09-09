package org.aksw.sparqlify.qa.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.NodeMetric;


public class BooleanTestingSink implements MeasureDataSink {

	private HashMap<String, HashMap<TriplePosition, Boolean>> nodeWrites;
	private HashMap<String, Boolean> writes;


	public BooleanTestingSink() {
		nodeWrites = new HashMap<String, HashMap<TriplePosition, Boolean>>();
		writes = new HashMap<String, Boolean>();
	}


	@Override
	public void initMeasure(String name, Class<? extends MetricImpl> cls,
			String parentDimension) {
		
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
		
		} else if (interfaceNames.contains(DatasetMetric.class.getName())) {
			// some DatasetMetrics use NodeMeasureDatum, some not
			
			HashMap<TriplePosition, Boolean> posWrites =
					new HashMap<TriplePosition, Boolean>();
			posWrites.put(TriplePosition.SUBJECT, false);
			posWrites.put(TriplePosition.PREDICATE, false);
			posWrites.put(TriplePosition.OBJECT, false);

			nodeWrites.put(name, posWrites);
			writes.put(name, false);
		// else
		} else {
			writes.put(name, false);
		}
	}


	@Override
	public void write(MeasureDatum datum) {
		
		// if node measure datum
		if (datum instanceof NodeMeasureDatum) {
			writeNodeMeasure((NodeMeasureDatum) datum);
			
		} else {
			writes.put(datum.getMetric(), true);
		} 
	}


	public boolean measureWritten(String metricName) {
		return writes.get(metricName);
	}


	/* node measure datum methods */
	private void writeNodeMeasure(NodeMeasureDatum datum) {
		nodeWrites.get(datum.getMetric()).put(datum.getTriplePosition(), true);
	}

	public boolean nodeMeasureWritten(String metricName, TriplePosition pos) {
		return nodeWrites.get(metricName).get(pos);
	}
}
