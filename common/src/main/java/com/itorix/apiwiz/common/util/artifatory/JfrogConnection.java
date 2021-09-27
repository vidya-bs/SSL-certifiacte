package com.itorix.apiwiz.common.util.artifatory;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

@Component
public class JfrogConnection {

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private MongoTemplate mongoTemplate;

	public JfrogIntegration getJfrogIntegration() {
		JfrogIntegration jfrogIntegration = getIntegration().getJfrogIntegration();
		if (jfrogIntegration != null) {
			String decryptedPassword = "";
			try {
				RSAEncryption rSAEncryption = new RSAEncryption();
				decryptedPassword = rSAEncryption.decryptText(jfrogIntegration.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
			jfrogIntegration.setPassword(decryptedPassword);
		} else {
			String hostURL = applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort();
			String userName = applicationProperties.getJfrogUserName();
			String password = applicationProperties.getJfrogPassword();
			jfrogIntegration = new JfrogIntegration();
			jfrogIntegration.setHostURL(hostURL);
			jfrogIntegration.setUsername(userName);
			jfrogIntegration.setPassword(password);
		}
		return jfrogIntegration;
	}

	private Integration getIntegration() {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("JFROG"));
		List<Integration> dbIntegrationList = mongoTemplate.find(query, Integration.class);
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			return dbIntegrationList.get(0);
		return integration;
	}

	public Resource getArtifact(JfrogIntegration jfrogIntegration, String url) {
		StringBuilder host = new StringBuilder();
		host.append(jfrogIntegration.getHostURL());
		host.append("/"+url);
		RestTemplate restTemplate = new RestTemplate();
		
		url = URLDecoder.decode(host.toString());
		return restTemplate.getForObject( url, Resource.class);
	}
}
