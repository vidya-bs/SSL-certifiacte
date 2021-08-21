package com.itorix.mockserver.dto;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
public class MultiTenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {


	public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

    @Override
	public MongoDatabase getMongoDatabase() {
		if (ServiceRequestContextHolder.getContext().getTenentId() instanceof String) {
			String tenant = ServiceRequestContextHolder.getContext().getTenentId();
			if (tenant != null) {
				return getMongoDatabase(tenant);
			}
		}
		return super.getMongoDatabase();
	}
}