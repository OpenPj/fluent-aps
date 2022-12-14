package com.activiti.aps.fluent.client;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.activiti.aps.fluent.api.TaskApi;
import com.activiti.sdk.client.ApiException;
import com.activiti.sdk.client.api.TaskFormsApi;
import com.activiti.sdk.client.api.TasksApi;
import com.activiti.sdk.client.model.CompleteFormRepresentation;
import com.activiti.sdk.client.model.ResultListDataRepresentationTaskRepresentation;
import com.activiti.sdk.client.model.TaskQueryRepresentation;
import com.activiti.sdk.client.model.TaskRepresentation;

public class TaskClient {

	protected TasksApi tasksApi;
	protected TaskFormsApi taskFormsApi;
	
	private static final Logger logger = LogManager.getLogger(TaskClient.class);


	public TaskClient(TasksApi tasksApi, TaskFormsApi taskFormsApi) {
		this.tasksApi = tasksApi;
		this.taskFormsApi = taskFormsApi;
	}

	public Optional<TaskRepresentation> findTaskWithName(String processInstanceId, Long appDefId, String taskName) {
		// Getting the First Approval Task Id
		TaskQueryRepresentation taskQuery = new TaskQueryRepresentation();
		taskQuery.appDefinitionId(appDefId);
		taskQuery.processInstanceId(processInstanceId);

		ResultListDataRepresentationTaskRepresentation tasks = null;
		try {
			tasks = tasksApi.listTasksUsingPOST(taskQuery);
		} catch (ApiException e) {
			logger.error("Error during getting tasks with name: " + taskName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error during getting tasks with name: " + taskName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		List<TaskRepresentation> currentTasks = tasks.getData();
		Iterator<TaskRepresentation> iteratorTasks = currentTasks.iterator();
		TaskRepresentation taskFound = null;
		while (iteratorTasks.hasNext()) {
			TaskRepresentation taskRepresentation = (TaskRepresentation) iteratorTasks.next();
			if (StringUtils.equals(taskName, taskRepresentation.getName())) {
				taskFound = taskRepresentation;
				break;
			}
		}
		logger.info("Found task with name " + taskName+ ": " + taskFound);
		return Optional.of(taskFound);
	}
	
	public Optional<List<TaskRepresentation>> findTaskWithProcessInstanceId(String processInstanceId, Long appDefId) {
		// Getting the First Approval Task Id
		TaskQueryRepresentation taskQuery = new TaskQueryRepresentation();
		taskQuery.appDefinitionId(appDefId);
		taskQuery.processInstanceId(processInstanceId);

		ResultListDataRepresentationTaskRepresentation tasks = null;
		try {
			tasks = tasksApi.listTasksUsingPOST(taskQuery);
		} catch (ApiException e) {
			logger.error("Error during getting tasks with process instance Id: " + processInstanceId + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error during getting tasks with process instance Id: " + processInstanceId + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		List<TaskRepresentation> currentTasks = tasks.getData();
		logger.info("Found tasks for process instance id " + processInstanceId+ ": " + currentTasks);
		return Optional.of(currentTasks);
	}

	public void completeTask(TaskApi task, String taskId) {
		CompleteFormRepresentation form = new CompleteFormRepresentation();
		form.setOutcome(task.getOutcome());
		
		if(task.hasFormValues()) {
			form.setValues(task.getFormValues());
		}
		
		try {
			taskFormsApi.completeTaskFormUsingPOST(taskId, form);
		} catch (ApiException e) {
			logger.error("Error completing task: " + taskId + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error completing task: " + taskId + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		
		logger.info("Task "+taskId+" completed");
	}

}
