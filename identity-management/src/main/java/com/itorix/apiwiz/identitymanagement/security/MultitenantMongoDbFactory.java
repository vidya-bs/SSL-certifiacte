package com.itorix.apiwiz.identitymanagement.security;

import com.mongodb.client.MongoClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.client.MongoDatabase;

public class MultitenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {
	// private MongoClient mongoClient;

	public MultitenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

	@Override
	public MongoDatabase getMongoDatabase() {
		if (ServiceRequestContextHolder.getContext().getUserSessionToken() instanceof UserSession) {
			UserSession authentication = ServiceRequestContextHolder.getContext().getUserSessionToken();
			if (authentication != null && StringUtils.hasText(authentication.getTenant())) {
				return getMongoDatabase(authentication.getTenant());
			}
		}

		if (StringUtils.hasText(TenantContext.getCurrentTenant())) {
			return getMongoDatabase(TenantContext.getCurrentTenant());
		}

		return super.getMongoDatabase();
	}
}
