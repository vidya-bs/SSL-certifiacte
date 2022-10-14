package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.marketing.downloads.model.AppDownloadModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AppDownloadDao {

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    public ResponseEntity<?> postDownload(AppDownloadModel appDownloadModel) {
        AppDownloadModel returnedData = masterMongoTemplate.save(appDownloadModel);
        return new ResponseEntity<>(returnedData, HttpStatus.CREATED);
    }

    public ResponseEntity<?> getDownloads() {
        List<AppDownloadModel>downloadsList = masterMongoTemplate.findAll(AppDownloadModel.class);
        return new ResponseEntity<>(downloadsList,HttpStatus.OK);
    }
}
