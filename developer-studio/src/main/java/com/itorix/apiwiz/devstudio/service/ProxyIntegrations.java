package com.itorix.apiwiz.devstudio.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;

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
}
