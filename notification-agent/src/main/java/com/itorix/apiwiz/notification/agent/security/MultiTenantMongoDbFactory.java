package com.itorix.apiwiz.notification.agent.security;

import com.itorix.apiwiz.notification.agent.executor.model.TenantContext;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.StringUtils;

public class MultiTenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {

    public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {

        if (StringUtils.hasText(TenantContext.getCurrentTenant())) {
            return getMongoDatabase(TenantContext.getCurrentTenant());
        }
        return super.getMongoDatabase();
    }
}