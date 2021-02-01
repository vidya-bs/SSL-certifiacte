package com.itorix.apiwiz.datadictionary.serviceimpl;

import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.business.DictionaryBusiness;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;
import com.itorix.apiwiz.datadictionary.service.DictionaryService;

@CrossOrigin
@RestController
/**
 * PortfolioController .
 *
 * @author itorix.inc
 *
 */
public class DictionaryServiceImpl implements DictionaryService{

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
					throws  Exception {
		HttpHeaders headers = new HttpHeaders();
		if (portfolioVO == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1000"), "Portfolio-1000");
		} else {
			portfolioVO.setInteractionid(interactionid);
			PortfolioVO vo = dictionaryBusiness.findPortfolio(portfolioVO);
			if (vo != null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Portfolio-1003"), portfolioVO.getName()),
						"Portfolio-1003");
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
						String.format(ErrorCodes.errorMessage.get("Portfolio-1002"), portfolioVO.getName()),
						"Portfolio-1002");
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
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<PortfolioHistoryResponse> getPortfolioOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws  Exception {
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
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws  Exception {
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
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable("id") String id) throws  Exception {
		PortfolioVO vo=new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		vo = dictionaryBusiness.getPortfolioById(vo);
		if(vo==null){
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), id),
					"Portfolio-1004");	
		}
		return new ResponseEntity<PortfolioVO>(vo, HttpStatus.OK);
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
	 * @param interactionid
	 * @param jsessionid
	 * @param portfolioVO
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	public ResponseEntity<Void> updatePortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable("id") String id, @RequestBody String body)
					throws  Exception {
		PortfolioVO vo=new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		PortfolioVO portfolioVO=dictionaryBusiness.findPortfolioById(vo);
		if (portfolioVO == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), id),
					"Portfolio-1004");	
		} else {
			PortfolioModel model=new PortfolioModel();
			model.setInteractionid(interactionid);
			model.setPortfolioID(id);
			ObjectMapper mapper=new ObjectMapper();
			JsonNode jsonNode=	mapper.readTree(body);
		    String name=(String) jsonNode.get("name").asText();
		    model.setModelName(name);
			model.setModel(body);
			dictionaryBusiness.createPortfolioModel(model);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
	}

	public ResponseEntity<Void> getPortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable("id") String id)
					throws  Exception {
		return null;
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
	public ResponseEntity<Object> getPortfolioModelNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable("id") String id, @RequestParam(name="filterby",required =false) String filterby)
					throws  Exception {
		PortfolioVO vo=new PortfolioVO();
		vo.setInteractionid(interactionid);
		vo.setId(id);
		vo = dictionaryBusiness.getPortfolioById(vo);
		if(vo!= null){
			ObjectMapper mapper = new ObjectMapper();
			List<Object> strModels = new ArrayList<Object>();
			List<Object> models = vo.getModels();
			if(models != null)
				for(Object model: models ){
					try {
						String json = mapper.writeValueAsString(model);
						JsonNode jsonNode = mapper.readTree(json);
						String name = jsonNode.get("name").textValue();
						strModels.add(name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			vo.setModels(strModels);
		}
		return new ResponseEntity<Object>(vo,HttpStatus.OK);
	}

	
	public ResponseEntity<Object> getPortfolioModel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id, 
			@PathVariable("model_name") String model_name,
			@RequestParam(name="filterby",required =false) String filterby)
			throws  Exception{
		PortfolioModel model = new PortfolioModel();
		model.setPortfolioID(id);
		model.setModelName(model_name);
		model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelName(model);
		if (model == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), id),
					"Portfolio-1004");	
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(model.getModel());
		return new ResponseEntity<Object>(jsonNode,HttpStatus.OK);
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
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable("id") String id, @PathVariable("model_name") String model_name)
					throws  Exception {
		PortfolioModel model=new PortfolioModel();
		model.setInteractionid(interactionid);
		model.setPortfolioID(id);
	    model.setModelName(model_name);
	    model = dictionaryBusiness.findPortfolioModelsByportfolioIDAndModelName(model);
	    if (model == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Portfolio-1004"), id),
					"Portfolio-1004");	
		}
		dictionaryBusiness.deletePortfolioModelByportfolioIDAndModelName(model);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

	}
	
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException {
		return new ResponseEntity<Object>(dictionaryBusiness.portfolioSearch(interactionid, name, limit),HttpStatus.OK);
	}

}
