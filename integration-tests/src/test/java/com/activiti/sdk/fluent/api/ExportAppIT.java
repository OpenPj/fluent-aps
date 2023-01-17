package com.activiti.sdk.fluent.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.activiti.aps.fluent.api.FluentAps;
import com.activiti.aps.fluent.api.FluentAps.FluentApsBuilder;

public class ExportAppIT {

	protected FluentAps fluentAps;
	
	@BeforeEach
	public void initApiClient() {
		fluentAps = new FluentApsBuilder("admin@app.activiti.com", "admin").build();
	}
	
	@Test
	@Order(1)
	public void testExportApp() throws URISyntaxException {
		System.out.println("--- /Start - Export Four Eyes App - Integration Test ---");

		fluentAps.afterLoadingAppArchive("../target/apps/Four-Eyes-App.zip");
		
		Path targetPath = Paths.get(getClass().getResource("/").toURI()).getParent();
		String exportedAppURI = targetPath.toString()+"/Four-Eyes-App-"+System.currentTimeMillis()+".zip";
		
		fluentAps.exportAppNamed("4 Eyes Principle", exportedAppURI);
		
		File exportedAppFile = new File(exportedAppURI);
		assertTrue(exportedAppFile.exists());

		System.out.println("--- /End - Export Four Eyes App - Integration Test ---");

	}
	
}
