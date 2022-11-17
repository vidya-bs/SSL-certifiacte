package com.itorix.apiwiz.identitymanagement.security;

import java.net.UnknownHostException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MultitenantMongoDbConfiguration {

	@Autowired
	private MongoProperties properties;

	@Autowired
	private Environment environment;

	@Autowired
	private MongoProperties mongoProperties;

	@Bean
	public MongoClient createMongoClient() throws UnknownHostException {
		return MongoClients.create(properties.getUri());
	}

	@Primary
	@Bean
	public SimpleMongoClientDatabaseFactory multitenantFactory() throws UnknownHostException {
		return new MultitenantMongoDbFactory(createMongoClient(), mongoProperties.getDatabase());
	}

	@Primary
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(multitenantFactory());
	}

	@Bean(name = "masterMongoTemplate")
	public MongoTemplate secondaryMongoTemplate() throws Exception {
		return new MongoTemplate(masterFactory());
	}

	@Bean
	public SimpleMongoClientDatabaseFactory masterFactory() throws Exception {
		return new SimpleMongoClientDatabaseFactory(createMongoClient(), mongoProperties.getDatabase());
	}
}
