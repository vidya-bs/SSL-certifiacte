package com.itorix.apiwiz.devportal.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.StaticFields;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.azure.AzureConfigurationVO;
import com.itorix.apiwiz.common.model.azure.AzureSubscriptionResponse;
import com.itorix.apiwiz.common.model.azure.AzureSubscriptionResponseDTO;
import com.itorix.apiwiz.common.model.azure.AzureSubscriptionValues;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.kong.*;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.devportal.model.DeveloperApp;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseRecord;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.devportal.dao.DevportalDao;
import com.itorix.apiwiz.devportal.service.DevportalService;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.springframework.data.mongodb.core.query.Query;
import java.util.*;
import java.util.stream.Collectors;

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

	@Autowired
	private MongoTemplate mongoTemplate;

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
			@PathVariable("email") String email, @RequestBody Map<String, Object> body) throws Exception {
		if (body != null) {
			ObjectMapper mapper = new ObjectMapper();
			DeveloperApp developerApp = new DeveloperApp();
			developerApp.setOrganization(org);
			developerApp.setEmail(email);
			developerApp.setAppName(body.get("name") != null ? body.get("name").toString() : "");
			developerApp.setDescription(body.get("description") != null ? body.get("description").toString() : "");
			developerApp.setProductBundle(body.get("productBundle") != null ? mapper.convertValue(body.get("productBundle"), ProductBundle.class) : null);
			developerApp.setRatePlan(body.get("ratePlan") != null ? mapper.convertValue(body.get("ratePlan"), RatePlan.class) : null);
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
				ResponseEntity<String> response = devportaldao.proxyService(httpConn, "POST");
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
		return devportaldao.getRegisteredApps(org, email, appId, appName);
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
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(body);
			String description = rootNode.get("description").asText();
			String appId=rootNode.get("appId").asText();
			Query query = new Query(Criteria.where("appId").is(appId));
			Update update = new Update().set("description", description);
			mongoTemplate.updateFirst(query, update, DeveloperApp.class);
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
				if (status != null) {
					try {
						String statusUrl = URL + "?action=" + status;
						statusUrl = statusUrl.replace("//v1/organizations", "/v1/organizations");
						HttpHeaders headers = new HttpHeaders();
						headers.set("Authorization", apigeexUtil.getApigeeCredentials(org, type));
						HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

						ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST, requestEntity, Void.class);
					} catch (Exception ex) {
						logger.error("Could Not Update App Status:" + ex.getMessage());
					}
				}
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "PUT");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
				if (status != null) {
					try {
						String statusUrl = URL + "?action=" + status;
						statusUrl = statusUrl.replace("//v1/organizations", "/v1/organizations");
						HttpHeaders headers = new HttpHeaders();
						headers.set("Authorization", getEncodedCredentials(org, type));
						HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

						ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST, requestEntity, Void.class);
					} catch (Exception ex) {
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
	private String allAppsWithGivenEmail(String type, String email,String expand,String org) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL;
			try {
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
				ResponseEntity<String> responseEntity = new ResponseEntity<String>(objectMapper.writeValueAsString(products), headers, HttpStatus.OK);
				return responseEntity.toString();
			}catch (Exception e){
				return null;
			}
		} else {
			String URL;
			try{
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
					if (developerApp != null) {
						prodObj.put("ratePlan", developerApp.getRatePlan());
						prodObj.put("productBundle", developerApp.getProductBundle());
						prodObj.put("description", developerApp.getDescription());
					}
					responseObject.add(prodObj);
				}
				JSONObject finalResponse = new JSONObject();
				finalResponse.put("app", responseObject);
				return finalResponse.toString();
			}catch (Exception e){
				return  null;
			}
		}
	}

	private List<JSONObject> convertJSONArrayToListOfJSONObjects(JSONArray jsonArray) {
		List<JSONObject> jsonObjectList = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			jsonObjectList.add(jsonArray.getJSONObject(i));
		}
		return jsonObjectList;
	}
	@Override
	public ResponseEntity<String> getAllOrgApps(String jsessionId, String interactionid,String email,String expand)
			throws Exception {
		if(email!=null){
			List<ApigeeXConfigurationVO> apigeexConnections = devportaldao.getApigeexConnectedOrgs();
			List<ApigeeConfigurationVO> apigeeConnections = devportaldao.getApigeeConnectedOrgs();

			List<JSONObject> allAppData = new ArrayList<>();

			if (apigeexConnections != null && !apigeexConnections.isEmpty()) {
				for (ApigeeXConfigurationVO apigeeXConfigurationVO : apigeexConnections) {
					String orgName = apigeeXConfigurationVO.getOrgName();
					String apigeexResponse = allAppsWithGivenEmail("apigeex", email, expand, orgName);
					if (apigeexResponse != null) {
						JSONObject apigeexData = (JSONObject) JSONSerializer.toJSON(apigeexResponse);
						JSONArray apigeexApps = apigeexData.getJSONArray("app");
						for(int i=0;i<apigeexApps.size();i++){
							JSONObject jsonObject=apigeexApps.getJSONObject(i);
							jsonObject.put("org",apigeeXConfigurationVO.getOrgName());
							jsonObject.put("orgType","saas");
						}
						allAppData.addAll(convertJSONArrayToListOfJSONObjects(apigeexApps));
					}
				}
			}

			if (apigeeConnections != null && !apigeeConnections.isEmpty()) {
				for (ApigeeConfigurationVO apigeeConfigurationVO : apigeeConnections) {
					String orgName = apigeeConfigurationVO.getOrgname();
					String apigeeResponse = allAppsWithGivenEmail(apigeeConfigurationVO.getType(), email, expand, orgName);
					if (apigeeResponse != null) {
						JSONObject apigeeData = (JSONObject) JSONSerializer.toJSON(apigeeResponse);
						JSONArray apigeeApps = apigeeData.getJSONArray("app");
						for(int i=0;i<apigeeApps.size();i++){
							JSONObject jsonObject=apigeeApps.getJSONObject(i);
							jsonObject.put("org",apigeeConfigurationVO.getOrgname());
							jsonObject.put("orgType",apigeeConfigurationVO.getType());
						}
						allAppData.addAll(convertJSONArrayToListOfJSONObjects(apigeeApps));
					}
				}
			}

			JSONObject finalResponse = new JSONObject();
			finalResponse.put("app", allAppData);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<String> responseEntity = new ResponseEntity<>(finalResponse.toString(), headers, HttpStatus.OK);
			return responseEntity;
		}
		else {
			List<ApigeeXConfigurationVO> apigeexConnections = devportaldao.getApigeexConnectedOrgs();
			JSONArray products = new JSONArray();
			if (apigeexConnections != null && !apigeexConnections.isEmpty()) {
				for (ApigeeXConfigurationVO apigeeXConfigurationVO : apigeexConnections) {
					String URL;
					if (apigeeXConfigurationVO != null) {
						URL = apigeexUtil.getApigeeHost(apigeeXConfigurationVO.getOrgName()) + "/v1/organizations/" + apigeeXConfigurationVO.getOrgName() + "/apps?expand=true";
						HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(apigeeXConfigurationVO.getOrgName(), "apigeex"));
						try {
							ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
							if (response.getStatusCode().is2xxSuccessful()) {
								String apiProductString = response.getBody();
								try {
									JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
									JSONArray apiProducts = (JSONArray) proxyObject.get("app");
									for(int i=0;i<apiProducts.size();i++){
										JSONObject jsonObject=apiProducts.getJSONObject(i);
										jsonObject.put("org",apigeeXConfigurationVO.getOrgName());
										jsonObject.put("orgType","saas");
									}
									products.addAll(apiProducts);
								} catch (Exception e) {
									logger.error("Exception occurred", e);
								}
							}
						} catch (Exception e) {
							logger.error("Exception occurred", e);
						}
					}
				}
			}
			List<ApigeeConfigurationVO> apigeeConnections = devportaldao.getApigeeConnectedOrgs();

			if (apigeeConnections != null && !apigeeConnections.isEmpty()) {
				String URL;
				for (ApigeeConfigurationVO apigeeConfigurationVO : apigeeConnections) {
					URL = apigeeUtil.getApigeeHost(apigeeConfigurationVO.getType(), apigeeConfigurationVO.getOrgname())
							+ "/v1/organizations/" + apigeeConfigurationVO.getOrgname() + "/apps?expand=true";
					HTTPUtil httpConn = new HTTPUtil(URL,
							getEncodedCredentials(apigeeConfigurationVO.getOrgname(), apigeeConfigurationVO.getType()));
					try {
						ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
						if (response.getStatusCode().is2xxSuccessful()) {
							String apiProductString = response.getBody();
							try {
								JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
								JSONArray apiProducts = (JSONArray) proxyObject.get("app");
								for(int i=0;i<apiProducts.size();i++){
									JSONObject jsonObject=apiProducts.getJSONObject(i);
									jsonObject.put("org",apigeeConfigurationVO.getOrgname());
									jsonObject.put("orgType",apigeeConfigurationVO.getType());
								}
								products.addAll(apiProducts);
							} catch (Exception e) {
								logger.error("Exception occurred", e);
							}
						}
					} catch (Exception e) {
						logger.error("Exception occurred", e);
					}
				}
			}
			JSONObject finalResponse = new JSONObject();
			finalResponse.put("app", products);
			return ResponseEntity.ok(finalResponse.toString());
		}
	}

	@Override
	public ResponseEntity<String> getAllOrgProducts(String orgs, String partners, String jsessionId, String interactionid) throws Exception {
		List<String> orgsList = (orgs!=null)?List.of(orgs.split(",")):new ArrayList<>();
		List<String> partnersList = (partners!=null)?List.of(partners.split(",")):new ArrayList<>();
		List<ApigeeXConfigurationVO> apigeexConnections;
		List<ApigeeConfigurationVO> apigeeConnections;
		List<String> swaggerPartners = new ArrayList<>();
		if(orgsList.isEmpty()){
			apigeexConnections = devportaldao.getApigeexConnectedOrgs();
			apigeeConnections = devportaldao.getApigeeConnectedOrgs();
		}else{
			apigeexConnections = devportaldao.getApigeexConnectedOrgs(orgsList);
			apigeeConnections = devportaldao.getApigeeConnectedOrgs(orgsList);
			if(apigeexConnections.isEmpty() && apigeeConnections.isEmpty()) return ResponseEntity.notFound().build();
		}
		if(!partnersList.isEmpty()){
			swaggerPartners = devportaldao.getSwaggerPartners(partnersList);
			if(swaggerPartners.isEmpty()) return ResponseEntity.notFound().build();
		}
		boolean filterPartner=(partners!=null && !swaggerPartners.isEmpty());
		JSONArray products = new JSONArray();

		if(!apigeexConnections.isEmpty())
			for (ApigeeXConfigurationVO apigeeXConfigurationVO : apigeexConnections) {
				String url;
				if (apigeeXConfigurationVO != null) {
					url =
							apigeexUtil.getApigeeHost(apigeeXConfigurationVO.getOrgName()) + StaticFields.V1_ORGANIZATIONS_PATH
									+ apigeeXConfigurationVO.getOrgName() + StaticFields.PRODUCTS_PATH;
					HTTPUtil httpConn = new HTTPUtil(url,
							apigeexUtil.getApigeeCredentials(apigeeXConfigurationVO.getOrgName(), "apigeex"));
					try {
						ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
						if (response.getStatusCode().is2xxSuccessful()) {
							String apiProductString = response.getBody();
							try {
								JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
								JSONArray apiProducts = (JSONArray) proxyObject.get("apiProduct");
								if(filterPartner) {
									for (int i = 0; i < apiProducts.size(); i++) {
										JSONObject jsonObject = apiProducts.getJSONObject(i);
										JSONArray attributes = (JSONArray) jsonObject.get("attributes");
										boolean isPartner = false;
										for (int j = 0; j < attributes.size(); j++) {
											JSONObject attribute = attributes.getJSONObject(j);
											String name=(String)attribute.get("name");
											if(StringUtils.equalsIgnoreCase(name,"partner")||StringUtils.equalsIgnoreCase(name,"partners")){
												List<String> values = List.of(((String) attribute.get("value")).split(","));
												for (String value : values) {
													if (swaggerPartners.contains(value)) {
														isPartner = true;
													}
												}
											}
										}
										if (isPartner){
											jsonObject.put("Organization",apigeeXConfigurationVO.getOrgName());
											products.add(jsonObject);
										}
									}
								}else{
									for (int i = 0; i < apiProducts.size(); i++){
										JSONObject jsonObject = apiProducts.getJSONObject(i);
										jsonObject.put("Organization",apigeeXConfigurationVO.getOrgName());
									}
									products.addAll(apiProducts);
								}
							} catch (Exception e) {
								logger.error("Exception occurred", e);
							}
						}
					} catch (Exception e) {
						logger.error("Exception occurred", e);
					}
				}
			}
		String url;
		if(!apigeeConnections.isEmpty())
			for (ApigeeConfigurationVO apigeeConfigurationVO : apigeeConnections) {
				url =
						apigeeUtil.getApigeeHost(apigeeConfigurationVO.getType(),
								apigeeConfigurationVO.getOrgname()) + StaticFields.V1_ORGANIZATIONS_PATH
								+ apigeeConfigurationVO.getOrgname() + StaticFields.PRODUCTS_PATH;
				HTTPUtil httpConn = new HTTPUtil(url,
						getEncodedCredentials(apigeeConfigurationVO.getOrgname(),
								apigeeConfigurationVO.getType()));
				try {
					ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
					if (response.getStatusCode().is2xxSuccessful()) {
						String apiProductString = response.getBody();
						try {
							JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
							JSONArray apiProducts = (JSONArray) proxyObject.get("apiProduct");
							if(filterPartner) {
								for (int i = 0; i < apiProducts.size(); i++) {
									JSONObject jsonObject = apiProducts.getJSONObject(i);
									JSONArray attributes = (JSONArray)jsonObject.get("attributes");
									boolean isPartner=false;
									for (int j = 0; j < attributes.size(); j++) {
										JSONObject attribute = attributes.getJSONObject(j);
										String name=(String)attribute.get("name");
										if(StringUtils.equalsIgnoreCase(name,"partner")||StringUtils.equalsIgnoreCase(name,"partners")){
											List<String> values= List.of(((String) attribute.get("value")).split(","));
											for(String value:values){
												if(swaggerPartners.contains(value)){
													isPartner=true;
												}
											}
										}
									}
									if(isPartner){
										jsonObject.put("Organization",apigeeConfigurationVO.getOrgname());
										products.add(jsonObject);
									}
								}
							}else {
								for (int i = 0; i < apiProducts.size(); i++){
									JSONObject jsonObject = apiProducts.getJSONObject(i);
									jsonObject.put("Organization",apigeeConfigurationVO.getOrgname());
								}
								products.addAll(apiProducts);
							}
						} catch (Exception e) {
							logger.error("Exception occurred", e);
						}
					}
				} catch (Exception e) {
					logger.error("Exception occurred", e);
				}
			}



		JSONArray filteredProducts = new JSONArray();

		for (int i = 0; i < products.size(); i++) {
			JSONObject product = products.getJSONObject(i);
			JSONArray proxiesArray = product.getJSONArray("proxies");
			List<String> proxiesList = new ArrayList<>();
			for (int j = 0; j < proxiesArray.size(); j++) {
				String value = proxiesArray.getString(j);
				proxiesList.add(value);
			}
			for(String k:proxiesList){
				Query query=new Query().addCriteria(Criteria.where("proxyName").is(k));
				ProxyData proxyData=mongoTemplate.findOne(query, ProxyData.class);
				if (proxyData!=null) {
					product.put("SwaggerId", proxyData.getSwaggerId());
					product.put("OasVersion", proxyData.getOasVersion());
					product.put("SwaggerRevision", proxyData.getSwaggerRevision());
				}
			}
			filteredProducts.add(product);
		}

		JSONObject finalResponse = new JSONObject();
		finalResponse.put("products", filteredProducts);
		return ResponseEntity.ok(finalResponse.toString());
	}
	@Override
	public ResponseEntity<List<ProductBundle>> getAllProductBundles(String jsessionId, String interactionid) throws ItorixException {
		List<String> orgNames = devportaldao.getOrganisationNames();
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is("Approved").and
				("organization").in(orgNames));
		return new ResponseEntity<>(devportaldao.getAllProductBundles(query), HttpStatus.OK);
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
				if (developerApp != null) {
					prodObj.put("ratePlan", developerApp.getRatePlan());
					prodObj.put("productBundle", developerApp.getProductBundle());
					prodObj.put("description", developerApp.getDescription());
				}
				responseObject.add(prodObj);
			}
			JSONObject finalResponse = new JSONObject();
			finalResponse.put("app", responseObject);
			return new ResponseEntity<>(finalResponse.toString(), HttpStatus.OK);
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
			} catch (Exception e) {
				logger.error(e.getMessage());

			}
			return ResponseEntity.ok(appResponseList);
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
			if (select != null && StringUtils.isNotEmpty(select))
				query = "?select=" + select;
			if (timeRange != null && StringUtils.isNotEmpty(timeRange))
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
					+ "/stats/apps";
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
												   String partnerType, String org, int offset, int pagesize, boolean paginated,String organizations) throws Exception {
		return new ResponseEntity<>(devportaldao.getProductBundleCards(partnerType, org, offset, pagesize, paginated,organizations), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> purchaseRatePlan(String jsessionId, String interactionid,
											  PurchaseRecord purchaseRecord) throws ItorixException {
		PurchaseResult purchaseResult = devportaldao.executePurchase(purchaseRecord);

		if (purchaseResult.equals(PurchaseResult.SUCCESS)) {
			return new ResponseEntity<>(purchaseRecord, HttpStatus.CREATED);
		} else if (purchaseResult.equals(PurchaseResult.INSUFFICIENT_BALANCE)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1010"), "Monetization-1010");
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1020"), "Monetization-1020");
		}
	}

	@Override
	public ResponseEntity<?> getPurchaseHistoryByAppId(String jsessionId, String interactionid, String appId)
			throws Exception {
		return new ResponseEntity<>(devportaldao.getPurchaseHistoryByAppId(appId), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPurchaseHistory(String jsessionId, String interactionid, String appId,
												String developerEmailId, String organization) throws ItorixException {

		return new ResponseEntity<>(devportaldao.getPurchaseHistory(appId, developerEmailId, organization), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deletePurchaseById(String jsessionId, String interactionid, String appId,
												String purchaseId) throws Exception {
		devportaldao.deletePurchaseById(appId, purchaseId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getWalletBalanceByFilter(String jsessionId, String interactionid, String appId,
													  String email) throws Exception {
		return new ResponseEntity<>(devportaldao.getWalletBalanceByFilter(appId, email), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getWalletBalanceByAppId(String jsessionId, String interactionid, String appId)
			throws Exception {
		return new ResponseEntity<>(devportaldao.getWalletBalanceByAppId(appId), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> addWalletBalanceForAppId(String jsessionId, String interactionid, double topUp,
													  String appId) throws Exception {
		return new ResponseEntity<>(devportaldao.addWalletBalanceForAppId(appId, topUp), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> computeBillForAppId(String jsessionId, String interactionid, double transactions, String startDate, String endDate, String appId)
			throws Exception {
		return new ResponseEntity<>(devportaldao.computeBillForAppId(appId, transactions, startDate, endDate), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateProductStatus(String jsessionId, String interactionid, String gwtype,
												 String type, String org, String developerEmailId, String appName, String consumerKey, String productName,
												 String action) throws Exception {
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + developerEmailId
					+ "/apps/" + appName + "/keys/" + consumerKey + "/apiproducts/" + productName;
			if (action != null) {
				try {
					String statusUrl = URL + "?action=" + action;
					statusUrl = statusUrl.replace("//v1/organizations", "/v1/organizations");
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization", apigeexUtil.getApigeeCredentials(org, type));
					HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

					ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST, requestEntity, Void.class);
				} catch (Exception ex) {
					logger.error("Could Not Update Product Status:" + ex.getMessage());
					throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1050"), "Monetization-1050");
				}
			}
		} else {
			String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + developerEmailId
					+ "/apps/" + appName + "/keys/" + consumerKey + "/apiproducts/" + productName;
			if (action != null) {
				try {
					String statusUrl = URL + "?action=" + action;
					statusUrl = statusUrl.replace("//v1/organizations", "/v1/organizations");
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization", getEncodedCredentials(org, type));
					HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

					ResponseEntity<Void> statusResponse = restTemplate.exchange(statusUrl, HttpMethod.POST, requestEntity, Void.class);
				} catch (Exception ex) {
					logger.error("Could Not Update Product Status:" + ex.getMessage());
					throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1050"), "Monetization-1050");
				}
			}
		}
		return new ResponseEntity<>("Successfully Updated Product Status", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllGateways(String jsessionId, String interactionid) throws Exception {
		return new ResponseEntity<>(devportaldao.getAllGateways(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getGatewayEnvs(String jsessionId, String interactionid, String gwtype, String type, String gateway, String resourceGroup) throws Exception {
		return new ResponseEntity<>(devportaldao.getGatewayEnvironments(gateway, resourceGroup), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getGatewayApps(String jsessionId, String interactionid, String type, String gateway, String env, String workspace, String resourceGroup) throws Exception {
		Object object;
		if (gateway.equalsIgnoreCase(StaticFields.APIGEE)) {
			//call Apigee With The Env
			object = apigeeAppsHelper(env, type);
		} else if (gateway.equalsIgnoreCase(StaticFields.APIGEEX)) {
			//call ApigeeX With The Env
			object = apigeeXAppsHelper(env, StaticFields.APIGEEX);
		} else if (gateway.equalsIgnoreCase(StaticFields.KONG)) {
			//call Kong With The Env
			object = getConsumersFromKong(env, workspace);
		} else if (gateway.equalsIgnoreCase(StaticFields.AZURE)) {
			//call Azure With The Env
			object = getAppsFromAzure(env, resourceGroup);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portal-1001"), "Portal-1001");
		}
		return new ResponseEntity<>(object, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getKongWorkspaces(String jsessionId, String interactionid, String runTime) throws Exception {
		return new ResponseEntity<>(getWorkspacesFromKong(runTime), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAzureResourceGroups(String jsessionId, String interactionid) throws Exception {
		return new ResponseEntity<>(getResourceGroupsFromAzure(), HttpStatus.OK);
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

	public Object apigeeXAppsHelper(String org, String type) throws Exception {
		String URL;
		URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apps?expand=true";
		HTTPUtil httpConn = new HTTPUtil(URL, apigeexUtil.getApigeeCredentials(org, type));
		ResponseEntity<String> response = devportaldao.proxyService(httpConn, "GET");
		JSONArray appResponseList = new JSONArray();
		String apiProductString = response.getBody();
		try {
			JSONObject proxyObject = (JSONObject) JSONSerializer.toJSON(apiProductString);
			if (!proxyObject.isEmpty()) {
				JSONArray apiProducts = (JSONArray) proxyObject.get("app");
				for (Object apiObj : apiProducts) {
					JSONObject prodObj = (JSONObject) apiObj;
					JSONObject appDetails = new JSONObject();
					appDetails.put("appId", prodObj.get("appId"));
					appDetails.put("appName", prodObj.get("name"));
					JSONArray json = (JSONArray) prodObj.get("attributes");
					for (Object obj : json) {
						if (obj instanceof JSONObject) {
							JSONObject nameObject = (JSONObject) obj;
							if (StringUtils.equalsIgnoreCase(nameObject.getString("name"), "DisplayName")) {
								appDetails.put("appName", nameObject.getString("value"));
							}
						}
					}
					if (appDetails.get("appName") == null) {
						appDetails.put("appName", (String) prodObj.get("name"));
					}
					appResponseList.add(appDetails);
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(
				objectMapper.writeValueAsString(appResponseList), headers, HttpStatus.OK);
		return responseEntity.getBody();
	}

	public JSONArray apigeeAppsHelper(String org, String type) throws ItorixException {
		String URL = apigeeUtil.getApigeeHost(type, org) + "v1/organizations/" + org
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
				if (appDetails.get("appName") == null) {
					appDetails.put("appName", (String) prodObj.get("name"));
				}
				appResponseList.add(appDetails);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return appResponseList;
	}

	public List<Consumer> getConsumersFromKong(String runtime, String workspace) throws ItorixException {
		try {
			KongRuntime kongRuntime = devportaldao.getKongRuntime(runtime);
			String url;
			HttpHeaders headers = new HttpHeaders();
			String kongHost;
			if (kongRuntime.getKongAdminHost().charAt(kongRuntime.getKongAdminHost().length() - 1) == '/') {
				kongHost = kongRuntime.getKongAdminHost().substring(0, kongRuntime.getKongAdminHost().length() - 1);
			} else {
				kongHost = kongRuntime.getKongAdminHost();
			}
			if (kongRuntime.getType().equalsIgnoreCase("onprem")) {
				headers.set("Kong-Admin-Token", kongRuntime.getKongAdminToken());
				if (workspace != null) {
					url = String.format("%s/%s/consumers", kongHost, workspace);
				} else {
					url = String.format("%s/consumers", kongHost);
				}
			} else {//saas
				headers.set("Authorization", kongRuntime.getKongAdminToken());
				url = String.format("%s/konnect-api/api/runtime_groups/%s/consumers", kongHost, workspace);
			}
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
			ResponseEntity<ConsumerResponse> consumers = restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, ConsumerResponse.class);
			return consumers.getBody().getData();
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portal-1001"), "Portal-1001");
		}
	}

	public List<AzureSubscriptionResponseDTO> getAppsFromAzure(String serviceName, String resourceGroup) throws ItorixException {
		AzureConfigurationVO connector = devportaldao.getAzureConnector(serviceName, resourceGroup);
		String url = String.format(StaticFields.AZURE_SUBSCRIPTIONS_URL, connector.getManagementHost(), connector.getSubscriptionId(), connector.getResourceGroup(), connector.getServiceName(), connector.getApiVersion());
		try {
			List<AzureSubscriptionResponseDTO> azureSubscriptionResponseDTOS = new ArrayList<>();
			ResponseEntity<AzureSubscriptionResponse> azureProductResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity(null, connector.getSharedAccessToken()), AzureSubscriptionResponse.class);
			List<AzureSubscriptionValues> response = azureProductResponse.getBody().getValue();
			for (AzureSubscriptionValues azureSubscriptionValues : response) {
				String[] scopeArray = azureSubscriptionValues.getProperties().getScope().split("/");
				if (scopeArray[scopeArray.length - 2].equalsIgnoreCase("products")) {
					String name = azureSubscriptionValues.getName();
					String displayName = azureSubscriptionValues.getProperties().getDisplayName();
					azureSubscriptionResponseDTOS.add(new AzureSubscriptionResponseDTO(name, displayName != null ? displayName : name));
				}
			}
			return azureSubscriptionResponseDTOS;
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portal-1001"), "Portal-1001");
		}
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

	public List<KongWorkspace> getWorkspacesFromKong(String runtime) throws ItorixException {
		try {
			KongRuntime kongRuntime = devportaldao.getKongRuntime(runtime);
			String url;
			HttpHeaders headers = new HttpHeaders();
			String kongHost;
			if (kongRuntime.getKongAdminHost().charAt(kongRuntime.getKongAdminHost().length() - 1) == '/') {
				kongHost = kongRuntime.getKongAdminHost().substring(0, kongRuntime.getKongAdminHost().length() - 1);
			} else {
				kongHost = kongRuntime.getKongAdminHost();
			}
			if (kongRuntime.getType().equalsIgnoreCase("onprem")) {
				url = String.format("%s/workspaces/", kongHost);
				headers.set("Kong-Admin-Token", kongRuntime.getKongAdminToken());
			} else {//saas
				url = String.format("%s/konnect-api/api/runtime_groups", kongHost);
				headers.set("Authorization", kongRuntime.getKongAdminToken());
			}

			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
			ResponseEntity<KongWorkspaceResponse> kongWorkspaces = restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, KongWorkspaceResponse.class);
			return kongWorkspaces.getBody().getData();
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portal-1001"), "Portal-1001");
		}
	}

	public Set<String> getResourceGroupsFromAzure() throws ItorixException {
		Set<String> response = new HashSet<>();
		List<AzureConfigurationVO> connectors = devportaldao.getAllAzureConnectors();
		if(connectors!=null && !connectors.isEmpty()){
			for(AzureConfigurationVO azureConfigurationVO: connectors){
				response.add(azureConfigurationVO.getResourceGroup());
			}
		}
		return response;
	}
}