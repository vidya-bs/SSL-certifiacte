package com.itorix.consentserver.dto;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;


@Configuration
public class MultiTenantMongoDbConfiguration {

	@Autowired
	private MongoProperties properties;

	@Bean
	public MongoClient createMongoClient() {
		return MongoClients.create(properties.getUri());
	}

	@Primary
	@Bean
	public SimpleMongoClientDatabaseFactory multitenantFactory() {
		return new MultiTenantMongoDbFactory(createMongoClient(), properties.getDatabase());
	}

	@Primary
	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(multitenantFactory());
	}

	@Bean(name = "masterMongoTemplate")
	public MongoTemplate secondaryMongoTemplate() {
		return new MongoTemplate(masterFactory());
	}

	@Bean
	public SimpleMongoClientDatabaseFactory masterFactory() {
		return new SimpleMongoClientDatabaseFactory(createMongoClient(), properties.getDatabase());
	}

}