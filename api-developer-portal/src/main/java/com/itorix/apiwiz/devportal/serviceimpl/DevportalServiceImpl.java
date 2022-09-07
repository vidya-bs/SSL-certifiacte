package com.itorix.apiwiz.devportal.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.proxystudio.APIProduct;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.devportal.dao.DevportalDao;
import com.itorix.apiwiz.devportal.service.DevportalService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@CrossOrigin(origins = "*")
@RestController
public class DevportalServiceImpl implements DevportalService {

	private Logger logger = Logger.getLogger(DevportalDao.class);

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
			@PathVariable("email") String email, @RequestBody String body) throws Exception {
		if (body != null) {
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps";
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps";
				HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
				return devportaldao.proxyService(httpConn, "POST");
			}
		}
		return null;
	}

	@Override
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName, @RequestBody String body)
			throws Exception {
		if (body != null) {
			if (type != null && type.equalsIgnoreCase("apigeex")) {
				String URL = apigeexUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
				HTTPUtil httpConn = new HTTPUtil(body, URL, apigeexUtil.getApigeeCredentials(org, type));
				return devportaldao.proxyService(httpConn, "PUT");
			} else {
				String URL = apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email
						+ "/apps/" + appName;
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
			return devportaldao.proxyService(httpConn, "GET");
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

	private String getEncodedCredentials(String org, String type) {
		return apigeeUtil.getApigeeAuth(org, type);
	}
}
