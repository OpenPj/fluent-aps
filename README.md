# Fluent APS 0.0.1-SNAPSHOT

Fluent APS is a Fluent API for Alfresco Process Services.
This project consists of the following Maven modules:

 * APS Fluent API JAR (`api`): this is the client API implementation based on the [`aps-2x-client`](https://github.com/OpenPj/aps-2x-client)
 * Docker Runner (`docker`): this starts your APS platform container with Postgres database and ElasticSearch server
 * Integration Tests (`integration-tests`): this is the integration tests module

## Capabilities

This API allows you to interact and write your tests using a Fluent API against your APS instance.
The main goal is to have a strong way to demonstrate or improve the overall project quality using this API for implementing tests.
You can implement tests for your workflows following the `integration-tests` module, below the example taken from the test class `FourEyesAppAPSTesterIT.java`:

```java
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
```

## Quickstart for using APS Fluent API in your Maven project
To use it in any Maven project add the following snippet in your `pom.xml`:

```xml
<repositories>
	<repository>
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
	</repository>
</repositories>
```

Then add the following dependency:

```xml
<dependency>
	<groupId>com.activiti.sdk</groupId>
	<artifactId>aps-fluent-api</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

A full example is provided in the `integration-tests` module of this project.

## Quickstart for using this Maven project

### Prerequisites
 * OpenJDK 11
 * Apache Maven 3.8.6
 * Docker
 * Put valid  _activiti.lic_  and  _transform.lic_  (or  _Aspose.Total.Java.lic_  )  in the `/license` folder for running unit / integration tests 
 * Set the `docker.aps.image` property in order to run the project on your own APS Docker image built with [APS SDK 2.x](https://github.com/OpenPj/alfresco-process-services-project-sdk/tree/2.x)
Full Maven lifecycle command:

 * `mvn clean install docker:build docker:start`
 
Stop all the Docker containers with:
 
 * `mvn docker:stop`

# Enterprise support
Official maintenance and support of this project is delivered by Zia Consulting
