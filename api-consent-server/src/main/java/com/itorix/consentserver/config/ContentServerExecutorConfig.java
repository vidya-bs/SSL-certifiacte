package com.itorix.consentserver.config;

import com.itorix.consentserver.sched.ContentServerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
//@Configuration
//@EnableScheduling
public class ContentServerExecutorConfig implements SchedulingConfigurer {


    private static final String CONSENT_EXPIRY_CHECK_INTERVAL = "itorix.core.consent.expiry.interval";
    private static final String COLLECTION_NAME = "Connectors.Workspace.List";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ContentServerExecutor contentServerExecutor;

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
                new Runnable() {
                    @Override public void run() {
                        contentServerExecutor.expireConsents();
                    }
                },
                new Trigger() {
                    @Override public Date nextExecutionTime(TriggerContext triggerContext) {
                        int checkInterval = 5;
                        Calendar nextExecutionTime =  new GregorianCalendar();
                        Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
                        try {
                            Document document = mongoTemplate.findById(CONSENT_EXPIRY_CHECK_INTERVAL, Document.class, COLLECTION_NAME);
                            log.debug(mongoTemplate.getDb().getName());
                            checkInterval = document.get("propertyValue", Integer.class);
                            log.debug("Successfully retrieved consent expiry check interval {} ", checkInterval);
                        } catch(Exception ex) {
                            log.error("Error while retrieving consent expiry check interval. Falling back to default checking interval", ex);
                        }
                        nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                        nextExecutionTime.add(Calendar.SECOND, checkInterval);
                        return nextExecutionTime.getTime();
                    }
                }
        );
    }
}
