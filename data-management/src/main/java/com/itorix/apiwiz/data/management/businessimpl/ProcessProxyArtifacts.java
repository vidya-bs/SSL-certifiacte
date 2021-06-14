package com.itorix.apiwiz.data.management.businessimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.util.http.HttpErrorHandler;

@Service
@SuppressWarnings("unchecked")
public class ProcessProxyArtifacts {
	//Logger logger = Logger.getLogger(ProcessProxyArtifacts.class);

	private static String host="localhost";
	private static String port = "8092";
	private static String uri="http://#host#:#port#/v1/buildconfig/proxies/#proxy#/proxyartifacts";
	//	private static String uri="http://#host#:#port#/UserManagement/v1/buildconfig/proxies/#proxy#/proxyartifacts";
	private static String proxyPath= "";

//	public static void main(String[] args) {
//		host = args[0].trim();
//		port = args[1].trim();
//		String proxy = args[2].trim();
//		proxyPath = args[3].trim();
//		uri = uri.replaceAll("#proxy#", proxy);
//		uri = uri.replaceAll("#host#", host);
//		uri = uri.replaceAll("#port#", port);
//		try{
//			updateProxyArtifacts(processArtifacts(proxyPath));
//			//CleanUnused.clean(proxyPath + System.getProperty("file.separator"));
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}

//	private static String updateProxyArtifacts(ProxyArtifacts proxyArtifacts) {
//		RestTemplate restTemplate = new RestTemplate();
//		ResponseEntity<String> response;
//		restTemplate.setErrorHandler(new HttpErrorHandler());
//		try{
//			//response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(proxyArtifacts), String.class);
//			response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(proxyArtifacts), new ParameterizedTypeReference<String>() {});
//		}catch (Exception e){
//			throw e;
//		}
//
//		return response.toString();
//	}

	@SuppressWarnings("rawtypes")
	public  static ProxyArtifacts processArtifacts(String path){
		String policiesPath = path + System.getProperty("file.separator") + "policies";
		String proxyPath = path + System.getProperty("file.separator") + "proxies";
		String targetPath = path + System.getProperty("file.separator") + "targets";
		File[] fList = finder(policiesPath);
		Set<String> kvmResources = new HashSet<String>();
		Set<String> cacheResources = new HashSet<String>();
		List<String> targets = new ArrayList<String>();
		for (File file : fList) {
			try {
				String resource = (processRegex(file.getPath(),"mapIdentifier=\"[\\s\\S]*?\"").replaceAll("mapIdentifier=\"", "")).replaceAll("\"", "");
				if(!resource.isEmpty())
					kvmResources.add(resource);
				resource = processRegex(file.getPath(),"<CacheResource>[\\s\\S]*?<\\/CacheResource>").replaceAll("<[\\s\\S]*?>", "");
				if(!resource.isEmpty())
					cacheResources.add(resource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			targets = getTargets(targetPath);
		} catch (IOException e) {
		}
		ProxyArtifacts data = new ProxyArtifacts();
		data.setKvms((List)Arrays.asList(kvmResources.toArray()));
		data.setCaches((List)Arrays.asList(cacheResources.toArray()));
		data.setTargetServers(targets);
		return data;
	}

	private static File[] finder( String dirName){
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".xml"); }
		} );
	}

	private static List<String> getTargets(String file) throws IOException {
		File[] fList = finder(file);
		List<String> targetList = new ArrayList<String>();
		if(fList != null && fList.length > 0){
			for(File fileName: fList){
				String targetEndpoint = processRegex(fileName.getPath(),"<Server name=[\\s\\S]*?>");
				String[] ary = targetEndpoint.split("<Server name=");
				for(int i = 0; i< ary.length ; i++)
					if(!ary[i].replaceAll(">", "").trim().isEmpty())
						targetList.add(ary[i].replaceAll(">", "").replaceAll("\"", ""));
			}
		}
		return targetList;
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

}