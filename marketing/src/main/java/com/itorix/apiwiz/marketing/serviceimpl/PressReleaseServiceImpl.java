package com.itorix.apiwiz.marketing.serviceimpl;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.common.PaginatedResponse;
import com.itorix.apiwiz.marketing.dao.PressReleaseDao;
import com.itorix.apiwiz.marketing.pressrelease.model.PressRelease;
import com.itorix.apiwiz.marketing.pressrelease.model.PressReleaseStatus;
import com.itorix.apiwiz.marketing.service.PressReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@Slf4j
public class PressReleaseServiceImpl implements PressReleaseService {

    @Autowired
    PressReleaseDao pressReleaseDao;

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> createPressRelease(String apikey,PressRelease pressRelease) throws Exception {
        log.info("creating press release : {}",pressRelease);
        PressRelease returnedData = pressReleaseDao.createRelease(pressRelease);
        if (returnedData != null)
            return new ResponseEntity<>(pressRelease, HttpStatus.CREATED);
        else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> editPressRelease(String apikey,String releaseId, PressRelease pressRelease) throws Exception {
        log.info("updating press release : {}",pressRelease);
        return pressReleaseDao.updateRelease(pressRelease,releaseId);
    }
    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> changeStatus(String apikey, String releaseId, PressReleaseStatus status) throws Exception {
        if (status.name().equals("DRAFT")) {
            return ResponseEntity.noContent().build();
        }
        return pressReleaseDao.changeStatus(releaseId, status);
    }
    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> getPressReleaseData(String apikey,int offset,int pageSize) throws Exception {
        PaginatedResponse response = new PaginatedResponse();
        response.setPagination(pressReleaseDao.getPagination(offset,pageSize));
        response.setData(pressReleaseDao.getPressReleases());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> getDataByFilter(String apikey,int offset, int pageSize, String filter,String filterValue) throws Exception {
        List<PressRelease>filteredData = pressReleaseDao.getDataByFilter(filter,filterValue);
        PaginatedResponse response = new PaginatedResponse();
        response.setPagination(pressReleaseDao.getPaginationForFilter(offset,pageSize,filteredData.size()));
        response.setData(filteredData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> deletePressRelease(String apikey,String releaseId) throws Exception {
        if (pressReleaseDao.deletePressRelease(releaseId) != null)
            return ResponseEntity.noContent().build();

        return ResponseEntity.notFound().build();
    }
}
