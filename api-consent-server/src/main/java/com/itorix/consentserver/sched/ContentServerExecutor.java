package com.itorix.consentserver.sched;


import com.itorix.consentserver.model.Consent;
import com.itorix.consentserver.model.ConsentStatus;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContentServerExecutor {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void expireConsents() {
        log.debug("Expiring consents");
        Update update = new Update();
        update.set("consent.status", ConsentStatus.Expired);
        Query query = Query.query(Criteria.where("expirationTimeInMillis").lte(System.currentTimeMillis()));
        UpdateResult result = mongoTemplate.updateMulti(query, update, Consent.class);
        log.debug("Expired {} number of consents " , result.getModifiedCount());
    }
}
