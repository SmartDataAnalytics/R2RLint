package org.aksw.sml_eval.core;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.jdbc.Column;
import org.aksw.commons.util.jdbc.Relation;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.talis.rdfwriters.json.JSONJenaWriter;

public class TaskRepo {
	private Map<String, TaskBundle> nameToTask;
	
	public TaskBundle getTask(String taskId) {
		TaskBundle result = nameToTask.get(taskId);
		return result;
	}
	
	public Model getReferenceModel(String taskId) {
		TaskBundle task = nameToTask.get(taskId);
		if(task == null) {
			throw new RuntimeException("No task '" + taskId + "' exists");
		}
		
		Model result = task.getRefSet();
		return result;
	}
	
	public TaskRepo(List<TaskBundle> taskBundles) {
		nameToTask = new HashMap<String, TaskBundle>();
		
		for(TaskBundle taskBundle : taskBundles) {
			nameToTask.put(taskBundle.getTaskName(), taskBundle);
		}
	}
	
	public Map<String, TaskBundle> getTasks() {
		return nameToTask;
	}
	
	public String toJson() {
		Object o = toJsonMap();
		
		Gson gson = new Gson();
		String result = gson.toJson(o);
		return result;
	}
	
	public Object toJsonMap() {
		//Map<String, Object> result = new HashMap<String, Object>();
		List<Object> result = new ArrayList<Object>();
		
		for(TaskBundle taskBundle : nameToTask.values()) {			
			//result.put(taskBundle.getTaskName(), toJsonMap(taskBundle));
			result.add(toJsonMap(taskBundle));
		}
		
		return result;		
	}
	
	public Map<String, Object> toJsonMap(TaskBundle taskBundle) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("id", taskBundle.getTaskName());
		result.put("name", taskBundle.getTaskName());
		result.put("tables", toJsonMap(taskBundle.getRelations()));

		result.put("initialMappings", taskBundle.getMappings());
		
		JSONJenaWriter writer = new JSONJenaWriter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.write(taskBundle.getRefSet(), out, "http://example.org/");
		
		Gson gson = new Gson();
		Object o = gson.fromJson(out.toString(), Object.class);
		
		result.put("referenceData", o);
		result.put("description", taskBundle.getProperties().get("description"));
		result.put("hint", taskBundle.getProperties().get("hint"));

		return result;
	}
	
	public List<Object> toJsonMap(List<Table> tables) {
		List<Object> result = new ArrayList<Object>();
		for(Table table : tables) {
			result.add(toJsonMap(table));
		}
		
		return result;
	}
	
	public Map<String, Object> toJsonMap(Table table) {
		Map<String, Object> result = new HashMap<String, Object>();

		Relation tableSchema = table.getSchema();
		
		result.put("name", tableSchema.getName());
		List<Column> columns = new ArrayList<Column>(tableSchema.getColumns().values());

		Collections.sort(columns, new Comparator<Column>() {
			@Override
			public int compare(Column a, Column b) {
				return a.getOrdinalPosition() - b.getOrdinalPosition();
			}
		});

		List<Map<String, Object>> heads = new ArrayList<Map<String, Object>>();
		for(Column column : columns) {
			Map<String, Object> head = new HashMap<String, Object>();
			head.put("name", column.getName());
			head.put("type", column.getType());
			
			heads.add(head);
		}
		
		result.put("head", heads);
		result.put("body", table.getRowData());
		
		return result;		
	}
}