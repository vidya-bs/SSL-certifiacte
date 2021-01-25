package com.itorix.apiwiz.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.web.config.SpringDataWebConfiguration;;


//@EnableAutoConfiguration(exclude = VelocityAutoConfiguration.class)
//@EnableAutoConfiguration
@ComponentScan("com.itorix.apiwiz")
@SpringBootApplication
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class Application {

	public static void main(String[] args) {
		try{
			SpringApplication.run(Application.class, args);
		}catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}