package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.proxystudio.APIProduct;
import com.itorix.apiwiz.common.model.proxystudio.App;
import com.itorix.apiwiz.common.model.proxystudio.Credential;
import com.itorix.apiwiz.common.model.proxystudio.Env;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnvs;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.*;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.User;
//import com.itorix.hyggee.common.model.proxystudio.DevApp;
//import com.itorix.hyggee.common.model.proxystudio.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("apigeeDetails")
public class ApigeeDetails {


	@Autowired
	private ApigeeUtil apigeeUtil;

	@Autowired
	private ApplicationProperties applicationProperties;

	private HTTPUtil httpUtil; 

	private String apigeeURL = "https://api.enterprise.apigee.com/"; 

	@PostConstruct
	private void init(){
		this.httpUtil = new HTTPUtil();
	}

	@Value("${itorix.core.apigee.edge.retry.timeout}")
	private int retryTimeout;

	public ProxyData getDetails(ProxyData proxyData, User user){
		String proxy = proxyData.getProxyName();
		OrgEnvs orgEnvs = proxyData.getOrgEnvs();
		ProxyApigeeDetails proxyApigeeDetails = new ProxyApigeeDetails();
		proxyApigeeDetails.setName(proxy);
		List<Deployments> deploymentList = new ArrayList<Deployments>();

		for(OrgEnv organization: orgEnvs.getOrgEnvs())
		{
			for(Env envEnv: organization.getEnvs()){
				try{
					Deployments deployments = getDeployments(proxy, organization.getName(),envEnv.getName(), organization.getType());
					deploymentList.add(deployments);
					String revision = null;
					if(deployments != null && deployments.getProxies() != null &&  deployments.getProxies().get(0).getRevision()!=null)
						revision = deployments.getProxies().get(0).getRevision();
					if(revision!= null && !revision.equals(""))
						envEnv.setStatus("deployed");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		if(deploymentList.size()>0)
			proxyApigeeDetails.setDeployments(deploymentList);
		proxyData.setProxyApigeeDetails(proxyApigeeDetails);
		proxyData.setOrgEnvs(orgEnvs);
		return proxyData;
	}

	public ProxyApigeeDetails getDetailsByProxy(ProxyData proxyData, String org, String env, String type){
		String proxy = proxyData.getProxyName();
		ProxyApigeeDetails proxyApigeeDetails = proxyData.getProxyApigeeDetails();
		List<Deployments> deploymentList;
		boolean updated = false;
		if(proxyApigeeDetails == null){
			proxyApigeeDetails = new ProxyApigeeDetails();
			proxyApigeeDetails.setName(proxy);
			deploymentList = new ArrayList<Deployments>();
		}else{
			deploymentList =  proxyApigeeDetails.getDeployments()==null?new ArrayList<Deployments>():proxyApigeeDetails.getDeployments();
		}
		
		try{
			Deployments deployments = getDeploymentsByProxy(proxy, org,env, type);
			for(Deployments deployment: deploymentList)
			{
				if(deployment.getOrg().equals(org) && deployment.getEnv().equals(env) && deployment.getType().equalsIgnoreCase(type)){
					deployment.setProxies(deployments.getProxies());
					updated= true;
				}
			}
			if(!updated)
				deploymentList.add(deployments);
			if(deploymentList.size()>0)
				proxyApigeeDetails.setDeployments(deploymentList);
		}catch(Exception e){
			e.printStackTrace();
		}
		return proxyApigeeDetails;
	}
	
	
	private Deployments getDeploymentsByProxy(String proxy, String org, String env, String type){
		try{
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org, type);
			User user = new User();
			Apigee apigee = new Apigee();
			apigee.setUserName(apigeeServiceUser.getUserName());
			apigee.setPassword(apigeeServiceUser.getPassword());
			user.setApigee(apigee);
			this.apigeeURL = apigeeUtil.getApigeeHost(type==null ? "saas" :type, org);
			List<Proxy> proxyList = this.getProxyList(apigeeURL, proxy, org, env, user , type);
			Map<String, List<String>> proxiesAndProductsLinkedMap = getAllProductsInOrganization(apigeeURL, user, org, type);
			Map<String, List<String>> productsAndAppsLinkedMap = getAllAppsInOrganization(apigeeURL, user, org, type);
			for(Proxy proxyObj : proxyList) {
				if(proxyObj.getName().equals(proxy)) 
					proxyObj.setProducts(this.addProductsToProxies(user, org, proxy, proxiesAndProductsLinkedMap, productsAndAppsLinkedMap, type));
			}
			Deployments deployments = new Deployments();
			deployments.setEnv(env);
			deployments.setOrg(org);
			deployments.setType(type);
			deployments.setProxies(proxyList);
			return deployments;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	private Deployments getDeployments(String proxy, String org, String env, String type){
		try{
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org, type);
			User user = new User();
			Apigee apigee = new Apigee();
			apigee.setUserName(apigeeServiceUser.getUserName());
			apigee.setPassword(apigeeServiceUser.getPassword());
			user.setApigee(apigee);
			this.apigeeURL = apigeeUtil.getApigeeHost(type==null ? "saas" :type, org);
			Map<String, List<String>> proxiesAndProductsLinkedMap = getAllProductsInOrganization(apigeeURL, user, org, type);
			Map<String, List<String>> productsAndAppsLinkedMap = getAllAppsInOrganization(apigeeURL, user, org, type);
			List<Proxy> proxyList = this.getProxyList(apigeeURL, proxy, org, env, user , type);
			for(Proxy proxyObj : proxyList) {
				if(proxyObj.getName().equals(proxy)) 
					proxyObj.setProducts(this.addProductsToProxies(user, org, proxy, proxiesAndProductsLinkedMap, productsAndAppsLinkedMap, type));
			}
			Deployments deployments = new Deployments();
			deployments.setEnv(env);
			deployments.setOrg(org);
			deployments.setType(type);
			deployments.setProxies(proxyList);
			return deployments;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}



	private List<Product> addProductsToProxies(User user, String organization, String apiProxy,
			Map<String, List<String>> proxiesAndProductsLinkedMap, Map<String, List<String>> productsAndAppsLinkedMap, String type)
					throws IOException {
		List<Product> productChildrenList = new ArrayList<Product>();
		List<String> products = proxiesAndProductsLinkedMap.get(apiProxy);
		if (null != products && products.size() > 0) {
			for (String productName : products) {
				Product productChild = new Product();
				productChild.setName(productName);
				List<DevApp> devAppChildList = addDeveloperAppsToProducts(user, productName, organization,
						productsAndAppsLinkedMap, type);
				productChild.setDevApps(devAppChildList);
				productChildrenList.add(productChild);
			}
		}
		return productChildrenList;
	}

	private List<DevApp> addDeveloperAppsToProducts(User user, String product, String organization, 
			Map<String, List<String>> productsAndAppsLinkedMap, String type) throws IOException {
		List<DevApp> devAppChildrenList = new ArrayList<DevApp>();
		List<String> appList = productsAndAppsLinkedMap.get(product);
		if (null != appList && appList.size() > 0) {
			for (String appName : appList) {
				DevApp appProductChildren = new DevApp();
				appProductChildren.setName(appName);
				appProductChildren.setEmailList(getDeveloperMail(apigeeURL, user, appName,organization, type));
				devAppChildrenList.add(appProductChildren);
			}
		}
		return devAppChildrenList;
	}

	public Map<String, List<String>> getAllProductsInOrganization(String apigeeHost, User user, String organization, String type) throws IOException {
		Map<String, List<String>> proxyProductLinkMap = new HashMap<String, List<String>>();
		String apigeeCred = apigeeUtil.getApigeeAuth(organization, type);
		httpUtil.setBasicAuth(apigeeCred);
		httpUtil.setuRL( apigeeHost + "v1/organizations/" + organization + "/apiproducts");
		try {
			ResponseEntity<String> response = makeCall( httpUtil, "GET");
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				String apiProductsString = response.getBody();
				JSONArray apiProducts = (JSONArray) JSONSerializer.toJSON(apiProductsString);
				JSONArray productsData = new JSONArray();
				for (Object apiObj : apiProducts) {
					final String apiProduct = (String) apiObj;
					// If string contains spaces, it will not allow to process.
					productsData.add(apiProduct);
					String productUrl = apigeeHost + "v1/organizations/" + organization + "/apiproducts/" + apiProduct;
					productUrl = productUrl.replace(" ", "%20");
					httpUtil.setBasicAuth(apigeeCred);
					httpUtil.setuRL( productUrl);
					response = makeCall( httpUtil, "GET");
					String apiProductString = response.getBody();
					ObjectMapper objectMapper = new ObjectMapper();
					APIProduct productObj = new APIProduct();
					productObj = objectMapper.readValue(apiProductString, APIProduct.class);
					for (Object proxyName : productObj.getProxies()) {
						String proxyLinkedInProduct = (String) proxyName;
						if (proxyProductLinkMap.containsKey(proxyLinkedInProduct)) {
							proxyProductLinkMap.get(proxyLinkedInProduct).add(productObj.getName());
						} else {
							List<String> products = new ArrayList<String>();
							products.add(productObj.getName());
							proxyProductLinkMap.put(proxyLinkedInProduct, products);
						}
					}
				} 
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return proxyProductLinkMap;
	}


	public Map<String, List<String>> getAllAppsInOrganization(String apigeeHost, User user, String organization, String type) throws IOException {
		String apigeeCred = apigeeUtil.getApigeeAuth(organization, type);
		Map<String, List<String>> productsAppsLinkedMap = new HashMap<String, List<String>>();
		httpUtil.setBasicAuth(apigeeCred);
		httpUtil.setuRL( apigeeHost + "v1/organizations/" + organization + "/apiproducts");
		ResponseEntity<String> response = makeCall( httpUtil, "GET");
		HttpStatus statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful()) {
			String appsString = response.getBody();
			JSONArray apps = (JSONArray) JSONSerializer.toJSON(appsString);
			for (Object appObj : apps) {
				@SuppressWarnings("unused")
				final String app = (String) appObj;
				try {
					httpUtil.setBasicAuth(apigeeCred);
					httpUtil.setuRL( apigeeHost +"/v1/organizations/" + organization +  "/apps/");
					response = makeCall( httpUtil, "GET");
					String developerAppString = response.getBody();
					ObjectMapper objectMapper = new ObjectMapper();
					JSONArray devApps = (JSONArray) JSONSerializer.toJSON(developerAppString);
					for (Object devObj : devApps) {
						@SuppressWarnings("unused")
						final String devApp = (String) devObj;
						httpUtil.setBasicAuth(apigeeCred);
						httpUtil.setuRL( apigeeHost +"/v1/organizations/" + organization +  "/apps/"+devApp);
						response = makeCall( httpUtil, "GET");
						String developerApp = response.getBody();
						App appObject = new App();
						appObject = objectMapper.readValue(developerApp, App.class);
						List<String> productsLinked = getAllProductsFromAppCredentials(appObject);
						for (String apiProduct : productsLinked) {
							if (productsAppsLinkedMap.containsKey(apiProduct)) {
								for (Object productObjectName : appObject.getApiProducts()) {
									productsAppsLinkedMap.get(apiProduct).add((String) productObjectName);
								}
							} else {
								List<String> appsLinked = new ArrayList<String>();
								appsLinked.add(appObject.getName());
								productsAppsLinkedMap.put(apiProduct, appsLinked);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return productsAppsLinkedMap;
	}

	public String getApigeeURL() {
		return apigeeURL;
	}

	public void setApigeeURL(String apigeeURL) {
		this.apigeeURL = apigeeURL;
	}

	public List<String> getDeveloperMail(String apigeeHost, User user, String devAppName, String organization, String type){
		try {

			httpUtil.setBasicAuth(apigeeUtil.getApigeeAuth(organization, type));
			httpUtil.setuRL( apigeeHost + "v1/organizations/" + organization + "/developers?app="+devAppName);
			ResponseEntity<String> response = makeCall( httpUtil, "GET");
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				JSONObject developer = (JSONObject) JSONSerializer.toJSON(response.getBody());
				ObjectMapper objectMapper = new ObjectMapper();
				JSONArray email = (JSONArray) JSONSerializer.toJSON(objectMapper.writeValueAsString(developer.get("developer")));
				List<String> mailList = new ArrayList<String>();
				for(int i=0; i < email.size(); i++){
					JSONObject mail = (JSONObject)email.get(0);
					mailList.add((String)mail.get("email"));
				}
				return mailList;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	private  List<String> getAllProductsFromAppCredentials(App app){
		List<Credential> credentialList = new ArrayList<Credential>();
		credentialList = app.getCredentials();
		List<String> apiProducts = new ArrayList<String>();
		for(Credential c: credentialList){
			for(APIProduct s: c.getApiProducts()){
				apiProducts.add(s.getApiproduct());
			}
		}
		return apiProducts;
	}

	public List<Proxy> getProxyList(String apigeeHost, String proxy, String organization, String env, User user, String type){
		httpUtil.setBasicAuth(apigeeUtil.getApigeeAuth(organization, type));
		httpUtil.setuRL( apigeeHost + "v1/organizations/" + organization + "/environments/" + env	+ "/deployments");
		JSONObject proxyObject = null;
		try {
			ResponseEntity<String> response = makeCall( httpUtil, "GET");
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				String stringResponse = response.getBody();
				proxyObject = (JSONObject) JSONSerializer.toJSON(stringResponse);
				List<Proxy> childList = new ArrayList<Proxy>();
				if (null != proxyObject) {
					JSONArray proxyList = (JSONArray) proxyObject.get("aPIProxy");
					if (null != proxyList) {
						for (Object object : proxyList) {
							JSONObject proxyObj = (JSONObject) object;
							String proxyName = (String) proxyObj.get("name");
							if(proxyName.equals(proxy)){
								JSONArray revisionArray = (JSONArray)proxyObj.get("revision");
								JSONObject revision = (JSONObject)revisionArray.get(0);
								Proxy proxyChild = new Proxy();
								proxyChild.setName(proxyName);
								proxyChild.setRevision((String)revision.get("name"));
								childList.add(proxyChild);
							}
						}
					}
				}
				return childList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ResponseEntity<String> makeCall(HTTPUtil httpUtil, String method) {
		ResponseEntity<String> response = null;
		int retryCount = 0;
		do {
			try {
				if(method.equals("POST"))
					response = httpUtil.doPost();
				else
					response = httpUtil.doGet();

				if(response.getStatusCode().is2xxSuccessful() || retryCount >= 2)
					return response;
			}catch(Exception e) {
				if(retryCount >= 2)
					throw e;
			}
			retryCount = retryCount + 1;
			try {
				Thread.sleep(retryTimeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(true);
	}

}

