package com.itorix.apiwiz.datadictionary.businessimpl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.scm.ScmMinifiedUtil;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.*;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import com.itorix.apiwiz.design.studio.model.NotificationType;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class DictionaryBusinessImpl implements DictionaryBusiness {

	private static final Logger logger = LoggerFactory.getLogger(DictionaryBusinessImpl.class);
	@Autowired
	BaseRepository baseRepository;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MailUtil mailUtil;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private NotificationBusiness notificationBusiness;

	@Autowired
	private RSAEncryption rsaEncryption;

	@Autowired
	private ScmMinifiedUtil scmUtilImpl;

	/**
	 * log
	 *
	 * @param methodName
	 * @param interactionid
	 * @param body
	 */
	private void log(String methodName, String interactionid, Object... body) {
		logger.debug("dataDictionary." + methodName + " | CorelationId=" + interactionid + " | request/response Body ="
				+ body);
	}

	public PortfolioVO createPortfolio(PortfolioVO portfolioVO) {
		log("createPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		if (null == portfolioVO.getDictionaryId()) {
			portfolioVO.setDictionaryId(new ObjectId().toString());
		}
		// portfolioVO.setRevision(1);
		PortfolioVO vo = baseRepository.save(portfolioVO);
		log("createPortfolio", portfolioVO.getInteractionid(), vo);
		return vo;
	}

	public PortfolioVO createPortfolioRevision(PortfolioVO portfolioVO, String id) {
		log("createPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		PortfolioVO vo = baseRepository.save(portfolioVO);
		List<PortfolioModel> models = findPortfolioModelsByportfolioID(id);
		Query query = new Query(Criteria.where("portfolioID").is(id));
		List<String> modelIds = mongoTemplate.findDistinct(query, "modelId", PortfolioModel.class, String.class);
		for(int i=0;i<modelIds.size(); i++) {
			List<PortfolioModel> uniqueModels = findPortfolioModelsWithAllRevisions(id, modelIds.get(i));
			if (uniqueModels != null) {
				String uniqueModelId=UUID.randomUUID().toString().replaceAll("-", "");
				for (PortfolioModel model : uniqueModels) {
					model.setPortfolioID(vo.getId());
					model.setMts(System.currentTimeMillis());
					model.setModelId(uniqueModelId);
					createPortfolioModel(model);
				}
			}
		}
		log("createPortfolio", portfolioVO.getInteractionid(), vo);
		return vo;
	}

	public PortfolioVO findPortfolio(PortfolioVO portfolioVO) {
		log("findPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		return baseRepository.findOne("name", portfolioVO.getName(), PortfolioVO.class);
	}


	public PortfolioHistoryResponse findAllPortfolios(String interactionid, int offset, int pageSize) {
		log("findPortfolio", interactionid);
		PortfolioHistoryResponse historyResponse = new PortfolioHistoryResponse();
		List<String> uniqueDictionaryIds = mongoTemplate.findDistinct("dictionaryId", PortfolioVO.class, String.class);
		// reversing the uniqueDictionaryIds since its in ascending order of
		// creation
		Collections.reverse(uniqueDictionaryIds);
		List<String> dictionaryIds = trimList2(uniqueDictionaryIds, offset, pageSize);
		List<PortfolioVO> portfolios = new ArrayList<>();
		if (dictionaryIds != null) {
			for (String dictionaryId : dictionaryIds) {
				PortfolioVO portfolio = getPortfolioByRevision(dictionaryId, getMaxRevision(dictionaryId));

				List<Object> strModels = new ArrayList<Object>();
				List<PortfolioModel> dataModels = findPortfolioModelsByportfolioID(portfolio);
				if (dataModels != null) {
					for (PortfolioModel model : dataModels) {
						try {
							String name = model.getModelName();
							strModels.add(name);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				portfolio.setModels(strModels);
				portfolios.add(portfolio);
			}
			Long counter = Long.valueOf(uniqueDictionaryIds.size());
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			historyResponse.setPagination(pagination);
			historyResponse.setData(portfolios);
		}
		return historyResponse;
	}
	public PortfolioHistoryResponse findAllPortfoliosV2(String interactionid, int offset,
			int pageSize) {
		log("findPortfolio", interactionid);
		PortfolioHistoryResponse historyResponse = new PortfolioHistoryResponse();

		String[] PROJECTION_FIELDS = {"id", "summary", "description", "name", "revision",
				"dictionaryId", "status", "createdBy", "modifiedBy", "cts", "mts",
				"modifiedUserName", "createdUserName"};

		ProjectionOperation projectRequiredFields = project(PROJECTION_FIELDS);

		GroupOperation groupByMaxRevision = group("$dictionaryId").max("revision")
				.as("maxRevision").push("$$ROOT")
				.as("originalDoc");

		ProjectionOperation filterMaxRevision = project()
				.and(filter("originalDoc").as("doc").by(valueOf("maxRevision")
						.equalToValue("$$doc.revision")))
				.as("originalDoc");

		SortOperation sortOperation = sort(Sort.Direction.DESC, "mts");

		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project("originalDoc.name").andInclude(
				"originalDoc.dictionaryId","originalDoc.summary","originalDoc.description",
				"originalDoc.revision","originalDoc.status","originalDoc.createdBy",
				"originalDoc.modifiedBy","originalDoc.cts","originalDoc.mts","originalDoc.modifiedUserName",
				"originalDoc.createdUserName", "originalDoc.modified_date","originalDoc._id");

		List<PortfolioVO> results = mongoTemplate
				.aggregate(
						newAggregation(projectRequiredFields, groupByMaxRevision,
								filterMaxRevision,unwindOperation,projectionOperation,sortOperation),
						PortfolioVO.class, PortfolioVO.class).getMappedResults();

		Long counter = (long) results.size();
		results = trimList(results, offset, pageSize);
		Pagination pagination = new Pagination();
		pagination.setOffset(offset);
		pagination.setTotal(counter);
		pagination.setPageSize(pageSize);
		historyResponse.setPagination(pagination);
		historyResponse.setData(results);
		return historyResponse;
	}
	private List<PortfolioVO> trimList(List<PortfolioVO> portfolioVOList, int offset, int pageSize) {
		List<PortfolioVO> dictionaryIds = new ArrayList<>();
		int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
		int end = i + pageSize;
		for (; i < portfolioVOList.size() && i < end; i++) {
			dictionaryIds.add(portfolioVOList.get(i));
		}
		return dictionaryIds;
	}

	private List<String> trimList2(List<String> ids, int offset, int pageSize) {
		List<String> dictionaryIds = new ArrayList<String>();
		int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
		int end = i + pageSize;
		for (; i < ids.size() && i < end; i++) {
			dictionaryIds.add(ids.get(i));
		}
		return dictionaryIds;
	}
	public List<PortfolioVO> findAllPortfolioSummary(String interactionid) {
		log("findPortfolio", interactionid);
		List<PortfolioVO> portfolios = baseRepository.findAll(PortfolioVO.class);
		if (portfolios != null) {
			for (PortfolioVO portfolio : portfolios) {
				portfolio.setDescription(null);
				portfolio.setSummary(null);
				List<Object> strModels = new ArrayList<>();
				List<PortfolioModel> dataModels = baseRepository.find("portfolioID", portfolio.getId(), PortfolioModel.class);
				if (dataModels != null) {
					for (PortfolioModel model : dataModels) {
						try {
							PortfolioModelResponse portfolioModelResponse = new PortfolioModelResponse();
							portfolioModelResponse.setModelName(model.getModelName());
							portfolioModelResponse.setMts(model.getMts());
							portfolioModelResponse.setStatus(model.getStatus());
							portfolioModelResponse.setModelId(model.getModelId());
							if(model.getRevision()== null)
								portfolioModelResponse.setRevision(1);
							else
								portfolioModelResponse.setRevision(model.getRevision());
							strModels.add(portfolioModelResponse);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				portfolio.setModels(strModels);
			}
		}
		return portfolios;
	}

	public PortfolioVO getPortfolioById(PortfolioVO portfolioVO) {
		log("findPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		PortfolioVO portfolio = baseRepository.findById(portfolioVO.getId(), PortfolioVO.class);
		List<Object> strModels = new ArrayList<Object>();
		List<PortfolioModel> dataModels = findPortfolioModelsByportfolioID(portfolio);
		ObjectMapper mapper = new ObjectMapper();
		if (dataModels != null)
			for (PortfolioModel model : dataModels) {
				try {
					ObjectNode objectNode = (ObjectNode) mapper.readTree(model.getModel());
					objectNode.put("modelId", model.getModelId());
					objectNode.put("revision", model.getRevision());
					objectNode.put("status", model.getStatus().name());
					strModels.add(objectNode);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		portfolio.setModels(strModels);
		return portfolio;
	}

	public PortfolioVO findPortfolioById(PortfolioVO portfolioVO) {
		log("findPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		return baseRepository.findById(portfolioVO.getId(), PortfolioVO.class);
	}

	public DeleteResult deletePortfolioByIdAndRevision(PortfolioVO portfolioVO) {
		log("deletePortfolio", portfolioVO.getInteractionid());
		baseRepository.delete("portfolioID", portfolioVO.getId(), PortfolioModel.class);
		return baseRepository.delete(portfolioVO.getId(), PortfolioVO.class);
	}

	public void deletePortfolioById(PortfolioVO portfolioVO) {
		log("deletePortfolio", portfolioVO.getInteractionid());
		List<PortfolioVO> portfolioIds = baseRepository.find("dictionaryId", portfolioVO.getDictionaryId(), PortfolioVO.class);
		if (portfolioIds != null && portfolioIds.size() > 0) {
			for (PortfolioVO portfolio : portfolioIds) {
				baseRepository.delete("portfolioID", portfolio.getId(), PortfolioModel.class);
				baseRepository.delete(portfolio.getId(), PortfolioVO.class);
			}
		}
	}

	public PortfolioModel createPortfolioModel(PortfolioModel model) {
		log("createPortfolioModel", model.getInteractionid(), model);
		Query query = new Query(Criteria.where("modelId").is(model.getModelId()).and("portfolioID")
				.is(model.getPortfolioID()).and("revision").is(model.getRevision()));
		Update update = new Update();
		update.set("model", model.getModel());
		update.set("mts", model.getMts());
		update.set("status", model.getStatus());
		update.set("modelName",model.getModelName());
		update.set("mts",System.currentTimeMillis());
		update.set("modifiedUserName",model.getModifiedUserName());
		if (model.getRevision() != null) {
			update.set("revision", model.getRevision());
		} else {
			update.set("revision", 1);
		}
		mongoTemplate.upsert(query, update, PortfolioModel.class);
		return model;
	}

	public PortfolioModel createnewPortfolioModel(PortfolioModel model) {
		Query query = new Query(
				Criteria.where("modelId").is(model.getModelId()).and("portfolioID").is(model.getPortfolioID()));
		log.debug("createnewPortfolioModel:{}",model);
		model = baseRepository.save(model);
		return model;
	}

	public PortfolioModel updateModelRevision(PortfolioModel model) {
		log("createPortfolioModel", model.getInteractionid(), model);
		Query query = new Query(Criteria.where("modelId").is(model.getModelId()).and("portfolioID")
				.is(model.getPortfolioID()).and("revision").is(model.getRevision()));
		PortfolioModel portfolioModel = mongoTemplate.findOne(query, PortfolioModel.class);
		Update update = new Update();
		if(portfolioModel == null) {
			query = new Query(Criteria.where("modelId").is(model.getModelId()).and("portfolioID")
					.is(model.getPortfolioID()));
			update.set("revision",1);
		}

		update.set("model", model.getModel());
		update.set("mts", model.getMts());
		update.set("status", model.getStatus());
		update.set("modelName",model.getModelName());
		mongoTemplate.upsert(query, update, PortfolioModel.class);

		return model;
	}

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.find("portfolioID", model.getPortfolioID(), PortfolioModel.class);
	}

	public List<PortfolioModel> findPortfolioModelsByportfolioID(String id) {
		return baseRepository.find("portfolioID", id, PortfolioModel.class);
	}

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioVO model) {
		log("findAllPortfolioModels", model.getInteractionid());
		//return baseRepository.find("portfolioID", model.getId(), PortfolioModel.class);
		Query query = new Query(Criteria.where("portfolioID").is(model.getId()));
		List<String> modelIds = mongoTemplate.findDistinct(query, "modelId", PortfolioModel.class, String.class);
		List<PortfolioModel> models = new ArrayList<>();
		if (modelIds != null) {
			for (String modelId : modelIds) {
				Integer revision = getDDRevisions(modelId, model.getId());
				PortfolioModel portfolioModel=new PortfolioModel();
				if(revision != null)
					portfolioModel = baseRepository.findOne("portfolioID", model.getId(), "modelId", modelId, "revision", revision, PortfolioModel.class);
				else
					portfolioModel= baseRepository.findOne ("portfolioID",model.getId(),"modelId",modelId,PortfolioModel.class);
				models.add(portfolioModel);
			}
		}

		return models;
	}

	public PortfolioModel findPortfolioModelsByportfolioIDAndModelId(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.findOne("portfolioID", model.getPortfolioID(), "modelId", model.getModelId(),
				PortfolioModel.class);
	}

	public DeleteResult deletePortfolioModelByModelId(String modelId) {
		log("findAllPortfolioModels", modelId);
		return baseRepository.delete("modelId", modelId,
				PortfolioModel.class);
	}

	@Override
	public Object portfolioSearch(String interactionid, String name, int limit) throws ItorixException {
		log("portfolioSearch", interactionid, "");
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<String> allPortfolios = getList(mongoTemplate.getCollection("Design.Dictionary.List").distinct("name",
				query.getQueryObject(), String.class));
		ObjectNode portfolioList = new ObjectMapper().createObjectNode();
		ArrayNode responseFields = new ObjectMapper().createArrayNode();
		for (String portfolioName : allPortfolios) {
			SearchItem item = new SearchItem();
			List<PortfolioVO> portfolioVOList = mongoTemplate.find(new Query(Criteria.where("name").is(portfolioName)),PortfolioVO.class);
			Optional<PortfolioVO> portfolioVO = portfolioVOList.stream().reduce(
					(portfolioVO1, portfolioVO2) -> portfolioVO1.getRevision() > portfolioVO2.getRevision()
							? portfolioVO1 : portfolioVO2);
			if(portfolioVO.isPresent()){
				item.setId(portfolioVO.get().getId());
				item.setName(portfolioName);
				responseFields.addPOJO(item);
			}
		}
		portfolioList.put("dataDictionaries", responseFields);
		return portfolioList;
	}

	@Override
	public void updatePortfolioModelStatus(String id, String model_name, ModelStatus modelStatus) {
		Query query = new Query(Criteria.where("modelName").is(model_name).and("portfolioID").is(id));
		Update update = new Update();
		update.set("status", modelStatus);
		mongoTemplate.upsert(query, update, PortfolioModel.class);
	}

	@Override
	public PortfolioVO getPortfolioByRevision(String id, Integer revision) {
		Query query = new Query(Criteria.where("dictionaryId").is(id).and("revision").is(revision));
		PortfolioVO portfolio = mongoTemplate.findOne(query, PortfolioVO.class);
		List<Object> strModels = new ArrayList<Object>();
		List<PortfolioModel> dataModels = findPortfolioModelsByportfolioID(portfolio);
		ObjectMapper mapper = new ObjectMapper();
		if (dataModels != null)
			for (PortfolioModel model : dataModels) {
				try {
					JsonNode jsonNode = mapper.readTree(model.getModel());
					strModels.add(jsonNode);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		portfolio.setModels(strModels);
		return portfolio;
	}

	public Integer getMaxRevision(String id) {
		Query query = new Query(Criteria.where("dictionaryId").is(id)).with(Sort.by(Direction.DESC, "revision"))
				.limit(1);
		PortfolioVO portfolio = mongoTemplate.findOne(query, PortfolioVO.class);
		if (portfolio != null) {
			return portfolio.getRevision();
		}
		return null;
	}

	public List<Revision> getRevisions(String id) {
		Query query = new Query(Criteria.where("dictionaryId").is(id));
		List<PortfolioVO> portfolioList = mongoTemplate.find(query, PortfolioVO.class);
		if (portfolioList != null) {
			List<Revision> revisions = new ArrayList<>();
			for (PortfolioVO portfolio : portfolioList) {
				Revision revision = new Revision();
				revision.setRevision(portfolio.getRevision());
				revision.setStatus(portfolio.getStatus());
				revision.setPortfolioId(portfolio.getId());
				revisions.add(revision);
			}
			return revisions;
		}
		return null;
	}

	@Override
	public PortfolioVO createPortfolioRevision(String id, Integer revision) {
		return null;
	}

	@Override
	public PortfolioModel findPortfolioModelByportfolioIDAndModelId(String id, String modelId) {
		log("findAllPortfolioModels", id);
		PortfolioModel portfolioModel = baseRepository.findOne("portfolioID", id, "modelId", modelId,
				PortfolioModel.class);
		if (portfolioModel == null) {
			return new PortfolioModel();
		} else {
			return portfolioModel;
		}
	}

	public Integer getDDRevisions(String modelId, String id) {
		Query query = new Query(Criteria.where("modelId").is(modelId).and("portfolioID").is(id))
				.with(Sort.by(Direction.DESC, "revision")).limit(1);
		PortfolioModel portfolio = mongoTemplate.findOne(query, PortfolioModel.class);
		if (portfolio != null) {
			return portfolio.getRevision();
		}
		return null;
	}

	@Override
	public List<PortfolioModel> findPortfolioModelsWithAllRevisions(String id, String modelId) {
		log("findAllPortfolioModels", id);
		List<PortfolioModel> models = baseRepository.find("portfolioID", id, "modelId", modelId, PortfolioModel.class);
		return models;

	}

	@Override
	public PortfolioModel findPortfolioModelsWithRevisions(String id, String modelId, Integer revision) {
		log.debug("findPortfolioModelsWithRevisions :{}",revision);
		PortfolioModel model = baseRepository.findOne("portfolioID", id, "modelId", modelId, "revision", revision,
				PortfolioModel.class);
		if (model == null)
			model = baseRepository.findOne("portfolioID", id, "modelId", modelId, PortfolioModel.class);
		return model;

	}

	public void updatePortfolioModelStatusWithRevision(String id, String modelId, ModelStatus modelStatus,
			Integer revision) {
		Update update = new Update();
		Query query = new Query(
				Criteria.where("modelId").is(modelId).and("portfolioID").is(id).and("revision").is(revision));
		log.debug("updatePortfolioModelStatusWithRevision:{}",revision);
		PortfolioModel portfolioModel = mongoTemplate.findOne(query, PortfolioModel.class);
		if(portfolioModel == null) {
			query = new Query(Criteria.where("modelId").is(modelId).and("portfolioID")
					.is(id));
			update.set("revision",1);
		}
		update.set("status", modelStatus);
		mongoTemplate.upsert(query, update, PortfolioModel.class);
	}

	@Override
	public DeleteResult deletePortfolioModelByportfolioIDAndModelIdAndRevision(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.deleteRevision("portfolioID", model.getPortfolioID(), "modelId", model.getModelId(),
				"revision", model.getRevision(), PortfolioModel.class);
	}

	@Override
	public PortfolioModel findPortfolioModelByportfolioIDAndModelIdAndRevison(String id, String modelId,
			Integer revision) {
		log("findAllPortfolioModels", id);
		Criteria criteria = Criteria.where("portfolioID").is(id).and("modelId").is(modelId).and("revision").is(revision);
		Query query = new Query(criteria);

		PortfolioModel portfolioModel = mongoTemplate.findOne(query, PortfolioModel.class);
		if (portfolioModel == null) {
			return new PortfolioModel();
		} else {
			return portfolioModel;
		}
	}

	public void sendNotificationToSwagger(String jsessionid, PortfolioVO portfolioVO,
										  String message) {
		Query query = new Query();
		query.addCriteria(Criteria.where("dictionary.id").is(portfolioVO.getId()));
		List<SwaggerDictionary> swaggerDictionaries = mongoTemplate.find(query,
				SwaggerDictionary.class);
		try {
			for (int i = 0; i < swaggerDictionaries.size(); i++) {
				SwaggerDictionary swagger = swaggerDictionaries.get(i);
				String name = swagger.getName();
				Integer revision = swagger.getRevision();
				String oasVersion = swagger.getOasVersion();
				Query querySwagger = new Query();
				if (oasVersion.equalsIgnoreCase("2.0")) {
					querySwagger.addCriteria(Criteria.where("name").is(name).and("revision").is(revision));
					SwaggerVO swaggerVO = baseRepository.findOne("name", name, "revision", revision,
							SwaggerVO.class);
					String swaggerDetails = swaggerVO.getSwagger();
					NotificationDetails notificationDetails = new NotificationDetails();
					notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy(), portfolioVO.getCreatedBy()));
					notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
					notificationDetails.setNotification(message);
					notificationBusiness.createNotification(notificationDetails, jsessionid);

				} else if (oasVersion.equalsIgnoreCase("3.0")) {
					querySwagger.addCriteria(Criteria.where("name").is(name).and("revision").is(revision));
					Swagger3VO swagger3VO = mongoTemplate.findOne(querySwagger, Swagger3VO.class);
					String swaggerDetails = swagger3VO.getSwagger();
					NotificationDetails notificationDetails = new NotificationDetails();
					notificationDetails.setUserId(Arrays.asList(swagger3VO.getCreatedBy(), portfolioVO.getCreatedBy()));
					notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
					notificationDetails.setNotification(message);
					notificationBusiness.createNotification(notificationDetails, jsessionid);

				}
			}
		} catch (Exception e) {
			log.error("exception while creating notification", e.getMessage());
		}
	}
	public void sendNotificationForModel(String jsessionid, PortfolioModel portfolioModel,
										 String message) {
		Query query = new Query();
		query.addCriteria(Criteria.where("dictionary.id").is(portfolioModel.getPortfolioID())
				.and("dictionary.models.id")
				.is(portfolioModel.getId()));
		List<SwaggerDictionary> swaggerDictionaries = mongoTemplate.find(query,
				SwaggerDictionary.class);
		try {
			for (int i = 0; i < swaggerDictionaries.size(); i++) {
				SwaggerDictionary swagger = swaggerDictionaries.get(i);
				String name = swagger.getName();
				Integer revision = swagger.getRevision();
				String oasVersion = swagger.getOasVersion();
				Query querySwagger = new Query();

				if (oasVersion.equalsIgnoreCase("2.0")) {
					querySwagger.addCriteria(Criteria.where("name").is(name).and("revision").is(revision));
					SwaggerVO swaggerVO = baseRepository.findOne("name", name, "revision", revision,
							SwaggerVO.class);
					String swaggerDetails = swaggerVO.getSwagger();
					NotificationDetails notificationDetails = new NotificationDetails();
					notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy(), portfolioModel.getCreatedBy()));
					notificationDetails.setType(NotificationType.fromValue("Model"));
					notificationDetails.setNotification(message);
					notificationBusiness.createNotification(notificationDetails, jsessionid);

				} else if (oasVersion.equalsIgnoreCase("3.0")) {
					querySwagger.addCriteria(Criteria.where("name").is(name).and("revision").is(revision));
					Swagger3VO swagger3VO = mongoTemplate.findOne(querySwagger, Swagger3VO.class);
					String swaggerDetails = swagger3VO.getSwagger();
					NotificationDetails notificationDetails = new NotificationDetails();
					notificationDetails.setUserId(Arrays.asList(swagger3VO.getCreatedBy(), portfolioModel.getCreatedBy()));
					notificationDetails.setType(NotificationType.fromValue("Model"));
					notificationDetails.setNotification(message);
					notificationBusiness.createNotification(notificationDetails, jsessionid);

				}
			}

		} catch (Exception e) {
			log.error("exception while creating notification", e.getMessage());
		}
	}

	@Override
	public PortfolioModel findPortfolioModelsByportfolioIDAndModelName(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.findOne("portfolioID", model.getPortfolioID(), "modelName", model.getModelName(),
				PortfolioModel.class);
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		return list;
	}

	@Override
	public String getGlobalRule() {
		Query query = Query.query(Criteria.where("ruleSetType").is("schema").and("isGlobalRuleSet").is(true));
		Document ruleset =  mongoTemplate.findOne(query,Document.class,"Linter.RuleSet");
		return ruleset!=null?ruleset.get("_id").toString():null;
	}

	@Override
	public List<DDSchema> getModels(String id){
		Query query = Query.query(Criteria.where("portfolioID").is(id));
		List<PortfolioModel> models=mongoTemplate.find(query, PortfolioModel.class);
		List<DDSchema> modelList=new ArrayList<>();
		for(PortfolioModel model : models){
			DDSchema ddSchema=new DDSchema();
			ddSchema.setModelId(model.getModelId());
			ddSchema.setRevision(model.getRevision());
			modelList.add(ddSchema);
		}
		return modelList;
	}

	@Override
	public PortfolioReport getModelswithRulesets(String id){
		PortfolioReport report =new PortfolioReport();
		report.setPortfolioId(id);
		List<PortfolioModel> models=new ArrayList<>();
		Query query = Query.query(Criteria.where("portfolioID").is(id));
		List<PortfolioModel> allModels=mongoTemplate.find(query, PortfolioModel.class);
		List<PortfolioModels> modelList=new ArrayList<>();
		for(PortfolioModel model : allModels){
			PortfolioModels portfolioModels=new PortfolioModels();
			portfolioModels.setModelId(model.getModelId());
			portfolioModels.setRevision(model.getRevision());
			String globalId= getGlobalRule();
			List<String> rulesets=model.getRuleSetIds();
			if(model.getRuleSetIds()!=null&&!model.getRuleSetIds().contains(globalId)&&globalId!=null){
				rulesets.add(globalId);
			}
			else if(rulesets==null)
			{
				rulesets=new ArrayList<String>();
				rulesets.add(globalId);
			}
			portfolioModels.setRuleSetIds(rulesets);
			modelList.add(portfolioModels);
		}
		report.setModels(modelList);
		return report;
	}
	@Override
	public void sync2Repo(String portfolioId, DictionaryScmUpload dictionaryScmUpload) throws Exception {

		//Push to SCM (Same as done in Editor Lite)
		if (dictionaryScmUpload.getDictionary() == null) {
			log.error("DataDictionary is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1090"), "Dictionary is empty"),
					"SCM-1010");
		}
		if (dictionaryScmUpload.getDictionaryName() == null) {
			log.error("Dictionary Name is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1091"), "Dictionary Name is empty"),
					"SCM-1020");
		}
		if (dictionaryScmUpload.getScmSource() == null) {
			log.error("SCM Source is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1030"), "Invalid Scm source"),
					"SCM-1030");
		}
		if (dictionaryScmUpload.getRepoName() == null) {
			log.error("SCM reponame is empty");
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("SCM-1040"), "Scm Repository name is empty"), "SCM-1040");
		}
		if (dictionaryScmUpload.getBranch() == null) {
			log.error("SCM branch is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1050"), "Scm branch is empty"),
					"SCM-1050");
		}
		if (dictionaryScmUpload.getHostUrl() == null) {
			log.error("SCM hostUrl is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1060"), "Scm host url is empty"),
					"SCM-1060");
		}
		if (dictionaryScmUpload.getAuthType().equalsIgnoreCase("TOKEN") && dictionaryScmUpload.getToken() == null) {
			log.error("SCM Token is empty");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1070"), "Scm Token is empty"),
					"SCM-1070");
		}
		if (dictionaryScmUpload.getAuthType()
				.equalsIgnoreCase("NONE") && (dictionaryScmUpload.getUsername() == null || dictionaryScmUpload.getPassword() == null)) {
			log.error("Invalid SCM Credentials");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1080"), "Invalid Credentials"),
					"SCM-1080");
		}
		log.info("begin : upload DD to SCM");

		Map<String,Map<String,String>> modelMap = new HashMap<>();
		try{
			Query query = new Query();
			query.addCriteria(Criteria.where("portfolioID").is(portfolioId));
			List<PortfolioModel> models = mongoTemplate.find(query,PortfolioModel.class);

			for(PortfolioModel model : models){
				if(!modelMap.containsKey(model.getModelName())){
					modelMap.put(model.getModelName(),new HashMap<>());
				}
				modelMap.get(model.getModelName()).put(model.getRevision().toString(),model.getModel());
			}

			String jsonModelMap = new ObjectMapper().writeValueAsString(modelMap);
			if(jsonModelMap != null && !jsonModelMap.isEmpty()){
				dictionaryScmUpload.setDictionary(jsonModelMap);
			}

		}catch (Exception ex){
			log.error("Couldn't Fetch All Dictionary Models. Syncing as Latest Active Model Revision Only:" + ex.getMessage());
		}

		ObjectMapper om = new ObjectMapper();
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		File file = createDDFile(dictionaryScmUpload.getDictionaryName(), om.readTree(dictionaryScmUpload.getDictionary()).toPrettyString(),
				dictionaryScmUpload.getFolderName());
		String commitMessage = dictionaryScmUpload.getCommitMessage();
		if (commitMessage == null) {
			commitMessage = "Pushed " + dictionaryScmUpload.getDictionaryName() + " to " + dictionaryScmUpload.getFolderName() + " in " + dictionaryScmUpload.getRepoName();
		}

		if (dictionaryScmUpload.getAuthType() != null && dictionaryScmUpload.getAuthType().equalsIgnoreCase("TOKEN")) {
			scmUtilImpl.pushFilesToSCMBase64(file, dictionaryScmUpload.getRepoName(), "TOKEN", dictionaryScmUpload.getToken(),
					dictionaryScmUpload.getHostUrl(), dictionaryScmUpload.getScmSource(), dictionaryScmUpload.getBranch(), commitMessage);

			if(!dictionaryScmUpload.getScmSource().equalsIgnoreCase("bitbucket")){
				//Bitbucket tokens are > 53 bytes hence invalid block size for encryption
				dictionaryScmUpload.setToken(rsaEncryption.encryptText(dictionaryScmUpload.getToken()));
			}

			if(dictionaryScmUpload.getUsername() != null){
				dictionaryScmUpload.setUsername(rsaEncryption.encryptText(dictionaryScmUpload.getUsername()));
			}

			if(dictionaryScmUpload.getPassword() != null){
				dictionaryScmUpload.setPassword(rsaEncryption.encryptText(dictionaryScmUpload.getPassword()));
			}
		} else {
			scmUtilImpl.pushFilesToSCM(file, dictionaryScmUpload.getRepoName(), dictionaryScmUpload.getUsername(), dictionaryScmUpload.getPassword(),
					dictionaryScmUpload.getHostUrl(), dictionaryScmUpload.getScmSource(), dictionaryScmUpload.getBranch(), commitMessage);
			dictionaryScmUpload.setUsername(rsaEncryption.encryptText(dictionaryScmUpload.getUsername()));
			dictionaryScmUpload.setPassword(rsaEncryption.encryptText(dictionaryScmUpload.getPassword()));
		}
		file.delete();

		//Create or Update Git Integration Record
		dictionaryScmUpload.setPortfolioId(portfolioId);

		deSyncFromRepo(portfolioId);
		mongoTemplate.save(dictionaryScmUpload);
	}

	private File createDDFile(String dictionaryName,String dataDictionaryJson, String folder) throws IOException {
		String separatorChar = String.valueOf(File.separatorChar);
		String revStr = separatorChar + "datadictionary" + separatorChar + dictionaryName;
		folder = folder != null && !folder.isEmpty() ? folder + revStr : "DataDictionary" + revStr;
		String location = System.getProperty("java.io.tmpdir") + System.currentTimeMillis();
		String fileLocation = location + separatorChar + folder + separatorChar + dictionaryName + ".json";
		File file = new File(fileLocation);
		file.getParentFile().mkdirs();
		file.createNewFile();
		Files.write(Paths.get(fileLocation), dataDictionaryJson.getBytes());
		return new File(location);
	}

	@Override
	public void deSyncFromRepo(String portfolioId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(portfolioId));
		mongoTemplate.findAndRemove(query,DictionaryScmUpload.class);
	}
	@Override
	public DictionaryScmUpload getGitIntegrations(String jsessionid, String portfolioId) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(portfolioId));
		DictionaryScmUpload dictionaryScmUpload = mongoTemplate.findOne(query,DictionaryScmUpload.class);

		if(dictionaryScmUpload != null){
			if(dictionaryScmUpload.getAuthType().equalsIgnoreCase("TOKEN") && !dictionaryScmUpload.getScmSource().equalsIgnoreCase("bitbucket")){
				dictionaryScmUpload.setToken(rsaEncryption.decryptText(dictionaryScmUpload.getToken()));
			}

			if(dictionaryScmUpload.getUsername() != null){
				dictionaryScmUpload.setUsername(rsaEncryption.decryptText(dictionaryScmUpload.getUsername()));
			}

			if(dictionaryScmUpload.getPassword() != null){
				dictionaryScmUpload.setPassword(rsaEncryption.decryptText(dictionaryScmUpload.getPassword()));
			}
		}

		return dictionaryScmUpload;

	}
}