package com.itorix.apiwiz.design.studio.businessimpl;

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

	private String processOAS(String swaggerString, List<Category> categoryList) {
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swaggerString);
		swagger.setVendorExtension("x-policies", categoryList);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return (mapper.writeValueAsString(swagger));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private String processMetadata(String swaggerString, Map<String, String> metadata) {
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swaggerString);
		swagger.setVendorExtension("x-metadata", metadata);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			return (mapper.writeValueAsString(swagger));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	public String getPolicyTemplates(String swaggerString) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode data = mapper.readTree(swaggerString);
			List<Category> categoryList = mongoTemplate.findAll(Category.class);
			Map<String, String> mappings = getApicMapping();
			Map<String, String> metadataMap = new HashMap<>();
			for (String key : mappings.keySet()) {
				if (key.contains("x-ibm-policy")) {
					if (null != parseNode(data, key)) {
						String value = mappings.get(key);
						enableTemplate(categoryList, value);
					}
				} else if (key.contains("x-ibm-metadata")) {
					String value = mappings.get(key);
					JsonNode node = parseNode(data, value);
					if (null != node) {
						key = key.replaceAll("x-ibm-metadata-", "").replaceAll("#", ".");
						metadataMap.put(key, node.toPrettyString());
					}
				}
			}
			String OAS = processOAS(swaggerString, categoryList);
			OAS = processMetadata(OAS, metadataMap);
			return removeResponseSchemaTag(OAS);
		} catch (JsonProcessingException e) {
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
		if (!CollectionUtils.isEmpty(dbIntegrations)) {
			mappings = dbIntegrations.get(0).getApicIntegration().getMappings();
		}
		return mappings;
	}

	private JsonNode parseNode(JsonNode node, String path) {
		path = path.replace("x-ibm-policy/", "");
		String[] paths = path.split("/");
		try {
			JsonNode jsonNode = node;
			for (int i = 0; i < paths.length; i++) {
				String pathToken = paths[i];
				if (pathToken.contains("[")) {
					String nodeName = pathToken.replaceAll("\\[.*?\\]", "");
					String elementName = pathToken.substring(pathToken.indexOf("[") + 1, pathToken.indexOf("]"));
					ArrayNode arrayNode = (ArrayNode) jsonNode.get(nodeName);
					for (JsonNode elementNode : arrayNode) {
						if (null != elementNode.get(getFieldName(elementName))) {
							jsonNode = elementNode.get(getFieldName(elementName));
						}
					}
				} else {
					jsonNode = jsonNode.get(pathToken);
				}
			}
			return jsonNode;
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
