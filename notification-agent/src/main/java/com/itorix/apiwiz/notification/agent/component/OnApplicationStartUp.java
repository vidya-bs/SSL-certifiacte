package com.itorix.apiwiz.notification.agent.component;

import com.itorix.apiwiz.notification.agent.dao.NotificationAgentExecutorSQLDao;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class OnApplicationStartUp {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NotificationAgentExecutorSQLDao executorSQLDao;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String createTable = "CREATE TABLE IF NOT EXISTS " + NotificationExecutorEntity.TABLE_NAME
                + "  (id  INTEGER PRIMARY KEY," + "   type  TEXT," + " content TEXT," + "   status TEXT" + " tenantId TEXT" + ")";

        String addColumn = "ALTER TABLE " + NotificationExecutorEntity.TABLE_NAME  +  " ADD tenantId TEXT";

        jdbcTemplate.execute(createTable);

        try {
            jdbcTemplate.execute(addColumn);
        } catch (Exception e) {

        }

        executorSQLDao.updateStatus(Arrays.asList(NotificationExecutorEntity.STATUSES.IN_PROGRESS.getValue()),
                NotificationExecutorEntity.STATUSES.SCHEDULED.getValue());
    }

}