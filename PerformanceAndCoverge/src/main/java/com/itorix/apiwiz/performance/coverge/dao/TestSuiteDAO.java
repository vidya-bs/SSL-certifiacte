package com.itorix.apiwiz.performance.coverge.dao;

import com.itorix.apiwiz.performance.coverge.model.DashboardStats;
import com.itorix.apiwiz.performance.coverge.model.DashboardSummary;
import com.itorix.apiwiz.performance.coverge.model.TestSuiteResponse;
import com.itorix.test.executor.beans.Scenario;
import com.itorix.test.executor.beans.TestCase;
import com.itorix.test.executor.beans.TestSuite;
import com.itorix.test.executor.beans.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TestSuiteDAO {

  @Autowired
  private MongoTemplate mongoTemplate;

  private static final Logger log = LoggerFactory.getLogger(TestSuiteDAO.class);


  public Object getTestSuite(String testsuiteid) {
    Query query = new Query(Criteria.where("_id").is(testsuiteid));
    return mongoTemplate.findOne(query, Object.class, "Test.Collections.List");
  }

  public Object getVariablesById(String id) {
    Query query = new Query(Criteria.where("id").is(id));
    return mongoTemplate.findOne(query, Object.class, "Test.Environments.List");
  }

  public TestSuiteResponse getTestSuiteResponseById(String testsuiteResponseid) {
    Query query = new Query(Criteria.where("_id").is(testsuiteResponseid));
    TestSuiteResponse response = mongoTemplate.findOne(query, TestSuiteResponse.class);

    if (response != null && response.getTestSuite() != null && response.getTestSuite().getScenarios() != null) {
      response = populateDuration(response);
    }
    response.getTestSuite().setExecutionStatus(null);
    return response;
  }

  public void saveTestSuiteResponse(TestSuiteResponse testSuiteResponse) {
    String testSuiteExecutionStatus = testSuiteResponse.getStatus();
    if (testSuiteExecutionStatus != null && (!testSuiteResponse.isManual())
            && (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue()))
            || testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.CANCELLED.getValue())
            || (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.IN_PROGRESS.getValue())
            && testSuiteResponse.getCounter() != null)) {
      Query query = new Query(Criteria.where("testSuiteId").is(testSuiteResponse.getTestSuiteId()).and("configId")
              .is(testSuiteResponse.getConfigId()).and("status")
              .is(TestSuiteResponse.STATUSES.IN_PROGRESS.getValue()));
      List<TestSuiteResponse> responses = mongoTemplate.find(query, TestSuiteResponse.class);
      for (TestSuiteResponse response : responses) {
        response.setMts(System.currentTimeMillis());
        response.setStatus(testSuiteResponse.getStatus());
        response.setCounter(testSuiteResponse.getCounter());
        response.setTestSuite(testSuiteResponse.getTestSuite());
        mongoTemplate.save(response);
        if (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue())
                || testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.CANCELLED.getValue())) {
          saveDashboardStats(response);
        }
      }
    } else {
      testSuiteResponse.setMts(System.currentTimeMillis());
      mongoTemplate.save(testSuiteResponse);
    }
  }

  private TestSuiteResponse populateDuration(TestSuiteResponse response) {
    if (response != null && response.getTestSuite() != null && response.getTestSuite().getScenarios() != null) {
      Long testSuiteDurationTotal = new Long(0);
      for (Scenario scenario : response.getTestSuite().getScenarios()) {
        Long total = new Long(0);
        for (TestCase testCase : scenario.getTestCases()) {
          if (testCase.getDuration() != null) {
            total += testCase.getDuration();
          }
        }
        testSuiteDurationTotal += total;
        scenario.setDuration(total);
      }
      response.getTestSuite().setDuration(testSuiteDurationTotal);
    }
    return response;
  }

  public void saveDashboardStats(TestSuiteResponse testSuiteResponse) {
    DashboardStats stats = new DashboardStats(testSuiteResponse.getTestSuiteId(), testSuiteResponse.getConfigId(),
            testSuiteResponse.getCts(), testSuiteResponse.getMts(), testSuiteResponse.getCreatedUserName(),
            testSuiteResponse.getModifiedUserName(), testSuiteResponse.getCreatedBy(),
            testSuiteResponse.getModifiedBy(), testSuiteResponse.getSuccessRate(),
            testSuiteResponse.getTestSuite().getStatus(), testSuiteResponse.getTestSuiteName());
    mongoTemplate.save(stats);

    DashboardSummary summary = mongoTemplate.findOne(
            new Query(Criteria.where("testSuiteName").is(testSuiteResponse.getTestSuite().getName())),
            DashboardSummary.class);

    if (testSuiteResponse.getStatus() != null && testSuiteResponse.getTestSuite() != null) {
      if (summary == null) {
        if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
          mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
                  testSuiteResponse.getTestSuite().getName(), 1, 0, 0,
                  testSuiteResponse.getTestSuite().getSuccessRate()));

        } else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
          mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
                  testSuiteResponse.getTestSuite().getName(), 0, 1, 0,
                  testSuiteResponse.getTestSuite().getSuccessRate()));
        } else {
          mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
                  testSuiteResponse.getTestSuite().getName(), 0, 0, 1,
                  testSuiteResponse.getTestSuite().getSuccessRate()));
        }
      } else {
        if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
          summary.setSuccessCount(summary.getSuccessCount() + 1);
          summary.setSuccessRatio(
                  ((summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2));


        } else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
          summary.setFailureCount(summary.getFailureCount() + 1);
          summary.setSuccessRatio(
                  ((summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2));
        } else {
          summary.setCancelledCount(summary.getCancelledCount() + 1);
        }
        mongoTemplate.save(summary);
      }

      Query query = new Query(Criteria.where("name").is(testSuiteResponse.getTestSuite().getName()));
      Update update = new Update();
      if(summary != null ){
        update.set("successRatio", summary.getSuccessRatio());
      } else {
        update.set("successRatio", testSuiteResponse.getTestSuite().getSuccessRate());
      }
      update.set("executionStatus",testSuiteResponse.getTestSuite().getStatus());
      mongoTemplate.updateFirst(query, update, TestSuite.class);

    }
  }
}
