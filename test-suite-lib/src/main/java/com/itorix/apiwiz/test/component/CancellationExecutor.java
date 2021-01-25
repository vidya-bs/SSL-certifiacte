package com.itorix.apiwiz.test.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.test.dao.TestSuitExecutorSQLDao;
import com.itorix.apiwiz.test.dao.TestSuiteExecutorDao;
import com.itorix.apiwiz.test.db.TestExecutorEntity;

@Component
public class CancellationExecutor {

	private final static Logger logger = LoggerFactory.getLogger(CancellationExecutor.class);

	@Autowired
	TestSuitExecutorSQLDao testSuitExecutorSQLDao;

	@Autowired
	TestSuiteExecutorDao testSuiteExecutorDao;

	private List<String> testSuitecancelledList = Collections.synchronizedList(new ArrayList<>());

	public boolean getAndRemoveTestSuiteCancellationId(String testSuiteResponseId) {
		if (testSuitecancelledList.contains(testSuiteResponseId)) {
			testSuitecancelledList.remove(testSuiteResponseId);
			logger.debug("cancelled scenario" + testSuiteResponseId);
			return true;
		}
		return false;

	}

	public void cancelTestSuite(String testSuiteResponseId) {
		if (!TestExecutorEntity.STATUSES.COMPLETED.getValue()
				.equals(testSuitExecutorSQLDao.getValueByColumnName(testSuiteResponseId, "status"))) {
			testSuitecancelledList.add(testSuiteResponseId);
		}
		testSuitExecutorSQLDao.updateStatusForTestExecutionId(testSuiteResponseId,
				TestExecutorEntity.STATUSES.CANCELLED.getValue());

		try {
			testSuiteExecutorDao.updateTestSuiteStatus(testSuiteResponseId, null,
					TestExecutorEntity.STATUSES.CANCELLED.getValue());
		} catch (Exception ex) {
			logger.error("Exception thrown when updaing status in cancelTestSuite", ex);
		}
	}
}
