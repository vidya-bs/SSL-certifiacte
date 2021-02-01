package com.itorix.apiwiz.sso.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;

@Component
public class BaseRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@SuppressWarnings("unchecked")
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
		mongoTemplate.save(obj);
		t = (T) obj;
		return t;
	}

	public <T> T save(T t, MongoTemplate mongoTemplate) {
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
		mongoTemplate.save(obj);
		t = (T) obj;
		return t;
	}

	public <T> List<T> saveAll(List<T> objs) {
		List<T> list = new LinkedList<T>();
		for (T obj : objs) {
			list.add(save(obj));
		}
		return list;
	}



	public <T> T findById(String id, Class<T> clazz) {
		return (T) mongoTemplate.findById(id, clazz);
	}

	public <T> T findOne(String fieldName, Object fieldValue, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> List<T> find(String fieldName, Object fieldValue, Class<T> clazz) {
		return (List<T>) mongoTemplate.find(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}

	public <T> T findOne(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
	}
	public <T> T findOne(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2,String fieldName3, Object fieldValue3, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2).and(fieldName3).is(fieldValue3)), clazz);
	}

	public <T> T findOne(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2,String fieldName3, Object fieldValue3,String fieldName4, Object fieldValue4, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2).and(fieldName3).is(fieldValue3).and(fieldName4).is(fieldValue4)), clazz);
	}

	public <T> List<T>  find(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2, Class<T> clazz) {
		return (List<T>) mongoTemplate.find(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
	}
	public <T> List<T>  find(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2, String fieldName3, Object fieldValue3,Class<T> clazz) {
		return (List<T>) mongoTemplate.find(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2).and(fieldName3).is(fieldValue3)), clazz);
	}

	public <T> List<T> findAll(Class<T> clazz) {
		return mongoTemplate.findAll(clazz);
	}



	public <T> List<T> find(Query query, Class<T> clazz) {
		return mongoTemplate.find(query, clazz);
	}

	private List<String> getList(DistinctIterable<String> iterable){
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
	    list.add(cursor.next());
		}
		return list;
	}

}
