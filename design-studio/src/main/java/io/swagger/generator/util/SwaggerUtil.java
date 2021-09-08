package io.swagger.generator.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerCloneDetails;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SwaggerUtil {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerUtil.class);

	@SneakyThrows
	private SwaggerUtil() {
		throw new IllegalAccessException();
	}

	@SneakyThrows
	public static void copyAllSwaggerFields(Swagger3VO dest, Swagger3VO orig) {
		BeanUtils.copyProperties(dest, orig);
		dest.setId(null);
	}

	@SneakyThrows
	public static void setCloneDetailsFromReq(Swagger3VO dest, SwaggerCloneDetails clone, String swaggerStr) {
		dest.setName(clone.getName());
		dest.setDescription(clone.getDescription());
		dest.setRevision(1);
		String swaggerId = UUID.randomUUID().toString().replaceAll("-", "");
		dest.setSwaggerId(swaggerId);

		OpenAPI openAPI = new OpenAPIParser().readContents(swaggerStr, null, null).getOpenAPI();
		openAPI.getInfo().setTitle(clone.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		dest.setSwagger(objectMapper.writeValueAsString(openAPI));
	}

	@SneakyThrows
	public static void copyAllSwaggerFields(SwaggerVO dest, SwaggerVO orig) {
		BeanUtils.copyProperties(dest, orig);
		dest.setId(null);
	}

	@SneakyThrows
	public static void setCloneDetailsFromReq(SwaggerVO dest, SwaggerCloneDetails clone, String swaggerStr) {
		dest.setName(clone.getName());
		dest.setDescription(clone.getDescription());
		dest.setRevision(1);
		String swaggerId = UUID.randomUUID().toString().replaceAll("-", "");
		dest.setSwaggerId(swaggerId);

		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swaggerStr);
		swagger.setBasePath(clone.getBasePath());
		swagger.getInfo().setTitle(clone.getName());
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.setSerializationInclusion(Include.NON_NULL);
		String swaggerJson = objMapper.writeValueAsString(swagger);
		swaggerJson = removeResponseSchemaTag(swaggerJson);
		dest.setSwagger(swaggerJson);
	}

	public static String removeResponseSchemaTag(String json) {
		DocumentContext documentContext = JsonPath.parse(json);

		String responseSchemaPath = "$.paths.[*].[*].responses.*.responseSchema";
		Object responseSchema = documentContext.read(responseSchemaPath);

		if (responseSchema != null) {
			json = documentContext.delete(responseSchemaPath).jsonString();
		}

		return json;

	}

}
