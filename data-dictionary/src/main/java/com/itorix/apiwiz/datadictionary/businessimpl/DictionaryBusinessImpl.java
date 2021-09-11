package com.itorix.apiwiz.datadictionary.businessimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.ModelStatus;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.mongodb.client.result.DeleteResult;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class DictionaryBusinessImpl implements DictionaryBusiness {

	private static final Logger logger = LoggerFactory.getLogger(DictionaryBusinessImpl.class);
	@Autowired
	BaseRepository baseRepository;
	@Autowired
	private MongoTemplate mongoTemplate;

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
		PortfolioVO vo = baseRepository.save(portfolioVO);
		log("createPortfolio", portfolioVO.getInteractionid(), vo);
		return vo;
	}

	public PortfolioVO findPortfolio(PortfolioVO portfolioVO) {
		log("findPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		return baseRepository.findOne("name", portfolioVO.getName(), PortfolioVO.class);
	}

	public PortfolioHistoryResponse findAllPortfolios(String interactionid, int offset, int paeSize) {
		log("findPortfolio", interactionid);
		Query query = new Query().with(Sort.by(Direction.DESC, "mts")).skip(offset > 0 ? ((offset - 1) * paeSize) : 0)
				.limit(paeSize);
		PortfolioHistoryResponse historyResponse = new PortfolioHistoryResponse();
		List<PortfolioVO> portfolios = mongoTemplate.find(query, PortfolioVO.class);
		if (portfolios != null) {
			for (PortfolioVO portfolio : portfolios) {
				List<Object> strModels = new ArrayList<Object>();
				List<PortfolioModel> dataModels = findPortfolioModelsByportfolioID(portfolio);
				if (dataModels != null)
					for (PortfolioModel model : dataModels) {
						try {
							String name = model.getModelName();
							strModels.add(name);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				portfolio.setModels(strModels);
			}
			Long counter = mongoTemplate.count(new Query(), PortfolioVO.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(paeSize);
			historyResponse.setPagination(pagination);
			historyResponse.setData(portfolios);
		}
		return historyResponse;
	}

	public List<PortfolioVO> findAllPortfolioSummary(String interactionid) {
		log("findPortfolio", interactionid);
		List<PortfolioVO> portfolios = baseRepository.findAll(PortfolioVO.class);
		if (portfolios != null) {
			for (PortfolioVO portfolio : portfolios) {
				portfolio.setDescription(null);
				portfolio.setSummary(null);
				List<Object> strModels = new ArrayList<Object>();
				List<PortfolioModel> dataModels = findPortfolioModelsByportfolioID(portfolio);
				if (dataModels != null)
					for (PortfolioModel model : dataModels) {
						try {
							String name = model.getModelName();
							strModels.add(name);
						} catch (Exception e) {
							e.printStackTrace();
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
					JsonNode jsonNode = mapper.readTree(model.getModel());
					strModels.add(jsonNode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		portfolio.setModels(strModels);
		return portfolio;
	}

	public PortfolioVO findPortfolioById(PortfolioVO portfolioVO) {
		log("findPortfolio", portfolioVO.getInteractionid(), portfolioVO);
		return baseRepository.findById(portfolioVO.getId(), PortfolioVO.class);
	}

	public DeleteResult deletePortfolioById(PortfolioVO portfolioVO) {
		log("deletePortfolio", portfolioVO.getInteractionid());
		baseRepository.delete("portfolioID", portfolioVO.getId(), PortfolioModel.class);
		return baseRepository.delete(portfolioVO.getId(), PortfolioVO.class);
	}

	public PortfolioModel createPortfolioModel(PortfolioModel model) {
		log("createPortfolioModel", model.getInteractionid(), model);
		Query query = new Query(
				Criteria.where("modelName").is(model.getModelName()).and("portfolioID").is(model.getPortfolioID()));
		Update update = new Update();
		update.set("model", model.getModel());
		update.set("mts", model.getMts());
		update.set("status", model.getStatus());
		mongoTemplate.upsert(query, update, PortfolioModel.class);
		return model;
	}

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.find("portfolioID", model.getPortfolioID(), PortfolioModel.class);
	}

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioVO model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.find("portfolioID", model.getId(), PortfolioModel.class);
	}

	public PortfolioModel findPortfolioModelsByportfolioIDAndModelName(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.findOne("portfolioID", model.getPortfolioID(), "modelName", model.getModelName(),
				PortfolioModel.class);
	}

	public DeleteResult deletePortfolioModelByportfolioIDAndModelName(PortfolioModel model) {
		log("findAllPortfolioModels", model.getInteractionid());
		return baseRepository.delete("portfolioID", model.getPortfolioID(), "modelName", model.getModelName(),
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
}
