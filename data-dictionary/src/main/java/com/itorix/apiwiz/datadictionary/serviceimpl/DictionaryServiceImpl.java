package com.itorix.apiwiz.datadictionary.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.*;
import com.itorix.apiwiz.datadictionary.service.DictionaryService;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import com.itorix.apiwiz.design.studio.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.springframework.web.client.RestTemplate;

@CrossOrigin
@Slf4j
@RestController
/**
 * PortfolioController .
 *
 * @author itorix.inc
 */
public class DictionaryServiceImpl implements DictionaryService {

	@Autowired
	DictionaryBusiness dictionaryBusiness;

	@Autowired
	NotificationBusiness notificationBusiness;

	@Autowired
	RestTemplate restTemplate;

	private static final Logger logger = LoggerFactory.getLogger(DictionaryServiceImpl.class);

	@Value("${linting.api.url:null}")
	private String lintingUrl;

	@Value("${linting.api.lintDD:null}")
	private String lintDD;

	@Value("${linting.api.lintPortfolio:null}")
	private String lintPortfolio;

	/**
	 * Using this method we can create the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> createPortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody PortfolioVO portfolioVO)
			throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (portfolioVO == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1000"), "Portfolio-1000");
		} else {
			portfolioVO.setInteractionid(interactionid);
			PortfolioVO vo = dictionaryBusiness.findPortfolio(portfolioVO);
			if (vo != null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Portfolio-1001"), portfolioVO.getName()),
						"Portfolio-1001");
			}
			portfolioVO = dictionaryBusiness.createPortfolio(portfolioVO);

			headers.add("Access-Control-Expose-Headers", "X-datadictionary-id");
			headers.add("X-datadictionary-id", portfolioVO.getId());
		}
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setNotification(
				"Portfolio has been created ".concat(portfolioVO.getName()));
		notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
		notificationBusiness.createNotification(notificationDetails, jsessionid);

		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Using this method we can create the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> updatePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody PortfolioVO portfolioVO)
			throws Exception {

		if (portfolioVO == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1000"), "Portfolio-1000");
		} else {
			portfolioVO.setInteractionid(interactionid);
			PortfolioVO vo = dictionaryBusiness.findPortfolioById(portfolioVO);
			if (vo == null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Portfolio-1001"), portfolioVO.getName()),
						"Portfolio-1001");
			}
			portfolioVO = dictionaryBusiness.createPortfolio(portfolioVO);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
			notificationDetails.setNotification(String.format("Data Dictionary %s revision %s has been updated",portfolioVO.getName(),portfolioVO.getRevision()));
			notificationBusiness.createNotification(notificationDetails, jsessionid);

			// dictionaryBusiness.sendNotificationForDD(portfolioVO);
		}
		PortfolioReport report=dictionaryBusiness.getModelswithRulesets(portfolioVO.getId());
		initiateLintingforPortfolio(jsessionid,report);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this method we can get the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<PortfolioHistoryResponse> getPortfolioOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception {
		PortfolioHistoryResponse list = dictionaryBusiness.findAllPortfolios(interactionid, offset, pageSize);
		return new ResponseEntity<PortfolioHistoryResponse>(list, HttpStatus.OK);
	}

	/**
	 * Using this method we can get the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<PortfolioHistoryResponse> getPortfolioOverviewV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception {
		PortfolioHistoryResponse list = dictionaryBusiness.findAllPortfoliosV2(interactionid, offset, pageSize);
		return new ResponseEntity<PortfolioHistoryResponse>(list, HttpStatus.OK);
	}

	/**
	 * Using this method we can get the Portfolios.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<List<PortfolioVO>> getPortfolios(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		List<PortfolioVO> list = dictionaryBusiness.findAllPortfolioSummary(interactionid);
		return new ResponseEntity<List<PortfolioVO>>(list, HttpStatus.OK);
	}

	/**
	 * Using this method we can get the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<PortfolioVO> getPortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception {
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		vo = dictionaryBusiness.getPortfolioById(vo);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		return new ResponseEntity<PortfolioVO>(vo, HttpStatus.OK);
	}

	public ResponseEntity<?> getPortfolioRevisions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception {
		List<Revision> revisions = dictionaryBusiness.getRevisions(id);
		if (revisions == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		return new ResponseEntity<>(revisions, HttpStatus.OK);
	}

	/**
	 * Using this method we can Delete the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> deletePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception {
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		PortfolioVO portfolioVO = dictionaryBusiness.findPortfolioById(vo);
		if ( portfolioVO == null){
			log.error("PortfolioVO not found: {}", id);
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), id), "Portfolio-1003");
		}
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
		notificationDetails.setNotification("Data Dictionary has been Deleted -".concat(portfolioVO.getName()));
		notificationBusiness.createNotification(notificationDetails, jsessionid);

		dictionaryBusiness.deletePortfolioById(portfolioVO);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * This method it will update the Portfolio models.
	 *
	 * @param portfolioVO
	 * @param interactionid
	 * @param jsessionid
	 * @param modelName
	 * @param revision
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> updatePortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id, String modelId,
			@RequestBody String body) throws Exception {
		log.info("Create or Update Portfolio Models ");
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		PortfolioVO portfolioVO = dictionaryBusiness.findPortfolioById(vo);
		if (portfolioVO == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		} else {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(body);
			String name = (String) jsonNode.get("name").asText();
			PortfolioModel model = new PortfolioModel();

//			if (modelId == null) {
//				model = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelId(id, modelId);
//			}
			if (modelId != null) {
				Integer revisions = dictionaryBusiness.getDDRevisions(modelId, id);
				model = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelIdAndRevison(id, modelId,
						revisions);
			}
			//String modelId = model.getId();
			if (StringUtils.isEmpty(model.getModelName())) {
				model.setInteractionid(interactionid);
				model.setPortfolioID(id);
				model.setMts(System.currentTimeMillis());
				model.setModelName(name);
				model.setModel(body);
				model.setRevision(1);
				model.setId(null);
				model.setModelId(UUID.randomUUID().toString().replaceAll("-", ""));
				dictionaryBusiness.createnewPortfolioModel(model);
				NotificationDetails notificationDetails = new NotificationDetails();
				notificationDetails.setUserId(Arrays.asList(model.getCreatedBy()));
				notificationDetails.setType(NotificationType.fromValue("Model"));
				notificationDetails.setNotification("Model has been created ".concat(model.getModelName()));
				notificationBusiness.createNotification(notificationDetails, jsessionid);
			} else {
				Integer revisions = dictionaryBusiness.getDDRevisions(modelId, id);
				Integer newRevision;
				if (revisions != null) {
					newRevision = revisions + 1;
				} else {
					newRevision = 1;
				}
				model.setInteractionid(interactionid);
				model.setPortfolioID(id);
				model.setMts(System.currentTimeMillis());
				model.setModelName(name);
				model.setModel(body);
				model.setRevision(newRevision);
				model.setId(null);
				model.setModelId(modelId);
				model = dictionaryBusiness.createnewPortfolioModel(model);
				model = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelIdAndRevison(id, modelId,
						revisions);
				NotificationDetails notificationDetails = new NotificationDetails();
				notificationDetails.setUserId(Arrays.asList(model.getCreatedBy()));
				notificationDetails.setType(NotificationType.fromValue("Model"));
				notificationDetails.setNotification(String.format("Model revision has been created %s",model.getModelName()));
				notificationBusiness.createNotification(notificationDetails, jsessionid);
				initiateLinting(jsessionid, model.getPortfolioID(),model.getModelId(),newRevision,model.getRuleSetIds());
				// dictionaryBusiness.sendNotificationForDDModel(model);
			}

		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary/{id}/schemas/{modelId}/revision/{revision}")
	public ResponseEntity<Void> updatePortfolioModelsWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id,
			@PathVariable("modelId") String modelId,
			@PathVariable("revision") Integer revision,
			@RequestBody String body) throws Exception {
		log.info("Update Portfolio Models with Revision");
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		PortfolioVO portfolioVO = dictionaryBusiness.findPortfolioById(vo);
		if (portfolioVO == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), id),
					"Portfolio-1003");
		} else {
			PortfolioModel model = new PortfolioModel();
			model.setInteractionid(interactionid);
			model.setPortfolioID(id);
			model.setMts(System.currentTimeMillis());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(body);
			String name = (String) jsonNode.get("name").asText();
			model.setRevision(revision);
			model.setModelName(name);
			model.setModelId(modelId);
			model.setModel(body);
			dictionaryBusiness.updateModelRevision(model);
			//PortfolioModel modelWithRevision = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelIdAndRevison(id, modelId, revision);
			PortfolioModel models = dictionaryBusiness
					.findPortfolioModelByportfolioIDAndModelIdAndRevison(id, modelId, revision);
			dictionaryBusiness.updateModelRevision(model);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setUserId(Arrays.asList(models.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Model"));
			notificationDetails.setNotification(String.format("Model %s revision %s has been updated",models.getModelName(),models.getRevision()));
			notificationBusiness.createNotification(notificationDetails, jsessionid);
			// dictionaryBusiness.sendNotificationForDDModel(modelWithRevision);
			initiateLinting(jsessionid, models.getPortfolioID(),models.getModelId(),models.getRevision(),models.getRuleSetIds());
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}

	}

	public ResponseEntity<Void> getPortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception {
		return null;
	}

	/**
	 * Using this method we can create the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 *
	 * @return
	 *
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPortfolioModelNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@RequestParam(name = "filterby", required = false) String filterby) throws Exception {
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		vo = dictionaryBusiness.getPortfolioById(vo);
		if (vo != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Object> strModels = new ArrayList<Object>();
			List<Object> models = vo.getModels();
			if (models != null)
				for (Object model : models) {
					try {
						String json = mapper.writeValueAsString(model);
						JsonNode jsonNode = mapper.readTree(json);
						String name = jsonNode.get("name").textValue();
						strModels.add(name);
					} catch (Exception e) {
						log.error("Exception occurred", e);
					}
				}
			vo.setModels(strModels);
		}
		return new ResponseEntity<Object>(vo, HttpStatus.OK);
	}

	public ResponseEntity<Object> getPortfolioModel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name,
			@RequestParam(name = "filterby", required = false) String filterby) throws Exception {
		PortfolioModel model = new PortfolioModel();
		model.setPortfolioID(id);
		model.setModelName(model_name);
//		model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelId(model);
		model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelName(model);
		if (model == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(model.getModel());
		return new ResponseEntity<Object>(jsonNode, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPortfolioModelWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId,
			@PathVariable(name = "revision", required = false) Integer revision) throws Exception {
		log.info("Fetching PortfolioModel with Revision");
		// List<PortfolioModel> model = new ArrayList<>();
		if (revision != null) {
			// Integer revisions = revision.get("revision");
			PortfolioModel portfolioModel = new PortfolioModel();
			portfolioModel = dictionaryBusiness.findPortfolioModelsWithRevisions(id, modelId, revision);
			if(portfolioModel != null){
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonNode = mapper.readTree(portfolioModel.getModel());
				ModelWithRevision modelWithRevision = new ModelWithRevision();
				modelWithRevision.setStatus(portfolioModel.getStatus());
				if (portfolioModel.getRevision() != null) {
					modelWithRevision.setRevision(portfolioModel.getRevision());
				} else {
					modelWithRevision.setRevision(1);
				}
				modelWithRevision.setModel(jsonNode);
				return new ResponseEntity<Object>(modelWithRevision, HttpStatus.OK);
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1011"), "Portfolio-1011");
			}
		}
		else {
			PortfolioModel model = new PortfolioModel();
			model.setPortfolioID(id);
			//model.setModelName(model_name);
			model.setModelId(modelId);
			model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelId(model);
			if (model == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), id),
						"Portfolio-1003");
			}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(model.getModel());
			ModelWithRevision modelWithRevision = new ModelWithRevision();
			modelWithRevision.setStatus(model.getStatus());
			if (model.getRevision() != null) {
				modelWithRevision.setRevision(model.getRevision());
			} else {
				modelWithRevision.setRevision(1);
			}
			modelWithRevision.setModel(jsonNode);
			return new ResponseEntity<Object>(modelWithRevision, HttpStatus.OK);

		}
	}

	@Override
	public ResponseEntity<Object> getAllPortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId,
			@PathVariable(name = "revision", required = false) Integer revision) throws Exception {
		List<PortfolioModel> model = new ArrayList<>();
		model = dictionaryBusiness.findPortfolioModelsWithAllRevisions(id, modelId);
		List<ModelWithRevision> modelWithRevisionList = new ArrayList<>();
		for (int i = 0; i < model.size(); i++) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(model.get(i).getModel());
			ModelWithRevision modelWithRevision = new ModelWithRevision();
			modelWithRevision.setStatus(model.get(i).getStatus());
			modelWithRevision.setRevision(model.get(i).getRevision());
			modelWithRevision.setModel(jsonNode);
			modelWithRevision.setModelId(model.get(i).getModelId());
			modelWithRevisionList.add(modelWithRevision);
		}
		return new ResponseEntity<Object>(modelWithRevisionList, HttpStatus.OK);
	}

	/**
	 * Using this method we can create the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> deletePortfolioModelByName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name) throws Exception {
		log.info("Delete All Model Revisions");
		PortfolioModel model = new PortfolioModel();
		List<PortfolioModel> model1 = new ArrayList<>();
		model1 = dictionaryBusiness.findPortfolioModelsWithAllRevisions(id, model_name);
		if(model1.isEmpty()){
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), id),
					"Portfolio-1003");
		}
		PortfolioModel models = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelId(id,
				model_name);

		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setUserId(Arrays.asList(models.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Model"));
		notificationDetails.setNotification(String.format("Model %s has been Deleted ",models.getModelName()));
		notificationBusiness.createNotification(notificationDetails, jsessionid);
		dictionaryBusiness.deletePortfolioModelByModelId(model_name);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException {
		return new ResponseEntity<Object>(dictionaryBusiness.portfolioSearch(interactionid, name, limit),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updatePortfolioModelStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("modelStatus") ModelStatus modelStatus)
			throws ItorixException {
		dictionaryBusiness.updatePortfolioModelStatus(id, modelId, modelStatus);
		PortfolioModel model = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelId(id,
				modelId);
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setUserId(Arrays.asList(model.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Model"));
		notificationDetails.setNotification(String.format("Model %s revision %s status has been updated",model.getModelName(),model.getRevision()));
		notificationBusiness.createNotification(notificationDetails, jsessionid);
		initiateLinting(jsessionid, model.getPortfolioID(),model.getModelId(),model.getRevision(),model.getRuleSetIds());
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> createPortfolioRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("revision") Integer revision) throws Exception {
		PortfolioVO portfolioVO = dictionaryBusiness.getPortfolioByRevision(id, revision);
		if (portfolioVO == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		PortfolioVO vo = new PortfolioVO();
		vo.setSummary(portfolioVO.getSummary());
		vo.setDescription(portfolioVO.getDescription());
		vo.setName(portfolioVO.getName());
		Integer rev = dictionaryBusiness.getMaxRevision(id);
		vo.setRevision(rev + 1);
		vo.setDictionaryId(portfolioVO.getDictionaryId());
		vo.setStatus(DictionaryStatus.Draft.toString());

		portfolioVO = dictionaryBusiness.createPortfolioRevision(vo, portfolioVO.getId());
		PortfolioVO portfolioVOs = dictionaryBusiness.getPortfolioByRevision(id, revision);
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setNotification(
				"Portfolio  revision has been created ".concat(portfolioVO.getName()));
		notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
		notificationBusiness.createNotification(notificationDetails, jsessionid);
		PortfolioReport report=dictionaryBusiness.getModelswithRulesets(portfolioVO.getId());
		initiateLintingforPortfolio(jsessionid,report);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> getPortfolioRevision(String interactionid, String jsessionid, String id, Integer revision)
			throws Exception {
		PortfolioVO vo = dictionaryBusiness.getPortfolioByRevision(id, revision);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		vo = dictionaryBusiness.getPortfolioById(vo);
		return new ResponseEntity<PortfolioVO>(vo, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deletePortfolioRevision(String interactionid, String jsessionid, String id,
													 Integer revision) throws Exception {
		PortfolioVO vo = dictionaryBusiness.getPortfolioByRevision(id, revision);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		PortfolioVO portfolioVO = dictionaryBusiness.getPortfolioByRevision(id, revision);
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setNotification(
				String.format("Portfolio %s - revision %s has been deleted ",portfolioVO.getName(),portfolioVO.getRevision()));
		notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
		notificationBusiness.createNotification(notificationDetails, jsessionid);
		dictionaryBusiness.deletePortfolioByIdAndRevision(vo);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> updatePortfolioStatus(String interactionid, String jsessionid, String id, Integer revision,
												   Revision status) throws Exception {
		PortfolioVO vo = dictionaryBusiness.getPortfolioByRevision(id, revision);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		String enumStatus = status.getStatus();
		if (EnumUtils.isValidEnum(DictionaryStatus.class, enumStatus)) {
			vo.setStatus(status.getStatus());
			dictionaryBusiness.createPortfolio(vo);
			PortfolioVO portfolioVO = dictionaryBusiness.getPortfolioByRevision(id, revision);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setUserId(Arrays.asList(portfolioVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Data Dictionary"));
			notificationDetails.setNotification(String.format("Data Dictionary %s revision %s status has been updated",portfolioVO.getName(),portfolioVO.getRevision()));
			notificationBusiness.createNotification(notificationDetails, jsessionid);
			PortfolioReport report=dictionaryBusiness.getModelswithRulesets(portfolioVO.getId());
			initiateLintingforPortfolio(jsessionid,report);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1017"), id),
					"Portfolio-1017");
		}
	}

	@Override
	public ResponseEntity<?> updatePortfolioModelStatusWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("modelStatus") ModelStatus modelStatus,
			@PathVariable("revision") Integer revision) throws ItorixException {
		log.info("Change Status of a particular model revision");
		dictionaryBusiness.updatePortfolioModelStatusWithRevision(id, modelId, modelStatus, revision);
		PortfolioModel model = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelIdAndRevison(
				id, modelId, revision);
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setUserId(Arrays.asList(model.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Model"));
		notificationDetails.setNotification(String.format("Model %s revision %s status has been update",model.getModelName(),model.getRevision()));
		notificationBusiness.createNotification(notificationDetails, jsessionid);
		initiateLinting(jsessionid, model.getPortfolioID(),model.getModelId(),model.getRevision(),model.getRuleSetIds());
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<Void> deletePortfolioModelByIdWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("revision") Integer revision)
			throws ItorixException {
		log.info("Delete a particular Model Revision");
		PortfolioModel model = new PortfolioModel();
		model.setInteractionid(interactionid);
		model.setPortfolioID(id);
		model.setModelId(modelId);
		//model.setModelName(model_name);
		model.setRevision(revision);
		PortfolioModel model1 = new PortfolioModel();
		model1 = dictionaryBusiness.findPortfolioModelsWithRevisions(model.getPortfolioID(), model.getModelId(),
				model.getRevision());
		if(null == model1){
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), id),
					"Portfolio-1003");
		}
		NotificationDetails notificationDetails = new NotificationDetails();
		notificationDetails.setUserId(Arrays.asList(model1.getCreatedBy()));
		notificationDetails.setType(NotificationType.fromValue("Model"));
		notificationDetails.setNotification(String.format("Model %s - revision %s has been Deleted ",model1.getModelName(),model1.getRevision()));
		notificationBusiness.createNotification(notificationDetails, jsessionid);

		dictionaryBusiness.deletePortfolioModelByportfolioIDAndModelIdAndRevision(model1);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	@Override
	public ResponseEntity<?> sync2Repo(String interactionid, String jsessionid, String portfolioId,
									   DictionaryScmUpload dictionaryScmUpload) throws Exception {
		dictionaryBusiness.sync2Repo(portfolioId, dictionaryScmUpload);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	@Override
	public ResponseEntity<?> getGitIntegrations(String interactionid, String jsessionid, String portfolioId)
			throws Exception {
		return new ResponseEntity<>(dictionaryBusiness.getGitIntegrations(jsessionid,portfolioId),HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> deSyncFromRepo(String interactionid, String jsessionid, String portfolioId)
			throws Exception {
		dictionaryBusiness.deSyncFromRepo(portfolioId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private void initiateLinting(String jsessionid,
								 String dictionaryId,String modelId,Integer revision,List<String> ruleSetIds) {
		try {
			String globalRule=dictionaryBusiness.getGlobalRule();
			if(globalRule!=null&&ruleSetIds!=null&&!ruleSetIds.contains(globalRule))
			{
				ruleSetIds.add(globalRule);
			}
			else if(globalRule!=null&&ruleSetIds==null){
				ruleSetIds=new ArrayList<String>();
				ruleSetIds.add(globalRule);
			}
			List<DDSchema> models = new ArrayList<>();
			if(modelId!=null){
				DDSchema model=new DDSchema();
				model.setModelId(modelId);
				model.setRevision(revision);
				models.add(model);
			}
			else {
				models = dictionaryBusiness.getModels(dictionaryId);
			}
			DataDictionaryReport dataDictionaryReport = new DataDictionaryReport();
			dataDictionaryReport.setDictionaryId(dictionaryId);
			dataDictionaryReport.setModels(models);
			dataDictionaryReport.setRuleSetId(ruleSetIds);
			callLintingAPI(dataDictionaryReport, jsessionid);
		} catch (Exception ex) {
			logger.error("Error while calling linting API {} ", ex.getMessage());
		}
	}


	private void initiateLintingforPortfolio(String jsessionid,PortfolioReport report) {
		try {
			callLintingAPIForPortfolio(report, jsessionid);
		} catch (Exception ex) {
			logger.error("Error while calling linting API {} ", ex.getMessage());
		}
	}

	private void callLintingAPI(DataDictionaryReport dataDictionaryReport, String jsessionid) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("jsessionid", jsessionid);
		HttpEntity<DataDictionaryReport> entity = new HttpEntity<>(dataDictionaryReport, httpHeaders);

		try {
			restTemplate.exchange(lintingUrl + lintDD, HttpMethod.POST, entity, String.class).getBody();
		} catch (Exception e) {
			logger.error("Error while calling linting API {} ", e.getMessage());
		}
	}

	private void callLintingAPIForPortfolio(PortfolioReport portfolio, String jsessionid) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("jsessionid", jsessionid);
		HttpEntity<PortfolioReport> entity = new HttpEntity<>(portfolio, httpHeaders);

		try {
			restTemplate.exchange(lintingUrl + lintPortfolio, HttpMethod.POST, entity, String.class).getBody();
		} catch (Exception e) {
			logger.error("Error while calling linting API {} ", e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> getDataModelMap(String interactionid, String jsessionid, String portfolioId)
			throws Exception {
		return new ResponseEntity<>(dictionaryBusiness.getDataModelMap(portfolioId),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllDatabaseConnections(String interactionid, String jsessionid, String databaseName) throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getAllDatabaseConnections(databaseName),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getMongoDatabases(String interactionid, String jsessionid, String connectionId)throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getDatabases(connectionId),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPostgresSchemaNames(String interactionid, String jsessionid, String connectionId)throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getPostgresSchemaNames(connectionId),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCollectionNames(String interactionid, String jsessionid,String connectionId,String databaseName, String databaseType)throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getCollectionNames(connectionId, databaseType, databaseName),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getTableNames(String interactionid, String jsessionid,String connectionId,String databaseType, String schemaName)throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getTableNames(connectionId,databaseType, schemaName),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSchemas(String interactionid, String jsessionid, String connectionId, String databaseType, String databaseName, String schemaName, Set<String> collections, Set<String> tables, boolean deepSearch)throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.getSchemas(databaseType, connectionId, databaseName,schemaName, collections, tables, deepSearch),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> searchKeyFromDB(String interactionid, String jsessionid, String connectionId, String databaseType, String databaseName, String schemaName, String searchKey) throws ItorixException {
		return new ResponseEntity<>(dictionaryBusiness.searchForKey(databaseType, connectionId, databaseName,schemaName, searchKey),HttpStatus.OK);
	}

}