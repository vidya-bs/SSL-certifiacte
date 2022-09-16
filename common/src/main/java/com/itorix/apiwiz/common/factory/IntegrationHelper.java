package com.itorix.apiwiz.common.factory;


import com.itorix.apiwiz.common.model.integrations.gcs.GcsIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogConnection;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.gcs.GcsUtils;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class IntegrationHelper {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationHelper.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GcsUtils gcsUtils;

    @Autowired
    private S3Connection s3Connection;

    @Autowired
    private S3Utils s3Utils;

    @Autowired
    private JfrogUtilImpl jfrogUtil;

    @Autowired
    private JfrogConnection jfrogConnection;

    public StorageIntegration getIntegration(){
        GcsIntegration gcsIntegration = gcsUtils.getGcsIntegration();
        S3Integration s3Integration = s3Connection.getS3Integration();
        if (gcsIntegration != null) {
            logger.debug("Using GCS integration");
            return gcsUtils;
        } else if (s3Integration != null) {
            logger.debug("Using S3 integration");
            return s3Utils;
        } else {
            logger.debug("Using JFrog integration");
            return jfrogUtil;
        }
    }
    public StorageIntegration getIntegration(String type) {
        if (type.equalsIgnoreCase("s3")) {
            logger.debug("Using S3 integration");
            return s3Utils;
        } else if (type.equalsIgnoreCase("gcs")) {
            logger.debug("Using GCS integration");
            return gcsUtils;
        } else if (type.equalsIgnoreCase("jfrog")) {
            logger.debug("Using JFrog integration");
            return jfrogUtil;
        }
        return null;
    }

    public Boolean checkIntegration(String type) {
        logger.debug("checking if {} integration is available ...", type);
        if (type.equalsIgnoreCase("s3")) {
            if (s3Connection.getS3Integration() != null) {
                logger.debug("{} integration is available", type);
                return true;
            }
        } else if (type.equalsIgnoreCase("gcs")) {
            if (gcsUtils.getGcsIntegration() != null) {
                logger.debug("{} integration is available", type);
                return true;
            }
        } else if (type.equalsIgnoreCase("jfrog")) {
            if (jfrogConnection.getJfrogIntegration() != null) {
                logger.debug("{} integration is available", type);
                return true;
            }
        }
        logger.debug("{} integration is not available", type);
        return false;
    }
}