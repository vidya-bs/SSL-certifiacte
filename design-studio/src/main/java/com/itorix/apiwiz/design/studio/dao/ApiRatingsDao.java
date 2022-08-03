package com.itorix.apiwiz.design.studio.dao;

import com.itorix.apiwiz.design.studio.model.ApiRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiRatingsDao {
    @Autowired
    private MongoTemplate mongoTemplate;


    public ResponseEntity<?> postRating(String swaggerId,ApiRatings apiRatings) {
        apiRatings.setSwaggerId(swaggerId);
        mongoTemplate.save(apiRatings);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public double getTotalRating(String swaggerId){
        Query query = new Query(Criteria.where("swaggerId").is(swaggerId));
        List<ApiRatings> totalRatings = mongoTemplate.find(query, ApiRatings.class);
        int totalRating = 0;
        for(ApiRatings apiRatings:totalRatings){
            totalRating+= apiRatings.getRating();
        }
        return (double)totalRating/totalRatings.size();
    }
}
