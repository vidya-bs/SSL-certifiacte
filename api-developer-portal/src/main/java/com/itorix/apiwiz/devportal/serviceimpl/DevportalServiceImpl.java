package com.itorix.apiwiz.devportal.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.azure.AzureConfigurationVO;
import com.itorix.apiwiz.common.model.azure.AzureProductResponse;
import com.itorix.apiwiz.common.model.azure.AzureProductResponseDTO;
import com.itorix.apiwiz.common.model.azure.AzureProductValues;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.kong.Consumer;
import com.itorix.apiwiz.common.model.kong.ConsumerDTO;
import com.itorix.apiwiz.common.model.kong.ConsumerResponse;
import com.itorix.apiwiz.common.model.kong.KongRuntime;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.devportal.dao.DevportalDao;
import com.itorix.apiwiz.devportal.model.DeveloperApp;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseRecord;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseResult;
import com.itorix.apiwiz.devportal.service.DevportalService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
public class DevportalServiceImpl implements DevportalService {

	private Logger logger = Logger.getLogger(DevportalDao.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ApigeeUtil apigeeUtil;

	@Autowired
	private ApigeeXUtill apigeexUtil;

	@Autowired
	private DevportalDao devportaldao;

	@Override
	public ResponseEntity<String> createDeveloper(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org, @RequestBody String body)
			throws Exception {
		if (body != null) {
			logger.debug("Returning proxy service for devportaldao");
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers";
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers";
				HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			}
		}
		return null;
	}

	@Override
	public org.springframework.http.ResponseEntity<String> registerApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestBody Map<String,Object> body) throws Exception {
		if (body != null) {
			ObjectMapper mapper = new ObjectMapper();
			DeveloperApp developerApp = new DeveloperApp();
			developerApp.setOrganization(org);
			developerApp.setEmail(email);
			developerApp.setAppName(body.get("name") != null ? body.get("name").toString():"");
			developerApp.setDescription(body.get("description") != null ? body.get("description").toString():"");
			developerApp.setProductBundle(body.get("productBundle") != null ? mapper.convertValue(body.get("productBundle"),ProductBundle.class):null);
			developerApp.setRatePlan(body.get("ratePlan") != null ? mapper.convertValue(body.get("ratePlan"),RatePlan.class):null);
			body.remove("description");
			body.remove("productBundle");
			body.remove("ratePlan");
			String bodyToApigee = mapper.writeValueAsString(body);
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps";
				logger.info(String.format("Making a call to Apigee with payload %s", bodyToApigee));
				HTTPUtil httpConn = new HTTPUtil(bodyToApigee, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps";
				logger.info(String.format("Making a call to Apigee with payload %s", bodyToApigee));
				HTTPUtil httpConn = new HTTPUtil(bodyToApigee, URL, getEncodedCredentials(org, type));
				ResponseEntity<String> response =  devportaldao.proxyService(httpConn, "POST");
				JSONObject json = mapper.readValue(response.getBody(), JSONObject.class);
				developerApp.setAppId(json.getString("appId"));
				devportaldao.saveDeveloperApp(developerApp);
				return response;
			}
		}
		return null;
	}


	@Override
	public List<DeveloperApp> getRegisteredApps(String jsessionId, String interactionid, String org,
			String email, String appId, String appName) throws Exception {
		return devportaldao.getRegisteredApps(org,email,appId,appName);
	}

	@Override
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@PathVariable("appName") String appName,
			@RequestParam(value = "status", required = false) String status,
			@RequestBody String body)
			throws Exception {
		if (body != null) {
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
				if(status != null){
					try{
						String statusUrl = URL+"?action="+status;
						statusUrl = statusUrl.replace("//v1/organizations","/v1/organizations");
						HttpHeaders headers = new HttpHeaders();
						headers.set("Authorization",apigeexUtil.getApigeeCredentials(org, type));
						HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

						ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST,requestEntity,Void.class);
					}catch (Exception ex){
						logger.error("Could Not Update App Status:" + ex.getMessage());
					}
				}
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "PUT");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
				if(status != null){
					try{
						String statusUrl = URL+"?action="+status;
						statusUrl = statusUrl.replace("//v1/organizations","/v1/organizations");
						HttpHeaders headers = new HttpHeaders();
						headers.set("Authorization",getEncodedCredentials(org, type));
						HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

						ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST,requestEntity,Void.class);
					}catch (Exception ex){
						logger.error("Could Not Update App Status:" + ex.getMessage());
					}
				}
				HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
				return devportaldao.proxyService(httpConn, "PUT");
			}
		}
		return null;
	}

	@Override
	public org.springframework.http.ResponseEntity<String> updateAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @RequestBody String body) throws Exception {
		if (body != null) {
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName + "/keys/" + appKey;
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName + "/keys/" + appKey;
				HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			}
		}
		return null;
	}

	@Override
	public org.springframework.http.ResponseEntity<String> deleteAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @PathVariable("product") String product) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email + "/apps/"
					+ appName + "/keys/" + appKey + "/apiproducts/" + product;
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			return devportaldao.proxyService(httpConn, "DELETE");
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
					+ "/apps/" + appName + "/keys/" + appKey + "/apiproducts/" + product;
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			return devportaldao.proxyService(httpConn, "DELETE");
		}
	}

	@Override
	public org.springframework.http.ResponseEntity<String> deleteApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email + "/apps/"
					+ appName;
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			return devportaldao.proxyService(httpConn, "DELETE");
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
					+ "/apps/" + appName;
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			return devportaldao.proxyService(httpConn, "DELETE");
		}
	}

	@Override
	public org.springframework.http.ResponseEntity<String> getProducts(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org,
			@RequestParam(value = "expand", required = false) String expand) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL;
			if (expand != null && expand != "")
				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apiproducts?expand=" + expand;
			else
				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apiproducts";
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
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
				logger.error(e);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<String> responseEntity = new ResponseEntity<String>(
					objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
			return responseEntity;
		} else {
			String URL;
			if (expand != null && expand != "")
				URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/apiproducts?expand="
						+ expand;
			else
				URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/apiproducts";
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			return devportaldao.proxyService(httpConn, "GET");
		}
	}

	@Override
	public org.springframework.http.ResponseEntity<String> getApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestParam(value = "expand", required = false) String expand)
			throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL;
			if (expand != null && expand != "")
				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps?expand=" + expand;
			else
				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email + "/apps";
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
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
				logger.error("Exception occurred", e);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<String> responseEntity = new ResponseEntity<String>(
					objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
			return responseEntity;
		} else {
			String URL;
			if (expand != null && expand != "")
				URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps?expand=" + expand;
			else
				URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps";
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
			JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(response.getBody());
			JSONArray apiProducts = (JSONArray) proxyObject.get("app");
			List<JSONObject> responseObject = new ArrayList<>();
			for (Object apiObj : apiProducts) {
				JSONObject prodObj = (JSONObject) apiObj;
				String appId = prodObj.get("appId").toString();
				DeveloperApp developerApp = devportaldao.getDeveloperAppWithAppId(appId);
				if(developerApp != null) {
					prodObj.put("ratePlan", developerApp.getRatePlan());
					prodObj.put("productBundle", developerApp.getProductBundle());
					prodObj.put("description", developerApp.getDescription());
				}
				responseObject.add(prodObj);
			}
			JSONObject finalResponse = new JSONObject();
			finalResponse.put("app",responseObject);
			return new ResponseEntity<>(finalResponse.toString(),HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getAppsByOrganisation(String jsessionId, String interactionid,
			String gwtype, String type, String org) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
//			String URL;
//			if (expand != null && expand != "")
//				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org
//						+ "/apps?expand=" + expand;
//			else
//				URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apps";
//			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
//			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
//			List<String> products = new ArrayList<>();
//			String apiProductString = response.getBody();
//			try {
//				JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
//				JSONArray apiProducts = (JSONArray) proxyObject.get("app");
//				for (Object apiObj : apiProducts) {
//					JSONObject prodObj = (JSONObject) apiObj;
//					final String apiProduct = (String) prodObj.get("appId");
//					products.add(apiProduct);
//				}
//			} catch (Exception e) {
//				logger.error("Exception occurred", e);
//			}
//			ObjectMapper objectMapper = new ObjectMapper();
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//			ResponseEntity<String> responseEntity = new ResponseEntity<String>(
//					objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
//			return responseEntity;
			return ResponseEntity.ok("{}");
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org
					+ "/apps?expand=true";

			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
			JSONArray appResponseList = new JSONArray();
			String appList = response.getBody();
			try {
				JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(appList);
				JSONArray apps = (JSONArray) proxyObject.get("app");
				for (Object appsObj : apps) {
					JSONObject appDetails = new JSONObject();
					JSONObject prodObj = (JSONObject) appsObj;
					appDetails.put("appId",(String) prodObj.get("appId"));
					JSONArray json = (JSONArray) prodObj.get("attributes");
					for(Object obj : json){
						if(obj instanceof  JSONObject){
							JSONObject nameObject = (JSONObject) obj;
							if (StringUtils.equalsIgnoreCase(nameObject.getString("name"), "DisplayName")){
								appDetails.put("appName", nameObject.getString("value"));
							}

						}

					}
					appResponseList.add(appDetails);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());

			}
			return ResponseEntity.ok(appResponseList);
		}
	}

	public ResponseEntity<?> apigeeXAppsHelper(String type,String org) throws Exception {
		String URL;
		URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apps";
		HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
		ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
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
			logger.error("Exception occurred", e);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(
				objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
		return responseEntity;
	}

	public JSONArray apigeeAppsHelper(String type,String org) throws ItorixException {
		String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org
				+ "/apps?expand=true";

		HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
		ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
		JSONArray appResponseList = new JSONArray();
		String appList = response.getBody();
		try {
			JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(appList);
			JSONArray apps = (JSONArray) proxyObject.get("app");
			for (Object appsObj : apps) {
				JSONObject appDetails = new JSONObject();
				JSONObject prodObj = (JSONObject) appsObj;
				appDetails.put("appId", (String) prodObj.get("appId"));
				JSONArray json = (JSONArray) prodObj.get("attributes");
				for (Object obj : json) {
					if (obj instanceof JSONObject) {
						JSONObject nameObject = (JSONObject) obj;
						if (StringUtils.equalsIgnoreCase(nameObject.getString("name"), "DisplayName")) {
							appDetails.put("appName", nameObject.getString("value"));
						}
					}
				}
				appResponseList.add(appDetails);
			}
		}catch (Exception e) {
			logger.error(e.getMessage());
		}
		return appResponseList;
	}

	public static <T> HttpEntity<T> requestEntity(T body, String azureAccessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", azureAccessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (body != null) {
			return new HttpEntity<>(body, headers);
		}
		return new HttpEntity<>(headers);
	}

	public Object getProductsFromAzure(String connectorName) throws ItorixException {
		AzureConfigurationVO connector = devportaldao.getConnector(connectorName);
		String url =String.format("https://%s/subscriptions/%s/resourceGroups/%s/providers/Microsoft.ApiManagement/service/%s/%s?api-version=%s", connector.getManagementHost(), connector.getSubscriptionId(), connector.getResourceGroup(), connector.getServiceName(), "products", connector.getApiVersion());
		try {
			ResponseEntity<AzureProductResponse> azureProductResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity(null, connector.getSharedAccessToken()), AzureProductResponse.class);
			List<AzureProductValues> response=azureProductResponse.getBody().getValue();
			List<AzureProductResponseDTO> azureProductResponseDTOS = new ArrayList<>();
			for(AzureProductValues azureProductValues:response){
				azureProductResponseDTOS.add(new AzureProductResponseDTO(azureProductValues.getName(),azureProductValues.getProperties().getDisplayName()));
			}
			return azureProductResponseDTOS;
		} catch (Exception e) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Azure-Connector-1022"), "Failed while syncing products."), "Azure-Connector-1022");
		}
	}

	public Object getConsumersFromKong(String runtime){
		try {
			KongRuntime kongRuntime = devportaldao.getKongRuntime(runtime);
			String url = String.format("%sconsumers/", kongRuntime.getKongAdminHost());
			HttpHeaders headers = new HttpHeaders();
			headers.set("Kong-Admin-Token", kongRuntime.getKongAdminToken());
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
			ResponseEntity<ConsumerResponse> consumers =  restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, ConsumerResponse.class);
			ConsumerResponse consumerResponse= consumers.getBody();

			List<ConsumerDTO> requiredConsumerList = new ArrayList<>();

			for(Consumer consumer : consumerResponse.getData()){
				requiredConsumerList.add(new ConsumerDTO(consumer.getId(),consumer.getUsername()));
			}
			return requiredConsumerList;
		}catch (Exception e){
			throw e;
		}
	}
	@Override
	public org.springframework.http.ResponseEntity<String> getApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email + "/apps/"
					+ appName;
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			return devportaldao.proxyService(httpConn, "GET");
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
					+ "/apps/" + appName;
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			return devportaldao.proxyService(httpConn, "GET");
		}
	}

	@Override
	public org.springframework.http.ResponseEntity<String> getPortalStats(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("env") String env, @RequestParam(value = "select", required = false) String select,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "timeUnit", required = false) String timeUnit,
			@RequestParam(value = "filter", required = false) String filter) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/environments/" + env
					+ "/stats/apps/";
			String query = "";
			if (select != null && select != "")
				query = "?select=" + select;
			if (timeRange != null && timeRange != "")
				query = query + "&timeRange=" + timeRange;
			if (timeUnit != null && timeUnit != "")
				query = query + "&timeUnit=" + timeUnit;
			if (filter != null && filter != "")
				query = query + "&filter=" + filter;
			URL = URL + query;
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			return devportaldao.proxyService(httpConn, "GET");
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/environments/" + env
					+ "/stats/apps/";
			String query = "";
			if (select != null && select != "")
				query = "?select=" + select;
			if (timeRange != null && timeRange != "")
				query = query + "&timeRange=" + timeRange;
			if (timeUnit != null && timeUnit != "")
				query = query + "&timeUnit=" + timeUnit;
			if (filter != null && filter != "")
				query = query + "&filter=" + filter;
			URL = URL + query;
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			return devportaldao.proxyService(httpConn, "GET");
		}
	}
	@Override
	public ResponseEntity<String> getProductsForPartner(String jsessionId, String interactionid,
			String partner, String type, String org) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL;
			URL =
					apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apiproducts?expand=true";
			HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
			ResponseEntity<String> responseEntity = getStringResponseEntity(partner,
					response);
			return responseEntity;
		} else {
			String URL;
			URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org
					+ "/apiproducts?expand=true";
			HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
			ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
			ResponseEntity<String> responseEntity = getStringResponseEntity(partner,
					response);
			return responseEntity;
		}
	}

	@Override
	public ResponseEntity<?> getProductBundleCards(String jsessionId, String interactionid,
			String partnerType,String org, int offset, int pagesize, boolean paginated) throws Exception {
		return new ResponseEntity<>(devportaldao.getProductBundleCards(partnerType,org,offset,pagesize,paginated),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> purchaseRatePlan(String jsessionId, String interactionid,
			PurchaseRecord purchaseRecord) throws Exception {
		PurchaseResult purchaseResult = devportaldao.executePurchase(purchaseRecord);

		if(purchaseResult.equals(PurchaseResult.SUCCESS)){
			return new ResponseEntity<>(purchaseRecord,HttpStatus.CREATED);
		}else if(purchaseResult.equals(PurchaseResult.INSUFFICIENT_BALANCE)){
			throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1010"),"Monetization-1010");
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1020"),"Monetization-1020");
		}
	}
	@Override
	public ResponseEntity<?> getPurchaseHistoryByAppId(String jsessionId, String interactionid, String appId)
		throws Exception {
		return new ResponseEntity<>(devportaldao.getPurchaseHistoryByAppId(appId),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> getPurchaseHistory(String jsessionId, String interactionid, String appId,
			String developerEmailId, String organization) throws Exception {

		return new ResponseEntity<>(devportaldao.getPurchaseHistory(appId,developerEmailId,organization),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> deletePurchaseById(String jsessionId, String interactionid, String appId,
			String purchaseId) throws Exception {
		devportaldao.deletePurchaseById(appId,purchaseId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	@Override
	public ResponseEntity<?> getWalletBalanceByFilter(String jsessionId, String interactionid, String appId,
			String email) throws Exception {
		return new ResponseEntity<>(devportaldao.getWalletBalanceByFilter(appId,email),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> getWalletBalanceByAppId(String jsessionId, String interactionid, String appId)
		throws Exception {
		return new ResponseEntity<>(devportaldao.getWalletBalanceByAppId(appId),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> addWalletBalanceForAppId(String jsessionId, String interactionid, double topUp,
	String appId) throws Exception {
		return new ResponseEntity<>(devportaldao.addWalletBalanceForAppId(appId,topUp),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> computeBillForAppId(String jsessionId, String interactionid, double transactions,String startDate, String endDate,String appId)
		throws Exception {
		return new ResponseEntity<>(devportaldao.computeBillForAppId(appId,transactions,startDate,endDate),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> updateProductStatus(String jsessionId, String interactionid, String gwtype,
			String type, String org, String developerEmailId, String appName, String consumerKey, String productName,
			String action) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + developerEmailId
					+ "/apps/" + appName + "/keys/" + consumerKey + "/apiproducts/" + productName;
			if(action != null){
				try{
					String statusUrl = URL+"?action="+action;
					statusUrl = statusUrl.replace("//v1/organizations","/v1/organizations");
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization",apigeexUtil.getApigeeCredentials(org, type));
					HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

					ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST,requestEntity,Void.class);
				}catch (Exception ex){
					logger.error("Could Not Update Product Status:" + ex.getMessage());
					throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1050"),"Monetization-1050");
				}
			}
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + developerEmailId
					+ "/apps/" + appName + "/keys/" + consumerKey + "/apiproducts/" + productName;
			if(action != null){
				try{
					String statusUrl = URL+"?action="+action;
					statusUrl = statusUrl.replace("//v1/organizations","/v1/organizations");
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization",getEncodedCredentials(org, type));
					HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

					ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST,requestEntity,Void.class);
				}catch (Exception ex){
					logger.error("Could Not Update Product Status:" + ex.getMessage());
					throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1050"),"Monetization-1050");
				}
			}
		}
		return new ResponseEntity<>("Successfully Updated Product Status",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllGateways(String jsessionId, String interactionid,
			String gwtype, String type) throws Exception {
		return new ResponseEntity<>(devportaldao.getAllGateways(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getGatewayDetails(String jsessionId, String interactionid, String gwtype,
			String type, String gateway) throws Exception {
		return new ResponseEntity<>(devportaldao.getGatewayInfo(gateway),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getGatewayDetails(String jsessionId, String interactionid, String gwtype,
			String type, String gateway, String env) throws Exception {

		Object object = new Object();
		if(gateway.equalsIgnoreCase("apigee")){
			//call Apigee With The Env
			object= apigeeAppsHelper("onprem",env);
		}
		if(gateway.equalsIgnoreCase("apigeeX")){
			//call ApigeeX With The Env
			object= apigeeXAppsHelper("apigeex",env);
		}
		if(gateway.equalsIgnoreCase("Kong")){
			//call Kong With The Env
			object=getConsumersFromKong(env);
		}
		if(gateway.equalsIgnoreCase("Azure")){
			//call Azure With The Env
			object=getProductsFromAzure(env);
		}

		return new ResponseEntity<>(object,HttpStatus.OK);
	}


	private ResponseEntity<String> getStringResponseEntity(String partner,
			ResponseEntity<String> response) throws JsonProcessingException, ItorixException {
		Set<String> products = new HashSet<>();
		List<String> partners = StringUtils.isNotBlank(partner) ? Arrays.asList(partner.split(","))
				: Collections.emptyList();
		String apiProductString = response.getBody();
		try {
			JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
			JSONArray apiProducts = (JSONArray) proxyObject.get("apiProduct");
			for (Object apiObj : apiProducts) {
				JSONObject prodObj = (JSONObject) apiObj;
				JSONArray attributes = (JSONArray) prodObj.get("attributes");
				for (Object objectNode : attributes) {
					JSONObject node = (JSONObject) objectNode;
					if (StringUtils.equalsIgnoreCase("partners", (String) node.get("name"))) {
						partners.forEach(selectedPartner -> {
							if (((String) node.get("value")).contains(selectedPartner)) {
								final String apiProduct = (String) prodObj.get("name");
								products.add(apiProduct);
							}
						});
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(
				objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
		return responseEntity;
	}

	private String getEncodedCredentials(String org, String type) {
		return apigeeUtil.getApigeeAuth(org, type);
	}
}
