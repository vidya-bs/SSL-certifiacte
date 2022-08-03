package com.itorix.apiwiz.test.dao;

import com.itorix.apiwiz.test.executor.beans.TimeOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


@Component
public class ScenarioTimeOutDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    public TimeOut getExistingTimeOut() {
        return mongoTemplate.findAll(TimeOut.class).get(0);
    }

    public void createTimeOut(TimeOut requestBody) {
        if(requestBody.getTestAgentType().equalsIgnoreCase("shared")){
            if(requestBody.getTimeout()>250 || requestBody.getTimeout()<0){
                requestBody.setTimeout(250);
            }
        }
        mongoTemplate.save(requestBody);
    }

    public void updateTimeOut(TimeOut requestBody) {
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
        mongoTemplate.updateMulti(new Query(),update,TimeOut.class);
    }

    public void deleteTimeOut() {
        mongoTemplate.remove(new Query(),TimeOut.class);
    }
}
