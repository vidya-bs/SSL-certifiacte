package com.itorix.apiwiz.validator.license.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class MultiTenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {


	public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

    @Override
	public MongoDatabase getMongoDatabase() {
		return super.getMongoDatabase();
	}
}