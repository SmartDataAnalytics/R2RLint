package org.aksw.sparqlify.qa.metrics;

import java.sql.Connection;

public class PinpointDbMetric extends PinpointMetric implements Metric {
	
	protected Connection conn;

	public void registerDbConnection(Connection conn) {
		this.conn = conn;
	}

}
