package com.itorix.apiwiz.common.factory;


import com.itorix.apiwiz.common.model.integrations.gcs.GcsIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.Gcs.GcsUtils;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class IntegrationHelper {
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

    public StorageIntegration getIntegration(){
        GcsIntegration gcsIntegration = gcsUtils.getGcsIntegration();
        S3Integration s3Integration = s3Connection.getS3Integration();
        if (gcsIntegration != null){
            return gcsUtils;
        }else if(s3Integration != null){
            return s3Utils;
        }else {
            return jfrogUtil;
        }
    }
    public StorageIntegration getIntegration(String type){
        if (type.equalsIgnoreCase("s3")){
            return s3Utils;
        }else if(type.equalsIgnoreCase("gcs")){
            return gcsUtils;
        }else if(type.equalsIgnoreCase("jfrog")){
            return jfrogUtil;
        }
        return null;
    }
}
