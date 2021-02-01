package com.itorix.hyggee.third.party.integration.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.hyggee.third.party.integration.business.GitIntegrationBusiness;
import com.itorix.hyggee.third.party.integration.model.GitHubUserReposResponse;
import com.itorix.hyggee.third.party.integration.model.GitHubUserResponse;
import com.itorix.hyggee.third.party.integration.service.GitIntegrationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * To use to generate the new swagger and update the existing swagger.
 *
 * @author itorix.inc
 *
 */
@CrossOrigin
@RestController
public class GitIntegrationServiceImpl implements GitIntegrationService {
	
	@Autowired
	private GitIntegrationBusiness gitIntegrationBusiness;

	@Override
	@ApiOperation(value = "Git hub user", notes = "", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get Git hub user sucessfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/github/user")
	public ResponseEntity<GitHubUserResponse> gitHubUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "token") String token) throws Exception{
		GitHubUserResponse gitHubUserResponse = gitIntegrationBusiness.gitHubUser(interactionid,jsessionid, token);
		return new ResponseEntity<GitHubUserResponse>(gitHubUserResponse, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Git hub user repos", notes = "", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get Git hub user Repos sucessfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/github/user/repos")
	public ResponseEntity<List<GitHubUserReposResponse>> gitHubUserRepos(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "token") String token) throws Exception{
		List<GitHubUserReposResponse> gitHubUserReposResponse = gitIntegrationBusiness.gitHubUserRepos(interactionid, jsessionid, token);
		return new ResponseEntity<List<GitHubUserReposResponse>>(gitHubUserReposResponse, HttpStatus.OK);
	}
}