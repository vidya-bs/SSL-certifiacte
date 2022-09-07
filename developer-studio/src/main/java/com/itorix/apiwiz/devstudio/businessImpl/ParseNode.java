package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParseNode {

	public JsonNode parse(JsonNode node, String path) {
		try {
			List<String> pathsList = path.contains("/") ? Arrays.asList(path.split("/")) : Arrays.asList(path);
			JsonNode jsonNode = node;

			if (jsonNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) jsonNode;
				JsonNode nodeItem = null;
				for (JsonNode arrayNodeItem : arrayNode) {
					boolean found = false;
					nodeItem = arrayNodeItem;
					for (String pathItem : pathsList) {
						if (pathItem.contains("=")) {
							String[] tokens = pathItem.split("=");
							String key = tokens[0];
							String value = tokens[1];
							nodeItem = nodeItem.get(key);
							if (null != nodeItem && nodeItem.asText().equals(value)) {
								found = true;
							}
						} else {
							nodeItem = nodeItem.get(pathItem);
						}
					}
					if (found)

						return arrayNodeItem;
				}
				return nodeItem;
			} else
				for (String pathItem : pathsList) {
					if (pathItem.contains("[") && null != jsonNode) {
						String nodeName = pathItem.replaceAll("\\[.*?\\]", "");
						String elementName = pathItem.substring(pathItem.indexOf("[") + 1, pathItem.indexOf("]"));
						elementName = elementName.replaceAll("\\^", "/");
						jsonNode = jsonNode.get(nodeName);
						if (jsonNode != null) {
							jsonNode = parse(jsonNode, elementName);
						} else
							return jsonNode;
					} else {
						if (pathItem.contains("=")) {
							String[] tokens = pathItem.split("=");
							String key = tokens[0];
							String value = tokens[1];
							jsonNode = jsonNode.get(key);
							if (null != jsonNode && jsonNode.asText().equals(value)) {
								jsonNode = jsonNode.get(key);
							} else
								jsonNode = null;
						} else
							jsonNode = jsonNode.get(pathItem);
					}
				}
			return jsonNode;
		} catch (Exception ex) {

		}
		return null;
	}

	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();
		String filePath = "/Itorix/tmp/getdocumentlist.json";
		try {
			ParseNode parseNode = new ParseNode();
			JsonNode node = mapper.readTree(new File(filePath));
			String jsonPath = "x-EndpointExtension[Endpoint^name=Sandbox]";
			JsonNode data = parseNode.parse(node, jsonPath);
			log.info(mapper.writeValueAsString(data));

			String jsonPath1 = "x-ibm-configuration/assembly/execute[log.to.k2]/LogDetail";
			log.info(mapper.writeValueAsString(parseNode.parse(node, jsonPath1)));

			String jsonPath2 = "Endpoint/url";
			log.info(mapper.writeValueAsString(parseNode.parse(data, jsonPath2)));

			String jsonPath3 = "Endpoint/environments";
			log.info(mapper.writeValueAsString(parseNode.parse(data, jsonPath3)));

			String jsonPath4 = "Endpoint/regions";
			log.info(mapper.writeValueAsString(parseNode.parse(data, jsonPath4).findValues("region")));
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}

	}

}
