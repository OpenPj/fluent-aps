package com.activiti.aps.fluent.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.activiti.sdk.client.ApiException;
import com.activiti.sdk.client.api.AppDefinitionsApi;
import com.activiti.sdk.client.api.RuntimeAppDefinitionsApi;
import com.activiti.sdk.client.api.RuntimeAppDeploymentsApi;
import com.activiti.sdk.client.model.AppDefinitionRepresentation;
import com.activiti.sdk.client.model.ResultListDataRepresentationAppDefinitionRepresentation;

public class AppClient {

	private RuntimeAppDefinitionsApi runtimeAppDefinitionsApi;
	private RuntimeAppDeploymentsApi runtimeAppDeploymentsApi;
	private AppDefinitionsApi appDefinitionsApi;
	
	private static final String APP_CONTEXT = "/activiti-app";
	private static final String ENTERPRISE_API_CONTEXT_PATH = APP_CONTEXT + "/api/enterprise";
	private static final String PUBLISH_APP_ENDPOINT = ENTERPRISE_API_CONTEXT_PATH + "/app-definitions/publish-app";
	private static final String EXPORT_APP_ENDPOINT = ENTERPRISE_API_CONTEXT_PATH + "/export-app-deployment/";
	
	private static final Logger logger = LogManager.getLogger(AppClient.class);

	public AppClient(RuntimeAppDefinitionsApi runtimeAppDefinitionsApi, RuntimeAppDeploymentsApi runtimeAppDeploymentsApi, AppDefinitionsApi appDefinitionsApi) {
		this.runtimeAppDefinitionsApi = runtimeAppDefinitionsApi;
		this.runtimeAppDeploymentsApi = runtimeAppDeploymentsApi;
		this.appDefinitionsApi = appDefinitionsApi;		
	}

	public Long getAppDefinitionId(String appName) {
		ResultListDataRepresentationAppDefinitionRepresentation apps = null;
		Optional<Long> appDefId = null;
		try {
			apps = this.runtimeAppDefinitionsApi.getAppDefinitionsUsingGET();
			List<AppDefinitionRepresentation> appDefs = apps.getData();
			Iterator<AppDefinitionRepresentation> iteratorApps = appDefs.iterator();
			while (iteratorApps.hasNext()) {
				AppDefinitionRepresentation appDefinitionRepresentation = (AppDefinitionRepresentation) iteratorApps
						.next();
				if (StringUtils.equals(appDefinitionRepresentation.getName(), appName)) {
					appDefId = Optional.of(appDefinitionRepresentation.getId());
					break;
				}
			}
			if(appDefId.isEmpty()) {
				logger.error("App definition Id not found. Currently active apps are: "+appDefs);
				throw new RuntimeException("App definition Id not found. Currently active apps are: "+appDefs);
			}
			logger.info("App Definition Id: " + appDefId);
		} catch (ApiException e) {
			logger.error("Error getting app definitionId " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error getting app definitionId " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		return appDefId.get();
	}
	
	public Long getAppModelId(String appName) {
		ResultListDataRepresentationAppDefinitionRepresentation apps = null;
		Optional<Long> appDefId = null;
		try {
			apps = this.runtimeAppDefinitionsApi.getAppDefinitionsUsingGET();
			List<AppDefinitionRepresentation> appDefs = apps.getData();
			Iterator<AppDefinitionRepresentation> iteratorApps = appDefs.iterator();
			while (iteratorApps.hasNext()) {
				AppDefinitionRepresentation appDefinitionRepresentation = (AppDefinitionRepresentation) iteratorApps
						.next();
				if (StringUtils.equals(appDefinitionRepresentation.getName(), appName)) {
					appDefId = Optional.of(appDefinitionRepresentation.getModelId());
					break;
				}
			}
			if(appDefId.isEmpty()) {
				throw new RuntimeException("Model Id not found. Currently active apps are: "+appDefs);
			}
			logger.info("App Definition Id: " + appDefId);
		} catch (ApiException e) {
			logger.error("Error getting app modelId " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error getting app modelId " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		return appDefId.get();
	}
	
	public AppDefinitionRepresentation getAppDef(String appName) {
		ResultListDataRepresentationAppDefinitionRepresentation apps = null;
		Optional<AppDefinitionRepresentation> appDef = null;
		try {
			apps = this.runtimeAppDefinitionsApi.getAppDefinitionsUsingGET();
			List<AppDefinitionRepresentation> appDefs = apps.getData();
			Iterator<AppDefinitionRepresentation> iteratorApps = appDefs.iterator();
			while (iteratorApps.hasNext()) {
				AppDefinitionRepresentation appDefinitionRepresentation = (AppDefinitionRepresentation) iteratorApps
						.next();
				if (StringUtils.equals(appDefinitionRepresentation.getName(), appName)) {
					appDef = Optional.of(appDefinitionRepresentation);
					break;
				}
			}
			if(appDef.isEmpty()) {
				throw new RuntimeException("App Def not found. Currently active apps are: "+appDefs);
			}
			logger.info("App Definition Id: " + appDef);
		} catch (ApiException e) {
			logger.error("Error getting app definition " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error getting app definition " + appName + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
		return appDef.get();
	}

	public void removeApp(AppDefinitionRepresentation appDef) {
		try {
			appDefinitionsApi.deleteAppDefinitionUsingDELETE(appDef.getId());
			runtimeAppDeploymentsApi.deleteAppDeploymentUsingDELETE(Long.valueOf(appDef.getDeploymentId()));
			logger.info("App removed with definition Id: " + appDef.getId());
		} catch (ApiException e) {
			logger.error("Error removing app " + appDef + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
			throw new RuntimeException("Error removing app " + appDef + " | " + e.getMessage() + " | "+e.getResponseBody(), e);
		}
	}
	
	public void importAndPublishApp(String appZipFile, String username, String password, String protocol, String hostname,
			Integer port) {
		Path resourceFourEyesAppZip = Paths.get(appZipFile);
		File file = resourceFourEyesAppZip.toFile();
		try {

			final BasicScheme basicAuth = new BasicScheme();
			basicAuth.initPreemptive(new UsernamePasswordCredentials(username, password.toCharArray()));
			final HttpHost target = new HttpHost(protocol, hostname, port);
			final HttpClientContext localContext = HttpClientContext.create();
			localContext.resetAuthExchange(target, basicAuth);

			try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
				final String publishAppUrl = protocol + "://" + hostname + ":" + port.toString() + PUBLISH_APP_ENDPOINT;
				final HttpPost httppost = new HttpPost(publishAppUrl);

				final FileBody binaryFile = new FileBody(file);
				final HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", binaryFile).build();
				httppost.setEntity(reqEntity);

				logger.info("Importing app " + httppost);
				
				HttpClientResponseHandler<String> responseHandler = new BasicHttpClientResponseHandler();
				final String response = httpclient.execute(httppost, localContext, responseHandler);
					
				System.out.println("----------------------------------------");
				System.out.println(response);
				
				if (StringUtils.isNotEmpty(response)) {
					System.out.println("Response content length: " + response.length());
				}
			}
			logger.info("App "+appZipFile+" correctly imported");
		} catch (FileNotFoundException e) {
			logger.error("App not found " + appZipFile + " | " + e.getMessage(), e);
			throw new RuntimeException("App not found " + appZipFile + " | " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Error importing app " + appZipFile + " | " + e.getMessage(), e);
			throw new RuntimeException("Error importing app definition " + appZipFile + " | " + e.getMessage(), e);
		}
	}
	
	public void exportApp(String deploymentId, String appZipFile, String username, String password, String protocol, String hostname,
			Integer port) {		
		final BasicScheme basicAuth = new BasicScheme();
		basicAuth.initPreemptive(new UsernamePasswordCredentials(username, password.toCharArray()));
		String exportAppUrl = protocol + "://" + hostname + ":" + port.toString() + EXPORT_APP_ENDPOINT + deploymentId;
		
		File exportedApp = new File(appZipFile);
		
		try (final CloseableHttpClient httpclient = HttpClients.custom()
						.setDefaultCredentialsProvider(CredentialsProviderBuilder.create()
                        .add(new HttpHost(hostname, port), username, password.toCharArray())
                        .build())
                .build()) {
            final HttpGet httpget = new HttpGet(exportAppUrl);

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            httpclient.execute(httpget, response -> {
                System.out.println("----------------------------------------");
                System.out.println(httpget + "->" + new StatusLine(response));
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (FileOutputStream outstream = new FileOutputStream(exportedApp)) {
                        entity.writeTo(outstream);
                        logger.info("Exported app " + appZipFile);
                    }
                }
                EntityUtils.consume(response.getEntity());
                return null;
            });
        } catch (IOException e) {
        	logger.error("IO error when exporting deployment id " + deploymentId + " | " + e.getMessage(), e);
			throw new RuntimeException("IO error when exporting deployment id " + deploymentId + " | " + e.getMessage(), e);
		} catch (URISyntaxException e) {
			logger.error("URISyntaxException error when exporting deployment id " + deploymentId + " | " + e.getMessage(), e);
			throw new RuntimeException("URISyntaxException error when exporting deployment id " + deploymentId + " | " + e.getMessage(), e);
		}
	}
	
}
