package org.aksw.sparqlify.qa.metrics;

import java.sql.Connection;

public class PinpointDbMetric_rm extends PinpointMetric_rm implements Metric {
	
	protected Connection conn;

	public void registerDbConnection(Connection conn) {
		this.conn = conn;
	}

}
