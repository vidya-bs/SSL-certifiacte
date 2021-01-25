package com.itorix.apiwiz.monitor.agent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.db.MonitorAgentExecutorEntity;
import com.itorix.apiwiz.monitor.agent.executor.MonitorAgentRunner;
import com.itorix.apiwiz.monitor.agent.executor.model.TenantContext;
import com.itorix.apiwiz.monitor.model.ExecutionContext;

@Configuration
@EnableAutoConfiguration
@Component
public class MonitorAgentExecutor {

	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

	@Autowired
	private MonitorAgentExecutorDao dao;

	@Autowired
	private MonitorAgentExecutorSQLDao sqlDao;

	@Autowired
	private MonitorAgentRunner testRunner;

	@Autowired
	JdbcTemplate jdbcTemplate;



	@Scheduled(fixedRate = 15000)
	public void fetchAndRunTestsuites() {

		int avlThreads = (Runtime.getRuntime().availableProcessors() * 5)
				- ((ThreadPoolExecutor) executor).getActiveCount();

		List<MonitorAgentExecutorEntity> executorEntities = sqlDao.getExecutorEntityByColumn("status",
				MonitorAgentExecutorEntity.STATUSES.SCHEDULED.getValue(), avlThreads);

		for (MonitorAgentExecutorEntity testExecutorEntity : executorEntities) {
			TenantContext.setCurrentTenant(testExecutorEntity.getTenant());
			ExecutionContext context = new ExecutionContext();
			context.setExecutionId(testExecutorEntity.getId());
			context.setTenant(testExecutorEntity.getTenant());
			context.setCollectionId(testExecutorEntity.getCollectionId());
			context.setSchedulerId(testExecutorEntity.getSchedulerId());
			testExecutorEntity.setStatus(MonitorAgentExecutorEntity.STATUSES.IN_PROGRESS.getValue());
			sqlDao.updateField(testExecutorEntity.getId(), "status",
					MonitorAgentExecutorEntity.STATUSES.IN_PROGRESS.getValue());
			executor.execute(() -> testRunner.run(context));
		}
	}
}