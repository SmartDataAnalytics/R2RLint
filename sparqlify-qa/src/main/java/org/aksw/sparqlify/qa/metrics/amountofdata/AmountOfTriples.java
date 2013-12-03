package org.aksw.sparqlify.qa.metrics.amountofdata;

import java.sql.SQLException;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Controller;

@Controller
public class AmountOfTriples extends MetricImpl implements DatasetMetric {

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		/*
		 * 1   : > 1,000,000,000                 triples
		 * 0.75:      10,000,000 - 1,000,000,000 triples
		 * 0.5 :         500,000 -    10,000,000 triples
		 * 0.25:          10,000 -       500,000 triples
		 * 0   :                        < 10,000 triples
		 */
		long high       = 1000000000;
		long mediumHigh =   10000000;
		long mediumLow  =     500000;
		long low        =      10000;
		
		long numTriples = dataset.size();
		float val;
		
		if (numTriples >= high) val = 1;
		else if (numTriples < high       && numTriples >= mediumHigh) val = (float) 0.75;
		else if (numTriples < mediumHigh && numTriples >= mediumLow) val = (float) 0.5;
		else if (numTriples < mediumLow  && numTriples >= low) val = (float) 0.25;
		else val = 0;
		
		writeDatasetMeasureToSink(val);
	}

}
