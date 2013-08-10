package org.aksw.sparqlify.qa.sinks;

import org.aksw.sparqlify.qa.metrics.MeasureDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummySink implements MeasureDataSink {
	
	private Logger logger;

	
	public DummySink() {
		logger = LoggerFactory.getLogger("Dummy sink");
	}
	
	@Override
	public void initMeasure(String name, String type, String parentDimension) {
		// pass
	}

	@Override
	public void write(MeasureDatum datum) {
		logger.info(datum.getDimension() + "'s measure " + datum.getMeasure() +
				" wrote value " + datum.getValue() +
				"\nfor: " + datum.getSource() + "\nbecause of: " + datum.getAdditionalSource());

	}

}
