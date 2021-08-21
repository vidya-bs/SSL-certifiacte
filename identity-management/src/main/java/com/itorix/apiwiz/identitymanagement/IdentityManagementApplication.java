package com.itorix.apiwiz.identitymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class IdentityManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityManagementApplication.class, args);
	}
}
