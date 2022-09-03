package com.itorix.apiwiz.test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.test.dao.TestSuitExecutorSQLDao;
import com.itorix.apiwiz.test.dao.TestSuiteExecutorDao;
import com.itorix.apiwiz.test.db.TestExecutorEntity;
import com.itorix.apiwiz.test.executor.TestRunner;
import com.itorix.apiwiz.test.executor.beans.ExecutionContext;
import com.itorix.apiwiz.test.executor.beans.TestSuiteResponse;
import com.itorix.apiwiz.test.executor.model.TenantContext;

@Configuration
@EnableAutoConfiguration
@Component
@EnableScheduling
@Slf4j
public class TestSuiteExecutor {

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

    @Autowired
    private TestSuiteExecutorDao dao;

    @Autowired
    private TestSuitExecutorSQLDao sqlDao;

    @Autowired
    private TestRunner testRunner;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${http.timeout}")
    int globalTimeout;

    @Scheduled(fixedRate = 15000)
    public void fetchAndRunTestsuites() {

        int avlThreads = (Runtime.getRuntime().availableProcessors() * 5)
                - ((ThreadPoolExecutor) executor).getActiveCount();

        List<TestExecutorEntity> executorEntities = sqlDao.getExecutorEntityByColumn("status",
                TestExecutorEntity.STATUSES.SCHEDULED.getValue(), avlThreads);

        for (TestExecutorEntity testExecutorEntity : executorEntities) {
            TenantContext.setCurrentTenant(testExecutorEntity.getTenant());
            ExecutionContext context = new ExecutionContext();
            TestSuiteResponse testSuiteResponse = dao
                    .getTestSuiteResponseById(testExecutorEntity.getTestSuiteExecutionId());
            if (testSuiteResponse == null) {
                sqlDao.updateErrorDescription(testExecutorEntity.getId(), "could not find testSuiteResponse");
                continue;
            }
            if (TestSuiteResponse.STATUSES.CANCELLED.getValue().equals(testSuiteResponse.getStatus())) {
                continue;
            }
            context.setTestSuiteResponse(testSuiteResponse);
            context.setTestExecutorEntity(testExecutorEntity);
            context.setTenant(testExecutorEntity.getTenant());
            if (globalTimeout != 0) {
                context.setGlobalTimeout(globalTimeout);
            }
            testExecutorEntity.setStatus(TestExecutorEntity.STATUSES.IN_PROGRESS.getValue());
            sqlDao.updateField(testExecutorEntity.getId(), "status",
                    TestExecutorEntity.STATUSES.IN_PROGRESS.getValue());
            try {
                dao.updateTestSuiteStatus(testSuiteResponse.getId(), testSuiteResponse,
                        TestSuiteResponse.STATUSES.IN_PROGRESS.getValue());

            } catch (Exception ex) {
                log.error("Exception occurred",ex);
            }
            executor.execute(() -> testRunner.run(context));
        }
    }
}