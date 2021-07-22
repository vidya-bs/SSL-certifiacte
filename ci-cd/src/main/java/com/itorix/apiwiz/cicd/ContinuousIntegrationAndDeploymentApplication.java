package com.itorix.apiwiz.cicd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.itorix.apiwiz")
public class ContinuousIntegrationAndDeploymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContinuousIntegrationAndDeploymentApplication.class, args);
	}
}
