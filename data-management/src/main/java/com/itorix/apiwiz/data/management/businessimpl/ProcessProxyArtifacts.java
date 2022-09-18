package com.itorix.apiwiz.data.management.businessimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.data.management.model.overview.Policy;
import com.itorix.apiwiz.data.management.model.overview.Proxies;

import com.itorix.apiwiz.data.management.model.overview.Resources;
import com.itorix.apiwiz.data.management.model.overview.Targetserver;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ProcessProxyArtifacts {
	
	///**
	public static void main(String[] args) throws JsonProcessingException {
		String path = "/Users/sudhakar/apiwiz/temp/apiproxy";
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(processArtifacts(path)));
	}
	//*/

	@SuppressWarnings("rawtypes")
	public static Proxies processArtifacts(String path) {
		Map<String, Policy> proxyPolicyMap = new HashMap<>();
		Map<String, Policy> targetPolicyMap = new HashMap<>();
		String policiesPath = path + System.getProperty("file.separator") + "policies";
		String proxyPath = path + System.getProperty("file.separator") + "proxies";
		String targetPath = path + System.getProperty("file.separator") + "targets";
		File[] fList = finder(policiesPath);
		Set<String> kvmResources = new HashSet<String>();
		Set<String> cacheResources = new HashSet<String>();
		List<Targetserver> targets = new ArrayList<>();
		List<Resources> paths = null;
		String basepath = null;
		if(fList != null && fList.length > 0)
			for (File file : fList) {
				try {
					String policyType = getType(file.getPath());
					String policyName = (processRegex(file, "name=\"[\\s\\S]*?\"").replaceAll("name=\"", ""))
							.replaceAll("\"", "");
					Policy policy = new Policy();
					policy.setType(policyType);
					policy.setName(policyName);
					proxyPolicyMap.put(policyName, policy);
					targetPolicyMap.put(policyName, policy);

					String resource = (processRegex(file, "mapIdentifier=\"[\\s\\S]*?\"")
							.replaceAll("mapIdentifier=\"", "")).replaceAll("\"", "");
					if (!resource.isEmpty())
						kvmResources.add(resource);
					resource = processRegex(file, "<CacheResource>[\\s\\S]*?<\\/CacheResource>")
							.replaceAll("<[\\s\\S]*?>", "");
					if (!resource.isEmpty())
						cacheResources.add(resource);
				} catch (IOException e) {
					e.printStackTrace();
					log.error("Exception occurred", e);
				}
			}
		try {
			populateProxyPolicies(proxyPath, proxyPolicyMap);
			paths = populatePaths(proxyPath);
			basepath = populateBasePath(proxyPath);
			// ObjectMapper objectMapper = new ObjectMapper();
			// System.out.println(objectMapper.writeValueAsString(proxyPolicyMap));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			targets = getTargets(targetPath);
			populateTargetPolicies(targetPath, targetPolicyMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Proxies data = new Proxies();
		data.setKvm((List) Arrays.asList(kvmResources.toArray()));
		data.setCache((List) Arrays.asList(cacheResources.toArray()));
		data.setTargetservers(targets);
		data.setPaths(paths);
		data.setProxyPolicies(getPolicyList(proxyPolicyMap));
		data.setTargetPolicies(getPolicyList(targetPolicyMap));
		data.setBasePath(basepath);
		return data;
	}

	private static File[] finder(String dirName) {
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
	}

	private static List<Targetserver> getTargets(String file) throws IOException {
		File[] fList = finder(file);
		List<Targetserver> targetList = new ArrayList<>();
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				String targetEndpoint = processRegex(fileName, "<Server name=[\\s\\S]*?>");
				String[] ary = targetEndpoint.split("<Server name=");
				for (int i = 0; i < ary.length; i++)
					if (!ary[i].replaceAll(">", "").trim().isEmpty()) {
						String targetName = ary[i].replaceAll(">", "").replaceAll("\"", "");
						Targetserver target = new Targetserver();
						target.setName(targetName);
						targetList.add(target);
					}
			}
		}
		return targetList;
	}

	private synchronized static String processRegex(File file, String regex) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder outString = null;
		try {
			while ((line = reader.readLine()) != null) {
				String text = processRegex(line, regex);
				if(text != null) {
					if (outString == null)
						outString = new StringBuilder();
					outString.append(text);
				}
				if (outString != null)
					break;
			}
			reader.close();
			if (outString != null)
				return (outString.toString());
			else
				return new StringBuilder().toString();

		} catch (IOException e) {
			log.error("Exception occurred", e);
		}
		return "";
	}

	private synchronized static String processRegex(String line, String regex) {
		Pattern regexPattern = Pattern.compile(regex);
		Matcher matcher = regexPattern.matcher(line);
		while (matcher.find()) {
			int start = matcher.start(0);
			int end = matcher.end(0);
			return line.substring(start, end);
		}
		return null;
	}

	public static List<Resources> populatePaths(String path) {
		File[] fList = finder(path);
		List<Resources> paths= null;
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				String xml = "";
				Document document;
				try (Stream<String> lines = Files.lines(Paths.get(fileName.getPath()))) {
					xml = lines.collect(Collectors.joining(System.lineSeparator()));
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
					String xPath = "//ProxyEndpoint/Flows/Flow/@name";
					Object result = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
							XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						xPath = "//ProxyEndpoint/Flows/Flow[@name='" + nodeValue + "']/Condition/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							String resourcePath = StringUtils.substringBetween(processRegex(nodeValue1, "MatchesPath([^\\)]*)") , "\"", "\"");
							String verb = StringUtils.substringBetween(processRegex(nodeValue1, "(?i)(?>verb).*(\\\".*\\\")") , "\"", "\"");
							if(resourcePath!= null || verb != null) {
								if(paths == null) {
									paths = new ArrayList<>();
								}
								Resources resource = new Resources();
								resource.setName(nodeValue);
								resource.setPath(resourcePath);
								resource.setVerb(verb);
								paths.add(resource);
							}
						}
					}
				}catch(Exception e) {
				}
			}
		}
		return paths;
	}
	
	public static String populateBasePath(String path) {
		File[] fList = finder(path);
		String basePath = null;
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				String xml = "";
				Document document;
				try (Stream<String> lines = Files.lines(Paths.get(fileName.getPath()))) {
					xml = lines.collect(Collectors.joining(System.lineSeparator()));
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
					String xPath = "//ProxyEndpoint/HTTPProxyConnection/BasePath/text()";
					Object result = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
							XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					for (int i = 0; i < nodes.getLength(); i++) {
						 basePath = nodes.item(i).getNodeValue();
					}
				}catch(Exception e) {
				}
			}
		}
		return basePath;
	}

	public static void populateProxyPolicies(String path, Map<String, Policy> policyMap) {
		File[] fList = finder(path);
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				String xml = "";
				Document document;
				try (Stream<String> lines = Files.lines(Paths.get(fileName.getPath()))) {

					xml = lines.collect(Collectors.joining(System.lineSeparator()));
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

					String xPath = "//ProxyEndpoint/FaultRules/FaultRule/@name";
					Object result = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
							XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//ProxyEndpoint/FaultRules/FaultRule[@name='" + nodeValue + "']/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("FaultRules");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the DefaultFaultRule

					xPath = "//ProxyEndpoint/DefaultFaultRule/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//ProxyEndpoint/DefaultFaultRule[@name='" + nodeValue + "']/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("DefaultFaultRule");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the PreFlow

					xPath = "//ProxyEndpoint/PreFlow/Request/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Request");
						policy.getFlowType().add("PreFlow");
						policyMap.put(nodeValue1, policy);
					}
					xPath = "//ProxyEndpoint/PreFlow/Response/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Response");
						policy.getFlowType().add("PreFlow");
						policyMap.put(nodeValue1, policy);
					}

					// check for the Flows
					xPath = "//ProxyEndpoint/Flows/Flow/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//ProxyEndpoint/Flows/Flow[@name='" + nodeValue + "']/Request/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("Flow-Request");
							policyMap.put(nodeValue1, policy);
						}
					}
					xPath = "//ProxyEndpoint/Flows/Flow/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//ProxyEndpoint/Flows/Flow[@name='" + nodeValue + "']/Response/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("Flow-Response");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the PostFlow

					xPath = "//ProxyEndpoint/PostFlow/Request/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Request");
						policy.getFlowType().add("PostFlow");
						policyMap.put(nodeValue1, policy);
					}
					xPath = "//ProxyEndpoint/PostFlow/Response/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Response");
						policy.getFlowType().add("PostFlow");
						policyMap.put(nodeValue1, policy);
					}

					// check for the PostClientFlow

					xPath = "//ProxyEndpoint/PostClientFlow/Response/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Response");
						policy.getFlowType().add("PostClientFlow");
						policyMap.put(nodeValue1, policy);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void populateTargetPolicies(String path, Map<String, Policy> policyMap) {
		File[] fList = finder(path);
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				String xml = "";
				Document document;
				try (Stream<String> lines = Files.lines(Paths.get(fileName.getPath()))) {
					xml = lines.collect(Collectors.joining(System.lineSeparator()));
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
					String xPath = "//TargetEndpoint/FaultRules/FaultRule/@name";
					Object result = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
							XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//TargetEndpoint/FaultRules/FaultRule[@name='" + nodeValue + "']/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("FaultRules");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the DefaultFaultRule

					xPath = "//TargetEndpoint/DefaultFaultRule/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//TargetEndpoint/DefaultFaultRule[@name='" + nodeValue + "']/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("DefaultFaultRule");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the PreFlow

					xPath = "//TargetEndpoint/PreFlow/Request/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Request");
						policy.getFlowType().add("PreFlow");
						policyMap.put(nodeValue1, policy);
					}
					xPath = "//TargetEndpoint/PreFlow/Response/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Response");
						policy.getFlowType().add("PreFlow");
						policyMap.put(nodeValue1, policy);
					}

					// check for the Flows
					xPath = "//TargetEndpoint/Flows/Flow/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//TargetEndpoint/Flows/Flow[@name='" + nodeValue + "']/Request/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("Flow-Request");
							policyMap.put(nodeValue1, policy);
						}
					}
					xPath = "//TargetEndpoint/Flows/Flow/@name";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int i = 0; i < nodes.getLength(); i++) {
						String nodeValue = nodes.item(i).getNodeValue();
						// System.out.println(nodeValue);
						xPath = "//TargetEndpoint/Flows/Flow[@name='" + nodeValue + "']/Response/Step/Name/text()";
						Object result1 = XPathFactory.newInstance().newXPath().evaluate(xPath, document,
								XPathConstants.NODESET);
						NodeList nodes1 = (NodeList) result1;
						// System.out.println(nodes1.getLength());
						for (int j = 0; j < nodes1.getLength(); j++) {
							String nodeValue1 = nodes1.item(j).getNodeValue();
							// System.out.println(nodeValue1);
							Policy policy = policyMap.get(nodeValue1);
							if (policy == null)
								policy = new Policy();
							policy.setName(nodeValue1);
							policy.getReferences().add(nodeValue);
							policy.getFlowType().add("Flow-Response");
							policyMap.put(nodeValue1, policy);
						}
					}

					// check for the PostFlow

					xPath = "//TargetEndpoint/PostFlow/Request/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Request");
						policy.getFlowType().add("PostFlow");
						policyMap.put(nodeValue1, policy);
					}
					xPath = "//TargetEndpoint/PostFlow/Response/Step/Name/text()";
					result = XPathFactory.newInstance().newXPath().evaluate(xPath, document, XPathConstants.NODESET);
					nodes = (NodeList) result;
					// System.out.println(nodes.getLength());
					for (int j = 0; j < nodes.getLength(); j++) {
						String nodeValue1 = nodes.item(j).getNodeValue();
						// System.out.println(nodeValue1);
						Policy policy = policyMap.get(nodeValue1);
						if (policy == null)
							policy = new Policy();
						policy.setName(nodeValue1);
						policy.getReferences().add("Response");
						policy.getFlowType().add("PostFlow");
						policyMap.put(nodeValue1, policy);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getType(String filePath) {
		Document document;
		DocumentBuilder builder;
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			String xml = lines.collect(Collectors.joining(System.lineSeparator()));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			String root = document.getDocumentElement().getNodeName();
			return root;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Policy> getPolicyList(Map<String, Policy> policyMap) {
		List<Policy> policies = new ArrayList<>();
		for (String key : policyMap.keySet()) {
			policies.add(policyMap.get(key));
		}
		return policies;
	}

}
