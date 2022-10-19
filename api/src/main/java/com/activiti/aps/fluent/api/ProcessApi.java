package com.activiti.aps.fluent.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.activiti.sdk.client.model.ProcessInstanceRepresentation;
import com.activiti.sdk.client.model.TaskRepresentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessApi {

	private String appName = StringUtils.EMPTY;
	private String initiator = StringUtils.EMPTY;
	private String assignee = StringUtils.EMPTY;

	protected FluentAps fluentAps = null;
	protected Long currentAppDefId = null;
	protected ProcessInstanceRepresentation currentProcessInstance = null;
	
	private Map<String, Object> formValues = new HashMap<String, Object>();
	
	private static final Logger logger = LogManager.getLogger(ProcessApi.class);
		
	public ProcessApi(FluentAps fluentAps, String appName) {
		this.fluentAps = fluentAps;
		this.appName = appName;
	}
	
	public ProcessApi withInitiator(String initiator) {
		this.initiator = initiator;
		return this;
	}
	
	public ProcessApi withAssignee(String assignee) {
		this.assignee = assignee;
		return this;
	}
	
	public ProcessApi withFormValue(String id, String value) {
		formValues.put(id, value);
		return this;
	}
	
	public ProcessApi hasInitiator() {
		if (StringUtils.isEmpty(this.initiator)) {
			logger.error("Process does not have an initiator defined.");
			throw new RuntimeException("Process does not have an initiator defined.");
		}
		return this;
	}
	
	public ProcessApi hasAssignee() {
		if (StringUtils.isEmpty(this.assignee)) {
			logger.error("Process does not have an assignee defined.");
			throw new RuntimeException("Process does not have an assignee defined.");
		}
		return this;
	}
	
	public boolean hasFormValues() {
		if(this.formValues.size()>0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	public ProcessApi withAdminAsInitiatorAndAssignee() {
		this.initiator = FluentAps.ADMIN_USER;
		this.assignee = FluentAps.ADMIN_USER;
		return this;
	}
	
	public ProcessApi withAdminAsInitiator() {
		this.initiator = FluentAps.ADMIN_USER;
		return this;
	}
	
	public ProcessApi withAdminAsAssignee() {
		this.assignee = FluentAps.ADMIN_USER;
		return this;
	}

	public String getAppName() {
		return appName;
	}

	/*
	public String getInitiator() {
		return initiator;
	}

	public String getAssignee() {
		return assignee;
	}
	*/

	public Map<String, Object> getFormValues() {
		return formValues;
	}

	public TaskApi thenSubmitTask(String taskName, String outcome) {
		currentProcessInstance = fluentAps.processUtils.startProcess(this);
		currentAppDefId = fluentAps.appUtils.getAppDefinitionId(this.appName);
		return new TaskApi(this, taskName, outcome);
	}

	/*
	public ProcessInstanceRepresentation getCurrentProcessInstace() {
		return this.currentProcessInstance;
	}

	public TaskRepresentation getCurrentTask() {
		return apsTester.taskUtils.findTaskWithProcessInstanceId(currentProcessInstance.getId(), currentAppDefId).get().get(0);
	}

	public List<TaskRepresentation> getCurrentTasks() {
		return apsTester.taskUtils.findTaskWithProcessInstanceId(currentProcessInstance.getId(), currentAppDefId).get();
	}
	*/

	public ProcessApi thenCheckThatTheProcessIsFinished() {
		Optional<List<TaskRepresentation>> tasks = fluentAps.taskUtils
				.findTaskWithProcessInstanceId(currentProcessInstance.getId(), currentAppDefId);
		if (tasks.isPresent()) {
			List<TaskRepresentation> currentTasks = tasks.get();
			if (currentTasks.size() > 0) {
				logger.error("The process is not finished, tasks active: " + currentTasks);
				throw new RuntimeException("The process is not finished, tasks active: " + currentTasks);
			}
		}
		return this;
	}


	@Override
	public String toString() {
		return "StartProcess [appName=" + appName + ", initiator=" + initiator + ", assignee=" + assignee
				+ ", formValues=" + formValues + "]";
	}
	
}
