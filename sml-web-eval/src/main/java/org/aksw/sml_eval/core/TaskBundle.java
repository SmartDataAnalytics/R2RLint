package org.aksw.sml_eval.core;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.commons.util.jdbc.Relation;

import com.hp.hpl.jena.rdf.model.Model;

public class TaskBundle {
	private String taskName;

	private Properties properties;
	private Map<String, String> mappings;
	private Model refSet;
	private List<Table> relations;

	public TaskBundle(String taskName, Properties properties, Map<String, String> mappings, Model refSet, List<Table> relations) {
		this.taskName = taskName;
		this.properties = properties;
		this.mappings = mappings;
		this.refSet = refSet;
		this.relations = relations;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mappings == null) ? 0 : mappings.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((refSet == null) ? 0 : refSet.hashCode());
		result = prime * result
				+ ((relations == null) ? 0 : relations.hashCode());
		result = prime * result
				+ ((taskName == null) ? 0 : taskName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskBundle other = (TaskBundle) obj;
		if (mappings == null) {
			if (other.mappings != null)
				return false;
		} else if (!mappings.equals(other.mappings))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (refSet == null) {
			if (other.refSet != null)
				return false;
		} else if (!refSet.equals(other.refSet))
			return false;
		if (relations == null) {
			if (other.relations != null)
				return false;
		} else if (!relations.equals(other.relations))
			return false;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TaskBundle [taskName=" + taskName + ", properties="
				+ properties + ", mappings=" + mappings + ", refSet=" + refSet
				+ ", relations=" + relations + "]";
	}
	
}