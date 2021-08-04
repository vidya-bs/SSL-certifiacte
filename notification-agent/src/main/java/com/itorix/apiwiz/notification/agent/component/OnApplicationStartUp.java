package com.itorix.apiwiz.notification.agent.component;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.notification.agent.dao.NotificationAgentExecutorSQLDao;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;

@Component
public class OnApplicationStartUp {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NotificationAgentExecutorSQLDao executorSQLDao;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String createTable = "CREATE TABLE IF NOT EXISTS " + NotificationExecutorEntity.TABLE_NAME
                + "  (id  INTEGER PRIMARY KEY," + "   type  TEXT," + " content TEXT," + "   status TEXT)";

        jdbcTemplate.execute(createTable);

        executorSQLDao.updateStatus(Arrays.asList(NotificationExecutorEntity.STATUSES.IN_PROGRESS.getValue()),
                NotificationExecutorEntity.STATUSES.SCHEDULED.getValue());
    }

}