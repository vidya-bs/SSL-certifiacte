package com.itorix.apiwiz.monitor.agent.serviceImpl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.db.MonitorAgentExecutorEntity;
import com.itorix.apiwiz.monitor.agent.executor.exception.ItorixException;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.monitor.agent.executor.model.TenantContext;
import com.itorix.apiwiz.monitor.agent.service.MonitorAgentService;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;

@CrossOrigin
@RestController
public class MonitorAgentServiceImpl implements MonitorAgentService {

	private static final String TENANT_ID = "tenantId";

	private static final Logger log = LoggerFactory.getLogger(MonitorAgentServiceImpl.class);

	@Autowired
	MonitorAgentExecutorSQLDao executorSQLDao;

	@Autowired
	MonitorAgentExecutorDao testSuiteExecutorDao;

	@Autowired
	HttpServletRequest request;

	@Value("${includeAgentPort:true}")
	boolean includeAgentPort;

	@Override
	public ResponseEntity<?> storeMonitorDetails(@RequestHeader HttpHeaders headers,
			@RequestBody Map<String, String> requestBody) throws Exception {

		String collectionId = requestBody.get("collectionId");
		String schedulerId = requestBody.get("schedulerId");
		if (!StringUtils.hasText(collectionId) || !StringUtils.hasText(schedulerId)) {
			log.error("collectionId or schedulerId is empty");
			throw new ItorixException(ErrorCodes.errorMessage.get("MonitorAgent-5"), "MonitorAgent-5");
		}

		String tenantName = ((String) headers.getFirst(TENANT_ID));

		if (StringUtils.hasText(tenantName)) {
			TenantContext.setCurrentTenant(tenantName);
			MonitorCollections collection = testSuiteExecutorDao.getMonitorCollections(collectionId, schedulerId);
			if (collection == null) {
				log.error("There is no entry found for collectionId or schedulerId {} , {}", collectionId, schedulerId);
				throw new ItorixException(ErrorCodes.errorMessage.get("MonitorAgent-8"), "MonitorAgent-8");
			}

			// String host =
			// request.getRequestURL().substring(0,request.getRequestURL().indexOf(request.getContextPath())+request.getContextPath().length());
			executorSQLDao.insertIntoTestExecutorEntity(tenantName, collectionId, schedulerId,
					MonitorAgentExecutorEntity.STATUSES.SCHEDULED.getValue());
			return new ResponseEntity<>(HttpStatus.OK);
		}

		log.error("Couldn't find tenant");
		throw new ItorixException(ErrorCodes.errorMessage.get("MonitorAgent-4"), "MonitorAgent-4");
	}
}