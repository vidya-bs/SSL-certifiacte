package com.itorix.hyggee.third.party.integration.business;

import java.util.List;

import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.hyggee.third.party.integration.model.GitHubUserReposResponse;
import com.itorix.hyggee.third.party.integration.model.GitHubUserResponse;

@Component
public interface GitIntegrationBusiness {
	public GitHubUserResponse gitHubUser(String interactionid, String jsessionid, String token) throws ItorixException, Exception ;
	public List<GitHubUserReposResponse> gitHubUserRepos(String interactionid, String jsessionid, String token) throws ItorixException, Exception;
}