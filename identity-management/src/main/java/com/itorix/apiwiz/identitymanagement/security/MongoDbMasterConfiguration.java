package com.itorix.apiwiz.identitymanagement.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "mongo.config")
@Getter
@Setter
public class MongoDbMasterConfiguration {

//	private String host;
//	private int port;
//	private String userName;
//	private String password;
//	private String database;
}
