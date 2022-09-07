package com.itorix.apiwiz.common.model.integrations.gcs;

public class GcsIntegration {
    private String projectId;
    private byte[] key;
    private String bucketName;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public String getProjectId() {
        return projectId;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
    public byte[] getKey() {
        return key;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    public String getBucketName() {
        return bucketName;
    }
}
