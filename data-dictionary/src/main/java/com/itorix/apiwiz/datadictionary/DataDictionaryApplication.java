package com.itorix.apiwiz.datadictionary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
public class DataDictionaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataDictionaryApplication.class, args);
	}
}
