package com.itorix.apiwiz.test.security;

import com.itorix.apiwiz.test.executor.model.TenantContext;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

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