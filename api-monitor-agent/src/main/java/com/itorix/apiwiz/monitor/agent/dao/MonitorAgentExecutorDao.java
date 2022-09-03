package com.itorix.apiwiz.monitor.agent.dao;

import com.itorix.apiwiz.monitor.agent.executor.exception.ItorixException;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.monitor.agent.util.RSAEncryption;
import com.itorix.apiwiz.monitor.model.Certificates;
import com.itorix.apiwiz.monitor.model.NotificationDetails;
import com.itorix.apiwiz.monitor.model.Variables;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.monitor.model.collection.Schedulers;
import com.itorix.apiwiz.monitor.model.execute.ExecutionResult;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MonitorAgentExecutorDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${server.ssl.key-alias:null}")
    private String keyAlias;

    @Value("${server.ssl.key-store-password:null}")
    private String keyStorepassword;

    @Value("${server.ssl.key-password:null}")
    private String keypassword;

    @Value("${server.ssl.key-store:null}")
    private String keyStoreFilePath;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final Logger log = LoggerFactory.getLogger(MonitorAgentExecutorDao.class);

    public Variables getVariablesById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Variables var = mongoTemplate.findOne(query, Variables.class);
        return var;
    }

    public SSLConnectionSocketFactory getSSLConnectionFactory(String sslReference) throws ItorixException {
        Query query = new Query(Criteria.where("name").is(sslReference));
        Certificates certificate = mongoTemplate.findOne(query, Certificates.class);
        if (certificate != null) {
            log.debug("Getting SSL Connection factory");
            try (ByteArrayInputStream instream = new ByteArrayInputStream(certificate.getContent());) {

                Resource storeFile = resourceLoader.getResource(keyStoreFilePath);
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try {
                    trustStore.load(instream,
                            (new RSAEncryption()).decryptText(certificate.getPassword()).toCharArray());
                } catch (Exception e) {
                    log.error("Could not load certificate to truststore", e);
                    throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-1008004"), "Testsuite-1008004");
                }

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(storeFile.getInputStream(), keyStorepassword.toCharArray());

                return new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(trustStore, null)
                        .loadKeyMaterial(keyStore, keypassword.toCharArray()).build());

            } catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
                    | CertificateException | IOException e) {
                log.error("Could not load certificate to truststore", e);
                throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-1008004"), "Testsuite-1008004");
            }
        }
        return null;
    }

    public Schedulers getScheduler(String collectionId, String schedulerId) {

        Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
                Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));
        query.fields().include("schedulers.$");
        MonitorCollections find = mongoTemplate.findOne(query, MonitorCollections.class);
        if (find != null) {
            if (!CollectionUtils.isEmpty(find.getSchedulers())) {
                return find.getSchedulers().get(0);
            }
        }
        return null;
    }

    public List<MonitorRequest> getMonitorRequests(String collectionId) {
        MonitorCollections monitorCollections = mongoTemplate.findById(collectionId, MonitorCollections.class);
        if (monitorCollections != null) {
            log.debug("Getting monitor collections");
            return monitorCollections.getMonitorRequest();
        }
        return null;
    }

    public MonitorCollections getMonitorCollections(String collectionId, String schedulerId) {
        Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
                Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));
        query.fields().include("schedulers.$").include("notifications").include("name").include("createdBy");
        MonitorCollections collection = mongoTemplate.findOne(query, MonitorCollections.class);
        if (collection != null) {
            return collection;
        }
        return null;
    }

    public void createExecutionResult(ExecutionResult request) {
        request.setId(new ObjectId().toString());
        mongoTemplate.insert(request);
    }

    public List<String> getRequestSequence(String collectionId) {
        List<String> requestSequence = new ArrayList<>();
        Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
        query.fields().include("sequence");
        MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
        if (monitorCollection != null) {
            requestSequence = monitorCollection.getSequence();
        }
        return requestSequence;
    }

    public List<NotificationDetails> getNotificationDetails(String workSpace, String collectionId) {

        Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));

        query.fields().include("id").include("name").include("schedulers").include("monitorRequest.id")
                .include("monitorRequest.name").include("notifications");

        MonitorCollections monitor = mongoTemplate.findOne(query, MonitorCollections.class);
        List<NotificationDetails> notificationDetails = new ArrayList<>();

        if (monitor != null) {
            log.debug("Adding notification details");
            for (Schedulers scheduler : monitor.getSchedulers()) {
                NotificationDetails notificationDetail = new NotificationDetails();
                notificationDetail.setNotifications(monitor.getNotifications());
                notificationDetail.setEnvironmentName(scheduler.getEnvironmentName());
                notificationDetail.setSchedulerId(scheduler.getId());
                setDailyNotificationResult(notificationDetail, monitor.getId(), scheduler.getId());
                setAvegareNotificationResult(notificationDetail, monitor.getId(), scheduler.getId());
                notificationDetail.setCollectionname(monitor.getName());
                notificationDetail.setWorkspaceName(workSpace);
                notificationDetails.add(notificationDetail);
            }
        }

        return notificationDetails;
    }

    private void setDailyNotificationResult(NotificationDetails notificationDetails, String collectionId,
            String schedulerId) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = new Date(calendar.getTime().getTime());

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
        Date endDate = new Date(calendar.getTime().getTime());

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        notificationDetails.setDate(dateFormat.format(startDate));
        Criteria criteria = new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
                Criteria.where("collectionId").is(collectionId), Criteria.where("schedulerId").is(schedulerId),
                Criteria.where("executedTime").gte(startDate.getTime()).lt(endDate.getTime()));

        Aggregation aggForLatency = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("collectionId").avg("$latency").as("latency"));

        List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class)
                .getMappedResults();
        Aggregation aggForCount = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class)
                .getMappedResults();

        Criteria successCriteria = new Criteria().andOperator(criteria, Criteria.where("status").is("Success"));

        Aggregation aggForSuccess = Aggregation.newAggregation(Aggregation.match(successCriteria),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class)
                .getMappedResults();

        int uptime = 0;
        long latencyInt = 0l;
        int count = 0;
        int success = 0;

        Optional<Document> latencyDoc = latency.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (latencyDoc.isPresent()) {
            latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
        }

        Optional<Document> countOptional = countDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (countOptional.isPresent()) {
            count = countOptional.get().getInteger("count");
        }

        Optional<Document> successOptional = successDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (successOptional.isPresent()) {
            success = successOptional.get().getInteger("count");
        }

        if (success != 0 || count != 0) {
            uptime = Math.round(((float) success / count) * 100);
        }
        notificationDetails.setDailyLatency(latencyInt);
        notificationDetails.setDailyUptime(uptime);
    }

    private void setAvegareNotificationResult(NotificationDetails notificationDetails, String collectionId,
            String schedulerId) {

        Criteria criteria = new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
                Criteria.where("collectionId").is(collectionId), Criteria.where("schedulerId").is(schedulerId));

        Aggregation aggForLatency = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("collectionId").avg("$latency").as("latency"));

        List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class)
                .getMappedResults();
        Aggregation aggForCount = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class)
                .getMappedResults();

        Criteria successCriteria = new Criteria().andOperator(criteria, Criteria.where("status").is("Success"));

        Aggregation aggForSuccess = Aggregation.newAggregation(Aggregation.match(successCriteria),
                Aggregation.group("collectionId").count().as("count"));
        List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class)
                .getMappedResults();

        int uptime = 0;
        long latencyInt = 0l;
        int count = 0;
        int success = 0;

        Optional<Document> latencyDoc = latency.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (latencyDoc.isPresent()) {
            latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
        }

        Optional<Document> countOptional = countDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (countOptional.isPresent()) {
            count = countOptional.get().getInteger("count");
        }

        Optional<Document> successOptional = successDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
                .findFirst();
        if (successOptional.isPresent()) {
            success = successOptional.get().getInteger("count");
        }

        if (success != 0 || count != 0) {
            uptime = Math.round(((float) success / count) * 100);
        }
        notificationDetails.setAvgLatency(latencyInt);
        notificationDetails.setAvgUptime(uptime);
    }
}