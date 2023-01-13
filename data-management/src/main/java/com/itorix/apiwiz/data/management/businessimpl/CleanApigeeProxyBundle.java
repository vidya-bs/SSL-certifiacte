package com.itorix.apiwiz.data.management.businessimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CleanApigeeProxyBundle {

	private static final List<String> unsupportedPolicyTypes = Arrays.asList("ConcurrentRatelimit".toUpperCase(),
			"Ldap".toUpperCase(), "StatisticsCollector".toUpperCase());

//	public static void main(String[] args) {
//		String path = "/Users/sudhakar/apiwiz/temp/ProductCatalog_v1.zip";
//		String outPath = "/Users/sudhakar/apiwiz/temp/proxy";
//		cleanProxy(path, outPath);
//
//		// String sharedflowPath = "/Users/sudhakar/apiwiz/temp/35.zip";
//		// String outputPath = "/Users/sudhakar/apiwiz/temp/sharedflow";
//		// cleanSharedflow(sharedflowPath, outputPath);
//	}

	public static void cleanSharedflow(String bundlePath, String outputFolder) {
		try {
			ZipUtil.unpack(new File(bundlePath), new File(outputFolder));
			String policiesPath = outputFolder + System.getProperty("file.separator") + "sharedflowbundle"
					+ System.getProperty("file.separator") + "policies";
			String proxyPath = outputFolder + System.getProperty("file.separator") + "sharedflowbundle"
					+ System.getProperty("file.separator") + "sharedflows";
			File[] fList = getPolicyFiles(policiesPath);
			List<ApigeePolicy> policies = new ArrayList<>();
			for (File file : fList) {
				ApigeePolicy apigeePolicy = getType(file.getPath());
				if (apigeePolicy != null)
					policies.add(apigeePolicy);
			}
			if (!CollectionUtils.isEmpty(policies)) {
				removeProxyPolicies(proxyPath, policies);
				removeNameElements(policies);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			System.out.println("OUTPUT : " + objectMapper.writeValueAsString(policies));
			ZipUtil.pack(new File(outputFolder), new File(bundlePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void cleanProxy(String proxyBundlePath, String outputFolder) {

		try {
			ZipUtil.unpack(new File(proxyBundlePath), new File(outputFolder));
			String policiesPath = outputFolder + System.getProperty("file.separator") + "apiproxy"
					+ System.getProperty("file.separator") + "policies";
			String proxyPath = outputFolder + System.getProperty("file.separator") + "apiproxy"
					+ System.getProperty("file.separator") + "proxies";
			String targetPath = outputFolder + System.getProperty("file.separator") + "apiproxy"
					+ System.getProperty("file.separator") + "targets";
			System.out.println("policies path : " + policiesPath);
			File[] fList = getPolicyFiles(policiesPath);
			List<ApigeePolicy> policies = new ArrayList<>();
			for (File file : fList) {
				ApigeePolicy apigeePolicy = getType(file.getPath());
				if (apigeePolicy != null)
					policies.add(apigeePolicy);
			}
			if (!CollectionUtils.isEmpty(policies)) {
				removeProxyPolicies(proxyPath, policies);
				removeProxyPolicies(targetPath, policies);
				removeNameElements(policies);
				ObjectMapper objectMapper = new ObjectMapper();
				System.out.println("OUTPUT : " + objectMapper.writeValueAsString(policies));
				ZipUtil.pack(new File(outputFolder), new File(proxyBundlePath));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File[] getPolicyFiles(String dirName) {
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
	}

	private static synchronized ApigeePolicy getType(String filePath) {
		Document document;
		DocumentBuilder builder;
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			String xml = lines.collect(Collectors.joining(System.lineSeparator()));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			String root = document.getDocumentElement().getNodeName();
			if (unsupportedPolicyTypes.contains(root.toUpperCase())) {
				String policyName = (processRegex(filePath, "name=\"[\\s\\S]*?\"").replaceAll("name=\"", ""))
						.replaceAll("\"", "");
				ApigeePolicy policy = new CleanApigeeProxyBundle().new ApigeePolicy();
				policy.setFilePath(filePath);
				policy.setName(policyName);
				policy.setType(root);
				return policy;
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private synchronized static String processRegex(String file, String regex) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder outString = null;
		try {
			while ((line = reader.readLine()) != null) {
				Pattern regexPattern = Pattern.compile(regex);
				Matcher matcher = regexPattern.matcher(line);
				while (matcher.find()) {
					int start = matcher.start(0);
					int end = matcher.end(0);
					if (outString == null)
						outString = new StringBuilder();
					outString.append(line.substring(start, end));
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
			e.printStackTrace();
		}
		return "";
	}

	private static void removeProxyPolicies(String path, List<ApigeePolicy> policiesToDelete) {
		File[] fList = getPolicyFiles(path);
		if (fList != null && fList.length > 0) {
			for (File fileName : fList) {
				try {
					getpaths(fileName.getPath(), policiesToDelete);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void removeNameElements(List<ApigeePolicy> policiesToDelete) {
		for (ApigeePolicy policy : policiesToDelete) {
			File file = new File(policy.getFilePath());
			file.delete();
		}
	}

	public static void getpaths(String path, List<ApigeePolicy> policiesToDelete) throws Exception {
		File file = new File(path);
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//*[not(*)]";
		String xml = "";
		Document document;
		try (Stream<String> lines = Files.lines(Paths.get(file.getPath()))) {
			xml = lines.collect(Collectors.joining(System.lineSeparator()));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				List<String> xPaths = new ArrayList<>();
				String namePath = getXPath(nodeList.item(i)).replace("#document", "/");
				xPaths.add(namePath);
				if (namePath.contains("Name")) {
					Object result1 = XPathFactory.newInstance().newXPath().evaluate(namePath + "/text()", document,
							XPathConstants.NODESET);
					NodeList nodes1 = (NodeList) result1;
					for (int j = 0; j < nodes1.getLength(); j++) {
						String nodeValue1 = nodes1.item(j).getNodeValue();
						for (ApigeePolicy policy : policiesToDelete) {
							if (policy.getName().equals(nodeValue1)) {
								nodes1.item(j).getParentNode().removeChild(nodes1.item(j));
							}
						}
					}
				}
			}
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.transform(new DOMSource(document), new StreamResult(new FileOutputStream(path)));
		} catch (Exception e) {

		}
	}

	private static String getXPath(Node node) {
		Node parent = node.getParentNode();
		if (parent == null) {
			return node.getNodeName();
		}
		return getXPath(parent) + "/" + node.getNodeName();
	}

	private class ApigeePolicy {
		private String name;
		private String type;
		private String filePath;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getFilePath() {
			return filePath;
		}
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
	}
}
