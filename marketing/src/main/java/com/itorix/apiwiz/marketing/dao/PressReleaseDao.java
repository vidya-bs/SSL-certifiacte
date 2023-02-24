package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.marketing.pressrelease.model.PressRelease;
import com.itorix.apiwiz.marketing.pressrelease.model.PressReleaseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PressReleaseDao {

    private  static final String DATE_FORMAT="dd-MM-yyyy";

    @Qualifier("masterMongoTemplate")
    @Autowired
    MongoTemplate masterMongoTemplate;

    public List<PressRelease> getDataByTagOrSlug(int offset, int pageSize, String filter, String filterValue) {
        log.info("Get pressrelease by category or tag or slug : {}", filterValue);
        List<PressRelease> returningList = new ArrayList<>();
        Query query = new Query();
        if (filter.equalsIgnoreCase("slug")) {
            query.addCriteria(Criteria.where("meta.slug").is(filterValue))
                    .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
            returningList.addAll(masterMongoTemplate.find(query, PressRelease.class));
        } else {
            query.addCriteria(
                            Criteria.where("meta.tags").elemMatch(Criteria.where("tagName").regex(filterValue, "i")))
                    .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
            returningList.addAll(masterMongoTemplate.find(query, PressRelease.class));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            returningList = returningList.stream().sorted((o1, o2) -> {
                LocalDate date = LocalDate.parse(o1.getMeta().getPublishingDate(), dateTimeFormatter);
                LocalDate date1 = LocalDate.parse(o2.getMeta().getPublishingDate(), dateTimeFormatter);
                return date1.compareTo(date);
            }).collect(Collectors.toList());
        }
        return returningList;
    }



    public List<PressRelease> getDataByYear(int offset, int pageSize, int year) {
        Query query = new Query().addCriteria(Criteria.where("meta.year").is(year))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        List<PressRelease> pressReleaseList = masterMongoTemplate.find(query, PressRelease.class);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        pressReleaseList = pressReleaseList.stream().sorted((o1, o2) -> {
            LocalDate date = LocalDate.parse(o1.getMeta().getPublishingDate(), dateTimeFormatter);
            LocalDate date1 = LocalDate.parse(o2.getMeta().getPublishingDate(), dateTimeFormatter);
            return date1.compareTo(date);
        }).collect(Collectors.toList());
        if (pressReleaseList != null) return pressReleaseList;
        return new ArrayList<>();
    }

    public List<PressRelease> getDataByFilter(int offset, int pageSize, String filter, String filterValue) {
        switch (filter) {
            case "year":
                return getDataByYear(offset, pageSize, Integer.parseInt(filterValue));
            case "tag":
            case "slug":
                return getDataByTagOrSlug(offset, pageSize, filter, filterValue);
        }
        return new ArrayList<>();
    }

    public List<PressRelease> getAllPressReleases(int offset, int pageSize,String status) {
        List<PressRelease> pressReleaseList = null;
        if (status == null) {
            List<PressRelease> existing = getPressReleases(offset, pageSize);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            existing = existing.stream().sorted((o1, o2) -> {
                LocalDate date = LocalDate.parse(o1.getMeta().getPublishingDate(), dateTimeFormatter);
                log.info("date:{} ", date);
                LocalDate date1 = LocalDate.parse(o2.getMeta().getPublishingDate(), dateTimeFormatter);
                log.info("date1:{} ", date1);
                return date1.compareTo(date);
            }).collect(Collectors.toList());

            if (existing.isEmpty()) {
                return new ArrayList<>();
            }
            return existing;
        } else {
            Query query = new Query();
            query.addCriteria(Criteria.where("meta.status").is(status))
                    .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
            pressReleaseList = masterMongoTemplate.find(query, PressRelease.class);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            pressReleaseList = pressReleaseList.stream().sorted((o1, o2) -> {
                LocalDate date = LocalDate.parse(o1.getMeta().getPublishingDate(), dateTimeFormatter);
                log.info("date:{} ", date);
                LocalDate date1 = LocalDate.parse(o2.getMeta().getPublishingDate(), dateTimeFormatter);
                log.info("date1:{} ", date1);
                return date1.compareTo(date);
            }).collect(Collectors.toList());
        }
        if (pressReleaseList.isEmpty()) {
            return new ArrayList<>();
        }
        return pressReleaseList;
    }

    public static String validDate(PressRelease pressRelease) {
        if (pressRelease.getMeta().getPublishingDate() == null) return pressRelease.getMeta().getPublishingDate();
        String validDate = null;
        String day = pressRelease.getMeta().getPublishingDate().split("-")[0];
        if (Integer.parseInt(day) < 10 && day.length() == 1) day = '0' + day;
        String month = pressRelease.getMeta().getPublishingDate().split("-")[1];
        if (Integer.parseInt(month) < 10 && month.length() == 1) month = '0' + month;
        String year = pressRelease.getMeta().getPublishingDate().split("-")[2];
        validDate = day + "-" + month + "-" + year;
        return validDate;
    }

    private boolean validateJavaDate(PressRelease pressRelease) {
        String day = pressRelease.getMeta().getPublishingDate().split("-")[0];
        if (Integer.parseInt(day) > 31) return false;
        String month = pressRelease.getMeta().getPublishingDate().split("-")[1];
        if (Integer.parseInt(month) > 12) return false;
        return true;
    }

    public PressRelease createRelease(PressRelease pressRelease) throws ParseException, ItorixException {
        log.info("pressRelease {}", pressRelease);
        if (validateJavaDate(pressRelease)) {
            List<PressRelease> allPressReleases = getPressReleases();
            String slug = pressRelease.getMeta().getTitle();
            if (allPressReleases.stream().anyMatch(n -> n.getMeta().getSlug().equals(slug))) {
                return null;
            }

            String validDate = validDate(pressRelease);
            pressRelease.getMeta().setPublishingDate(validDate);
            long currentTime = System.currentTimeMillis();
            pressRelease.setCts(currentTime);
            String year = pressRelease.getMeta().getPublishingDate().split("-")[2];
            pressRelease.getMeta().setSlug(slug);
            pressRelease.getMeta().setYear(Integer.parseInt(year));
            return masterMongoTemplate.save(pressRelease);
        } else {
            throw new ItorixException(
                    String.format(ErrorCodes.errorMessage.get("Marketing-1001")),
                    "Marketing-1001");
        }
    }

    public List<PressRelease> getPressReleases() {
        return masterMongoTemplate.findAll(PressRelease.class);
    }

    public List<PressRelease> getPressReleases(int offset, int pageSize) {
        Query query = new Query()
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return masterMongoTemplate.find(query,PressRelease.class);
    }


    public Pagination getPagination(int offset, int pageSize){
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setPageSize(pageSize);
        pagination.setTotal(masterMongoTemplate.count(new Query(),PressRelease.class));
        return pagination;
    }

    public Pagination getPaginationForFilter(int offset, int pageSize, int size) {
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setPageSize(pageSize);
        pagination.setTotal((long)size);
        return pagination;
    }

    public ResponseEntity<?> updateRelease(PressRelease pressRelease, String releaseId) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(releaseId));
        log.debug("edit release {}", query);
        PressRelease existing = masterMongoTemplate.findOne(query, PressRelease.class);
        if (existing != null){
            long currentTime = System.currentTimeMillis();
            pressRelease.setMts(currentTime);
            String year = pressRelease.getMeta().getPublishingDate().split("-")[2];
            pressRelease.getMeta().setYear(Integer.parseInt(year));
            pressRelease.getMeta().setSlug(existing.getMeta().getSlug());
            if(existing.getMeta().getStatus().equals(PressReleaseStatus.PUBLISH)){
                pressRelease.getMeta().setStatus(PressReleaseStatus.PUBLISH);
            }
            Update update = new Update();
            update.set("meta",pressRelease.getMeta());
            update.set("content",pressRelease.getContent());
            masterMongoTemplate.upsert(query,update,PressRelease.class);
            return new ResponseEntity<>(pressRelease,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public PressRelease deletePressRelease(String releaseId) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(releaseId));
        return masterMongoTemplate.findAndRemove(query, PressRelease.class);
    }

    public ResponseEntity<Object> changeStatus(String releaseId, PressReleaseStatus status) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(releaseId));
        PressRelease blog = masterMongoTemplate.findOne(query, PressRelease.class);
        if (blog != null) {
            Update update = new Update();
            update.set("meta.status", status.name());
            masterMongoTemplate.upsert(query, update, PressRelease.class);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
