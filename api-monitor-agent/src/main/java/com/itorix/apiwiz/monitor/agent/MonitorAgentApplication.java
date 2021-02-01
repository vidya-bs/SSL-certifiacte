package com.itorix.apiwiz.monitor.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("com.itorix.apiwiz")
@EnableScheduling
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class MonitorAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitorAgentApplication.class, args);
	}
}