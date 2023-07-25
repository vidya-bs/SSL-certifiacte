package com.itorix.apiwiz.datadictionary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Create Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Portfolio Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
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
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
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
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Portfolio Overview", notes = "", code = 200, response = PortfolioVO.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PortfolioVO.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
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
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v2/data-dictionary/overview")
	public ResponseEntity<PortfolioHistoryResponse> getPortfolioOverviewV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;


	@ApiOperation(value = "Get Portfolio Overview", notes = "", code = 200, response = PortfolioVO.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PortfolioVO.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary")
	public ResponseEntity<List<PortfolioVO>> getPortfolios(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * Using this method we can get the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Portfolio ", notes = "", code = 200, response = PortfolioVO.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}")
	public ResponseEntity<PortfolioVO> getPortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}/revisions")
	public ResponseEntity<?> getPortfolioRevisions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

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
	@ApiOperation(value = "Delete Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio Deleted sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}")
	public ResponseEntity<Void> deletePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id) throws Exception;

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
	@ApiOperation(value = "Update Portfolio Models", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = {"/v1/data-dictionary/{id}/schemas",
			"/v1/data-dictionary/{id}/schemas/{modelId}/revision"})
	public ResponseEntity<Void> updatePortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable(value = "modelId", required = false) String modelId, @RequestBody String body)
			throws Exception;

	@ApiOperation(value = "Update Portfolio Models", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary/{id}/schemas/{modelId}/revision/{revision}")
	public ResponseEntity<Void> updatePortfolioModelsWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable(value = "modelId", required = false) String modelId,
			@PathVariable(value = "revision", required = false) Integer revision, @RequestBody String body)
			throws Exception;
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
	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
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
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Portfolio Model", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Portfolio retrieved sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Not able to retrive the model", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}/schemas/{model_name}")
	public ResponseEntity<Object> getPortfolioModel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name,
			@RequestParam(name = "filterby", required = false) String filterby) throws Exception;

	@ApiOperation(value = "Get Portfolio Model with revision", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Model is retrieved successfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Not able to retrieve the model", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = {
			"/v1/data-dictionary/{id}/schemas/{modelId}/revision/{revision}",
			"/v1/data-dictionary/{id}/schemas/{modelId}/revision"})
	public ResponseEntity<Object> getPortfolioModelWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId,
			@PathVariable(name = "revision", required = false) Integer revision) throws Exception;

	@ApiOperation(value = "Update Portfolio", notes = "", code = 201, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Portfolio updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Portfolio - %s already exists.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/data-dictionary/{id}/schemas/{modelId}/revisions"})
	public ResponseEntity<Object> getAllPortfolioModels(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId,
			@PathVariable(name = "revision", required = false) Integer revision) throws Exception;

	/**
	 * Using this method we can create the Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Delete Portfolio model", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Portfolio deleted sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Not able to find the portfolio model.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}/schemas/{model_name}")
	public ResponseEntity<Void> deletePortfolioModelByName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("model_name") String model_name) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/search", produces = {"application/json"})
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException;
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary/{id}/schemas/{modelId}/{modelStatus}")
	public ResponseEntity<?> updatePortfolioModelStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("modelStatus") ModelStatus modelStatus)
			throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/data-dictionary/{id}/revision/{revision}")
	public ResponseEntity<?> createPortfolioRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("revision") Integer revision) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{id}/revision/{revision}")
	public ResponseEntity<?> getPortfolioRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("revision") Integer revision) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}/revision/{revision}")
	public ResponseEntity<?> deletePortfolioRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("revision") Integer revision) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/data-dictionary/{id}/revision/{revision}/status")
	public ResponseEntity<?> updatePortfolioStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("revision") Integer revision, @RequestBody Revision status) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/data-dictionary/{id}/schemas/{modelId}/revision/{revision}/{modelStatus}")
	public ResponseEntity<?> updatePortfolioModelStatusWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("modelStatus") ModelStatus modelStatus,
			@PathVariable("revision") Integer revision) throws Exception;

	@ApiOperation(value = "Delete Portfolio Model with revision", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Portfolio  Model deleted sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! Not able to find the model", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{id}/schemas/{modelId}/revision/{revision}")
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	ResponseEntity<Void> deletePortfolioModelByIdWithRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			@PathVariable("modelId") String modelId, @PathVariable("revision") Integer revision)
			throws ItorixException;


	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/data-dictionary/{portfolioId}/sync2Repo")
	public ResponseEntity<?> sync2Repo(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("portfolioId") String portfolioId,
			@RequestBody DictionaryScmUpload dictionaryScmUpload) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{portfolioId}/git-integrations")
	public ResponseEntity<?> getGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("portfolioId") String portfolioId) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/data-dictionary/{portfolioId}/git-integrations")
	public ResponseEntity<?> deSyncFromRepo(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("portfolioId") String portfolioId) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/{portfolioId}/modelmap")
	public ResponseEntity<?> getDataModelMap(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("portfolioId") String portfolioId) throws Exception;


	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/connections/{databaseType}")
	public ResponseEntity<?> getAllDatabaseConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "databaseType", required = false) String databaseType
	) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/database-names/{connectionId}")
	public ResponseEntity<?> getDatabases(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/schemas/{connectionId}")
	public ResponseEntity<?> getPostgresSchemas(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/collections/{connectionId}")
	public ResponseEntity<?> getCollectionNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId,
			@RequestParam(value = "databaseType")String databaseType,
			@RequestParam(value = "databaseName", required = false) String databaseName) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/tables/{connectionId}")
	public ResponseEntity<?> getTableNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId,
			@RequestParam(value = "databaseType")String databaseType,
			@RequestParam(value = "schemaName", required = false) String schemaName) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/schema/{connectionId}")
	public ResponseEntity<?> getSchemas(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId,
			@RequestParam(value = "databaseType",required = false)String databaseType,
			@RequestParam(value = "databaseName",required = false)String databaseName,
			@RequestParam(value = "schemaName",required = false) String schemaName,
			@RequestParam(value = "collections",required = false)List<String> collections,
			@RequestParam(value = "tables",required = false)List<String> tables,
			@RequestParam(value = "deepSearch",defaultValue = "false")boolean deepSearch
	) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/data-dictionary/database/search-key/{connectionId}")
	public ResponseEntity<?> searchKeyFromDB(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "connectionId") String connectionId,
			@RequestParam(value = "databaseType")String databaseType,
			@RequestParam(value = "databaseName",required = false)String databaseName,
			@RequestParam(value = "schemaName", required = false) String schemaName,
			@RequestParam(value = "searchKey") String searchKey
	) throws ItorixException;

}