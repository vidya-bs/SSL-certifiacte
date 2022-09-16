package com.itorix.apiwiz.common.util.gcs;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gcs.GcsIntegration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;

@Primary
@Component("GCS")
public class GcsUtils extends StorageIntegration {

    private static final Logger logger = LoggerFactory.getLogger(GcsUtils.class);

    @Value("${itorix.core.application.url:}")
    private String host;

    @Value("${server.contextPath:}")
    private String context;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public String uploadFile(String path, String data) throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        return uploadFile(gcsIntegration, path, data);
    }

    @Override
    public String uploadFile(String path, InputStream data) throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        return uploadFile(gcsIntegration, path, data);
    }
    @Override
    public InputStream getFile(String path) throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        return readFile(gcsIntegration, URLDecoder.decode(path, "UTF-8"));
    }

    @Override
    public void deleteFile (String path) throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        deleteFile(gcsIntegration, URLDecoder.decode(path, "UTF-8"));
    }

    public Integration getIntegration() {
        logger.debug("Getting GCS integration...");
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is("GCS"));
        List<Integration> integrationList = mongoTemplate.find(query, Integration.class);
        if (integrationList != null && integrationList.size() > 0) {
            return integrationList.get(0);
        }
        return null;
    }
    public GcsIntegration getGcsIntegration() {
        Integration integration = getIntegration();
        if (integration != null) {
            logger.debug("GCS Integration found");
            return integration.getGcsIntegration();
        }
        logger.debug("GCS Integration not found");
        return null;
    }

    public Storage connectToStorage() throws Exception {
        logger.debug("Connecting to GCS storage...");
        try {
            GcsIntegration gcsIntegration = getGcsIntegration();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gcsIntegration.getKey());
            Credentials credentials = GoogleCredentials.fromStream(byteArrayInputStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(gcsIntegration.getProjectId()).build().getService();
            return storage;
        } catch (Exception e){
            logger.error("Error connecting to GCS storage : {}");
            throw new ItorixException("Error connecting to GCS storage", "General-1000");
        }
    }
    public Bucket getBucket(GcsIntegration gcsIntegration, Storage storage) throws Exception {
        try {
            Bucket bucket = storage.get(gcsIntegration.getBucketName());
            return bucket;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public Bucket createBucket(String bucketName) throws Exception {
        logger.debug("Creating bucket in GCS: {}", bucketName);
        Integration integration = getIntegration();
        GcsIntegration gcsIntegration = null;
        if (integration != null && integration.getGcsIntegration() != null)
            gcsIntegration = integration.getGcsIntegration();
        Storage storage = connectToStorage();
        try {
            Bucket bucket = storage.create(BucketInfo.newBuilder(bucketName).build());
            gcsIntegration.setBucketName(bucketName);
            integration.setGcsIntegration(gcsIntegration);
            mongoTemplate.save(integration);
            return bucket;
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    public String uploadFile(GcsIntegration gcsIntegration, String path, InputStream data) throws Exception {
        logger.debug("Upload file to GCS: {}", path);
        Storage storage = connectToStorage();
        Bucket bucket = getBucket( gcsIntegration, storage);
        Blob blob = bucket.create(path, data);
        return getURL(path);
    }
    public String uploadFile(GcsIntegration gcsIntegration, String path, String filePath) throws Exception {
        logger.debug("Upload file to GCS: {}", path);;
        Storage storage = connectToStorage();
        Bucket bucket = getBucket( gcsIntegration, storage);
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        Blob blob = bucket.create(path, inputStream);
        return getURL(path);
    }

    private InputStream readFile(GcsIntegration gcsIntegration, String path) throws Exception {
        logger.debug("get file from GCS: {}", path);
        Storage storage = connectToStorage();
        Bucket bucket = storage.get(gcsIntegration.getBucketName());
        Blob blob = bucket.get(path);
        return new ByteArrayInputStream(blob.getContent());
    }
    private void deleteFile ( GcsIntegration gcsIntegration, String path) throws Exception {
        logger.debug("delete file in GCS: {}", path);
        Storage storage = connectToStorage();
        Bucket bucket = storage.get(gcsIntegration.getBucketName());
        Blob blob = bucket.get(path);
        try {
            blob.delete();
            logger.debug("File deleted from GCS");
        } catch (Exception e) {
            logger.error("File not deleted from GCS");
            logger.error(e.getMessage());
        }
    }

    private String getURL(String key) {
        String URL = "<host><context>/v1/download/<key>?type=gcs";

        URL = URL.replaceAll("<host>", host).replaceAll("<context>", context).replaceAll("<key>", key);
        return URL;
    }
}