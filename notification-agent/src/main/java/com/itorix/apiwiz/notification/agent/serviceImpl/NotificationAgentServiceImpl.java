package com.itorix.apiwiz.notification.agent.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.notification.agent.dao.NotificationAgentExecutorSQLDao;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;
import com.itorix.apiwiz.notification.agent.model.RequestModel;
import com.itorix.apiwiz.notification.agent.service.NotificationAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class NotificationAgentServiceImpl implements NotificationAgentService {

    private static final String TENANT_ID = "tenantId";

    @Autowired
    NotificationAgentExecutorSQLDao executorSQLDao;

    @Autowired
    HttpServletRequest request;

    @Value("${includeAgentPort:true}")
    boolean includeAgentPort;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResponseEntity<?> createNotification(@RequestHeader HttpHeaders headers, @RequestBody RequestModel model)
            throws Exception {

        String tenantId =  headers.getFirst(TENANT_ID);

        executorSQLDao.insertIntoTestExecutorEntity(model.getType().name(),
                mapper.writeValueAsString(model.getEmailContent()),
                NotificationExecutorEntity.STATUSES.SCHEDULED.getValue(), tenantId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
