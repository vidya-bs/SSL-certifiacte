package com.itorix.apiwiz.consent.management.sched;

import com.itorix.apiwiz.consent.management.dao.ConsentManagementDao;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

@Slf4j
@Component
public class ConsentScheduler {

    public static final String CONSENT_GROUP = "CONSENT-GROUP";
    @Autowired
    private ConsentManagementDao consentManagementDao;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private MongoProperties mongoProperties;

    @PostConstruct
    private void initSchedulers() throws SchedulerException {

        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
            MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
            while (dbsCursor.hasNext()) {
                String currentTenant = dbsCursor.next();
                TenantContext.setCurrentTenant(currentTenant);
                Integer interval = consentManagementDao.getConsentExpirationInterval();
                if(interval != null) {
                    Workspace workspace = consentManagementDao.getWorkspace(currentTenant);
                    String tenantKey = workspace.getKey();
                    String publicKey = consentManagementDao.getConsentPublicKey(tenantKey);
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("tenantKey", tenantKey);
                    jobDataMap.put("publicKey", publicKey);

                    JobDetail jobDetail = buildJobDetail(tenantKey);
                    Trigger trigger = buildJobTrigger(jobDetail, jobDataMap, interval);
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            }
        }
        log.debug("Successfully defined Triggers for the tenants {} ", scheduler.getTriggerKeys(groupEquals(CONSENT_GROUP)));

    }

    private JobDetail buildJobDetail(String tenantId) {
        return JobBuilder.newJob(ConsentSchedulerJob.class)
                .withIdentity(tenantId, CONSENT_GROUP)
                .withDescription("Consent Job")
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, JobDataMap jobDataMap, int interval) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .usingJobData(jobDataMap)
                .withIdentity(jobDetail.getKey().getName(), CONSENT_GROUP)
                .withDescription("Consent Trigger")
                .withSchedule(simpleSchedule().withIntervalInMinutes(interval).repeatForever())
                .build();
    }


    @SneakyThrows
    public void updateTrigger(String tenantKey) {
        Trigger existingTrigger = scheduler.getTrigger(TriggerKey.triggerKey(tenantKey, CONSENT_GROUP));

        TriggerBuilder existingTriggerBuilder = existingTrigger.getTriggerBuilder();

        Integer interval = consentManagementDao.getConsentExpirationInterval();
        String publicKey = consentManagementDao.getConsentPublicKey(tenantKey);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("tenantKey", tenantKey);
        jobDataMap.put("publicKey", publicKey);

        Trigger newTrigger = existingTriggerBuilder.
                withSchedule(simpleSchedule()
                        .withIntervalInMinutes(interval).
                        repeatForever())
                .usingJobData(jobDataMap)
                .build();

        scheduler.rescheduleJob(existingTrigger.getKey(), newTrigger);

        log.debug("Successfully defined Jobs for the tenants {} ", scheduler.getJobKeys(groupEquals(CONSENT_GROUP)));
        log.debug("Successfully defined Triggers for the tenants {} ", scheduler.getTriggerKeys(groupEquals(CONSENT_GROUP)));
    }
}