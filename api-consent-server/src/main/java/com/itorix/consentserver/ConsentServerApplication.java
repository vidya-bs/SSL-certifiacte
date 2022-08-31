package com.itorix.consentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({"com.itorix.consentserver", "com.itorix.apiwiz.validator"})
@EnableScheduling
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class ConsentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsentServerApplication.class, args);
    }

}