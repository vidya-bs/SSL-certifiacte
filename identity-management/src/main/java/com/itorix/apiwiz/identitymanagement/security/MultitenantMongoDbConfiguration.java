package com.itorix.apiwiz.identitymanagement.security;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;


@Configuration
public class MultitenantMongoDbConfiguration {

	@Autowired(required = false)
	private MongoClientOptions options;

	@Autowired
	private Environment environment;

	@Autowired
	private MongoProperties mongoProperties;

	@Bean
	public MongoClient createMongoClient() throws UnknownHostException {
		return new MongoClient(new MongoClientURI(mongoProperties.getUri()));
		//return mongoProperties.createMongoClient(options, environment);
	}

	@Primary
	@Bean
	public MongoDbFactory multitenantFactory() throws UnknownHostException {
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
	public MongoDbFactory masterFactory() throws Exception {
		return new SimpleMongoDbFactory(createMongoClient(), mongoProperties.getDatabase());
	}

}