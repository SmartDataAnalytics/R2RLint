package org.aksw.sparqlify.qa.metrics.amountofdata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This metric measures the coverage of a dataset referring to the number of
 * details that are described. This number of details is expressed with the
 * number of different properties that are used in the dataset.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CoverageDetail extends MetricImpl implements DatasetMetric {
	
	List<Property> seenProperties;
	long numProperties;
	long numTriples;

	protected void clearCaches() {
		seenProperties = new ArrayList<Property>();
		numProperties = 0;
		numTriples = 0;
	}

	public CoverageDetail() {
		super();
		seenProperties = new ArrayList<Property>();
		numProperties = 0;
		numTriples = 0;
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		numTriples = dataset.size();
		StmtIterator statementsIt = dataset.listStatements();
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			Property predicate = statement.getPredicate();
			
			if (!seenProperties.contains(predicate)) {
				numProperties++;
				seenProperties.add(predicate);
			}
		}
		
		float ratio;
		if (numTriples == 0) ratio = 0;
		else ratio = numProperties / (float) numTriples;
		
		writeDatasetMeasureToSink(ratio);
	}

}
