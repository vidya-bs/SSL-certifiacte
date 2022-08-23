package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.common.model.GridFsData;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.policyperformance.ApigeeVO;
import com.itorix.apiwiz.common.model.policyperformance.ExecutedFlowAndPolicies;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.ProxyEndpoint;
import com.itorix.apiwiz.common.model.policyperformance.target.endpoint.TargetEndpoint;
import com.itorix.apiwiz.common.model.postman.Point;
import com.itorix.apiwiz.common.model.postman.PostManBackUpInfo;
import com.itorix.apiwiz.common.model.postman.Property;
import com.itorix.apiwiz.common.model.postman.Result;
import com.itorix.apiwiz.common.model.postman.Trace;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.testsuite.dao.TestSuiteDAO;
import com.itorix.test.executor.beans.Header;
import com.itorix.test.executor.beans.Scenario;
import com.itorix.test.executor.beans.TestCase;
import com.itorix.test.executor.beans.TestSuite;
import com.itorix.test.executor.beans.Variables;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.itorix.test.executor.TestExecutor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@Component
public class CommonServices {
	Logger logger = Logger.getLogger(CommonServices.class);
	@Autowired
	BaseRepository baseRepository;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	ApigeeUtil apigeeUtil;

	@Autowired
	PerformanceAndCoveragePostmanCollectionRunner postmanCollectionRunner;
	@Autowired
	private TestSuiteDAO testsuitDAO;

	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	public static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

	/*
	 * This method is used to create session, pass all the parameters which
	 * would return you sessionid, The generated sessionid shall be used to get
	 * transaction id's further
	 */
	public String createDebugSession(String org, String env, String rev, String apiproxy, String userName,
			String password) throws ItorixException {

		CommonConfiguration cfg = new CommonConfiguration() {
			{
				setOrganization(org);
				setEnvironment(env);
				setApiName(apiproxy);
				setRevision(rev);
				setApigeeEmail(userName);
				setApigeePassword(password);
			}
		};

		return apigeeUtil.createSession(cfg);
	}

	public String deleteSession(CommonConfiguration cfg, String sessionID) throws ItorixException {
		/*
		 * CommonConfiguration cfg = new CommonConfiguration() { {
		 * setOrganization(org); setEnvironment(env); setApiProxyName(apiproxy);
		 * setRevision(rev); setUserName(userName); setPassword(password);
		 * setApigeeEmail(userName); setApigeePassword(password); } };
		 */
		return apigeeUtil.deleteSession(cfg, sessionID);
	}

	/*
	 * public String getProxyDeployedRevision(String org, String proxyname,
	 * String env, String userName, String password) throws IOException {
	 * CommonConfiguration cfg = new CommonConfiguration() { {
	 * setOrganization(org); setEnvironment(env); setApiProxyName(proxyname);
	 * setApigeeEmail(userName); setApigeePassword(password); } }; return
	 * getLatestDeploymentForAPIProxy(cfg);
	 * 
	 * }
	 */

	public PostManBackUpInfo savePostMan(MultipartFile postmanFile, MultipartFile envFile, String org, String env,
			String proxy, String tempToken) {

		PostManBackUpInfo postManBackUpInfo = new PostManBackUpInfo();
		logger.debug(postmanFile.getOriginalFilename());
		try {
			long timeStamp = System.currentTimeMillis();
			String postmanFileBackUpLocation = applicationProperties.getBackupDir() + timeStamp + "/" + org + "-" + env
					+ "-" + proxy;
			if (!new File(postmanFileBackUpLocation).exists()) {
				new File(postmanFileBackUpLocation).mkdirs();
			}
			// instrumentService.savePostmanCollectionToMongo(cfg);
			writeToFile(postmanFile.getInputStream(),
					postmanFileBackUpLocation + "/" + postmanFile.getOriginalFilename());

			writeToFile(envFile.getInputStream(), postmanFileBackUpLocation + "/" + envFile.getOriginalFilename());
			ZipUtil.pack(new File(postmanFileBackUpLocation), new File(postmanFileBackUpLocation + ".zip"));
			GridFSFile gridFSFile = gridFsRepository
					.store(new GridFsData(postmanFileBackUpLocation + ".zip", org + "-" + env + "-" + proxy));
			String oid = gridFSFile.getId().toString();

			long endTime = System.currentTimeMillis();

			File file1 = new File(postmanFileBackUpLocation + "/" + postmanFile.getOriginalFilename());
			File file2 = new File(postmanFileBackUpLocation + "/" + envFile.getOriginalFilename());
			String postManFileContent = null;
			String envFileContent = null;

			postManFileContent = FileUtils.readFileToString(file1);
			envFileContent = FileUtils.readFileToString(file2);

			postManBackUpInfo.setJfrogUrl(oid);
			postManBackUpInfo.setOrganization(org);
			postManBackUpInfo.setEnvironment(env);
			postManBackUpInfo.setProxy(proxy);
			postManBackUpInfo.setTimeTaken((endTime - timeStamp) / 1000);
			postManBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
			postManBackUpInfo.setPostManFileContent(postManFileContent);
			postManBackUpInfo.setEnvFileContent(envFileContent);
			postManBackUpInfo = baseRepository.save(postManBackUpInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception while saving postman file", e);
		}

		return null;
	}

	/*
	 * Pass the generated sessionid from method createSession() to fetch all the
	 * transaction id's This method would return a list of transaction id's that
	 * were found in the session
	 */

	public JSONArray getTransactionIds(CommonConfiguration cfg, String sessionID)
			throws ItorixException, InterruptedException {
		String result = apigeeUtil.getListOfTransactionIds(cfg, sessionID);
		JSONArray txIds = (JSONArray) JSONSerializer.toJSON(result);
		return txIds;
	}

	public String getTransactionId(CommonConfiguration cfg, String sessionID)
			throws ItorixException, InterruptedException {
		String result = apigeeUtil.getListOfTransactionIds(cfg, sessionID);

		return result;
	}

	public List<Trace> getTransactionData(CommonConfiguration cfg, String sessionID, JSONArray txIds)
			throws JsonParseException, JsonMappingException, IOException, ItorixException, InterruptedException {

		List<Trace> tracesList = new ArrayList<Trace>();
		// Fetch trace for each and every transactionId
		for (Object oo : txIds) {
			String tid = (String) oo;
			String traceResult = apigeeUtil.getTraceResponse(cfg, sessionID, tid);
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Trace trace = new Trace();
			trace = objMapper.readValue(traceResult, Trace.class);
			tracesList.add(trace);
		}

		return tracesList;
	}

	public String getXMLTransactionData(CommonConfiguration cfg, String sessionID, JSONArray txIds)
			throws JsonParseException, JsonMappingException, IOException, ItorixException {

		List<String> tracesList = new ArrayList<String>();
		// Fetch trace for each and every transactionId
		for (Object oo : txIds) {
			String tid = (String) oo;
			String traceResult = apigeeUtil.getXMLTraceResponse(cfg, sessionID, tid);
			tracesList.add(traceResult);
			// tracesList.add(tid);
			// ObjectMapper objMapper = new ObjectMapper();
			// objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			// false);
			// Trace trace = new Trace();
			// trace = objMapper.readValue(traceResult, Trace.class);
			// tracesList.add(trace);
		}

		return tracesList.get(0);
	}

	public List<String> getTransactionDataAsString(CommonConfiguration cfg, String sessionID, JSONArray txIds)
			throws JsonParseException, JsonMappingException, IOException, ItorixException {

		// Fetch trace for each and every transactionId
		List<String> traceResult = new ArrayList<String>();
		for (Object oo : txIds) {
			String tid = (String) oo;
			logger.debug(tid);
			String temp = apigeeUtil.getTraceResponseAsXml(cfg, sessionID, tid);
			traceResult.add(temp);
		}

		return traceResult;
	}

	/*
	 * public Map<String, List<String>> getExecutedFlowAndPolicies(List<Trace>
	 * traces) throws JsonParseException, JsonMappingException, IOException {
	 * 
	 * Map<String, List<String>> executedFlowAndPoliciesMap = new
	 * HashMap<String, List<String>>(); // Fetch trace for each and every
	 * transactionId for (Trace tempTrace : traces) {
	 * 
	 * Stream<Point> stream = tempTrace.getPoint().stream(); List<Point> points
	 * = stream .filter(point -> (point.getId().equals("FlowInfo") ||
	 * point.getId().equals("Execution") || point.getId().equals("Condition")))
	 * .collect(Collectors.toList());
	 * 
	 * //List<Property> properties = new ArrayList<Property>(); String flowName
	 * = null; List<String> executedPoliciesInFlow = new ArrayList<String>();
	 * for (Point p : points) { for (Result r : p.getResults()) {
	 * //List<Property> property = new ArrayList<Property>(); if (null !=
	 * r.getProperties() && null != r.getProperties().getProperty() &&
	 * r.getProperties().getProperty().size() > 0) {
	 * 
	 * List<Property> propertyList=r.getProperties().getProperty(); String
	 * stepDefinitionValue=null; Boolean expression=false; Boolean
	 * expressionResult=true; Boolean expression1=false; Boolean
	 * expressionResult1=true; String treeValue=null; for(Property
	 * property:propertyList){ if
	 * (property.getName().equals("proxy.pathsuffix")) { flowName =
	 * property.getValue().substring(1, property.getValue().length()); }else
	 * if(property.getName().equals("stepDefinition-name")){
	 * stepDefinitionValue=property.getValue();
	 * //executedPoliciesInFlow.add(property.getValue()); }else
	 * if(property.getName().equals("expression")){ expression=true; }else
	 * if(property.getName().equals("expressionResult")){
	 * expressionResult=Boolean.parseBoolean(property.getValue()); }else
	 * if(property.getName().equals("Tree")){ treeValue=property.getValue();
	 * }else if(property.getName().equals("Expression")){ expression1=true;
	 * }else if(property.getName().equals("ExpressionResult")){
	 * expressionResult1=Boolean.parseBoolean(property.getValue()); } }
	 * if(stepDefinitionValue !=null && expression == false &&
	 * expressionResult==true){ executedPoliciesInFlow.add(stepDefinitionValue);
	 * }else if(stepDefinitionValue !=null && expression == true &&
	 * expressionResult==true){ executedPoliciesInFlow.add(stepDefinitionValue);
	 * }else if(treeValue !=null && expression1 == true &&
	 * expressionResult1==true){ executedPoliciesInFlow.add(treeValue); }
	 * Stream<Property> streamProperties =
	 * r.getProperties().getProperty().stream(); property =
	 * streamProperties.filter(prop ->
	 * (prop.getName().equals("stepDefinition-name") ||
	 * prop.getName().equals("proxy.pathsuffix"))).collect(Collectors.toList());
	 * if (property.size() > 0) properties.add(property.get(0));
	 * 
	 * }
	 * 
	 * } }
	 * 
	 * 
	 * 
	 * for (Property property : properties) {
	 * 
	 * if (property.getName().equals("proxy.pathsuffix")) { flowName =
	 * property.getValue().substring(1, property.getValue().length()); }
	 * 
	 * if (property.getName().equals("stepDefinition-name")) {
	 * executedPoliciesInFlow.add(property.getValue()); } }
	 * 
	 * List<Property> property = stream .filter(point-> (
	 * point.getId().equals("Execution"))) .flatMap(f->f.getResults().stream())
	 * .flatMap(c->c.getProperties().getProperty().stream())
	 * .filter(d->d.getName().equals("stepDefinition-name"))
	 * .collect(Collectors.toList());
	 * 
	 * executedFlowAndPoliciesMap.put(flowName, executedPoliciesInFlow); }
	 * 
	 * return executedFlowAndPoliciesMap;
	 * 
	 * }
	 */

	public ExecutedFlowAndPolicies getExecutedFlowAndPolicies(List<Object> traces)
			throws JsonParseException, JsonMappingException, IOException {
		ExecutedFlowAndPolicies executedFlowAndPolicies = new ExecutedFlowAndPolicies();
		Map<String, List<String>> executedPoliciesMap = new HashMap<String, List<String>>();
		Map<String, List<String>> executedFlowsMap = new HashMap<String, List<String>>();
		List<String> executedPoliciesInFlow = new ArrayList<String>();
		List<String> executedFlows = new ArrayList<String>();
		String flowName = null;
		// Fetch trace for each and every transactionId
		for (Object trace : traces) {
			Trace tempTrace = (Trace) trace;
			Stream<Point> stream = tempTrace.getPoint().stream();
			List<Point> points = stream.filter(point -> (point.getId().equals("FlowInfo")
					|| point.getId().equals("Execution") || point.getId().equals("Condition")))
					.collect(Collectors.toList());

			// List<Property> properties = new ArrayList<Property>();

			for (Point p : points) {
				for (Result r : p.getResults()) {
					// List<Property> property = new ArrayList<Property>();
					if (null != r.getProperties() && null != r.getProperties().getProperty()
							&& r.getProperties().getProperty().size() > 0) {

						List<Property> propertyList = r.getProperties().getProperty();
						String stepDefinitionValue = null;
						Boolean expression = false;
						Boolean expressionResult = true;
						Boolean expression1 = false;
						Boolean expressionResult1 = true;
						String treeValue = null;
						for (Property property : propertyList) {
							if (property.getName().equals("proxy.pathsuffix")) {
								if (property.getValue() != null && property.getValue().length() > 1) {
									flowName = property.getValue().substring(1, property.getValue().length());
								}
							} else if (property.getName().equals("stepDefinition-name")) {
								stepDefinitionValue = property.getValue();
								// executedPoliciesInFlow.add(property.getValue());
							} else if (property.getName().equals("expression")) {
								expression = true;
							} else if (property.getName().equals("expressionResult")) {
								expressionResult = Boolean.parseBoolean(property.getValue());
							} else if (property.getName().equals("Tree")) {
								treeValue = property.getValue();
							} else if (property.getName().equals("Expression")) {
								expression1 = true;
							} else if (property.getName().equals("ExpressionResult")) {
								expressionResult1 = Boolean.parseBoolean(property.getValue());
							}
						}
						if (stepDefinitionValue != null && expression == false && expressionResult == true) {
							executedPoliciesInFlow.add(stepDefinitionValue);
						} else if (stepDefinitionValue != null && expression == true && expressionResult == true) {
							executedPoliciesInFlow.add(stepDefinitionValue);
						} else if (treeValue != null && expression1 == true && expressionResult1 == true) {
							executedFlows.add(treeValue);
						}
					}
				}
			}
		}
		executedPoliciesMap.put(flowName, executedPoliciesInFlow);
		executedFlowsMap.put(flowName, executedFlows);
		executedFlowAndPolicies.setExecutedPoliciesMap(executedPoliciesMap);
		executedFlowAndPolicies.setExecutedFlowMap(executedFlowsMap);
		// return executedFlowAndPoliciesMap;
		return executedFlowAndPolicies;
	}

	public List<ProxyEndpoint> fetchProxyEndPointsForApigeeX(CommonConfiguration cfg) {
		String path;
		try {
			path = Paths.get(".").toAbsolutePath().normalize().toString();
			byte[] bundle = apigeeUtil.getApigeexAPIProxyRevision(cfg);
			logger.info(File.separator);

			FileUtils.writeByteArrayToFile(new File(path + File.separator + cfg.getApiName() + ".zip"), bundle);
			ZIPUtil zipUtil = new ZIPUtil();
			zipUtil.unzip(path + File.separator + cfg.getApiName() + ".zip", path);

			File folder = new File(path + File.separator + "apiproxy" + File.separator + "proxies");
			File[] listOfFiles = folder.listFiles();
			List<ProxyEndpoint> proxyEndPointList = new ArrayList<ProxyEndpoint>();
			for (int i = 0; i < listOfFiles.length; i++) {
				String content = FileUtils.readFileToString(listOfFiles[i]);
				JAXBContext jaxbContext = JAXBContext.newInstance(ProxyEndpoint.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				ProxyEndpoint endpoint = (ProxyEndpoint) jaxbUnmarshaller.unmarshal(new StringReader(content));
				proxyEndPointList.add(endpoint);

			}
			return proxyEndPointList;

		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			logger.error("ItorixException occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException occurred", e);
		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			logger.error("ArchiveException occurred", e);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("JAXBException occurred", e);
		}
		return null;
	}

	public List<TargetEndpoint> fetchTargetEndPointsForApigeeX(CommonConfiguration cfg) {
		String path;
		try {
			path = Paths.get(".").toAbsolutePath().normalize().toString();
			byte[] bundle = apigeeUtil.getApigeexAPIProxyRevision(cfg);
			FileUtils.writeByteArrayToFile(new File(path + File.separator + cfg.getApiName() + ".zip"), bundle);
			ZIPUtil zipUtil = new ZIPUtil();
			zipUtil.unzip(path + File.separator + cfg.getApiName() + ".zip", path);

			File folder = new File(path + File.separator + "apiproxy" + File.separator + "targets");
			File[] listOfFiles = folder.listFiles();
			List<TargetEndpoint> proxyEndPointList = new ArrayList<TargetEndpoint>();
			for (int i = 0; i < listOfFiles.length; i++) {
				String content = FileUtils.readFileToString(listOfFiles[i]);
				JAXBContext jaxbContext = JAXBContext.newInstance(TargetEndpoint.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				TargetEndpoint endpoint = (TargetEndpoint) jaxbUnmarshaller.unmarshal(new StringReader(content));
				proxyEndPointList.add(endpoint);

			}
			return proxyEndPointList;

		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("RestClientException occurred", e);
		}
		return null;
	}

	public List<ProxyEndpoint> fetchProxyEndPoints(String org, String userName, String password, String rev,
			String apiProxy, String type) throws ItorixException {

		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(userName);
		cfg.setApigeePassword(password);
		cfg.setOrganization(org);
		cfg.setRevision(rev);
		cfg.setApiName(apiProxy);
		cfg.setType(type);
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(org, type));

		Object[] listOfProxyEndPoints = apigeeUtil.getProxyEndPoints(cfg);

		List<ProxyEndpoint> proxyEndPointList = new ArrayList<ProxyEndpoint>();
		for (Object obj : listOfProxyEndPoints) {
			String proxy = (String) obj;
			try {
				cfg.setApiName(apiProxy);
				String end = apigeeUtil.getProxyEndPoint(cfg, proxy);

				JAXBContext jaxbContext = JAXBContext.newInstance(ProxyEndpoint.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				ProxyEndpoint endpoint = (ProxyEndpoint) jaxbUnmarshaller.unmarshal(new StringReader(end));
				proxyEndPointList.add(endpoint);

			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				logger.error("JAXBException occurred", e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			}
		}

		return proxyEndPointList;
	}

	public List<TargetEndpoint> fetchTargetEndPoints(String org, String userName, String password, String rev,
			String apiProxy, String type) throws ItorixException {

		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(userName);
		cfg.setApigeePassword(password);
		cfg.setOrganization(org);
		cfg.setRevision(rev);
		cfg.setApiName(apiProxy);
		cfg.setType(type);
		Object[] listOfTargetEndPoints = apigeeUtil.getTargetEndPoints(cfg);

		List<TargetEndpoint> proxyEndPointList = new ArrayList<TargetEndpoint>();
		for (Object obj : listOfTargetEndPoints) {
			String proxy = (String) obj;
			try {
				String end = apigeeUtil.getTargetEndPoint(cfg, proxy);

				JAXBContext jaxbContext = JAXBContext.newInstance(TargetEndpoint.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				TargetEndpoint endpoint = (TargetEndpoint) jaxbUnmarshaller.unmarshal(new StringReader(end));
				proxyEndPointList.add(endpoint);

			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			}
		}

		return proxyEndPointList;
	}

	public String getLatestDeploymentForAPIProxy(CommonConfiguration cfg) throws IOException, ItorixException {
		if (cfg.getGwtype() != null && cfg.getGwtype().equalsIgnoreCase("apigeex")) {
			String deploymentStr = apigeeUtil.getProxyDeploymentsForProxyInEnvironment(cfg);
			JSONObject deploymentsObj = (JSONObject) JSONSerializer.toJSON(deploymentStr);
			JSONArray deploymentsArr = (JSONArray) deploymentsObj.get("deployments");
			for (Object deployment : deploymentsArr) {
				JSONObject obj = (JSONObject) deployment;
				String envStr = (String) obj.get("environment");
				String rev = (String) obj.get("revision");
				if (cfg.getEnvironment().equals(envStr)) {
					return rev;
				}
			}
			return null;
		} else {
			String deployed = apigeeUtil.getProxyDeploymentsForProxyInEnvironment(cfg);
			JSONObject deployments = (JSONObject) JSONSerializer.toJSON(deployed);
			JSONArray proxyRevisions = (JSONArray) deployments.get("revision");
			List<Integer> al = new ArrayList<Integer>();
			for (Object rev : proxyRevisions) {
				JSONObject obj = (JSONObject) rev;
				String revision = (String) obj.get("name");
				al.add(Integer.parseInt(revision));
			}

			return Collections.max(al) + "";
		}

	}

	public String getDeploymentsForEnvironment(CommonConfiguration cfg) throws IOException, ItorixException {
		String deployments = apigeeUtil.getProxyDeployments(cfg);
		return deployments;
	}

	public String executePostManCollection(ApigeeVO vo) throws Exception {
		String filesLocation = downloadLatestPostMan(vo);
		// download postman finish
		// hit using postman
		File f = new File(filesLocation);
		f.list();
		String postManFileName = null;
		String envFileName = null;
		for (File tempFileName : f.listFiles()) {

			switch (FilenameUtils.getExtension(tempFileName.getAbsolutePath())) {
				case "postman_environment" :
					logger.debug("Getting absolute path of envFileName");
					envFileName = tempFileName.getAbsolutePath();
					break;
				case "json" :
					logger.debug("Getting absolute path of postManFileName");
					postManFileName = tempFileName.getAbsolutePath();
					break;
			}
		}

		PerformanceAndCoveragePostmanCollectionRunner runner = new PerformanceAndCoveragePostmanCollectionRunner();
		runner.executePostManCollection(postManFileName, envFileName);

		return "Executed Postman";
	}

	public String executeLivePostmanCollection(CommonConfiguration cfg, String backupLocation) throws Exception {
		try {
			File fileLoc = new File(backupLocation);
			if (!fileLoc.exists())
				fileLoc.mkdirs();
			FileUtils.copyInputStreamToFile(cfg.getPostmanFile().getInputStream(),
					new File(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename()));
			FileUtils.copyInputStreamToFile(cfg.getEnvFile().getInputStream(),
					new File(backupLocation + "/" + cfg.getEnvFile().getOriginalFilename()));
			PerformanceAndCoveragePostmanCollectionRunner runner = new PerformanceAndCoveragePostmanCollectionRunner();
			runner.executePostManCollection(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename(),
					backupLocation + "/" + cfg.getEnvFile().getOriginalFilename());
			return "Success";
		} catch (IOException e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public List<Object> executeLivePostmanCollectionXMLTraceAsObject(CommonConfiguration cfg, String backupLocation)
			throws Exception {
		try {
			String testsuiteId = cfg.getTestsuiteId();
			String variableId = cfg.getVariableId();
			if (testsuiteId != null && testsuiteId != "" && variableId != null && variableId != "") {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				String stringTestSuite = mapper.writeValueAsString(testsuitDAO.getTestSuite(testsuiteId));
				String stringvariables = mapper.writeValueAsString(testsuitDAO.getVariablesById(variableId));
				TestSuite testSuite = mapper.readValue(stringTestSuite, TestSuite.class);
				Variables variables = mapper.readValue(stringvariables, Variables.class);
				return executeTestsuiteForCodecoverage(testSuite, variables, cfg, true);
			} else {
				File fileLoc = new File(backupLocation);
				if (!fileLoc.exists())
					fileLoc.mkdirs();
				FileUtils.copyInputStreamToFile(cfg.getPostmanFile().getInputStream(),
						new File(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename()));
				FileUtils.copyInputStreamToFile(cfg.getEnvFile().getInputStream(),
						new File(backupLocation + "/" + cfg.getEnvFile().getOriginalFilename()));
				return postmanCollectionRunner.executePostManCollectionTraceAsObject(
						backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename(),
						backupLocation + "/" + cfg.getEnvFile().getOriginalFilename(), cfg);
			}
		} catch (IOException e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public List<Object> executeLivePostmanCollectionTraceAsObject(CommonConfiguration cfg, String backupLocation)
			throws Exception {
		try {
			String testsuiteId = cfg.getTestsuiteId();
			String variableId = cfg.getVariableId();
			if (testsuiteId != null && testsuiteId != "" && variableId != null && variableId != "") {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				String stringTestSuite = mapper.writeValueAsString(testsuitDAO.getTestSuite(testsuiteId));
				String stringvariables = mapper.writeValueAsString(testsuitDAO.getVariablesById(variableId));
				logger.info("testSuite : " + stringTestSuite);
				TestSuite testSuite = mapper.readValue(stringTestSuite, TestSuite.class);
				Variables variables = mapper.readValue(stringvariables, Variables.class);
				return executeTestsuiteForCodecoverage(testSuite, variables, cfg, false);
			} else {
				File fileLoc = new File(backupLocation);
				if (!fileLoc.exists())
					fileLoc.mkdirs();
				FileUtils.copyInputStreamToFile(cfg.getPostmanFile().getInputStream(),
						new File(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename()));
				FileUtils.copyInputStreamToFile(cfg.getEnvFile().getInputStream(),
						new File(backupLocation + "/" + cfg.getEnvFile().getOriginalFilename()));
				return postmanCollectionRunner.executePostManCollectionTraceAsObject(
						backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename(),
						backupLocation + "/" + cfg.getEnvFile().getOriginalFilename(), cfg);
			}
		} catch (IOException e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public List<Object> executeTestsuiteForCodecoverage(TestSuite testSuite, Variables vars, CommonConfiguration cfg,
			boolean traceAsXML) {
		List<com.itorix.test.executor.beans.Header> variables = vars.getVariables();
		Map<String, String> globalVars = TestExecutor.computeHeaders(variables, null);
		List<Object> traceList = new ArrayList<Object>();
		if (testSuite.getScenarios() != null) {
			for (Scenario scenario : testSuite.getScenarios()) {
				Map<String, Boolean> succededTests = new HashMap<String, Boolean>();
				Map<String, Boolean> failedTests = new HashMap<String, Boolean>();
				if (scenario != null && scenario.getTestCases() != null) {
					List<TestCase> testCases = scenario.getTestCases();
					int numberOfTestCases = testCases.size();
					for (int counter = 0; counter < numberOfTestCases; counter++) {
						TestCase testCase = testCases.get(counter);
						Map<String, Integer> testStatus = new HashMap<String, Integer>();
						try {
							if (TestExecutor.canExecuteTestCase(testCase, succededTests, failedTests)) {
								String sessionID = apigeeUtil.createSession(cfg);
								testCase.getRequest().addHeader(new Header("itorix", "", sessionID));
								try {
									TestExecutor.invokeTestCase(testCase, globalVars, testStatus, true, false);

								} catch (Exception ex) {
									logger.error("Exception occurred", ex);
								}
								JSONArray txId = getTransactionIds(cfg, sessionID);

								for (int x = 0; txId.isEmpty() && x < 5; x++) {
									Thread.sleep(10000);
									txId = getTransactionIds(cfg, sessionID);

									if (!txId.isEmpty()) {
										break;
									}

								}
								if (traceAsXML)
									traceList.add(getXMLTransactionData(cfg, sessionID, txId));
								else
									traceList.addAll(getTransactionData(cfg, sessionID, txId));
								apigeeUtil.deleteSession(cfg, sessionID);
							} else {
								counter++;
								if (counter > testCases.size()) {
									counter = 0;
								}
								continue;
							}
						} catch (Exception ex) {
							logger.error("Exception occurred", ex);
						}
					}
				}
			}
		}
		return traceList;
	}

	public List<Object> executeLivePostmanCollectionTraceAsObject(InputStream postManFileName, InputStream envFileName,
			CommonConfiguration cfg, String backupLocation) throws Exception {
		try {
			String testsuiteId = cfg.getTestsuiteId();
			String variableId = cfg.getVariableId();
			if (testsuiteId != null && testsuiteId != "" && variableId != null && variableId != "") {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				String stringTestSuite = mapper.writeValueAsString(testsuitDAO.getTestSuite(testsuiteId));
				String stringvariables = mapper.writeValueAsString(testsuitDAO.getVariablesById(variableId));
				TestSuite testSuite = mapper.readValue(stringTestSuite, TestSuite.class);
				Variables variables = mapper.readValue(stringvariables, Variables.class);
				return executeTestsuiteForCodecoverage(testSuite, variables, cfg, false);
			} else {
				File fileLoc = new File(backupLocation);
				if (!fileLoc.exists())
					fileLoc.mkdirs();
				return postmanCollectionRunner.executePostManCollectionTraceAsObject(postManFileName, envFileName, cfg);
			}
		} catch (IOException e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public String executeLivePostmanCollectionTraceAsString(CommonConfiguration cfg, String backupLocation)
			throws Exception {
		try {
			File fileLoc = new File(backupLocation);
			if (!fileLoc.exists())
				fileLoc.mkdirs();
			FileUtils.copyInputStreamToFile(cfg.getPostmanFile().getInputStream(),
					new File(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename()));
			FileUtils.copyInputStreamToFile(cfg.getEnvFile().getInputStream(),
					new File(backupLocation + "/" + cfg.getEnvFile().getOriginalFilename()));
			PerformanceAndCoveragePostmanCollectionRunner runner = new PerformanceAndCoveragePostmanCollectionRunner();
			runner.executePostManCollection(backupLocation + "/" + cfg.getPostmanFile().getOriginalFilename(),
					backupLocation + "/" + cfg.getEnvFile().getOriginalFilename());
			return "Success";
		} catch (IOException e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public String downloadLatestPostMan(ApigeeVO cfg) {
		long timeStamp = System.currentTimeMillis();
		String postmanFileBackUpLocation = applicationProperties.getRestoreDir() + timeStamp;

		if (!new File(postmanFileBackUpLocation).exists()) {
			new File(postmanFileBackUpLocation).mkdirs();
		}
		String fileName = null;
		try {
			fileName = cfg.getOrg() + "-" + cfg.getEnv() + "-" + cfg.getApi();
			GridFsData gridFsData = new GridFsData(postmanFileBackUpLocation + "/" + fileName + ".zip", fileName);
			gridFsRepository.findLatestByName(gridFsData);
			ZipUtil.unpack(new File(postmanFileBackUpLocation + "/" + fileName + ".zip"),
					new File(postmanFileBackUpLocation + "/" + fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}

		return postmanFileBackUpLocation + "/" + fileName;
	}

	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			logger.error("Exception while performing OutputStream file operations", e);
		}
	}

	public void copyFiles(File src, String dest) {
		File[] filesList = src.listFiles();
		for (File f : filesList) {
			try {
				FileUtils.copyFile(f, new File(dest + "/" + f.getName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			}
		}
	}
}
