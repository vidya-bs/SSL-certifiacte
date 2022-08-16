package com.itorix.apiwiz.test.dao;

import com.itorix.apiwiz.test.executor.beans.ScenarioTimeOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


@Component
public class ScenarioTimeOutDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    public ScenarioTimeOut getExistingTimeOut(String testsuiteId) {
        Query query = new Query(Criteria.where("testSuiteId").is(testsuiteId));
        return mongoTemplate.findOne(query, ScenarioTimeOut.class);
    }

    public void createTimeOut(ScenarioTimeOut requestBody, String testsuiteId) {
        if(requestBody.getTimeout()>250 || requestBody.getTimeout()<0){
            requestBody.setTimeout(250);
        }
        requestBody.setTestSuiteId(testsuiteId);
        mongoTemplate.save(requestBody);
    }

    public void updateTimeOut(ScenarioTimeOut requestBody, String testsuiteId) {
        Update update = new Update();
        if(requestBody.getTimeout()>250 || requestBody.getTimeout()<0){
            update.set("timeout",250);
        }
        else{
            update.set("timeout",requestBody.getTimeout());
        }
        update.set("testsuiteId",testsuiteId);
        mongoTemplate.updateMulti(new Query(),update, ScenarioTimeOut.class);
    }
    public void deleteTimeOut(String testsuiteId) {
        Query query = new Query(Criteria.where("testsuiteId").is(testsuiteId));
        mongoTemplate.findAndRemove(query, ScenarioTimeOut.class);
    }
}
