package com.itorix.apiwiz.apimonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
public class ApiMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiMonitorApplication.class, args);
	}
}
