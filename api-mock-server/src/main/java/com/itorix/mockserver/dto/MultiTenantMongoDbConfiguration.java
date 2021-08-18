package com.itorix.mockserver.dto;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.net.UnknownHostException;



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
	public SimpleMongoClientDatabaseFactory multitenantFactory() throws UnknownHostException {
		return new MultiTenantMongoDbFactory(createMongoClient(), properties.getDatabase());
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
		return new SimpleMongoClientDatabaseFactory(createMongoClient(), properties.getDatabase());
	}

}