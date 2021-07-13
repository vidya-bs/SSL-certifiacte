package com.itorix.mockserver.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.itorix.mockserver.common.model.expectation.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;

@Controller
@Slf4j
public class MockValidator {

	private static Map<String,SpecVersion.VersionFlag> schemaVersions = new HashMap<>();

	@PostConstruct
	void setJsonSchemaVersions(){
		schemaVersions.put("http://json-schema.org/draft-04/schema#",  SpecVersion.VersionFlag.V4);
		schemaVersions.put("http://json-schema.org/draft-06/schema#", SpecVersion.VersionFlag.V6);
		schemaVersions.put("http://json-schema.org/draft-07/schema#", SpecVersion.VersionFlag.V7);
		schemaVersions.put("http://json-schema.org/draft/2019-09/schema#", SpecVersion.VersionFlag.V201909);
	}

	public boolean checkBody(Expectation expectation, String body,
			MultiValueMap<String, String> formParams, MultiValueMap<String, String> urlEncodedParam) {

		try {
			if(expectation.getRequest().getBody() == null){
				return true;
			}

			if (Body.Type.json.equals(expectation.getRequest().getBody().getType())) {
				if (expectation.getRequest().getBody().isStrict()) {
					if(expectation.getRequest().getBody().getValue()==null){
						return false;
					}
					ObjectMapper mapper = new ObjectMapper();
					JsonNode expctationJson = mapper.readTree(expectation.getRequest().getBody().getValue());
					JsonNode inputJson = mapper.readTree(body);
					return expctationJson.equals(inputJson);
				} else {
					DocumentContext context = JsonPath.parse(body);
					List<Data> dataList = expectation.getRequest().getBody().getData();
					for (Data data : dataList) {
						if (!checkAssertionString(""+context.read(data.getPath()), data.getValue(),
								data.getCondition().name())) {
							return false;
						}
					}
				}
			} else if (Body.Type.jsonSchema.equals(expectation.getRequest().getBody().getType())) {
				return validateJSONSchema(expectation.getRequest().getBody().getValue(), body);
			} else if (Body.Type.xmlSchema.equals(expectation.getRequest().getBody().getType())) {
				return validateXMLSchema(expectation.getRequest().getBody().getValue(), body);
			} else if (Body.Type.xml.equals(expectation.getRequest().getBody().getType())) {
				if (expectation.getRequest().getBody().isStrict()) {
					return isXMLEqual(expectation.getRequest().getBody().getValue(), body);
				} else if (expectation.getRequest().getBody().isStrict()) {
					return isXMLEqual(expectation.getRequest().getBody().getValue(), body);
				} else {
					List<Data> dataList = expectation.getRequest().getBody().getData();
					return validateXML(dataList, body);
				}

			} else if(Body.Type.formParams.equals(expectation.getRequest().getBody().getType())) {
				List<NameMultiValue> expectedformParams = expectation.getRequest().getBody().getFormParams();
				if(!CollectionUtils.isEmpty(expectedformParams) && CollectionUtils.isEmpty(formParams)){
					return false;
				}
				return validateFormParams(formParams, expectedformParams);

			} else if (Body.Type.formURLEncoded.equals(expectation.getRequest().getBody().getType())) {
				List<NameMultiValue> expectedformUrl = expectation.getRequest().getBody().getFormURLEncoded();
				if (!CollectionUtils.isEmpty(expectedformUrl) && CollectionUtils.isEmpty(urlEncodedParam)) {
					return false;
				}
				return validateFormParams(urlEncodedParam, expectedformUrl);

			}
		} catch (Exception e) {
			log.debug("body check did not match", e);
			return false;
		}
		return true;
	}

	private boolean validateFormParams(MultiValueMap<String, String> params, List<NameMultiValue> expected) {
		return validateNameValue(params, expected);
	}

	private boolean validateXML(List<Data> dataList, String body) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(body)));

			for (Data data : dataList) {
				String value = XPathFactory.newInstance().newXPath().evaluate(data.getPath() + "/text()", document);
				if (!checkAssertionString(value, data.getValue(), data.getCondition().name())) {
					return false;
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			log.error("exception occured during xml validation", e);
			return false;
		}
		return true;
	}

	private boolean isXMLEqual(String source, String target) {
		Diff d = DiffBuilder.compare(source).withTest(target).checkForSimilar().ignoreWhitespace().ignoreComments()
				.build();
		return !d.hasDifferences();
	}

	public boolean checkCookie(Expectation expectation, Cookie[] cookies) {
		List<NameSingleValue> expectationCookies = expectation.getRequest().getCookies();
		if (CollectionUtils.isEmpty(expectationCookies)) {
			return true;
		}
		if (cookies == null || cookies.length == 0) {
			log.debug("No cookie found");
			return false;
		}

		boolean matched = false;
		for (NameSingleValue queryParams : expectationCookies) {
			for (Cookie cookie : cookies) {
				matched = false;
				log.debug("actual cookie name {} value {} " , cookie.getName() , cookie.getValue());
				if (checkAssertionString(cookie.getName(), queryParams.getName().getKey(),
						queryParams.getName().getCondition().name())) {
					log.debug("cookie compare act value {}, exp value {} cond {}" , cookie.getName() , cookie.getValue(), queryParams.getValue().getCondition().name());
					if (checkAssertionString(cookie.getValue(), queryParams.getValue().getText(),
							queryParams.getValue().getCondition().name())) {
						matched = true;
					}
					break;
				}
			}
			if (!matched) {
				return false;
			}
		}
		return matched;
	}


	public boolean checkHeader(Expectation expectation, MultiValueMap<String, String> actualHeaders) {
		if (!checkHeaderKeys(expectation, actualHeaders)) {
			return checkHeaderValues(expectation, actualHeaders);
		}
		return false;
	}

	private boolean checkHeaderKeys(Expectation expectation, MultiValueMap<String, String> actualHeaders) {
		Map<String, Boolean> map = new HashMap<>();
		for (NameMultiValue expectedHeader : expectation.getRequest().getHeaders()) {
			Optional<String> matchedHeader = actualHeaders.keySet().stream().filter(actualHeader -> actualHeader.toLowerCase().equals(expectedHeader.getName().getKey().toLowerCase())).findFirst();
			if (matchedHeader.isPresent()) {
				map.put(expectedHeader.getName().getKey(), checkAssertionString(matchedHeader.get().toLowerCase(), expectedHeader.getName().getKey().toLowerCase(),
						expectedHeader.getName().getCondition().name()));
			} else {
				if(expectedHeader.getName().getCondition().equals(Name.Condition.notEqualTo)) {
					map.put(expectedHeader.getName().getKey(), true);
				}
			}
		}
		return map.values().stream().anyMatch(r -> r.booleanValue() == false);

	}

	private boolean checkHeaderValues(Expectation expectation, MultiValueMap<String, String> actualHeaders) {
		for (NameMultiValue expectedHeader : expectation.getRequest().getHeaders()) {
			List<String> actualValues = actualHeaders.get(expectedHeader.getName().getKey());
			if (actualValues != null) {
				return checkAssertion(expectedHeader.getValue().getText(), actualValues,
						expectedHeader.getValue().getCondition().name());
			} else {
				Optional<List<String>> matchedValue = actualHeaders.values().stream().filter(value -> value.equals(expectedHeader.getValue().getText())).findAny();
				if(matchedValue.isPresent()) {
					return checkAssertion(expectedHeader.getValue().getText(), matchedValue.get(),
							expectedHeader.getValue().getCondition().name());
				}
			}
		}
			return false;
	}

	public boolean checkQueryString(Expectation expectation, MultiValueMap<String, String> queryParams) {
		List<NameMultiValue> queryParam = expectation.getRequest().getQueryParams();
		return validateNameValue(queryParams, queryParam);
	}

	private boolean validateNameValue(MultiValueMap<String, String> actualNameValueMap, List<NameMultiValue> expectedNameValueMap) {
		if (CollectionUtils.isEmpty(expectedNameValueMap)) {
			return true;
		}
		boolean matched = false;
		for (NameMultiValue expectedNameValue : expectedNameValueMap) {
			for (String actualQueryParam : actualNameValueMap.keySet()) {
				matched = false;
				String expectedName = expectedNameValue.getName().getKey().toLowerCase();
				Name.Condition expectedNameCondition = expectedNameValue.getName().getCondition();
				if (checkAssertionString(actualQueryParam.toLowerCase(), expectedName,
						expectedNameCondition.name())) {
					List<String> actualValues = actualNameValueMap.get(actualQueryParam);
					if (checkAssertion(expectedNameValue.getValue().getText(), actualValues,
							expectedNameValue.getValue().getCondition().name())) {
						matched = true;
					}
					break;
				}
			}
			if (!matched) {
				return false;
			}
		}
		return matched;
	}

	public boolean chechPath(Expectation expectation, String path) {
		if (Path.Condition.notEqualTo.equals(expectation.getRequest().getPath().getCondition())) {
			Path expectationPath = expectation.getRequest().getPath();
			AntPathMatcher matcher = new AntPathMatcher();
			return !matcher.match(expectationPath.getValue(), path);
		} else {
			String expectationPath = expectation.getRequest().getPath().getValue();
			AntPathMatcher matcher = new AntPathMatcher();
			expectationPath = expectationPath.endsWith("/") ? expectationPath.substring(0, expectationPath.length() - 1) : expectationPath;
			path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
			return matcher.match(expectationPath, path);
		}
	}

	public boolean checkMethod(Expectation expectation, String method) {
		if (expectation.getRequest().getMethod().getCondition().name().equals("any")
				|| checkAssertionString(method, expectation.getRequest().getMethod().getName(),
						expectation.getRequest().getMethod().getCondition().name())) {
			return true;
		}
		return false;
	}

	public static boolean checkAssertionString(String actualValue, String expectedValue, String condition) {
		if (condition == null) {
			return true;
		}

		try {
			if (condition.equalsIgnoreCase("equalTo")) {
				Assert.assertEquals(actualValue, expectedValue);
			} else if (condition.equalsIgnoreCase("present")) {
				Assert.assertNotNull(actualValue);
			} else if (condition.equalsIgnoreCase("absent")) {
				Assert.assertNull(actualValue);
			} else if (condition.equalsIgnoreCase("notEqualTo")) {
				Assert.assertNotEquals(actualValue, expectedValue);
			} else if (condition.equalsIgnoreCase("contains")) {
				Assert.assertTrue(actualValue.contains(expectedValue));
			} else if (condition.equalsIgnoreCase("regex")) {
				Assert.assertTrue(regexMatcher(expectedValue, actualValue));
			} else if (condition.equalsIgnoreCase("is")) {
				if (expectedValue.equalsIgnoreCase("null")) {
					Assert.assertNull(actualValue);
				} else {
					Assert.assertNotNull(actualValue);
				}
			} else if (condition.equalsIgnoreCase("boolean")) {
				if (expectedValue.equalsIgnoreCase("true")) {
					Assert.assertTrue(Boolean.getBoolean(actualValue));
				} else {
					Assert.assertFalse(Boolean.getBoolean(actualValue));
				}
			}
		} catch (AssertionError | Exception ex) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println(checkAssertion(Arrays.asList("A","B"), Arrays.asList("B","A"), "notEqualTo"));
	}

	public static boolean checkAssertion(List<String> expectedValue, List<String> actualValue, String condition) {
		if (condition == null) {
			return true;
		}

		Collections.sort(actualValue);
		Collections.sort(expectedValue);

		try { // Cover equalTo, present, absent, doesNotMatch, matches, contains
			if (condition.equalsIgnoreCase("equalTo")) {
				Assert.assertEquals(actualValue, expectedValue);
			} else if (condition.equalsIgnoreCase("present")) {
				Assert.assertNotNull(actualValue);
			} else if (condition.equalsIgnoreCase("absent")) {
				Assert.assertNull(actualValue);
			} else if (condition.equalsIgnoreCase("notEqualTo")) {
				Assert.assertNotEquals(actualValue, expectedValue);
			} else if (condition.equalsIgnoreCase("contains")) {
				Assert.assertTrue(expectedValue.containsAll(actualValue));

			} else if (condition.equalsIgnoreCase("regex")) {
				Assert.assertTrue(regexMatcher(expectedValue.get(0), actualValue.get(0)));

			}
		} catch (AssertionError | Exception ex) {
			return false;
		}
		return true;
	}

	private static boolean regexMatcher(String regex, String actualValue) {
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(actualValue);
		while (matcher.find()) {
			return true;
		}
		return false;
	}

	public static boolean validateXMLSchema(String xsd, String xml) {

		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(new StringReader(StringEscapeUtils.unescapeJava(xsd))));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(xml)));
		} catch (IOException | SAXException e) {
			System.out.println("Exception: " + e.getMessage());
			return false;
		}
		return true;

	}

	private static boolean validateJSONSchema(String jsonSchema , String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		DocumentContext context = JsonPath.parse(jsonSchema);

		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(schemaVersions.get(context.read("$.$schema")));

		try (InputStream jsonStream = new ByteArrayInputStream(json.getBytes());
				InputStream schemaStream = new ByteArrayInputStream(jsonSchema.getBytes());) {
			JsonNode jsonNode = objectMapper.readTree(jsonStream);
			com.networknt.schema.JsonSchema schema = schemaFactory.getSchema(schemaStream);
			Set<ValidationMessage> validationResult = schema.validate(jsonNode);
			return validationResult.isEmpty();

		} catch (IOException e) {
			log.error("exception occured during json validation", e);
			return false;
		}

	}
}
