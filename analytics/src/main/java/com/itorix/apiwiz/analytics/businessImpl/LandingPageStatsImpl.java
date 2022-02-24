package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.model.LandingPageMetrics;
import com.itorix.apiwiz.analytics.model.WorkspaceDashboard;
import com.itorix.apiwiz.common.model.proxystudio.Swagger3VO;
import com.itorix.apiwiz.common.model.proxystudio.SwaggerVO;
import com.itorix.apiwiz.identitymanagement.model.ActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class LandingPageStatsImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LandingPageStatsImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TestSuiteStatsImpl testSuiteStats;

    @Autowired
    private ProxyStatsImpl proxyStatsImpl;

    @Autowired
    private MonitorStatsImpl monitorStatsImpl;

    public void generateWorkspaceDashboard(String userId) {
        WorkspaceDashboard workspaceDashboard = new WorkspaceDashboard();
        LandingPageMetrics landingPageMetrics = new LandingPageMetrics();

        if(userId == null || "".equals(userId)) {
            workspaceDashboard.setUserId("SYSTEM");
        } else {
            workspaceDashboard.setUserId(userId);
        }

        workspaceDashboard.setCreatedTs(System.currentTimeMillis());
        landingPageMetrics.setNumberOfPortfoliosCreated(getNumberOfPortfolioCreated(userId));
        landingPageMetrics.setNumberOfSwaggersCreated(getNumberOfSwaggersCreated(userId));
        landingPageMetrics.setNumberOfProxiesCreated(getNumberOfProxiesCreated(userId));
        landingPageMetrics.setNumberOfMockScenariosCreated(getNumberOfMockScenariosCreated(userId));
        landingPageMetrics.setNumberOfTestSuitesCreated(getNumberOfTestSuiteCreated(userId));
        landingPageMetrics.setNumberOfPipelinesCreated(getNumberOfPipelineCreated(userId));
        landingPageMetrics.setNumberOfMonitorCollectionsCreated(getNumberOfMonitorCollectionsCreated(userId));
        landingPageMetrics.setNumberOfTestsExecuted(getNumberOfTestsExecuted(userId));
        landingPageMetrics.setNumberOfPipelinesTriggered(getNumberOfPipelineTriggered(userId));

        workspaceDashboard.setTestStudioStats(testSuiteStats.createTestSuiteStats(userId));
        workspaceDashboard.setProxyStats(proxyStatsImpl.createProxyStats(userId));
        workspaceDashboard.setMonitorStats(monitorStatsImpl.createMonitorStats(userId));

        workspaceDashboard.setLandingPageMetrics(landingPageMetrics);

        mongoTemplate.save(workspaceDashboard);


    }

    private int getNumberOfPipelineTriggered(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/pipelines/.*run", true).size();
    }

    private int getNumberOfTestsExecuted(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/testsuites/.*run", true).size();
    }

    private int getNumberOfMonitorCollectionsCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/monitor/collections/", false).size();
    }

    private int getNumberOfPipelineCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/pipelines", false).size();
    }

    private int getNumberOfTestSuiteCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/testsuites", false).size();
    }

    private int getNumberOfMockScenariosCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/mock/scenarios", false).size();
    }

    private int getNumberOfProxiesCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/buildconfig/codegen/generate", false).size();
    }

    private int getNumberOfPortfolioCreated(String userId) {
        return getFromActivityLog(userId, "/itorix/v1/portfolios", false).size();
    }

    private int getNumberOfSwaggersCreated(String userId) {
        Query query = null;
        if(userId == null ) {
            query = getQueryForSwaggerCreated();
        } else {
            query = getQueryForSwaggerCreatedByUser(userId);
        }
        int oas2Size = mongoTemplate.find(query, SwaggerVO.class).size();
        int oas3Size = mongoTemplate.find(query, Swagger3VO.class).size();
        return oas2Size + oas3Size;
    }

    private Query getQueryForSwaggerCreatedByUser(String userId) {
        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        return Query.query(Criteria.where("createdBy").is(userId).and("cts").gte(unixEpoch));
    }

    private Query getQueryForSwaggerCreated() {
        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        return Query.query(Criteria.where("cts").gte(unixEpoch));
    }

    private List<ActivityLog> getFromActivityLog(String userId, String requestURI, boolean useRegex) {
        LOGGER.debug("getFromActivityLog userId {}, requestURI {} , useRegex {}", userId, requestURI, useRegex);
        Criteria criteria = null;

        if(userId != null && !"".equals(userId)) {
            criteria = getCriteriaForUser(userId);
        } else {
            criteria = getCriteriaWithoutUser();
        }

        if(useRegex) {
            criteria.and("requestURI").regex(requestURI);
        } else {
            criteria.and("requestURI").is(requestURI);
        }
        return mongoTemplate.find(Query.query(criteria), ActivityLog.class);
    }

    private Criteria getCriteriaForUser(String userId) {
        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        Criteria criteria = Criteria.where("id_user").is(userId)
                .and("operation").is("POST").and("statusCode").is(200).and("last_Changed_At").gte(unixEpoch);
        return criteria;
    }

    private Criteria getCriteriaWithoutUser() {
        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        Criteria criteria = Criteria.where("operation").is("POST").and("statusCode").is(200).and("last_Changed_At").gte(unixEpoch);
        return criteria;
    }

    public WorkspaceDashboard getWorkspaceDashboard(String userId) {
        if (userId == null || userId == "") {
            userId = "SYSTEM";
        }
        return mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)).
                limit(1).with(Sort.by(Sort.Direction.DESC, "createdTs")), WorkspaceDashboard.class);
    }
}
