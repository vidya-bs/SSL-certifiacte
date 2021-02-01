package com.itorix.apiwiz.devportal.serviceimpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.devportal.dao.DevportalDao;
import com.itorix.apiwiz.devportal.service.DevportalService;


@CrossOrigin(origins = "*")
@RestController
public class DevportalServiceImpl implements DevportalService  {

	@Autowired
	private ApigeeUtil apigeeUtil;

	@Autowired
	private DevportalDao devportaldao;

	
	@Override
	public ResponseEntity<String> createDeveloper(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type") String type,
			@PathVariable("org") String org, 
			@RequestBody String body) throws Exception{
		if(body !=null)
		{
			String URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/"+org+"/developers";
			HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org,type));
			return  devportaldao.proxyService(httpConn, "POST");
		}
		return null;
	}

	   
	@Override
	public org.springframework.http.ResponseEntity<String> registerApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@RequestBody String body) throws Exception{
		if(body !=null)
		{
			String URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/"+org+"/developers/"+email+"/apps";
			HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
			return  devportaldao.proxyService(httpConn, "POST");
		}
		return null;
	}
	
	   
	@Override
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@PathVariable("appName") String appName,
			@RequestBody String body) throws Exception{
		if(body !=null)
		{
			String URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/"+org+"/developers/"+email+"/apps/"+appName;
			HTTPUtil httpConn = new HTTPUtil(body, URL, getEncodedCredentials(org, type));
			return  devportaldao.proxyService(httpConn, "PUT");
		}
		return null;
	}

	
	@Override
	public org.springframework.http.ResponseEntity<String> getProducts(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type") String type,
			@PathVariable("org") String org,
			@RequestParam(value="expand",required=false) String expand) throws Exception{
		String URL;
		if( expand !=null && expand!="")
			URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/"+org+"/apiproducts?expand="+expand;
		else 
			URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/"+org+"/apiproducts";
		HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
		return  devportaldao.proxyService(httpConn, "GET");
	}

	
	@Override
	public org.springframework.http.ResponseEntity<String> getApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@RequestParam(value="expand",required=false) String expand) throws Exception{
		String URL;
		if( expand !=null && expand!="")
			URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email + "/apps?expand=" + expand;
		else 
			URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email + "/apps";
		HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
		return  devportaldao.proxyService(httpConn, "GET");
	}

	
	@Override
	public org.springframework.http.ResponseEntity<String> getApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@PathVariable("appName") String appName) throws Exception{
		String URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/developers/" + email + "/apps/" + appName;
		HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
		return  devportaldao.proxyService(httpConn, "GET");
	}

	
	@Override
	public org.springframework.http.ResponseEntity<String> getPortalStats(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("env") String env,
			@RequestParam(value="select",required=false) String select,
			@RequestParam(value="timeRange",required=false) String timeRange,
			@RequestParam(value="timeUnit",required=false) String timeUnit,
			@RequestParam(value="filter",required=false) String filter) throws Exception{
		String URL =  apigeeUtil.getApigeeHost(type, org) + "/v1/organizations/" + org + "/environments/" + env + "/stats/apps/";
		String query = "";
		if( select !=null && select!="")
			query = "?select="+select;
		if( timeRange !=null && timeRange!="")
			query = query + "&timeRange="+timeRange;
		if( timeUnit !=null && timeUnit!="")
			query = query + "&timeUnit="+timeUnit;
		if( filter !=null && filter!="")
			query = query + "&filter="+filter;
		URL = URL+query;
		HTTPUtil httpConn = new HTTPUtil(URL, getEncodedCredentials(org, type));
		return  devportaldao.proxyService(httpConn, "GET");
	}

	private String getEncodedCredentials(String org, String type){
		return apigeeUtil.getApigeeAuth(org, type);
	}
}