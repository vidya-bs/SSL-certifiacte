package com.itorix.mockserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({"com.itorix.mockserver", "com.itorix.apiwiz.validator"})
@EnableScheduling
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class MockServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockServerApplication.class, args);
    }

}