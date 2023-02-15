package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.marketing.news.model.News;
import com.itorix.apiwiz.marketing.pressrelease.model.PressRelease;
import com.itorix.apiwiz.marketing.pressrelease.model.PressReleaseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PressReleaseDao {
    @Qualifier("masterMongoTemplate")
    @Autowired
    MongoTemplate masterMongoTemplate;

    public List<PressRelease> getPressReleases(int offset, int pageSize) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return masterMongoTemplate.find(query,PressRelease.class);
    }

    public List<PressRelease> getAllPressReleases(int offset, int pageSize,String status) {
        List<PressRelease> pressReleaseList = null;
        if(status==null){
            List<PressRelease>existing=getPressReleases(offset,pageSize);
            if(existing.isEmpty())return new ArrayList<>();
            return existing;
        }
        else{
            Query query=new Query();
            query.addCriteria(Criteria.where("meta.status").is(status)).with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
                    .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
            pressReleaseList=masterMongoTemplate.find(query, PressRelease.class);
        }
        if(pressReleaseList.isEmpty())return new ArrayList<>();
        return pressReleaseList;
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

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static String validateJavaDate(PressRelease pressRelease) {
        if(pressRelease.getMeta().getPublishingDate()==null)return pressRelease.getMeta().getPublishingDate();
        String validDate=null;
        String day=pressRelease.getMeta().getPublishingDate().split("-")[0];
        if(Integer.parseInt(day)<10&&day.length()==1)day='0'+day;
        String month=pressRelease.getMeta().getPublishingDate().split("-")[1];
        if(Integer.parseInt(month)<10&&month.length()==1)month='0'+month;
        String year=pressRelease.getMeta().getPublishingDate().split("-")[2];
        validDate=day+"-"+month+"-"+year;
        return  validDate;
    }


    public PressRelease createRelease(PressRelease pressRelease) {
        List<PressRelease> allReleases = getPressReleases();
        String title = pressRelease.getMeta().getTitle();
        String slug=encodeValue(title);
        if(allReleases.stream().anyMatch(r->r.getMeta().getSlug().equals(slug))){
            return null;
        }
        String validDate=validateJavaDate(pressRelease);
        pressRelease.getMeta().setPublishingDate(validDate);
        long currentTime = System.currentTimeMillis();
        pressRelease.setCts(currentTime);
        String year = pressRelease.getMeta().getPublishingDate().split("-")[2];
        pressRelease.getMeta().setYear(Integer.parseInt(year));
        pressRelease.getMeta().setSlug(slug);
        return masterMongoTemplate.save(pressRelease);
    }

    public List<PressRelease> getPressReleases(){
        return masterMongoTemplate.findAll(PressRelease.class);
    }

    public List<PressRelease> getDataByYear(int offset, int pageSize, int year) {
        Query query = new Query().addCriteria(Criteria.where("meta.year").is(year));
        query.with(Sort.by(Sort.Direction.DESC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return masterMongoTemplate.find(query,PressRelease.class);
    }



    public List<PressRelease> getDataByTagOrSlug(int offset, int pageSize, String filter, String filterValue) {
        List<PressRelease> returningList = new ArrayList<>();
        List<PressRelease> releaseList = getPressReleases(offset, pageSize);
        releaseList.forEach(rl->{
            if(filter.equals("tag")){
                Query query = new Query();
                query.addCriteria(Criteria.where("meta.tags").is(filterValue));
                query.with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
                        .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
                returningList.addAll(masterMongoTemplate.find(query, PressRelease.class));
            }
            else {
                Query query = new Query();
                query.addCriteria(Criteria.where("meta.slug").is(filterValue));
                query.with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
                        .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
                returningList.addAll(masterMongoTemplate.find(query, PressRelease.class));
            }
        });
        return returningList;
    }

    public List<PressRelease> getDataByFilter(int offset, int pageSize, String filter, String filterValue) {
        switch (filter){
            case "tag":
            case "slug":
                return getDataByTagOrSlug(offset,pageSize,filter,filterValue);
            case "year":
                return getDataByYear(offset,pageSize,Integer.parseInt(filterValue));
        }
        return new ArrayList<>();
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
            if(existing.getMeta().getStatus().equals("PUBLISH")){
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
