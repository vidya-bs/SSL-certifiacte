package com.itorix.apiwiz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class LicenseApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LicenseApiApplication.class, args);
		log.info("License API started successfully.");
	}


}
