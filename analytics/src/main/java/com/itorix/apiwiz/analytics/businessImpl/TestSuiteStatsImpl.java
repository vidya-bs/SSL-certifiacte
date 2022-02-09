package com.itorix.apiwiz.analytics.businessImpl;


import com.itorix.apiwiz.analytics.model.TestStudioStats;
import com.itorix.apiwiz.analytics.model.TestSuiteExecCountByStatus;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestSuiteStatsImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteStatsImpl.class);
    private static final String TEST_SUITE_COLLECTION = "Test.Collections.List";

    private static final String TEST_EVENT_HISTORY = "Test.Events.History";

    @Autowired
    private MongoTemplate mongoTemplate;

    public TestStudioStats createTestSuiteStats() {
        TestStudioStats testStudioStats = new TestStudioStats();
        try {
            testStudioStats.setTopFiveTestsBasedOnSuccessRatio(getTopFiveTestsBasedOnSuccessRatio());
            testStudioStats.setTestSuiteExecCountByStatuses(getTestSuiteByExecutionStatus());
        } catch (Exception ex) {
            LOGGER.error("Error while calculation Monitoring Stats", ex);
        }
        return testStudioStats;
    }

    private List<TestSuiteExecCountByStatus> getTestSuiteByExecutionStatus() {
        LOGGER.debug("getTestSuiteByExecutionStatus started");
        List<TestSuiteExecCountByStatus> testSuiteByStatusList = new ArrayList<>();
        GroupOperation groupOperation = Aggregation.group("testSuite.name", "testSuite.status").count().as("count");
        Aggregation aggregation = Aggregation.newAggregation(groupOperation);
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, TEST_EVENT_HISTORY, Document.class).getMappedResults();
        mappedResults.forEach( d ->  {
            TestSuiteExecCountByStatus testSuiteByStatus = getTestSuiteByStatus(d);
            testSuiteByStatusList.add(testSuiteByStatus);
        });
        LOGGER.debug("getTestSuiteByExecutionStatus completed");
        return testSuiteByStatusList;
    }

    private TestSuiteExecCountByStatus getTestSuiteByStatus(Document d) {
        TestSuiteExecCountByStatus testSuiteByStatus = new TestSuiteExecCountByStatus();
        testSuiteByStatus.setTestSuiteName(d.getString("name"));
        testSuiteByStatus.setStatus(d.getString("status"));
        testSuiteByStatus.setCount(d.getInteger("count"));
        return testSuiteByStatus;
    }

    public Map<String, Integer> getTopFiveTestsBasedOnSuccessRatio() {
        LOGGER.debug("getTopFiveTestsBasedOnSuccessRatio started");
        Map<String, Integer> testsWithHigherSuccessRatio = new LinkedHashMap<>();
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("successRatio"))).limit(5);
        List<Document> documents = mongoTemplate.find(query, Document.class, TEST_SUITE_COLLECTION);
        documents.forEach( d -> testsWithHigherSuccessRatio.put(d.getString("name"), d.getInteger("successRatio")));
        LOGGER.debug("getTopFiveTestsBasedOnSuccessRatio completed");
        return testsWithHigherSuccessRatio;
    }

}
