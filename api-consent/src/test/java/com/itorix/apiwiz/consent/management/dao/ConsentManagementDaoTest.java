package com.itorix.apiwiz.consent.management.dao;

import com.itorix.apiwiz.consent.management.model.Consent;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class ConsentManagementDaoTest {

    private static MongoTemplate mongoTemplate = null;

    @BeforeClass
    public static void init() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .build();
        MongoClients.create(mongoClientSettings);
        mongoTemplate = new MongoTemplate(MongoClients.create(mongoClientSettings), "acme-team-dev");
    }

    @Test
    public void checkConsentKeys() {
        ProjectionOperation projectionOperation = project().and(ObjectOperators.valueOf("consent").toArray()).as("consent");

        UnwindOperation unwindOperation = unwind("consent");

        GroupOperation groupOperation = group("consent.k");

        Aggregation aggregation = newAggregation(projectionOperation, unwindOperation, groupOperation);


        AggregationResults<Document> aggregationResult = mongoTemplate.aggregate(aggregation, Consent.class, Document.class);

        List<String> columnName = aggregationResult.getMappedResults().stream().map(d -> d.getString("_id")).collect(Collectors.toList());

        System.out.println(columnName);

    }
}