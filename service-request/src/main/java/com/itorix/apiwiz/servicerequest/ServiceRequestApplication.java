package com.itorix.apiwiz.servicerequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({@PropertySource("classpath:application.properties")})
@ComponentScan("com.itorix.hyggee")
public class ServiceRequestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRequestApplication.class, args);
	}
}
