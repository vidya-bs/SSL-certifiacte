package com.itorix.apiwiz.sso.configuration;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MultiTenantMongoDbFactory extends SimpleMongoDbFactory {

	public MultiTenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

	@Override
	public DB getDb() throws DataAccessException {

		if (StringUtils.hasText(TenantContext.getCurrentTenant())) {
			return getDb(TenantContext.getCurrentTenant());
		}
		return super.getDb();
	}
}