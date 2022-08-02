package com.itorix.apiwiz.test.dao;

import com.itorix.apiwiz.test.executor.beans.TimeOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


@Component
public class ScenarioTimeOutDao {

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    public TimeOut getExistingTimeOut(String tenant) {
        Query query = new Query(Criteria.where("tenant").is(tenant));
        return masterMongoTemplate.findOne(query,TimeOut.class);
    }

    public void createTimeOut(TimeOut requestBody) {
        if(requestBody.getTestAgentType().equalsIgnoreCase("shared")){
            if(requestBody.getTimeout()>250 || requestBody.getTimeout()<0){
                requestBody.setTimeout(250);
            }
        }
        masterMongoTemplate.save(requestBody);
    }

    public void updateTimeOut(TimeOut requestBody) {
        Query query = new Query(Criteria.where("tenant").is(requestBody.getTenant()));
        Update update = new Update();
        if(requestBody.getTestAgentType().equalsIgnoreCase("shared")){
            if(requestBody.getTimeout()>250 || requestBody.getTimeout()<0){
                update.set("timeout",250);
            }
            else{
                update.set("timeout",requestBody.getTimeout());
            }
        }
        else{
            update.set("timeout",requestBody.getTimeout());
        }
        update.set("enabled",requestBody.isEnabled());
        update.set("testAgentType",requestBody.getTestAgentType());
        masterMongoTemplate.updateMulti(query, update, TimeOut.class);
    }

    public void deleteTimeOut(String tenant) {
        Query query = new Query(Criteria.where("tenant").is(tenant));
        masterMongoTemplate.remove(query,TimeOut.class);
    }
}
