package com.itorix.apiwiz.design.studio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
public class DesignStudioApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesignStudioApplication.class, args);
	}
}
