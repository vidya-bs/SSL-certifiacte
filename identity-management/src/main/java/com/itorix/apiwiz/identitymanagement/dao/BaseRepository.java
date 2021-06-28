package com.itorix.apiwiz.identitymanagement.dao;

import java.util.*;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.BaseObject;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.WriteResult;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;

import static com.itorix.apiwiz.identitymanagement.model.Constants.SWAGGER_PROJECTION_FIELDS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

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

	public <T> DeleteResult delete(String id, Class<T> clazz) {
		return mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), clazz);
	}

	public <T> DeleteResult delete(String fieldName, Object fieldValue, Class<T> clazz) {
		return mongoTemplate.remove(new Query(Criteria.where(fieldName).is(fieldValue)), clazz);
	}
	public <T> DeleteResult delete(String fieldName1, Object fieldValue1,String fieldName2, Object fieldValue2, Class<T> clazz) {
		return  mongoTemplate.remove(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
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
	
	@SuppressWarnings("unchecked")
	public <T> List<String> findDistinctValuesByColumnName(Class<T> clazz,String columName) {
		return getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columName, String.class));
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<String> findDistinctValuesByColumnNameWherecreatedBy(Class<T> clazz,String columName, String createdBy) {
		return getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columName, new Query(Criteria.where("createdBy").is(createdBy)).getQueryObject(), String.class));
	}

	public <T> List<T> find(Query query, Class<T> clazz) {
		return mongoTemplate.find(query, clazz);
	}

	public <T> List<T> findAll(String sortField, String order, Class<T> clazz) {
		Query query = new Query();
		Direction direction = null;
		switch (order) {
		case "+":
			direction = Sort.Direction.ASC;
			break;
		case "-":
			direction = Sort.Direction.DESC;
			break;
		}
		query.with(Sort.by(direction, sortField));
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

	public List<String> filterAndGroupBySwaggerName(Map<String, Object> filterFieldsAndValues, Class<?> clazz) {
		ArrayList<String> names = new ArrayList();
		ProjectionOperation projectRequiredFields = project(SWAGGER_PROJECTION_FIELDS).
				andExpression("toDate(mts)").as("mtsToDate");

		ProjectionOperation dateToString = Aggregation.project(SWAGGER_PROJECTION_FIELDS)
				.and("mtsToDate")
				.dateAsFormattedString("%m%d%Y")
				.as("modified_date");

		GroupOperation groupByName = group("name");

		MatchOperation match = getMatchOperation(filterFieldsAndValues);
		AggregationResults<Document> results = null;

		if(match != null) {
			results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, dateToString, match, groupByName), clazz, Document.class);
		} else {
			results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, dateToString, groupByName), clazz, Document.class);
		}
		results.getMappedResults().forEach( d -> names.add(d.getString("_id")));
		return names;
	}

	private MatchOperation getMatchOperation(Map<String, Object> filterFieldsAndValues) {
		List<Criteria> criteriaList = new ArrayList<>();
		filterFieldsAndValues.forEach((k, v) -> {
			if (null != v) {
				criteriaList.add(Criteria.where(k).is(v));
			}
		});

		Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		return criteriaList.size() > 0 ? match(criteria) : null;
	}

}
