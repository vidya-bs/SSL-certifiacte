package com.itorix.mockserver.dto;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MultitenantMongoDbFactory extends SimpleMongoDbFactory {

    public MultitenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    public DB getDb() throws DataAccessException {
        if (ServiceRequestContextHolder.getContext().getTenentId() instanceof String) {
            String tenant = ServiceRequestContextHolder.getContext().getTenentId();
            if (tenant != null) {
                return getDb(tenant);
            }
        }
        return super.getDb();
    }
}