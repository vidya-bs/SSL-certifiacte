package com.itorix.apiwiz.datadictionary.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
/**
 * PortfolioController .
 *
 * @author itorix.inc
 */
public interface DictionaryService {

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
	@ApiOperation(value = "Create Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Portfolio Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','ANALYST') and hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/data-dictionary")
	public ResponseEntity<Void> createPortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody PortfolioVO portfolioVO)
			throws Exception;

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
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','ANALYST') and hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary")
	public ResponseEntity<Void> updatePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody PortfolioVO portfolioVO)
			throws Exception;

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
	@ApiOperation(value = "Get Portfolio Overview", notes = "", code = 200, response = PortfolioVO.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PortfolioVO.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/overview")
	public ResponseEntity<PortfolioHistoryResponse> getPortfolioOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;

	@ApiOperation(value = "Get Portfolio Overview", notes = "", code = 200, response = PortfolioVO.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PortfolioVO.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary")
	public ResponseEntity<List<PortfolioVO>> getPortfolios(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

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
	@ApiOperation(value = "Get Portfolio ", notes = "", code = 200, response = PortfolioVO.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}")
	public ResponseEntity<PortfolioVO> getPortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

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
	@ApiOperation(value = "Delete Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio Deleted sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','ANALYST') and hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}")
	public ResponseEntity<Void> deletePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

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
	@ApiOperation(value = "Update Portfolio Models", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','ANALYST') and hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary/{id}/schemas")
	public ResponseEntity<Void> updatePortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@RequestBody String body) throws Exception;

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
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}/schemas")
	public ResponseEntity<Object> getPortfolioModelNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@RequestParam(name = "filterby", required = false) String filterby) throws Exception;

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
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}/schemas/{model_name}")
	public ResponseEntity<Object> getPortfolioModel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name,
			@RequestParam(name = "filterby", required = false) String filterby) throws Exception;

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
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','ANALYST') and hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}/schemas/{model_name}")
	public ResponseEntity<Void> deletePortfolioModelByName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name) throws Exception;

	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/search", produces = {"application/json"})
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException;
}
