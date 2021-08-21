package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.performance.coverge.business.PolicyPerformanceBusiness;
import com.itorix.apiwiz.performance.coverge.model.Debug;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.PolicyPerformanceBackUpInfo;
import com.itorix.apiwiz.performance.coverge.model.Root;
import com.itorix.apiwiz.performance.coverge.model.Transform;

import net.sf.json.JSONArray;

@Component
public class PolicyPerformanceBusinessImpl implements PolicyPerformanceBusiness {
	private static final Logger logger = LoggerFactory.getLogger(PolicyPerformanceBusinessImpl.class);
	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	CommonServices commonServices;
	@Autowired
	ApigeeUtil apigeeUtil;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	public Object executePolicyPerformance(CommonConfiguration cfg) throws ItorixException, Exception {
		log("executePolicyPerformance", cfg.getInteractionid(), cfg);
		long startTime = System.nanoTime();
		long timeStamp = System.currentTimeMillis();
		String backUpLocation = applicationProperties.getMonitorDir() + timeStamp;
		PolicyPerformanceBackUpInfo policyPerformanceInfo = new PolicyPerformanceBackUpInfo();
		policyPerformanceInfo.setOrganization(cfg.getOrganization());
		policyPerformanceInfo.setEnvironment(cfg.getEnvironment());
		policyPerformanceInfo.setProxy(cfg.getApiName());
		policyPerformanceInfo.setStatus(Constants.STATUS_INPROGRESS);
		policyPerformanceInfo.setUser(cfg.getUserName());
		policyPerformanceInfo = baseRepository.save(policyPerformanceInfo);
		String rev = null;
		JSONArray txIds = null;
		// step 1: getProxyDeployedRevision
		try {
			rev = commonServices.getLatestDeploymentForAPIProxy(cfg);
			log("executePolicyPerformance", cfg.getInteractionid(), "step 1: getProxyDeployedRevision" + rev);
			cfg.setRevision(rev);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
			throw e1;
		}

		try {
			List<Object> tracesObjects = commonServices.executeLivePostmanCollectionXMLTraceAsObject(cfg,
					backUpLocation);
			log("executePolicyPerformance", cfg.getInteractionid(), "step5: getTransactionData:" + tracesObjects);
			Root root = new Root();
			List<Debug> dbgLst = new ArrayList<Debug>();
			for (Object traceObj : tracesObjects) {
				String trace = (String) traceObj;
				log("executePolicyPerformance", cfg.getInteractionid(), "step 6: do policy performance :" + trace);
				Document doc = getDoc(trace);
				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile("//Data").evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node nNode = nodeList.item(i);
					String tmp = nodeToString(nNode);
					Debug dbg = getPolicyTimes(tmp);
					dbgLst.add(dbg);
				}
				root.setDebugList((ArrayList<Debug>) dbgLst);
			}
			Map<String, Object> policies = new HashMap<String, Object>();
			policies.put("policies", root.getAverageTimes());
			policies.put("stepTypes", root.getAveragePolicyTimes());
			policyPerformanceInfo.setProxyStat(policies);
		} catch (Exception e) {
			e.printStackTrace();
		}

		long endtTime = System.nanoTime();
		policyPerformanceInfo.setTimeTaken((endtTime - startTime) / 10000000);
		policyPerformanceInfo.setStatus(Constants.STATUS_COMPLETED);
		policyPerformanceInfo = baseRepository.save(policyPerformanceInfo);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String result = mapper.writeValueAsString(policyPerformanceInfo);
		return result;
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
	 * getDoc
	 *
	 * @param content
	 * 
	 * @return
	 */
	private static Document getDoc(String content) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
			Document doc = builder.parse(input);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * nodeToString
	 *
	 * @param node
	 * 
	 * @return
	 */
	private static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error("nodeToString Transformer Exception");
		}
		return sw.toString();
	}

	/**
	 * getPolicyTimes
	 *
	 * @param trace
	 * 
	 * @return
	 */
	public Debug getPolicyTimes(String trace) {
		try {

			/*
			 * String xslt = org.apache.commons.io.IOUtils .toString(new
			 * FileInputStream(new
			 * ClassPathResource("policyPerformance.xslt").getFile()));
			 */
			String xslt = org.apache.commons.io.IOUtils.toString(
					CodeCoverageBusinessImpl.class.getClassLoader().getResourceAsStream("policyPerformance.xslt"));
			StreamSource xmlSource = new StreamSource(new StringReader(trace));
			StreamSource xslSource = new StreamSource(new StringReader(xslt));
			StreamResult stream = Transform.simpleTransform(xmlSource, xslSource);
			StringReader reader = new StringReader(((StringWriter) stream.getWriter()).getBuffer().toString());
			XMLStreamReader XMLReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
			Debug debug = (Debug) JAXBContext.newInstance(Debug.class).createUnmarshaller().unmarshal(XMLReader);
			return debug;
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getPolicyPerformanceList
	 *
	 * @param interactionid
	 * 
	 * @return
	 */
	public List<History> getPolicyPerformanceList(String interactionid) throws Exception {
		log("getPolicyPerformanceList", interactionid, "");
		List<PolicyPerformanceBackUpInfo> policyPerformanceInfo = baseRepository
				.findAll(PolicyPerformanceBackUpInfo.LABEL_CREATED_TIME, "-", PolicyPerformanceBackUpInfo.class);
		List<History> history = new ArrayList<History>();

		for (PolicyPerformanceBackUpInfo info : policyPerformanceInfo) {
			History h = new History();
			h.setId(info.getId());
			h.setName(info.getProxy());
			h.setModified(info.getMts() + "");
			h.setUser(info.getUser());
			h.setOrganization(info.getOrganization());
			h.setEnvironment(info.getEnvironment());
			history.add(h);
		}
		log("getPolicyPerformanceList", interactionid, history);
		return history;
	}

	public List<History> getPolicyPerformanceList(String interactionid, boolean filter, String proxy, String org,
			String env, String daterange) throws Exception {
		log("getPolicyPerformanceList", interactionid, "");
		List<PolicyPerformanceBackUpInfo> policyPerformanceInfo = new ArrayList<>();
		List<History> history = new ArrayList<History>();
		Criteria criteria = new Criteria();
		if (filter) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Query query = new Query();
			if (proxy != null) {
				criteria.and("proxy").is(proxy);
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("PolicyPerformance-1003"),
						"PolicyPerformance-1003");
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
			policyPerformanceInfo = baseRepository.find(query, PolicyPerformanceBackUpInfo.class);
		} else {
			policyPerformanceInfo = baseRepository.findAll(PolicyPerformanceBackUpInfo.LABEL_CREATED_TIME, "-",
					PolicyPerformanceBackUpInfo.class);
		}
		for (PolicyPerformanceBackUpInfo info : policyPerformanceInfo) {
			History h = new History();
			h.setId(info.getId());
			h.setName(info.getProxy());
			h.setModified(info.getMts() + "");
			h.setUser(info.getUser());
			h.setOrganization(info.getOrganization());
			h.setEnvironment(info.getEnvironment());
			history.add(h);
		}
		log("getPolicyPerformanceList", interactionid, history);
		return history;
	}

	/**
	 * getPolicyPerformanceOnId
	 *
	 * @param id
	 * @param interactionid
	 * 
	 * @return
	 */
	public PolicyPerformanceBackUpInfo getPolicyPerformanceOnId(String id, String interactionid) {
		log("getPolicyPerformanceOnId", interactionid, "");
		PolicyPerformanceBackUpInfo response = baseRepository.findOne("id", id, PolicyPerformanceBackUpInfo.class);
		log("getPolicyPerformanceOnId", interactionid, response);
		return response;
	}

	/**
	 * deletePolicyPerformanceOnId
	 *
	 * @param id
	 * @param interactionid
	 */
	public void deletePolicyPerformanceOnId(String id, String interactionid) {
		log("deletePolicyPerformanceOnId", interactionid, "");
		baseRepository.delete(id, PolicyPerformanceBackUpInfo.class);
	}

	/**
	 * log
	 *
	 * @param methodName
	 * @param interactionid
	 * @param body
	 */
	private void log(String methodName, String interactionid, Object... body) {

		logger.debug("PolicyPerformanceService." + methodName + " : CorelationId=" + interactionid
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
