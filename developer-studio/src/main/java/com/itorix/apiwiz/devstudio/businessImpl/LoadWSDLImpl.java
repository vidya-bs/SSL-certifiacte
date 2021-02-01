package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.xml.sax.InputSource;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.Target;
import com.itorix.apiwiz.devstudio.business.LoadWSDL;

public class LoadWSDLImpl implements LoadWSDL{	

	private static String convertYamlToJson(String yaml) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);
		ObjectMapper jsonWriter = new ObjectMapper();
		return jsonWriter.writeValueAsString(obj);
	}


	public String loadWSDLProxyOperations(String document) throws Exception{
		Proxy proxy = new Proxy();
		Flows flows = new Flows();
		proxy.setFlows(flows);
		List<Flow> flowList = new ArrayList<Flow>();
		Definition definition = parseWSDLDefinition(document);
		String basePath = definition.getTargetNamespace();
		String targetName = null;
		proxy.setBasePath(basePath.split("//")[1].split(".com")[1]);
		Map<String,PortType> oper = definition.getPortTypes();
		System.out.println("BasePath : " + basePath.split("//")[1].split(".com")[1]);
		for(PortType portType: oper.values() ) {
			String[] name= portType.getQName().toString().split("}");
			if(name.length >= 1)
				targetName = name[1];
			System.out.println(portType.getQName());
			List<Operation> operationsList = portType.getOperations();
			for (Operation operation: operationsList) {
				Flow flow =new Flow();
				flow.setName(operation.getName().replaceAll("\\s",""));
				flow.setVerb("POST");
				flow.setDescription(operation.getName());
				flow.setPath("/");
				flowList.add(flow);
			}
		}
		proxy.setName(targetName);
		proxy.setDescription(targetName);
		proxy.setVersion("v1");
		flows.setFlows(flowList);
		String value = new ObjectMapper().writeValueAsString(proxy);
		return value;
	}


	public String loadWSDLOperations(String document) throws Exception{
		Target target = new Target();
		Flows flows = new Flows();
		target.setFlows(flows);
		List<Flow> flowList = new ArrayList<Flow>();
		Definition definition = parseWSDLDefinition(document);
		String basePath = definition.getTargetNamespace();
		basePath = basePath.replaceAll("^((http[s]?):\\/\\/)?\\/?([^\\/\\.]+\\.)*?([^\\/\\.]+\\.[^:\\/\\s\\.]{2,3}(\\.[^:\\/\\s\\.]{2,3})?)(:\\d+)?($|\\/)", "/");
		String targetName = null;
		target.setBasePath(basePath);
		Map<String,PortType> oper = definition.getPortTypes();
		System.out.println("BasePath : " + basePath);
		for(PortType portType: oper.values() ) {
			String[] name= portType.getQName().toString().split("}");
			if(name.length >= 1)
				targetName = name[1];
			System.out.println(portType.getQName());
			List<Operation> operationsList = portType.getOperations();
			for (Operation operation: operationsList) {
				Flow flow =new Flow();
				flow.setName(operation.getName().replaceAll("\\s",""));
				flow.setVerb("POST");
				flow.setDescription(operation.getName());
				flow.setPath("/");
				flowList.add(flow);
			}
		}
		target.setName(targetName);
		target.setDescription(targetName);
		flows.setFlows(flowList);
		String value = new ObjectMapper().writeValueAsString(target);
		return value;
	}


	private Definition parseWSDLDefinition(String content) throws UnsupportedEncodingException, WSDLException{
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		WSDLFactory wsdlFactory = WSDLFactory.newInstance();
		WSDLReader reader = wsdlFactory.newWSDLReader();
		reader.setFeature("javax.wsdl.verbose", false);
		reader.setFeature("javax.wsdl.importDocuments", true);
		Definition definition = reader.readWSDL(null, new InputSource(input));
		return definition;
	}


	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		try {
			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append( line );
				stringBuilder.append( ls );
			}
			reader.close();
			return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return ls;
	}



}
