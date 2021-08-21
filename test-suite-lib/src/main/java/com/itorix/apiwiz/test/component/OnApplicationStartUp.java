package com.itorix.apiwiz.test.component;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.test.dao.TestSuitExecutorSQLDao;
import com.itorix.apiwiz.test.db.TestExecutorEntity;

@Component
public class OnApplicationStartUp {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TestSuitExecutorSQLDao executorSQLDao;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String createTable = "CREATE TABLE IF NOT EXISTS " + TestExecutorEntity.TABLE_NAME
                + "  (id  INTEGER PRIMARY KEY," + "   tenant            TEXT," + "   testSuiteExecutionId   TEXT,"
                + "   errorDescription            TEXT," + "   status TEXT)";

        jdbcTemplate.execute(createTable);

        executorSQLDao.updateStatus(Arrays.asList(TestExecutorEntity.STATUSES.IN_PROGRESS.getValue()),
                TestExecutorEntity.STATUSES.SCHEDULED.getValue());
    }

}