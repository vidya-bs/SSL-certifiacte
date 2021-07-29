package com.itorix.mockserver.dto;

import java.net.UnknownHostException;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;

@Configuration
public class MultiTenantMongoDbConfiguration {

	@Autowired
	private MongoProperties properties;

	@Bean
	public MongoClient createMongoClient() throws UnknownHostException {
		return new MongoClient(new MongoClientURI(properties.getUri(), mongoClientOptions()));
	}

	@Primary
	@Bean
	public MongoDbFactory multitenantFactory() throws UnknownHostException {
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
	public MongoDbFactory masterFactory() throws Exception {
		return new SimpleMongoDbFactory(createMongoClient(), properties.getDatabase());
	}

	@Bean
	public MongoClientOptions.Builder mongoClientOptions() {
		return MongoClientOptions.builder()
				.socketKeepAlive(true);
	}

}