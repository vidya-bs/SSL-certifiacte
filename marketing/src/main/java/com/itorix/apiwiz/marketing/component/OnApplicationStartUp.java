package com.itorix.apiwiz.marketing.component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.marketing.db.NotificationExecutorEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OnApplicationStartUp {

    @Autowired
    @Qualifier("notificationSQLiteDB")
    JdbcTemplate notificationJdbcTemplate;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws ItorixException {
        String createNotifyTable = "CREATE TABLE IF NOT EXISTS " + NotificationExecutorEntity.TABLE_NAME
                + "  (id  INTEGER PRIMARY KEY," + "   tenant  TEXT," + " notificationExecutionId TEXT,"
                + "   errorDescription            TEXT,"+ "   status      TEXT," + "   errorCode      TEXT," + "  lockedBy TEXT)";
        notificationJdbcTemplate.execute(createNotifyTable);
    }

}