package com.itorix.apiwiz.devportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
public class DevportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevportalApplication.class, args);
	}
}
