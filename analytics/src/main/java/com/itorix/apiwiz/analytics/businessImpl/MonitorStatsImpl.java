package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.beans.monitor.MonitorCollections;
import com.itorix.apiwiz.analytics.beans.monitor.MonitorCollectionsResponse;
import com.itorix.apiwiz.analytics.model.MonitorExecutionMetric;
import com.itorix.apiwiz.analytics.model.MonitorStats;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Component
public class MonitorStatsImpl {

	private static final String MONITOR_EVENTS_HISTORY = "Monitor.Collections.Events.History";

	private static final String MONITOR_COLLECTIONS = "Monitor.Collections.List";

	@Autowired
	private MongoTemplate mongoTemplate;

	public MonitorStats createMonitorStats(String userId) {
		MonitorStats monitorStats = new MonitorStats();
		List<MonitorCollectionsResponse> monitorResponses = getMonitorResponses();

		if (userId != null) {
			monitorResponses = monitorResponses.stream().filter(m -> m.getCreatedBy().equals(userId))
					.collect(Collectors.toList());
		}

		monitorStats.setTopFiveMonitorsBasedOnUptime(getTopFiveMonitorsBasedOnUptime(monitorResponses));
		monitorStats.setTopFiveMonitorsBasedOnLatency(getTopFiveMonitorsBasedOnLatency(monitorResponses));
		List<MonitorExecutionMetric> monitorCountByExecStatus = getMonitorCountByExecStatus(userId);
		replaceMonitorCollectionIdWithName(monitorCountByExecStatus);
		monitorStats.setMonitorCountByExecStatus(monitorCountByExecStatus);
		return monitorStats;
	}

	private void replaceMonitorCollectionIdWithName(List<MonitorExecutionMetric> monitorExecCountByStatuses) {
		HashMap<String, String> monitorIdAndName = new HashMap<>();
		List<MonitorCollections> monitorCollections = mongoTemplate.findAll(MonitorCollections.class,
				MONITOR_COLLECTIONS);
		monitorCollections.stream().forEach(m -> monitorIdAndName.put(m.getId(), m.getName()));
		monitorExecCountByStatuses
				.forEach(m -> m.setMonitorCollectionName(monitorIdAndName.get(m.getMonitorCollectionName())));
	}

	private List<MonitorExecutionMetric> getMonitorCountByExecStatus(String userId) {
		List<MonitorExecutionMetric> monitorExecCountByStatuses = new ArrayList<>();
		Aggregation aggregation = null;

		Cond successCondition = ConditionalOperators.when(Criteria.where("status").is("Success")).then(1).otherwise(0);
		Cond failureCondition = ConditionalOperators.when(Criteria.where("status").is("Failed")).then(1).otherwise(0);

		GroupOperation groupOperation = Aggregation.group("collectionId").count().as("total").sum(successCondition)
				.as("success").sum(failureCondition).as("failure").avg("latency").as("latency");

		if (userId != null) {
			aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("collectionCreatedBy").is(userId)), groupOperation);
		} else {
			aggregation = Aggregation.newAggregation(groupOperation);
		}
		List<Document> mappedResults = mongoTemplate.aggregate(aggregation, MONITOR_EVENTS_HISTORY, Document.class)
				.getMappedResults();
		mappedResults.forEach(d -> {
			MonitorExecutionMetric monitorExecCountByStatus = getMonitorExecCountStatus(d);
			monitorExecCountByStatuses.add(monitorExecCountByStatus);
		});
		return monitorExecCountByStatuses;
	}

	private MonitorExecutionMetric getMonitorExecCountStatus(Document d) {
		MonitorExecutionMetric monitorExecutionMetric = new MonitorExecutionMetric();
		monitorExecutionMetric.setMonitorCollectionName(d.getString("_id"));
		monitorExecutionMetric.setTotalExecutionCount(d.getInteger("total"));
		monitorExecutionMetric.setSuccessFullExecutionCount(d.getInteger("success"));
		monitorExecutionMetric.setFailedExecutionCount(d.getInteger("failure"));
		monitorExecutionMetric.setAvgLatency(d.getDouble("latency"));
		return monitorExecutionMetric;
	}

	private Map<String, Long> getTopFiveMonitorsBasedOnLatency(List<MonitorCollectionsResponse> monitorResponses) {
		Map<String, Long> topFiveMonitorBasedOnUptime = new LinkedHashMap<>();
		monitorResponses.stream().filter(r -> r.getLatency() > 0)
				.sorted(Comparator.comparingLong(MonitorCollectionsResponse::getLatency)).limit(5)
				.forEach(r -> topFiveMonitorBasedOnUptime.put(r.getName(), r.getLatency()));
		return topFiveMonitorBasedOnUptime;
	}

	private Map<String, Integer> getTopFiveMonitorsBasedOnUptime(List<MonitorCollectionsResponse> responses) {
		Map<String, Integer> topFiveMonitorBasedOnUptime = new LinkedHashMap<>();
		responses.stream().filter(r -> r.getUptime() > 0)
				.sorted(Comparator.comparingLong(MonitorCollectionsResponse::getUptime).reversed()).limit(5)
				.forEach(r -> topFiveMonitorBasedOnUptime.put(r.getName(), r.getUptime()));
		return topFiveMonitorBasedOnUptime;
	}

	private List<MonitorCollectionsResponse> getMonitorResponses() {
		Query query = new Query();

		query.fields().include("id").include("name").include("summary").include("cts").include("createdBy")
				.include("modifiedBy").include("mts").include("monitorRequest.id").include("monitorRequest.name");

		List<MonitorCollections> monitorCollections = mongoTemplate.find(query, MonitorCollections.class,
				MONITOR_COLLECTIONS);

		List<String> collectionIds = monitorCollections.stream().map(s -> s.getId()).collect(Collectors.toList());

		Aggregation aggForLatency = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
				Aggregation.group("collectionId").avg("$latency").as("latency"));

		List<Document> latency = mongoTemplate.aggregate(aggForLatency, MONITOR_EVENTS_HISTORY, Document.class)
				.getMappedResults();

		Aggregation aggForCount = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> countDoc = mongoTemplate.aggregate(aggForCount, MONITOR_EVENTS_HISTORY, Document.class)
				.getMappedResults();

		Aggregation aggForSuccess = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("collectionId").in(collectionIds).and("status").is("Success")),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, MONITOR_EVENTS_HISTORY, Document.class)
				.getMappedResults();

		List<MonitorCollectionsResponse> monitorResponses = new ArrayList<>();
		log.debug("Getting details for monitor collections");
		if (!CollectionUtils.isEmpty(monitorCollections)) {

			for (MonitorCollections monitor : monitorCollections) {
				MonitorCollectionsResponse collectionResponse = new MonitorCollectionsResponse();
				int uptime = 0;
				long latencyInt = 0l;
				int count = 0;
				int success = 0;

				Optional<Document> latencyDoc = latency.stream().filter(f -> f.getString("_id").equals(monitor.getId()))
						.findFirst();
				if (latencyDoc.isPresent()) {
					latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
				}

				Optional<Document> countOptional = countDoc.stream()
						.filter(f -> f.getString("_id").equals(monitor.getId())).findFirst();
				if (countOptional.isPresent()) {
					count = countOptional.get().getInteger("count");
				}

				Optional<Document> successOptional = successDoc.stream()
						.filter(f -> f.getString("_id").equals(monitor.getId())).findFirst();
				if (successOptional.isPresent()) {
					success = successOptional.get().getInteger("count");
				}

				if (success != 0 || count != 0) {
					uptime = Math.round(((float) success / count) * 100);
				}
				collectionResponse.setUptime(uptime);
				collectionResponse.setLatency(latencyInt);
				collectionResponse.setModifiedBy(monitor.getModifiedBy());
				collectionResponse.setMts(monitor.getMts());
				collectionResponse.setName(monitor.getName());
				collectionResponse.setSummary(monitor.getSummary());
				collectionResponse.setCreatedBy(monitor.getCreatedBy());
				monitorResponses.add(collectionResponse);
			}
		}

		return monitorResponses;
	}
}