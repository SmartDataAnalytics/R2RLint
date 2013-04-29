package org.aksw.sml_eval.core;

import java.util.List;
import java.util.Map;

import org.aksw.commons.util.jdbc.Relation;

public class Table {
	private Relation schema;
	private List<Map<String, Object>> rowData;
	
	public Table(Relation schema, List<Map<String, Object>> rowData) {
		super();
		this.schema = schema;
		this.rowData = rowData;
	}

	public Relation getSchema() {
		return schema;
	}

	public void setSchema(Relation schema) {
		this.schema = schema;
	}

	public List<Map<String, Object>> getRowData() {
		return rowData;
	}

	public void setRowData(List<Map<String, Object>> rowData) {
		this.rowData = rowData;
	}
}
