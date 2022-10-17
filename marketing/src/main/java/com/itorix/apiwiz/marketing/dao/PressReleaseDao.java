package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
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

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PressReleaseDao {
    @Qualifier("masterMongoTemplate")
    @Autowired
    MongoTemplate masterMongoTemplate;

    public List<PressRelease> getPressReleases(int offset, int pageSize) {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return masterMongoTemplate.find(query,PressRelease.class);
    }

    public Pagination getPagination(int offset, int pageSize){
        Pagination pagination = new Pagination();
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        pagination.setOffset(offset);
        pagination.setPageSize(pageSize);
        pagination.setTotal(masterMongoTemplate.count(query,PressRelease.class));
        return pagination;
    }

    public Pagination getPaginationForFilter(int offset, int pageSize, int size) {
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setPageSize(pageSize);
        pagination.setTotal((long)size);
        return pagination;
    }
    public PressRelease createRelease(PressRelease pressRelease) {
        List<PressRelease> allReleases = getPressReleases();
        String title = pressRelease.getMeta().getTitle();
        String slug = title.toLowerCase().replace(" ", "-").replace(":","-");
        if(allReleases.stream().anyMatch(r->r.getMeta().getSlug().equals(slug))){
            return null;
        }
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
        query.with(Sort.by(Sort.Direction.ASC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return masterMongoTemplate.find(query,PressRelease.class);
    }

    public List<PressRelease> getDataByTagOrSlug(int offset, int pageSize, String filter, String filterValue) {
        List<PressRelease> returningList = new ArrayList<>();
        List<PressRelease> releaseList = getPressReleases(offset, pageSize);
        releaseList.forEach(rl->{
            if(filter.equals("tag")){
                if(rl.getMeta().getTags().stream().anyMatch(tl->tl.getTagName().equalsIgnoreCase(filterValue))) {
                    returningList.add(rl);
                }
            }
            else {
                if(rl.getMeta().getSlug().equalsIgnoreCase(filterValue)) {
                    returningList.add(rl);
                }
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
