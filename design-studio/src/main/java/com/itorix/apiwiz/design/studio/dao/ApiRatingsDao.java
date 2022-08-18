package com.itorix.apiwiz.design.studio.dao;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.design.studio.model.ApiRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
            apiRatings.setSwaggerId(swaggerId);
            mongoTemplate.save(apiRatings);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
       return new ResponseEntity<>(new ErrorObj("Rating should be between 1-5", "500"),
               HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Map<String,Object> getRatingSummary(String swaggerId,String oas,int revision){
        List<ApiRatings> totalRatings = getRatings(swaggerId,oas,revision);
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
        summary.put("average",String.format("%.1f",(double)totalRating/totalRatings.size()));
        summary.put("ratings", Arrays.toString(individualRatings));
        return summary;
    }

    public List<ApiRatings> getRatings(String swaggerId,String oas,int revison) {
        Query query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oasVersion").is(oas)
                .and("revision").is(revison));
        return mongoTemplate.find(query, ApiRatings.class);
    }

}
