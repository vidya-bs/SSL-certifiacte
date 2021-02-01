package com.itorix.hyggee.mockserver.templates.engine.velocity;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.script.VelocityScriptEngineFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.hyggee.mockserver.client.serialization.model.DTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpTemplate;
import com.itorix.hyggee.mockserver.model.Variable;
import com.itorix.hyggee.mockserver.templates.engine.TemplateEngine;
import com.itorix.hyggee.mockserver.templates.engine.model.HttpRequestTemplateObject;
import com.itorix.hyggee.mockserver.templates.engine.serializer.HttpTemplateOutputDeserializer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;


import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static com.itorix.hyggee.mockserver.formatting.StringFormatter.formatLogMessage;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.TEMPLATE_GENERATED;


public class VelocityTemplateEngine implements TemplateEngine {

	private static final ScriptEngineManager manager = new ScriptEngineManager();
	private static final ScriptEngine engine;
	private final MockServerLogger logFormatter;
	private HttpTemplateOutputDeserializer httpTemplateOutputDeserializer;

	static {
		manager.registerEngineName("velocity", new VelocityScriptEngineFactory());
		engine = manager.getEngineByName("velocity");
	}

	public VelocityTemplateEngine(MockServerLogger logFormatter) {
		this.logFormatter = logFormatter;
		this.httpTemplateOutputDeserializer = new HttpTemplateOutputDeserializer(logFormatter);
	}

	@Override
	public <T> T executeTemplate(Expectation expectation,HttpTemplate template, HttpRequest request, Class<? extends DTO<T>> dtoClass) {
		T result = null;
		try {
			@SuppressWarnings("rawtypes")
			Map variables = populateVariables(template.getVariables(), request, expectation);
			Writer writer = new StringWriter();
			ScriptContext context = engine.getContext();
			context.setWriter(writer);
			context.setAttribute("variables", variables , ScriptContext.ENGINE_SCOPE);
			context.setAttribute("request", new HttpRequestTemplateObject(request), ScriptContext.ENGINE_SCOPE);
			engine.eval(template.getTemplate());
			logFormatter.info(TEMPLATE_GENERATED, request, "generated output:{}from template:{}for request:{}", writer.toString(), template, request);
			result = httpTemplateOutputDeserializer.deserializer(request, writer.toString(), dtoClass);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(formatLogMessage("Exception transforming template:{}for request:{}", template, request), e);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> Map<T,T> populateVariables(List<Variable> variables,  HttpRequest request, Expectation expectation){
		Map pathVariables = populatePathVariables(expectation.getHttpRequest().getPath().toString(),request.getPath().toString());
		Map bodyVariables = populateBodyVariables(variables,request);
		Map variablesMap = new HashMap();
		if(variables != null)
		for (Variable variable : variables) {
			if (variable.getReference() != null) {
				if (variable.getReference().equalsIgnoreCase("path")) {
					try {
						variablesMap.put(variable.getName(), pathVariables.get(variable.getName()));
					}catch(Exception e) {
						variablesMap.put(variable.getName(), "not available");
					}
				} else if (variable.getReference().equalsIgnoreCase("query")) {
					variablesMap.put(variable.getName(), request.getFirstQueryStringParameter(variable.getName()));
				} else if (variable.getReference().equalsIgnoreCase("body")) {
					try {
						variablesMap.put(variable.getName(), bodyVariables.get(variable.getName()));
					}catch(Exception e) {
						variablesMap.put(variable.getName(), "not available");
					}
				}
			}
		}
		return variablesMap;
	}

	@SuppressWarnings("rawtypes")
	private Map  populatePathVariables(String expectationPath, String requestPath) {
		try {
			org.springframework.web.util.UriTemplate uriTemplate = new org.springframework.web.util.UriTemplate(expectationPath);
			ObjectMapper mapper = new ObjectMapper();
			Map varMap =  mapper.convertValue(uriTemplate.match(requestPath), Map.class);
			return varMap;
		}catch (Exception ex) {
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Map  populateBodyVariables(List<Variable> variables, HttpRequest request) {
		Map varMap = null;
		try {
			if(request.getFirstHeader("Content-Type").contains("json"))
				varMap = populateJsonBodyVariables(variables, request);
			if(request.getFirstHeader("Content-Type").contains("xml"))
				varMap = populateXMLBodyVariables(variables, request);
			//TODO add from parms
			return varMap;
		}catch (Exception ex) {
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map  populateJsonBodyVariables(List<Variable> variables, HttpRequest request) {
		DocumentContext context;
		context = JsonPath.parse(request.getBody().toString());
		Map varMap = new HashMap();
		try {
			for (Variable variable : variables) {
				if (variable.getReference().equalsIgnoreCase("body"))
				varMap.put(variable.getName(), getAttributeValue(variable.getValue(), context));
			}
			return varMap;
		}catch (Exception ex) {
			return null;
		}
	}
	
	private String getAttributeValue(String path, DocumentContext context) throws XPathExpressionException {
		String value = null;
		try {
			value = context.read(path).toString();
			
			String content = value
					.replaceAll("=", "\":\"")
					.replaceAll(", ", "\",\"")
//					.replaceAll("}", "}")
//					.replaceAll("\\{", "{")
					.replaceAll("\"\\{", "{\"")
					.replaceAll("\"\\[", "\\[");
			value = content.replaceFirst("\\{", "{\"");
			
			
			if(!value.contains("\""))
				value = "\""+value+"\"";
			value = StringEscapeUtils.escapeJson(value);
//			value = "\"" + value + "\"";
			System.out.println("**************************************************************************************************************");
			System.out.println("value : " + value);
			System.out.println("content : " + content);
			System.out.println("**************************************************************************************************************");
//			if(context.read(path) instanceof JSONArray) {
//				JSONArray array = context.read(path);
//				if(array.size() == 1) {
//					value = array.get(0).toString();
//				} else {
//					
//					throw new Exception("Invalid JSON Path specified");
//				}
//			} else {
//				value = context.read(path).toString();
//			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return value;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map  populateXMLBodyVariables(List<Variable> variables, HttpRequest request) {
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		Document document;
		
		Map varMap = new HashMap();
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(request.getBody().toString())));
			for (Variable variable : variables) {
				if (variable.getReference().equalsIgnoreCase("body"))
				varMap.put(variable.getName(), getAttributeValue( document, variable.getValue()));
			}
			return varMap;
		}catch (Exception ex) {
			return null;
		}
	}
	
	private  String getAttributeValue(Document document, String path) throws XPathExpressionException {
		String value = null;
		try {
			value = XPathFactory.newInstance().newXPath().evaluate(path + "/text()", document);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return value; 
	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static void main(String[] arg) {
//		org.springframework.web.util.UriTemplate uriTemplate = new org.springframework.web.util.UriTemplate("/hotels/*/bookings/{booking}/test?q={test1}");
//		System.out.println(uriTemplate.getVariableNames());
//		ObjectMapper mapper = new ObjectMapper();
//		System.out.println(uriTemplate.match("/hotels/*/bookings/450/test?q=12123"));
//		Map varMap =  mapper.convertValue(uriTemplate.match("/hotels/*/bookings/450/test?q=12123"), Map.class);
//		String URI= uriTemplate.expand(varMap).toString();
//		try {
//			System.out.println(java.net.URLDecoder.decode(URI,"UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//System.out.println(uriTemplate.expand("hotel"));
////		System.out.println(uriTemplate.matches("/hotels/1/bookings/42/test"));
////		System.out.println(uriTemplate.match("/hotels/1/bookings/42/test?q=1234,3456"));
//	}
}

