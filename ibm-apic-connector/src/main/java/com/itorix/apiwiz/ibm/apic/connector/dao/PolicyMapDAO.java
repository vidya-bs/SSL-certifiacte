package com.itorix.apiwiz.ibm.apic.connector.dao;

import com.itorix.apiwiz.common.model.proxystudio.Policy;
import com.itorix.apiwiz.ibm.apic.connector.model.PolicyMappingItem;
import com.itorix.apiwiz.ibm.apic.connector.model.PolicyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PolicyMapDAO {
	@Autowired
	private MongoTemplate mongoTemplate;

	public void updatePolicyMap(List<Map<String, String>> updatedPolicyMap) {
		updatedPolicyMap.forEach(policyMapping->{
			Query query = Query.query(Criteria.where("ibmPolicyName").is(policyMapping.get("ibmPolicyName")));
			Update update = new Update().set("apigeePolicyName", policyMapping.get("apigeePolicyName"));
			mongoTemplate.upsert(query, update, PolicyMappingItem.class);
		});
	}
	public List<String> getApigeePolicies(String searchKey) {
		List<String> policyNames = new ArrayList<>();

		List<PolicyTemplate> templates = mongoTemplate.findAll(PolicyTemplate.class);
		List<String> tempPolicyNames = policyNames;
		templates.forEach(template->{
			template.getPolicies().forEach(policy -> {
				tempPolicyNames.add(policy.getName());
			});
		});

		if(searchKey != null){
			policyNames = tempPolicyNames.stream().filter(x->x.contains(searchKey)).collect(Collectors.toList());
		}

		Collections.sort(policyNames);
		return policyNames;
	}
}
