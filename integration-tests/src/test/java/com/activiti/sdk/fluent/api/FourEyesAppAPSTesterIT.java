package com.activiti.sdk.fluent.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.activiti.aps.fluent.api.FluentAps;
import com.activiti.aps.fluent.api.FluentAps.FluentApsBuilder;


public class FourEyesAppAPSTesterIT {
	
	protected FluentAps fluentAps;

	@BeforeEach
	public void initApiClient() {
		fluentAps = new FluentApsBuilder("admin@app.activiti.com", "admin").build();
	}

	@Test
	@Order(1)
	public void testFourEyesApp() {
		System.out.println("--- /Start - Four Eyes App - Integration Test ---");

		// Importing the Four Eyes App in APS
		// NOTE: test flows, but that methods available in each instance
		// make sense
		fluentAps
			.afterLoadingAppArchive("../target/apps/Four-Eyes-App.zip")
			.startProcessForApp("4 Eyes Principle")
			.withFormValue("reviewtitle", "Review from APS SDK")
			.withFormValue("description", "Description from APS SDK")
			.thenSubmitTask("First approval", "submit")
			.withFormValue("reviewtitle", "Review from APS SDK")
			.withFormValue("description", "Description from APS SDK")
			.thenSubmitTask("Second approval", "submit")
			.withFormValue("reviewtitle", "Review from APS SDK")
			.withFormValue("description", "Description from APS SDK")
			.thenCheckThatTheProcessIsFinished();

		System.out.println("--- /End - Four Eyes App - Integration Test ---");

	}
}