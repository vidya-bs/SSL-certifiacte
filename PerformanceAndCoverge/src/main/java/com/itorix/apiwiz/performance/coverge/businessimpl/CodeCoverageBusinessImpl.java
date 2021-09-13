package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.zeroturnaround.zip.ZipUtil;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.policyperformance.ExecutedFlowAndPolicies;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.FaultRule;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.FaultRules;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.Flow;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.PostClientFlow;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.ProxyEndpoint;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.Response;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.Step;
import com.itorix.apiwiz.common.model.policyperformance.target.endpoint.TargetEndpoint;
import com.itorix.apiwiz.common.postman.PostmanRunResult;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.performance.coverge.business.CodeCoverageBusiness;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageBackUpInfo;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageVO;
import com.itorix.apiwiz.performance.coverge.model.CoverageReport;
import com.itorix.apiwiz.performance.coverge.model.EndpointStat;
import com.itorix.apiwiz.performance.coverge.model.EndpointStatVO;
import com.itorix.apiwiz.performance.coverge.model.FlowExecutions;
import com.itorix.apiwiz.performance.coverge.model.FlowStat;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.PolicyStatus;
import com.itorix.apiwiz.performance.coverge.model.ProxyStat;
import com.itorix.apiwiz.performance.coverge.model.Stats;
import com.itorix.apiwiz.testsuite.dao.TestSuiteDAO;
import com.itorix.test.executor.TestExecutor;
import com.itorix.test.executor.beans.Scenario;
import com.itorix.test.executor.beans.TestCase;
import com.itorix.test.executor.beans.TestSuite;
import com.itorix.test.executor.beans.TestSuiteResponse;
import com.itorix.test.executor.beans.Variables;

@Component
public class CodeCoverageBusinessImpl implements CodeCoverageBusiness {

	private static final Logger logger = LoggerFactory.getLogger(CodeCoverageBusinessImpl.class);

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	ApigeeUtil apigeeUtil;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	/*
	 * @Autowired ApigeeService apigeeService;
	 */

	@Autowired
	CommonServices commonServices;

	@Autowired
	TestSuiteDAO testsuitDAO;

	/**
	 * executeCodeCoverage
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public CodeCoverageBackUpInfo executeCodeCoverage(CommonConfiguration cfg) throws Exception {
		log("executeCodeCoverage", cfg.getInteractionid(), cfg);
		long timeBegin = System.currentTimeMillis();
		long timeStamp = System.currentTimeMillis();
		String backUpLocation = applicationProperties.getMonitorDir() + timeStamp;
		CodeCoverageBackUpInfo codeCoverageBackUpInfo = new CodeCoverageBackUpInfo();
		codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
		codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
		codeCoverageBackUpInfo.setProxy(cfg.getApiName());
		codeCoverageBackUpInfo.setApigeeUser(cfg.getUserName());
		codeCoverageBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		codeCoverageBackUpInfo = baseRepository.save(codeCoverageBackUpInfo);

		String rev = null;

		// JSONArray txIds = null;
		// step 1: getProxyDeployedRevision
		rev = commonServices.getLatestDeploymentForAPIProxy(cfg);
		log("executeCodeCoverage", cfg.getInteractionid(), "step 1: getProxyDeployedRevision ::" + rev);
		cfg.setRevision(rev);
		// step 2: create session with filter
		/*
		 * String sessionID = apigeeUtil.createSession(cfg);
		 * log("executeCodeCoverage", cfg.getInteractionid(),
		 * "step 2: create session with filter  sessionID ::" + sessionID);
		 */
		// step 3: execute postman
		List<Object> traces = commonServices.executeLivePostmanCollectionTraceAsObject(cfg, backUpLocation);
		// log("executeCodeCoverage", cfg.getInteractionid(), "step 3: execute
		// postman ::" + status);
		// step4: getTransactionIds
		/*
		 * if (sessionID != null) { txIds =
		 * commonServices.getTransactionIds(cfg, sessionID);
		 * log("executeCodeCoverage", cfg.getInteractionid(),
		 * " step4: getTransactionIds ::" + txIds); }
		 */

		// step5: getTransactionData
		/*
		 * List<Trace> traces = commonServices.getTransactionData(cfg,
		 * sessionID, txIds); log("executeCodeCoverage", cfg.getInteractionid(),
		 * " step5: getTransactionData ::" + traces);
		 */
		// step6: get ExecutedFlowAnd PoliciesMap's in DTO
		ExecutedFlowAndPolicies executedFlowAndPolicies = commonServices.getExecutedFlowAndPolicies(traces);
		log("executeCodeCoverage", cfg.getInteractionid(),
				" step6: get ExecutedFlowAnd PoliciesMap ::" + executedFlowAndPolicies);
		// Step 7: Generate Codecoverage report
		ProxyStat proxyStat = getCodeCoverageReport(cfg, executedFlowAndPolicies, new File(backUpLocation));
		log("executeCodeCoverage", cfg.getInteractionid(), "Step 7: Generate Codecoverage report ::" + proxyStat);
		// step 8: delete session
		/*
		 * String sessionStatus = commonServices.deleteSession(cfg, sessionID);
		 * log("executeCodeCoverage", cfg.getInteractionid(),
		 * " step 8: delete session ::" + sessionStatus);
		 */
		// step 9: execute xslt
		try {
			executeXslts(applicationProperties.getMonitorDir() + timeStamp + "/");
		} catch (TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		// step 10: copy supporting files
		// File bootStrap = new
		// ClassPathResource("bootstrap.min.css").getFile();
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("bootstrap.min.css");
		FileUtils.copyInputStreamToFile(inputStream,
				new File(applicationProperties.getMonitorDir() + timeStamp + "/bootstrap.min.css"));
		inputStream.close();

		// File bootStrap = new
		// File(CodeCoverageService.class.getClassLoader().getResource("bootstrap.min.css").getFile());
		// String fileName = bootStrap.getName();
		// FileUtils.copyFile(bootStrap, new
		// File(applicationProperties.getMonitorDir() + timeStamp + "/" +
		// fileName));
		FileUtils.copyDirectory(new File(applicationProperties.getMonitorDir() + timeStamp + "/"),
				new File(applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
						+ cfg.getEnvironment() + "-" + cfg.getApiName() + "/"));

		// zip dist foler and save to db
		String zipFileName = applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
				+ cfg.getEnvironment() + "-" + cfg.getApiName() + ".zip";
		ZipUtil.pack(new File(applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
				+ cfg.getEnvironment() + "-" + cfg.getApiName()), new File(zipFileName));
		JSONObject obj = null;
		try {
			obj = jfrogUtilImpl.uploadFiles(zipFileName, applicationProperties.getApigeeCodecoverage(),
					applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort() + "/artifactory/",
					"Codecoverage/" + timeStamp + "", applicationProperties.getJfrogUserName(),
					applicationProperties.getJfrogPassword());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		// TODO We need to delete the hard copy of zipFileName
		long end = System.currentTimeMillis();
		codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
		codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
		codeCoverageBackUpInfo.setProxy(cfg.getApiName());
		codeCoverageBackUpInfo.setTimeTaken((end - timeBegin) / 1000);
		codeCoverageBackUpInfo.setUrl(obj.getString("downloadURI"));
		codeCoverageBackUpInfo.setProxyStat(proxyStat);
		codeCoverageBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
		codeCoverageBackUpInfo.setHtmlReportLoc(obj.getString("downloadURI"));
		codeCoverageBackUpInfo = baseRepository.save(codeCoverageBackUpInfo);
		log("executeCodeCoverage", cfg.getInteractionid(), codeCoverageBackUpInfo);
		return codeCoverageBackUpInfo;
	}

	/**
	 * executeXslts
	 *
	 * @param string
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void executeXslts(String string) throws FileNotFoundException, IOException, TransformerException {
		File folder = new File(string);
		File[] filesList = folder.listFiles();
		for (File f : filesList) {
			// String summaryXslt = IOUtils.toString(new FileInputStream(new
			// ClassPathResource("summary.xsl").getFile()));
			String summaryXslt = IOUtils
					.toString(CodeCoverageBusinessImpl.class.getClassLoader().getResourceAsStream("summary.xsl"));
			// String proxyXslt = IOUtils.toString(new FileInputStream(new
			// ClassPathResource("proxy.xsl").getFile()));
			String proxyXslt = IOUtils
					.toString(CodeCoverageBusinessImpl.class.getClassLoader().getResourceAsStream("proxy.xsl"));
			StreamSource streamSummaryXslt = new StreamSource(new StringReader(summaryXslt));
			StreamSource streamProxyXslt = new StreamSource(new StringReader(proxyXslt));
			if (FilenameUtils.getBaseName(f.getAbsolutePath()).equals("summary")
					&& FilenameUtils.getExtension(f.getAbsolutePath()).equals("xml")) {
				TransformerFactory fac = TransformerFactory.newInstance();
				Transformer t = fac.newTransformer(streamSummaryXslt);
				Source s = new StreamSource(new File(f.getAbsolutePath()));
				Result r = new StreamResult(new File(string + "" + "summary.html"));
				t.transform(s, r);
			}
			if (!FilenameUtils.getBaseName(f.getAbsolutePath()).equals("summary")
					&& FilenameUtils.getExtension(f.getAbsolutePath()).equals("xml")) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder;
				System.out.println(f.getAbsolutePath());
				String root = "";
				try {
					docBuilder = dbFactory.newDocumentBuilder();
					Document xmlDom = docBuilder.parse(f);
					Node node = xmlDom.getDocumentElement();
					root = node.getNodeName();
					xmlDom.getElementsByTagName("TargetEndpoint");
					// System.out.println("");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TransformerFactory fac = TransformerFactory.newInstance();
				Transformer t = fac.newTransformer(streamProxyXslt);
				Source s = new StreamSource(new File(f.getAbsolutePath()));
				Result r = null;
				if (root.equalsIgnoreCase("TargetEndpoint")) {
					String fileName = string + "Target_" + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".html";
					r = new StreamResult(new File(fileName));
				} else if (root.equalsIgnoreCase("ProxyEndpoint")) {
					String fileName = string + "Proxy_" + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".html";
					r = new StreamResult(new File(fileName));
				}
				t.transform(s, r);
			}
		}
	}

	/**
	 * getCodeCoverageReport
	 *
	 * @param cfg
	 * @param executedFlowAndPolicies
	 * @param fileLoc
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 * @throws ItorixException
	 * @throws JSONException
	 */
	private ProxyStat getCodeCoverageReport(CommonConfiguration cfg, ExecutedFlowAndPolicies executedFlowAndPolicies,
			File fileLoc) throws JAXBException, IOException, ItorixException, JSONException {

		if (!fileLoc.exists())
			fileLoc.mkdirs();

		ProxyStat ProxyStat = new ProxyStat();
		Stats rootStatsType = new Stats();
		// process codecoverage for proxyendpoints
		List<ProxyEndpoint> proxyEndPoints = commonServices.fetchProxyEndPoints(cfg.getOrganization(),
				cfg.getApigeeEmail(), cfg.getApigeePassword(), cfg.getRevision(), cfg.getApiName(), cfg.getType());

		List<EndpointStat> p = new ArrayList<EndpointStat>();
		Set<String> totalPoliciesMap = new HashSet<>();
		Set<String> executedPoliciesMap = new HashSet<>();
		for (ProxyEndpoint temp : proxyEndPoints) {
			ProxyEndpoint updatedEndpoint = markExecutedPoliciesForProxy(temp, executedFlowAndPolicies);
			EndpointStatVO stat = doAnalyticsForProxyEndpoint(updatedEndpoint);
			p.add(stat.getEndpointStat());
			totalPoliciesMap.addAll(stat.getTotalPoliciesMap());
			executedPoliciesMap.addAll(stat.getExecutedPoliciesMap());
			// save the updated proxyendpoint files
			JAXBContext jaxbContext = JAXBContext.newInstance(ProxyEndpoint.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			File file = new File(fileLoc + "/" + temp.getName() + ".xml");
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream s = new FileOutputStream(file, false);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(updatedEndpoint, s);
		}
		// process codecoverage for targetendpoints
		List<TargetEndpoint> targetEndPoints = commonServices.fetchTargetEndPoints(cfg.getOrganization(),
				cfg.getApigeeEmail(), cfg.getApigeePassword(), cfg.getRevision(), cfg.getApiName(), cfg.getType());
		List<EndpointStat> t = new ArrayList<EndpointStat>();
		for (TargetEndpoint temp : targetEndPoints) {
			TargetEndpoint updatedEndpoint = markExecutedPoliciesForTarget(temp, executedFlowAndPolicies);
			EndpointStatVO stat = doAnalyticsForTargetEndpoint(updatedEndpoint);
			t.add(stat.getEndpointStat());
			totalPoliciesMap.addAll(stat.getTotalPoliciesMap());
			executedPoliciesMap.addAll(stat.getExecutedPoliciesMap());
			// save the updated proxyendpoint files
			JAXBContext jaxbContext = JAXBContext.newInstance(TargetEndpoint.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			File file = new File(fileLoc + "/" + temp.getName() + ".xml");
			file.getParentFile().mkdirs(); // Will create parent directories if
			// not exists
			file.createNewFile();
			FileOutputStream s = new FileOutputStream(file, false);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(updatedEndpoint, s);
		}
		t.addAll(p);
		// prepare final xml
		rootStatsType.setEndpointStatType(t);
		ProxyStat.setStats(rootStatsType);
		ProxyStat.setName(cfg.getApiName());
		ProxyStat.setExecutedPolicies(executedPoliciesMap.size() + "");
		ProxyStat.setTotalPolicies(totalPoliciesMap.size() + "");
		float coverage = 0;
		if (totalPoliciesMap.size() > 0) {
			coverage = ((executedPoliciesMap.size() * 100) / totalPoliciesMap.size());
		}
		ProxyStat.setCoverage(coverage + "");

		JAXBContext jaxbContext = JAXBContext.newInstance(ProxyStat.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		File file = new File(fileLoc + "/" + "summary.xml");
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(ProxyStat, file);
		StringWriter sw = new StringWriter();
		jaxbMarshaller.marshal(ProxyStat, sw);
		String proxyStatAsXML = sw.toString();
		JSONObject xmlJSONObj = XML.toJSONObject(proxyStatAsXML);
		logger.debug(xmlJSONObj.toString());
		return ProxyStat;
	}

	/**
	 * markExecutedPoliciesForProxy
	 *
	 * @param endpoint
	 * @param executedFlowAndPolicies
	 * 
	 * @return
	 */
	public ProxyEndpoint markExecutedPoliciesForProxy(ProxyEndpoint endpoint,
			ExecutedFlowAndPolicies executedFlowAndPolicies) {
		// put all executedFlowAndPolices into one list so that all policies can
		// be verified using contains method
		Map<String, List<String>> executedFlowAndPoliciesMap = executedFlowAndPolicies.getExecutedPoliciesMap();
		Map<String, List<String>> executedFlowMap = executedFlowAndPolicies.getExecutedFlowMap();
		List<String> allExecutedPolicies = new ArrayList<>();
		for (Entry<String, List<String>> entry : executedFlowAndPoliciesMap.entrySet()) {
			allExecutedPolicies.addAll(entry.getValue());
		}
		List<String> allExecutedFlows = new ArrayList<>();
		for (Entry<String, List<String>> entry : executedFlowMap.entrySet()) {
			allExecutedFlows.addAll(entry.getValue());
		}
		// PreFlow
		for (Step r : endpoint.getPreFlow().getRequest().getStep()) {
			if (allExecutedPolicies.contains(r.getName())) {
				r.setExecuted("true");
			}
		}

		for (Step r : endpoint.getPreFlow().getResponse().getStep()) {
			if (allExecutedPolicies.contains(r.getName())) {
				r.setExecuted("true");
			}
		}

		// PostFlow
		for (Step r : endpoint.getPostFlow().getRequest().getStep()) {
			if (allExecutedPolicies.contains(r.getName())) {
				r.setExecuted("true");
			}
		}

		for (Step r : endpoint.getPostFlow().getResponse().getStep()) {
			if (allExecutedPolicies.contains(r.getName())) {
				r.setExecuted("true");
			}
		}

		FaultRules faultRules = endpoint.getFaultRules();
		if (faultRules != null) {
			for (FaultRule faultRule : faultRules.getFaultRule()) {
				if (allExecutedFlows.contains("FAULT_" + faultRule.getName())) {
					for (Step r : faultRule.getStep()) {
						if (allExecutedPolicies.contains(r.getName())) {
							r.setExecuted("true");
						}
					}
				}
			}
		}
		// Flows
		for (Flow f : endpoint.getFlows().getFlow()) {
			if (allExecutedFlows.contains(f.getName() + "_REQUEST_FLOW")
					|| allExecutedFlows.contains(f.getName() + "_RESPONSE_FLOW")) {
				for (Step r : f.getRequest().getStep()) {
					if (allExecutedPolicies.contains(r.getName())) {
						r.setExecuted("true");
					}
				}

				for (Step r : f.getResponse().getStep()) {
					if (allExecutedPolicies.contains(r.getName())) {
						r.setExecuted("true");
					}
				}
			}
		}
		// PostClientFlow
		PostClientFlow postClientFlow = endpoint.getPostClientFlow();
		if (postClientFlow != null) {
			Response response = postClientFlow.getResponse();
			if (response != null) {
				List<Step> step = new ArrayList<>();
				step = response.getStep();
				if (step != null) {
					for (Step r : endpoint.getPostClientFlow().getResponse().getStep()) {
						if (allExecutedPolicies.contains(r.getName())) {
							r.setExecuted("true");
						}
					}
				}
			}
		}

		return endpoint;
	}

	/**
	 * markExecutedPoliciesForTarget
	 *
	 * @param endpoint
	 * @param executedFlowAndPolicies
	 * 
	 * @return
	 */
	public TargetEndpoint markExecutedPoliciesForTarget(TargetEndpoint endpoint,
			ExecutedFlowAndPolicies executedFlowAndPolicies) {

		// put all executedFlowAndPolices into one list so that all policies can
		// be verified using contains method
		Map<String, List<String>> executedFlowAndPoliciesMap = executedFlowAndPolicies.getExecutedPoliciesMap();
		Map<String, List<String>> executedFlowMap = executedFlowAndPolicies.getExecutedFlowMap();
		List<String> allExecutedPolicies = new ArrayList<>();
		for (Entry<String, List<String>> entry : executedFlowAndPoliciesMap.entrySet()) {
			allExecutedPolicies.addAll(entry.getValue());
		}
		List<String> allExecutedFlows = new ArrayList<>();
		for (Entry<String, List<String>> entry : executedFlowMap.entrySet()) {
			allExecutedFlows.addAll(entry.getValue());
		}

		if (allExecutedFlows.contains("TARGET_" + endpoint.getName())) {
			// PreFlow
			for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : endpoint.getPreFlow()
					.getRequest().getStep()) {
				if (allExecutedPolicies.contains(r.getName())) {
					r.setExecuted("true");
				}
			}

			for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : endpoint.getPreFlow()
					.getResponse().getStep()) {
				if (allExecutedPolicies.contains(r.getName())) {
					r.setExecuted("true");
				}
			}

			// PostFlow
			for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : endpoint.getPostFlow()
					.getRequest().getStep()) {
				if (allExecutedPolicies.contains(r.getName())) {
					r.setExecuted("true");
				}
			}

			for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : endpoint.getPostFlow()
					.getResponse().getStep()) {
				if (allExecutedPolicies.contains(r.getName())) {
					r.setExecuted("true");
				}
			}

			// FaultHandling
			com.itorix.apiwiz.common.model.policyperformance.target.endpoint.FaultRules faultRules = endpoint
					.getFaultRules();
			if (faultRules != null) {
				for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.FaultRule faultRule : faultRules
						.getFaultRule()) {
					if (allExecutedFlows.contains("FAULT_" + faultRule.getName())) {
						for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : faultRule
								.getStep()) {
							if (allExecutedPolicies.contains(r.getName())) {
								r.setExecuted("true");
							}
						}
					}
				}
			}

			// Flows

			for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Flow f : endpoint.getFlows()
					.getFlow()) {
				if (allExecutedFlows.contains(f.getName() + "_REQUEST_FLOW")
						|| allExecutedFlows.contains(f.getName() + "_RESPONSE_FLOW")) {
					for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : f.getRequest()
							.getStep()) {
						if (allExecutedPolicies.contains(r.getName())) {
							r.setExecuted("true");
						}
					}

					for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step r : f.getResponse()
							.getStep()) {
						if (allExecutedPolicies.contains(r.getName())) {
							r.setExecuted("true");
						}
					}
				}
			}
		}
		return endpoint;
	}

	/**
	 * doAnalyticsForProxyEndpoint
	 *
	 * @param updatedEndpoint
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 */
	public EndpointStatVO doAnalyticsForProxyEndpoint(ProxyEndpoint updatedEndpoint) throws JAXBException {
		EndpointStatVO endpointStatVO = new EndpointStatVO();
		EndpointStat endpointStatType = new EndpointStat();
		endpointStatType.setName(updatedEndpoint.getName());
		Stats statsTypeFlows = new Stats();
		Set<String> totalPoliciesMap = new HashSet<>();
		Set<String> executedPoliciesMap = new HashSet<>();
		List<FlowStat> flowsStatsList = new ArrayList<>();
		// getpre
		FlowStat preFlowStat = new FlowStat();
		List<Step> step = new ArrayList<>();
		step.addAll(updatedEndpoint.getPreFlow().getRequest().getStep());
		step.addAll(updatedEndpoint.getPreFlow().getResponse().getStep());
		PolicyCount pc = getCountForFlowProxySteps(step);
		preFlowStat.setFlowType("Pre Flow");
		preFlowStat.setName("Pre Flow");
		preFlowStat.setExecutedPolicies(pc.executedPolicies + "");
		preFlowStat.setTotalPolicies(pc.totalPolicies + "");
		totalPoliciesMap.addAll(pc.totalPoliciesMap);
		executedPoliciesMap.addAll(pc.executedPoliciesMap);
		float precov = 0;
		if (pc.totalPolicies > 0)
			precov = (pc.executedPolicies * 100) / pc.totalPolicies;
		preFlowStat.setCoverage(precov + "");
		flowsStatsList.add(preFlowStat);
		step.clear();
		// post
		FlowStat postFlowStat = new FlowStat();
		step.addAll(updatedEndpoint.getPostFlow().getRequest().getStep());
		step.addAll(updatedEndpoint.getPostFlow().getResponse().getStep());
		PolicyCount post = getCountForFlowProxySteps(step);
		postFlowStat.setFlowType("Post Flow");
		postFlowStat.setName("Post Flow");
		postFlowStat.setExecutedPolicies(post.executedPolicies + "");
		postFlowStat.setTotalPolicies(post.totalPolicies + "");
		totalPoliciesMap.addAll(post.totalPoliciesMap);
		executedPoliciesMap.addAll(post.executedPoliciesMap);
		step.clear();
		float postcov = 0;
		if (post.totalPolicies > 0)
			postcov = (post.executedPolicies * 100) / post.totalPolicies;
		postFlowStat.setCoverage(postcov + "");
		flowsStatsList.add(postFlowStat);
		step.clear();
		// fault
		for (FaultRule faultRule : updatedEndpoint.getFaultRules().getFaultRule()) {
			FlowStat faultFlowStat = new FlowStat();
			PolicyCount fault = getCountForFlowProxySteps(faultRule.getStep());
			faultFlowStat.setFlowType("Flow");
			faultFlowStat.setName(faultRule.getName());
			faultFlowStat.setExecutedPolicies(fault.executedPolicies + "");
			faultFlowStat.setTotalPolicies(fault.totalPolicies + "");
			totalPoliciesMap.addAll(fault.totalPoliciesMap);
			executedPoliciesMap.addAll(fault.executedPoliciesMap);
			float faultcov = 0;
			if (fault.totalPolicies > 0)
				faultcov = (fault.executedPolicies * 100) / fault.totalPolicies;
			faultFlowStat.setCoverage(faultcov + "");
			flowsStatsList.add(faultFlowStat);
		}
		// flows
		for (Flow f : updatedEndpoint.getFlows().getFlow()) {
			FlowStat flowFlowStat = new FlowStat();
			PolicyCount flowReq = getCountForFlowProxySteps(f.getRequest().getStep());
			PolicyCount flowRes = getCountForFlowProxySteps(f.getResponse().getStep());
			flowFlowStat.setFlowType("Flow");
			flowFlowStat.setName(f.getName());
			flowFlowStat.setExecutedPolicies(flowReq.executedPolicies + flowRes.executedPolicies + "");
			flowFlowStat.setTotalPolicies(flowReq.totalPolicies + flowRes.totalPolicies + "");
			totalPoliciesMap.addAll(flowReq.totalPoliciesMap);
			executedPoliciesMap.addAll(flowReq.executedPoliciesMap);
			totalPoliciesMap.addAll(flowRes.totalPoliciesMap);
			executedPoliciesMap.addAll(flowRes.executedPoliciesMap);
			float cov = 0;
			if (flowReq.totalPolicies > 0 || flowRes.executedPolicies > 0)
				cov = ((flowReq.executedPolicies + flowRes.executedPolicies) * 100)
						/ (flowReq.totalPolicies + flowRes.totalPolicies);
			flowFlowStat.setCoverage(cov + "");
			flowsStatsList.add(flowFlowStat);
		}
		// PostClientFlow
		FlowStat postClientFlowStat = new FlowStat();
		if (updatedEndpoint.getPostClientFlow() != null) {
			PostClientFlow postClientFlow = updatedEndpoint.getPostClientFlow();

			step.addAll(postClientFlow.getResponse().getStep());
			postClientFlowStat.setName(postClientFlow.getName());
		}
		PolicyCount pcfs = getCountForFlowProxySteps(step);
		postClientFlowStat.setFlowType("Flow");

		postClientFlowStat.setExecutedPolicies(pcfs.executedPolicies + "");
		postClientFlowStat.setTotalPolicies(pcfs.totalPolicies + "");
		totalPoliciesMap.addAll(pcfs.totalPoliciesMap);
		executedPoliciesMap.addAll(pcfs.executedPoliciesMap);
		float pcfscov = 0;
		if (pcfs.totalPolicies > 0) {
			pcfscov = (pcfs.executedPolicies * 100) / pcfs.totalPolicies;
		}
		postClientFlowStat.setCoverage(pcfscov + "");
		flowsStatsList.add(postClientFlowStat);
		step.clear();
		statsTypeFlows.setFlowStat(flowsStatsList);
		endpointStatType.setStats(statsTypeFlows);
		endpointStatType.setEndpointType("Proxy");
		endpointStatType.setTotalPolicies(totalPoliciesMap.size() + "");
		float proxyCoverage = 0;
		if (totalPoliciesMap.size() > 0)
			proxyCoverage = (executedPoliciesMap.size() * 100) / totalPoliciesMap.size();
		endpointStatType.setCoverage(proxyCoverage + "");
		endpointStatType.setExecutedPolicies(executedPoliciesMap.size() + "");
		endpointStatVO.setEndpointStat(endpointStatType);
		endpointStatVO.setExecutedPoliciesMap(executedPoliciesMap);
		endpointStatVO.setTotalPoliciesMap(totalPoliciesMap);
		return endpointStatVO;
	}

	/**
	 * doAnalyticsForTargetEndpoint
	 *
	 * @param updatedEndpoint
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 */
	public EndpointStatVO doAnalyticsForTargetEndpoint(TargetEndpoint updatedEndpoint) throws JAXBException {
		EndpointStatVO endpointStatVO = new EndpointStatVO();
		EndpointStat endpointStatType = new EndpointStat();
		endpointStatType.setName(updatedEndpoint.getName());
		Stats statsTypeFlows = new Stats();
		Set<String> totalPoliciesMap = new HashSet<>();
		Set<String> executedPoliciesMap = new HashSet<>();
		List<FlowStat> flowsStatsList = new ArrayList<>();
		// getpre
		FlowStat preFlowStat = new FlowStat();
		List<com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step> step = new ArrayList<>();
		step.addAll(updatedEndpoint.getPreFlow().getRequest().getStep());
		step.addAll(updatedEndpoint.getPreFlow().getResponse().getStep());
		PolicyCount pc = getCountForFlowTargetSteps(step);
		preFlowStat.setFlowType("Pre Flow");
		preFlowStat.setName("Pre Flow");
		preFlowStat.setExecutedPolicies(pc.executedPolicies + "");
		preFlowStat.setTotalPolicies(pc.totalPolicies + "");
		totalPoliciesMap.addAll(pc.totalPoliciesMap);
		executedPoliciesMap.addAll(pc.executedPoliciesMap);
		float precov = 0;
		if (pc.totalPolicies > 0)
			precov = (pc.executedPolicies * 100) / pc.totalPolicies;
		preFlowStat.setCoverage(precov + "");
		flowsStatsList.add(preFlowStat);
		step.clear();
		// post
		FlowStat postFlowStat = new FlowStat();
		step.addAll(updatedEndpoint.getPostFlow().getRequest().getStep());
		step.addAll(updatedEndpoint.getPostFlow().getResponse().getStep());
		PolicyCount post = getCountForFlowTargetSteps(step);
		postFlowStat.setFlowType("Post Flow");
		postFlowStat.setName("Post Flow");
		postFlowStat.setExecutedPolicies(post.executedPolicies + "");
		postFlowStat.setTotalPolicies(post.totalPolicies + "");
		totalPoliciesMap.addAll(post.totalPoliciesMap);
		executedPoliciesMap.addAll(post.executedPoliciesMap);
		float postcov = 0;
		if (post.totalPolicies > 0)
			postcov = (post.executedPolicies * 100) / post.totalPolicies;
		postFlowStat.setCoverage(postcov + "");

		flowsStatsList.add(postFlowStat);

		// fault

		for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.FaultRule faultRule : updatedEndpoint
				.getFaultRules().getFaultRule()) {
			FlowStat faultFlowStat = new FlowStat();
			PolicyCount fault = getCountForFlowTargetSteps(faultRule.getStep());
			faultFlowStat.setFlowType("Flow");
			faultFlowStat.setName(faultRule.getName());
			faultFlowStat.setExecutedPolicies(fault.executedPolicies + "");
			faultFlowStat.setTotalPolicies(fault.totalPolicies + "");
			totalPoliciesMap.addAll(fault.totalPoliciesMap);
			executedPoliciesMap.addAll(fault.executedPoliciesMap);
			float faultcov = 0;
			if (fault.totalPolicies > 0)
				faultcov = (fault.executedPolicies * 100) / fault.totalPolicies;
			faultFlowStat.setCoverage(faultcov + "");
			flowsStatsList.add(faultFlowStat);
		}
		// flows
		for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Flow f : updatedEndpoint.getFlows()
				.getFlow()) {

			FlowStat flowFlowStat = new FlowStat();

			PolicyCount flowReq = getCountForFlowTargetSteps(f.getRequest().getStep());
			PolicyCount flowRes = getCountForFlowTargetSteps(f.getResponse().getStep());
			flowFlowStat.setFlowType("Flow");
			flowFlowStat.setName(f.getName());
			flowFlowStat.setExecutedPolicies(flowReq.executedPolicies + flowRes.executedPolicies + "");
			flowFlowStat.setTotalPolicies(flowReq.totalPolicies + flowRes.totalPolicies + "");
			totalPoliciesMap.addAll(flowReq.totalPoliciesMap);
			executedPoliciesMap.addAll(flowReq.executedPoliciesMap);
			totalPoliciesMap.addAll(flowRes.totalPoliciesMap);
			executedPoliciesMap.addAll(flowRes.executedPoliciesMap);
			float cov = 0;
			if (flowReq.totalPolicies > 0 || flowRes.executedPolicies > 0)
				cov = ((flowReq.executedPolicies + flowRes.executedPolicies) * 100)
						/ (flowReq.totalPolicies + flowRes.totalPolicies);
			flowFlowStat.setCoverage(cov + "");
			flowsStatsList.add(flowFlowStat);
		}

		statsTypeFlows.setFlowStat(flowsStatsList);
		endpointStatType.setStats(statsTypeFlows);
		endpointStatType.setEndpointType("Target");
		endpointStatType.setTotalPolicies(totalPoliciesMap.size() + "");
		float targetCoverage = 0;
		if (totalPoliciesMap.size() > 0) {
			targetCoverage = (executedPoliciesMap.size() * 100) / totalPoliciesMap.size();
		}
		endpointStatType.setCoverage(targetCoverage + "");
		endpointStatType.setExecutedPolicies(executedPoliciesMap.size() + "");
		endpointStatVO.setEndpointStat(endpointStatType);
		endpointStatVO.setExecutedPoliciesMap(executedPoliciesMap);
		endpointStatVO.setTotalPoliciesMap(totalPoliciesMap);
		return endpointStatVO;
	}

	/**
	 * getFinalCoverage
	 *
	 * @param executedFlowAndPoliciesMap
	 * @param actualPolicies
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private CoverageReport getFinalCoverage(Map<String, List<String>> executedFlowAndPoliciesMap,
			Map<String, List<String>> actualPolicies) {
		CoverageReport report = new CoverageReport();
		for (Map.Entry<String, List<String>> entry : executedFlowAndPoliciesMap.entrySet()) {
			List<String> executedPolicies = entry.getValue();
			if (actualPolicies.get("preflow").size() > 0) {
				FlowExecutions f = new FlowExecutions();
				f.setFlowType("preflow");
				List<PolicyStatus> al = new ArrayList<PolicyStatus>();

				for (String s : actualPolicies.get("preflow")) {
					PolicyStatus p = new PolicyStatus();
					p.setPolicyName(s);
					if (executedPolicies.contains(s))
						p.setExecuted(true);
					else
						p.setExecuted(false);
					al.add(p);
				}
				f.setPolicyStatus(al);
				report.setPreFlow(f);
			}
			if (actualPolicies.get("Response-Flow").size() > 0) {
				FlowExecutions f = new FlowExecutions();
				f.setFlowType("Response-Flow");
				List<PolicyStatus> al = new ArrayList<PolicyStatus>();

				for (String s : actualPolicies.get("Response-Flow")) {
					PolicyStatus p = new PolicyStatus();
					p.setPolicyName(s);
					if (executedPolicies.contains(s))
						p.setExecuted(true);
					else
						p.setExecuted(false);
					al.add(p);
				}
				f.setPolicyStatus(al);
				report.setResponseFlow(f);
			}
			if (actualPolicies.get("Request-Flow").size() > 0) {
				FlowExecutions f = new FlowExecutions();
				f.setFlowType("Request-Flow");
				List<PolicyStatus> al = new ArrayList<PolicyStatus>();

				for (String s : actualPolicies.get("Request-Flow")) {
					PolicyStatus p = new PolicyStatus();
					p.setPolicyName(s);
					if (executedPolicies.contains(s))
						p.setExecuted(true);
					else
						p.setExecuted(false);
					al.add(p);
				}
				f.setPolicyStatus(al);
				report.setRequestFlow(f);
			}

			if (actualPolicies.get("postflow").size() > 0) {
				FlowExecutions f = new FlowExecutions();
				f.setFlowType("postflow");
				List<PolicyStatus> al = new ArrayList<PolicyStatus>();

				for (String s : actualPolicies.get("postflow")) {
					PolicyStatus p = new PolicyStatus();
					p.setPolicyName(s);
					if (executedPolicies.contains(s))
						p.setExecuted(true);
					else
						p.setExecuted(false);
					al.add(p);
				}
				f.setPolicyStatus(al);
				report.setPostFlow(f);
			}
		}

		return report;
	}

	/**
	 * getCountForFlowProxySteps
	 *
	 * @param step
	 * 
	 * @return
	 */
	private PolicyCount getCountForFlowProxySteps(List<Step> step) {
		int total = step.size();
		int executed = 0;
		Set<String> totalPoliciesMap = new HashSet<>();
		Set<String> executedPoliciesMap = new HashSet<>();
		for (Step s : step) {
			totalPoliciesMap.add(s.getName());
			if (s.getExecuted() == "true") {
				executed++;
				executedPoliciesMap.add(s.getName());
			}
		}

		PolicyCount pc = new PolicyCount();
		pc.totalPolicies = total;
		pc.executedPolicies = executed;
		pc.totalPoliciesMap = totalPoliciesMap;
		pc.executedPoliciesMap = executedPoliciesMap;
		return pc;
	}

	/**
	 * getCountForFlowTargetSteps
	 *
	 * @param step
	 * 
	 * @return
	 */
	private PolicyCount getCountForFlowTargetSteps(
			List<com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step> step) {
		int total = step.size();
		int executed = 0;
		Set<String> totalPoliciesMap = new HashSet<>();
		Set<String> executedPoliciesMap = new HashSet<>();
		for (com.itorix.apiwiz.common.model.policyperformance.target.endpoint.Step s : step) {
			totalPoliciesMap.add(s.getName());
			if (s.getExecuted() == "true") {
				executed++;
				executedPoliciesMap.add(s.getName());
			}
		}
		PolicyCount pc = new PolicyCount();
		pc.totalPolicies = total;
		pc.executedPolicies = executed;
		pc.totalPoliciesMap = totalPoliciesMap;
		pc.executedPoliciesMap = executedPoliciesMap;
		return pc;
	}

	/**
	 * fetchPoliciesInBundle
	 *
	 * @param proxyFile
	 * @param executedFlowAndPoliciesMap
	 * 
	 * @return
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	@SuppressWarnings("unused")
	private CoverageDetailsHelper fetchPoliciesInBundle(File proxyFile,
			Map<String, List<String>> executedFlowAndPoliciesMap)
			throws FileNotFoundException, IOException, JAXBException {

		Map<String, List<String>> actualPolicies = new HashMap<String, List<String>>();

		String targetFileStr = IOUtils.toString(new FileInputStream(proxyFile));
		JAXBContext jaxbContext = JAXBContext.newInstance(ProxyEndpoint.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		ProxyEndpoint endpoint = (ProxyEndpoint) jaxbUnmarshaller.unmarshal(new StringReader(targetFileStr));

		// get preflow policies
		List<String> prePolicies = endpoint.getPreFlow().getRequest().getStep().stream().map(Step::getName)
				.collect(Collectors.toList());
		actualPolicies.put("preflow", prePolicies);

		// get main flow
		String key = (String) executedFlowAndPoliciesMap.keySet().toArray()[0];

		List<String> mainPoliciesInRequest = endpoint.getFlows().getFlow().stream()
				.filter(fl -> fl.getName().equals(key)).flatMap(r -> (r.getRequest().getStep()).stream())
				.map(Step::getName).collect(Collectors.toList());
		List<String> mainPoliciesInResponse = endpoint.getFlows().getFlow().stream()
				.filter(fl -> fl.getName().equals(key)).flatMap(r -> (r.getResponse().getStep()).stream())
				.map(Step::getName).collect(Collectors.toList());

		actualPolicies.put("Request-Flow", mainPoliciesInRequest);
		actualPolicies.put("Response-Flow", mainPoliciesInResponse);

		// get post flow steps
		List<String> postPolicies = endpoint.getPostFlow().getResponse().getStep().stream().map(Step::getName)
				.collect(Collectors.toList());
		actualPolicies.put("postflow", postPolicies);

		int flowPoliciesCount = (null == mainPoliciesInRequest ? 0 : mainPoliciesInRequest.size())
				+ (null == mainPoliciesInResponse ? 0 : mainPoliciesInResponse.size());
		int totalPoliciesCount = (null == postPolicies ? 0 : postPolicies.size()) + flowPoliciesCount
				+ (null == prePolicies ? 0 : prePolicies.size());
		int executedPoliciesCount = executedFlowAndPoliciesMap.get(key).size();
		long codeCoveragePercentage = (executedPoliciesCount * 100) / totalPoliciesCount;

		CoverageDetailsHelper helper = new CoverageDetailsHelper();
		helper.actualPolicies = actualPolicies;
		helper.coveragePercentage = codeCoveragePercentage;

		return helper;
	}

	/**
	 * getCodeCoverageList
	 *
	 * @param interactionid
	 * 
	 * @return
	 */
	public List<History> getCodeCoverageList(String interactionid) throws ItorixException {
		log("getCodeCoverageList", interactionid, "");
		List<CodeCoverageBackUpInfo> codeCoverageInfo = baseRepository
				.findAll(CodeCoverageBackUpInfo.LABEL_CREATED_TIME, "-", CodeCoverageBackUpInfo.class);
		List<History> history = new ArrayList<History>();
		for (CodeCoverageBackUpInfo info : codeCoverageInfo) {
			History h = new History();
			h.setId(info.getId());
			h.setName(info.getProxy());
			h.setModified(info.getMts() + "");
			h.setUser(info.getApigeeUser());
			h.setOrganization(info.getOrganization());
			h.setEnvironment(info.getEnvironment());
			history.add(h);
		}
		log("getCodeCoverageList", interactionid, history);
		return history;
	}

	/**
	 * getCodeCoverageList
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<History> getCodeCoverageList(String interactionid, boolean filter, String proxy, String org, String env,
			String daterange) throws Exception {
		log("getCodeCoverageList", interactionid, "");
		List<CodeCoverageBackUpInfo> codeCoverageInfo = new ArrayList<>();
		List<History> history = new ArrayList<History>();
		Criteria criteria = new Criteria();
		if (filter) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Query query = new Query();
			if (proxy != null) {
				criteria.and("proxy").is(proxy);
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("CodeCoverage-1003"), "CodeCoverage-1003");
			}
			if (org != null) {
				criteria.and("organization").is(org);
			}
			if (env != null) {
				criteria.and("environment").is(env);
			}
			if (daterange != null) {
				String dates[] = daterange.split("~");
				String date0 = dates[0];
				String date1 = dates[1];
				Date startDate = dateFormat.parse(date0);
				Date endDate = dateFormat.parse(date1);
				criteria.andOperator(Criteria.where("cts").gt(getStartOfDay(startDate).getTime()),
						Criteria.where("cts").lt(getEndOfDay(endDate).getTime()));
			}
			query.addCriteria(criteria);
			codeCoverageInfo = baseRepository.find(query, CodeCoverageBackUpInfo.class);

		} else {

			codeCoverageInfo = baseRepository.findAll(CodeCoverageBackUpInfo.LABEL_CREATED_TIME, "-",
					CodeCoverageBackUpInfo.class);
		}
		for (CodeCoverageBackUpInfo info : codeCoverageInfo) {
			History h = new History();
			h.setId(info.getId());
			h.setName(info.getProxy());
			h.setModified(info.getMts() + "");
			h.setUser(info.getApigeeUser());
			h.setOrganization(info.getOrganization());
			h.setEnvironment(info.getEnvironment());
			h.setPercentage(info.getProxyStat() != null ? info.getProxyStat().getCoverage() : "0");
			history.add(h);
		}
		log("getCodeCoverageList", interactionid, history);
		return history;
	}

	/**
	 * getCodeCoverageOnId
	 *
	 * @param id
	 * @param interactionid
	 * 
	 * @return
	 */
	public CodeCoverageBackUpInfo getCodeCoverageOnId(String id, String interactionid) throws ItorixException {
		log("getCodeCoverageOnId", interactionid, id);
		CodeCoverageBackUpInfo response = baseRepository.findById(id, CodeCoverageBackUpInfo.class);
		log("getCodeCoverageOnId", interactionid, response);
		return response;
	}

	/**
	 * deleteCodeCoverageOnId
	 *
	 * @param id
	 * @param interactionid
	 */
	public void deleteCodeCoverageOnId(String id, String interactionid) {
		log("deleteCodeCoverageOnId", interactionid, id);
		baseRepository.delete(id, CodeCoverageBackUpInfo.class);
	}

	/**
	 * executeUnitTests
	 *
	 * @param postman
	 * @param env
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@SuppressWarnings("unchecked")
	public Object executeUnitTests(String postman, String env) throws ItorixException {
		try {
			PerformanceAndCoveragePostmanCollectionRunner runner = new PerformanceAndCoveragePostmanCollectionRunner();
			InputStream collectionStream = new ByteArrayInputStream(postman.getBytes(StandardCharsets.UTF_8));
			InputStream envStream = new ByteArrayInputStream(env.getBytes(StandardCharsets.UTF_8));
			PostmanRunResult result;
			result = runner.executePostManCollection(collectionStream, envStream);
			@SuppressWarnings("rawtypes")
			Map response = new HashMap();
			response.put("total", result.totalTest);
			response.put("failed", result.failedTest);
			response.put("html", getUnitTestReport(result));
			// logger.debug((String) response.get("html"));
			// logger.debug("total : "+(String) response.get("total"));
			// logger.debug("failed : "+(String) response.get("failed"));
			return response;
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "General-1000", e);
		}
	}

	public Object executeSoapUi(String soapUiFile, String soapUiEnv) throws IOException {
		// Map<String, String> map = new
		// SOAPUITestReportExecutor().soapUIProjectTestReportExecutor(FileUtils.readFileToString(new
		// File("/Users/sudhakar/Desktop/REST-Itorix-soapui-project.xml")),null);
		Map<String, String> map = new SOAPUITestReportExecutor().soapUIProjectTestReportExecutor(soapUiFile, null);
		return map;
	}

	public Object executeTestsuiteUnittests(String testsuiteId, String variableId) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String stringTestSuite = mapper.writeValueAsString(testsuitDAO.getTestSuite(testsuiteId));
		String stringvariables = mapper.writeValueAsString(testsuitDAO.getVariablesById(variableId));
		TestSuite testSuite = mapper.readValue(stringTestSuite, TestSuite.class);
		Variables variables = mapper.readValue(stringvariables, Variables.class);
		TestSuiteResponse testSuiteResponse = TestExecutor.executeTestSuite(testSuite, variables, false, false);
		testSuiteResponse.setConfigId(variableId);
		return getUnitTestTestsuiteReport(testSuiteResponse);
	}

	private Object getUnitTestTestsuiteReport(TestSuiteResponse testSuiteResponse) {
		saveExecutionData(testSuiteResponse);
		int total = 0;
		int failed = 0;
		StringBuilder testNames = new StringBuilder();
		for (Scenario scenario : testSuiteResponse.getTestSuite().getScenarios()) {
			for (TestCase testCase : scenario.getTestCases()) {
				total++;
				if (!testCase.getStatus().equalsIgnoreCase("PASS")) {
					failed++;
					testNames.append("<li>" + scenario.getName() + "_" + testCase.getName() + "</li>");
				}
			}
		}
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put("total", Integer.toString(total));
		reportMap.put("failed", Integer.toString(failed));
		String passed = String.valueOf(total - failed);
		String htmlContent = "<html><body><p style=\"text-align: left;\"><span style=\"color: #33cccc;\"><strong>Unit Test Summary&nbsp;</strong></span></p><table border=\"1\" width=\"246\"><tbody><tr style=\"text-align:left;\"><td style=\"width: 115px;\">Total&nbsp;</td><td style=\"width: 115px; text-align: center;\">#total#</td></tr><tr style=\"text-align: left;\"><td style=\"width: 115px;\">Passed</td><td style=\"width: 115px; text-align: center;\">#passed#</td></tr><tr><td style=\"width: 115px;\">Failed</td><td style=\"width: 115px; text-align: center;\">#failed#</td></tr></tbody></table><p>&nbsp;</p><p style=\"text-align: left;\">&nbsp;<span style=\"color: #33cccc;\"><strong>Failed Requests</strong></span></p><ul>#requests#</ul><p>&nbsp;</p></body></html>";
		htmlContent = htmlContent.replaceAll("#total#", Integer.toString(total))
				.replaceAll("#failed#", Integer.toString(failed)).replaceAll("#passed#", passed);
		htmlContent = htmlContent.replaceAll("#requests#", testNames.toString());
		reportMap.put("html", htmlContent);
		return reportMap;
	}

	private void saveExecutionData(TestSuiteResponse testSuiteResponse) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String testSuiteResponseString;
		try {
			testSuiteResponseString = mapper.writeValueAsString(testSuiteResponse);
			System.out.println(testSuiteResponseString);
			com.itorix.apiwiz.testsuite.model.TestSuiteResponse testSuiteExecutionResponse = mapper
					.readValue(testSuiteResponseString, com.itorix.apiwiz.testsuite.model.TestSuiteResponse.class);
			testSuiteExecutionResponse.setCounter(String.valueOf(System.currentTimeMillis()));
			testSuiteExecutionResponse.setStatus("Completed");
			testSuiteExecutionResponse.setManual(true);
			testSuiteExecutionResponse.setCreatedBy(applicationProperties.getServiceUserName());
			testSuiteExecutionResponse.setModifiedBy(applicationProperties.getServiceUserName());
			testSuiteExecutionResponse.setMts(System.currentTimeMillis());
			testSuiteExecutionResponse.setCts(System.currentTimeMillis());
			testsuitDAO.saveTestSuiteResponse(testSuiteExecutionResponse);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * getUnitTestReport
	 *
	 * @param result
	 * 
	 * @return
	 */
	private Object getUnitTestReport(PostmanRunResult result) {
		String htmlContent = "<html><body><p style=\"text-align: left;\"><span style=\"color: #33cccc;\"><strong>Unit Test Summary&nbsp;</strong></span></p><table border=\"1\" width=\"246\"><tbody><tr style=\"text-align:left;\"><td style=\"width: 115px;\">Total&nbsp;</td><td style=\"width: 115px; text-align: center;\">#total#</td></tr><tr style=\"text-align: left;\"><td style=\"width: 115px;\">Passed</td><td style=\"width: 115px; text-align: center;\">#passed#</td></tr><tr><td style=\"width: 115px;\">Failed</td><td style=\"width: 115px; text-align: center;\">#failed#</td></tr></tbody></table><p>&nbsp;</p><p style=\"text-align: left;\">&nbsp;<span style=\"color: #33cccc;\"><strong>Failed Requests</strong></span></p><ul>#requests#</ul><p>&nbsp;</p></body></html>";
		String total = String.valueOf(result.totalTest);
		String failed = String.valueOf(result.failedTest);
		String passed = String.valueOf(result.totalTest - result.failedTest);
		htmlContent = htmlContent.replaceAll("#total#", total).replaceAll("#failed#", failed).replaceAll("#passed#",
				passed);
		StringBuilder testNames = new StringBuilder();
		for (String testname : result.failedTestName) {
			testNames.append("<li>" + testname + "</li>");
		}
		htmlContent = htmlContent.replaceAll("#requests#", testNames.toString());
		return htmlContent;
	}

	/**
	 * codeCoverageTest
	 *
	 * @param codeCoverageVO
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public CodeCoverageBackUpInfo codeCoverageTest(CodeCoverageVO codeCoverageVO) throws Exception {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(codeCoverageVO.getUserName());
		cfg.setApigeePassword(codeCoverageVO.getPassword());
		cfg.setOrganization(codeCoverageVO.getOrg());
		cfg.setEnvironment(codeCoverageVO.getEnv());
		cfg.setApiName(codeCoverageVO.getProxy());
		cfg.setType(codeCoverageVO.getType() != null ? codeCoverageVO.getType() : "onprem");
		cfg.setCodeCoverage(true);
		cfg.setInteractionid(RandomStringUtils.randomAlphanumeric(15).toUpperCase());
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		cfg.setJsessionId(userSessionToken.getId());
		cfg.setTestsuiteId(codeCoverageVO.getTestSuiteId());
		cfg.setVariableId(codeCoverageVO.getConfigId());

		long timeBegin = System.currentTimeMillis();
		long timeStamp = System.currentTimeMillis();
		String backUpLocation = applicationProperties.getMonitorDir() + timeStamp;

		CodeCoverageBackUpInfo codeCoverageBackUpInfo = new CodeCoverageBackUpInfo();
		codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
		codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
		codeCoverageBackUpInfo.setProxy(cfg.getApiName());
		codeCoverageBackUpInfo.setApigeeUser(cfg.getApigeeEmail());
		codeCoverageBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		String rev = null;

		// step 1: getProxyDeployedRevision
		rev = commonServices.getLatestDeploymentForAPIProxy(cfg);
		log("codeCoverageTest", "step 1: getProxyDeployedRevision ::" + rev);
		cfg.setRevision(rev);
		// step 2: create session with filter
		String sessionID = apigeeUtil.createSession(cfg);
		log("codeCoverageTest", cfg.getInteractionid(), "step 2: create session with filter  sessionID ::" + sessionID);
		// step 3: execute postman

		InputStream collectionStream = null;
		if (codeCoverageVO.getPostmanFile() != null)
			collectionStream = new ByteArrayInputStream(
					codeCoverageVO.getPostmanFile().getBytes(StandardCharsets.UTF_8));

		InputStream envStream = null;
		if (codeCoverageVO.getEnvFile() != null)
			envStream = new ByteArrayInputStream(codeCoverageVO.getEnvFile().getBytes(StandardCharsets.UTF_8));

		// step5: getTransactionData
		List<Object> traces = commonServices.executeLivePostmanCollectionTraceAsObject(collectionStream, envStream, cfg,
				backUpLocation);

		log("codeCoverageTest", cfg.getInteractionid(), " step5: getTransactionData ::" + traces);
		if (collectionStream != null)
			collectionStream.close();
		if (envStream != null)
			envStream.close();
		// step6: get ExecutedFlowAnd PoliciesMap's in DTO
		ExecutedFlowAndPolicies executedFlowAndPolicies = commonServices.getExecutedFlowAndPolicies(traces);
		log("executeCodeCoverage", cfg.getInteractionid(),
				" step6: get ExecutedFlowAnd PoliciesMap ::" + executedFlowAndPolicies);
		// Step 7: Generate Codecoverage report
		ProxyStat proxyStat = getCodeCoverageReport(cfg, executedFlowAndPolicies, new File(backUpLocation));
		log("executeCodeCoverage", cfg.getInteractionid(), "Step 7: Generate Codecoverage report ::" + proxyStat);
		// step 8: delete session
		// step 9: execute xslt
		try {
			executeXslts(applicationProperties.getMonitorDir() + timeStamp + "/");
		} catch (TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		// step 10: copy supporting files
		// File bootStrap = new
		// File(CodeCoverageService.class.getClassLoader().getResource("bootstrap.min.css").getFile());

		// File bootStrap = new
		// ClassPathResource("bootstrap.min.css").getFile();

		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("bootstrap.min.css");
		FileUtils.copyInputStreamToFile(inputStream,
				new File(applicationProperties.getMonitorDir() + timeStamp + "/bootstrap.min.css"));
		inputStream.close();
		// FileUtils.copyFile(bootStrap, new
		// File(applicationProperties.getMonitorDir() + timeStamp + "/" +
		// fileName));
		FileUtils.copyDirectory(new File(applicationProperties.getMonitorDir() + timeStamp + "/"),
				new File(applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
						+ cfg.getEnvironment() + "-" + cfg.getApiName() + "/"));

		// zip dist foler and save to db
		String zipFileName = applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
				+ cfg.getEnvironment() + "-" + cfg.getApiName() + ".zip";
		ZipUtil.pack(new File(applicationProperties.getRestoreDir() + timeStamp + "/" + cfg.getOrganization() + "-"
				+ cfg.getEnvironment() + "-" + cfg.getApiName()), new File(zipFileName));
		String downloadURI = null;
		try {
			S3Integration s3Integration = s3Connection.getS3Integration();
			if (null != s3Integration) {
				String workspace = userSessionToken.getWorkspaceId();
				downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
						Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
						workspace + "/codecoverage/" + cfg.getOrganization() + "-" + cfg.getEnvironment() + "-"
								+ cfg.getApiName() + ".zip",
						zipFileName);

			} else {
				org.json.JSONObject obj = jfrogUtilImpl.uploadFiles(zipFileName,
						applicationProperties.getPipelineCodecoverage(),
						applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort()
								+ "/artifactory/",
						"codecoverage-pipeline/" + codeCoverageVO.getProxy() + "/" + timeStamp + "",
						applicationProperties.getJfrogUserName(), applicationProperties.getJfrogPassword());
				downloadURI = obj.getString("downloadURI");
			}
		} catch (Exception e) {
			logger.error("Error Storing file in Artifactory : " + e.getMessage());
			e.printStackTrace();
		}
		// TODO We need to delete the hard copy of zipFileName
		long end = System.currentTimeMillis();
		codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
		codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
		codeCoverageBackUpInfo.setProxy(cfg.getApiName());
		codeCoverageBackUpInfo.setTimeTaken((end - timeBegin) / 1000);
		if (downloadURI != null)
			codeCoverageBackUpInfo.setUrl(downloadURI);
		else
			codeCoverageBackUpInfo.setUrl("N/A");
		codeCoverageBackUpInfo.setProxyStat(proxyStat);
		codeCoverageBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
		log("executeCodeCoverage", cfg.getInteractionid(), codeCoverageBackUpInfo);
		return codeCoverageBackUpInfo;
	}

	public Apigee getApigeeCredential(String jsessionid) {
		UserSession userSessionToken = baseRepository.findById(jsessionid, UserSession.class);
		User user = baseRepository.findById(userSessionToken.getUserId(), User.class);
		if (user != null) {
			Apigee apigee = user.getApigee();
			return apigee;
		} else {
			return null;
		}
	}

	public User getUserDetailsFromSessionID(String jsessionid) {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		// UserSession userSessionToken =
		// baseRepository.findById(jsessionid,UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
		return user;
	}
	/**
	 * executeUnitTests
	 *
	 * @param postman
	 * @param env
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	/*
	 * @SuppressWarnings("unchecked") public Object executeUnitTests(String
	 * postman, String env) throws ItorixException { try {
	 * PostmanCollectionRunner runner = new PostmanCollectionRunner();
	 * InputStream collectionStream = new
	 * ByteArrayInputStream(postman.getBytes(StandardCharsets.UTF_8));
	 * InputStream envStream = new
	 * ByteArrayInputStream(env.getBytes(StandardCharsets.UTF_8));
	 * PostmanRunResult result; result =
	 * runner.executePostManCollection(collectionStream, envStream);
	 *
	 * @SuppressWarnings("rawtypes") Map response = new HashMap();
	 * response.put("total", result.totalTest); response.put("failed",
	 * result.failedTest); response.put("html", getUnitTestReport(result));
	 * return response; } catch (Exception e) { throw new
	 * ItorixException(e.getMessage(), "General-1000", e); } }
	 */

	/**
	 * codeCoverageTest
	 *
	 * @param codeCoverageVO
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	/*
	 * public CodeCoverageBackUpInfo codeCoverageTest(CodeCoverageVO
	 * codeCoverageVO) throws Exception { CommonConfiguration cfg = new
	 * CommonConfiguration(); cfg.setApigeeEmail(codeCoverageVO.getUserName());
	 * cfg.setApigeePassword(codeCoverageVO.getPassword());
	 * cfg.setOrganization(codeCoverageVO.getOrg());
	 * cfg.setEnvironment(codeCoverageVO.getEnv());
	 * cfg.setApiName(codeCoverageVO.getProxy());
	 * cfg.setType(codeCoverageVO.getType());
	 *
	 * cfg.setInteractionid(RandomStringUtils.randomAlphanumeric(15).
	 * toUpperCase());
	 * cfg.setJsessionId(RandomStringUtils.randomAlphanumeric(10).
	 * toUpperCase());
	 *
	 * long timeBegin = System.currentTimeMillis(); long timeStamp =
	 * System.currentTimeMillis(); String backUpLocation =
	 * applicationProperties.getMonitorDir() + timeStamp;
	 *
	 * CodeCoverageBackUpInfo codeCoverageBackUpInfo = new
	 * CodeCoverageBackUpInfo();
	 * codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
	 * codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
	 * codeCoverageBackUpInfo.setProxy(cfg.getApiName());
	 * codeCoverageBackUpInfo.setApigeeUser(cfg.getApigeeEmail());
	 * codeCoverageBackUpInfo.setStatus(Constants.STATUS_INPROGRESS); String rev
	 * = null;
	 *
	 * JSONArray txIds = null; // step 1: getProxyDeployedRevision rev =
	 * commonServices.getLatestDeploymentForAPIProxy(cfg);
	 * log("codeCoverageTest", "step 1: getProxyDeployedRevision ::" + rev);
	 * cfg.setRevision(rev); // step 2: create session with filter String
	 * sessionID = apigeeUtil.createSession(cfg); log("codeCoverageTest",
	 * cfg.getInteractionid(),
	 * "step 2: create session with filter  sessionID ::" + sessionID); // step
	 * 3: execute postman PostmanCollectionRunner runner = new
	 * PostmanCollectionRunner(); InputStream collectionStream = new
	 * ByteArrayInputStream(
	 * codeCoverageVO.getPostmanFile().getBytes(StandardCharsets.UTF_8));
	 * InputStream envStream = new
	 * ByteArrayInputStream(codeCoverageVO.getEnvFile().getBytes(
	 * StandardCharsets.UTF_8)); PostmanRunResult result; result =
	 * runner.executePostManCollection(collectionStream, envStream);
	 * log("codeCoverageTest", cfg.getInteractionid(),
	 * "step 3: execute postman ::" + result); // step4: getTransactionIds if
	 * (sessionID != null) { txIds = commonServices.getTransactionIds(cfg,
	 * sessionID); log("codeCoverageTest", cfg.getInteractionid(),
	 * " step4: getTransactionIds ::" + txIds); }
	 *
	 * // step5: getTransactionData //List<Trace> traces =
	 * commonServices.getTransactionData(cfg, sessionID, txIds); List<Object>
	 * traces = commonServices.executeLivePostmanCollectionTraceAsObject(cfg,
	 * backUpLocation); log("codeCoverageTest", cfg.getInteractionid(),
	 * " step5: getTransactionData ::" + traces); // step6: get ExecutedFlowAnd
	 * PoliciesMap's in DTO ExecutedFlowAndPolicies executedFlowAndPolicies =
	 * commonServices.getExecutedFlowAndPolicies(traces);
	 * log("codeCoverageTest", cfg.getInteractionid(),
	 * " step6: get ExecutedFlowAnd PoliciesMap ::" + executedFlowAndPolicies);
	 * // Step 7: Generate Codecoverage report ProxyStat proxyStat =
	 * getCodeCoverageReport(cfg, executedFlowAndPolicies, new
	 * File(backUpLocation)); log("codeCoverageTest", cfg.getInteractionid(),
	 * "Step 7: Generate Codecoverage report ::" + proxyStat); // step 8: delete
	 * session String sessionStatus = commonServices.deleteSession(cfg,
	 * sessionID); log("codeCoverageTest", cfg.getInteractionid(),
	 * " step 8: delete session ::" + sessionStatus); // step 9: execute xslt
	 * try { executeXslts(applicationProperties.getMonitorDir() + timeStamp +
	 * "/"); } catch (TransformerException e) { logger.error(e.getMessage());
	 * e.printStackTrace(); throw e; }
	 *
	 * // step 10: copy supporting files File bootStrap = new
	 * ClassPathResource("bootstrap.min.css").getFile(); String fileName =
	 * bootStrap.getName(); FileUtils.copyFile(bootStrap, new
	 * File(applicationProperties.getMonitorDir() + timeStamp + "/" +
	 * fileName)); FileUtils.copyDirectory(new
	 * File(applicationProperties.getMonitorDir() + timeStamp + "/"), new
	 * File(applicationProperties.getRestoreDir() + timeStamp + "/" +
	 * cfg.getOrganization() + "-" + cfg.getEnvironment() + "-" +
	 * cfg.getApiName() + "/"));
	 *
	 * // zip dist foler and save to db String zipFileName =
	 * applicationProperties.getRestoreDir() + timeStamp + "/" +
	 * cfg.getOrganization() + "-" + cfg.getEnvironment() + "-" +
	 * cfg.getApiName() + ".zip"; ZipUtil.pack(new
	 * File(applicationProperties.getRestoreDir() + timeStamp + "/" +
	 * cfg.getOrganization() + "-" + cfg.getEnvironment() + "-" +
	 * cfg.getApiName()), new File(zipFileName)); JSONObject obj = null; try {
	 * obj = jfrogUtil.uploadFiles(zipFileName,
	 * "apigee-pipeline-codecoverage-repo", "http://" +
	 * applicationProperties.getJfrogHost() + ":" +
	 * applicationProperties.getJfrogPort() + "/artifactory/", timeStamp + "",
	 * applicationProperties.getJfrogUserName(),
	 * applicationProperties.getJfrogPassword()); } catch (Exception e) {
	 * logger.error(e.getMessage()); e.printStackTrace(); throw e; } // TODO
	 * need to implement the delete the zip directory. long end =
	 * System.currentTimeMillis();
	 * codeCoverageBackUpInfo.setOrganization(cfg.getOrganization());
	 * codeCoverageBackUpInfo.setEnvironment(cfg.getEnvironment());
	 * codeCoverageBackUpInfo.setProxy(cfg.getApiName());
	 * codeCoverageBackUpInfo.setTimeTaken((end - timeBegin) / 1000);
	 * codeCoverageBackUpInfo.setUrl(obj.getString("downloadURI"));
	 * codeCoverageBackUpInfo.setProxyStat(proxyStat);
	 * codeCoverageBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
	 * codeCoverageBackUpInfo.setHtmlReportLoc(zipFileName);
	 * log("executeCodeCoverage", cfg.getInteractionid(),
	 * codeCoverageBackUpInfo); return codeCoverageBackUpInfo; }
	 */

	/**
	 * writeToFile
	 *
	 * @param uploadedInputStream
	 * @param uploadedFileLocation
	 */
	// private void writeToFile(InputStream uploadedInputStream, String
	// uploadedFileLocation) {
	//
	// try {
	// OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
	// int read = 0;
	// byte[] bytes = new byte[1024];
	//
	// out = new FileOutputStream(new File(uploadedFileLocation));
	// while ((read = uploadedInputStream.read(bytes)) != -1) {
	// out.write(bytes, 0, read);
	// }
	// out.flush();
	// out.close();
	// } catch (IOException e) {
	//
	// e.printStackTrace();
	// }
	// }

	@SuppressWarnings("unused")
	protected static class CoverageDetailsHelper {
		private Map<String, List<String>> actualPolicies;
		private long coveragePercentage;
	}

	protected static class PolicyCount {
		private int totalPolicies;
		private int executedPolicies;
		private Set<String> totalPoliciesMap = new HashSet<>();
		private Set<String> executedPoliciesMap = new HashSet<>();
	}

	/**
	 * @param methodName
	 * @param interactionid
	 * @param body
	 */
	private void log(String methodName, String interactionid, Object... body) {
		logger.debug("CodeCoverageService." + methodName + " : CorelationId=" + interactionid
				+ " : request/response Body =" + body);
	}

	public static Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static Date getStartOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfDay);
	}

	private static Date localDateTimeToDate(LocalDateTime startOfDay) {
		return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static LocalDateTime dateToLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
	}
}
