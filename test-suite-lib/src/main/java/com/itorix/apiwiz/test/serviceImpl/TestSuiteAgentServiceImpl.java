package com.itorix.apiwiz.test.serviceImpl;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.test.component.CancellationExecutor;
import com.itorix.apiwiz.test.dao.TestSuitExecutorSQLDao;
import com.itorix.apiwiz.test.dao.TestSuiteExecutorDao;
import com.itorix.apiwiz.test.db.TestExecutorEntity;
import com.itorix.apiwiz.test.executor.beans.TestSuiteResponse;
import com.itorix.apiwiz.test.executor.exception.ItorixException;
import com.itorix.apiwiz.test.executor.model.ErrorCodes;
import com.itorix.apiwiz.test.executor.model.TenantContext;
import com.itorix.apiwiz.test.service.TestSuiteAgentService;

@CrossOrigin
@RestController
public class TestSuiteAgentServiceImpl implements TestSuiteAgentService {

	private static final Logger log = LoggerFactory.getLogger(TestSuiteAgentServiceImpl.class);

	@Autowired
	TestSuitExecutorSQLDao executorSQLDao;

	@Autowired
	TestSuiteExecutorDao testSuiteExecutorDao;

	@Autowired
	CancellationExecutor cancellationExecutor;

	@Autowired HttpServletRequest request;

	@Value("${includeAgentPort:true}")
	boolean includeAgentPort;

	@Override
	public ResponseEntity<?> storeExecutionId(@RequestBody Map<String, String> requestBody) throws Exception {

		String executionId = requestBody.get("testSuiteExecutionId");
		if (!StringUtils.hasText(executionId)) {
			log.error("testSuiteExecutionId is empty");
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-5"), "TestSuiteAgent-5");
		}

		TestSuiteResponse testSuiteResponse = testSuiteExecutorDao.getTestSuiteResponseById(executionId);
		if (testSuiteResponse == null) {
			log.error("There is no entry found in TestSuiteResponse for executionId " + executionId);
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-8"), "TestSuiteAgent-8");
		}

		List<TestExecutorEntity> executionEntity = executorSQLDao.getExecutorEntityByColumn("testSuiteExecutionId",
				executionId, 1);
		if (!executionEntity.isEmpty()) {
			log.error("A request is already processed for executionId");
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-6"), "TestSuiteAgent-6");
		}

		String tenantName = TenantContext.getCurrentTenant();
		if (StringUtils.hasText(tenantName)) {
			String agent;
			if(includeAgentPort){
				agent = InetAddress.getLocalHost().getHostName() + ":" + request.getServerPort();
			} else {
				agent = InetAddress.getLocalHost().getHostName();
			}
			//String host  = request.getRequestURL().substring(0,request.getRequestURL().indexOf(request.getContextPath())+request.getContextPath().length());
			executorSQLDao.insertIntoTestExecutorEntity(tenantName, executionId,
					TestExecutorEntity.STATUSES.SCHEDULED.getValue());
			testSuiteExecutorDao.updateTestSuiteField(executionId,"testSuiteAgent",agent);
			return new ResponseEntity<>(HttpStatus.OK);
		}

		log.error("Couldn't find tenant");
		throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-4"), "TestSuiteAgent-4");
	}

	@Override
	public ResponseEntity<?> cancelExecution(@RequestBody Map<String, String> requestBody) throws Exception {
		String executionId = requestBody.get("testSuiteExecutionId");
		if (!StringUtils.hasText(executionId)) {
			log.error("testSuiteExecutionId is empty");
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-5"), "TestSuiteAgent-5");
		}

		TestSuiteResponse testSuiteResponse = testSuiteExecutorDao.getTestSuiteResponseById(executionId);
		if (testSuiteResponse == null) {
			log.error("There is no entry found in TestSuiteResponse for executionId " + executionId);
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-8"), "TestSuiteAgent-8");
		}

		cancellationExecutor.cancelTestSuite(executionId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}