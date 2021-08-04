package com.itorix.apiwiz.app;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public GroupedOpenApi swaggerApi() {
		return GroupedOpenApi.builder().group("swagger").pathsToMatch("/v1/swaggers/**").build();
	}

	@Bean
	public GroupedOpenApi userApi() {
		return GroupedOpenApi.builder().group("user").pathsToMatch("/v1/users/**").build();
	}

	@Bean
	public GroupedOpenApi portalApi() {
		return GroupedOpenApi.builder().group("portal").pathsToMatch("/v1/organizations/**").build();
	}

	@Bean
	public GroupedOpenApi testSuiteApi() {
		return GroupedOpenApi.builder().group("testSuite").pathsToMatch("/v1/testsuites/**").build();
	}

	@Bean
	public GroupedOpenApi monitorApi() {
		return GroupedOpenApi.builder().group("monitor").pathsToMatch("/v1/monitor/**").build();
	}

	@Bean
	public GroupedOpenApi pipelinesApi() {
		return GroupedOpenApi.builder().group("pipelines").pathsToMatch("/v1/pipelines/**").build();
	}

	@Bean
	public GroupedOpenApi packagesApi() {
		return GroupedOpenApi.builder().group("pipelinepackages").pathsToMatch("/v1/packages/**").build();
	}

	@Bean
	public GroupedOpenApi virtualizationApi() {
		return GroupedOpenApi.builder().group("virtualization").pathsToMatch("/v1/mock/**").build();
	}

	@Bean
	public GroupedOpenApi portfolioApi() {
		return GroupedOpenApi.builder().group("portfolio").pathsToMatch("/v1/portfolios/**").build();
	}

	@Bean
	public GroupedOpenApi configApi() {
		return GroupedOpenApi.builder().group("config").pathsToMatch("/v1/config/**").build();
	}

	@Bean
	public GroupedOpenApi dataDictionaryApi() {
		return GroupedOpenApi.builder().group("dataDictionary").pathsToMatch("/v1/data-dictionary/**").build();
	}

	@Bean
	public GroupedOpenApi proxyApi() {
		return GroupedOpenApi.builder().group("proxyStudio").pathsToMatch("/v1/buildconfig/**").build();
	}

	@Bean
	public GroupedOpenApi serviceRegistryApi() {
		return GroupedOpenApi.builder().group("serviceRegistry").pathsToMatch("/v1/service-registry/**").build();
	}
}
