package com.itorix.apiwiz.analytics.businessImpl;


import com.itorix.apiwiz.analytics.model.TestStudioStats;
import com.itorix.apiwiz.analytics.model.TestSuiteExecCountByStatus;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestSuiteStatsImpl {

    private static final String TEST_SUITE_COLLECTION = "Test.Collections.List";

    private static final String TEST_EVENT_HISTORY = "Test.Events.History";

    @Autowired
    private MongoTemplate mongoTemplate;

    public TestStudioStats createTestSuiteStats(String userId) {
        TestStudioStats testStudioStats = new TestStudioStats();
        testStudioStats.setTopFiveTestsBasedOnSuccessRatio(getTopFiveTestsBasedOnSuccessRatio(userId));
        testStudioStats.setTestSuiteExecCountByStatuses(getTestSuiteByExecutionStatus(userId));
        return testStudioStats;
    }

    private List<TestSuiteExecCountByStatus> getTestSuiteByExecutionStatus(String userId) {
        List<TestSuiteExecCountByStatus> testSuiteByStatusList = new ArrayList<>();
        Aggregation aggregation = null;

        if(userId != null) {
            aggregation = getAggregationForUserId(userId);
        } else {
            aggregation = getAggregation();
        }


        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, TEST_EVENT_HISTORY, Document.class).getMappedResults();
        mappedResults.forEach( d ->  {
            TestSuiteExecCountByStatus testSuiteByStatus = getTestSuiteByStatus(d);
            testSuiteByStatusList.add(testSuiteByStatus);
        });
        return testSuiteByStatusList;
    }

    private Aggregation getAggregation() {
        GroupOperation groupOperation = Aggregation.group("testSuite.name", "testSuite.status").count().as("count");
        Aggregation aggregation = Aggregation.newAggregation(groupOperation);
        return aggregation;
    }

    private Aggregation getAggregationForUserId(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("userId").is(userId));
        GroupOperation groupOperation = Aggregation.group("testSuite.name", "testSuite.status").count().as("count");
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
        return aggregation;
    }

    private TestSuiteExecCountByStatus getTestSuiteByStatus(Document d) {
        TestSuiteExecCountByStatus testSuiteByStatus = new TestSuiteExecCountByStatus();
        testSuiteByStatus.setTestSuiteName(d.getString("name"));
        testSuiteByStatus.setStatus(d.getString("status"));
        testSuiteByStatus.setCount(d.getInteger("count"));
        return testSuiteByStatus;
    }

    public Map<String, Integer> getTopFiveTestsBasedOnSuccessRatio(String userId) {
        Map<String, Integer> testsWithHigherSuccessRatio = new LinkedHashMap<>();
        Query query = null;

        if(userId != null ) {
            query = getTopFiveTestsBasedOnSuccessRatioQueryForUser(userId);
        } else {
            query = getTopFiveTestsBasedOnSuccessRatioQuery();
        }

        List<Document> documents = mongoTemplate.find(query, Document.class, TEST_SUITE_COLLECTION);
        documents.forEach( d -> testsWithHigherSuccessRatio.put(d.getString("name"), d.getInteger("successRatio")));
        return testsWithHigherSuccessRatio;
    }

    private Query getTopFiveTestsBasedOnSuccessRatioQueryForUser(String userId) {
        return Query.query(Criteria.where("createdBy").is(userId)).with(Sort.by(Sort.Order.desc("successRatio"))).limit(5);
    }

    private Query getTopFiveTestsBasedOnSuccessRatioQuery() {
        return new Query().with(Sort.by(Sort.Order.desc("successRatio"))).limit(5);
    }

}
