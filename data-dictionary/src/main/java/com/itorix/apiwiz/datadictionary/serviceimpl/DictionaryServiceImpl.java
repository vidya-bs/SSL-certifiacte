package com.itorix.apiwiz.datadictionary.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.*;
import com.itorix.apiwiz.datadictionary.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
			// dictionaryBusiness.sendNotificationForDD(portfolioVO);
		}
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
		dictionaryBusiness.deletePortfolioById(vo);
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
			PortfolioModel modelWithRevision = dictionaryBusiness
					.findPortfolioModelByportfolioIDAndModelIdAndRevison(id, modelId, revision);
			// dictionaryBusiness.sendNotificationForDDModel(modelWithRevision);
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
			@PathVariable("modelId") String modelId, @PathVariable("modelStatus") ModelStatus modelStatus) {
		dictionaryBusiness.updatePortfolioModelStatus(id, modelId, modelStatus);
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
		vo.setStatus(portfolioVO.getStatus());
		portfolioVO = dictionaryBusiness.createPortfolioRevision(vo, portfolioVO.getId());
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
		dictionaryBusiness.deletePortfolioById(vo);
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
			@PathVariable("revision") Integer revision) {
		log.info("Change Status of a particular model revision");
		dictionaryBusiness.updatePortfolioModelStatusWithRevision(id, modelId, modelStatus, revision);
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
		dictionaryBusiness.deletePortfolioModelByportfolioIDAndModelIdAndRevision(model1);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/model/{modelId1}/diff/{modelId2}")
	public ResponseEntity<Object> findDiffBetweenModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "portfolioId1", required = true) String portfolioId1,
			@RequestParam(value = "portfolioId2", required = true) String portfolioId2,
			@RequestParam(value = "Revision1", required = true) Integer revisionid1,
			@RequestParam(value = "Revision2", required = true) Integer revisionid2,
			@PathVariable("modelId1") String modelId1, @PathVariable("modelId2") String modelId2) throws Exception {
		PortfolioModel portfolioModel = new PortfolioModel();
		PortfolioModel portfolioModel1 = new PortfolioModel();
		if (revisionid1 != null) {
			portfolioModel = dictionaryBusiness.findPortfolioModelByportfolioIDAndModelIdAndRevison(portfolioId1, modelId1, revisionid1);
			if (portfolioModel == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), portfolioId1),
						"Portfolio-1004");
			}
		}
		if (revisionid2 != null) {
			portfolioModel1 = dictionaryBusiness.findPortfolioModelsWithRevisions(portfolioId2, modelId2, revisionid2);
			if (portfolioModel1 == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), portfolioId2),
						"Portfolio-1004");
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		DiffResponse diff = new DiffResponse();
		JsonNode beforeNode = mapper.readTree(portfolioModel.getModel());
		JsonNode afterNode = mapper.readTree(portfolioModel1.getModel());
		//JsonNode patch = JsonDiff.asJson(beforeNode, afterNode);
		diff.setModel1(portfolioModel);
		diff.setModel2(portfolioModel1);
		diff.setDiff(JsonDiff.asJson(beforeNode, afterNode));
		return new ResponseEntity<>(diff, HttpStatus.OK);

	}
}