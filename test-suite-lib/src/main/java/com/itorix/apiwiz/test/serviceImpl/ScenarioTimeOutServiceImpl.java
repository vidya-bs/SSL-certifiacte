package com.itorix.apiwiz.test.serviceImpl;

import com.itorix.apiwiz.test.dao.ScenarioTimeOutDao;
import com.itorix.apiwiz.test.executor.beans.TimeOut;
import com.itorix.apiwiz.test.executor.model.TenantContext;
import com.itorix.apiwiz.test.service.ScenarioTimeOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class ScenarioTimeOutServiceImpl implements ScenarioTimeOutService {

    @Autowired
    ScenarioTimeOutDao scenarioTimeOutDao;

    @Override
    public ResponseEntity<Object> createTimeOut(String jsessionid,TimeOut requestBody) throws Exception {
        scenarioTimeOutDao.createTimeOut(requestBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateTimeOut(String jsessionid,TimeOut requestBody) throws Exception {
        TimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(TenantContext.getCurrentTenant());
        if(existingTimeout == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        scenarioTimeOutDao.updateTimeOut(requestBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteTimeOut(String jsessionid) throws Exception {
        TimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(TenantContext.getCurrentTenant());
        if(existingTimeout != null){
            scenarioTimeOutDao.deleteTimeOut();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public TimeOut getTimeOut(String jsessionId) throws Exception {
        return scenarioTimeOutDao.getExistingTimeOut(TenantContext.getCurrentTenant());
    }
}
