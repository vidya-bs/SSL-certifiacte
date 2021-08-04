package com.itorix.hyggee.third.party.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan("com.itorix.hyggee")
@SpringBootApplication
public class ThirdPartyIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThirdPartyIntegrationApplication.class, args);
	}
}
