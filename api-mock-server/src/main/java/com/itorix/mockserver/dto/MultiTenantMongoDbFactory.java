package com.itorix.mockserver.dto;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

public class MultiTenantMongoDbFactory extends SimpleMongoDbFactory {


	public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

    @Override
    public MongoDatabase getDb() throws DataAccessException {
        if (ServiceRequestContextHolder.getContext().getTenentId() instanceof String) {
            String tenant = ServiceRequestContextHolder.getContext().getTenentId();
            if (tenant != null) {
                return getDb(tenant);
            }
        }
        return super.getDb();
    }
}