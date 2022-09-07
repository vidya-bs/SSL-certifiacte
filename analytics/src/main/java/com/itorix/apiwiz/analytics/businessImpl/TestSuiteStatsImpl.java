package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.beans.test.Scenario;
import com.itorix.apiwiz.analytics.beans.test.TestCase;
import com.itorix.apiwiz.analytics.beans.test.TestSuiteResponse;
import com.itorix.apiwiz.analytics.model.TestStudioStats;
import com.itorix.apiwiz.analytics.model.TestSuiteExecutionMetric;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

@Component
public class TestSuiteStatsImpl {

	private static final String TEST_SUITE_COLLECTION = "Test.Collections.List";

	private static final String TEST_EVENT_HISTORY = "Test.Events.History";

	@Autowired
	private MongoTemplate mongoTemplate;

	public TestStudioStats createTestSuiteStats(String userId) {
		TestStudioStats testStudioStats = new TestStudioStats();
		testStudioStats.setTopFiveTestsBasedOnSuccessRatio(getTopFiveTestsBasedOnSuccessRatio(userId));
		List<TestSuiteExecutionMetric> testSuiteExecMetrics = getTestSuiteExecMetrics(userId);

		testSuiteExecMetrics.forEach(t -> calculateAvgDuration(t));

		testStudioStats.setTestSuiteExecutionMetricsList(testSuiteExecMetrics);
		return testStudioStats;
	}

	private void calculateAvgDuration(TestSuiteExecutionMetric executionMetric) {
		List list = new ArrayList<>();
		Query query = Query.query(Criteria.where("testSuite.name").is(executionMetric.getTestSuiteName()));
		List<TestSuiteResponse> testSuiteResponses = mongoTemplate.find(query, TestSuiteResponse.class,
				TEST_EVENT_HISTORY);

		int totalDuration = 0;
		int count = 0;

		for (TestSuiteResponse testSuiteResponse : testSuiteResponses) {
			List<Scenario> scenarios = testSuiteResponse.getTestSuite().getScenarios();
			if (scenarios != null) {
				for (Scenario scenario : scenarios) {
					List<TestCase> testCases = scenario.getTestCases();
					if (testCases != null) {
						for (TestCase testCase : testCases) {
							Long duration = testCase.getDuration();
							if (null != duration && duration != 0) {
								totalDuration += duration;
								count++;
							}
						}
					}
				}
			}
		}

		if (count != 0) {
			executionMetric.setAverageExecutionTime(totalDuration / count);
		}

	}

	private List<TestSuiteExecutionMetric> getTestSuiteByExecution(String userId) {
		List<TestSuiteExecutionMetric> testSuiteByStatusList = new ArrayList<>();
		Aggregation aggregation = null;

		if (userId != null) {
			aggregation = getAggregationForUserId(userId, "PASS");
		} else {
			aggregation = getAggregation("");
		}

		List<Document> mappedResults = mongoTemplate.aggregate(aggregation, TEST_EVENT_HISTORY, Document.class)
				.getMappedResults();
		mappedResults.forEach(d -> {
			TestSuiteExecutionMetric testSuiteByStatus = getTestSuiteByStatus(d);
			testSuiteByStatusList.add(testSuiteByStatus);
		});
		return testSuiteByStatusList;
	}

	private Aggregation getAggregation(String status) {
		MatchOperation matchStatus = Aggregation.match(Criteria.where("testSuite.status").is(status));
		GroupOperation groupOperation = Aggregation.group("testSuite.name").count().as("count");
		Aggregation aggregation = Aggregation.newAggregation(matchStatus, groupOperation);
		return aggregation;
	}

	private Aggregation getAggregationForUserId(String userId, String status) {
		MatchOperation matchOperation = Aggregation.match(Criteria.where("userId").is(userId));
		MatchOperation matchStatus = Aggregation.match(Criteria.where("testSuite.status").is(status));
		GroupOperation groupOperation = Aggregation.group("testSuite.name").count().as("count");
		Aggregation aggregation = Aggregation.newAggregation(matchOperation, matchStatus, groupOperation);
		return aggregation;
	}

	private TestSuiteExecutionMetric getTestSuiteByStatus(Document d) {
		TestSuiteExecutionMetric testSuiteByStatus = new TestSuiteExecutionMetric();
		testSuiteByStatus.setTestSuiteName(d.getString("name"));
		return testSuiteByStatus;
	}

	private List<TestSuiteExecutionMetric> getTestSuiteExecMetrics(String userId) {
		LinkedList<TestSuiteExecutionMetric> testSuiteExecutionMetricsList = new LinkedList<>();

		GroupOperation groupOperation = Aggregation.group("$testSuite.name").count().as("totalCount").push("$$ROOT")
				.as("docs");

		ProjectionOperation projectionOperation = Aggregation.project("_id", "totalCount")
				.and(filter("docs").as("doc").by(valueOf("doc.testSuite.status").equalToValue("PASS")))
				.as("successfulExecCount")
				.and(filter("docs").as("doc").by(valueOf("doc.testSuite.status").equalToValue("FAIL")))
				.as("failureExecCount");

		ProjectionOperation projectCount = Aggregation.project("totalCount").and("$successfulExecCount").size()
				.as("successfulExecCount").and("$failureExecCount").size().as("failureExecCount");

		AggregationResults<Document> results = null;

		if (userId != null) {
			MatchOperation matchOperation = Aggregation.match(Criteria.where("userId").is(userId));
			results = mongoTemplate.aggregate(
					Aggregation.newAggregation(matchOperation, groupOperation, projectionOperation, projectCount),
					TEST_EVENT_HISTORY, Document.class);
		} else {
			results = mongoTemplate.aggregate(
					Aggregation.newAggregation(groupOperation, projectionOperation, projectCount), TEST_EVENT_HISTORY,
					Document.class);
		}

		for (Document document : results.getMappedResults()) {
			TestSuiteExecutionMetric testSuiteExecutionMetrics = new TestSuiteExecutionMetric();
			testSuiteExecutionMetrics.setTestSuiteName(document.getString("_id"));
			testSuiteExecutionMetrics.setTotalExecutionCount(document.getInteger("totalCount"));
			testSuiteExecutionMetrics.setSuccessFullExecutionCount(document.getInteger("successfulExecCount"));
			testSuiteExecutionMetrics.setFailedExecutionCount(document.getInteger("failureExecCount"));
			testSuiteExecutionMetricsList.add(testSuiteExecutionMetrics);
		}

		return testSuiteExecutionMetricsList;
	}

	public Map<String, Integer> getTopFiveTestsBasedOnSuccessRatio(String userId) {
		Map<String, Integer> testsWithHigherSuccessRatio = new LinkedHashMap<>();
		Query query = null;

		if (userId != null) {
			query = getTopFiveTestsBasedOnSuccessRatioQueryForUser(userId);
		} else {
			query = getTopFiveTestsBasedOnSuccessRatioQuery();
		}

		List<Document> documents = mongoTemplate.find(query, Document.class, TEST_SUITE_COLLECTION);
		documents.forEach(d -> testsWithHigherSuccessRatio.put(d.getString("name"), d.getInteger("successRatio")));
		return testsWithHigherSuccessRatio;
	}

	private Query getTopFiveTestsBasedOnSuccessRatioQueryForUser(String userId) {
		return Query.query(Criteria.where("createdBy").is(userId)).with(Sort.by(Sort.Order.desc("successRatio")))
				.limit(5);
	}

	private Query getTopFiveTestsBasedOnSuccessRatioQuery() {
		return new Query().with(Sort.by(Sort.Order.desc("successRatio"))).limit(5);
	}

}
