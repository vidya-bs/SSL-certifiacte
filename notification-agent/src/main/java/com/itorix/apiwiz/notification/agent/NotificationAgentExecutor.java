package com.itorix.apiwiz.notification.agent;

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

import com.itorix.apiwiz.notification.agent.dao.NotificationAgentExecutorSQLDao;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;
import com.itorix.apiwiz.notification.agent.executor.NotificationAgentRunner;
import com.itorix.apiwiz.notification.agent.model.ExecutionContext;

@Configuration
@EnableAutoConfiguration
@Component
public class NotificationAgentExecutor {

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

    @Autowired
    private NotificationAgentExecutorSQLDao sqlDao;

    @Autowired
    private NotificationAgentRunner testRunner;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Scheduled(fixedRate = 15000)
    public void fetchAndRunTestsuites() {

        int avlThreads = (Runtime.getRuntime().availableProcessors() * 5)
                - ((ThreadPoolExecutor) executor).getActiveCount();

        List<NotificationExecutorEntity> executorEntities = sqlDao.getExecutorEntityByColumn("status",
                NotificationExecutorEntity.STATUSES.SCHEDULED.getValue(), avlThreads);

        for (NotificationExecutorEntity executorEntity : executorEntities) {
            executorEntity.setStatus(NotificationExecutorEntity.STATUSES.IN_PROGRESS.getValue());
            sqlDao.updateField(executorEntity.getId(), "status",
                    NotificationExecutorEntity.STATUSES.IN_PROGRESS.getValue());
            ExecutionContext context = new ExecutionContext();
            context.setNotificationExecutorEntity(executorEntity);
            executor.execute(() -> testRunner.run(context));
        }
    }
}