package org.aksw.sml_eval.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TaskState {
	
}

/**
 * Summary about the completion of a stage
 * 
 * Select ?s ?if { ?s a ex:Stage ; ex:userId ?uid ; ex:isFinished ?if }
 * 
 * @author raven
 *
 */
class SummaryStage {
	private String stageId;
	private Map<String, Boolean> taskCompletion = new HashMap<String, Boolean>();
	
	
	
}



public class SummaryTotal {
	private List<SummaryStage> stageSummaries;	
	private boolean isAllCompleted;
	
	public SummaryTotal(List<SummaryStage> stageSummaries, boolean isAllCompleted) {
		this.stageSummaries = stageSummaries;
		this.isAllCompleted = isAllCompleted;
	}
	
	public boolean isAllCompleted() {
		return isAllCompleted;
	}
}
