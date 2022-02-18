package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.beans.monitor.ExecutionResult;
import com.itorix.apiwiz.analytics.beans.monitor.MonitorCollections;
import com.itorix.apiwiz.analytics.beans.monitor.MonitorCollectionsResponse;
import com.itorix.apiwiz.analytics.model.MonitorCountByExecStatus;
import com.itorix.apiwiz.analytics.model.MonitorStats;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MonitorStatsImpl {

    private static final String MONITOR_EVENTS_HISTORY = "Monitor.Collections.Events.History";

    @Autowired
    private MongoTemplate mongoTemplate;

    public MonitorStats createMonitorStats(String userId) {
        MonitorStats monitorStats = new MonitorStats();
        List<MonitorCollectionsResponse> monitorResponses = getMonitorResponses();

        if(userId != null) {
            monitorResponses = monitorResponses.stream().filter( m -> m.getCreatedBy().equals(userId)).collect(Collectors.toList());
        }

        monitorStats.setTopFiveMonitorsBasedOnUptime(getTopFiveMonitorsBasedOnUptime(monitorResponses));
        monitorStats.setTopFiveMonitorsBasedOnLatency(getTopFiveMonitorsBasedOnLatency(monitorResponses));
        List<MonitorCountByExecStatus> monitorCountByExecStatus = getMonitorCountByExecStatus(userId);
        replaceMonitorCollectionIdWithName(monitorCountByExecStatus);
        monitorStats.setMonitorCountByExecStatus(monitorCountByExecStatus);
        return monitorStats;
    }

    private void replaceMonitorCollectionIdWithName(List<MonitorCountByExecStatus> monitorExecCountByStatuses) {
        HashMap<String, String> monitorIdAndName = new HashMap<>();
        List<MonitorCollections> monitorCollections = mongoTemplate.findAll(MonitorCollections.class);
        monitorCollections.stream().forEach( m -> monitorIdAndName.put(m.getId(), m.getName()));
        monitorExecCountByStatuses.forEach( m -> m.setMonitorCollectionName(monitorIdAndName.get(m.getMonitorCollectionName())));
    }

    private List<MonitorCountByExecStatus> getMonitorCountByExecStatus(String userId) {
        List<MonitorCountByExecStatus> monitorExecCountByStatuses = new ArrayList<>();
        Aggregation aggregation = null;
        GroupOperation groupOperation = Aggregation.group("collectionId", "status").count().as("count");

        if(userId != null ) {
           aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("collectionCreatedBy").is(userId)), groupOperation);
        } else {
            aggregation = Aggregation.newAggregation(groupOperation);
        }
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, MONITOR_EVENTS_HISTORY, Document.class).getMappedResults();
        mappedResults.forEach( d ->  {
            MonitorCountByExecStatus monitorExecCountByStatus = getMonitorExecCountStatus(d);
            monitorExecCountByStatuses.add(monitorExecCountByStatus);
        });
        return monitorExecCountByStatuses;
    }

    private MonitorCountByExecStatus getMonitorExecCountStatus(Document d) {
        MonitorCountByExecStatus monitorExecCountByStatus = new MonitorCountByExecStatus();
        monitorExecCountByStatus.setMonitorCollectionName(d.getString("collectionId"));
        monitorExecCountByStatus.setStatus(d.getString("status"));
        monitorExecCountByStatus.setCount(d.getInteger("count"));

        return monitorExecCountByStatus;
    }

    private Map<String, Long> getTopFiveMonitorsBasedOnLatency(List<MonitorCollectionsResponse> monitorResponses) {
        Map<String, Long> topFiveMonitorBasedOnUptime = new LinkedHashMap<>();
        monitorResponses.stream().filter( r -> r.getLatency() > 0).
                sorted(Comparator.comparingLong(MonitorCollectionsResponse::getLatency)).
                limit(5).forEach( r -> topFiveMonitorBasedOnUptime.put(r.getName(), r.getLatency()));
        return topFiveMonitorBasedOnUptime;
    }

    private Map<String, Integer> getTopFiveMonitorsBasedOnUptime(List<MonitorCollectionsResponse> responses) {
        Map<String, Integer> topFiveMonitorBasedOnUptime = new LinkedHashMap<>();
        responses.stream().filter( r -> r.getUptime() > 0).
                sorted(Comparator.comparingLong(MonitorCollectionsResponse::getUptime).reversed()).
                limit(5).forEach( r -> topFiveMonitorBasedOnUptime.put(r.getName(), r.getUptime()));
        return topFiveMonitorBasedOnUptime;
    }

    private List<MonitorCollectionsResponse> getMonitorResponses() {
        Query query = new Query();

        query.fields().include("id").include("name").include("summary").include("cts").include("createdBy")
                .include("modifiedBy").include("mts").include("schedulers").include("monitorRequest.id")
                .include("monitorRequest.name");

        List<MonitorCollections> monitorCollections = mongoTemplate.find(query, MonitorCollections.class);

        List<String> collectionIds = monitorCollections.stream().map(s -> s.getId()).collect(Collectors.toList());

        Aggregation aggForLatency = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
                Aggregation.group("collectionId").avg("$latency").as("latency"));

        List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class)
                .getMappedResults();

        Aggregation aggForCount = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class)
                .getMappedResults();

        Aggregation aggForSuccess = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("collectionId").in(collectionIds).and("status").is("Success")),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class)
                .getMappedResults();

        List<MonitorCollectionsResponse> monitorResponses = new ArrayList<>();

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
