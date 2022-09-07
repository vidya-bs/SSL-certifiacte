package com.itorix.apiwiz.datadictionary.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.ModelStatus;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;
import com.itorix.apiwiz.datadictionary.model.Revision;
import com.itorix.apiwiz.datadictionary.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@CrossOrigin
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
	 * 
	 * @return
	 * 
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
	 * 
	 * @return
	 * 
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
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this method we can get the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @return
	 * 
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
	 * 
	 * @return
	 * 
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
	 * 
	 * @return
	 * 
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
	 * 
	 * @return
	 * 
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
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> updatePortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@RequestBody String body) throws Exception {
		PortfolioVO vo = new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		PortfolioVO portfolioVO = dictionaryBusiness.findPortfolioById(vo);
		if (portfolioVO == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		} else {
			PortfolioModel model = new PortfolioModel();
			model.setInteractionid(interactionid);
			model.setPortfolioID(id);
			model.setMts(System.currentTimeMillis());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(body);
			String name = (String) jsonNode.get("name").asText();
			model.setModelName(name);
			model.setModel(body);
			dictionaryBusiness.createPortfolioModel(model);
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
		model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelName(model);
		if (model == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(model.getModel());
		return new ResponseEntity<Object>(jsonNode, HttpStatus.OK);
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
	public ResponseEntity<Void> deletePortfolioModelByName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name) throws Exception {
		PortfolioModel model = new PortfolioModel();
		model.setInteractionid(interactionid);
		model.setPortfolioID(id);
		model.setModelName(model_name);
		model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelName(model);
		if (model == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), id),
					"Portfolio-1002");
		}
		dictionaryBusiness.deletePortfolioModelByportfolioIDAndModelName(model);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
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
			@PathVariable("model_name") String model_name, @PathVariable("modelStatus") ModelStatus modelStatus) {
		dictionaryBusiness.updatePortfolioModelStatus(id, model_name, modelStatus);
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
		vo.setStatus(status.getStatus());
		dictionaryBusiness.createPortfolio(vo);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

	}

}
