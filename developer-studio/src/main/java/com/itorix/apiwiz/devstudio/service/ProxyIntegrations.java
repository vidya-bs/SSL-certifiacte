package com.itorix.apiwiz.devstudio.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;

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

}
