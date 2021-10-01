package io.swagger.generator.util;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerCloneDetails;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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

		String replaceSwaggerTitle = replaceSwaggerTitle(swaggerStr, clone.getName());
		String replaceSwaggerDescription = replaceSwaggerDescription(replaceSwaggerTitle, clone.getDescription());
		String replaceSwaggerVersion = replaceSwaggerVersion(replaceSwaggerDescription, clone.getVersion());
		String replaceSwagger3BasePath = replaceSwagger3BasePath(replaceSwaggerVersion, clone.getBasePath());

		dest.setSwagger(replaceSwagger3BasePath);
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

		String replaceSwaggerTitle = replaceSwaggerTitle(swaggerStr, clone.getName());
		String replaceSwaggerDescription = replaceSwaggerDescription(replaceSwaggerTitle, clone.getDescription());
		String replaceSwaggerVersion = replaceSwaggerVersion(replaceSwaggerDescription, clone.getVersion());
		String replaceSwagger2BasePath = replaceSwagger2BasePath(replaceSwaggerVersion, clone.getBasePath());

		dest.setSwagger(replaceSwagger2BasePath);
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

	public static String replaceSwaggerTitle(String swaggerJson, String newTitle) {
		return JsonPath.parse(swaggerJson).set("$.info.title", newTitle).jsonString();
	}

	public static String replaceSwagger2BasePath(String swaggerJson, String pathToReplace) {
		return JsonPath.parse(swaggerJson).set("$.basePath", pathToReplace).jsonString();
	}



	public static String replaceSwagger3BasePath(String swaggerJson, String pathToReplace)
			throws ItorixException {
		String serverPath = "$.servers[*].url";;

		DocumentContext parse = JsonPath.parse(swaggerJson);
		List<String> servers = parse.read(serverPath, List.class);

		for (int i = 0; i < servers.size(); i++) {
			parse.set("$.servers[" + i + "].url", replaceURL(servers.get(i), pathToReplace));
		}
		return parse.jsonString();
	}

	public static String replaceURL(String urlStr, String pathToReplace) throws ItorixException {
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException m) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1008"), m.getMessage()), "Swagger-1008");
		}
		StringBuilder newUrl = new StringBuilder();
		newUrl.append(url.getProtocol() + "://" + replaceNull(url.getHost()));
		if (url.getPort() > 0) {
			newUrl.append(":" + url.getPort());
		}

		newUrl.append(replaceNull(pathToReplace));

		if (url.getQuery() != null) {
			newUrl.append("?" + url.getQuery());
		}
		return newUrl.toString();
	}

	private static String replaceNull(String str) {
		return str != null ? str : "";
	}

	private static String replaceSwaggerDescription(String swaggerJson, String descriptionToReplace) {
		return JsonPath.parse(swaggerJson).set("$.info.description", descriptionToReplace).jsonString();
	}

	private static String replaceSwaggerVersion(String swaggerJson, String versionToReplace) {
		try {
			return JsonPath.parse(swaggerJson).set("$.info.version", versionToReplace).jsonString();
		} catch (Exception ex) {
			return swaggerJson;
		}
	}

}
