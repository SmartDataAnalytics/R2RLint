package org.aksw.sparqlify.qa.sinks;

import java.util.HashMap;

import org.aksw.sparqlify.qa.exceptions.NotImplementedException;

import com.hp.hpl.jena.sdb.exprmatch.NoExprMatch;


public class BooleanTestingSink implements MeasureDataSink {

	private HashMap<String, HashMap<TriplePosition, Boolean>> writes;


	public BooleanTestingSink() {
		writes = new HashMap<String, HashMap<TriplePosition, Boolean>>();
	}


	@Override
	public void initMeasure(String name, String type, String parentDimension) {
		HashMap<TriplePosition, Boolean> posWrites =
							new HashMap<TriplePosition, Boolean>();
		posWrites.put(TriplePosition.SUBJECT, false);
		posWrites.put(TriplePosition.PREDICATE, false);
		posWrites.put(TriplePosition.OBJECT, false);
		
		writes.put(name, posWrites);
	}


	@Override
	public void write(MeasureDatum datum) throws NotImplementedException {
		if (datum.getClass().getName().equals(NodeMeasureDatum.class.getName())) {
			writeNodeMeasure((NodeMeasureDatum) datum);
		} else {
			throw new NotImplementedException();
		}
	}


	private void writeNodeMeasure(NodeMeasureDatum datum) {
		writes.get(datum.metric).put(datum.pos, true);
	}


	public boolean written(String metricName, TriplePosition pos) {
		return writes.get(metricName).get(pos);
	}

}
