package com.itorix.apiwiz.data.management.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.data.management.model.PolicyMapping;
import com.itorix.apiwiz.data.management.model.PolicyMappings;

@Component
public class PolicyMappingDAO {

	@Autowired
	private MongoTemplate mongoTemplate;

	public PolicyMappings savePolicyMapping(PolicyMappings policyMappings) {
		for (PolicyMapping policyMapping : policyMappings.getPolicyMapping()) {
			Query query = new Query(Criteria.where("name").is(policyMapping.getName()));
			Update update = new Update();
			update.set("value", policyMapping.getValue());
			mongoTemplate.upsert(query, update, PolicyMapping.class);
		}
		return getPolicyMappings();
	}

	public PolicyMappings getPolicyMappings() {
		List<PolicyMapping> mappings = mongoTemplate.findAll(PolicyMapping.class);
		PolicyMappings policyMappings = new PolicyMappings();
		if (mappings != null)
			policyMappings.setPolicyMapping(mappings);
		return policyMappings;
	}

	public PolicyMapping getPolicyMapping(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		PolicyMapping mapping = mongoTemplate.findOne(query, PolicyMapping.class);
		return mapping;
	}
}
