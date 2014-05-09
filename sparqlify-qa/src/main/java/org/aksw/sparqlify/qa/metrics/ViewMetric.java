package org.aksw.sparqlify.qa.metrics;

import java.sql.SQLException;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;


public interface ViewMetric extends Metric {
	
	public void assessViews(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException;

}
