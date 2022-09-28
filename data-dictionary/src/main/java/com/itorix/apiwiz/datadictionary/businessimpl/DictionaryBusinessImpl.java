package com.itorix.apiwiz.datadictionary.businessimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.mail.MailUtil;
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
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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
		List<String> dictionaryIds = trimList(uniqueDictionaryIds, offset, pageSize);
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

	private List<String> trimList(List<String> ids, int offset, int pageSize) {
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
		String dictionaryId = portfolioVO.getDictionaryId();
		List<PortfolioVO> portfolioIds = baseRepository.find("dictionaryId", dictionaryId, PortfolioVO.class);
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
		List<PortfolioVO> allPortfolios = mongoTemplate.find(query, PortfolioVO.class);
		ObjectNode portfolioList = new ObjectMapper().createObjectNode();
		ArrayNode responseFields = new ObjectMapper().createArrayNode();
		for (PortfolioVO vo : allPortfolios) {
			SearchItem item = new SearchItem();
			item.setId(vo.getId());
			item.setName(vo.getName());
			responseFields.addPOJO(item);
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

}