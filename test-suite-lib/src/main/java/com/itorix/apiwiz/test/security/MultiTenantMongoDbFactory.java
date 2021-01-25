package com.itorix.apiwiz.test.security;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.test.executor.model.TenantContext;
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