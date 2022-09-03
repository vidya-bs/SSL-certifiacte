package com.itorix.apiwiz.consent.management.sched;

import com.itorix.apiwiz.consent.management.dao.ConsentManagementDao;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;

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
				String tenantId = dbsCursor.next();
				TenantContext.setCurrentTenant(tenantId);
				Integer interval = consentManagementDao.getConsentExpirationInterval();
				if (interval != null) {
					scheduleTrigger(tenantId, interval);
				}
			}
		}
		log.debug("Successfully defined Triggers for the tenants {} ",
				scheduler.getTriggerKeys(groupEquals(CONSENT_GROUP)));

	}

	private void scheduleTrigger(String tenantId, Integer interval) throws SchedulerException {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("tenantId", tenantId);

		JobDetail jobDetail = buildJobDetail(tenantId);
		Trigger trigger = buildJobTrigger(jobDetail, jobDataMap, interval);
		scheduler.scheduleJob(jobDetail, trigger);
	}

	private JobDetail buildJobDetail(String tenantId) {
		return JobBuilder.newJob(ConsentSchedulerJob.class).withIdentity(tenantId, CONSENT_GROUP)
				.withDescription("Consent Job").storeDurably().build();
	}

	private Trigger buildJobTrigger(JobDetail jobDetail, JobDataMap jobDataMap, int interval) {
		return TriggerBuilder.newTrigger().forJob(jobDetail).usingJobData(jobDataMap)
				.withIdentity(jobDetail.getKey().getName(), CONSENT_GROUP).withDescription("Consent Trigger")
				.withSchedule(simpleSchedule().withIntervalInMinutes(interval).repeatForever()).build();
	}

	@SneakyThrows
	public void updateTrigger(String tenantId, int interval) {
		Trigger existingTrigger = scheduler.getTrigger(TriggerKey.triggerKey(tenantId, CONSENT_GROUP));

		if (existingTrigger != null) {
			long repeatInterval = ((SimpleTrigger) existingTrigger).getRepeatInterval();

			long toMillis = TimeUnit.MINUTES.toMillis(interval);

			if (repeatInterval != toMillis) {
				TriggerBuilder existingTriggerBuilder = existingTrigger.getTriggerBuilder();

				Trigger newTrigger = existingTriggerBuilder
						.withSchedule(simpleSchedule().withIntervalInMinutes(interval).repeatForever()).build();

				scheduler.rescheduleJob(existingTrigger.getKey(), newTrigger);

				log.debug("Successfully rescheduled trigger for the tenant {}", tenantId);
			} else {
				log.debug("Consent expiry interval hasn't changed. Skipping reschedule of trigger for the tenant {}",
						tenantId);
			}

		} else {
			log.debug(
					"Trigger wasn't initialized during start up. Scheduling trigger during update check for tenant {} ",
					tenantId);
			scheduleTrigger(tenantId, interval);
		}

	}

	@Scheduled(cron = "0 0/30 * * * *")
	public void updateTriggerInterval() {
		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
			while (dbsCursor.hasNext()) {
				String currentTenant = dbsCursor.next();
				TenantContext.setCurrentTenant(currentTenant);
				Integer interval = consentManagementDao.getConsentExpirationInterval();
				if (interval != null) {
					updateTrigger(currentTenant, interval);
				} else {
					log.debug("No consent expiry interval found. Skipping reschedule of trigger for the tenant {}",
							currentTenant);
				}
			}
		}
	}

}