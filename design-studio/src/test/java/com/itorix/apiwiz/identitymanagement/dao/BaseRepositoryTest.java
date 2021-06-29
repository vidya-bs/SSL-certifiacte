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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.itorix.apiwiz.identitymanagement.model.Constants.SWAGGER_PROJECTION_FIELDS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        ProjectionOperation projectRequiredFields = project(SWAGGER_PROJECTION_FIELDS).
                andExpression("toDate(mts)").as("mtsToDate");

        ProjectionOperation dateToString = Aggregation.project(SWAGGER_PROJECTION_FIELDS)
                .and("mtsToDate")
                .dateAsFormattedString("%m%d%Y")
                .as("modified_date");



        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        Map<String, Object> matchFields = new LinkedHashMap<>();
        matchFields.put("status", "Draft");
        matchFields.put("mtsToDate", null);

        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        SortOperation sortOperation = getSortOperation("asc");

        MatchOperation match = getMatchOperation(matchFields);
        AggregationResults<Document> results = null;

        if(match != null) {
            results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, dateToString, match, groupByName, sortOperation), SwaggerVO.class, Document.class);
        } else {
            results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, dateToString, groupByName, sortOperation), SwaggerVO.class, Document.class);
        }

        List test = null;

        results.getMappedResults().stream().forEach(System.out::println);

        List<String> names = new LinkedList<>();
        results.getMappedResults().stream().forEach( d -> names.add(d.getString("_id")));
        System.out.println(names);
    }

    private SortOperation getSortOperation(String sortByModifiedTS) {
        SortOperation sortOperation = null;
        if(sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("ASC")) {
            sortOperation = sort(Sort.Direction.ASC, "mts");
            groupByName = group("name").max("mts").as("mts");
        } else if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("DESC")) {
            sortOperation = sort(Sort.Direction.DESC, "mts");
        } else {
            sortOperation = sort(Sort.Direction.ASC, "name");
            groupByName = group("name").max("name").as("name");
        }

        return sortOperation;
    }

    private MatchOperation getMatchOperation(Map<String, Object> filterFieldsAndValues) {
        List<Criteria> criteriaList = new ArrayList<>();
        filterFieldsAndValues.forEach((k, v) -> {
            if (null != v) {
                criteriaList.add(Criteria.where(k).is(v));
            }
        });

        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
        return criteriaList.size() > 0 ? match(criteria) : null;
    }

    GroupOperation groupByName = group("name").max("mts").as("mts");
}