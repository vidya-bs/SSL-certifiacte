package com.itorix.apiwiz.configmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.itorix.apiwiz")
public class ConfigManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigManagementApplication.class, args);
	}
}
