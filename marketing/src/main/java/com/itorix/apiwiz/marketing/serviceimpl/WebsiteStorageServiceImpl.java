package com.itorix.apiwiz.marketing.serviceimpl;

import com.amazonaws.regions.Regions;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.marketing.service.WebsiteStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin
@RestController
@Slf4j
public class WebsiteStorageServiceImpl implements WebsiteStorageService {

    @Autowired
    S3Utils s3Utils;

    @Autowired
    ApplicationProperties applicationProperties;

    @Override
    public ResponseEntity<?> uploadImage(String interactionid, String jsessionid, MultipartFile file) throws Exception {
        try {
            String awsBucketName = applicationProperties.getAwsMarketingBucketName();
            String awsRegion = applicationProperties.getAwsMarketingRegion();
            String awsKey = applicationProperties.getAwsMarketingKey();
            String awsSecret = applicationProperties.getAwsMarketingSecret();
            String downloadUri = s3Utils.uploadWebsiteResource(awsKey, awsSecret, Regions.fromName(awsRegion), awsBucketName, file.getName() + System.currentTimeMillis(), file.getInputStream());
            return new ResponseEntity<>(downloadUri, HttpStatus.OK);
        } catch (Exception ex){
            log.error("Exception occurred while uploading to s3 bucket - ", ex);
            throw new ItorixException(ErrorCodes.errorMessage.get("Marketing-1002"), "Marketing-1002");
        }
    }
}
