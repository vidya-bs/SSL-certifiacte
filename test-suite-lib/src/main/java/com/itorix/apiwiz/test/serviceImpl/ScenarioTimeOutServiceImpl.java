package com.itorix.apiwiz.test.serviceImpl;

import com.itorix.apiwiz.test.dao.ScenarioTimeOutDao;
import com.itorix.apiwiz.test.executor.beans.ScenarioTimeOut;
import com.itorix.apiwiz.test.service.ScenarioTimeOutService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Object> createTimeOut(String jsessionid, ScenarioTimeOut requestBody, String testsuiteId) throws Exception {
        ScenarioTimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(testsuiteId);
        if(existingTimeout != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        scenarioTimeOutDao.createTimeOut(requestBody,testsuiteId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateTimeOut(String jsessionid, ScenarioTimeOut requestBody, String testsuiteId) throws Exception {
        ScenarioTimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(testsuiteId);
        if(existingTimeout == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        scenarioTimeOutDao.updateTimeOut(requestBody,testsuiteId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteTimeOut(String jsessionid,String testsuiteId) throws Exception {
        ScenarioTimeOut existingTimeout = scenarioTimeOutDao.getExistingTimeOut(testsuiteId);
        if(existingTimeout != null){
            scenarioTimeOutDao.deleteTimeOut(testsuiteId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ScenarioTimeOut getTimeOut(String jsessionId, String testsuiteId) throws Exception {
        return scenarioTimeOutDao.getExistingTimeOut(testsuiteId);
    }
}
