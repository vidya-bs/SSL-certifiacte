package com.itorix.apiwiz.common.util.artifatory;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import groovyx.net.http.HttpResponseException;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClient;
import org.jfrog.artifactory.client.model.LightweightRepository;
import org.jfrog.artifactory.client.model.Repository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl.LOCAL;

@Component("Jfrog")
public class JfrogUtilImpl extends StorageIntegration {

	private static final Logger log = LoggerFactory.getLogger(JfrogUtilImpl.class);

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private JfrogConnection jfrogConnection;

	private String artifactoryHost;

	private String artifactoryName;

	private String username;
	private String userpassword;

	@Value("${itorix.core.application.url}")
	private String host;

	@Value("${server.contextPath}")
	private String context;

	@PostConstruct
	private void initValues() {
		StringBuilder host = new StringBuilder();
		host.append(applicationProperties.getJfrogHost());
		// log.info();
		if (applicationProperties.getJfrogPort() != null && !applicationProperties.getJfrogPort().equals("0"))
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
			JSONObject obj = uploadFiles(fileArr[i], artifactoryName, artifactoryHost, artifactoryResourcePath,
					username, userpassword);
			arr.put(obj);
		}
		files_list.put("files", arr);
		return files_list;
	}

	public JSONObject uploadFiles(String file, String artifactoryResourcePath) throws Exception {
		JSONObject obj = uploadFiles(file, artifactoryName, artifactoryHost, artifactoryResourcePath, username,
				userpassword);
		return obj;
	}

	public JSONObject uploadFiles(String[] fileArr, String repoName, String artifactoryBasePath,
			String artifactoryResourcePath, String userName, String password) throws Exception {
		JSONArray arr = new JSONArray();
		JSONObject files_list = new JSONObject();
		for (int i = 0; i < fileArr.length; i++) {
			JSONObject obj = uploadFiles(fileArr[i], repoName, artifactoryHost, artifactoryResourcePath, username,
					password);
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
			files = new File(file);
			String newFilePath = artifactoryResourcePath + "/" + files.getName();
			org.jfrog.artifactory.client.model.File result = artifactory.repository(repoName).upload(newFilePath, files)
					.doUpload();
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
			log.error("Exception occurred", e);
			obj.put("logtrail", "Unable to connect to artifactory");
			throw (e);
		}
	}

	private String replaceHost(String URL) {
		try {

			StringBuilder new_host = new StringBuilder();
			new_host.append(host);
			new_host.append(context + "/v1/download");
			String[] tokens = URL.split("/");
			// String newHost = applicationProperties.getJfrogHost() + ":" +
			// applicationProperties.getJfrogPort();
			StringBuilder newUrl = new StringBuilder();
			newUrl.append(new_host.toString() + "/");
			for (int x = 3; x < tokens.length; x++)
				if (x == tokens.length - 1)
					newUrl.append(tokens[x]);
				else
					newUrl.append(tokens[x] + "/");
			newUrl.append("?type=jfrog");
			return newUrl.toString();
		} catch (Exception e) {
			return URL;
		}
	}

	private String replaceHost_bak(String URL) {
		try {

			StringBuilder host = new StringBuilder();
			host.append(applicationProperties.getJfrogHost());
			if (applicationProperties.getJfrogPort() != null && !applicationProperties.getJfrogPort().equals("0"))
				host.append(":" + applicationProperties.getJfrogPort());
			String[] tokens = URL.split("/");
			// String newHost = applicationProperties.getJfrogHost() + ":" +
			// applicationProperties.getJfrogPort();
			StringBuilder newUrl = new StringBuilder();
			newUrl.append(host.toString() + "/");
			for (int x = 3; x < tokens.length; x++)
				if (x == tokens.length - 1)
					newUrl.append(tokens[x]);
				else
					newUrl.append(tokens[x] + "/");
			return newUrl.toString();
		} catch (Exception e) {
			return URL;
		}
	}

	public JSONObject uploadFiles(InputStream file, String artifactoryResourcePath) throws Exception {
		JSONObject obj = new JSONObject();
		log.info("artifactoryHost : " + artifactoryHost);
		log.info("artifactoryResourcePath : " + artifactoryResourcePath);
		log.info("artifactoryName : " + artifactoryName);

		obj = uploadFiles(file, artifactoryName, artifactoryHost, artifactoryResourcePath, username, userpassword);
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
				log.error("Exception occurred", e);
				obj.put("logtrail", "Unable to Upload file.");
				throw (e);
			}

		} catch (Exception e) {
			log.error("Exception occurred", e);
			log.error("error while uploading file to jfrog", e);
			obj.put("logtrail", "Unable to connect to artifactory");
			throw (e);
		}
	}

/*	public JSONObject deleteFile(String filePath) throws Exception {
		JSONObject obj = new JSONObject();
		obj = deleteFile(filePath, artifactoryName, artifactoryHost, username, userpassword);
		return obj;
	}*/

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

	public JSONObject deleteFile(String filePath, String repoName, String artifactoryBasePath, String userName,
			String password) throws Exception {
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
			throw (e);
		}
	}

	private static String createNewRepository(Artifactory artifactory, String repoName) {
		if (artifactory == null || StringUtils.isEmpty(repoName)) {
			throw new IllegalArgumentException("Arguments passed to createNewRepository are not valid");
		}
		List<LightweightRepository> repoList = artifactory.repositories().list(LOCAL);
		Set<String> repoNamesList = repoList.stream().map(LightweightRepository::getKey).collect(Collectors.toSet());
		String creationResult = null;
		if (repoNamesList != null && !(repoNamesList.contains(repoName))) {
			// GenericRepositorySettingsImpl settings = new
			// GenericRepositorySettingsImpl();
			Repository repository = artifactory.repositories().builders().localRepositoryBuilder().key(repoName)
					.description("new example local repository")
					// .repositorySettings(settings)
					.build();
			creationResult = artifactory.repositories().create(1, repository);
		}
		return creationResult;
	}

	private Map<String, String> getartifactoryDetails(String path) {
		return null;
	}

	@Override
	public String uploadFile(String path, String data) throws Exception {
		String artifactoryBasePath = null;
		String jfrogHost = applicationProperties.getJfrogHost();
		if (jfrogHost.endsWith("/")){
			jfrogHost = jfrogHost.substring(0, jfrogHost.length()-1);
		}
		if (applicationProperties.getJfrogPort() != null && applicationProperties.getJfrogPort().length() > 2) {
			artifactoryBasePath = jfrogHost + ":" + applicationProperties.getJfrogPort();
		}else {
			artifactoryBasePath = applicationProperties.getJfrogHost();
		}
		org.json.JSONObject obj = uploadFiles(data, applicationProperties.getPipelineCodecoverage(), artifactoryBasePath + "/artifactory/", path, applicationProperties.getJfrogUserName(), applicationProperties.getJfrogPassword());
		return obj.getString("downloadURI");
	}

	@Override
	public String uploadFile(String path, InputStream data) throws Exception {
		org.json.JSONObject obj = uploadFiles(data, applicationProperties.getPipelineCodecoverage(), applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort() + "/artifactory/", path, applicationProperties.getJfrogUserName(), applicationProperties.getJfrogPassword());
		return obj.getString("downloadURI");
	}

	@Override
	public InputStream getFile(String path) throws Exception {
		return jfrogConnection.getArtifact(jfrogConnection.getJfrogIntegration(), path).getInputStream();
	}

	@Override
	public void deleteFile(String path) throws Exception {
		deleteFileIgnore404(path);
	}
}
