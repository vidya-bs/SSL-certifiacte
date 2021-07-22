package com.itorix.hyggee.third.party.integration.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.hyggee.third.party.integration.model.GitHubUserReposResponse;
import com.itorix.hyggee.third.party.integration.model.GitHubUserResponse;

@CrossOrigin
@RestController
/**
 * To use to integrate git with our application modules..
 *
 * @author itorix.inc
 */
public interface GitIntegrationService {

	@RequestMapping(method = RequestMethod.GET, value = "/v1/github/user")
	public ResponseEntity<GitHubUserResponse> gitHubUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestHeader(value = "token") String token)
			throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/github/user/repos")
	public ResponseEntity<List<GitHubUserReposResponse>> gitHubUserRepos(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestHeader(value = "token") String token)
			throws Exception;
}
