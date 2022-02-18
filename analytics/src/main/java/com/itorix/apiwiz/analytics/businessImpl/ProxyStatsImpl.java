package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.model.ProxyStats;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProxyStatsImpl {
    private static final String CODE_COVERAGE_LIST = "Connectors.Apigee.CodeCoverage.List";

    @Autowired
    private MongoTemplate mongoTemplate;

    public ProxyStats createProxyStats(String userId) {
        ProxyStats proxyStats = new ProxyStats();
        proxyStats.setTopFiveProxiesBasedOnCoverage(getTopFiveProxiesBasedOnCoverage(userId));
        return proxyStats;
    }

    private Map<String, Double> getTopFiveProxiesBasedOnCoverage(String userId) {
        Map<String, Double> topFiveProxiesBasedOnCoverage = new LinkedHashMap<>();
        Aggregation aggregation = null;

        if(userId != null) {
            aggregation = getAggregationForUser(userId);
        } else {
            aggregation = getAggregation();
        }

        AggregationResults<Document> aggregateResults = mongoTemplate.aggregate(aggregation, CODE_COVERAGE_LIST, Document.class);

        for (Document mappedResult : aggregateResults.getMappedResults()) {
            String proxyName = mappedResult.getString("_id");
            Double avgCodeCoverage = mappedResult.getDouble("avgCodeCoverage");
            topFiveProxiesBasedOnCoverage.put(proxyName, avgCodeCoverage);
        }

        return topFiveProxiesBasedOnCoverage;
    }

    private Aggregation getAggregationForUser(String userId) {
        GroupOperation groupOperation = Aggregation.group("proxy").avg(ConvertOperators.Convert.convertValueOf("proxyStat.coverage").to(JsonSchemaObject.Type.DOUBLE)).as("avgCodeCoverage");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "avgCodeCoverage");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("createdBy").is(userId)),
                groupOperation, sortOperation, Aggregation.limit(5));
        return aggregation;
    }

    private Aggregation getAggregation() {
        GroupOperation groupOperation = Aggregation.group("proxy").avg(ConvertOperators.Convert.convertValueOf("proxyStat.coverage").to(JsonSchemaObject.Type.DOUBLE)).as("avgCodeCoverage");

        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "avgCodeCoverage");

        Aggregation aggregation = Aggregation.newAggregation(groupOperation, sortOperation, Aggregation.limit(5));
        return aggregation;
    }
}
