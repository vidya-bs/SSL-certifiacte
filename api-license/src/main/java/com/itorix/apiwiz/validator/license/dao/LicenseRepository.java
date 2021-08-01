package com.itorix.apiwiz.validator.license.dao;

import com.itorix.apiwiz.common.model.BaseObject;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LicenseRepository {

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;


	public <T> T save(T t) {
		BaseObject obj = (BaseObject) t;
		String userId = null;
		String username = null;
		try {
			UserSession userSession  = UserSession.getCurrentSessionToken();
			userId = userSession.getUserId();
			username = userSession.getUsername();
		} catch (Exception e) {
		}
		String id = obj.getId();
		long timestamp = System.currentTimeMillis();
		obj.setMts(timestamp);
		obj.setModifiedBy(userId);
		obj.setModifiedUserName(username);
		if (id == null || id == "") {
			obj.setCts(timestamp);
			obj.setCreatedBy(userId);
			obj.setCreatedUserName(username);
		}
		masterMongoTemplate.save(obj);
		t = (T) obj;
		return t;
	}

	public <T> DeleteResult delete(String fieldName, Object fieldValue, Class<T> clazz) {
		return masterMongoTemplate.remove(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> T findOne(String fieldName, Object fieldValue, Class<T> clazz) {
		return (T) masterMongoTemplate.findOne(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> List<T> findAll(Class<T> clazz) {
		return masterMongoTemplate.findAll(clazz);
	}
}
