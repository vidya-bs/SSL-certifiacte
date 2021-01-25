package com.itorix.apiwiz.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@EnableAutoConfiguration
@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = false)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
