package com.itorix.apiwiz.analytics.dao;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.analytics.model.DashBoardEnvironments;
import com.itorix.apiwiz.analytics.model.DashBoardOverview;
import com.itorix.apiwiz.analytics.model.DashBoardSetUp;
import com.itorix.apiwiz.analytics.model.DashBoardTimeSeries;
import com.itorix.apiwiz.analytics.model.PerformanceMetrics;
import com.itorix.apiwiz.common.model.apigee.ApigeeResponse;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.apigee.DeveloperDetails;
import com.itorix.apiwiz.common.model.apigee.Dimensions;
import com.itorix.apiwiz.common.model.apigee.Environments;
import com.itorix.apiwiz.common.model.apigee.Level;
import com.itorix.apiwiz.common.model.apigee.Metrics;
import com.itorix.apiwiz.common.model.apigee.Values;
import com.itorix.apiwiz.common.model.apigee.metrics.PerformanceTrafficResponse;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.DashBoardOrganisations;

@Component
public class AnalyticsDao {

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	ApigeeUtil apigeeUtil;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	DashBoardTimeSeries dashBoardTimeSeries;

	@Autowired
	ApplicationProperties applicationProperties;

	public Object apiProxyPerformanceTraffic(PerformanceMetrics performanceMetrics, String jsessionId,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {

		try {

			Map<String, String> elementDataWithOutTimeUnit = new HashMap<>();

			CommonConfiguration cfg = new CommonConfiguration();
			cfg.setOrganization(performanceMetrics.getOrgName());
			cfg.setEnvironment(performanceMetrics.getEnvironment());
			if (performanceMetrics.getTimeRange() != null) {
				cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
			}
			cfg.setTimeUnit(performanceMetrics.getTimeUnit());
			cfg.setJsessionId(jsessionId);

			if (performanceMetrics.getType() != null) {
				cfg.setType(performanceMetrics.getType());
			} else {
				cfg.setType("saas");
			}

			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());

			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));

			PerformanceTrafficResponse apiProxyPerfomanceTraffic = apigeeUtil.getApiProxyPerfomanceTraffic(cfg);

			String apiProxyPerfomanceTrafficWithOutTimeUnit = apigeeUtil
					.getApiProxyPerfomanceTrafficWithOutTimeUnit(cfg);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHistory = mapper.readTree(apiProxyPerfomanceTrafficWithOutTimeUnit);
			JsonNode enviromentsNode = jsonHistory.path("environments");

			Iterator<JsonNode> elements = enviromentsNode.elements();
			while (elements.hasNext()) {
				JsonNode metricList = elements.next();
				Iterator<JsonNode> metric = metricList.elements();
				while (metric.hasNext()) {
					JsonNode metricNode = metric.next();
					Iterator<JsonNode> metricNodeDetails = metricNode.elements();
					while (metricNodeDetails.hasNext()) {
						JsonNode metricElement = metricNodeDetails.next();
						System.out.println(metricElement.get("name") + "value::"
								+ metricElement.get("values").elements().next().asText());
						elementDataWithOutTimeUnit.put(metricElement.get("name").asText(),
								metricElement.get("values").elements().next().asText());
					}
				}
			}

			JsonNode performanceTrafficNode = mapper.convertValue(apiProxyPerfomanceTraffic, JsonNode.class);

			JsonNode environmentNode = performanceTrafficNode.path("environments");

			Iterator<JsonNode> envrootNode = environmentNode.elements();

			while (envrootNode.hasNext()) {

				JsonNode envList = envrootNode.next();
				((ObjectNode) envList).put("successCount",
						Double.valueOf(elementDataWithOutTimeUnit.get("sum(message_count)"))
								- Double.valueOf(elementDataWithOutTimeUnit.get("sum(is_error)")));
				((ObjectNode) envList).put("successDelta",
						(Double.valueOf(elementDataWithOutTimeUnit.get("sum(message_count)"))
								- Double.valueOf(elementDataWithOutTimeUnit.get("sum(is_error)"))) * 100
								/ Double.valueOf(elementDataWithOutTimeUnit.get("sum(message_count)")));
				Iterator<JsonNode> metricsMap = envList.elements();
				while (metricsMap.hasNext()) {
					JsonNode metricsList = metricsMap.next();
					Iterator<JsonNode> metricNodeDetails = metricsList.elements();
					while (metricNodeDetails.hasNext()) {
						JsonNode metricElement = metricNodeDetails.next();
						if (metricElement.get("name").asText().equals("sum(message_count)")) {
							((ObjectNode) metricElement).put("displayName", "Total Traffic");
							((ObjectNode) metricElement).put("globalCount",
									elementDataWithOutTimeUnit.get("sum(message_count)"));
						} else if (metricElement.get("name").asText().equals("tps")) {
							((ObjectNode) metricElement).put("displayName", "Average TPS");
							((ObjectNode) metricElement).put("globalCount", elementDataWithOutTimeUnit.get("tps"));
						} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
							((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							((ObjectNode) metricElement).put("globalCount",
									elementDataWithOutTimeUnit.get("sum(is_error)"));
							double delta = (Double.valueOf(elementDataWithOutTimeUnit.get("sum(is_error)"))
									/ Double.valueOf(elementDataWithOutTimeUnit.get("sum(message_count)"))) * 100;
							((ObjectNode) metricElement).put("delta", delta);
						}
					}
				}
			}

			return performanceTrafficNode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Object averageResponseTime(PerformanceMetrics performanceMetrics, String jsessionid, String interactionid)
			throws ItorixException, JsonProcessingException, IOException {

		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=avg(total_response_time),avg(target_response_time),avg(request_processing_latency),avg(response_processing_latency)"
					+ "&timeRange=" + cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=avg(total_response_time),avg(target_response_time),avg(request_processing_latency),avg(response_processing_latency)"
					+ "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNode = mapper.readTree(apiAverageResponseTime);

		((ObjectNode) (averageResponseNode)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNode.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode metricList = elements.next();
			Iterator<JsonNode> metric = metricList.elements();
			while (metric.hasNext()) {
				JsonNode metricNode = metric.next();

				List<Integer> removableNodes = new ArrayList<>();
				if (metricNode.isArray()) {

					int i = 0;
					// ArrayNode arrayNode=(ArrayNode)metricNode;

					for (JsonNode jsonNode : metricNode) {
						boolean isDelete = true;
						if (jsonNode.get("name").asText().startsWith("sum")) {
							// jsonNode.elements().remove();
							isDelete = false;
							System.out.println("ArrayNode :::::" + i);
							removableNodes.add(i);

							((ObjectNode) jsonNode).removeAll();
						} else if (isDelete && jsonNode.get("name").asText().equals("avg(total_response_time)")) {
							((ObjectNode) jsonNode).put("displayName", "Average Response Time");
						} else if (isDelete
								&& jsonNode.get("name").asText().equals("avg(response_processing_latency)")) {
							((ObjectNode) jsonNode).put("displayName", "Average Response Processing Latency");
						} else if (isDelete && jsonNode.get("name").asText().equals("avg(target_response_time)")) {
							((ObjectNode) jsonNode).put("displayName", "Average Target Response Time");
						} else if (isDelete
								&& jsonNode.get("name").asText().equals("avg(request_processing_latency)")) {
							((ObjectNode) jsonNode).put("displayName", "Average Request Processing Latency");
						} else if (isDelete
								&& jsonNode.get("name").asText().equals("global-avg-target_response_time")) {
							((ObjectNode) jsonNode).put("displayName", "Global Average Target Response Time");
						} else if (isDelete && jsonNode.get("name").asText().equals("global-avg-total_response_time")) {
							((ObjectNode) jsonNode).put("displayName", "Global Average Total Response Time");
						} else if (isDelete
								&& jsonNode.get("name").asText().equals("global-avg-request_processing_latency")) {
							((ObjectNode) jsonNode).put("displayName", "Global Average Request Processing Latency");
						}

						i++;
					}

					Collections.sort(removableNodes, Collections.reverseOrder());

					for (int postion : removableNodes) {
						System.out.println("Position####" + postion);
						((ArrayNode) (metricNode)).remove(postion);
					}
				}
			}
		}

		return averageResponseNode;
	}

	public Object averageResponseTimeAtProxy(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {

		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));

		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=avg(total_response_time),avg(target_response_time),avg(request_processing_latency),avg(response_processing_latency)"
					+ "&timeRange=" + cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=avg(total_response_time),avg(target_response_time),avg(request_processing_latency),avg(response_processing_latency)"
					+ "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {
							if (metricElement.get("name").asText().equals("avg(total_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Response Time");
							} else if (metricElement.get("name").asText().equals("avg(response_processing_latency)")) {
								((ObjectNode) metricElement).put("displayName", "Average Response Processing Latency");
							} else if (metricElement.get("name").asText().equals("avg(target_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Target Response Time");
							} else if (metricElement.get("name").asText().equals("avg(request_processing_latency)")) {
								((ObjectNode) metricElement).put("displayName", "Average Request Processing Latency");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateTrafficAtProxy(PerformanceMetrics performanceMetrics, String jsessionid, String interactionid)
			throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
				+ "/stats/apiproxy?select=sum(message_count),tps,sum(is_error)" + "&timeRange=" + cfg.getTimeRange();

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("tps")) {
								((ObjectNode) metricElement).put("displayName", "Average TPS");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateTrafficByTarget(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("tps")) {
								((ObjectNode) metricElement).put("displayName", "Average TPS");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateTargetErrorComposition(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target_response_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit()
					+ "&filter=(target_response_code ge 400 and target_response_code le 599)";

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target_response_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&filter=(target_response_code ge 400 and target_response_code le 599)";
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("tps")) {
								((ObjectNode) metricElement).put("displayName", "Average TPS");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateResponseTimeComposition(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=avg(target_response_time)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=avg(target_response_time)" + "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("avg(target_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Target Response Time");
							} else if (metricElement.get("name").asText().equals("global-avg-target_response_time")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Target Response Time");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateTargetRequestPayLoadSize(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=avg(request_size)" + "&timeRange=" + cfg.getTimeRange() + "&timeUnit="
					+ cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=avg(request_size)" + "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("avg(request_size)")) {
								((ObjectNode) metricElement).put("displayName", "Average Target Payload Size");
							} else if (metricElement.get("name").asText().equals("global-avg-request_size")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Payload Size");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateLatencyAnalysis(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?_optimized=js&limit=14400&select=percentile(total_response_time,50),percentile(total_response_time,95),percentile(total_response_time,99)"
					+ ",percentile(target_response_time,50),percentile(target_response_time,95),percentile(target_response_time,99),percentile(response_processing_latency,50)"
					+ ",percentile(response_processing_latency,95),percentile(response_processing_latency,99),percentile(request_processing_latency,50),percentile(request_processing_latency,95),"
					+ "percentile(request_processing_latency,99)&sort=ASC&sortby=percentile(total_response_time,50),percentile(total_response_time,95),percentile(total_response_time,99),"
					+ "percentile(target_response_time,50),percentile(target_response_time,95),percentile(target_response_time,99),percentile(response_processing_latency,50)"
					+ ",percentile(response_processing_latency,95),percentile(response_processing_latency,99)"
					+ ",percentile(request_processing_latency,50),percentile(request_processing_latency,95),percentile(request_processing_latency,99)&t=agg_percentile"
					+ "&timeRange=" + cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?_optimized=js&limit=14400&select=percentile(total_response_time,50),percentile(total_response_time,95),percentile(total_response_time,99)"
					+ ",percentile(target_response_time,50),percentile(target_response_time,95),percentile(target_response_time,99),percentile(response_processing_latency,50)"
					+ ",percentile(response_processing_latency,95),percentile(response_processing_latency,99),percentile(request_processing_latency,50),percentile(request_processing_latency,95),"
					+ "percentile(request_processing_latency,99)&sort=ASC&sortby=percentile(total_response_time,50),percentile(total_response_time,95),percentile(total_response_time,99),"
					+ "percentile(target_response_time,50),percentile(target_response_time,95),percentile(target_response_time,99),percentile(response_processing_latency,50)"
					+ ",percentile(response_processing_latency,95),percentile(response_processing_latency,99)"
					+ ",percentile(request_processing_latency,50),percentile(request_processing_latency,95),percentile(request_processing_latency,99)&t=agg_percentile"
					+ "&timeRange=" + cfg.getTimeRange();
		}

		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);

		JsonNode responseNode = averageResponseNodeAtProxy.path("Response");
		((ObjectNode) (responseNode)).remove("metaData");

		return averageResponseNodeAtProxy;
	}

	public Object totalTrafficVsProxyErrorVsTargetError(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=sum(message_count),sum(is_error),sum(target_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();
		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/?select=sum(message_count),sum(is_error),sum(target_error)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();

		while (elements.hasNext()) {
			JsonNode metricList = elements.next();
			Iterator<JsonNode> metric = metricList.elements();
			while (metric.hasNext()) {
				JsonNode metricNode = metric.next();

				if (metricNode.isArray()) {

					for (JsonNode jsonNode : metricNode) {

						if (jsonNode.get("name").asText().equals("sum(message_count)")) {
							((ObjectNode) jsonNode).put("displayName", "Total Traffic");

						} else if (jsonNode.get("name").asText().equals("sum(target_error)")) {
							((ObjectNode) jsonNode).put("displayName", "Sum of Target Errors");

						} else if (jsonNode.get("name").asText().equals("sum(is_error)")) {
							((ObjectNode) jsonNode).put("displayName", "Traffic Errors");
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object proxyErrorVsResponseCode(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		// Apigee apigee =
		// identityManagementDao.getApigeeCredential(cfg.getJsessionId());
		// if (apigee == null) {
		// throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1007"),
		// "Apigee-1007");
		// }
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/response_status_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit() + "&filter=(response_status_code gt 399)";

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/response_status_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&filter=(response_status_code gt 399)";
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object TargetErrorVsResponseCode(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target_response_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit() + "&filter=(target_response_code gt 399)";

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target_response_code?select=sum(message_count)" + "&timeRange=" + cfg.getTimeRange()
					+ "&filter=(target_response_code gt 399)";
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateErrorVsProxyName(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=sum(message_count),sum(is_error),sum(target_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=sum(message_count),sum(is_error),sum(target_error)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateErrorVsTargetName(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=sum(message_count),sum(target_error)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/target?select=sum(message_count),sum(target_error)" + "&timeRange=" + cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Traffic Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evaluateTrafficCountVsErrorCodes(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_email?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_email?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Proxy Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalAvgTotalResponseTimeVsAvgRequestSize(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_email?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_email?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("avg(request_size)")) {
								((ObjectNode) metricElement).put("displayName", "Average Request Size");

							} else if (metricElement.get("name").asText().equals("avg(total_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Total Response Time");

							} else if (metricElement.get("name").asText().equals("global-avg-request_size")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Request Size");

							} else if (metricElement.get("name").asText().equals("global-avg-total_response_time")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Total Response Time");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalTotalTrafficSucessVsErrorCountByApp(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {
			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {
			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Proxy Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalAvgTotalResponseTimeVsAvgRequestSizeByApp(PerformanceMetrics performanceMetrics,
			String jsessionid, String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {
			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {
			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("avg(request_size)")) {
								((ObjectNode) metricElement).put("displayName", "Average Request Size");

							} else if (metricElement.get("name").asText().equals("avg(total_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Total Response Time");

							} else if (metricElement.get("name").asText().equals("global-avg-request_size")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Request Size");

							} else if (metricElement.get("name").asText().equals("global-avg-total_response_time")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Total Response Time");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalTotalSucessVsErrorAtProxyLevel(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		cfg.setAppName(performanceMetrics.getAppName());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit() + "&filter=(developer_app eq '" + cfg.getAppName() + "')";

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/apiproxy?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange()
					+ "&filter=(developer_app eq '" + cfg.getAppName() + "')";
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Proxy Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalTotalSucessVsErrorAtProduct(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		cfg.setAppName(performanceMetrics.getAppName());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/api_product?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange()
					+ "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/api_product?select=sum(message_count),sum(is_error)" + "&timeRange=" + cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Proxy Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalTotalSucessVsErrorAtProductVsApp(PerformanceMetrics performanceMetrics, String jsessionid,
			String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		cfg.setAppName(performanceMetrics.getAppName());
		cfg.setApiProductName(performanceMetrics.getProductName());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);

		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit() + "&filter=(api_product eq '"
					+ cfg.getApiProductName() + "')";

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/developer_app?select=sum(message_count),sum(is_error)" + "&timeRange="
					+ cfg.getTimeRange() + "&filter=(api_product eq '" + cfg.getApiProductName() + "')";
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("sum(message_count)")) {
								((ObjectNode) metricElement).put("displayName", "Total Traffic");

							} else if (metricElement.get("name").asText().equals("sum(target_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Target Errors");

							} else if (metricElement.get("name").asText().equals("sum(is_error)")) {
								((ObjectNode) metricElement).put("displayName", "Sum of Proxy Errors");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object evalAvgTotalResponseTimeVsAvgRequestSizeByProduct(PerformanceMetrics performanceMetrics,
			String jsessionid, String interactionid) throws ItorixException, JsonProcessingException, IOException {
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(performanceMetrics.getOrgName());
		cfg.setEnvironment(performanceMetrics.getEnvironment());
		if (performanceMetrics.getTimeRange() != null) {
			cfg.setTimeRange(performanceMetrics.getTimeRange().replaceAll("%20", " "));
		}
		cfg.setTimeUnit(performanceMetrics.getTimeUnit());
		cfg.setJsessionId(jsessionid);
		if (performanceMetrics.getType() != null) {
			cfg.setType(performanceMetrics.getType());
		} else {
			cfg.setType("saas");
		}
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		String suffix = null;
		if (cfg.getTimeUnit() != null && StringUtils.isNotBlank(cfg.getTimeUnit())) {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/api_product?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange() + "&timeUnit=" + cfg.getTimeUnit();

		} else {

			suffix = "v1/organizations/" + cfg.getOrganization() + "/environments/" + cfg.getEnvironment()
					+ "/stats/api_product?select=avg(total_response_time),avg(request_size)" + "&timeRange="
					+ cfg.getTimeRange();
		}
		String apiAverageResponseTime = apigeeUtil.getMetricsData(cfg, suffix);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode averageResponseNodeAtProxy = mapper.readTree(apiAverageResponseTime);
		((ObjectNode) (averageResponseNodeAtProxy)).remove("metaData");

		JsonNode enviromentsNode = averageResponseNodeAtProxy.path("environments");

		Iterator<JsonNode> elements = enviromentsNode.elements();
		while (elements.hasNext()) {
			JsonNode dimesnsionRootNode = elements.next();

			JsonNode mectricElements = dimesnsionRootNode.path("dimensions");

			Iterator<JsonNode> metricElementsList = mectricElements.elements();

			while (metricElementsList.hasNext()) {
				JsonNode metricDetails = metricElementsList.next();
				Iterator<JsonNode> metricNodeDetails = metricDetails.elements();
				while (metricNodeDetails.hasNext()) {
					JsonNode metricElements = metricNodeDetails.next();
					if (metricElements.isArray()) {
						for (JsonNode metricElement : metricElements) {

							if (metricElement.get("name").asText().equals("avg(request_size)")) {
								((ObjectNode) metricElement).put("displayName", "Average Request Size");

							} else if (metricElement.get("name").asText().equals("avg(total_response_time)")) {
								((ObjectNode) metricElement).put("displayName", "Average Total Response Time");

							} else if (metricElement.get("name").asText().equals("global-avg-request_size")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Request Size");

							} else if (metricElement.get("name").asText().equals("global-avg-total_response_time")) {
								((ObjectNode) metricElement).put("displayName", "Global Average Total Response Time");
							}
						}
					}
				}
			}
		}

		return averageResponseNodeAtProxy;
	}

	public Object getOverview(String organization, String environment, String interactionid, String type)
			throws ItorixException, InterruptedException, ExecutionException {

		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(organization);
		cfg.setEnvironment(environment);
		if (type != null) {
			cfg.setType(type);
		} else {
			cfg.setType("saas");
		}

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date todayDate = new Date();
		Date dateBefore31Days = DateUtils.addDays(new Date(), -31);

		cfg.setTimeRangestartDate(df.format(dateBefore31Days));
		cfg.setTimeRangeendDate(df.format(todayDate));

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(organization, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		DashBoardOverview dbOverViewResponse = findOverViewResponse(organization, environment, type);

		if (dbOverViewResponse == null) {
			Map<String, ApigeeResponse> responseList = apigeeUtil.getOverview(cfg);
			JSONObject objList = postprocess(responseList, cfg);
			DashBoardOverview dashBoardOverview = new DashBoardOverview();
			dashBoardOverview.setDashBoradFunctionName(DashBoardOverview.FUNCTION_NAME);
			dashBoardOverview.setEnvironment(environment);
			dashBoardOverview.setOrganisation(organization);
			dashBoardOverview.setOverviewResponse(objList);
			mongoTemplate.save(dashBoardOverview);
			return objList;
		} else {
			return dbOverViewResponse.getOverviewResponse();
		}
	}

	public DashBoardSetUp findDashBoardSetUpDetails() {
		Query query = new Query(
				Criteria.where(DashBoardSetUp.DASH_BOARD_FUNCTION_NAME).is(DashBoardSetUp.DASH_BOARD_SETUP));
		DashBoardSetUp dashBoardSetUp = mongoTemplate.findOne(query, DashBoardSetUp.class);
		return dashBoardSetUp;
	}

	public DashBoardOverview findOverViewResponse(String org, String env, String type) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(DashBoardOverview.LABEL_DASH_BOARD_ORGANISATION).is(org),
						Criteria.where(DashBoardOverview.LABEL_DASH_BOARD_ENVIROMENT).is(env),
						Criteria.where(DashBoardOverview.LABEL_DASH_BOARD_TYPE).is(type)));
		DashBoardOverview dashBoardDBOverview = mongoTemplate.findOne(query, DashBoardOverview.class);
		return dashBoardDBOverview;
	}

	public JSONObject postprocess(Map<String, ApigeeResponse> responseList, CommonConfiguration cfg)
			throws ItorixException {
		List returnList = new ArrayList();
		JSONObject levelList = new JSONObject();
		JSONObject levelsList = new JSONObject();
		JSONObject timeSeriesList = new JSONObject();
		JSONArray arr = new JSONArray();

		HashMap<String, String> deltaCalcuation = new HashMap<String, String>();
		deltaCalcuation.put("totalTrafficCount", "N");
		deltaCalcuation.put("totalErrorRate", "Y");
		deltaCalcuation.put("topAPITrafficCount", "Y");
		deltaCalcuation.put("topAPIWithErrorCount", "Y");
		deltaCalcuation.put("minPerformingAPIWithResponseTime", "N");
		deltaCalcuation.put("maxPerformingAPIWithResponseTime", "N");
		deltaCalcuation.put("topAppWithTraffic", "N");
		deltaCalcuation.put("topAPIProductWithTraffic", "N");
		deltaCalcuation.put("geoMetrics", "N");
		deltaCalcuation.put("topFiveAPI", "N");
		deltaCalcuation.put("topFiveAPP", "N");
		deltaCalcuation.put("topFiveAPIProducts", "N");
		deltaCalcuation.put("topFiveDevelopers", "N");

		Double delta;
		ApigeeResponse totalTrafficapigeeResponse = responseList.get("totalTrafficCount");
		double totalTrafficcount = Double.parseDouble(
				totalTrafficapigeeResponse.getEnvironments()[0].getMetrics()[0].getValues()[0].replace(".0", ""));
		ApigeeResponse totalErrorRateapigeeResponse = responseList.get("totalErrorRate");
		Level sucesslevel = calculateSuccessRate(totalErrorRateapigeeResponse, totalTrafficcount);
		returnList.add(sucesslevel);

		for (Map.Entry<String, ApigeeResponse> apigeeresponse : responseList.entrySet()) {
			for (Environments apigeeEnvironments : apigeeresponse.getValue().getEnvironments()) {
				Level level = new Level();
				Values[] valuesnew;
				if (apigeeEnvironments.getMetrics() != null) {
					valuesnew = new Values[apigeeEnvironments.getMetrics().length];
				} else {
					valuesnew = new Values[apigeeEnvironments.getDimensions()[0].getMetrics().length];
				}
				if (apigeeEnvironments.getMetrics() != null) {
					level.setLevelname(apigeeresponse.getKey());
					ArrayList<Values> valuesArrayList = new ArrayList<Values>();
					for (Metrics metric : apigeeEnvironments.getMetrics()) {
						Values valuenew = new Values();
						valuenew.setValue(metric.getValues()[0]);
						valuenew.setName(apigeeEnvironments.getName());
						if (deltaCalcuation.get(apigeeresponse.getKey()).equalsIgnoreCase("Y")) {
							delta = (double) ((Double.parseDouble(metric.getValues()[0].replace(".0", "")) * 100)
									/ totalTrafficcount);
							valuenew.setDelta(String.format("%.2f", delta) + "%");
						} else {
							valuenew.setDelta("N/A");
						}

						valuesArrayList.add(valuenew);
					}
					valuesnew = valuesArrayList.toArray(new Values[]{});
				} else if (apigeeEnvironments.getDimensions() != null) {
					level.setLevelname(apigeeresponse.getKey());
					ArrayList<Values> valuesArrayList = new ArrayList<Values>();
					for (Dimensions dimension : apigeeEnvironments.getDimensions()) {
						for (Metrics metric : dimension.getMetrics()) {
							Values valuenew = new Values();
							valuenew.setValue(metric.getValues()[0]);
							valuenew.setName(dimension.getName());
							if (apigeeresponse.getKey().equalsIgnoreCase("topFiveDevelopers")
									&& !dimension.getName().contains("not set")) {
								DeveloperDetails developerDetails = (DeveloperDetails) apigeeUtil
										.getDeveloperName(dimension.getName(), cfg);
								valuenew.setName(
										developerDetails.getFirstName() + " " + developerDetails.getLastName());
							}
							if (deltaCalcuation.get(apigeeresponse.getKey()).equalsIgnoreCase("Y")) {
								delta = (double) ((Double.parseDouble(metric.getValues()[0].replace(".0", "")) * 100)
										/ totalTrafficcount);
								valuenew.setDelta(String.format("%.2f", delta) + "%");
							} else {
								valuenew.setDelta("N/A");
							}

							valuesArrayList.add(valuenew);
						}
					}
					valuesnew = valuesArrayList.toArray(new Values[]{});
				}
				level.setValues(valuesnew);
				returnList.add(level);
			}
		}

		levelList.put("level", returnList);
		levelsList.put("levels", levelList);

		return levelsList;
	}

	private Level calculateSuccessRate(ApigeeResponse totalErrorRateapigeeResponse, double totalTrafficcount) {
		Double delta;
		double totalErrorRatecount = Double.parseDouble(
				totalErrorRateapigeeResponse.getEnvironments()[0].getMetrics()[0].getValues()[0].replace(".0", ""));
		Double totalSuccessRateCount = totalTrafficcount - totalErrorRatecount;
		System.out.println("Sucess Rate totalSuccessRateCount " + totalSuccessRateCount);
		delta = (double) (((totalTrafficcount - totalErrorRatecount) * 100) / totalTrafficcount);
		System.out.println("Sucess Rate " + delta);
		Level sucesslevel = new Level();
		Values[] sucessvalues = new Values[1];
		Values sucessvalue = new Values();
		sucesslevel.setLevelname("totalSuccessRate");
		sucessvalue.setName(totalErrorRateapigeeResponse.getEnvironments()[0].getName());
		sucessvalue.setValue(totalSuccessRateCount.toString());
		sucessvalue.setDelta(String.format("%.2f", delta) + "%");
		sucessvalues[0] = sucessvalue;
		sucesslevel.setValues(sucessvalues);
		return sucesslevel;
	}

	public Object getTimeSeriesData(String org, String env, String interactionid, String type)
			throws ItorixException, InterruptedException, ExecutionException {

		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();

		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setOrganization(org);
		cfg.setEnvironment(env);

		if (type != null) {
			cfg.setType(type);
		} else {
			cfg.setType("saas");
		}
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date todayDate = new Date();
		Date dateBefore31Days = DateUtils.addDays(new Date(), -31);

		cfg.setTimeRangestartDate(df.format(dateBefore31Days));
		cfg.setTimeRangeendDate(df.format(todayDate));

		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org, cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		DashBoardTimeSeries dbResponse = findTimeSeriesResponse(org, env, type);
		if (dbResponse == null) {
			JSONObject responseList = (JSONObject) apigeeUtil.getTimeSeriesData(cfg);
			dashBoardTimeSeries.setEnvironment(env);
			dashBoardTimeSeries.setOrganisation(org);
			dashBoardTimeSeries.setDashBoradFunctionName(DashBoardTimeSeries.FUNCTION_NAME);
			dashBoardTimeSeries.setTimeSeriesResponse(responseList.get("environments"));
			mongoTemplate.save(dashBoardTimeSeries);
			return responseList.get("environments");
		} else {
			return dbResponse.getTimeSeriesResponse();
		}
	}

	public DashBoardTimeSeries findTimeSeriesResponse(String org, String env, String type) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(DashBoardTimeSeries.LABEL_DASH_BOARD_ORGANISATION).is(org),
						Criteria.where(DashBoardTimeSeries.LABEL_DASH_BOARD_ENVIROMENT).is(env),
						Criteria.where(DashBoardOverview.LABEL_DASH_BOARD_TYPE).is(type)));
		DashBoardTimeSeries dashBoardDBTimeSeries = mongoTemplate.findOne(query, DashBoardTimeSeries.class);
		return dashBoardDBTimeSeries;
	}

	public Object dashBoardSet(Apigee apigee, String interactionid, String jsessionid) throws Exception {

		Set<DashBoardOrganisations> organisations;
		CommonConfiguration cfg = new CommonConfiguration();
		DashBoardSetUp dashBoardSetUp = new DashBoardSetUp();
		DashBoardEnvironments dashBoardEnvironments = new DashBoardEnvironments();
		dashBoardSetUp.setApigee(apigee);
		dashBoardSetUp.setDashBoardSetUpDetails(DashBoardSetUp.DASH_BOARD_SETUP);

		cfg.setApigeeEmail(applicationProperties.getApigeeServiceUsername());
		cfg.setApigeePassword(applicationProperties.getApigeeServicePassword());

		mongoTemplate.dropCollection(DashBoardEnvironments.class);
		if (apigee.getOrganizations() != null) {
			organisations = apigee.getOrganizations();

			for (DashBoardOrganisations org : organisations) {
				ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org.getName(), org.getType());
				cfg.setApigeeEmail(apigeeServiceUser.getUserName());
				cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
				cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
				cfg.setOrganization(org.getName());
				dashBoardEnvironments.setOrg(org.getName());
				dashBoardEnvironments.setEnvironments(org.getEnvironments());
				mongoTemplate.save(dashBoardEnvironments);
			}
		}

		mongoTemplate.dropCollection(DashBoardSetUp.class);
		mongoTemplate.save(dashBoardSetUp);
		return "";
	}

	public Object getDashBoardSetUpDetails(String interactionid, String jsessionid) {
		JSONObject apigeeDetails = new JSONObject();
		JSONObject apigeeResponse = new JSONObject();
		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();
		apigeeDetails.put("username", dbDashBoardSetUp.getApigee().getUserName());
		apigeeDetails.put("password", dbDashBoardSetUp.getApigee().getPassword());
		apigeeDetails.put("organisations", dbDashBoardSetUp.getApigee().getOrganizations());
		apigeeResponse.put("apigee", apigeeDetails);
		return apigeeResponse;
	}

	public Object updateDashBoardDetails(Apigee apigee, String jsessionid, String interactionid)
			throws ItorixException {

		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();
		Set<DashBoardOrganisations> organisations;
		Map<String, DashBoardEnvironments> orgAndEnv = new HashMap<String, DashBoardEnvironments>();
		CommonConfiguration cfg = new CommonConfiguration();

		cfg.setApigeeEmail(applicationProperties.getApigeeServiceUsername());
		cfg.setApigeePassword(applicationProperties.getApigeeServicePassword());

		dbDashBoardSetUp.setApigee(apigee);
		if (apigee.getOrganizations() != null) {
			organisations = apigee.getOrganizations();

			for (DashBoardOrganisations org : organisations) {
				ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org.getName(), org.getType());
				cfg.setApigeeEmail(apigeeServiceUser.getUserName());
				cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
				cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
				DashBoardEnvironments dashBoardEnvironments = new DashBoardEnvironments();
				// List<String> envList = null;
				cfg.setOrganization(org.getName());
				// envList = apigeeUtil.getEnvironmentNames(cfg);
				dashBoardEnvironments.setOrg(org.getName());
				dashBoardEnvironments.setEnvironments(org.getEnvironments());
				orgAndEnv.put(org.getName(), dashBoardEnvironments);
			}
		}
		mongoTemplate.dropCollection(DashBoardEnvironments.class);
		for (Map.Entry<String, DashBoardEnvironments> org : orgAndEnv.entrySet()) {
			mongoTemplate.save(org.getValue());
		}
		mongoTemplate.save(dbDashBoardSetUp);
		return "";
	}

	public Object getOrganisationsList(String interactionid, String jsessionid) throws ItorixException {

		JSONObject organisationsList = new JSONObject();
		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();
		if (dbDashBoardSetUp != null) {
			organisationsList.put("organisations", dbDashBoardSetUp.getApigee().getOrganizations());
		} else {
			throw new ItorixException(new Throwable().getMessage(), "PERFORMANCE_MONITORING_SETUP", new Throwable());
		}
		return organisationsList;
	}

	public Object getEnvironmentsForOrganisations(String org, String interactionid, String jsessionid)
			throws ItorixException {
		JSONObject environmentsList = new JSONObject();
		DashBoardEnvironments dbDashBoardEnvironments = findEnvironmentsList(org);
		if (dbDashBoardEnvironments != null) {
			environmentsList.put("Environments", dbDashBoardEnvironments.getEnvironments());
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1005", new Throwable());
		}
		return environmentsList;
	}

	public DashBoardEnvironments findEnvironmentsList(String org) {
		Query query = new Query((Criteria.where(DashBoardEnvironments.LABEL_DASH_BOARD_ENV_ORGANISATION).is(org)));
		DashBoardEnvironments dashBoardDBOverview = mongoTemplate.findOne(query, DashBoardEnvironments.class);
		return dashBoardDBOverview;
	}

	public Object refreshEnvironments(String jsessionid, String interactionid) throws ItorixException {
		DashBoardSetUp dbDashBoardSetUp = findDashBoardSetUpDetails();
		Map<String, DashBoardEnvironments> orgAndEnv = new HashMap<String, DashBoardEnvironments>();
		Set<DashBoardOrganisations> organisations;
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setApigeeEmail(applicationProperties.getApigeeServiceUsername());
		cfg.setApigeePassword(applicationProperties.getApigeeServicePassword());
		if (dbDashBoardSetUp.getApigee().getOrganizations() != null) {
			organisations = dbDashBoardSetUp.getApigee().getOrganizations();
			for (DashBoardOrganisations org : organisations) {
				ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org.getName(), org.getType());
				cfg.setApigeeEmail(apigeeServiceUser.getUserName());
				cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
				cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
				DashBoardEnvironments dashBoardEnvironments = new DashBoardEnvironments();
				List<String> envList = null;
				cfg.setOrganization(org.getName());
				envList = apigeeUtil.getEnvironmentNames(cfg);
				dashBoardEnvironments.setOrg(org.getName());
				dashBoardEnvironments.setEnvironments(envList);
				orgAndEnv.put(org.getName(), dashBoardEnvironments);
			}
			mongoTemplate.dropCollection(DashBoardEnvironments.class);
			for (Map.Entry<String, DashBoardEnvironments> org : orgAndEnv.entrySet()) {
				mongoTemplate.save(org.getValue());
			}
		}
		return "";
	}
}
