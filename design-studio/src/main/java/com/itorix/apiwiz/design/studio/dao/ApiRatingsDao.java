package com.itorix.apiwiz.design.studio.dao;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.design.studio.model.ApiRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApiRatingsDao {
    @Autowired
    private MongoTemplate mongoTemplate;


    public ResponseEntity<?> postRating(String swaggerId,ApiRatings apiRatings) {
        if(apiRatings.getRating()>0 && apiRatings.getRating()<=5){
            Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(apiRatings.getOasVersion()).and("revision").is(apiRatings.getRevision()).and("email").is(apiRatings.getEmail()));
            if(mongoTemplate.findOne(query,ApiRatings.class) != null){
                return new ResponseEntity<>(new ErrorObj("review already existed", "409"),
                        HttpStatus.CONFLICT);
            }
            apiRatings.setSwaggerId(swaggerId);
            apiRatings.setCts(System.currentTimeMillis());
            apiRatings.setCreatedBy(apiRatings.getUserName());
            mongoTemplate.save(apiRatings);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
       return new ResponseEntity<>(new ErrorObj("Rating should be between 1-5", "500"),
               HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Map<String,Object> getRatingSummary(String swaggerId,String oas,int revision){
        List<ApiRatings> totalRatings  =  ratingsList(swaggerId,oas,revision);
        int[] individualRatings = new int[5];
        int totalRating = 0;
        for(ApiRatings apiRatings:totalRatings){
            int rating = apiRatings.getRating();
            totalRating+= rating;
            switch(rating){
                case 1:individualRatings[0]++;
                    break;
                case 2:individualRatings[1]++;
                    break;
                case 3:individualRatings[2]++;
                    break;
                case 4:individualRatings[3]++;
                    break;
                case 5:individualRatings[4]++;
                    break;
            }
        }
        Map<String,Object> summary = new HashMap<>();
        summary.put("ratings", individualRatings);
        if(totalRating==0)
            summary.put("average",null);
        else
            summary.put("average",(double)totalRating/totalRatings.size());
        return summary;
    }

    public List<ApiRatings> ratingsList(String swaggerId, String oas, int revision) {
        Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(oas)
                .and("revision").is(revision));
        return  mongoTemplate.find(query, ApiRatings.class);
    }
    public List<ApiRatings> getRatings(String swaggerId, String oas, int revision, String email) {
        List<ApiRatings> totalRatings  = ratingsList(swaggerId,oas,revision);
        for(int i=0;i<totalRatings.size();i++){
            if(totalRatings.get(i).getEmail().equals(email)){
                Collections.swap(totalRatings,0,i);
                break;
            }
        }
        return totalRatings;
    }

    public ResponseEntity<?> editRating(String swaggerId, ApiRatings apiRatings) {
        if(apiRatings.getRating()>0 && apiRatings.getRating()<=5){
            Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(apiRatings.getOasVersion()).and("revision").is(apiRatings.getRevision()).and("email").is(apiRatings.getEmail()));
            if(mongoTemplate.findOne(query,ApiRatings.class) == null){
                return new ResponseEntity<>(new ErrorObj("Cannot find rating", "404"),
                        HttpStatus.NOT_FOUND);
            }
            Update update = new Update();
            update.set("comments",apiRatings.getComments());
            update.set("rating",apiRatings.getRating());
            update.set("mts",System.currentTimeMillis());
            update.set("modifiedBy",apiRatings.getUserName());
            mongoTemplate.upsert(query,update,ApiRatings.class);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(new ErrorObj("Rating should be between 1-5", "500"),
         HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<?> deleteRatingadmin(String swaggerId, int revision, String oas, String ratingid) {
        Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(oas).and("revision")
                .is(revision).and("id").is(ratingid));
        mongoTemplate.remove(query, ApiRatings.class);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> deleteRating(String swaggerId, int revision, String oas, String email, String ratingid) {
        Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(oas).and("revision")
                .is(revision).and("email").is(email).and("id").is(ratingid));
        mongoTemplate.remove(query, ApiRatings.class);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

