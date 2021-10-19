package com.itorix.apiwiz.devstudio.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;

@Component
public class IntegrationsDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private BaseRepository baseRepository;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	public void updateIntegratoin(Integration integration) {
		List<Integration> dbIntegrationList = getIntegration(integration.getType());
		if (dbIntegrationList != null && dbIntegrationList.size() > 0) {
			Integration dbIntegration = dbIntegrationList.get(0);
			if (integration.getType().equalsIgnoreCase("JFROG"))
				dbIntegration.setJfrogIntegration(integration.getJfrogIntegration());
			else
				dbIntegration.setGoCDIntegration(integration.getGoCDIntegration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public void updateApicIntegratoin(Integration integration) {
		List<Integration> dbIntegrationList = getIntegration(integration.getType());
		if (dbIntegrationList != null && dbIntegrationList.size() > 0) {
			Integration dbIntegration = dbIntegrationList.get(0);
			if (integration.getType().equalsIgnoreCase("APIC"))
				dbIntegration.setApicIntegration(integration.getApicIntegration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public void updateS3Integratoin(Integration integration) {
		List<Integration> dbIntegrationList = getIntegration(integration.getType());
		if (dbIntegrationList != null && dbIntegrationList.size() > 0) {
			Integration dbIntegration = dbIntegrationList.get(0);
			if (integration.getType().equalsIgnoreCase("S3"))
				dbIntegration.setS3Integration(integration.getS3Integration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public void removeIntegratoin(Integration integration) {
		mongoTemplate.remove(integration);
	}

	public void removeIntegratoin(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		mongoTemplate.remove(query, Integration.class);
	}

	public List<Integration> getIntegration(String type) {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is(type));
		List<Integration> dbIntegrations = mongoTemplate.find(query, Integration.class);
		return dbIntegrations;
	}

	public List<Integration> getGitIntegration() {
		List<Integration> integrations = getIntegration("GIT");
		if (integrations != null)
			return integrations;
		else
			return new ArrayList<Integration>();
	}

	public Integration getGitIntegration(String type, String usage) {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is(type));
		query.addCriteria(Criteria.where("gitIntegration.userType").is(usage));
		Integration dbIntegration = mongoTemplate.findOne(query, Integration.class);
		if (dbIntegration != null)
			return dbIntegration;
		return null;
	}

	public void updateGITIntegratoin(Integration integration) {
		Integration dbIntegration = getGitIntegration(integration.getType(),
				integration.getGitIntegration().getUserType());
		if (dbIntegration != null) {
			dbIntegration.setGitIntegration(integration.getGitIntegration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public void removeGitIntegration(String id) {
		removeIntegratoin(id);
	}

	public void updateWorkspaceIntegration(WorkspaceIntegration workspaceIntegration) {
		mongoTemplate.save(workspaceIntegration);
	}

	public List<WorkspaceIntegration> getWorkspaceIntegration() {
		return mongoTemplate.findAll(WorkspaceIntegration.class);
	}

	public void removeWorkspaceIntegration(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		mongoTemplate.remove(query, WorkspaceIntegration.class);
	}

	public Object getMetaData() {
		Query query = new Query().addCriteria(Criteria.where("key").is("workspace"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null)
			return metaData.getMetadata();
		return null;
	}

	public Integration getJfrogIntegration() {
		List<Integration> dbIntegrationList = getIntegration("JFROG");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			return dbIntegrationList.get(0);
		return integration;
	}

	public Integration getS3Integration() {
		List<Integration> dbIntegrationList = getIntegration("S3");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			return dbIntegrationList.get(0);
		return integration;
	}

	public void updateJfrogIntegratoin(Integration integration) {
		List<Integration> dbIntegrationList = getIntegration("JFROG");
		if (dbIntegrationList != null && dbIntegrationList.size() > 0) {
			Integration dbIntegration = dbIntegrationList.get(0);
			dbIntegration.setJfrogIntegration(integration.getJfrogIntegration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public void removeJfrogIntegration() {
		Integration integration = getIntegration("JFROG").get(0);
		if (integration != null)
			removeIntegratoin(integration);
	}

	public List<Integration> getGitLabIntegration() {
		List<Integration> integrations = getIntegration("GITLAB");
		if (integrations != null)
			return integrations;
		else
			return new ArrayList<Integration>();
	}

	public void removeGitLabIntegration() {
		Integration integration = getIntegration("GITLAB").get(0);
		if (integration != null)
			removeIntegratoin(integration);
	}

	public List<Integration> getBitBucketIntegration() {
		List<Integration> integrations = getIntegration("BITBUCKET");
		if (integrations != null)
			return integrations;
		else
			return new ArrayList<Integration>();
	}

	public void removeBitBucketIntegration() {
		Integration integration = getIntegration("BITBUCKET").get(0);
		if (integration != null)
			removeIntegratoin(integration);
	}

	public List<Integration> getCodeconnectIntegration() {
		List<Integration> integrations = getIntegration("CODECOMMIT");
		if (integrations != null)
			return integrations;
		else
			return new ArrayList<Integration>();
	}

	public void removeCodeconnectIntegration() {
		Integration integration = getIntegration("CODECOMMIT").get(0);
		if (integration != null)
			removeIntegratoin(integration);
	}

	public String getGoServerVersion(GoCDIntegration goCDIntegration)
			throws JsonMappingException, JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/vnd.go.cd.v1+json");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		String url = goCDIntegration.getHostURL() + "/go/api/version";
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonResponse = mapper.readTree(responseEntity.getBody());
		return jsonResponse.get("version").asText();
	}
}
