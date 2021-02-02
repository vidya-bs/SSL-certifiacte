package com.itorix.apiwiz.marketing.dao;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.marketing.careers.model.JobApplication;
import com.itorix.apiwiz.marketing.careers.model.JobPosting;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Component
public class CareersDao {
	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	public String createUpdatePosting(JobPosting jobPosting){
		Query query = new Query().addCriteria(Criteria.where("name").is(jobPosting.getName()));
		JobPosting dbJobPosting = masterMongoTemplate.findOne(query, JobPosting.class);
		if(dbJobPosting != null){
			jobPosting.setId(dbJobPosting.getId());
		}
		else{
			masterMongoTemplate.save(jobPosting);
		}
		return jobPosting.getId();
	}

	public void deletePosting(String jobId){
		Query query = new Query().addCriteria(Criteria.where("id").is(jobId));
		masterMongoTemplate.remove(query, JobPosting.class);
	}

	public List<JobPosting> getAllPostings(){
		List<JobPosting> postings =  masterMongoTemplate.findAll(JobPosting.class);
		for(JobPosting posting :  postings){
			posting.setAboutUs(null);
			posting.setDetail(null);
		}
		return postings;
	}
	
	public List<JobPosting> getAllPostings(List<String> categoryList, List<String> locationList, List<String> rollList){
		Query query = new Query();
		if(categoryList != null)
			query.addCriteria(Criteria.where("category").in(categoryList));
		if(locationList != null)
			query.addCriteria(Criteria.where("location").in(locationList));
		if(rollList != null)
			query.addCriteria(Criteria.where("role").in(rollList));
		List<JobPosting> postings =  masterMongoTemplate.find(query, JobPosting.class);
		for(JobPosting posting :  postings){
			posting.setAboutUs(null);
			posting.setDetail(null);
		}
		return postings;
	}

	public JobPosting getPosting(String jobId){
		Query query = new Query().addCriteria(Criteria.where("id").is(jobId));
		JobPosting posting =  masterMongoTemplate.findOne(query,JobPosting.class);
		return posting;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getCategories(){
		List<String> categoryList = new ArrayList<>();
		List<String> rollList = new ArrayList<>();
		List<String> locationList = new ArrayList<>();
		try{
		String collectionName = masterMongoTemplate.getCollectionName(JobPosting.class);
		MongoCollection mongoCollection = masterMongoTemplate.getCollection(collectionName);
		DistinctIterable distinctIterable = mongoCollection.distinct("category",String.class);
		if(distinctIterable != null){
			MongoCursor cursor = distinctIterable.iterator();
			if(cursor != null){
				while (cursor.hasNext()) {
					String category = (String)cursor.next();
					categoryList.add(category);
				}
			}
		}
		distinctIterable = mongoCollection.distinct("location",String.class);
		if(distinctIterable != null){
			MongoCursor cursor = distinctIterable.iterator();
			if(cursor != null){
				while (cursor.hasNext()) {
					String category = (String)cursor.next();
					locationList.add(category);
				}
			}
		}
		distinctIterable = mongoCollection.distinct("role",String.class);
		if(distinctIterable != null){
			MongoCursor cursor = distinctIterable.iterator();
			if(cursor != null){
				while (cursor.hasNext()) {
					String category = (String)cursor.next();
					rollList.add(category);
				}
			}
		}
		}catch(Exception ex){
			
		}
		Map categories = new HashMap();
		categories.put("category", categoryList);
		categories.put("location", locationList);
		categories.put("role", rollList);
		return categories;
	}

	public String createUpdateJobApplication(JobApplication jobApplication){
		masterMongoTemplate.save(jobApplication);
		return jobApplication.getId();
	}

	public JobApplication getJobApplication(String emailId){
		Query query = new Query().addCriteria(Criteria.where("emailId").is(emailId));
		JobApplication application = masterMongoTemplate.findOne(query,JobApplication.class);
		return application;
	}

	public String updateProfileFile(String userId, String filename, byte[] bytes) throws ItorixException{
		return updateToJfrog(userId + "/" + filename, bytes);
	}

	public void deleteProfileFile(String userId, String imagePath) throws ItorixException{
		deleteFileJfrogFile("/marketing/careers/" + userId);
	}

	private String updateToJfrog(String folderPath, byte[] bytes)
			throws ItorixException {
		try {
			JSONObject uploadFiles = jfrogUtilImpl.uploadFiles(new ByteArrayInputStream(bytes), "/marketing/careers/" + folderPath);
			return uploadFiles.getString("downloadURI");
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-7"), "Marketing-2");
		}
	}

	private void deleteFileJfrogFile(String folderPath) throws ItorixException {
		try {
			jfrogUtilImpl.deleteFileIgnore404(folderPath);
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-15"), "Marketing-3");
		}
	}

}