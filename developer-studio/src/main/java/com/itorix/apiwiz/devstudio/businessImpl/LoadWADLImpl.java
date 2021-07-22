package com.itorix.apiwiz.devstudio.businessImpl;

import javax.xml.xpath.XPathExpressionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.Target;
import com.itorix.apiwiz.devstudio.business.LoadWADL;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class LoadWADLImpl implements LoadWADL {

	@Override
	public String getWADLTargetOperations(String document) throws XPathExpressionException, JsonProcessingException {
		Document doc = getDoc(document);
		Target target = new Target();
		Flows flows = new Flows();
		target.setFlows(flows);
		Node rootNode = doc.getDocumentElement();
		String prefix = "";
		if (rootNode.getPrefix() != null)
			prefix = rootNode.getPrefix() + ":";
		Node resources = doc.getElementsByTagName(prefix + "resources").item(0);
		Element resourcesElement = (Element) resources;
		target.setBasePath(resourcesElement.getAttribute("base"));
		NodeList resource = doc.getElementsByTagName(prefix + "resource");
		List<Flow> flowList = new ArrayList<Flow>();
		for (int temp = 0; temp < resource.getLength(); temp++) {
			Node nNode = resource.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String path = eElement.getAttribute("path");
				// System.out.println("path : " + path);
				NodeList methodList = nNode.getChildNodes();
				for (int i = 0; i < methodList.getLength(); i++) {
					Node methodNode = methodList.item(i);
					if (methodNode.getNodeType() == Node.ELEMENT_NODE
							&& methodNode.getNodeName().equals(prefix + "method")) {
						Flow flow = new Flow();
						Element methodElement = (Element) methodNode;
						flow.setVerb(methodElement.getAttribute("name"));
						flow.setPath(path);
						flow.setName(methodElement.getAttribute("id").replaceAll("\\s", ""));
						flow.setDescription(methodElement.getAttribute("id"));
						flowList.add(flow);
					}
				}
			}
		}

		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(target);
	}

	@Override
	public String getWADLProxyOperations(String document) throws XPathExpressionException, JsonProcessingException {
		Document doc = getDoc(document);
		Proxy proxy = new Proxy();
		Flows flows = new Flows();
		proxy.setFlows(flows);
		Node rootNode = doc.getDocumentElement();
		String prefix = "";
		if (rootNode.getPrefix() != null)
			prefix = rootNode.getPrefix() + ":";
		Node resources = doc.getElementsByTagName(prefix + "resources").item(0);
		Element resourcesElement = (Element) resources;
		proxy.setBasePath(resourcesElement.getAttribute("base"));
		NodeList resource = doc.getElementsByTagName(prefix + "resource");
		List<Flow> flowList = new ArrayList<Flow>();
		for (int temp = 0; temp < resource.getLength(); temp++) {
			Node nNode = resource.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String path = eElement.getAttribute("path");
				NodeList methodList = nNode.getChildNodes();
				for (int i = 0; i < methodList.getLength(); i++) {
					Node methodNode = methodList.item(i);
					if (methodNode.getNodeType() == Node.ELEMENT_NODE
							&& methodNode.getNodeName().equals(prefix + "method")) {
						Flow flow = new Flow();
						Element methodElement = (Element) methodNode;
						flow.setVerb(methodElement.getAttribute("name"));
						flow.setPath(path);
						flow.setName(methodElement.getAttribute("id").replaceAll("\\s", ""));
						flow.setDescription(methodElement.getAttribute("id"));
						flowList.add(flow);
					}
				}
			}
		}

		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy);
	}

	private static Document getDoc(String content) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
			Document doc = builder.parse(input);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
