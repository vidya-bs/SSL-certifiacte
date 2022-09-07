package com.itorix.hyggee.third.party.integration.businessimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.hyggee.third.party.integration.business.GitIntegrationBusiness;
import com.itorix.hyggee.third.party.integration.model.GitHubUserReposResponse;
import com.itorix.hyggee.third.party.integration.model.GitHubUserResponse;

@Component
public class GitIntegrationBusinessImpl implements GitIntegrationBusiness {
	private static final Logger logger = LoggerFactory.getLogger(GitIntegrationBusinessImpl.class);
	@Autowired
	private ApplicationProperties applicationProperties;

	private HttpEntity<String> getHttpEntity(String token) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", getToken(token));
		return new HttpEntity<String>("parameters", headers);
	}

	private String getToken(String token) throws Exception {
		RSAEncryption rsaEncryption = new RSAEncryption();
		String[] values = token.split(" ");
		if (token.contains("Basic"))
			return token;
		else
			return "Token " + rsaEncryption.decryptText(values[1]);
	}

	public GitHubUserResponse gitHubUser(String interactionid, String jsessionid, String token)
			throws ItorixException, Exception {
		GitHubUserResponse gitHubUserResponse = new GitHubUserResponse();
		ResponseEntity<String> response = new RestTemplate().exchange(
				applicationProperties.getThirdPartyIntegrationGitHubHost() + "/user", HttpMethod.GET,
				getHttpEntity(token), new ParameterizedTypeReference<String>() {
				});
		String responseString = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode rootNode = objectMapper.readTree(responseString);
			gitHubUserResponse.setLogin(rootNode.get("login").asText());
			gitHubUserResponse.setAvatar_url(rootNode.get("avatar_url").asText());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
		return gitHubUserResponse;
	}

	public List<GitHubUserReposResponse> gitHubUserRepos(String interactionid, String jsessionid, String token)
			throws ItorixException, Exception {
		List<GitHubUserReposResponse> listGitHubUserReposResponse = new ArrayList<>();
		ResponseEntity<String> response = new RestTemplate().exchange(
				applicationProperties.getThirdPartyIntegrationGitHubHost() + "/user/repos", HttpMethod.GET,
				getHttpEntity(token), new ParameterizedTypeReference<String>() {
				});
		String responseString = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode rootNode = objectMapper.readTree(responseString);
			if (rootNode != null && rootNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) rootNode;
				Iterator<JsonNode> nodeIterator = arrayNode.iterator();
				while (nodeIterator.hasNext()) {
					JsonNode elementNode = nodeIterator.next();
					GitHubUserReposResponse gitHubUserReposResponse = new GitHubUserReposResponse();
					gitHubUserReposResponse.setName(elementNode.get("name").asText());
					gitHubUserReposResponse.setFull_name(elementNode.get("full_name").asText());
					gitHubUserReposResponse.setClone_url(elementNode.get("clone_url").asText());
					ResponseEntity<String> branchResponse = new RestTemplate().exchange(
							applicationProperties.getThirdPartyIntegrationGitHubHost() + "/repos/"
									+ gitHubUserReposResponse.getFull_name() + "/branches",
							HttpMethod.GET, getHttpEntity(token), new ParameterizedTypeReference<String>() {
							});
					List<String> list = new ArrayList<>();
					String branchResponseString = branchResponse.getBody();
					JsonNode branchResponseNode = objectMapper.readTree(branchResponseString);
					if (branchResponseNode != null && branchResponseNode.isArray()) {
						ArrayNode branchArrayNode = (ArrayNode) branchResponseNode;
						Iterator<JsonNode> branchNodeIterator = branchArrayNode.iterator();
						while (branchNodeIterator.hasNext()) {
							JsonNode branchNode = branchNodeIterator.next();
							list.add(branchNode.get("name").asText());
						}
						gitHubUserReposResponse.setBranches(list);
					}
					listGitHubUserReposResponse.add(gitHubUserReposResponse);
				}
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}

		return listGitHubUserReposResponse;
	}
}
