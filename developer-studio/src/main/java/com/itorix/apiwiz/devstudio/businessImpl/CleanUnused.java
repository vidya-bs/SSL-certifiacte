package com.itorix.apiwiz.devstudio.businessImpl;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.model.proxystudio.ProxyEndpoint;

import org.w3c.dom.Node;


public class CleanUnused {
	private static String resources = "";
	
	public static ProxyArtifacts processArtifacts(String path){
		String policiesPath = path + System.getProperty("file.separator") + "policies";
		String proxyPath = path + System.getProperty("file.separator") + "proxies";
		String targetPath = path + System.getProperty("file.separator") + "targets";
		File[] fList = finder(policiesPath);
		Set<String> kvmResources = new HashSet<String>();
		Set<String> cacheResources = new HashSet<String>();
		List<String> targets = new ArrayList<String>();
		List<String> sharedflows = new ArrayList<String>();
		for (File file : fList) {
			try {
				String resource = (processRegex(file.getPath(),"mapIdentifier=\"[\\s\\S]*?\"").replaceAll("mapIdentifier=\"", "")).replaceAll("\"", "");
				if(!resource.isEmpty())
					kvmResources.add(resource);
				resource = processRegex(file.getPath(),"<CacheResource>[\\s\\S]*?<\\/CacheResource>").replaceAll("<[\\s\\S]*?>", "");
				if(!resource.isEmpty())
					cacheResources.add(resource);
				resource = processRegex(file.getPath(),"<SharedFlowBundle>[\\s\\S]*?<\\/SharedFlowBundle>").replaceAll("<[\\s\\S]*?>", "");
				if(!resource.isEmpty())
					sharedflows.add(resource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			targets = getTargets(targetPath);
		} catch (IOException e) {
		}
		ProxyArtifacts data = new ProxyArtifacts();
		data.setKvm((List)Arrays.asList(kvmResources.toArray()));
		data.setCaches((List)Arrays.asList(cacheResources.toArray()));
		data.setTargetServers(targets);
		data.setSharedflows(sharedflows);
		data.setProxyEndpoint(getProxyEndpoints(proxyPath));
		return data;
	}
	
	private static List<String> getTargets(String file) throws IOException {
		File[] fList = finder(file);
		List<String> targetList = new ArrayList<String>();
		if(fList != null)
		for(File fileName: fList){
			String targetEndpoint  = processRegex(fileName.getPath(),"<Server name=[\\s\\S]*?>");
			String[] ary = targetEndpoint.split("<Server name=");
			for(int i = 0; i< ary.length ; i++)
				if(!ary[i].replaceAll(">", "").trim().isEmpty())
					targetList.add(ary[i].replaceAll(">", "").replaceAll("\"", ""));
		}
		return targetList;
	}
	
	private static List<ProxyEndpoint> getProxyEndpoints(String proxyLocation) {
		List<ProxyEndpoint> proxyEndpoints = new ArrayList<ProxyEndpoint>();
		try {
		File[] fList = finder(proxyLocation);
		if(fList != null)
		for(File fileName: fList){
			ProxyEndpoint endpoint = new ProxyEndpoint();
			String proxyEndpoint  = processRegex(fileName.getPath(),"<ProxyEndpoint name=[\\s\\S]*?>");
			String[] ary = proxyEndpoint.split("<ProxyEndpoint name=");
			for(int i = 0; i< ary.length ; i++)
				if(!ary[i].replaceAll(">", "").trim().isEmpty())
					endpoint.setName(ary[i].replaceAll(">", "").replaceAll("\"", ""));
			String basePath = processRegex(fileName.getPath(),"<BasePath>[\\s\\S]*?<\\/BasePath>").replaceAll("<[\\s\\S]*?>", "");
			endpoint.setBasePath(basePath);
			endpoint.setVirtualHosts(getVirtualhosts(fileName.getPath()));
			proxyEndpoints.add(endpoint);
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return proxyEndpoints;
	}
	
	private static String processRegex(String file, String regex) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
		String line = null;
		StringBuilder outString = new StringBuilder();
		try {
			while( ( line = reader.readLine() ) != null ) {
				Pattern regexPattern = Pattern.compile(regex);
				Matcher matcher = regexPattern.matcher(line);
				while (matcher.find()) {
					int start = matcher.start(0);
					int end = matcher.end(0);
					outString.append( line.substring(start, end) );
				}
			}
			reader.close();
			return (outString.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return "";
	}

	public static List<String> getVirtualhosts(String file) throws IOException {
		String data =  readNames(file, "//VirtualHost");
		List<String> virtualHosts = Arrays.asList(data.trim().split("\n"));
//		BufferedReader reader = new BufferedReader( new FileReader (file));
//		
//		String line = null;
//		StringBuilder cacheString = new StringBuilder();
//		String ls = System.getProperty("line.separator");
//		try {
//			while( ( line = reader.readLine() ) != null ) {
//				String cachePatternString = "<VirtualHost>[\\s\\S]*?<\\/VirtualHost>";
//				Pattern cachePattern = Pattern.compile(cachePatternString);
//				Matcher cacheMatcher = cachePattern.matcher(line);
//				while (cacheMatcher.find()) {
//					int start = cacheMatcher.start(0);
//					int end = cacheMatcher.end(0);
//					cacheString.append( line.substring(start, end) );
//					cacheString.append( ls );
//				}
//				virtualHosts.add(cacheString.toString().replaceAll("<[\\s\\S]*?>", ""));
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
		return virtualHosts;
	}

	public static void clean(String rootPath) {
		long startTime = System.nanoTime();

		String proxiesPath = rootPath + "proxies/";
		String targetsPath = rootPath + "targets/";
		String policiesPath = rootPath + "policies/";
		String resourcesPath = rootPath +"resources/";
		String data="";
		String list = "";

		File[] fList = finder(policiesPath);
		if(fList != null)
		for (File file : fList) {
			try {
				//System.out.println("file: " + file.getName());
				StringTokenizer tokenString = new StringTokenizer(file.getName(), ".");
				list = list + tokenString.nextToken()+"\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fList = finder(proxiesPath);
		if(fList != null)
		for (File file : fList) {
			try {
				System.out.println("file: " + file.getName());//.getCanonicalPath());
				data = data + readNames(file.getCanonicalPath(), "//Name");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		fList = finder(targetsPath);
		if(fList != null)
		for (File file : fList) {
			try {
				System.out.println("file: " + file.getCanonicalPath());
				data = data + readNames(file.getCanonicalPath(), "//Name");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			data = removeDuplicates(data);
			String invalidPolicies = "";
			System.out.println("==================================================================");
			System.out.println("Policies not used: ");
			System.out.println("==================================================================");
			StringTokenizer tokenString = new StringTokenizer(list, "\n");
			while (tokenString.hasMoreElements()) {
				String tk = (String) tokenString.nextElement();
				if(!data.contains(tk))
					invalidPolicies = invalidPolicies + policiesPath + tk +".xml\n";	
			}
			System.out.println(invalidPolicies);
			String disablePolicies = getDisabledPolicies(data, policiesPath);
			System.out.println("==================================================================");
			System.out.println("Policies disabled: ");
			System.out.println("==================================================================");
			System.out.println(disablePolicies);
			String invalidResources = getInvalidResources(invalidPolicies, resourcesPath);
			System.out.println("==================================================================");
			System.out.println("invalid Resources : ");
			System.out.println("==================================================================");
			System.out.println(invalidResources);
			System.out.println("==================================================================");
			System.out.println("removed files : ");
			System.out.println("==================================================================");
			System.out.println(removeUnused(invalidPolicies+invalidResources));
		} catch (Exception e) {
			e.printStackTrace();
		}
		long stopTime = System.nanoTime();
		System.out.println((stopTime - startTime)/1000000);
	}

	public static String readFile(String file) throws IOException {
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

	public static File[] finder( String dirName){
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".xml"); }
		} );
	}

	public static String readNames(String fileName ,String xpath){
		String filesInXML= "";
		try {
			String data = readFile(fileName);
			Document doc = getDoc(data);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				String tmp = nNode.getTextContent();
				if(!filesInXML.contains(tmp))
					filesInXML =filesInXML +"\n"+ tmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesInXML;
	}

	public static Document getDoc(String content){
		try{
			DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input =  new ByteArrayInputStream(content.getBytes("UTF-8"));
			Document doc = builder.parse(input);
			return doc;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String removeDuplicates(String content){
		String list="";
		StringTokenizer tokenString = new StringTokenizer(content, "\n");
		while (tokenString.hasMoreElements()) {
			String tk = (String) tokenString.nextElement();
			if(!list.contains(tk))
				list = list + tk + "\n";	
		}
		return list;
	}

	public static String getDisabledPolicies(String content, String path){
		String list = "";
		StringTokenizer tokenString = new StringTokenizer(content, "\n");
		while (tokenString.hasMoreElements()) {
			String tk = (String) tokenString.nextElement();
			try {
				String fileContent = readFile(path + tk + ".xml");
				Document doc = getDoc(fileContent);
				String root = doc.getDocumentElement().getNodeName();
				String xpath = "/" + root + "/@enabled";
				XPath xPath =  XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile(xpath ).evaluate(doc, XPathConstants.NODESET);
				if(nodeList.getLength()>0 && nodeList.item(0).getTextContent().equals("false")) {
					list = list + tk +"\n";
				}
				xpath ="//ResourceURL";
				nodeList = (NodeList) xPath.compile(xpath ).evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					StringTokenizer fileName = new StringTokenizer(nodeList.item(i).getTextContent(), "//");
					if(fileName.nextToken().equals("xsl:"))
						resources = resources + fileName.nextToken() +"\n";
					else 
						resources = resources + fileName.nextToken() +"\n";
				}
			} catch (Exception e) {
				System.out.println(tk);
				e.printStackTrace();
			}	
		}
		return list;
	}

	public static String getInvalidResources(String content, String path){
		String list = "";
		StringTokenizer tokenString = new StringTokenizer(content, "\n");
		while (tokenString.hasMoreElements()) {
			String tk = (String) tokenString.nextElement();
			try {
				String fileContent = readFile( tk );
				Document doc = getDoc(fileContent);
				String xpath = "//ResourceURL";
				XPath xPath =  XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile(xpath ).evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					
					StringTokenizer fileName = new StringTokenizer(nodeList.item(i).getTextContent(), "//");
					if(fileName.nextToken().equals("xsl:")){
						String toDelete = fileName.nextToken();
						if(!resources.contains(toDelete))
							list = list +path+"xsl/"+ toDelete +"\n";
					}
					else {
						String toDelete = fileName.nextToken();
						if(!resources.contains(toDelete))
							list = list +path+"jsc/"+ toDelete +"\n";
					}
				}
			} catch (Exception e) {
				System.out.println(tk);
				e.printStackTrace();
			}	
		}
		return list;
	}

	public static void saveData(String content, String fileName){
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String removeUnused(String list){
		String removedFiles = "";
		StringTokenizer tokenString = new StringTokenizer(list, "\n");
		while (tokenString.hasMoreElements()) {
			String tk = (String) tokenString.nextElement();
				if(deleteFile(tk)){
					removedFiles = removedFiles + tk + System.lineSeparator();
				}
		}
		return removedFiles;
	}
	
	public static boolean deleteFile(String fileName){
		//System.out.println("Delete : " + fileName);
		try{
			File file = new File(fileName);
			if (file.exists()) {
				return(file.delete());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}


}
