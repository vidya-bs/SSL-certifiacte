package com.itorix.apiwiz.ibm.apic.connector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.designstudio.Swagger3VO;
import com.itorix.apiwiz.common.model.designstudio.SwaggerVO;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.ibm.apic.connector.model.APIDropdownListItem;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardResponse;
import com.itorix.apiwiz.ibm.apic.connector.model.PolicyMappingItem;
import com.itorix.apiwiz.ibm.apic.connector.serviceImpl.ConnectorCardServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class IBMAPICSpecUtil {

	private static final Logger logger = LoggerFactory.getLogger(IBMAPICSpecUtil.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RSAEncryption rsaEncryption;

	@Autowired
	private RestTemplate restTemplate;



	public List<APIDropdownListItem> getAPIDropdownList(String connectorId){
		Query oasWithIbmConfigsQuery = new Query(Criteria.where("swagger").regex(connectorId));
		List<APIDropdownListItem> oasWithIbmConfigs = new ArrayList<>();

		List<Swagger3VO> oas3IbmSpecs = mongoTemplate.find(oasWithIbmConfigsQuery,Swagger3VO.class);
		List<SwaggerVO> oas2IbmSpecs = mongoTemplate.find(oasWithIbmConfigsQuery,SwaggerVO.class);

		oas3IbmSpecs.forEach(oas3Spec ->{
			APIDropdownListItem item = new APIDropdownListItem();
			item.setSwaggerId(oas3Spec.getSwaggerId());
			item.setRevision(oas3Spec.getRevision());
			item.setOasType("3.0");
			item.setName(oas3Spec.getName());
			oasWithIbmConfigs.add(item);
		});

		oas2IbmSpecs.forEach(oas2Spec ->{
			APIDropdownListItem item = new APIDropdownListItem();
			item.setSwaggerId(oas2Spec.getSwaggerId());
			item.setRevision(oas2Spec.getRevision());
			item.setOasType("2.0");
			item.setName(oas2Spec.getName());
			oasWithIbmConfigs.add(item);
		});

		Collections.sort(oasWithIbmConfigs, Comparator.comparing(APIDropdownListItem::getName));
		return oasWithIbmConfigs;
	}
	public Object fetchPolicyMapForSelectedAPIs(List<APIDropdownListItem> selectedAPIs,int pageSize, int offset,String connectorId) {
		HashSet<String> policyNameFilter = new HashSet<>();
		selectedAPIs.forEach(api->{
			try{
				List<String> ibmPolicyNames = getIbmPolicyNames(api.getSwaggerId(),api.getRevision(), api.getOasType(),connectorId);
				policyNameFilter.addAll(ibmPolicyNames);
			}catch (Exception ex){
				logger.error("Error fetching policy names:" + ex.getMessage());
			}
		});

		Query query = new Query(Criteria.where("ibmPolicyName").in(policyNameFilter));
		query.addCriteria(Criteria.where("connectorId").is(connectorId));
		Long total = mongoTemplate.count(query, PolicyMappingItem.class);

		Query paginatedQuery = new Query().with(Sort.by(Sort.Direction.ASC, "ibmPolicyName"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
		paginatedQuery.addCriteria(Criteria.where("ibmPolicyName").in(policyNameFilter));
		paginatedQuery.addCriteria(Criteria.where("connectorId").is(connectorId));

		List<PolicyMappingItem> policies = mongoTemplate.find(paginatedQuery,PolicyMappingItem.class);
		List<Map<String,String>> filteredPolicies = new ArrayList<>();
		policies.forEach(policy->{
			Map<String,String> policyMapping = new HashMap<>();
			policyMapping.put("ibmPolicyName", policy.getIbmPolicyName());
			policyMapping.put("apigeePolicyName", policy.getApigeePolicyName());
			filteredPolicies.add(policyMapping);
		});

		Map<String,Object> paginatedResponse = new HashMap<>();
		Map<String,Object> pagination = new HashMap<>();
		pagination.put("total",total);
		pagination.put("pageSize",pageSize);
		pagination.put("offset",offset);
		paginatedResponse.put("pagination",pagination);
		paginatedResponse.put("data",filteredPolicies);

		return paginatedResponse;
	}

	public List<String> getIbmPolicyNames(String swaggerId,Integer revision,String oasType,String connectorId)
			throws JsonProcessingException {
		List<String> ibmPolicyNames = new ArrayList<>();
		if(oasType.equalsIgnoreCase("2.0")){
			Query oas2Query = new Query(Criteria.where("swaggerId").is(swaggerId));
			oas2Query.addCriteria(Criteria.where("revision").is(revision));
			SwaggerVO vo = mongoTemplate.findOne(oas2Query,SwaggerVO.class);

			ibmPolicyNames.addAll(getIbmPolicyNamesFromJson(vo.getSwagger()));
			ibmPolicyNames.forEach(ibmPolicyName->{
				Query policyQuery = new Query(Criteria.where("ibmPolicyName").is(ibmPolicyName));
				policyQuery.addCriteria(Criteria.where("connectorId").is(connectorId));
				Long count = mongoTemplate.count(policyQuery,PolicyMappingItem.class);
				if(count == 0){
					PolicyMappingItem policyMappingItem = new PolicyMappingItem();
					policyMappingItem.setConnectorId(connectorId);
					policyMappingItem.setIbmPolicyName(ibmPolicyName);
					policyMappingItem.setApigeePolicyName("");
					mongoTemplate.save(policyMappingItem);
				}
			});

		}else{
			Query oas3Query = new Query(Criteria.where("swaggerId").is(swaggerId));
			oas3Query.addCriteria(Criteria.where("revision").is(revision));
			Swagger3VO vo = mongoTemplate.findOne(oas3Query,Swagger3VO.class);

			ibmPolicyNames.addAll(getIbmPolicyNamesFromJson(vo.getSwagger()));
			ibmPolicyNames.forEach(ibmPolicyName->{
				Query policyQuery = new Query(Criteria.where("ibmPolicyName").is(ibmPolicyName));
				policyQuery.addCriteria(Criteria.where("connectorId").is(connectorId));
				Long count = mongoTemplate.count(policyQuery,PolicyMappingItem.class);
				if(count == 0){
					PolicyMappingItem policyMappingItem = new PolicyMappingItem();
					policyMappingItem.setConnectorId(connectorId);
					policyMappingItem.setIbmPolicyName(ibmPolicyName);
					policyMappingItem.setApigeePolicyName("");
					mongoTemplate.save(policyMappingItem);
				}
			});
		}
		return ibmPolicyNames;
	}

	public List<String> getIbmPolicyNamesFromJson(String oasJson) throws JsonProcessingException {
		List<String> ibmPolicyNames = new ArrayList<>();

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root = objectMapper.readTree(oasJson);

		// Extract the "cors" policy (outside the x-ibm-configuration block)
		JsonNode corsNode = root.path("x-ibm-configuration").path("cors");
		if (corsNode.isObject() && corsNode.path("enabled").asBoolean()) {
			ibmPolicyNames.add("cors");
		}

		// Extract policy names inside the "execute" block
		JsonNode executeBlock = root.path("x-ibm-configuration").path("assembly").path("execute");
		if (executeBlock.isArray()) {
			Iterator<JsonNode> executeNodes = executeBlock.elements();
			while (executeNodes.hasNext()) {
				JsonNode executeNode = executeNodes.next();
				Iterator<String> fieldNames = executeNode.fieldNames();
				while (fieldNames.hasNext()) {
					String policyName = fieldNames.next();
					ibmPolicyNames.add(policyName);
				}
			}
		}

		return ibmPolicyNames;
	}
}
