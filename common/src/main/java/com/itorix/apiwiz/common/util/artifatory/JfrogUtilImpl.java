package com.itorix.apiwiz.common.util.artifatory;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClient;
import org.jfrog.artifactory.client.model.LightweightRepository;
import org.jfrog.artifactory.client.model.Repository;
//import org.jfrog.artifactory.client.model.repository.settings.impl.GenericRepositorySettingsImpl;
import static org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl.LOCAL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.common.properties.ApplicationProperties;

import groovyx.net.http.HttpResponseException;

@Component
public class JfrogUtilImpl{

	private static final Logger log = LoggerFactory.getLogger(JfrogUtilImpl.class);

	@Autowired
	private ApplicationProperties applicationProperties;

	private String artifactoryHost;

	private String artifactoryName ;

	private String username ;
	private String userpassword ;


	@PostConstruct
	private void initValues(){
		StringBuilder host = new StringBuilder();
		host.append(applicationProperties.getJfrogHost());
		System.out.println();
		if(applicationProperties.getJfrogPort() != null && !applicationProperties.getJfrogPort().equals("0"))
			host.append(":" + applicationProperties.getJfrogPort());
		host.append("/artifactory/");
		
		artifactoryHost = host.toString();
		artifactoryName = applicationProperties.getArtifactoryName();
		username = applicationProperties.getJfrogUserName();
		userpassword = applicationProperties.getJfrogPassword();
	}

	public JSONObject uploadFiles(String[] fileArr, String artifactoryResourcePath) throws Exception {
		JSONArray arr = new JSONArray();
		JSONObject files_list = new JSONObject();
		for (int i = 0; i < fileArr.length; i++) {
			JSONObject obj = uploadFiles(fileArr[i], artifactoryName, artifactoryHost,artifactoryResourcePath, username, userpassword);
			arr.put(obj);
		}
		files_list.put("files", arr);
		return files_list;
	}

	public JSONObject uploadFiles(String file, String artifactoryResourcePath) throws Exception {
		JSONObject obj = uploadFiles(file, artifactoryName, artifactoryHost,artifactoryResourcePath, username, userpassword);
		return obj;
	}

	public JSONObject uploadFiles(String[] fileArr, String repoName, String artifactoryBasePath,
			String artifactoryResourcePath, String userName, String password) throws Exception {
		JSONArray arr = new JSONArray();
		JSONObject files_list = new JSONObject();
		for (int i = 0; i < fileArr.length; i++) {
			JSONObject obj = uploadFiles(fileArr[i], repoName, artifactoryHost,artifactoryResourcePath, username, password);
			arr.put(obj);
		}
		files_list.put("files", arr);
		return files_list;
	}

	public JSONObject uploadFiles(String file, String repoName, String artifactoryBasePath,
			String artifactoryResourcePath, String userName, String password) throws Exception {
		JSONObject obj = new JSONObject();
		try {

			File files;
			Artifactory artifactory = ArtifactoryClient.create(artifactoryHost, userName, password);
			files= new File(file);
			String newFilePath = artifactoryResourcePath + "/" + files.getName();
			org.jfrog.artifactory.client.model.File result = artifactory.repository(repoName)
					.upload(newFilePath, files).doUpload();
			try {
				obj.put("filename", result.getName());
				obj.put("downloadURI", replaceHost(result.getDownloadUri()));
				obj.put("sha1", result.getChecksums().getSha1());
				obj.put("md5", result.getChecksums().getMd5());
				return obj;
			} catch (Exception e) {
				obj.put("logtrail", "Unable to Upload file.");
				throw (e);
			}

		} catch (Exception e) {
			e.printStackTrace();
			obj.put("logtrail", "Unable to connect to artifactory");
			throw(e);
		}

	}

	private  String replaceHost(String URL){
		try{
			
			StringBuilder host = new StringBuilder();
			host.append(applicationProperties.getJfrogHost());
			if(applicationProperties.getJfrogPort() != null && !applicationProperties.getJfrogPort().equals("0"))
				host.append(":" + applicationProperties.getJfrogPort());
			String[] tokens = URL.split("/");
			//String newHost = applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort();
			StringBuilder newUrl = new StringBuilder();
			newUrl.append( host.toString() + "/");
			for (int x=3; x<tokens.length; x++)
				if(x==tokens.length-1)
					newUrl.append(tokens[x]);
				else
					newUrl.append(tokens[x] + "/");
			return newUrl.toString();
		}catch(Exception e){
			return URL;
		}
	}


	public JSONObject uploadFiles(InputStream file, String artifactoryResourcePath) throws Exception {
		JSONObject obj = new JSONObject();
		System.out.println("artifactoryHost : "+ artifactoryHost);
		System.out.println("artifactoryResourcePath : "+ artifactoryResourcePath);
		System.out.println("artifactoryName : "+ artifactoryName);
		
		obj = uploadFiles(file, artifactoryName, artifactoryHost,artifactoryResourcePath, username, userpassword);
		return obj;
	}

	public JSONObject uploadFiles(InputStream file, String repoName, String artifactoryBasePath,
			String artifactoryResourcePath, String userName, String password) throws Exception {
		JSONObject obj = new JSONObject();
		try {

			Artifactory artifactory = ArtifactoryClient.create(artifactoryBasePath, userName, password);
			org.jfrog.artifactory.client.model.File result = artifactory.repository(repoName)
					.upload(artifactoryResourcePath, file).doUpload();
			try {
				obj.put("filename", result.getName());
				obj.put("downloadURI", replaceHost(result.getDownloadUri()));
				obj.put("sha1", result.getChecksums().getSha1());
				obj.put("md5", result.getChecksums().getMd5());
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
				obj.put("logtrail", "Unable to Upload file.");
				throw (e);
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("error while uploading file to jfrog", e);
			obj.put("logtrail", "Unable to connect to artifactory");
			throw(e);
		}

	}

	public JSONObject deleteFile(String filePath) throws Exception {
		JSONObject obj = new JSONObject();
		obj = deleteFile(filePath, artifactoryName, artifactoryHost, username, userpassword);
		return obj;
	}

	public String deleteFileIgnore404(String filePath) throws Exception {
		try {
			Artifactory artifactory = ArtifactoryClient.create(artifactoryHost, username, userpassword);
				return artifactory.repository(artifactoryName).delete(filePath);
		} catch (Exception e) {
			if (e instanceof HttpResponseException && ((HttpResponseException) e).getStatusCode() == 404) {
				return null;
			}
			throw e;
		}
	}


	public JSONObject deleteFile(String filePath, String repoName, String artifactoryBasePath,
			String userName, String password) throws Exception {
		JSONObject obj = new JSONObject();
		try {
			Artifactory artifactory = ArtifactoryClient.create(artifactoryBasePath, userName, password);
			try {
				String result = artifactory.repository(repoName).delete(filePath);
				return obj;
			} catch (Exception e) {
				obj.put("logtrail", "Unable to delete file.");
				throw (e);
			}
		} catch (Exception e) {
			log.error("error while connecting  to jfrog", e);
			obj.put("logtrail", "Unable to connect to artifactory");
			throw(e);
		}
	}

	private static String createNewRepository(Artifactory artifactory, String repoName) {
		if (artifactory == null || StringUtils.isEmpty(repoName)){
			throw new IllegalArgumentException("Arguments passed to createNewRepository are not valid");
		}
		List<LightweightRepository> repoList = artifactory.repositories().list(LOCAL);
		Set<String> repoNamesList = repoList.stream()
				.map(LightweightRepository::getKey)
				.collect(Collectors.toSet());
		String creationResult = null;
		if ( repoNamesList != null && !(repoNamesList.contains(repoName)) ){
			//		    GenericRepositorySettingsImpl settings = new GenericRepositorySettingsImpl();
			Repository repository = artifactory.repositories()
					.builders()
					.localRepositoryBuilder()
					.key(repoName)
					.description("new example local repository")
					//		        .repositorySettings(settings)
					.build();
			creationResult = artifactory.repositories().create(1, repository);
		}
		return creationResult;
	}

	private Map<String, String> getartifactoryDetails(String path){
		return null;
	}


}
