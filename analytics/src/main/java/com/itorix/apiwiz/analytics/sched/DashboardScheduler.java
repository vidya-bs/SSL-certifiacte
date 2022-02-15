package com.itorix.apiwiz.analytics.sched;

import com.itorix.apiwiz.analytics.beans.PessimisticLock;
import com.itorix.apiwiz.analytics.businessImpl.LandingPageStatsImpl;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class DashboardScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardScheduler.class);

    private static final String KEEPER_ID = UUID.randomUUID().toString();

    private static final String JOB_NAME = "generate_dashboard";

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoProperties mongoProperties;

    @Autowired
    private LandingPageStatsImpl landingPageStatsImpl;


    public boolean acquireLock() {
        Query query = Query.query(Criteria.where("_id").is(JOB_NAME).and("keeperId").is(KEEPER_ID));
        try {
            UpdateResult upsert = masterMongoTemplate.upsert(query, Update.update("ts", new Date()), PessimisticLock.class);
            if(upsert.getUpsertedId() != null || upsert.getModifiedCount() > 0) {
                return true;
            }
        } catch(DuplicateKeyException de) {
            return false;
        }catch (Exception ex) {
            LOGGER.error("Error while acquiring lock ", ex);
        }
        return false;
    }


    public void releaseLock() {
        masterMongoTemplate.setWriteConcern(WriteConcern.MAJORITY);
        Query query = Query.query(Criteria.where("_id").is(JOB_NAME).and("keeperId").is(KEEPER_ID));
        masterMongoTemplate.remove(query, PessimisticLock.class);
    }


    @Scheduled(cron = "0/30 * * * * *")
    public void createDashBoard() {
        if (acquireLock()) {
            LOGGER.info("Acquired the lock!! Creating Dashboard {} ", KEEPER_ID);

            try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
                MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
                while (dbsCursor.hasNext()) {
                    String tenantId = dbsCursor.next();
                    TenantContext.setCurrentTenant(tenantId);
                    landingPageStatsImpl.generateWorkspaceDashboard("SYSTEM");
                    List<User> users = masterMongoTemplate.find(Query.query(Criteria.where("workspaces.workspace._id").in(Arrays.asList(tenantId))), User.class);
                    for (User user : users) {
                        landingPageStatsImpl.generateWorkspaceDashboard(user.getUserId());
                    }
                }
            }
            LOGGER.info("Dashboard created");
            releaseLock();
            LOGGER.info("Released Lock ");
        } else {
            LOGGER.info("I'm a follower {} ", KEEPER_ID);
        }
    }

}
