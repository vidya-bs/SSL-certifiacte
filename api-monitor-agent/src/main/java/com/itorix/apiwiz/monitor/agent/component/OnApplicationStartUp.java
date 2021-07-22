package com.itorix.apiwiz.monitor.agent.component;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.db.MonitorAgentExecutorEntity;

@Component
public class OnApplicationStartUp {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MonitorAgentExecutorSQLDao executorSQLDao;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String createTable = "CREATE TABLE IF NOT EXISTS " + MonitorAgentExecutorEntity.TABLE_NAME
                + "  (id  INTEGER PRIMARY KEY," + "   tenant  TEXT," + " collection_id TEXT," + " scheduler_id  TEXT"
                + "   errorDescription            TEXT," + "   status TEXT)";

        jdbcTemplate.execute(createTable);

        executorSQLDao.updateStatus(Arrays.asList(MonitorAgentExecutorEntity.STATUSES.IN_PROGRESS.getValue()),
                MonitorAgentExecutorEntity.STATUSES.SCHEDULED.getValue());
    }

}