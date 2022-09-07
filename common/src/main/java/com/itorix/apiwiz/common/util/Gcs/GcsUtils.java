package com.itorix.apiwiz.common.util.Gcs;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gcs.GcsIntegration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component("GCS")
public class GcsUtils extends StorageIntegration {

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
        return readFile(gcsIntegration, path);
    }

    @Override
    public void deleteFile (String path) throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        deleteFile(gcsIntegration, path);
    }

    public Integration getIntegration() {
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
            return integration.getGcsIntegration();
        }
        return null;
    }

    public Storage connectToStorage() throws Exception {
        GcsIntegration gcsIntegration = getGcsIntegration();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gcsIntegration.getKey());
        Credentials credentials = GoogleCredentials.fromStream(byteArrayInputStream);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(gcsIntegration.getProjectId()).build().getService();
        return storage;
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

    private String getBlobNameFromPath(String path) {
        List<String> temp = Arrays.stream(path.split("/")).collect(Collectors.toList());
        String filePath = String.join("/", temp.subList(8, temp.size()));
        return filePath;
    }

    public String uploadFile(GcsIntegration gcsIntegration, String path, InputStream data) throws Exception {
        Storage storage = connectToStorage();
        Bucket bucket = getBucket( gcsIntegration, storage);
        Blob blob = bucket.create(path, data);
        return blob.getSelfLink().concat("?type=gcs");
    }
    public String uploadFile(GcsIntegration gcsIntegration, String path, String filePath) throws Exception {
        Storage storage = connectToStorage();
        Bucket bucket = getBucket( gcsIntegration, storage);
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        Blob blob = bucket.create(path, inputStream);
        return blob.getSelfLink().concat("?type=gcs");
    }

    private InputStream readFile(GcsIntegration gcsIntegration, String path) throws Exception {
        Storage storage = connectToStorage();
        String filePath = getBlobNameFromPath(path);
        Bucket bucket = storage.get(gcsIntegration.getBucketName());
        Blob blob = bucket.get(filePath);
        return new ByteArrayInputStream(blob.getContent());
    }
    private void deleteFile ( GcsIntegration gcsIntegration, String path) throws Exception {
        Storage storage = connectToStorage();
        String filePath = getBlobNameFromPath(path);
        Bucket bucket = storage.get(gcsIntegration.getBucketName());
        Blob blob = bucket.get(filePath);
        blob.delete();
    }
}
