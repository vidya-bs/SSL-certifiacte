package com.itorix.apiwiz.identitymanagement.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SchemaInfo;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerData;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static com.itorix.apiwiz.identitymanagement.model.Constants.SWAGGER_PROJECTION_FIELDS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

public class BaseRepositoryTest {

	private static MongoTemplate mongoTemplate = null;

	@BeforeClass
	public static void init() {
		ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();
		MongoClients.create(mongoClientSettings);
		mongoTemplate = new MongoTemplate(MongoClients.create(mongoClientSettings), "acme-team-dev");
	}

	@Test
	public void checkFiltersOnSwaggerList() {
		String swaggerCollName = mongoTemplate.getCollectionName(SwaggerVO.class);
		MongoCollection<Document> swaggerDocs = mongoTemplate.getCollection(swaggerCollName);

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

		Query query = new Query();
		List<Criteria> criteriaList = new ArrayList<>();
		Map<String, Object> matchFields = new LinkedHashMap<>();
		matchFields.put("status", null);
		matchFields.put("modified_date", null);

		Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

		SortOperation sortOperation = getSortOperation("asc");

		MatchOperation match = getMatchOperation(matchFields);
		AggregationResults<Document> results = null;

		// unwindOperation projectionOperation//match, groupByName,
		// sortOperation
		if (match != null) {
			results = mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, dateToString, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, match, groupByName, sortOperation),
					SwaggerVO.class, Document.class);
		} else {
			results = mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, dateToString, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, groupByName, sortOperation),
					SwaggerVO.class, Document.class);
		}

		List test = null;

		results.getMappedResults().stream().forEach(System.out::println);

		List<String> names = new LinkedList<>();
		results.getMappedResults().stream().forEach(d -> names.add(d.getString("_id")));
		System.out.println(names);
	}

	private SortOperation getSortOperation(String sortByModifiedTS) {
		SortOperation sortOperation = null;
		if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("ASC")) {
			sortOperation = sort(Sort.Direction.ASC, "mts");
			groupByName = group("name").max("mts").as("mts");
		} else if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("DESC")) {
			sortOperation = sort(Sort.Direction.DESC, "mts");
		} else {
			sortOperation = sort(Sort.Direction.ASC, "name");
			groupByName = group("name").max("name").as("name");
		}

		return sortOperation;
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

	GroupOperation groupByName = group("name").max("mts").as("mts");

	@Test
	public void getDDTest() throws JsonProcessingException {
		String ddId = "603d820ec001f044e3934f3e";

		UnwindOperation unwindDictionary = unwind("dictionary");
		UnwindOperation unwindDictionaryModels = unwind("dictionary.models");
		MatchOperation matchOperation = match(Criteria.where("dictionary._id").is(new ObjectId(ddId))
				.and("dictionary.models.name").is("GeographicLocation"));

		AggregationResults<Document> aggregate = mongoTemplate.aggregate(
				newAggregation(unwindDictionary, unwindDictionaryModels, matchOperation), SwaggerDictionary.class,
				Document.class);

		List<Document> mappedResults = aggregate.getMappedResults();
		System.out.println(mappedResults);
		HashMap<String, List<String>> output = new HashMap<>();
		DictionarySwagger dictionarySwagger = new DictionarySwagger();

		if (mappedResults.size() > 0) {
			Document dictionaryObj = mappedResults.get(0).get("dictionary", Document.class);
			dictionarySwagger.setId(dictionaryObj.get("_id", ObjectId.class).toString());
			dictionarySwagger.setName(dictionaryObj.getString("name"));
		} else {
			return;
		}

		for (Document doc : mappedResults) {
			Document dictionaryObj = doc.get("dictionary", Document.class);
			Document modelsObj = dictionaryObj.get("models", Document.class);
			String modelName = modelsObj.getString("name");

			if (dictionarySwagger.getSchemas() != null && dictionarySwagger.getSchemas().size() > 0) {
				Optional<SchemaInfo> schemaInfoOptional = dictionarySwagger.getSchemas().stream()
						.filter(s -> s.getName().equals(modelName)).findFirst();
				if (schemaInfoOptional.isPresent()) {
					SwaggerData swaggerData = new SwaggerData();
					swaggerData.setId(doc.getString("swaggerId"));
					swaggerData.setName(doc.getString("name"));
					swaggerData.setOasVersion(doc.getString("oasVersion"));
					swaggerData.setRevision(doc.getInteger("revision"));
					swaggerData.setStatus(doc.getString("status"));
					schemaInfoOptional.get().getSwaggers().add(swaggerData);
				} else {
					ArrayList<SwaggerData> swaggers = new ArrayList<>();
					SwaggerData swaggerData = new SwaggerData();
					swaggerData.setId(doc.getString("swaggerId"));
					swaggerData.setName(doc.getString("name"));
					swaggerData.setOasVersion(doc.getString("oasVersion"));
					swaggerData.setRevision(doc.getInteger("revision"));
					swaggerData.setStatus(doc.getString("status"));
					SchemaInfo schemaInfo = new SchemaInfo();
					schemaInfo.setName(modelName);
					swaggers.add(swaggerData);
					schemaInfo.setSwaggers(swaggers);
					dictionarySwagger.getSchemas().add(schemaInfo);

				}
			} else {
				ArrayList<SchemaInfo> schemaInfos = new ArrayList<>();
				SchemaInfo schemaInfo = new SchemaInfo();
				schemaInfo.setName(modelName);
				ArrayList<SwaggerData> swaggers = new ArrayList<>();
				SwaggerData swaggerData = new SwaggerData();
				swaggerData.setId(doc.getString("swaggerId"));
				swaggerData.setName(doc.getString("name"));
				swaggerData.setOasVersion(doc.getString("oasVersion"));
				swaggerData.setRevision(doc.getInteger("revision"));
				swaggerData.setStatus(doc.getString("status"));
				swaggers.add(swaggerData);
				schemaInfo.setSwaggers(swaggers);
				schemaInfos.add(schemaInfo);
				dictionarySwagger.setSchemas(schemaInfos);
			}

			// if(swaggerDataMap.containsKey(modelName)) {
			// List<SwaggerData> swaggerDataList =
			// swaggerDataMap.get(modelName);
			// SwaggerData swaggerData = new SwaggerData();
			// swaggerData.setId(doc.getString("swaggerId"));
			// swaggerData.setName(doc.getString("name"));
			// swaggerData.setOasVersion(doc.getString("oasVersion"));
			// swaggerData.setRevision(doc.getInteger("revision"));
			// swaggerData.setStatus(doc.getString("status"));
			// swaggerDataList.add(swaggerData);
			// } else {
			// List<SwaggerData> swaggerDataList = new ArrayList<>();
			// SwaggerData swaggerData = new SwaggerData();
			// swaggerData.setId(doc.getString("swaggerId"));
			// swaggerData.setName(doc.getString("name"));
			// swaggerData.setOasVersion(doc.getString("oasVersion"));
			// swaggerData.setRevision(doc.getInteger("revision"));
			// swaggerData.setStatus(doc.getString("status"));
			// swaggerDataList.add(swaggerData);
			// swaggerDataMap.put(modelName, swaggerDataList);
			// }
		}

		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(dictionarySwagger));

	}

}
