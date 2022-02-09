package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.beans.monitor.MonitorCollectionsResponse;
import com.itorix.apiwiz.analytics.model.MonitorExecCountByStatus;
import com.itorix.apiwiz.analytics.model.MonitorStats;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MonitorStatsImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorStatsImpl.class);

    private static final String MONITOR_COLLECTIONS = "Monitor.Collections.List";
    private static final String MONITOR_EVENTS_HISTORY = "Monitor.Collections.Events.History";

    @Autowired
    private MongoTemplate mongoTemplate;

    public MonitorStats createMonitorStats() {
        MonitorStats monitorStats = new MonitorStats();
        try {
            Map<String, String> monitorNameAndId = getMonitorNameAndId();
            monitorStats.setTopFiveMonitorsBasedOnUptime(getTopFiveMonitorsBasedOnUptime(monitorNameAndId));
            monitorStats.setTopFiveMonitorsBasedOnLatency(getTopFiveMonitorsBasedOnLatency(monitorNameAndId));
            List<MonitorExecCountByStatus> monitorExecCountByStatuses = getMonitorExecCountByStatuses(monitorNameAndId);
            monitorStats.setMonitorExecCountByStatuses(monitorExecCountByStatuses);
        } catch (Exception ex) {
            LOGGER.error("Error while calculation Monitoring Stats", ex);
        }
        return monitorStats;
    }

    private Map<String, Long> getTopFiveMonitorsBasedOnLatency(Map<String, String> monitorNameAndId) {
        LOGGER.debug("getTopFiveMonitorsBasedOnUptime started");
        Map<String, Long> topFiveMonitorsBasedOnLatency = new LinkedHashMap<>();
        GroupOperation groupOperation = Aggregation.group("collectionId").avg("$latency").as("latency");
        AggregationOperation sort = Aggregation.sort(Sort.Direction.ASC, "latency");
        AggregationOperation limit = Aggregation.limit(5);
        mongoTemplate.aggregate(Aggregation.newAggregation(groupOperation, sort, limit), MONITOR_EVENTS_HISTORY, Document.class)
                .getMappedResults().forEach( d -> {
                 if( null != monitorNameAndId.get(d.getString("_id"))) {
                     topFiveMonitorsBasedOnLatency.put(monitorNameAndId.get(d.getString("_id")), d.getDouble("latency").longValue());
                 }
                });
        LOGGER.debug("getTopFiveMonitorsBasedOnUptime completed");
         return topFiveMonitorsBasedOnLatency;
    }

    private Map<String, String> getMonitorNameAndId() {
        Query query = new Query();
        query.fields().include("_id").include("name");
        return mongoTemplate.find(query, Document.class, MONITOR_COLLECTIONS).stream().collect(Collectors.toMap(
                d -> d.getObjectId("_id").toString(), d -> d.getString("name")));
    }


    private List<MonitorExecCountByStatus> getMonitorExecCountByStatuses(Map<String, String> monitorNameAndId) {
        LOGGER.debug("getMonitorExecCountByStatuses started");
        List<MonitorExecCountByStatus> monitorExecCountByStatuses = new ArrayList<>();
        GroupOperation groupOperation = Aggregation.group("collectionId", "status").count().as("count");
        Aggregation aggregation = Aggregation.newAggregation(groupOperation);
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, MONITOR_EVENTS_HISTORY, Document.class).getMappedResults();
        mappedResults.parallelStream().forEach( d ->  {
            String monitorName = monitorNameAndId.get(d.getString("collectionId"));
            if(null != monitorName) {
                MonitorExecCountByStatus monitorExecCountByStatus = new MonitorExecCountByStatus();
                monitorExecCountByStatus.setMonitorCollectionName(monitorName);
                monitorExecCountByStatus.setStatus(d.getString("status"));
                monitorExecCountByStatus.setCount(d.getInteger("count"));
                monitorExecCountByStatuses.add(monitorExecCountByStatus);
            }
        });
        LOGGER.debug("getMonitorExecCountByStatuses completed");
        return monitorExecCountByStatuses;
    }


    private Map<String, Integer> getTopFiveMonitorsBasedOnUptime(Map<String, String> monitorNameAndId) {
        LOGGER.debug("getTopFiveMonitorsBasedOnUptime started");
        Map<String, Integer> topFiveMonitorBasedOnUptime = new LinkedHashMap<>();
        getMonitorResponses().stream().filter( r -> r.getUptime() > 0).
                sorted(Comparator.comparingLong(MonitorCollectionsResponse::getUptime).reversed()).
                limit(5).forEach( r -> topFiveMonitorBasedOnUptime.put(monitorNameAndId.get(r.getId()), r.getUptime()));
        LOGGER.debug("getTopFiveMonitorsBasedOnUptime completed");
        return topFiveMonitorBasedOnUptime;
    }

    private List<MonitorCollectionsResponse> getMonitorResponses() {
        LOGGER.debug("getMonitorResponses started");
        Query query = new Query();

        query.fields().include("id").include("name").include("summary").include("cts").include("createdBy")
                .include("modifiedBy").include("mts").include("schedulers").include("monitorRequest.id")
                .include("monitorRequest.name");

        List<Document> monitorCollections = mongoTemplate.find(query, Document.class, MONITOR_COLLECTIONS);

        List<String> collectionIds = monitorCollections.stream().map(d -> d.getObjectId("_id").toString()).collect(Collectors.toList());

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



        successDoc.stream().forEach( d -> {
            MonitorCollectionsResponse collectionsResponse = new MonitorCollectionsResponse();
            String collectionId = d.getString("_id");
            Integer successCount = d.getInteger("count");
            if(successCount != 0 ) {
                Optional<Document> totalCount = countDoc.stream()
                       .filter(f -> f.getString("_id").equals(collectionId)).findFirst();
                if(totalCount.isPresent()) {
                    Integer count = totalCount.get().getInteger("count");
                    int uptime = Math.round(((float) successCount / count) * 100);
                    collectionsResponse.setUptime(uptime);
                    collectionsResponse.setId(collectionId);
                    monitorResponses.add(collectionsResponse);
                }
            }
        });


        LOGGER.debug("getMonitorResponses completed");

        return monitorResponses;
    }
}
