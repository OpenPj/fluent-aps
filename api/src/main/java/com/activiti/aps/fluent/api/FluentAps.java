package com.activiti.aps.fluent.api;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.activiti.aps.fluent.client.AppClient;
import com.activiti.aps.fluent.client.ProcessClient;
import com.activiti.aps.fluent.client.TaskClient;
import com.activiti.sdk.client.ApiClient;
import com.activiti.sdk.client.api.AboutApi;
import com.activiti.sdk.client.api.AppDefinitionsApi;
import com.activiti.sdk.client.api.ProcessDefinitionsApi;
import com.activiti.sdk.client.api.ProcessInstancesApi;
import com.activiti.sdk.client.api.RuntimeAppDefinitionsApi;
import com.activiti.sdk.client.api.RuntimeAppDeploymentsApi;
import com.activiti.sdk.client.api.TaskFormsApi;
import com.activiti.sdk.client.api.TasksApi;
import com.activiti.sdk.client.model.AppDefinitionRepresentation;
import com.activiti.sdk.client.model.TaskRepresentation;

public class FluentAps {

	public static final String ADMIN_USER = "admin@app.activiti.com";

	private final String username;
	private final String password;

	private final String proto;
	private final String host;
	private final int port;

	ApiClient apiClient;
	AboutApi aboutApi;
	AppDefinitionsApi appDefinitionsApi;
	RuntimeAppDefinitionsApi runtimeAppDefinitionsApi;
	RuntimeAppDeploymentsApi runtimeAppDeploymentsApi;
	ProcessDefinitionsApi processDefinitionsApi;
	ProcessInstancesApi processInstancesApi;
	TasksApi tasksApi;
	TaskFormsApi taskFormsApi;

	AppClient appUtils;
	ProcessClient processUtils;
	TaskClient taskUtils;

	protected AppDefinitionRepresentation currentAppDef = null;
	
	private static final Logger logger = LogManager.getLogger(FluentAps.class);


	public FluentAps(FluentApsBuilder builder) {
		this.username = builder.username;
		this.password = builder.password;
		this.proto = builder.proto;
		this.host = builder.host;
		this.port = builder.port;

		apiClient = new ApiClient();
		apiClient.setBasePath(this.proto + "://" + this.host + ":" + this.port);
		apiClient.setUsername(this.username);
		apiClient.setPassword(this.password);

		aboutApi = new AboutApi(apiClient);
		appDefinitionsApi = new AppDefinitionsApi(apiClient);
		runtimeAppDefinitionsApi = new RuntimeAppDefinitionsApi(apiClient);
		processDefinitionsApi = new ProcessDefinitionsApi(apiClient);
		processInstancesApi = new ProcessInstancesApi(apiClient);
		tasksApi = new TasksApi(apiClient);
		taskFormsApi = new TaskFormsApi(apiClient);

		appUtils = new AppClient(runtimeAppDefinitionsApi, runtimeAppDeploymentsApi, appDefinitionsApi);
		processUtils = new ProcessClient(processDefinitionsApi, processInstancesApi, appUtils);
		taskUtils = new TaskClient(tasksApi, taskFormsApi);
	}

	public FluentAps afterLoadingAppArchive(String appZipFile) {
		appUtils.importAndPublishApp(appZipFile, this.username, this.password, this.proto, this.host, this.port);
		return this;
	}

	public FluentAps removeAppNamed(String appName) {
		currentAppDef = appUtils.getAppDef(appName);
		appUtils.removeApp(currentAppDef);
		return this;
	}
	
	public ProcessApi startProcessForApp(String appName) {
		// TODO: fail() if app name does not exist.
		return new ProcessApi(this, appName);
	}

	void thenSubmitTask(TaskApi task) {
		/* Not needed, required arguments moved to constructor
		if (StringUtils.isEmpty(task.getName())) {
			fail("Cannot submit task with no name: " + task);
		}
		if (StringUtils.isEmpty(task.getOutcome())) {
			fail("Cannot submit task with no outcome: " + task);
		}
		*/

		// TODO: check that all required form fields have been provided.

		ProcessApi process = task.processApi;
		Optional<TaskRepresentation> currentTask = taskUtils.findTaskWithName(process.currentProcessInstance.getId(),
				process.currentAppDefId, task.getName());

		if (currentTask.isEmpty()) {
			Optional<List<TaskRepresentation>> currentTasks = taskUtils
					.findTaskWithProcessInstanceId(process.currentProcessInstance.getId(), process.currentAppDefId);
			logger.error("Task not found: " + task + " current active tasks are: " + currentTasks);
			throw new RuntimeException("Task not found: " + task + " current active tasks are: " + currentTasks);
		}

		taskUtils.completeTask(task, currentTask.get().getId());
	}


	/*
	public APSTester reset() {
		currentProcessInstance = null;
		currentAppDefId = null;
		return this;
	}
	*/

	public static class FluentApsBuilder {

		private final String username;
		private final String password;

		private static final String DEFAULT_HTTP_PROTOCOL = "http";
		private static final String DEFAULT_HOST = "localhost";
		private static final int DEFAULT_PORT = 8080;

		private String proto = DEFAULT_HTTP_PROTOCOL;
		private String host = DEFAULT_HOST;
		private int port = DEFAULT_PORT;

		public FluentApsBuilder(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public FluentApsBuilder proto(String proto) {
			this.proto = proto;
			return this;
		}

		public FluentApsBuilder host(String host) {
			this.host = host;
			return this;
		}

		public FluentApsBuilder port(int port) {
			this.port = port;
			return this;
		}

		public FluentAps build() {
			FluentAps tester = new FluentAps(this);
			validateAPSTester(tester);
			return tester;
		}

		private void validateAPSTester(FluentAps fluentAps) {
			if (StringUtils.isEmpty(fluentAps.username) || StringUtils.isEmpty(fluentAps.password)) {
				logger.error("Username and password must be not null");
				throw new RuntimeException("Username and password must be not null");
			}
		}

	}

}
