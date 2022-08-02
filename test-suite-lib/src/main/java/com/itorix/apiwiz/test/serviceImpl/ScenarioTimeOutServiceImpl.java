package com.itorix.apiwiz.test.serviceImpl;

import com.itorix.apiwiz.test.dao.ScenarioTimeOutDao;
import com.itorix.apiwiz.test.executor.beans.TimeOut;
import com.itorix.apiwiz.test.service.ScenarioTimeOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class ScenarioTimeOutServiceImpl implements ScenarioTimeOutService {

    @Autowired
    ScenarioTimeOutDao scenarioTimeOutDao;

    @Override
    public ResponseEntity<Object> createTimeOut(TimeOut requestBody) throws Exception {
        TimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(requestBody.getTenant());
        if(existingTimeout != null){
            return new ResponseEntity<Object>(HttpStatus.CONFLICT);
        }
        scenarioTimeOutDao.createTimeOut(requestBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateTimeOut(TimeOut requestBody) throws Exception {
        TimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(requestBody.getTenant());
        if(existingTimeout == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        scenarioTimeOutDao.updateTimeOut(requestBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteTimeOut(TimeOut requestBody) throws Exception {
        TimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(requestBody.getTenant());
        if(existingTimeout != null){
            scenarioTimeOutDao.deleteTimeOut(requestBody.getTenant());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public TimeOut getTimeOut(String tenant) throws Exception {
        return scenarioTimeOutDao.getExistingTimeOut(tenant);
    }
}
