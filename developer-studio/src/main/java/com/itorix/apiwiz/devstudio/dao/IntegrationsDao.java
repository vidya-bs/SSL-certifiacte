package com.itorix.apiwiz.devstudio.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.devstudio.model.metricsMetadata.BuildGovernance;
import com.itorix.apiwiz.devstudio.model.metricsMetadata.MetricMetadata;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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

import java.util.ArrayList;
import java.util.List;
@Slf4j
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
			log.debug("Updating Integration");
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
			log.debug("Updating APIC Integration");
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
			log.debug("Updating S3 Integration");
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
			log.debug("Updating GIT Integration");
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
		if(workspaceIntegration.getPropertyKey().equalsIgnoreCase("apiwiz.codecoverage.threshold.rating")){
			List<MetricMetadata> metricMetadataList = mongoTemplate.findAll(MetricMetadata.class);
			if(metricMetadataList.size()>0){
				metricMetadataList.forEach(metricMetadata -> {
					BuildGovernance buildGovernance = metricMetadata.getBuildGovernance()!=null ?
							metricMetadata.getBuildGovernance() : null;
					if(buildGovernance!=null){
						int percentage = buildGovernance.getAvgCodeCoverage();
						String [] values = workspaceIntegration.getPropertyValue().split("\\|");
						for (String value : values) {
							String[] range = value.split(":");
							int leftRange = Integer.parseInt(range[0].split("-")[0]);
							int rightRange = Integer.parseInt(range[0].split("-")[1]);
							if (percentage >= leftRange && percentage <= rightRange) {
								int currentRating = Integer.parseInt(range[1]);
								buildGovernance.setMaturity(currentRating);
								break;
							}
						}
						metricMetadata.setBuildGovernance(buildGovernance);
						mongoTemplate.save(metricMetadata);
					}
				});
			}
		}
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
			log.debug("Updating Jfrog Integration");
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

	public List<Integration> getAzureDevopsIntegration() {
		List<Integration> integrations = getIntegration("AZUREDEVOPS");
		if (integrations != null)
			return integrations;
		else
			return new ArrayList<Integration>();
	}

	public void removeAzureDevopsIntegrationIntegration() {
		Integration integration = getIntegration("AZUREDEVOPS").get(0);
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
		log.debug("Making a call to {}", url);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonResponse = mapper.readTree(responseEntity.getBody());
		return jsonResponse.get("version").asText();
	}

	public void updateGcsIntegration(Integration integration) {
		Integration dbIntegration = getGcsIntegration();
		if (dbIntegration != null) {
			dbIntegration.setGcsIntegration(integration.getGcsIntegration());
			baseRepository.save(dbIntegration);
		} else {
			baseRepository.save(integration);
		}
	}

	public Integration getGcsIntegration() {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("GCS"));
		Integration dbIntegration = mongoTemplate.findOne(query, Integration.class);
		if (dbIntegration != null)
			return dbIntegration;
		return null;
	}

	public void removeGcsIntegration() {
		Integration integration = getGcsIntegration();
		if (integration != null) {
			mongoTemplate.remove(integration);
		}
	}

	public void deleteMongodbDatabaseIntegratoin(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
		mongoTemplate.remove(query, MongoDBConfiguration.class);
	}

	public void deleteMysqlDatabaseIntegratoin(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
		mongoTemplate.remove(query, MySQLConfiguration.class);
	}

	public void deletePostgresqlDatabaseIntegratoin(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
		mongoTemplate.remove(query, PostgreSQLConfiguration.class);
	}


	public String createMongoDbDatabaseConfig(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
		if(mongoDBConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name is required!"), "DatabaseConfiguration-1000");
		}
		updateUserDetails(null, mongoDBConfiguration);
		mongoDBConfiguration = baseRepository.save(mongoDBConfiguration);
		log.info("Database Configuration created, id - {}", mongoDBConfiguration.getId());
		return mongoDBConfiguration.getId();
	}

	public String createMySqlDatabaseConfig(MySQLConfiguration mySQLConfiguration) throws ItorixException {
		if(mySQLConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name is required!"), "DatabaseConfiguration-1000");
		}
		updateUserDetails(null, mySQLConfiguration);
		mySQLConfiguration = baseRepository.save(mySQLConfiguration);
		log.info("Database Configuration created, id - {}", mySQLConfiguration.getId());
		return mySQLConfiguration.getId();
	}

	public String createPostgreSqlDatabaseConfig(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
		if(postgreSQLConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name is required!"), "DatabaseConfiguration-1000");
		}
		updateUserDetails(null, postgreSQLConfiguration);
		postgreSQLConfiguration = baseRepository.save(postgreSQLConfiguration);
		log.info("Database Configuration created, id - {}", postgreSQLConfiguration.getId());
		return postgreSQLConfiguration.getId();
	}

	public void updateMongoDbDatabaseIntegration(MongoDBConfiguration mongoDBConfiguration, String id) throws ItorixException {
		if(mongoDBConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name cannot be empty!"), "DatabaseConfiguration-1000");
		}
		MongoDBConfiguration oldMongoDBConfiguration = baseRepository.findById(id, MongoDBConfiguration.class);
		updateUserDetails(oldMongoDBConfiguration, mongoDBConfiguration);
		mongoTemplate.save(mongoDBConfiguration);
	}

	public void updateMySqlDatabaseIntegration(MySQLConfiguration mySQLConfiguration, String id) throws ItorixException {
		if(mySQLConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name cannot be empty!"), "DatabaseConfiguration-1000");
		}
		MySQLConfiguration oldMySQLConfiguration = baseRepository.findById(id, MySQLConfiguration.class);
		updateUserDetails(oldMySQLConfiguration, mySQLConfiguration);
		mongoTemplate.save(mySQLConfiguration);
	}


	public void updatePostgreSqlDatabaseIntegration(PostgreSQLConfiguration postgreSQLConfiguration, String id) throws ItorixException {
		if(postgreSQLConfiguration.getName() == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connector name cannot be empty!"), "DatabaseConfiguration-1000");
		}
		PostgreSQLConfiguration olddatabaseConfiguration = baseRepository.findById(id, PostgreSQLConfiguration.class);
		updateUserDetails(olddatabaseConfiguration, postgreSQLConfiguration);
		mongoTemplate.save(postgreSQLConfiguration);
	}

    public MongoDBConfiguration getMongoDbDatabaseIntegrationById(String id)  {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
        return mongoTemplate.findOne(query, MongoDBConfiguration.class);
    }

    public MySQLConfiguration getMysqlDatabaseIntegrationById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
        return mongoTemplate.findOne(query, MySQLConfiguration.class);
    }

    public PostgreSQLConfiguration getPostgresqlDatabaseIntegrationById(String id)  {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));
        return mongoTemplate.findOne(query, PostgreSQLConfiguration.class);
    }

    public List<MongoDBConfiguration> getAllMongoDbIntegrations() {
        return mongoTemplate.findAll(MongoDBConfiguration.class);
    }
    public List<MySQLConfiguration> getAllMysqlIntegrations() {
        return mongoTemplate.findAll(MySQLConfiguration.class);
    }
    public List<PostgreSQLConfiguration> getAllPostgresqlIntegrations() {
        return mongoTemplate.findAll(PostgreSQLConfiguration.class);
    }


	private void updateUserDetails(MongoDBConfiguration oldMongoDBConfiguration, MongoDBConfiguration mongoDBConfiguration) {

		String userName = null;
		String userId = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userName = userSession.getUsername();
			userId = userSession.getUserId();
		}catch (Exception e){
			log.error("Error while getting user");
		}

		if(oldMongoDBConfiguration == null) {
			mongoDBConfiguration.setCreatedUserName(userName);
			mongoDBConfiguration.setCreatedBy(userId);
			mongoDBConfiguration.setCts(System.currentTimeMillis());
			return;
		}

		//copying old database configuration data to new database configuration
		mongoDBConfiguration.setId(oldMongoDBConfiguration.getId());
		mongoDBConfiguration.setCreatedUserName(oldMongoDBConfiguration.getCreatedUserName());
		mongoDBConfiguration.setCreatedBy(oldMongoDBConfiguration.getCreatedBy());
		mongoDBConfiguration.setCts(oldMongoDBConfiguration.getCts());

		// setting new database configuration
		mongoDBConfiguration.setModifiedUserName(userName);
		mongoDBConfiguration.setModifiedBy(userId);
		mongoDBConfiguration.setMts(System.currentTimeMillis());
	}

	private void updateUserDetails(MySQLConfiguration oldMySQLConfiguration, MySQLConfiguration mySQLConfiguration) {

		String userName = null;
		String userId = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userName = userSession.getUsername();
			userId = userSession.getUserId();
		}catch (Exception e){
			log.error("Error while getting user");
		}

		if(oldMySQLConfiguration == null) {
			mySQLConfiguration.setCreatedUserName(userName);
			mySQLConfiguration.setCreatedBy(userId);
			mySQLConfiguration.setCts(System.currentTimeMillis());
			return;
		}

		//copying old database configuration data to new database configuration
		mySQLConfiguration.setId(oldMySQLConfiguration.getId());
		mySQLConfiguration.setCreatedUserName(oldMySQLConfiguration.getCreatedUserName());
		mySQLConfiguration.setCreatedBy(oldMySQLConfiguration.getCreatedBy());
		mySQLConfiguration.setCts(oldMySQLConfiguration.getCts());

		// setting new database configuration
		mySQLConfiguration.setModifiedUserName(userName);
		mySQLConfiguration.setModifiedBy(userId);
		mySQLConfiguration.setMts(System.currentTimeMillis());
	}

	private void updateUserDetails(PostgreSQLConfiguration oldPostgreSQLConfiguration, PostgreSQLConfiguration PostgreSQLConfiguration) {

		String userName = null;
		String userId = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userName = userSession.getUsername();
			userId = userSession.getUserId();
		}catch (Exception e){
			log.error("Error while getting user");
		}

		if(oldPostgreSQLConfiguration == null) {
			PostgreSQLConfiguration.setCreatedUserName(userName);
			PostgreSQLConfiguration.setCreatedBy(userId);
			PostgreSQLConfiguration.setCts(System.currentTimeMillis());
			return;
		}

		//copying old database configuration data to new database configuration
		PostgreSQLConfiguration.setId(oldPostgreSQLConfiguration.getId());
		PostgreSQLConfiguration.setCreatedUserName(oldPostgreSQLConfiguration.getCreatedUserName());
		PostgreSQLConfiguration.setCreatedBy(oldPostgreSQLConfiguration.getCreatedBy());
		PostgreSQLConfiguration.setCts(oldPostgreSQLConfiguration.getCts());

		// setting new database configuration
		PostgreSQLConfiguration.setModifiedUserName(userName);
		PostgreSQLConfiguration.setModifiedBy(userId);
		PostgreSQLConfiguration.setMts(System.currentTimeMillis());
	}

	public List<MongoDBConfiguration> getMongoDbDatabaseIntegrationsMetadata() {
		Query query = new Query();
		query.fields().include("id", "name", "description", "cts", "mts", "createdUserName", "modifiedUserName");
		return mongoTemplate.find(query, MongoDBConfiguration.class);
	}

	public List<MySQLConfiguration> getMySqlIntegrationsMetadata() {
		Query query = new Query();
		query.fields().include("id", "name", "description", "cts", "mts", "createdUserName", "modifiedUserName");
		return mongoTemplate.find(query, MySQLConfiguration.class);
	}

	public List<PostgreSQLConfiguration> getPostgreSqlIntegrationsMetadata() {
		Query query = new Query();
		query.fields().include("id", "name", "description", "cts", "mts", "createdUserName", "modifiedUserName");
		return mongoTemplate.find(query, PostgreSQLConfiguration.class);
	}
}
