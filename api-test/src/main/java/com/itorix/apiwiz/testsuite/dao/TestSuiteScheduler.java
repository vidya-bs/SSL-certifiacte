package com.itorix.apiwiz.testsuite.dao;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.testsuite.model.TestSuiteSchedule;
import com.itorix.apiwiz.testsuite.serviceimpl.TestsuiteServiceImpl;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;

@Component
public class TestSuiteScheduler {
	@Autowired
	public MongoTemplate mongoTemplate;
	@Autowired
	public ApplicationProperties config;

	@Autowired
	TestsuiteServiceImpl testService;

	@Autowired
	TestSuiteDAO testSuiteExecutorService;

	@Autowired
	private MongoProperties mongoProperties;

	@Scheduled(cron = "0 */12 * * * *")
	public void triggerTestSuites()
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {

		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
			while (dbsCursor.hasNext()) {

				TenantContext.setCurrentTenant(dbsCursor.next());
				List<TestSuiteSchedule> schedules = mongoTemplate.findAll(TestSuiteSchedule.class);
				for (TestSuiteSchedule schedule : schedules) {
					boolean canExecuteTestSuite = false;
					if (schedule.getRecurrenceMode() != null) {
						int scheduleTime = 0;
						if (schedule.getScheduleTime() != null && schedule.getScheduleTime().equalsIgnoreCase("AM")) {
							scheduleTime = 0;
						} else {
							scheduleTime = 1;
						}
						if (schedule.getRecurrenceMode().equalsIgnoreCase("Daily")) {
							if (Calendar.getInstance().get(Calendar.AM_PM) == scheduleTime) {
								canExecuteTestSuite = true;
							}
						} else if (schedule.getRecurrenceMode().equalsIgnoreCase("Weekly")) {
							if (schedule.getDays() != null
									&& schedule.getDays().contains(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
									&& Calendar.getInstance().get(Calendar.AM_PM) == scheduleTime) {
								canExecuteTestSuite = true;
							}
						}

						Query query = new Query(
								Criteria.where("id").is(schedule.getId()).orOperator
								(Criteria.where("executedDay").ne(LocalDate.now().getDayOfMonth()) , Criteria.where("executedDay").
										exists(false)));

						Update update = new Update();
						update.set("executedDay", LocalDate.now().getDayOfMonth());
						if (canExecuteTestSuite && mongoTemplate.updateFirst(query, update , TestSuiteSchedule.class).getModifiedCount()!=0) {
							testService.triggerTestSuite(schedule.getTestSuiteId(), schedule.getConfigId(), null, null,
									null, true);
						}
					}
				}
			}
		}
	}
}