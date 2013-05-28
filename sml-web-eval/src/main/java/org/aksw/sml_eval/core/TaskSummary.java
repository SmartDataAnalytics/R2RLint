package org.aksw.sml_eval.core;

public class TaskSummary {
	private String taskId;
	private boolean isCompleted;

	public TaskSummary(String taskId) {
		this(taskId, false);
	}
	
	public TaskSummary(String taskId, boolean isCompleted) {
		super();
		this.taskId = taskId;
		this.isCompleted = isCompleted;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
	
	
}