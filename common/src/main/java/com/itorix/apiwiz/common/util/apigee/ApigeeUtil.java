package com.itorix.apiwiz.common.util.apigee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.itorix.apiwiz.common.model.monetization.apigeepayloads.ApigeeRatePlan;
import net.sf.json.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.apigee.APIProduct;
import com.itorix.apiwiz.common.model.apigee.APIProxyDeploymentDetailsResponse;
import com.itorix.apiwiz.common.model.apigee.APIProxyDeploymentEnvResponse;
import com.itorix.apiwiz.common.model.apigee.APIProxyResponse;
import com.itorix.apiwiz.common.model.apigee.APIProxyRevisionDeploymentsResponse;
import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeResponse;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.apigee.DeveloperApp;
import com.itorix.apiwiz.common.model.apigee.DeveloperDetails;
import com.itorix.apiwiz.common.model.apigee.Environment;
import com.itorix.apiwiz.common.model.apigee.KeyForADeveloperAppResponse;
import com.itorix.apiwiz.common.model.apigee.Revision;
import com.itorix.apiwiz.common.model.apigee.metrics.PerformanceTrafficResponse;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@Component
public class ApigeeUtil {
	private static final Logger logger = LoggerFactory.getLogger(ApigeeUtil.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private ApigeeXUtill apigeexUtil;

	@Value("${apigee.edge.api.base.token:ZWRnZWNsaTplZGdlY2xpc2VjcmV0}")
	private String apigeeEdgeAPIBaseToken;

	// @Autowired
	// ApplicationProperties applicationProperties;

	@Value("${apigee.host:null}")
	private String apigeeHost;

	/*
	 * private String getSecureURL(String suffix) { if
	 * (applicationProperties.getApigeePort() == null ||
	 * applicationProperties.getApigeePort().length() == 0) { return "https://"
	 * + applicationProperties.getApigeeHost() + "/" + suffix; } else { return
	 * "https://" + applicationProperties.getApigeeHost() + ":" +
	 * applicationProperties.getApigeePort() + "/" + suffix; } }
	 */

	private String getSecureURL(String suffix, CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && !(cfg.getType().equalsIgnoreCase("saas"))) {
			ApigeeConfigurationVO vo = mongoTemplate.findOne(
					new Query(Criteria.where("type").is(cfg.getType()).and("orgname").is(cfg.getOrganization())),
					ApigeeConfigurationVO.class);
			if (vo == null) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1005"), "Apigee-1005");
			}
			if (vo.getPort() == null || vo.getPort().length() == 0) {
				return vo.getScheme() + "://" + vo.getHostname() + "/" + suffix;
			} else {
				return vo.getScheme() + "://" + vo.getHostname() + ":" + vo.getPort() + "/" + suffix;
			}
		} else {
			return "https://" + apigeeHost + "/" + suffix;
		}
	}

	private String getApigeexURL(String suffix, CommonConfiguration cfg) {
		return apigeexUtil.getApigeeHost(cfg.getOrganization()) + suffix;
	}

	private String getSecureURL(String suffix, String type, String org) throws ItorixException {
		if (!(StringUtils.isEmpty(type)) && !(type.equalsIgnoreCase("saas"))) {
			ApigeeConfigurationVO vo = mongoTemplate.findOne(
					new Query(Criteria.where("type").is(type).and("orgname").is(org)), ApigeeConfigurationVO.class);
			if (vo == null) {
				logger.error("no configuration available.");
				throw new ItorixException(
						String.format("no configuration exist by type = %s and orgname = %s.", type, org), "DMB_0004");
			}
			return vo.getScheme() + "://" + vo.getHostname() + ":" + vo.getPort() + "/" + suffix;
		} else {
			return "https://" + apigeeHost + "/" + suffix;
		}
	}

	private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, int count, CommonConfiguration cfg) throws ItorixException {
		logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
				+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : count = " + count);
		ResponseEntity<T> response = null;
		try {
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				protected boolean hasError(HttpStatus statusCode) {
					return false;
				}
			});
			logger.debug("Making a call to {}", url);
			response = restTemplate.exchange(url, method, requestEntity, responseType);

			logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : count = "
					+ count + "/n Response : " + response);
		} catch (Exception e) {
			logger.error("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : count = "
					+ count + "/n Response : " + response);
			throw new RestClientException(e.getMessage());
		}
		if (response != null) {
			if (response.getStatusCode().value() == 403) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1006"), "Apigee-1006");
			} else if (response.getStatusCode().value() == 401) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1003"), "Apigee-1003");
			} else if (response.getStatusCode().value() == 500) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1002"), "Apigee-1002");
			}
		}
		return response;
	}

	private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, CommonConfiguration cfg, Object... uriVariables) throws ItorixException {
		logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
				+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
				+ uriVariables);
		ResponseEntity<T> response = null;
		try {
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				protected boolean hasError(HttpStatus statusCode) {
					return false;
				}
			});
			logger.debug("Making a call to {}", url);
			response = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);

			logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
					+ uriVariables + "/n Response : " + response);
		} catch (Exception e) {
			logger.error("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
					+ uriVariables + "/n Response : " + response);
			throw new RestClientException(e.getMessage());
		}
		if (response != null) {
			if (response.getStatusCode().value() == 403) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1006"), "Apigee-1006");
			} else if (response.getStatusCode().value() == 401) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1003"), "Apigee-1003");
			} else if (response.getStatusCode().value() == 500) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1002"), "Apigee-1002");
			}
		}
		return response;
	}

	private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, CommonConfiguration cfg, Object... uriVariables)
			throws ItorixException {
		logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
				+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
				+ uriVariables);

		ResponseEntity<T> response = null;
		try {
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				protected boolean hasError(HttpStatus statusCode) {
					return false;
				}
			});
			logger.debug("Making a call to {}", url);
			response = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);

			logger.debug("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
					+ uriVariables + "/n Response : " + response);
		} catch (Exception e) {
			logger.error("interactionid = " + cfg.getInteractionid() + " : url = " + url + ": method = " + method
					+ " : requestEntity = " + requestEntity + " : responseType = " + responseType + " : uriVariables = "
					+ uriVariables + "/n Response : " + response);
			throw new RestClientException(e.getMessage());
		}
		if (response != null) {
			if (response.getStatusCode().value() == 403) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1006"), "Apigee-1006");
			} else if (response.getStatusCode().value() == 401) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1003"), "Apigee-1003");
			} else if (response.getStatusCode().value() == 500) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1002"), "Apigee-1002");
			}
		}
		return response;
	}

	private <T> ResponseEntity<T> postForEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType,
			Object... uriVariables) {
		logger.debug("Making a call to {}", url);
		ResponseEntity<T> response = restTemplate.postForEntity(url, requestEntity, responseType);
		return response;
	}

	public String getApigeeHost(String type, String org) throws ItorixException {
		return getSecureURL("", type, org);
	}

	/**
	 * Gets the .
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getApigeeAddress(CommonConfiguration cfg) throws ItorixException {
		return getSecureURL("", cfg);
	}

	/**
	 * Gets the names of all environments in an organization. By default, an
	 * Apigee organization contains two environments: test and prod.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> getEnvironmentNames(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<List<String>> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), new ParameterizedTypeReference<List<String>>() {
					}, cfg);
			return response.getBody();

		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, 1, cfg);
		return response.getBody();
	}

	/**
	 * Gets the names of all environments in an organization. By default, an
	 * Apigee organization contains two environments: test and prod.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> getEnvironmentNames(CommonConfiguration cfg, String host, String port, String scheme)
			throws ItorixException {
		String url = null;
		if (host != null && port != null) {

			url = scheme + "://" + host + ":" + port + "/" + "v1/organizations/" + cfg.getOrganization()
					+ "/environments";
		} else {
			url = "https" + "://" + apigeeHost + "/" + "v1/organizations/" + cfg.getOrganization() + "/environments";
		}
		ResponseEntity<List<String>> response = exchange(url, HttpMethod.GET, getHttpEntity(cfg),
				new ParameterizedTypeReference<List<String>>() {
				}, 1, cfg);
		if (response.getStatusCode().value() == 404) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1001"), "Apigee-1001");
		}
		return response.getBody();
	}

	/**
	 * Gets the names of all API proxies in an organization. The names
	 * correspond to the names defined in the configuration files for each API
	 * proxy.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listAPIProxies(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			String serviceResponse = response.getBody();
			List<String> envApiList = new ArrayList<String>();
			JSONObject envJsonObject = (JSONObject) JSONSerializer.toJSON(serviceResponse);
			JSONArray envApis = (JSONArray) envJsonObject.get("proxies");
			for (int j = 0; j < envApis.size(); j++) {
				JSONObject apiProxyObject = (JSONObject) envApis.get(j);
				String apiName = (String) apiProxyObject.get("name");
				envApiList.add(apiName);
			}
			return envApiList;
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	public String getApigeexAPIDeployment(CommonConfiguration cfg) throws ItorixException {
		String proxy = cfg.getApiName();
		ResponseEntity<String> response = exchange(
				getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis/" + proxy + "/deployments", cfg),
				HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
		return response.getBody();
	}

	public String getApigeexSharedflowDeployment(CommonConfiguration cfg) throws ItorixException {
		String proxy = cfg.getApiName();
		ResponseEntity<String> response = exchange(
				getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + proxy + "/deployments",
						cfg),
				HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets an array of the names of shared flows in the organization. The
	 * response is a simple array of strings.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listSharedflows(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			String serviceResponse = response.getBody();
			List<String> envApiList = new ArrayList<String>();
			JSONObject envJsonObject = (JSONObject) JSONSerializer.toJSON(serviceResponse);
			JSONArray envApis = (JSONArray) envJsonObject.get("sharedFlows");
			for (int j = 0; j < envApis.size(); j++) {
				JSONObject apiProxyObject = (JSONObject) envApis.get(j);
				String apiName = (String) apiProxyObject.get("name");
				envApiList.add(apiName);
			}
			return envApiList;
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/sharedflows", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Returns detail on all deployments of the API proxy for all environments.
	 * All deployments are listed in the test and prod environments, as well as
	 * other environments, if they exist.
	 *
	 * @param cfg
	 * 
	 * @return APIProxyDeploymentDetailsResponse
	 * 
	 * @throws ItorixException
	 */
	public APIProxyDeploymentDetailsResponse getAPIProxyDeploymentDetails(CommonConfiguration cfg)
			throws ItorixException {

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/deployments", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return mapResponse(response.getBody());
		}

		ResponseEntity<APIProxyDeploymentDetailsResponse> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/deployments",
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), APIProxyDeploymentDetailsResponse.class, cfg);
		return response.getBody();
	}

	private APIProxyDeploymentDetailsResponse mapResponse(String response) {
		try {
			APIProxyDeploymentDetailsResponse apiResponse = new APIProxyDeploymentDetailsResponse();
			JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(response);
			JSONArray deployments = (JSONArray) proxyObject.get("deployments");
			Environment[] envList = new Environment[deployments.size()];
			int i = 0;
			for (Object apiObj : deployments) {
				Environment env = new Environment();
				JSONObject prodObj = (JSONObject) apiObj;
				String environmentName = (String) prodObj.get("environment");
				String revisionStr = (String) prodObj.get("revision");
				env.setName(environmentName);
				Revision[] revList = new Revision[1];
				Revision revision = new Revision();
				revision.setName(revisionStr);
				revList[0] = revision;
				env.setRevision(revList);
				envList[i] = env;
				i++;
			}
			apiResponse.setEnvironment(envList);
			return apiResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns detail on all deployments of the shared flows for all
	 * environments. All deployments are listed in the test and prod
	 * environments, as well as other environments, if they exist.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getSharedflowsDeploymentDetails(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows/"
							+ cfg.getSharedflowName() + "/deployments", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			ObjectMapper objMapper = new ObjectMapper();
			String responseStr = null;
			try {
				responseStr = objMapper.writeValueAsString(mapResponse(response.getBody()));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return responseStr;
		}
		ResponseEntity<String> response = exchange(getSecureURL("v1/organizations/" + cfg.getOrganization()
				+ "/sharedflows/" + cfg.getSharedflowName() + "/deployments", cfg), HttpMethod.GET, getHttpEntity(cfg),
				String.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets a specific revision of an API proxy.
	 *
	 * @param cfg
	 * 
	 * @return byte[]
	 * 
	 * @throws ItorixException
	 * @throws RestClientException
	 */
	public byte[] getAnAPIProxyRevision(CommonConfiguration cfg) throws RestClientException, ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			String URL = getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName()
					+ "/revisions/" + cfg.getRevision() + "?format=bundle", cfg);
			ResponseEntity<byte[]> response = restTemplate.exchange(URL, HttpMethod.GET,
					getHttpEntityXToDownloadFile(cfg), byte[].class);
			return response.getBody();
		}

		cfg.setApigeeCred(getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		ResponseEntity<byte[]> response = restTemplate.exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "?format=bundle", cfg),
				HttpMethod.GET, getHttpEntityToDownloadFile(cfg), byte[].class);
		return response.getBody();
	}

	public byte[] getApigeexAPIProxyRevision(CommonConfiguration cfg) throws RestClientException, ItorixException {
		cfg.setApigeeCred(getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		ResponseEntity<byte[]> response = restTemplate.exchange(
				getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "?format=bundle", cfg),
				HttpMethod.GET, getHttpEntityX(cfg), byte[].class);
		return response.getBody();
	}

	/**
	 * Gets a specific revision of an Sharedflows.
	 *
	 * @param cfg
	 * 
	 * @return byte[]
	 * 
	 * @throws ItorixException
	 * @throws RestClientException
	 */
	public byte[] getAnSharedflowRevision(CommonConfiguration cfg) throws RestClientException, ItorixException {
		RestTemplate restTemplate = new RestTemplate();

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<byte[]> response = restTemplate.exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows/"
							+ cfg.getSharedflowName() + "/revisions/" + cfg.getRevision() + "?format=bundle", cfg),
					HttpMethod.GET, getHttpEntityXToDownloadFile(cfg), byte[].class);
			return response.getBody();
		}

		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		ResponseEntity<byte[]> response = restTemplate.exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName()
						+ "/revisions/" + cfg.getRevision() + "?format=bundle", cfg),
				HttpMethod.GET, getHttpEntityToDownloadFile(cfg), byte[].class);
		return response.getBody();
	}

	/**
	 * Gets a specific revision of an API proxy.
	 *
	 * @param cfg
	 * 
	 * @return APIProxyResponse
	 * 
	 * @throws ItorixException
	 */
	public APIProxyResponse getAPIProxy(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<APIProxyResponse> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<APIProxyResponse>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<APIProxyResponse> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), APIProxyResponse.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets a specific revision of an Sharedflow.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getSharedflow(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();

		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName(),
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Force the undeployment of the API proxy that is partially deployed. This
	 * can be necessary if the API proxy becomes partially deployed and must be
	 * undeployed, then redeployed
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public String forceUndeployAPIProxy(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision() + "/deployments",
							cfg),
					HttpMethod.DELETE, getHttpEntityXWithOctetStreamContentType(cfg),
					new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/deployments?action=undeploy&force=true&env=" + cfg.getEnvironment(),
						cfg),
				HttpMethod.POST, getHttpEntityWithOctetStreamContentType(cfg),
				new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Force the undeployment of the Shared flow that is partially deployed.
	 * This can be necessary if the Shared flow becomes partially deployed and
	 * must be undeployed, then redeployed
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public String forceUndeploySharedflow(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/sharedflows/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision()
							+ "/deployments", cfg),
					HttpMethod.DELETE, getHttpEntityXWithOctetStreamContentType(cfg),
					new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();

		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/sharedflows/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision()
						+ "/deployments", cfg),
				HttpMethod.DELETE, getHttpEntityWithOctetStreamContentType(cfg),
				new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Deletes an API proxy and all associated endpoints, policies, resources,
	 * and revisions. The API proxy must be undeployed before you can delete it.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public String deleteAPIProxy(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName(), cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Deletes an Shared flow and all associated endpoints, policies, resources,
	 * and revisions. The API proxy must be undeployed before you can delete it.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public String deleteSharedflow(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName(), cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName(),
						cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public Map<String, ApigeeResponse> getOverview(CommonConfiguration cfg)
			throws ItorixException, InterruptedException, ExecutionException {

		ExecutorService executor = Executors.newFixedThreadPool(12);

		Map<String, String> urlList = new HashMap<String, String>();
		urlList.put("totalTrafficCount",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/?select=sum(message_count))&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59");
		urlList.put("totalErrorRate",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/?select=sum(is_error)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59");
		urlList.put("topAPITrafficCount",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apis?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=sum(message_count)");
		urlList.put("topAPIWithErrorCount",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apis?select=sum(is_error)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=sum(is_error)");
		urlList.put("minPerformingAPIWithResponseTime",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apis?select=min(total_response_time)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=min(total_response_time)");
		urlList.put("maxPerformingAPIWithResponseTime",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apis?select=max(total_response_time)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=max(total_response_time)");
		urlList.put("topAppWithTraffic",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apps?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=sum(message_count)");
		urlList.put("topAPIProductWithTraffic",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apiproducts?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59&topk=0&sortby=sum(message_count)");
		urlList.put("geoMetrics",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/ax_geo_country?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59");
		urlList.put("topFiveAPI",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apis?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&topk=5&sortby=sum(message_count)");
		urlList.put("topFiveAPP",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apps?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&topk=5&sortby=sum(message_count)");
		urlList.put("topFiveAPIProducts",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/apiproducts?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59&topk=5&sortby=sum(message_count)");
		urlList.put("topFiveDevelopers",
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/developer_email?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate()
						+ " 00:00~" + cfg.getTimeRangeendDate() + " 23:59&topk=5&sortby=sum(message_count)");
		Map<String, ApigeeResponse> result = new HashMap<String, ApigeeResponse>();
		for (Map.Entry<String, String> entry : urlList.entrySet()) {
			Future future = executor.submit(new Callable() {
				@Override
				public Object call() throws ItorixException {
					ResponseEntity<ApigeeResponse> response;
					try {
						response = exchange(getSecureURL(entry.getValue(), cfg), HttpMethod.GET, getHttpEntity(cfg),
								ApigeeResponse.class, cfg);
						logger.debug(response.getBody().toString());
						result.put(entry.getKey(), response.getBody());
						return response.getBody();
					} catch (ItorixException e) {
						throw e;
					}
				}
			});
			logger.debug("response" + future.get());
		}
		return result;
	}

	public Object getTimeSeriesData(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/stats/?select=sum(message_count)&timeRange=" + cfg.getTimeRangestartDate() + " 00:00~"
						+ cfg.getTimeRangeendDate() + " 23:59&timeUnit=day", cfg),
				HttpMethod.GET, getHttpEntity(cfg), ObjectNode.class, cfg);
		logger.debug(response.getBody().toString());
		return response.getBody();
	}

	public PerformanceTrafficResponse getApiProxyPerfomanceTraffic(CommonConfiguration cfg) throws ItorixException {

		String suffix = null;

		if (cfg.getTimeUnit() != null) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=sum(message_count),tps,sum(is_error)&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit();
		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=sum(message_count),tps,sum(is_error)&timeRange=" + cfg.getTimeRange();
		}

		ResponseEntity<PerformanceTrafficResponse> response = exchange(getSecureURL(suffix, cfg), HttpMethod.GET,
				getHttpEntity(cfg), PerformanceTrafficResponse.class, cfg);
		logger.debug(response.getBody().toString());
		return response.getBody();
	}

	public String getApiProxyPerfomanceTrafficWithOutTimeUnit(CommonConfiguration cfg) throws ItorixException {

		ResponseEntity<String> response = exchange(
				getSecureURL(
						"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/stats/?select=sum(message_count),tps,sum(is_error)&timeRange=" + cfg.getTimeRange(),
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		logger.debug(response.getBody().toString());
		return response.getBody();
	}

	public Object getDeveloperName(String developerId, CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<DeveloperDetails> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + developerId, cfg),
				HttpMethod.GET, getHttpEntity(cfg), DeveloperDetails.class, cfg);
		logger.debug(response.getBody().toString());
		return response.getBody();
	}

	/**
	 * To get the list of APPIDs in Organization
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listAppIDsInAnOrganization(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apps/", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			List<String> products = new ArrayList<>();
			String apiProductString = response.getBody();
			try {
				JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
				JSONArray apiProducts = (JSONArray) proxyObject.get("app");
				for (Object apiObj : apiProducts) {
					JSONObject prodObj = (JSONObject) apiObj;
					final String apiProduct = (String) prodObj.get("appId");
					products.add(apiProduct);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return products;
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apps", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Returns the app profile for the specified app ID
	 *
	 * @param cfg
	 * 
	 * @return Apps
	 * 
	 * @throws ItorixException
	 */
	public String getAppInAnOrganizationByAppID(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apps/" + cfg.getAppID(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apps/" + cfg.getAppID(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Get a list of all API product names for an organization.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listAPIProducts(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apiproducts/", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), String.class, cfg);
			List<String> products = new ArrayList<>();
			String apiProductString = response.getBody();
			try {
				JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
				JSONArray apiProducts = (JSONArray) proxyObject.get("apiProduct");
				for (Object apiObj : apiProducts) {
					JSONObject prodObj = (JSONObject) apiObj;
					final String apiProduct = (String) prodObj.get("name");
					products.add(apiProduct);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return products;
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apiproducts", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Get a list of all API product names for an organization.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public String listAPIProductsByQuery(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apiproducts?expand=" + cfg.isExpand(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apiproducts?expand=" + cfg.isExpand(),
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Get a List App IDs in an Organization By Query.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String listAppIDsInAnOrganizationByQuery(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apps?expand=" + cfg.isExpand(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apps?expand=" + cfg.isExpand(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Get a List Developers.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String listDevelopersByQuery(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers?expand=" + cfg.isExpand(),
							cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers?expand=" + cfg.isExpand(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets an API product by name.
	 *
	 * @param cfg
	 * 
	 * @return APIProduct
	 * 
	 * @throws ItorixException
	 */
	public String getAPIProduct(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apiproducts/" + cfg.getApiProductName(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apiproducts/" + cfg.getApiProductName(),
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Deletes an API product by name.
	 *
	 * @param cfg
	 * 
	 * @return APIProduct
	 * 
	 * @throws ItorixException
	 */
	public APIProduct deleteAPIProduct(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<APIProduct> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apiproducts/" + cfg.getApiProductName(), cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<APIProduct>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<APIProduct> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apiproducts/" + cfg.getApiProductName(),
						cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), APIProduct.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets a specific revision of an API proxy.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 * @throws RestClientException
	 */
	public List<String> getAnAPIProxyRevisionList(CommonConfiguration cfg) throws RestClientException, ItorixException {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		ResponseEntity<List<String>> response = restTemplate.exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions",
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				});
		return response.getBody();
	}

	public String getDeploymentsForAnOrganization(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/deployments", cfg), HttpMethod.GET,
				getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	public String getAPIProxyDeploymentDetail1s(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/deployments", cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	public String getSharedflowDeploymentDetails(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/sharedflows/" + cfg.getSharedflowName() + "/deployments", cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Get deployment details for a specific revision number of an API proxy.
	 *
	 * @param cfg
	 * 
	 * @return APIProxyRevisionDeploymentsResponse
	 * 
	 * @throws ItorixException
	 */
	public APIProxyRevisionDeploymentsResponse getAPIProxyRevisionDeployments(CommonConfiguration cfg)
			throws ItorixException {
		ResponseEntity<APIProxyRevisionDeploymentsResponse> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/deployments", cfg),
				HttpMethod.GET, getHttpEntity(cfg), APIProxyRevisionDeploymentsResponse.class, cfg);
		return response.getBody();
	}

	public APIProxyDeploymentEnvResponse getDeploymentDetailsForAnAPIProxyInAnEnvironment(CommonConfiguration cfg)
			throws ItorixException {
		ResponseEntity<APIProxyDeploymentEnvResponse> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/deployments", cfg),
				HttpMethod.GET, getHttpEntity(cfg), APIProxyDeploymentEnvResponse.class, cfg);
		return response.getBody();
	}

	public List<Integer> getRevisionsListForProxy(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			List<Integer> revisionList = new ArrayList<Integer>();
			JSON jsonObject = JSONSerializer.toJSON(response.getBody());
			if (jsonObject.isArray()) {
				JSONArray revisionsArray = (JSONArray) JSONSerializer.toJSON(response.getBody());
				for (Object revisionObj : revisionsArray) {
					String revision = (String) revisionObj;
					revisionList.add(Integer.parseInt(revision));
				}
			} else {
				if (org.apache.commons.lang3.StringUtils.isNumeric(jsonObject.toString(1))) {
					revisionList.add(Integer.parseInt(jsonObject.toString(1)));
				}
			}

			return revisionList;
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions",
						cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		List<Integer> revisionList = new ArrayList<Integer>();
		// TODO
		/*
		 * JSONArray revisionsArray = (JSONArray)
		 * JSONSerializer.toJSON(response.getBody()); for (Object revisionObj :
		 * revisionsArray) { String revision = (String) revisionObj;
		 * revisionList.add(Integer.parseInt(revision)); }
		 */
		return revisionList;
	}

	public List<Integer> getRevisionsListForSharedflow(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows/"
							+ cfg.getSharedflowName() + "/revisions", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			List<Integer> revisionList = new ArrayList<Integer>();
			if(response.getStatusCode().is2xxSuccessful()) {
				JSONArray revisionsArray = (JSONArray) JSONSerializer.toJSON(response.getBody());
				for (Object revisionObj : revisionsArray) {
					String revision = (String) revisionObj;
					revisionList.add(Integer.parseInt(revision));
				}
			}
			return revisionList;
		}

		ResponseEntity<String> response = exchange(getSecureURL(
				"v1/organizations/" + cfg.getOrganization() + "/sharedflows/" + cfg.getSharedflowName() + "/revisions",
				cfg), HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		List<Integer> revisionList = new ArrayList<Integer>();
		// TODO
		/*
		 * JSONArray revisionsArray = (JSONArray)
		 * JSONSerializer.toJSON(response.getBody()); for (Object revisionObj :
		 * revisionsArray) { String revision = (String) revisionObj;
		 * revisionList.add(Integer.parseInt(revision)); }
		 */
		return revisionList;
	}

	/**
	 * Get Developer.
	 *
	 * @param cfg
	 * 
	 * @return Developer
	 * 
	 * @throws ItorixException
	 */
	public String getDeveloper(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperId(),
							cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);

			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperId(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Deletes a consumer key that belongs to an app, and removes all API
	 * products associated with the app. Once deleted, the consumer key cannot
	 * be used to access any APIs.
	 *
	 * @param cfg
	 * 
	 * @return KeyForADeveloperAppResponse
	 * 
	 * @throws ItorixException
	 */
	public KeyForADeveloperAppResponse deleteKeyForADeveloperApp(CommonConfiguration cfg) throws ItorixException {

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<KeyForADeveloperAppResponse> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/"
							+ cfg.getDeveloperEmail() + "/apps/" + cfg.getAppName() + "/keys/" + cfg.getConsumerKey(),
							cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg),
					new ParameterizedTypeReference<KeyForADeveloperAppResponse>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<KeyForADeveloperAppResponse> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperEmail()
						+ "/apps/" + cfg.getAppName() + "/keys/" + cfg.getConsumerKey(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), KeyForADeveloperAppResponse.class, cfg);
		return response.getBody();
	}

	/**
	 * Delete Developer App.
	 *
	 * @param cfg
	 * 
	 * @return DeveloperApp
	 * 
	 * @throws ItorixException
	 */
	public DeveloperApp deleteDeveloperApp(CommonConfiguration cfg, String developerEmail, String appName)
			throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<DeveloperApp> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/"
							+ cfg.getDeveloperEmail() + "/apps/" + cfg.getAppName(), cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<DeveloperApp>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<DeveloperApp> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperEmail()
						+ "/apps/" + cfg.getAppName(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), DeveloperApp.class, cfg);
		return response.getBody();
	}

	/**
	 * Delete Developer.
	 *
	 * @param cfg
	 * 
	 * @return Developer
	 * 
	 * @throws ItorixException
	 */
	public DeveloperApp deleteDeveloper(CommonConfiguration cfg, String developerEmail) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<DeveloperApp> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperId(),
							cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<DeveloperApp>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<DeveloperApp> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperId(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), DeveloperApp.class, cfg);
		return response.getBody();
	}

	/**
	 * List caches in an environment.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listCachesInAnEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/caches", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Lists the name of all key/value maps in an environment and optionally
	 * returns an expanded view of all key/value maps for the environment.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listKeyValueMapsInAnEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * List all TargetServers in an environment.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listTargetServersInAnEnvironment(CommonConfiguration cfg) throws ItorixException {

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<List<String>> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/targetservers", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<List<String>>() {
					}, cfg);
			return response.getBody();
		}

		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/targetservers", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Lists all virtual hosts in an environment. By default, two virtual hosts
	 * are available for each environment: 'default' and 'secure'.
	 *
	 * @param cfg
	 * 
	 * @return List<String>
	 * 
	 * @throws ItorixException
	 */
	public List<String> listVirtualHosts(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/virtualhosts", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	/**
	 * Gets information about a cache..
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getInformationAboutACache(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(getSecureURL("v1/organizations/" + cfg.getOrganization()
				+ "/environments/" + cfg.getEnvironment() + "/caches/" + cfg.getCacheName(), cfg), HttpMethod.GET,
				getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Deletes information about a cache..
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String deleteCache(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/caches/" + cfg.getCacheName(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets a key/value map in an environment by name, along with associated
	 * entries.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getKeyValueMapInAnEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps/" + cfg.getKeyValueMapName(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Deletes a key/value map in an environment by name, along with associated
	 * entries.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String deleteKeyValueMapInAnEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps/" + cfg.getKeyValueMapName(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Gets details for a named virtual host. Every environment has at least one
	 * virtual host that defines the HTTP settings for connection with the
	 * Apigee organization. All API proxies in an environment share the same
	 * virtual hosts. By default, two virtual hosts are available for each
	 * environment: 'default' and 'secure'.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getVirtualHost(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/virtualhosts/" + cfg.getVirtualHostName(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Returns a TargetServer definition.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String getTargetServer(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/targetservers/" + cfg.getTargetServerName(), cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/targetservers/" + cfg.getTargetServerName(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Delets a TargetServer definition.
	 *
	 * @param cfg
	 * 
	 * @return String
	 * 
	 * @throws ItorixException
	 */
	public String deleteTargetServer(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/targetservers/" + cfg.getTargetServerName(), cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/targetservers/" + cfg.getTargetServerName(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	/**
	 * Lists all developers in an organization by email address. This call does
	 * not list any company developers who are a part of the designated
	 * organization.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<String> listDevelopers(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers", cfg), HttpMethod.GET,
					getHttpEntityX(cfg), String.class, cfg);

			List<String> elements = new ArrayList<>();
			String apiProductString = response.getBody();
			try {
				JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
				JSONArray apiProducts = (JSONArray) proxyObject.get("developer");
				for (Object apiObj : apiProducts) {
					JSONObject prodObj = (JSONObject) apiObj;
					final String apiProduct = (String) prodObj.get("email");
					elements.add(apiProduct);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return elements;
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers", cfg), HttpMethod.GET,
				getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	public String createApiProduct(CommonConfiguration cfg) throws ItorixException {

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			System.out.println("1");
			cfg.setApiProduct(getApigeeXProductBody(cfg.getApiProduct()));
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apiproducts", cfg), HttpMethod.POST,
					getHttpEntityXWithBodyJSON(cfg, cfg.getApiProduct()), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apiproducts", cfg), HttpMethod.POST,
				getHttpEntityWithBodyJSON(cfg, cfg.getApiProduct()), String.class, cfg);
		return response.getBody();
	}

	private String getApigeeXProductBody(String productStr) {
		System.out.println("2");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			productStr = productStr.replaceAll(",\"additionalProperties\":\\{}", "");
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			APIProduct productObj = objectMapper.readValue(productStr, APIProduct.class);
			productObj.setLastModifiedBy(null);
			productObj.setCreatedBy(null);
			return objectMapper.writeValueAsString(productObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return productStr;
	}

	private String getApigeeXDeveloperBody(String productStr1) {
		try {
			String productStr = productStr1;
			productStr = productStr.replaceAll("\".*\" : \\[ ],", "");
			productStr = productStr.replaceAll("\"createdBy\".*\",", "");
			productStr = productStr.replaceAll("\"createdBy\".*\"", "");
			productStr = productStr.replaceAll(",[\n]\"lastModifiedBy\".*\"", "");
			productStr = productStr.replaceAll("\"lastModifiedBy\".*\",", "");
			productStr = productStr.replaceAll("\"lastModifiedBy\".*\"", "");
			productStr = productStr.replace("\n", "").replace("\r", "");
			productStr = productStr.replaceAll(",\\s*}", "}");
			return productStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return productStr1;
	}

	public String getProxyDeployments(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(getSecureURL(
				"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment() + "/deployments",
				cfg), HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	public String getProxyDeploymentsForProxyInEnvironment(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/deployments", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);

			return response.getBody();
		} else {
			ResponseEntity<String> response = exchange(
					getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/deployments", cfg),
					HttpMethod.GET, getHttpEntity(cfg), String.class, cfg);
			return response.getBody();
		}
	}

	public String deployAPIProxy(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/deployments?action=deploy&env=" + cfg.getEnvironment(), cfg),
				HttpMethod.POST, postHttpEntityForOctetStream(cfg, "{}"), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String deployAPIProxy1(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			System.out.println("");
			String URL = getApigeexURL(
					"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment() + "/apis/"
							+ cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/deployments",
					cfg);
			ResponseEntity<String> response = exchange(URL, HttpMethod.POST, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/deployments", cfg),
				HttpMethod.POST, postHttpEntityForOctetStream(cfg, "{}"), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String deploySharedflow(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/sharedflows/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision()
							+ "/deployments?override=true", cfg),
					HttpMethod.POST, getHttpEntityX(cfg), String.class, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/sharedflows/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision()
						+ "/deployments", cfg),
				HttpMethod.POST, getHttpEntityWithOctetStreamContentType(cfg), String.class, cfg);
		return response.getBody();
	}

	public String unDeployAPIProxy(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL(
							"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
									+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/deployments",
							cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/deployments", cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), String.class, cfg);
		return response.getBody();
	}

	public String unDeploySharedflow(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/sharedflows/" + cfg.getSharedflowName() + "/revisions/" + cfg.getRevision()
						+ "/deployments", cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String deleteAPIProxyRevision(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision(), cfg),
				HttpMethod.DELETE, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String postDeveloperApps(CommonConfiguration cfg, String body) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/"
							+ cfg.getDeveloperEmail() + "/apps", cfg),
					HttpMethod.POST, postHttpEntityX(cfg, body), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperEmail()
						+ "/apps", cfg),
				HttpMethod.POST, postHttpEntity(cfg, body), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String createdeveloperKeysForDeveloperApp(CommonConfiguration cfg, String body) throws ItorixException {

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/"
							+ cfg.getDeveloperEmail() + "/apps/" + cfg.getAppName() + "/keys/create", cfg),
					HttpMethod.POST, postHttpEntityX(cfg, body), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperEmail()
						+ "/apps/" + cfg.getAppName() + "/keys/create", cfg),
				HttpMethod.POST, postHttpEntity(cfg, body), String.class, cfg);
		return response.getBody();
	}

	public String addAPIProductToKey(CommonConfiguration cfg, String body) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/"
							+ cfg.getDeveloperEmail() + "/apps/" + cfg.getAppName() + "/keys/" + cfg.getConsumerKey(),
							cfg),
					HttpMethod.POST, postHttpEntityX(cfg, body), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/" + cfg.getDeveloperEmail()
						+ "/apps/" + cfg.getAppName() + "/keys/" + cfg.getConsumerKey(), cfg),
				HttpMethod.POST, postHttpEntity(cfg, body), String.class, cfg);
		return response.getBody();
	}

	public String createDeveloper(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			System.out.println("1");
			cfg.setAppDeveloper(getApigeeXDeveloperBody(cfg.getAppDeveloper()));
			String URL = getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/developers/", cfg);
			ResponseEntity<String> response = exchange(URL, HttpMethod.POST,
					getHttpEntityXWithBodyJSON(cfg, cfg.getAppDeveloper()), String.class, cfg);
			return response.getBody();
		}

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/developers/", cfg), HttpMethod.POST,
				getHttpEntityWithBodyJSON(cfg, cfg.getAppDeveloper()), String.class, cfg);
		return response.getBody();
	}

	public String createResourceInEnvironment(CommonConfiguration cfg, String resourceType) throws ItorixException {
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			String URL = getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/"
					+ cfg.getEnvironment() + "/" + resourceType, cfg);
			ResponseEntity<String> response = exchange(URL, HttpMethod.POST,
					getHttpEntityXWithBodyJSON(cfg, cfg.getResource()), String.class, cfg);
			return response.getBody();
		}
		String URL = getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
				+ "/" + resourceType + "/", cfg);
		ResponseEntity<String> response = exchange(URL, HttpMethod.POST,
				getHttpEntityWithBodyJSON(cfg, cfg.getResource()), String.class, cfg);
		return response.getBody();
	}

	public String getProxyEndPoint(CommonConfiguration cfg, String proxy) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/proxies/" + proxy, cfg),
				HttpMethod.GET, getHttpEntityAndXmlAcceptType(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String getTargetEndPoint(CommonConfiguration cfg, String target) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/targets/" + target, cfg),
				HttpMethod.GET, getHttpEntityAndXmlAcceptType(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	/*
	 * public String getAPIsDeployedToEnvironment(CommonConfiguration cfg) {
	 * ResponseEntity<String> response = exchange(
	 * getSecureURL("v1/organizations/" + cfg.getOrganization() +
	 * "/environments/" + cfg.getEnvironment() + "/deployments"),
	 * HttpMethod.GET, getHttpEntity(cfg), new
	 * ParameterizedTypeReference<String>() { }); return response.getBody(); }
	 */

	public String importApiProxy(CommonConfiguration cfg, File revisionBundle) throws Exception {
		final String encodedCredentials;
		final String contentType;
		String URL;

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			URL = getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/apis?action=import&validate=true&name="
					+ cfg.getApiName(), cfg);
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE.toString();
			encodedCredentials = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} else {
			URL = getSecureURL(
					"v1/organizations/" + cfg.getOrganization() + "/apis?action=import&name=" + cfg.getApiName(), cfg);
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE.toString();
			encodedCredentials = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		}

		final InputStream fis = new FileInputStream(revisionBundle);
		final RequestCallback requestCallback = new RequestCallback() {
			@Override
			public void doWithRequest(final ClientHttpRequest request) throws IOException {
				request.getHeaders().add("Content-Type", contentType);
				request.getHeaders().add("Authorization", encodedCredentials);
				IOUtils.copy(fis, request.getBody());
			}
		};

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setBufferRequestBody(false);
		restTemplate.setRequestFactory(requestFactory);
		final HttpMessageConverterExtractor<String> responseExtractor = new HttpMessageConverterExtractor<String>(
				String.class, restTemplate.getMessageConverters());
		String response = restTemplate.execute(URL, HttpMethod.POST, requestCallback, responseExtractor);
		requestFactory.setBufferRequestBody(true);
		restTemplate.setRequestFactory(requestFactory);
		return response;
	}

	public String importSharedflows(CommonConfiguration cfg, File revisionBundle) throws Exception {

		final String encodedCredentials;
		String URL;

		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			URL = getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/sharedflows/?action=import&name="
					+ cfg.getSharedflowName(), cfg);
			encodedCredentials = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} else {
			URL = getSecureURL("v1/organizations/" + cfg.getOrganization() + "/sharedflows/?action=import&name="
					+ cfg.getSharedflowName(), cfg);
			encodedCredentials = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		}

		final InputStream fis = new FileInputStream(revisionBundle);
		final RequestCallback requestCallback = new RequestCallback() {
			@Override
			public void doWithRequest(final ClientHttpRequest request) throws IOException {
				request.getHeaders().add("Content-type", "application/octet-stream");
				request.getHeaders().add("Authorization", encodedCredentials);
				IOUtils.copy(fis, request.getBody());
			}
		};

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setBufferRequestBody(false);
		restTemplate.setRequestFactory(requestFactory);
		final HttpMessageConverterExtractor<String> responseExtractor = new HttpMessageConverterExtractor<String>(
				String.class, restTemplate.getMessageConverters());
		String response = restTemplate.execute(URL, HttpMethod.POST, requestCallback, responseExtractor);
		requestFactory.setBufferRequestBody(true);
		restTemplate.setRequestFactory(requestFactory);
		return response;
	}

	public String getTraceResponse(CommonConfiguration cfg, String sessionID, String tid)
			throws ItorixException, InterruptedException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			// Thread.sleep(3000);
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
							+ sessionID + "/data/" + tid, cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		} else {
			ResponseEntity<String> response = exchange(
					getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
							+ sessionID + "/data/" + tid, cfg),
					HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
	}

	public String getXMLTraceResponse(CommonConfiguration cfg, String sessionID, String tid) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
						+ sessionID + "/data/" + tid, cfg),
				HttpMethod.GET, getXMLHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String getTraceResponse(String org, String env, String rev, String apiproxy, String userName,
			String password, String sessionID, String tid) throws ItorixException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(userName);
		cfg.setApigeePassword(password);

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + org + "/environments/" + env + "/apis/" + apiproxy + "/revisions/"
						+ rev + "/debugsessions/" + sessionID + "/data/" + tid, cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String getTraceResponseAsXml(CommonConfiguration cfg, String sessionID, String tid) throws ItorixException {
		/*
		 * CommonConfiguration cfg = new CommonConfiguration();
		 * cfg.setApigeeEmail(userName); cfg.setApigeePassword(password);
		 */

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
						+ sessionID + "/data/" + tid, cfg),
				HttpMethod.GET, getHttpEntityAndXmlAcceptType(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public String getTraceResponseAsXml(String org, String env, String rev, String apiproxy, String userName,
			String password, String sessionID, String tid) throws ItorixException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(userName);
		cfg.setApigeePassword(password);

		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + org + "/environments/" + env + "/apis/" + apiproxy + "/revisions/"
						+ rev + "/debugsessions/" + sessionID + "/data/" + tid, cfg),
				HttpMethod.GET, getHttpEntityAndXmlAcceptType(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		return response.getBody();
	}

	public Object[] getProxyEndPoints(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<Object[]> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/proxies", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<Object[]>() {
				}, cfg);
		return response.getBody();
	}

	public Object[] getTargetEndPoints(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<Object[]> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/apis/" + cfg.getApiName() + "/revisions/"
						+ cfg.getRevision() + "/targets", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<Object[]>() {
				}, cfg);
		return response.getBody();
	}

	public String createSession(CommonConfiguration cfg) throws ItorixException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			String body = "{\"validity\":600,\"count\":15,\"tracesize\":5120, \"timeout\": \"300\"}";
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions", cfg),
					HttpMethod.POST, postHttpEntityX(cfg, body), new ParameterizedTypeReference<String>() {
					}, cfg);
			JSONObject sessionObj = (JSONObject) JSONSerializer.toJSON(response.getBody());
			String sessionid = (String) sessionObj.get("name");
			return sessionid;
		} else {
			UUID uuid = UUID.randomUUID();
			String randomSessionId = uuid.toString().substring(0, 2);
			ResponseEntity<String> response = postForEntity(
					getSecureURL(
							"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
									+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision()
									+ "/debugsessions?session=" + randomSessionId + "&header_itorix=" + randomSessionId,
							cfg),
					getHttpEntity(cfg), String.class);

			return randomSessionId;
		}
	}

	public String getListOfTransactionIds(CommonConfiguration cfg, String sessionID)
			throws ItorixException, InterruptedException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			Thread.sleep(3000);
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
							+ sessionID + "/data", cfg),
					HttpMethod.GET, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		} else {
			ResponseEntity<String> response = exchange(
					getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
							+ sessionID + "/data", cfg),
					HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
	}

	public String deleteSession(CommonConfiguration cfg, String sessionID) throws ItorixException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			ResponseEntity<String> response = exchange(
					getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
							+ "/apis/" + cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/"
							+ sessionID + "/data", cfg),
					HttpMethod.DELETE, getHttpEntityX(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		} else {
			ResponseEntity<String> response = exchange(getSecureURL(
					"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment() + "/apis/"
							+ cfg.getApiName() + "/revisions/" + cfg.getRevision() + "/debugsessions/" + sessionID,
					cfg), HttpMethod.DELETE, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
					}, cfg);
			return response.getBody();
		}
	}

	private HttpEntity<String> getHttpEntity(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		if (StringUtils.isNotEmpty(cfg.getApigeeEmail()) && StringUtils.isNotEmpty(
				cfg.getApigeePassword())) {
			String encodedCredentials = Base64
					.encodeBase64String((cfg.getApigeeEmail() + ":" + cfg.getApigeePassword()).getBytes());
			headers.set("Authorization", "Basic " + encodedCredentials);
		} else if (StringUtils.isEmpty(cfg.getApigeeEmail()) && StringUtils.isEmpty(
				cfg.getApigeePassword())) {
			String authorization = getApigeeAuth(cfg.getOrganization(), cfg.getType());
			headers.set("Authorization", authorization);
		}

		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityX(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String authorization = null;
		try {
			authorization = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		headers.set("Authorization", authorization);
		return new HttpEntity<String>(headers);
	}
	private HttpEntity<String> postHttpEntityX(CommonConfiguration cfg, String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String authorization = null;
		try {
			authorization = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		headers.set("Authorization", authorization);
		return new HttpEntity<String>(body, headers);
	}

	private HttpEntity<String> getXMLHttpEntity(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_XML));
		String encodedCredentials = Base64
				.encodeBase64String((cfg.getApigeeEmail() + ":" + cfg.getApigeePassword()).getBytes());
		headers.set("Authorization", "Basic " + encodedCredentials);
		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> postHttpEntity(CommonConfiguration cfg, String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String authorization = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", authorization);
		return new HttpEntity<String>(body, headers);
	}

	private HttpEntity<String> postHttpEntityForOctetStream(CommonConfiguration cfg, String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String authorization = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", authorization);
		return new HttpEntity<String>(body, headers);
	}

	public HttpEntity<String> getHttpEntityWithApplicationJSON(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String encodedCredentials = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", encodedCredentials);
		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityWithOctetStreamContentType(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String encodedCredentials = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", encodedCredentials);
		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityXWithOctetStreamContentType(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		String authorization = null;
		try {
			authorization = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		headers.set("Authorization", authorization);
		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityWithBodyJSON(CommonConfiguration cfg, String request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String encodedCredentials = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", encodedCredentials);
		return new HttpEntity<String>(request, headers);
	}

	private HttpEntity<String> getHttpEntityXWithBodyJSON(CommonConfiguration cfg, String request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String encodedCredentials = null;
		try {
			encodedCredentials = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		headers.set("Authorization", encodedCredentials);
		return new HttpEntity<String>(request, headers);
	}

	private HttpEntity<String> getHttpEntityToDownloadFile(CommonConfiguration cfg) {
		HttpHeaders headers = new HttpHeaders();
		// headers.set("Authorization", "Basic " + encodedCredentials);
		String authorization = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		headers.set("Authorization", authorization);
		return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityXToDownloadFile(CommonConfiguration cfg) {

		HttpHeaders headers = new HttpHeaders();
		// headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String authorization = null;
		try {
			authorization = apigeexUtil.getApigeeCredentials(cfg.getOrganization(), cfg.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		headers.set("Authorization", authorization);
		return new HttpEntity<String>("parameters", headers);

		// HttpHeaders headers = new HttpHeaders();
		// headers.set("Authorization", "Basic " + encodedCredentials);
		// String authorization = getApigeeAuth(cfg.getOrganization(),
		// cfg.getType());
		// headers.set("Authorization", authorization);
		// return new HttpEntity<String>("parameters", headers);
	}

	private HttpEntity<String> getHttpEntityAndXmlAcceptType(CommonConfiguration cfg) {
		String auth = getApigeeAuth(cfg.getOrganization(), cfg.getType());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String encodedCredentials = Base64
				.encodeBase64String((cfg.getApigeeEmail() + ":" + cfg.getApigeePassword()).getBytes());
		headers.set("Authorization", auth);
		headers.set("Accept", "application/xml");
		return new HttpEntity<String>("parameters", headers);
	}

	private File getBaseBackupDirectory(boolean rollover, CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOrganization());

		if (rollover && backupDirectory.exists()) {
			backupDirectory.renameTo(new File(backupDirectory.getAbsolutePath() + "__" + System.currentTimeMillis()));
		}

		backupDirectory.mkdirs();
		return backupDirectory;
	}

	private HttpEntity<Object> getHttpEntity1() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("accountNumber", "932942416");
		params.add("phoneNumber", "4042008190");
		params.add("startTime", "2016-08-09T07:15:11.000-07:00");
		params.add("endTime", "2016-08-10T10:01:13.000-07:00");
		params.add("pageNumber", "1");
		params.add("pageSize", "10");
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Bearer " + "ScrRi19MJEX5CfDODAAAbt35rQgw");
		return new HttpEntity<Object>(params, headers);
	}

	public String postCall() {

		Object o = restTemplate.exchange("", HttpMethod.POST, getHttpEntity1(), Object.class);
		logger.debug(o.toString());
		// JsonNode obj = restTemplate.postForObject("", params,
		// JsonNode.class);
		return null;
	}

	/**
	 * Returns information about every API proxy deployed to the environment
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public String getAPIsDeployedToEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/deployments", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, 1, cfg);
		return response.getBody();
	}

	public String getOrganization(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(getSecureURL("v1/organizations/" + cfg.getOrganization(), cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<String>() {
				}, cfg);
		if (response.getStatusCode().value() == 404) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1000"), "Apigee-1000");
		}
		return response.getBody();
	}

	public List<String> listKeysInAnEnvironmentKeyValueMap(CommonConfiguration cfg, String map_name)
			throws ItorixException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			ResponseEntity<List<String>> response = exchange(getApigeexURL(
							"/v1/organizations/" + cfg.getOrganization() + "/keyvaluemaps/" + map_name + "/entries", cfg),
					HttpMethod.POST, getHttpEntityX(cfg), new ParameterizedTypeReference<List<String>>(){
					}, cfg);

			return response.getBody();
		}
		ResponseEntity<List<String>> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps/" + map_name + "/keys", cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();
	}

	public List<String> listXKeysInAnEnvironmentKeyValueMap(CommonConfiguration cfg, String map_name)
			throws ItorixException {

		ResponseEntity<String> response = exchange(getApigeexURL(
						"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/keyvaluemaps/" + map_name + "/entries", cfg),
				HttpMethod.GET, getHttpEntityX(cfg), String.class, cfg);

		List<String> keys = new ArrayList<String>();
		if (response.getStatusCode().is2xxSuccessful()) {
			JSONObject keyValueResponse = (JSONObject) JSONSerializer.toJSON(response.getBody());
			JSONArray keyValue = keyValueResponse.getJSONArray("keyValueEntries");

			for (Object keyValueEntry : keyValue) {
				if (keyValueEntry instanceof JSONObject) {
					JSONObject entryJson = (JSONObject) keyValueEntry;
					String entry_name = entryJson.getString("name");
					keys.add(entry_name);
				}
			}
		}
		return keys;
	}

	public boolean isKeyValueMap(CommonConfiguration cfg, String map_name) throws ItorixException {
		ResponseEntity<Object> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps/" + map_name, cfg),
				HttpMethod.GET, getHttpEntity(cfg), new ParameterizedTypeReference<Object>() {
				}, cfg);
		if (response.getStatusCode().is2xxSuccessful()) {
			return true;
		} else {
			return false;
		}
	}

	public String createAnEntryInAnEnvironmentKeyValueMap(CommonConfiguration cfg, String map_name)
			throws ItorixException {

		ResponseEntity<String> response = exchange(
				getSecureURL(
						"v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/keyvaluemaps/" + map_name + "/entries", cfg),
				HttpMethod.POST, getHttpEntityWithBodyJSON(cfg, cfg.getResource()), String.class, cfg);
		return response.getBody();
	}

	public String createKeyValueMapInAnEnvironment(CommonConfiguration cfg) throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps", cfg),
				HttpMethod.POST, getHttpEntityWithBodyJSON(cfg, cfg.getResource()), String.class, cfg);
		return response.getBody();
	}

	public String createXKeyValueMapInAnEnvironment(CommonConfiguration cfg, String map_name) throws ItorixException {

		HttpHeaders headers = getHttpEntityX(cfg).getHeaders();
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("name", map_name);
		bodyObj.put("encrypted", true);
		HttpEntity<Object> entity = new HttpEntity<>(bodyObj.toString(), headers);


		ResponseEntity<String> response = exchange(
				getApigeexURL("/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps", cfg),
				HttpMethod.POST, entity, String.class, cfg);

		logger.info("HttpRequest : {}", entity);
		return response.getBody();
	}

	public String createXKeyValueMapEntryInAnEnvironment(CommonConfiguration cfg, String map_name,
			String entryName, Object entryValue) throws ItorixException {

		HttpHeaders headers = getHttpEntityX(cfg).getHeaders();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", entryName);
		map.put("value", entryValue);
		HttpEntity<Object> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = exchange(
				getApigeexURL(
						"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/keyvaluemaps", cfg),
				HttpMethod.POST, entity, String.class, cfg);

		return response.getBody();
	}

	public String updateAKeyValueMapEntryInAnEnvironment(CommonConfiguration cfg, String map_name, String entry_name)
			throws ItorixException {
		ResponseEntity<String> response = exchange(
				getSecureURL("v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
						+ "/keyvaluemaps/" + map_name + "/entries/" + entry_name, cfg),
				HttpMethod.POST, getHttpEntityWithBodyJSON(cfg, cfg.getResource()), String.class, cfg);
		return response.getBody();
	}

	public String updateXAKeyValueMapEntryInAnEnvironment(CommonConfiguration cfg, String map_name,
			String entry_name, Object entry_value)
			throws ItorixException {

		ResponseEntity<String> response = exchange(getApigeexURL(
						"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/keyvaluemaps/" + map_name + "/entries" + entry_name,
						cfg),
				HttpMethod.DELETE, getHttpEntityX(cfg), String.class, cfg);

		if (response.getStatusCode().is2xxSuccessful()) {
			return createXKeyValueMapEntryInAnEnvironment(cfg, map_name, entry_name, entry_value);
		}

		return response.getBody();
	}

	public String getMetricsData(CommonConfiguration cfg, String suffix) throws ItorixException {

		ResponseEntity<String> response = exchange(getSecureURL(suffix, cfg), HttpMethod.GET, getHttpEntity(cfg),
				String.class, cfg);
		logger.debug(response.getBody().toString());
		return response.getBody();
	}

	public ApigeeServiceUser getApigeeServiceAccount(String org, String type) {
		if (type == null)
			type = "saas";
		ApigeeServiceUser serviceUser = null;
		try {
			Query query = new Query(Criteria.where("orgname").is(org).and("type").is(type));
			ApigeeConfigurationVO apigeeConfigurationVO = mongoTemplate.findOne(query, ApigeeConfigurationVO.class);
			if (apigeeConfigurationVO == null) {
				serviceUser = new ApigeeServiceUser();
				serviceUser.setOrgName(org);
				serviceUser.setType(type);
				// serviceUser.setUserName(applicationProperties.getApigeeServiceUsername());
				// serviceUser.setPassword(new
				// RSAEncryption().encryptText(applicationProperties.getApigeeServicePassword()));
			} else
				serviceUser = apigeeConfigurationVO.getApigeeServiceUser();
		} catch (Exception e) {
			serviceUser = new ApigeeServiceUser();
			serviceUser.setOrgName(org);
			serviceUser.setType(type);
			// serviceUser.setUserName(applicationProperties.getApigeeServiceUsername());
			try {
				// serviceUser.setPassword(new
				// RSAEncryption().encryptText(applicationProperties.getApigeeServicePassword()));
			} catch (Exception ex) {
				logger.error("Exception occurred", ex);
			}
		}
		return serviceUser;
	}

	/*
	 * public ApigeeServiceUser getApigeeServiceAccount(String id, String org,
	 * String type){ if(type == null) type = "saas"; ApigeeConfigurationVO
	 * apigeeConfigurationVO = null; ApigeeServiceUser serviceUser; try { Query
	 * query = new Query( Criteria.where("id").is(id)); apigeeConfigurationVO =
	 * mongoTemplate.findOne(query, ApigeeConfigurationVO.class); if
	 * (apigeeConfigurationVO == null) { serviceUser = new ApigeeServiceUser();
	 * serviceUser.setOrgName(org); serviceUser.setType(type);
	 * serviceUser.setUserName(applicationProperties.getApigeeServiceUsername())
	 * ; serviceUser.setPassword(new
	 * RSAEncryption().encryptText(applicationProperties.
	 * getApigeeServicePassword())); } else { serviceUser =
	 * apigeeConfigurationVO.getApigeeServiceUser(); } }catch(Exception e) {
	 * serviceUser = new ApigeeServiceUser(); serviceUser.setOrgName(org);
	 * serviceUser.setType(type);
	 * serviceUser.setUserName(applicationProperties.getApigeeServiceUsername())
	 * ; try { serviceUser.setPassword(new
	 * RSAEncryption().encryptText(applicationProperties.
	 * getApigeeServicePassword())); } catch(Exception ex) {
	 * log.error("Exception occurred")(); } } return serviceUser; }
	 */

	public String getApigeeAuth(String org, String type) {
		if (type == null)
			type = "saas";
		ApigeeServiceUser serviceUser = getApigeeServiceAccount(org, type);
		if (serviceUser.getAuthType().equals("basic")) {
			return "Basic " + Base64.encodeBase64String(
					(serviceUser.getUserName() + ":" + serviceUser.getDecryptedPassword()).getBytes());
		} else {
			return "Bearer " + getAccessToken(serviceUser.getTokenURL(), serviceUser.getBasicToken(),
					serviceUser.getGrantType(), serviceUser.getUserName(), serviceUser.getDecryptedPassword());
		}
	}

	private String getAccessToken(String url, String key, String grantType, String username, String password) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String token = null;
		ResponseEntity<String> result = null;
		try {
			headers.add("Authorization", key);
			headers.add("Content-Type", "application/x-www-form-urlencoded");
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("username", username);
			map.add("password", password);
			map.add("grant_type", grantType);
			HttpEntity<Object> request = new HttpEntity<Object>(map, headers);
			result = restTemplate.postForEntity(url, request, String.class);
			if (result.getStatusCode().equals(HttpStatus.OK)) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonHistory = mapper.readTree(result.getBody());
				token = jsonHistory.get("access_token").asText();
			}
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			// throw e;
		}
		return token;
	}

	public List<String> getKeyValueMapsX(CommonConfiguration cfg)
			throws ItorixException {
		ResponseEntity<List<String>> response = exchange(
				getApigeexURL(
						"/v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
								+ "/keyvaluemaps", cfg),
				HttpMethod.GET,
				getHttpEntityX(cfg), new ParameterizedTypeReference<List<String>>() {
				}, cfg);
		return response.getBody();

	}

	public Map<String, Object> getOrganizationProfile(String organization) throws JsonProcessingException{
		String url = String.format("https://%s/v1/mint/organizations/%s/",apigeeHost,organization);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization",getApigeeAuth(organization,null));
		HttpEntity<String> requestEntity = new HttpEntity(headers);

		ResponseEntity<JSONObject> res = restTemplate.exchange(url,HttpMethod.GET,requestEntity,JSONObject.class);
		if(res.getStatusCodeValue() == 200) {
			return (Map<String, Object>) res.getBody();
		}
		return null;
	}

	public Map<String,LinkedHashMap> getCurrencyProfile(String organization,String ccy){
		String url = String.format("https://%s/v1/mint/organizations/%s/supported-currencies/%s",apigeeHost,organization,ccy);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization",getApigeeAuth(organization,null));
		HttpEntity<Object> requestEntity = new HttpEntity(headers);

		ResponseEntity<Object> res = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Object.class);
		if(res.getStatusCodeValue() == 200){
			return (Map<String, LinkedHashMap>) res.getBody();
		}

		return null;
	}

	public Map<String, Object> getMonetizationPackage(String organization, String productBundleId){
		String url = String.format("https://%s/v1/mint/organizations/%s/monetization-packages/%s",apigeeHost,organization,productBundleId);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization",getApigeeAuth(organization,null));
		HttpEntity<Object> requestEntity = new HttpEntity(headers);

		ResponseEntity<Object> res = restTemplate.exchange(url,HttpMethod.GET,requestEntity,Object.class);
		if(res.getStatusCodeValue() == 200){
			return (Map<String, Object>) res.getBody();
		}

		return null;
	}

	public Map<String,Object> createOrUpdateRatePlan(String organization, String apigeeRatePlanId, ApigeeRatePlan apigeeRatePlan,String productBundleId){

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", getApigeeAuth(organization,null));
		HttpEntity<Object> requestEntity = new HttpEntity(apigeeRatePlan,headers);

		if(apigeeRatePlanId != null){
			String url = String.format("https://%s/v1/mint/organizations/%s/monetization-packages/%s/rate-plans/%s",apigeeHost,organization,productBundleId,apigeeRatePlanId);			ResponseEntity<Object> res = restTemplate.exchange(url,HttpMethod.PUT,requestEntity,Object.class);
			if(res.getStatusCodeValue() == 200){
				return (Map<String, Object>) res.getBody();
			}
		}else{
			String url = String.format("https://%s/v1/mint/organizations/%s/monetization-packages/%s/rate-plans",apigeeHost,organization,productBundleId);			ResponseEntity<Object> res = restTemplate.exchange(url,HttpMethod.POST,requestEntity,Object.class);
			if(res.getStatusCodeValue() == 201){
				return (Map<String, Object>) res.getBody();
			}
		}
		return null;
	}
}
