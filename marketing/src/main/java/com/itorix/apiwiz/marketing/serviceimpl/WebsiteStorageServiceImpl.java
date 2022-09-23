package com.itorix.apiwiz.marketing.serviceimpl;

import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.marketing.service.WebsiteStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin
@RestController
public class WebsiteStorageServiceImpl implements WebsiteStorageService {

    @Autowired
    S3Utils s3Utils;

    @Override
    public ResponseEntity<?> uploadImage(String interactionid, String jsessionid, MultipartFile file) throws Exception {
        String downloadUri= s3Utils.uploadWebsiteResource(file.getName()+System.currentTimeMillis(),file.getInputStream());
        return new ResponseEntity<>(downloadUri,HttpStatus.OK);
    }
}
