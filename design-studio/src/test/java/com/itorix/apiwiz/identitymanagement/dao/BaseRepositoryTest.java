package com.itorix.apiwiz.identitymanagement.dao;

import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.itorix.apiwiz.identitymanagement.security.MongoDbConfiguration;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jdk.nashorn.internal.ir.ObjectNode;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;

public class BaseRepositoryTest {

    private static MongoTemplate mongoTemplate = null;

    @BeforeClass
    public static void init() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClients.create(mongoClientSettings);
        mongoTemplate = new MongoTemplate(MongoClients.create(mongoClientSettings), "acme-team-dev");
    }

    @Test
    public void checkFiltersOnSwaggerList() {
        String swaggerCollName = mongoTemplate.getCollectionName(SwaggerVO.class);
        MongoCollection<Document> swaggerDocs = mongoTemplate.getCollection(swaggerCollName);

        ProjectionOperation projectRequiredFields = project("name", "status", "mts").
                andExpression("toDate(mts)").as("mtsToDate");

        ProjectionOperation dateToString = Aggregation.project("name", "status")
                .and("mtsToDate")
                .dateAsFormattedString("%m%d%Y")
                .as("mtsToDate");

        GroupOperation groupByName = group("name");

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        Map<String, Object> matchFields = new LinkedHashMap<>();
        matchFields.put("status", "Draft");
        //matchFields.put("mtsToDate", "05142021");
        matchFields.forEach((k, v) -> {
            criteriaList.add(Criteria.where(k).is(v));
            });

        //Criteria criteria = Criteria.where("status").is("Draft").and("mtsToDate").is("05142021");
        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        MatchOperation match = match(criteria);


        AggregationResults<Document> results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, dateToString, match, groupByName), SwaggerVO.class, Document.class);
        System.out.println(results.getMappedResults());

        List<String> names = new ArrayList<>();
        results.getMappedResults().forEach( d -> names.add(d.getString("_id")));
        System.out.println(names);


    }




}