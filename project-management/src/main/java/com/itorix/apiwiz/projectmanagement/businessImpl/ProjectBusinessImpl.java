package com.itorix.apiwiz.projectmanagement.businessImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigee.VirtualHost;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.configmanagement.ProductAttributes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.Endpoint;
import com.itorix.apiwiz.common.model.projectmanagement.Endpoints;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Pipeline;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProxyConnection;
import com.itorix.apiwiz.common.model.projectmanagement.RegistryEndpoint;
import com.itorix.apiwiz.common.model.projectmanagement.ServiceRegistry;
import com.itorix.apiwiz.common.model.projectmanagement.Stage;
import com.itorix.apiwiz.common.model.proxystudio.APIProduct;
import com.itorix.apiwiz.common.model.proxystudio.Env;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnvs;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.common.model.proxystudio.ProxyEndpoint;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.devstudio.businessImpl.CodeGenService;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.projectmanagement.dao.ProjectManagementDao;
import com.itorix.apiwiz.servicerequest.dao.ServiceRequestDao;
import com.itorix.apiwiz.servicerequest.model.ServiceRequest;

@Component
public class ProjectBusinessImpl {

	@Autowired
	private ProjectManagementDao projectManagementDao;

	@Autowired
	private ServiceRequestDao serviceRequestDao;

	@Autowired
	private CodeGenService codeGenService;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	private ApigeeUtil apigeeUtil;
	
	@Autowired
	private ApplicationProperties applicationProperties;

	public void createServiceConfig(Organization organization, String projectName, String proxyName,
			String registryName, String jsessionId) throws ItorixException {
		try {
			ServiceRegistry serviceRegistry = projectManagementDao.getServiceRegistry(projectName, proxyName, registryName);
			if(serviceRegistry == null) throw new ItorixException("no service registry for proxy "+proxyName +" registry name " + registryName ,"");
			ServiceRequest config = new ServiceRequest();
			config.setType("KVM");
			config.setName(proxyName);
			config.setOrg(organization.getName());
			config.setEnv(organization.getEnv());
			config.setEncrypted("false");
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas")?true:false);
			KVMEntry entry = new KVMEntry();
			entry.setName("endpoints");
			entry.setValue(getEndpoints(serviceRegistry.getEndpoints()));
			List<KVMEntry> entries = new ArrayList<KVMEntry>();
			entries.add(entry);
			config.setEntry(entries);

			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionId);
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName()+" "+user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);

			serviceRequestDao.createServiceRequest(config);
			config.setStatus("Approved");
			List<String> roles = identityManagementDao.getUserRoles(jsessionId); //user.getRoles();
			if(!roles.contains("Admin"))
				roles.add("Admin");
			config.setUserRole(roles);
			serviceRequestDao.changeServiceRequestStatus(config,user);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}


	private void createServiceProdutConfig(Organization organization, String proxyName, String jsessionId) throws ItorixException {
		try {
			ServiceRequest config = new ServiceRequest();
			config.setType("Product");
			config.setName("Product-" + proxyName);
			config.setDisplayName("Product-" + proxyName);
			config.setDescription("Product-" + proxyName);
			config.setOrg(organization.getName());
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas")?true:false);
			List<String> apiResources = new ArrayList<String>();
			apiResources.add("/**");
			apiResources.add("/");
			config.setApiResources(apiResources);
			List<String> environments = getProductEnv(organization.getName(),organization.getEnv(), organization.getType(), "Product-" + proxyName);
			config.setEnvironments(environments);
			List<String> proxies = new ArrayList<String>();
			proxies.add(proxyName);
			config.setAttributes(getProductAttributes());
			config.setProxies(proxies);
			config.setApprovalType("auto");
			config.setQuota("10000");
			config.setQuotaInterval("1");
			config.setQuotaTimeUnit("month");
			User user ;
			if(jsessionId != null)
				user = identityManagementDao.getUserDetailsFromSessionID(jsessionId);
			else 
				user = identityManagementDao.findByLogin(applicationProperties.getServiceUserName());
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName()+" "+user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.createServiceRequest(config);
			
			config.setStatus("Approved");
			List<String> roles = identityManagementDao.getUserRoles(jsessionId); //user.getRoles();
			if(!roles.contains("Admin"))
				roles.add("Admin");
			config.setUserRole(roles);
			serviceRequestDao.changeServiceRequestStatus(config,user);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> getProductEnv(String org, String env, String type, String productName) {
		List<String> environments ;
		boolean found = false;
		APIProduct product = getProductDetails(org, type, productName);
		if(product!= null && product.getEnvironments()!=null) {
			for(String name :product.getEnvironments())
				if(name.equals(env))
					found = true;
			environments = product.getEnvironments();
		}
		else {
			environments = new ArrayList<String>();
		}
		if(!found)
			environments.add(env);
		return environments;
	}

	private List<ProductAttributes> getProductAttributes() {
		List<ProductAttributes> attributes = new ArrayList<ProductAttributes>();

		ProductAttributes description = new ProductAttributes();
		description.setName("description");
		description.setValue("json Product");
		attributes.add(description);

		ProductAttributes developerQuota = new ProductAttributes();
		developerQuota.setName("developer.quota.limit");
		developerQuota.setValue("10000");
		attributes.add(developerQuota);

		ProductAttributes developerQuotaInterval = new ProductAttributes();
		developerQuotaInterval.setName("developer.quota.interval");
		developerQuotaInterval.setValue("1");
		attributes.add(developerQuotaInterval);

		ProductAttributes developerQuotaTime = new ProductAttributes();
		developerQuotaTime.setName("developer.quota.timeunit");
		developerQuotaTime.setValue("month");
		attributes.add(developerQuotaTime);

		return attributes;
	}

	private APIProduct getProductDetails(String org, String type, String productName) {
		try {
			String apigeeURL  = apigeeUtil.getApigeeHost(type, org) + "v1/organizations/" + org + "/apiproducts/" + productName;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("Authorization", apigeeUtil.getApigeeAuth(org, type));
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			ResponseEntity<APIProduct> response  = 
					restTemplate.exchange(apigeeURL, HttpMethod.GET, requestEntity, APIProduct.class);
			APIProduct	product	= response.getBody();
			return product;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getEndpoints(List<RegistryEndpoint> registryEndpoints) {
		Endpoints endpoints = new Endpoints();
		List<Endpoint> endpointsList = new ArrayList<Endpoint>();
		for(RegistryEndpoint registryEndpoint: registryEndpoints) {
			Endpoint endpoint = new Endpoint();
			endpoint.setEndpoint(registryEndpoint);
			endpointsList.add(endpoint);
		}
		endpoints.setEndpoints(endpointsList);
		try {
			String value = new ObjectMapper().writeValueAsString(endpoints);
			value = value.replaceAll("\"endpoints\"", "\"Endpoints\"").replaceAll("\"endpoint\"", "\"Endpoint\"");
			return value;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public void createServiceConfigs(String projectName, String proxyName, String branchType, String jsessionid) throws ItorixException {
		Project project = projectManagementDao.findByProjectName(projectName);
		List<Pipeline> pipelines = project.getProxies().get(0).getPipelines();
		for(Pipeline pipeline : pipelines) {
			if(pipeline.getBranchType().equals(branchType)) {
				for(Stage stage: pipeline.getStages()) {
					Organization org = new Organization();
					org.setName(stage.getOrgName());	
					org.setEnv(stage.getEnvName());
					org.setType(stage.getIsSaaS().equalsIgnoreCase("true")?"saas":"onprem");
					createServiceConfig(org, projectName, proxyName, stage.getName(), jsessionid);
					//createServiceProdutConfig(org, proxyName,  jsessionid);
				}
			}
		}
	}

	public List<String> getProxyConnectionURL(String org, String env, String isSaaS, String vHostName)  {
		try {
			String apigeeURL  = apigeeUtil.getApigeeHost(isSaaS.equalsIgnoreCase("true")?"saas":"onprem", org) + "v1/organizations/" + org + "/environments/" + env + "/virtualhosts/" + vHostName;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("Authorization", apigeeUtil.getApigeeAuth(org, isSaaS.equalsIgnoreCase("true")?"saas":"onprem"));
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			ResponseEntity<VirtualHost> response  = 
					restTemplate.exchange(apigeeURL, HttpMethod.GET, requestEntity, VirtualHost.class);
			VirtualHost	virtualHost	= response.getBody();
			if(virtualHost != null) {
				List<String> hosts = new ArrayList<String>();
				for(String hAlias : virtualHost.getHostAliases())
				{
					String host = ((virtualHost.getsSLInfo()!= null && virtualHost.getsSLInfo().getEnabled().equalsIgnoreCase("true"))? "https":"http") +
							"://" + hAlias + ":" + virtualHost.getPort();
					hosts.add(host);
					System.out.println(host);
				}
				return hosts;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ProxyConnection> getProxyConnections(OrgEnv orgEnv, ProxyArtifacts proxyArtifacts){
		List<ProxyEndpoint> proxyEndpoints = proxyArtifacts.getProxyEndpoints();
		List<ProxyConnection> proxyConnections = new ArrayList<ProxyConnection>();
		for(Env env: orgEnv.getEnvs()) {
			for(ProxyEndpoint proxyEndpoint : proxyEndpoints)
				for (String virtualHost : proxyEndpoint.getVirtualHosts()) {
					List<String> hosts = getProxyConnectionURL(orgEnv.getName(), env.getName(), 
							orgEnv.getType().equalsIgnoreCase("saas")?"true":"false", virtualHost);
					if(hosts != null)	
						for( String host : hosts)
						{
							String url;
							if(host == null) 
								url = "N/A";
							else 
								url = host + proxyEndpoint.getBasePath();
							ProxyConnection connection = new ProxyConnection();
							connection.setEnvName(env.getName());
							connection.setIsSaaS(orgEnv.getType().equalsIgnoreCase("saas")?"true":"false");
							connection.setOrgName(orgEnv.getName());
							connection.setProxyEndpoint(proxyEndpoint.getName());
							connection.setProxyURL(url);
							proxyConnections.add(connection);
						}
				}
		}
		return proxyConnections;
	}
	
	private void createProduct(OrgEnv orgEnv, String proxyName) {
		for(Env env: orgEnv.getEnvs()) {
			Organization org = new Organization();
			org.setName(orgEnv.getName());	
			org.setEnv(env.getName());
			org.setType(orgEnv.getType());
			
			try {
				createServiceProdutConfig(org,proxyName, null);
			} catch (ItorixException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void publishProxyConnections( String proxyName, OrgEnv orgEnv) {
		try {
			String projectName = codeGenService.getProjectName(proxyName);
			publishProxyConnections(projectName, proxyName, orgEnv);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void publishProxyConnections(String projectName, String proxyName, OrgEnv orgEnv) {
		try {
			createProduct(orgEnv, proxyName);
			if(projectName == null)
				projectName = codeGenService.getProjectName(proxyName);
			//codeGenService.saveAssociatedOrgforProxy(proxyName,orgEnv);
			Project project = projectManagementDao.findByProjectName(projectName);
			ProxyArtifacts proxyArtifacts = codeGenService.getProxyArtifacts(proxyName);
			List<ProxyConnection> connections = getProxyConnections(orgEnv, proxyArtifacts);
			List<ProxyConnection> uniqueconnections;
			if(project.getProxyByName(proxyName).getProxyConnections()!= null)
			{
				uniqueconnections = getUniqueValues(project.getProxyByName(proxyName).getProxyConnections(), orgEnv.getName(), orgEnv.getEnvs().get(0).getName());
				uniqueconnections.addAll(connections);
			}
			else 
				uniqueconnections = connections;
			project.getProxyByName(proxyName).setProxyConnections(uniqueconnections);
			projectManagementDao.updateProject(project, null);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void publishProxyOrg( String proxyName, OrgEnv orgEnv) {
		try {
			ProxyData data = codeGenService.getProxyData(proxyName);
			for(Env env: orgEnv.getEnvs())
				env.setStatus("deployed");
			OrgEnvs org = data.getOrgEnvs() != null ? data.getOrgEnvs() : new OrgEnvs();
			org.addOrgEnv(orgEnv);
			data.setOrgEnvs(org);
			codeGenService.saveProxyData(data);
			//codeGenService.saveAssociatedOrgforProxy(proxyName,orgEnv);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<ProxyConnection> getUniqueValues(List<ProxyConnection> connections, String org, String env){
		List<ProxyConnection> connectionList = new ArrayList<ProxyConnection>();
		for(ProxyConnection connection : connections) {
			if(!(connection.getOrgName().equals(org) && connection.getEnvName().equals(env)))
				connectionList.add(connection);
		}
		return connectionList;
	}


}
