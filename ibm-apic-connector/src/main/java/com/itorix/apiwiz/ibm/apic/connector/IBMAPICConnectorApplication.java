package com.itorix.apiwiz.ibm.apic.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@ComponentScan({"com.itorix.apiwiz","com.itorix.apiwiz.ibm.apic.connector"})
@SpringBootApplication
public class IBMAPICConnectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(IBMAPICConnectorApplication.class, args);
	}
}
