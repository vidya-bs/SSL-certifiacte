package com.itorix.apiwiz.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("com.itorix.apiwiz.test")
@EnableScheduling
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class TestSuiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestSuiteApplication.class, args);
	}
}