package com.itorix.apiwiz.design.studio.businessimpl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.Policy;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
@Slf4j
@Component
public class ApicUtil {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
  private ObjectMapper mapper;

	private String processOAS(String swaggerString, List<Category> categoryList) {
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swaggerString);
		swagger.setVendorExtension("x-policies", categoryList);
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return (mapper.writeValueAsString(swagger));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private String processMetadata(String swaggerString, Map<String, Object> metadata) {
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swaggerString);
		swagger.setVendorExtension("x-proxymetadata", metadata);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return (mapper.writeValueAsString(swagger));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private void processOpenApi(ObjectNode node, List<Category> categoryList) {

		ArrayNode arrayNode =  mapper.convertValue(categoryList, ArrayNode.class);
		node.set("x-policies", arrayNode);
	}

	private String processMetadataOpenAPI(ObjectNode node, Map<String, Object> metadata) {
		ObjectNode objectNode =  mapper.convertValue(metadata, ObjectNode.class);
		node.set("x-proxymetadata", objectNode);
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return (mapper.writeValueAsString(node));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}



	public String getPolicyTemplates(String swaggerString) {
		try {
			List<Category> categoryList = mongoTemplate.findAll(Category.class);
			Map<String, String> mappings = getApicMapping();
			Map<String, Object> metadataMap = new HashMap<>();
			for (String key : mappings.keySet()) {
				if (key.contains("x-ibm-policy")) {
					if (null != parseNode(swaggerString, key)) {
						String value = mappings.get(key);
						enableTemplate(categoryList, value);
					}
				} else if (key.contains("x-ibm-metadata")) {
					String value = mappings.get(key);
					JsonNode node = parseNode(swaggerString, value);
					if (null != node) {
						key = key.replaceAll("x-ibm-metadata-", "").replaceAll("#", ".");
						metadataMap.put(key, node);
					}
				}
			}
			String OAS = processOAS(swaggerString, categoryList);
			OAS = processMetadata(OAS, metadataMap);
			return removeResponseSchemaTag(OAS);
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	public String getPolicyTemplatesForOpenApi(String swaggerString, OpenAPI openAPI) {
		try {
			List<Category> categoryList = mongoTemplate.findAll(Category.class);
			Map<String, String> mappings = getApicMapping();
			Map<String, Object> metadataMap = new HashMap<>();
			for (String key : mappings.keySet()) {
				if (key.contains("x-ibm-policy")) {
					if (null != parseNode(swaggerString, key)) {
						String value = mappings.get(key);
						enableTemplate(categoryList, value);
					}
				} else if (key.contains("x-ibm-metadata")) {
					String value = mappings.get(key);
					JsonNode node = parseNode(swaggerString, value);
					if (null != node) {
						key = key.replaceAll("x-ibm-metadata-", "").replaceAll("#", ".");
						metadataMap.put(key, node);
					}
				}
			}
			ObjectNode node = mapper.readValue(swaggerString, ObjectNode.class);
			processOpenApi(node, categoryList);
			String OAS = processMetadataOpenAPI(node, metadataMap);
			return removeResponseSchemaTag(OAS);
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private void enableTemplate(List<Category> categoryList, String value) {
		String[] tokens = value.split("\\.");
		String categoryName = tokens[0];
		String policyName = tokens[1];
		Category category = categoryList.stream().filter(c -> c.getType().equals(categoryName)).findFirst().get();
		Policy policy = category.getPolicies().stream().filter(p -> p.getName().equals(policyName)).findFirst().get();
		policy.setEnabled(true);
	}

	private Map<String, String> getApicMapping() {
		Map<String, String> mappings = null;
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("APIC"));
		List<Integration> dbIntegrations = mongoTemplate.find(query, Integration.class);
		log.debug("getApicMapping : {}",query);
		if (!CollectionUtils.isEmpty(dbIntegrations)) {
			mappings = dbIntegrations.get(0).getApicIntegration().getMappings();
		}
		return mappings;
	}

	private JsonNode parseNode(String swaggerString, String path) {
		if(path.contains("x-ibm-policy/")){
			path = path.replace("[",".");
			path = path.replace("]","");
		}
		path = path.replace("x-ibm-policy/", "");
		try {
			DocumentContext context = JsonPath.parse(swaggerString, Configuration.defaultConfiguration());
			Object object = context.read(path);
			JsonNode node = mapper.convertValue(object, JsonNode.class);
			return node;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private String getFieldName(String name) {
		String[] tokens = name.split("=");
		return tokens[1].replaceAll("'", "").replaceAll("#", ".");
	}

	private String removeResponseSchemaTag(String json) {
		DocumentContext documentContext = JsonPath.parse(json);
		String responseSchemaPath = "$.paths.[*].[*].responses.*.responseSchema";
		Object responseSchema = documentContext.read(responseSchemaPath);
		if (responseSchema != null) {
			json = documentContext.delete(responseSchemaPath).jsonString();
		}
		return json;
	}

}
