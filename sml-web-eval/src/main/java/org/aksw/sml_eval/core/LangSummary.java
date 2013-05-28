package org.aksw.sml_eval.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary for a language of the eval
 * 
 * 
 * @author raven
 *
 */
public class LangSummary {
	private List<String> taskOrder;
	private Map<String, TaskSummary> taskSummaries;

	public LangSummary() {
		//this.taskSummaries = new HashMap<String, TaskSummary>();
		this(new ArrayList<String>(), new HashMap<String, TaskSummary>());
	}
	
	public LangSummary(List<String> taskOrder, Map<String, TaskSummary> taskSummaries) {
		super();
		this.taskOrder = taskOrder;
		this.taskSummaries = taskSummaries;
	}

	public List<String> getTaskOrder() {
		return taskOrder;
	}
	
	public Map<String, TaskSummary> getTaskSummaries() {
		return taskSummaries;
	}	
}