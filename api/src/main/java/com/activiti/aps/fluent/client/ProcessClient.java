package com.activiti.aps.fluent.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.activiti.aps.fluent.api.ProcessApi;
import com.activiti.sdk.client.ApiException;
import com.activiti.sdk.client.api.ProcessDefinitionsApi;
import com.activiti.sdk.client.api.ProcessInstancesApi;
import com.activiti.sdk.client.model.CreateProcessInstanceRepresentation;
import com.activiti.sdk.client.model.ProcessDefinitionRepresentation;
import com.activiti.sdk.client.model.ProcessInstanceRepresentation;
import com.activiti.sdk.client.model.RestVariable;
import com.activiti.sdk.client.model.ResultListDataRepresentationProcessDefinitionRepresentation;

public class ProcessClient {

	private ProcessDefinitionsApi processDefinitionsApi;
	private ProcessInstancesApi processInstancesApi;
	private AppClient appUtils;
	
	private static final Logger logger = LogManager.getLogger(ProcessClient.class);


	public ProcessClient(ProcessDefinitionsApi processDefinitionsApi, ProcessInstancesApi processInstancesApi,
			AppClient appUtils) {
		this.processDefinitionsApi = processDefinitionsApi;
		this.processInstancesApi = processInstancesApi;
		this.appUtils = appUtils;
	}

	public ProcessDefinitionRepresentation getProcessDefinition(Long appDefinitionId) {
		ResultListDataRepresentationProcessDefinitionRepresentation process = null;
		try {
			process = processDefinitionsApi.getProcessDefinitionsUsingGET(Boolean.TRUE, appDefinitionId, null);
		} catch (ApiException e) {
			throw new RuntimeException(e.getResponseBody(), e);
		}

		List<ProcessDefinitionRepresentation> processDefs = process.getData();
		Iterator<ProcessDefinitionRepresentation> iteratorProcesses = processDefs.iterator();
		ProcessDefinitionRepresentation currentProcessDefinition = null;
		while (iteratorProcesses.hasNext()) {
			ProcessDefinitionRepresentation processDefinitionRepresentation = (ProcessDefinitionRepresentation) iteratorProcesses
					.next();
			currentProcessDefinition = processDefinitionRepresentation;
		}
		logger.info("Process Definition Id: " + currentProcessDefinition.getId());
		return currentProcessDefinition;
	}

	public ProcessInstanceRepresentation startProcess(ProcessApi process) {
		ProcessInstanceRepresentation currentProcessInstance = null;
		String appName = process.getAppName();
		if (StringUtils.isNotEmpty(appName)) {
			Long appDefId = appUtils.getAppDefinitionId(appName);
			ProcessDefinitionRepresentation processDef = getProcessDefinition(appDefId);
			String processKey = processDef.getKey();
			List<RestVariable> restVars = buildRestVars(process.getFormValues());

			// Creating a new workflow instance
			CreateProcessInstanceRepresentation newProcess = new CreateProcessInstanceRepresentation();
			newProcess.setProcessDefinitionKey(processKey);
			if (process.hasFormValues()) {
				newProcess.setVariables(restVars);
			}

			try {
				currentProcessInstance = processInstancesApi.startNewProcessInstanceUsingPOST(newProcess);
			} catch (ApiException e) {
				logger.error("Error during creating a new process instance: " + process + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
				throw new RuntimeException("Error removing app " + process + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			}
			logger.info("Process instance created: "+currentProcessInstance + "created");
		} else {
			logger.error("Can't start the process with no appName: " + process);
			throw new RuntimeException("Can't start the process with no appName: " + process);
		}
		return currentProcessInstance;

	}

	/**
	 * TODO manage all the types of variables: string, long, integer, date, @see org.activiti.engine.impl.variable.VariableType
	 */
	private List<RestVariable> buildRestVars(Map<String, Object> formValues) {
		List<RestVariable> restVars = new ArrayList<RestVariable>();
		Iterator<Entry<String, Object>> formValuesIterator = formValues.entrySet().iterator();
		while (formValuesIterator.hasNext()) {
			Map.Entry<String, Object> formValue = (Map.Entry<String, Object>) formValuesIterator.next();
			RestVariable currentFormRestVar = new RestVariable();
			currentFormRestVar.setName(formValue.getKey());
			currentFormRestVar.setValue(formValue.getValue());
			currentFormRestVar.setType("string");
			restVars.add(currentFormRestVar);
		}
		return restVars;
	}

}
