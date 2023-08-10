package com.itorix.apiwiz.design.studio.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OnApplicationStarts {

    @Autowired
    @Qualifier("complianceJdbcTemplate")
    JdbcTemplate jdbcTemplate;


    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String createTable = "CREATE TABLE IF NOT EXISTS " + ComplicanceScannerExecutorEntity.TABLE_NAME
                + "  (id  SERIAL PRIMARY KEY," + "   tenant            TEXT," + "   complianceScannerExecutionId   TEXT,"
                +"operation     TEXT," +"   error_description            TEXT,"+ "   status      TEXT,"+ "   errorCode      TEXT,"
                + "  lockedBy TEXT)";

        jdbcTemplate.execute(createTable);
    }

}