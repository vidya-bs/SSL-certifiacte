package com.itorix.apiwiz.common.model.integrations.workspace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

@Component
public class WorkspaceIntegrationUtils {

	@Autowired
	public MongoTemplate mongoTemplate;

	public String getBuildScmProp(String key) {
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("propertyKey").is(key));
			WorkspaceIntegration integration = mongoTemplate.findOne(query, WorkspaceIntegration.class);
			if (integration != null) {
				RSAEncryption rSAEncryption;
				rSAEncryption = new RSAEncryption();
				return integration.getEncrypt()
						? rSAEncryption.decryptText(integration.getPropertyValue())
						: integration.getPropertyValue();
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}
