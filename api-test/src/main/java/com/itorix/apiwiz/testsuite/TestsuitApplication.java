package com.itorix.apiwiz.testsuite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
public class TestsuitApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestsuitApplication.class, args);
	}
}
