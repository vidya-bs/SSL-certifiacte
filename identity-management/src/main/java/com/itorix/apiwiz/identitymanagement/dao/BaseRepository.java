package com.itorix.apiwiz.identitymanagement.dao;

import com.itorix.apiwiz.common.model.BaseObject;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.itorix.apiwiz.identitymanagement.model.Constants.SWAGGER_PROJECTION_FIELDS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

@Component
public class BaseRepository {

	@Autowired
	MongoTemplate mongoTemplate;
	private GroupOperation groupByName = null;

	@SuppressWarnings("unchecked")
	public <T> T save(T t) {
		BaseObject obj = (BaseObject) t;
		String userId = null;
		String username = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
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

	public <T> T saveMongoDoc(T doc) {
		doc = mongoTemplate.save(doc);
		return doc;
	}

	public <T> T save(T t, MongoTemplate mongoTemplate) {
		BaseObject obj = (BaseObject) t;
		String userId = null;
		String username = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
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

	public <T> DeleteResult delete(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2,
			Class<T> clazz) {
		return mongoTemplate
				.remove(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
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

	public <T> T findOne(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2, Class<T> clazz) {
		return (T) mongoTemplate
				.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
	}

	public <T> T findOne(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2,
			String fieldName3, Object fieldValue3, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2)
				.is(fieldValue2).and(fieldName3).is(fieldValue3)), clazz);
	}

	public <T> T findOne(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2,
			String fieldName3, Object fieldValue3, String fieldName4, Object fieldValue4, Class<T> clazz) {
		return (T) mongoTemplate.findOne(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2)
				.is(fieldValue2).and(fieldName3).is(fieldValue3).and(fieldName4).is(fieldValue4)), clazz);
	}

	public <T> List<T> find(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2,
			Class<T> clazz) {
		return (List<T>) mongoTemplate
				.find(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2).is(fieldValue2)), clazz);
	}

	public <T> List<T> find(String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2,
			String fieldName3, Object fieldValue3, Class<T> clazz) {
		return (List<T>) mongoTemplate.find(new Query(Criteria.where(fieldName1).is(fieldValue1).and(fieldName2)
				.is(fieldValue2).and(fieldName3).is(fieldValue3)), clazz);
	}

	public <T> List<T> findAll(Class<T> clazz) {
		return mongoTemplate.findAll(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> List<String> findDistinctValuesByColumnName(Class<T> clazz, String columnName) {
		return getList(
				mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columnName, String.class));
	}

	@SuppressWarnings("unchecked")
	public <T> List<String> findDistinctValuesByColumnNameWherecreatedBy(Class<T> clazz, String columName,
			String createdBy) {
		return getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columName,
				new Query(Criteria.where("createdBy").is(createdBy)).getQueryObject(), String.class));
	}

	public <T> List<T> find(Query query, Class<T> clazz) {
		return mongoTemplate.find(query, clazz);
	}

	public <T> List<T> findAll(String sortField, String order, Class<T> clazz) {
		Query query = new Query();
		Direction direction = null;
		switch (order) {
			case "+" :
				direction = Sort.Direction.ASC;
				break;
			case "-" :
				direction = Sort.Direction.DESC;
				break;
		}
		query.with(Sort.by(direction, sortField));
		return mongoTemplate.find(query, clazz);
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		return list;
	}

	public List<String> filterAndGroupBySwaggerName(Map<String, Object> filterFieldsAndValues, Class<?> clazz,
			String sortByModifiedTS) {
		List<String> names = new LinkedList();
		AggregationResults<Document> results = null;

		ProjectionOperation projectRequiredFields = project(SWAGGER_PROJECTION_FIELDS).andExpression("toDate(mts)")
				.as("mtsToDate");

		ProjectionOperation dateToString = Aggregation.project(SWAGGER_PROJECTION_FIELDS).and("mtsToDate")
				.dateAsFormattedString("%m%d%Y").as("modified_date");

		GroupOperation groupByMaxRevision = group("name").max("revision").as("maxRevision").push("$$ROOT")
				.as("originalDoc");
		ProjectionOperation filterMaxRevision = project()
				.and(filter("originalDoc").as("doc").by(valueOf("maxRevision").equalToValue("$$doc.revision")))
				.as("originalDoc");

		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project("originalDoc.name").andInclude("originalDoc.status",
				"originalDoc.modified_date", "originalDoc.mts");

		MatchOperation match = getMatchOperation(filterFieldsAndValues);
		groupByName = group("name").max("mts").as("mts");
		SortOperation sortOperation = getSortOperation(sortByModifiedTS);

		if (match != null) {
			results = mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, dateToString, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, match, groupByName, sortOperation),
					clazz, Document.class);
		} else {
			results = mongoTemplate
					.aggregate(
							newAggregation(projectRequiredFields, dateToString, groupByMaxRevision, filterMaxRevision,
									unwindOperation, projectionOperation, groupByName, sortOperation),
							clazz, Document.class);
		}
		results.getMappedResults().forEach(d -> names.add(d.getString("_id")));
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

	private SortOperation getSortOperation(String sortByModifiedTS) {
		SortOperation sortOperation = null;
		if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("ASC")) {
			sortOperation = sort(Sort.Direction.ASC, "mts");
			groupByName = group("name").max("mts").as("mts");
		} else if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("DESC")) {
			groupByName = group("name").max("mts").as("mts");
			sortOperation = sort(Sort.Direction.DESC, "mts");
		} else {
			sortOperation = sort(Sort.Direction.ASC, "name");
			groupByName = group("name").max("name").as("name");
		}

		return sortOperation;
	}

	public List<Document> getSwaggerAssociatedWithDictionary(String dictionaryId, Class clazz) {
		UnwindOperation unwindDictionary = unwind("dictionary");
		UnwindOperation unwindDictionaryModels = unwind("dictionary.models");
		MatchOperation matchOperation = match(Criteria.where("dictionary._id").is(new ObjectId(dictionaryId)));

		AggregationResults<Document> aggregate = mongoTemplate.aggregate(
				newAggregation(unwindDictionary, unwindDictionaryModels, matchOperation), clazz, Document.class);

		return aggregate.getMappedResults();
	}

	public List<Document> getSwaggerAssociatedWithSchemaName(String dictionaryId, String schemaName, Class clazz) {
		UnwindOperation unwindDictionary = unwind("dictionary");
		UnwindOperation unwindDictionaryModels = unwind("dictionary.models");
		MatchOperation matchOperation = match(Criteria.where("dictionary._id").is(new ObjectId(dictionaryId))
				.and("dictionary.models.name").is(schemaName));

		AggregationResults<Document> aggregate = mongoTemplate.aggregate(
				newAggregation(unwindDictionary, unwindDictionaryModels, matchOperation), clazz, Document.class);

		return aggregate.getMappedResults();
	}
}
