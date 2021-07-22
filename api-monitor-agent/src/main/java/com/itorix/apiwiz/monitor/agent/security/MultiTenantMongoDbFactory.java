package com.itorix.apiwiz.monitor.agent.security;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.monitor.agent.executor.model.TenantContext;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MultiTenantMongoDbFactory extends SimpleMongoDbFactory {

    public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    public MongoDatabase getDb() throws DataAccessException {

        if (StringUtils.hasText(TenantContext.getCurrentTenant())) {
            return getDb(TenantContext.getCurrentTenant());
        }
        return super.getDb();
    }
}