package com.itorix.apiwiz.validator.license.dao;

import com.itorix.apiwiz.validator.license.model.db.License;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LicenseRepository {

	@Autowired
	private MongoTemplate mongoTemplate;


	public License save(License license) {
		String id = license.getId();
		long timestamp = System.currentTimeMillis();
		license.setMts(timestamp);
//		license.setModifiedUserName(license.getUserName());
//		if (id == null || id == "") {
//			license.setCts(timestamp);
//			license.setCreatedUserName(license.getUserName());
//		}
		return  mongoTemplate.save(license);
	}

	public <T> DeleteResult delete(String fieldName, Object fieldValue, Class<T> clazz) {
		return mongoTemplate.remove(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> T findOne(String fieldName, Object fieldValue, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> List<T> findAll(Class<T> clazz) {
		return mongoTemplate.findAll(clazz);
	}
}
