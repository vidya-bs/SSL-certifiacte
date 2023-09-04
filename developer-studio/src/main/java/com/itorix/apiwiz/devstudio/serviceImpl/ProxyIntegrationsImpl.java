package com.itorix.apiwiz.devstudio.serviceImpl;

import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.gcs.GcsIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogConnection;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.devstudio.service.ProxyIntegrations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

@CrossOrigin
@Slf4j
@RestController
public class ProxyIntegrationsImpl implements ProxyIntegrations {

	@Autowired
	private IntegrationsDao integrationsDao;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private JfrogConnection jfrogConnection;

	@Autowired
	private IntegrationHelper integrationHelper;

	@Value("${server.contextPath}")
	private String context;

	@Override
	public ResponseEntity<?> getGitIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getGitIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateGitIntegraton(String interactionid, String jsessionid,
													   GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GIT");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGitIntegraton(String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getJfrogIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getJfrogIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateJfrogIntegraton(String interactionid, String jsessionid,
												   JfrogIntegration jfrogIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("JFROG");
		integration.setJfrogIntegration(jfrogIntegration);
		integrationsDao.updateJfrogIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeJfrogIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getGitLabIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getGitLabIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateGitLabIntegraton(String interactionid, String jsessionid,
														  GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GITLAB");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGitLabIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getBitBucketIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getBitBucketIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateBitBucketIntegraton(String interactionid, String jsessionid,
															 GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("BITBUCKET");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeBitBucketIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getGocdIntegraton(String interactionid, String jsessionid) throws Exception {
		List<Integration> dbIntegrationList = integrationsDao.getIntegration("GOCD");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			integration = dbIntegrationList.get(0);
		return new ResponseEntity<>(integration, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateGocdIntegraton(String interactionid, String jsessionid,
												  GoCDIntegration goCDIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GOCD");
		integration.setGoCDIntegration(goCDIntegration);
		String version = integrationsDao.getGoServerVersion(goCDIntegration);
		goCDIntegration.setVersion(version);
		integrationsDao.updateIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGocdIntegraton(String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getWorkspaceIntegratons(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getWorkspaceIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getWorkspaceIntegratonsKeys(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getMetaData(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createWorkspaceIntegratons(String interactionid, String jsessionid,
														WorkspaceIntegration workspaceIntegration) throws Exception {
		integrationsDao.updateWorkspaceIntegration(workspaceIntegration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> removeWorkspaceIntegratons(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeWorkspaceIntegration(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getApicIntegratons(String interactionid, String jsessionid) throws Exception {
		List<Integration> dbIntegrationList = integrationsDao.getIntegration("APIC");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			integration = dbIntegrationList.get(0);
		return new ResponseEntity<>(integration.getApicIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createApicIntegratons(String interactionid, String jsessionid,
												   ApicIntegration apicIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("APIC");
		integration.setApicIntegration(apicIntegration);
		integrationsDao.updateApicIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getS3Integratons(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getS3Integration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createS3Integratons(String interactionid, String jsessionid, S3Integration s3Integration)
			throws Exception {
		Integration integration = new Integration();
		integration.setType("S3");
		integration.setS3Integration(s3Integration);
		integrationsDao.updateS3Integratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeS3Integraton(String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public void downloadFile(String interactionid, String jsessionid, String type,
							 HttpServletRequest httpServletRequest, HttpServletResponse response) throws Exception {
		String uri = httpServletRequest.getRequestURI();
		uri = uri.replaceAll(context + "/v1/download/", "");
		StorageIntegration storageIntegration = integrationHelper.getIntegration(type);
		InputStream inputStream = storageIntegration.getFile(uri);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + uri + "\""));
		FileCopyUtils.copy(inputStream, response.getOutputStream());
	}

	@Override
	public ResponseEntity<?> getCodeConnectIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getCodeconnectIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateCodeconnectIntegraton(String interactionid, String jsessionid,
															   GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("CODECOMMIT");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeCodeconnectIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getAzureIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getAzureDevopsIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateAzureIntegraton(String interactionid, String jsessionid,
														 GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("AZUREDEVOPS");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeAzureIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getGcsIntegration(String interactionid, String jsessionid) {
		log.debug("Fetching GCS connector...");
		Integration integration = integrationsDao.getGcsIntegration();
		if (integration != null) {
			log.debug("GCS connector is found");
			return new ResponseEntity<>(integration, HttpStatus.OK);
		}
		log.debug("GCS connector is not found");
		return ResponseEntity.ok("{}");
	}

	@Override
	public ResponseEntity<?> createUpdateGcsIntegration(String interactionid, String jsessionid, String projectId, String bucketName, MultipartFile gcsKey) throws Exception {
		log.debug("Creating/Updating GCS connector...");
		log.debug("checking if key is in proper format");
		if (!getFileExtension(gcsKey.getOriginalFilename()).equals(".json")){
			log.error("Invalid GCS Key");
			throw new ItorixException("Invalid GCS Key", "General-1001");
		}
		Integration integration = new Integration();
		integration.setType("GCS");
		GcsIntegration gcsIntegration = new GcsIntegration();
		gcsIntegration.setProjectId(projectId);
		gcsIntegration.setBucketName(bucketName);
		gcsIntegration.setKey(gcsKey.getBytes());
		integration.setGcsIntegration(gcsIntegration);
		integrationsDao.updateGcsIntegration(integration);
		log.debug("Created/Updated GCS connector");
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGcsIntegration(String interactionid, String jsessionid) {
		log.debug("Removing GCS connector...");
		integrationsDao.removeGcsIntegration();
		log.debug("Removed GCS connector");
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> createMongoDbIntegration(String interactionid, String jsessionid, MongoDBConfiguration mongoDBConfiguration) throws Exception {
		String id = integrationsDao.createMongoDbDatabaseConfig(mongoDBConfiguration);
		return new ResponseEntity<>(id,HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getAllMongoDbIntegration(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getAllMongoDbIntegrations(),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateMongoDbIntegration(String interactionid, String jsessionid, String id, MongoDBConfiguration mongoDBConfiguration) throws Exception {
		integrationsDao.updateMongoDbDatabaseIntegration(mongoDBConfiguration,id);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteMongoDbIntegration(String interactionid, String jsessionid, String id) throws Exception {
		log.info("Deleting the configuration with id {}", id);
		integrationsDao.deleteMongodbDatabaseIntegratoin(id);
		log.info("Deleted successfully!");
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getMongoDbIntegrationById(String interactionid, String jsessionid, String id) throws Exception {
		MongoDBConfiguration mongoDBConfiguration = integrationsDao.getMongoDbDatabaseIntegrationById(id);
		if (mongoDBConfiguration == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(mongoDBConfiguration, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getMongoDbIntegrationsMetadata(String interactionid, String jsessionid) throws Exception {
		List<MongoDBConfiguration> documents = integrationsDao.getMongoDbDatabaseIntegrationsMetadata();
//		if (documents == null || documents.isEmpty()) {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
		return new ResponseEntity<>(documents, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createMySqlIntegration(String interactionid, String jsessionid, MySQLConfiguration mySQLConfiguration) throws Exception {
		String id = integrationsDao.createMySqlDatabaseConfig(mySQLConfiguration);
		return new ResponseEntity<>(id,HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getAllMySqlIntegration(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getAllMysqlIntegrations(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateMySqlIntegration(String interactionid, String jsessionid, String id, MySQLConfiguration mySQLConfiguration) throws Exception {
		integrationsDao.updateMySqlDatabaseIntegration(mySQLConfiguration,id);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteMySqlIntegration(String interactionid, String jsessionid, String id) throws Exception {
		log.info("Deleting the configuration with id {}", id);
		integrationsDao.deleteMysqlDatabaseIntegratoin(id);
		log.info("Deleted successfully!");
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getMySqlIntegrationById(String interactionid, String jsessionid, String id) throws Exception {
		MySQLConfiguration mySQLConfiguration = integrationsDao.getMysqlDatabaseIntegrationById(id);
		if (mySQLConfiguration == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(mySQLConfiguration,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getMySqlIntegrationsMetaData(String interactionid, String jsessionid) throws Exception {
		List<MySQLConfiguration> documents = integrationsDao.getMySqlIntegrationsMetadata();
//		if (documents == null || documents.isEmpty()) {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
		return new ResponseEntity<>(documents, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createPostgreSqlIntegration(String interactionid, String jsessionid, PostgreSQLConfiguration postgreSQLConfiguration) throws Exception {
		String id = integrationsDao.createPostgreSqlDatabaseConfig(postgreSQLConfiguration);
		return new ResponseEntity<>(id,HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getAllPostgreSqlIntegration(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getAllPostgresqlIntegrations(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updatePostgreSqlIntegration(String interactionid, String jsessionid, String id, PostgreSQLConfiguration postgreSQLConfiguration) throws Exception {
		integrationsDao.updatePostgreSqlDatabaseIntegration(postgreSQLConfiguration,id);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deletePostgreSqlIntegration(String interactionid, String jsessionid, String id) throws Exception {
		log.info("Deleting the configuration with id {}", id);
		integrationsDao.deletePostgresqlDatabaseIntegratoin(id);
		log.info("Deleted successfully!");
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getPostgreSqlIntegrationById(String interactionid, String jsessionid, String id) throws Exception {
		PostgreSQLConfiguration postgreSQLConfiguration = integrationsDao.getPostgresqlDatabaseIntegrationById(id);
		if (postgreSQLConfiguration == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(postgreSQLConfiguration, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPostgreSqlIntegrationsMetaData(String interactionid, String jsessionid) throws Exception {
		List<PostgreSQLConfiguration> documents = integrationsDao.getPostgreSqlIntegrationsMetadata();
//		if (documents == null || documents.isEmpty()) {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
		return new ResponseEntity<>(documents, HttpStatus.OK);
	}

	private String getFileExtension(String file) {
		if (file == null) {
			return "";
		}
		int lastIndexOf = file.lastIndexOf(".");
		if (lastIndexOf == -1)
			return "";
		String ext = file.substring(lastIndexOf);
		return ext;
	}

}
