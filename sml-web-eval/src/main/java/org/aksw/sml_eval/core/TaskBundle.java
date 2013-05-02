package org.aksw.sml_eval.core;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.commons.util.jdbc.DataSourceConfig;

import com.hp.hpl.jena.rdf.model.Model;

public class TaskBundle {
	private String taskName;

	private Properties properties;
	private Map<String, String> mappings;
	private Model refSet;
	private List<Table> relations;
	
	private DataSourceConfig dataSourceConfig;

	public TaskBundle(String taskName, Properties properties, Map<String, String> mappings, Model refSet, List<Table> relations, DataSourceConfig dataSourceConfig) {
		this.taskName = taskName;
		this.properties = properties;
		this.mappings = mappings;
		this.refSet = refSet;
		this.relations = relations;
		this.dataSourceConfig = dataSourceConfig;
	}
	
	public String getTaskName() {
		return taskName;
	}

	public Properties getProperties() {
		return properties;
	}

	public Map<String, String> getMappings() {
		return mappings;
	}

	public Model getRefSet() {
		return refSet;
	}
	
	public List<Table> getRelations() {
		return relations;
	}

	public DataSourceConfig getDataSourceConfig() {
		return dataSourceConfig;
	}

	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	public void setRefSet(Model refSet) {
		this.refSet = refSet;
	}

	public void setRelations(List<Table> relations) {
		this.relations = relations;
	}

	public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
		this.dataSourceConfig = dataSourceConfig;
	}
}