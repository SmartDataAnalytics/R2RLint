package org.aksw.sml_eval.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvalSummary {
	private Integer userId;
	private String currentLang;
	private List<String> langOrder = new ArrayList<String>();
	private Map<String, LangSummary> langSummaries = new HashMap<String, LangSummary>();

	private boolean canAdvance;
	private boolean isAllTasksComplete;
	private boolean isEvalComplete;
	
//	public EvalSummary(Map<String, LangSummary> langSummaries) {
//		super();
//		this.langSummaries = langSummaries;
//	}

	public EvalSummary(Integer userId, Map<String, LangSummary> langSummaries, String currentLang, List<String> langOrder) {
		super();
		this.userId = userId;
		this.langSummaries = langSummaries;
		this.currentLang = currentLang;
		this.langOrder = langOrder;
	}

	public Map<String, LangSummary> getLangToSummaries() {
		return langSummaries;
	}

	public void setLangToSummaries(Map<String, LangSummary> langSummaries) {
		this.langSummaries = langSummaries;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getCurrentLang() {
		return currentLang;
	}

	public void setCurrentLang(String currentLang) {
		this.currentLang = currentLang;
	}

	public List<String> getLangOrder() {
		return langOrder;
	}

	public void setLangOrder(List<String> langOrder) {
		this.langOrder = langOrder;
	}

	public Map<String, LangSummary> getLangSummaries() {
		return langSummaries;
	}

	public void setLangSummaries(Map<String, LangSummary> langSummaries) {
		this.langSummaries = langSummaries;
	}

	public boolean isCanAdvance() {
		return canAdvance;
	}

	public void setCanAdvance(boolean canAdvance) {
		this.canAdvance = canAdvance;
	}

	public boolean isCompleted() {
		return isAllTasksComplete;
	}

	public void setCompleted(boolean isCompleted) {
		this.isAllTasksComplete = isCompleted;
	}

	public boolean isAllTasksComplete() {
		return isAllTasksComplete;
	}

	public void setAllTasksComplete(boolean isAllTasksComplete) {
		this.isAllTasksComplete = isAllTasksComplete;
	}

	public boolean isEvalComplete() {
		return isEvalComplete;
	}

	public void setEvalComplete(boolean isEvalComplete) {
		this.isEvalComplete = isEvalComplete;
	}
}
