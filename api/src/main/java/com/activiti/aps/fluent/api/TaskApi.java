package com.activiti.aps.fluent.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskApi {

	private String taskName = StringUtils.EMPTY;
	private String outcome = StringUtils.EMPTY;
	private String assignee = StringUtils.EMPTY;

	protected ProcessApi processApi = null;
	
	private Map<String, Object> formValues = new HashMap<String, Object>();
	
	private static final Logger logger = LogManager.getLogger(TaskApi.class);

	public TaskApi(ProcessApi processApi, String taskName, String outcome) {
		this.processApi = processApi;
		this.taskName = taskName;
		this.outcome = outcome;
	}
	
	public TaskApi withAssignee(String assignee) {
		this.assignee = assignee;
		return this;
	}
	
	public TaskApi withFormValue(String id, String value) {
		formValues.put(id, value);
		return this;
	}
	
	public TaskApi hasAssignee() {
		if (StringUtils.isEmpty(this.assignee)) {
			logger.error("Process does not have an assignee defined.");
			throw new RuntimeException("Process does not have an assignee defined.");
		}
		return this;
	}
	
	public TaskApi withAdminAsAssignee() {
		this.assignee = FluentAps.ADMIN_USER;
		return this;
	}

	/*
	public String getAssignee() {
		return assignee;
	}
	*/
	
	public boolean hasFormValues() {
		if(this.formValues.size()>0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public Map<String, Object> getFormValues() {
		return formValues;
	}

	public String getName() {
		return taskName;
	}

	public String getOutcome() {
		return outcome;
	}

	public TaskApi thenSubmitTask(String taskName, String outcome) {
		processApi.fluentAps.thenSubmitTask(this);
		return new TaskApi(processApi, taskName, outcome);
	}

	// NOTE: return ProcessTester in case there are other check methods
	// we'd like to call at this point. If there were task checks they
	// would be called before this and would return a TaskTester.
	public ProcessApi thenCheckThatTheProcessIsFinished() {
		processApi.fluentAps.thenSubmitTask(this);
		return processApi.thenCheckThatTheProcessIsFinished();
	}

	@Override
	public String toString() {
		return "Task [name=" + taskName + ", outcome=" + outcome + ", assignee=" + assignee + ", formValues=" + formValues
				+ "]";
	}
	
}
