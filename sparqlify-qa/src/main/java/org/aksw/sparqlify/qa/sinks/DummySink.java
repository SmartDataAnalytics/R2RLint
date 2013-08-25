package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummySink implements MeasureDataSink {
	
	private Logger logger;

	
	public DummySink() {
		logger = LoggerFactory.getLogger("Dummy sink");
	}
	
	
	@Override
	public void initMeasure(String name,Class<? extends MetricImpl> cls, String parentDimension) {
		// pass
	}

	
	@Override
	public void write(MeasureDatum datum) {
		String logLine = datum.getDimension() + "'s measure " +
				datum.getMetric() + " wrote value " + datum.getValue() + "\n" +
						"\tfor: ";
		
		if (datum.getClass().getName().equals(NodeMeasureDatum.class.getName())) {
			logLine += ((NodeMeasureDatum) datum).getTriplePosition().name() +
					" position in " + ((NodeMeasureDatum) datum).getTriple();
			
		} else if (datum.getClass().getName().equals(TripleMeasureDatum.class.getName())) {
			logLine += ((TripleMeasureDatum) datum).getTriple();
			
//		} else if (datum.getClass().getName().equals(DatasetMeasureDatum.class.getName())) {
//			logLine += ???
		}
		
		logger.info(logLine);
	}
}
