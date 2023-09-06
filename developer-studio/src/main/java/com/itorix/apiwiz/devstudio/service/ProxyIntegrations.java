package com.itorix.apiwiz.devstudio.service;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
public interface ProxyIntegrations {

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/github", produces = {"application/json"})
	public ResponseEntity<?> getGitIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/github", produces = {"application/json"})
	public ResponseEntity<?> createupdateGitIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GitIntegration gitIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/github/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeGitIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/jfrog", produces = {"application/json"})
	public ResponseEntity<?> getJfrogIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/jfrog", produces = {"application/json"})
	public ResponseEntity<?> updateJfrogIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody JfrogIntegration jfrogIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/jfrog/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeJfrogIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/gitlab", produces = {"application/json"})
	public ResponseEntity<?> getGitLabIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/gitlab", produces = {"application/json"})
	public ResponseEntity<?> createupdateGitLabIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GitIntegration gitIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/gitlab/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeGitLabIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/bitbucket", produces = {"application/json"})
	public ResponseEntity<?> getBitBucketIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/bitbucket", produces = {"application/json"})
	public ResponseEntity<?> createupdateBitBucketIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GitIntegration gitIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/bitbucket/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeBitBucketIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/gocd", produces = {"application/json"})
	public ResponseEntity<?> getGocdIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/gocd", produces = {"application/json"})
	public ResponseEntity<?> updateGocdIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GoCDIntegration goCDIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/gocd/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeGocdIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/workspace", produces = {"application/json"})
	public ResponseEntity<?> getWorkspaceIntegratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/workspace/keys", produces = {
			"application/json"})
	public ResponseEntity<?> getWorkspaceIntegratonsKeys(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/workspace", produces = {"application/json"})
	public ResponseEntity<?> createWorkspaceIntegratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody WorkspaceIntegration workspaceIntegration) throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/workspace/{id:.+}", produces = {
			"application/json"})
	public ResponseEntity<?> removeWorkspaceIntegratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/apic", produces = {"application/json"})
	public ResponseEntity<?> getApicIntegratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/apic", produces = {"application/json"})
	public ResponseEntity<?> createApicIntegratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody ApicIntegration apicIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/s3", produces = {"application/json"})
	public ResponseEntity<?> getS3Integratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/s3", produces = {"application/json"})
	public ResponseEntity<?> createS3Integratons(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody S3Integration s3Integration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/s3/{id}", produces = {"application/json"})
	public ResponseEntity<?> removeS3Integraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/download/**", produces = {"application/json"})
	public void downloadFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
							 @RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("type") String type,
							 HttpServletRequest httpServletRequest, HttpServletResponse response) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/codeconnect", produces = {"application/json"})
	public ResponseEntity<?> getCodeConnectIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/codeconnect", produces = {"application/json"})
	public ResponseEntity<?> createupdateCodeconnectIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GitIntegration gitIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/codeconnect/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> removeCodeconnectIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/azuredevops", produces = {"application/json"})

	public ResponseEntity<?> getAzureIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/azuredevops", produces = {"application/json"})

	public ResponseEntity<?> createupdateAzureIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody GitIntegration gitIntegration)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/azuredevops/{id}", produces = {

			"application/json"})
	public ResponseEntity<?> removeAzureIntegraton(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/gcs", produces = {"application/json"})
	public ResponseEntity<?> getGcsIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/gcs", produces = {"application/json"})
	public ResponseEntity<?> createUpdateGcsIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestHeader(value = "projectId") String projectId, @RequestHeader(value = "bucketName") String bucketName, @RequestBody MultipartFile gcsKey)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/gcs", produces = {"application/json"})
	public ResponseEntity<?> removeGcsIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@RequestMapping(method = RequestMethod.POST, value = "/v1/integrations/database/mongodb", produces = {"application/json"})
	public ResponseEntity<?> createMongoDbIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody MongoDBConfiguration mongoDBConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mongodb", produces = {"application/json"})
	public ResponseEntity<?> getAllMongoDbIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/database/mongodb/{id}", produces = {"application/json"})
	public ResponseEntity<?> updateMongoDbIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id,
			@RequestBody MongoDBConfiguration mongoDBConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/database/mongodb/{id}", produces = {"application/json"})
	public ResponseEntity<?> deleteMongoDbIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mongodb/{id}", produces = {"application/json"})
	public ResponseEntity<?> getMongoDbIntegrationById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mongodb/meta-data", produces = {"application/json"})
	public ResponseEntity<?> getMongoDbIntegrationsMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;


	@RequestMapping(method = RequestMethod.POST, value = "/v1/integrations/database/mysql", produces = {"application/json"})
	public ResponseEntity<?> createMySqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody MySQLConfiguration mySQLConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mysql", produces = {"application/json"})
	public ResponseEntity<?> getAllMySqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/database/mysql/{id}", produces = {"application/json"})
	public ResponseEntity<?> updateMySqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id,
			@RequestBody MySQLConfiguration mySQLConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/database/mysql/{id}", produces = {"application/json"})
	public ResponseEntity<?> deleteMySqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mysql/{id}", produces = {"application/json"})
	public ResponseEntity<?> getMySqlIntegrationById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/mysql/meta-data", produces = {"application/json"})
	public ResponseEntity<?> getMySqlIntegrationsMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/integrations/database/postgresql", produces = {"application/json"})
	public ResponseEntity<?> createPostgreSqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody PostgreSQLConfiguration postgreSQLConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/postgresql", produces = {"application/json"})
	public ResponseEntity<?> getAllPostgreSqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/integrations/database/postgresql/{id}", produces = {"application/json"})
	public ResponseEntity<?> updatePostgreSqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id,
			@RequestBody PostgreSQLConfiguration postgreSQLConfiguration
	) throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/integrations/database/postgresql/{id}", produces = {"application/json"})
	public ResponseEntity<?> deletePostgreSqlIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/postgresql/{id}", produces = {"application/json"})
	public ResponseEntity<?> getPostgreSqlIntegrationById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id
	) throws Exception;
	@RequestMapping(method = RequestMethod.GET, value = "/v1/integrations/database/postgresql/meta-data", produces = {"application/json"})
	public ResponseEntity<?> getPostgreSqlIntegrationsMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid
	) throws Exception;

}
