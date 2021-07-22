package com.itorix.apiwiz.identitymanagement.security;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MultitenantMongoDbFactory extends SimpleMongoDbFactory {
	// private MongoClient mongoClient;

	public MultitenantMongoDbFactory(MongoClient mongoClient, String databaseName) {
		super(mongoClient, databaseName);
	}

	@Override
	public MongoDatabase getDb() throws DataAccessException {
		if (ServiceRequestContextHolder.getContext().getUserSessionToken() instanceof UserSession) {
			UserSession authentication = ServiceRequestContextHolder.getContext().getUserSessionToken();
			if (authentication != null && StringUtils.hasText(authentication.getTenant())) {
				return getDb(authentication.getTenant());
			}
		}

		if (StringUtils.hasText(TenantContext.getCurrentTenant())) {
			return getDb(TenantContext.getCurrentTenant());
		}

		return super.getDb();
	}
}
