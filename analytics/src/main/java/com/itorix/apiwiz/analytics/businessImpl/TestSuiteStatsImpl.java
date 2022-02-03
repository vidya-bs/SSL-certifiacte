package com.itorix.apiwiz.analytics.businessImpl;


import com.itorix.apiwiz.analytics.model.TestStudioStats;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestSuiteStatsImpl {

    private static final String TEST_SUITE_COLLECTION = "Test.Collections.List";

    @Autowired
    private MongoTemplate mongoTemplate;

    public TestStudioStats createTestSuiteStats() {
        TestStudioStats testStudioStats = new TestStudioStats();
        testStudioStats.setTopFiveTestsBasedOnSuccessRatio(getTopFiveTestsBasedOnSucessRatio());
        return testStudioStats;
    }

    public Map<String, Integer> getTopFiveTestsBasedOnSucessRatio() {
        Map<String, Integer> testsWithHigherSuccessRatio = new LinkedHashMap<>();
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("successRatio"))).limit(5);
        List<Document> documents = mongoTemplate.find(query, Document.class, TEST_SUITE_COLLECTION);
        documents.forEach( d -> testsWithHigherSuccessRatio.put(d.getString("name"), d.getInteger("successRatio")));
        return testsWithHigherSuccessRatio;
    }

}
