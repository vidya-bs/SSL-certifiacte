package com.itorix.apiwiz.virtualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@ComponentScan("com.itorix.hyggee")
@SpringBootApplication
public class VirtualizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualizationApplication.class, args);
	}
}
