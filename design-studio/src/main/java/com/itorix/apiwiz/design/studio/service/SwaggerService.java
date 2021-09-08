package com.itorix.apiwiz.design.studio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.generator.model.ResponseCode;
import io.swagger.models.Swagger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
/**
 * To use to generate the new swagger and update the existing swagger.
 *
 * @author itorix.inc associated-products
 */
public interface SwaggerService {

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER') and hasAnyAuthority('BASIC','PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/puls")
	public String checkPuls(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response);

	@PreAuthorize("hasAnyAuthority('BASIC','PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/import")
	public ResponseEntity<Object> importSwaggers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "gitURI", required = false) String gitURI,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "personalToken", required = false) String personalToken) throws Exception;

	/**
	 * This method is used to create the swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Create Swagger", notes = "", response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('BASIC','PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}")
	public ResponseEntity<Void> createSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername,
			@RequestBody String json) throws Exception;

	/**
	 * Using this we can update are change the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param create
	 * @param swaggername
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Create Swagger With new Revison", notes = "")
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize(" hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/revisions")
	public ResponseEntity<Void> createSwaggerWithNewRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "create", required = false, defaultValue = "new") String create,
			@PathVariable("swaggername") String swaggername, @RequestBody String json) throws Exception;

	/**
	 * Using this we can update the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Update Swagger With new Revison", notes = "")
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}")
	public ResponseEntity<Void> updateSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody String json) throws Exception;

	/**
	 * Using this we will get all the list of version's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Swagger Revison's", notes = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = Revision.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<Revision>> getListOfRevisions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * Using this we will get all the Swagger's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@ApiOperation(value = "Get List Of Swagger Names", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfSwaggerNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return @throws IOException @throws ItorixException @throws
	 */
	@ApiOperation(value = "Get List Of Swagger Details", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('BASIC','PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/history", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfSwaggerDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "swagger", required = false) String swagger,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "modifieddate", required = false) String modifiedDate,
			@RequestParam(value = "sortbymodifieddate", required = false) String sortByModifiedDate,
			@RequestParam(value = "product", required = false) String product) throws Exception;

	/**
	 * We will get when the swagger state is published.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param status
	 * @param request
	 * @param response
	 * @return @throws IOException @throws ItorixException @throws
	 */
	@ApiOperation(value = "Get List Of Published Swagger Details", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerDocumentationVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/documentation", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfPublishedSwaggerDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "partnerID", required = false) String partnerID) throws Exception;

	/**
	 * We need to pass the particular swagger name and we will get the
	 * respective.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Swagger", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Swagger.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * If we pass particular swagger name if it exist in the system it will
	 * delete.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Delete Swagger", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Ok", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swaggername}")
	public ResponseEntity<Void> deleteSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * Using this method we will delete the respective revision.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@ApiOperation(value = "delete Swagger based on Revison", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger deleted sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swaggername}/revisions/{revision}")
	public ResponseEntity<Void> deleteSwaggerVersion(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, HttpServletRequest request, HttpServletResponse response)
			throws ItorixException;

	/**
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Swagger With revision", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwaggerWithrevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception;

	/**
	 * Using this we can update the status of swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param json
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Status", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/status")
	public ResponseEntity<Void> updateStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody String json) throws Exception;

	/**
	 * get roles.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "get roles", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Roles associate with user", response = List.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/roles")
	public ResponseEntity<Object> getRoles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * Update the swagger comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param comment
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "Update Comment", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/comment")
	public ResponseEntity<Void> updateComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerComment comment) throws Exception;

	/**
	 * To get the particular swagger comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Swagger Comments", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger Updated sucessfully", response = SwaggerComment.class, responseContainer = "list"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/comments/history", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwaggerComments(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception;

	/**
	 * Using this we can update the lock status.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Lock Status", notes = "", code = 204)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Swagger Lock Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/lockstatus")
	public ResponseEntity<Void> updateLockStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerVO swaggerVO) throws Exception;

	/**
	 * Using this we can deprecate the particular swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Deprecate Swagger", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger Updated sucessfully", response = SwaggerVO.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/deprecate")
	public ResponseEntity<Object> deprecate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerVO swaggerVO) throws Exception;

	/**
	 * Using this we can update the proxies for that swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Proxies", notes = "", code = 204)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Swagger Proxies Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/proxies")
	public ResponseEntity<Void> updateProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception;

	/**
	 * Using this we will get the lock status of that swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get LockStatus", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Boolean.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/lockstatus", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getLockStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception;

	/**
	 * Using this we will generate the xpath.
	 *
	 * @param interactionid
	 * @param xsdFile
	 * @param elementName
	 * @param type
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "Genarate Xpath", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = XmlSchemaVo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/genaratexpath", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> genarateXpath(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("xsdfile") MultipartFile xsdFile, @RequestParam("elementname") String elementName,
			@RequestParam("type") String type, @RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * Using this we will get the swagger definitions.
	 *
	 * @param interactionid
	 * @param xpathFile
	 * @param sheetName
	 * @param swaggername
	 * @param revision
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "GenarateSwaggerDefinations", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/definitions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Void> genarateSwaggerDefinations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("xpathfile") MultipartFile xpathFile, @RequestParam("sheetname") String sheetName,
			@PathVariable("swaggername") String swaggername, @RequestParam("revision") Integer revision,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * To get the swagger Json definitions.
	 *
	 * @param interactionid
	 * @param json
	 * @param swaggername
	 * @param revision
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "Genarate Swagger Json Definations", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/jsondefinitions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Void> genarateSwaggerJsonDefinations(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody String json,
			@PathVariable("swaggername") String swaggername, @RequestParam("revision") Integer revision,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception;

	/**
	 * Using this we can create the review.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerReview
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Create Review", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/revisions/{revision}/review")
	public ResponseEntity<Void> createReview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerReview swaggerReview) throws Exception;

	/**
	 * Using this we can write the review comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Create Review Comment", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/reviews")
	public ResponseEntity<Void> createReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception;

	/**
	 * We can update the review comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Review Comment", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/reviews")
	public ResponseEntity<Void> updateReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception;

	/**
	 * Using this we can replay the review comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param reviewid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Review Comment Replay", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "No records found for selected review id - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/reviews/{reviewid}/comment")
	public ResponseEntity<Void> reviewCommentReplay(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("reviewid") String reviewid,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception;

	/**
	 * To get the review comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param versionnumber
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Swagger With new Revison", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerReviewComents.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/reviews")
	public ResponseEntity<Object> getReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception;

	@ApiOperation(value = "Get Swagger Revison's", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/stats")
	public ResponseEntity<Object> getSwaggerStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;

	@ApiOperation(value = "Get Team Stats", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/stats")
	public ResponseEntity<Object> getTeamStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swagger-gen/clients/servers")
	public @ResponseBody ResponseEntity<Object> getClientsServers(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Genrate client", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swagger-gen/clients/{framework}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> genrateClient(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("framework") String framework,
			@RequestBody GenrateClientRequest genrateClientRequest) throws Exception;

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Genrate client", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swagger-gen/servers/{framework}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ResponseCode> genrateServer(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("framework") String framework,
			@RequestBody GenrateClientRequest genrateClientRequest) throws Exception;

	/**
	 * Using this we can associate the swagger with team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Assoiate Product", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associate-products")
	public ResponseEntity<Void> assoiateProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception;

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Products", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-products")
	public ResponseEntity<Set<String>> getAssoiatedProducts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * Using this we can associate the swagger with team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "Assoiate Projects", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/teams/{teamname}/associate-projects")
	public ResponseEntity<Void> assoiateTeamsToProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname,
			@RequestBody SwaggerTeam swaggerTeam) throws Exception;

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Projects", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/{teamname}/associated-projects")
	public ResponseEntity<Set<String>> getassoiateTeamsToProjects(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
			throws Exception;

	/**
	 * Using this we can associate the swagger with Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Assoiate Product", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associated-portfolio")
	public ResponseEntity<Void> assoiatePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception;

	/**
	 * To get the assoiated Portfolios.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Portfolios", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-portfolio")
	public ResponseEntity<Set<String>> getAssoiatedPortfolios(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername)
			throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/validate")
	public ResponseEntity<?> validateSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @RequestBody String swaggerStr)
			throws Exception;

	/**
	 * @param interactionid
	 * @param jsessionid
	 * @param name
	 * @param limit
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "v1/swaggers/search", produces = {"application/json"})
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestHeader(value = "oas", required = false) String oas, @RequestParam("limit") int limit)
			throws ItorixException, JsonProcessingException;

	/**
	 * To get the assoiated BasePaths.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Portfolios", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/basepaths")
	public ResponseEntity<Object> getAssoiatedBasePaths(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception;

	/**
	 * Create or update git Integration.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Create or update git Integration", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Create or update git Integration", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<Void> createOrUpdateGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable("swagger-id") String swaggerid, @RequestBody SwaggerIntegrations swaggerIntegrations)
			throws Exception;

	@ApiOperation(value = "Get git Integrations", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<SwaggerIntegrations> getGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable("swagger-id") String swaggerid) throws Exception;

	/**
	 * If we pass particular swagger name if it exist in the system it will
	 * delete.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Delete Swagger", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Ok", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<Void> deleteGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable("swagger-id") String swaggerid) throws Exception;

	@ApiOperation(value = "Get Info of Swagger", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Object.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/info", produces = {"application/json"})
	public ResponseEntity<Object> getSwaggerInfo(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestParam("id") String swaggerid) throws Exception;

	@ApiOperation(value = "Clone existing Swagger. Creates a new clone based on the request details ", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Cloned successfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/clone")
	public ResponseEntity<?> cloneSwagger(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestBody SwaggerCloneDetails swaggerCloneDetails) throws Exception;

	@ApiOperation(value = "Get proxies associated to swagger", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swagger}/revisions/{revision}/getassociatedproxy", produces = {
			"application/json"})
	public ResponseEntity<?> getProxies(@PathVariable("swagger") String swagger,
			@PathVariable("revision") String revision,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/partner-groups", produces = {"application/json"})
	public ResponseEntity<?> createPartnerGroup(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerPartner swaggerPartner)
			throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/partner-groups/{partnerId}", produces = {
			"application/json"})
	public ResponseEntity<?> updatePartnerGroup(@PathVariable("partnerId") String partnerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerPartner swaggerPartner)
			throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/partner-groups/{partnerId}", produces = {
			"application/json"})
	public ResponseEntity<?> deletePartnerGroup(@PathVariable("partnerId") String partnerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/partner-groups", produces = {"application/json"})
	public ResponseEntity<?> getPartnerGroups(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggerId}/associate-partner", produces = {
			"application/json"})
	public ResponseEntity<?> manageSwaggerPartners(@PathVariable("swaggerId") String swaggerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestBody AsociateSwaggerPartnerRequest swaggerPartnerRequest) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggerId}/associate-partner", produces = {
			"application/json"})
	public ResponseEntity<?> getSwaggerPartners(@PathVariable("swaggerId") String swaggerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas) throws Exception;

	// EN-356
	@ApiOperation(value = "Create or update swagger data dictionary", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Create or update swagger data dictionary", response = Void.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/associate-data-dictionary")
	public ResponseEntity<?> updateSwaggerDictionary(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody SwaggerDictionary swaggerDictionary);

	@ApiOperation(value = "Get swagger data dictionary", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get swagger data dictionary", response = SwaggerDictionary.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggerId}/{revision}/associate-data-dictionary")
	public ResponseEntity<?> getSwaggerDictionary(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("swaggerId") String swaggerId, @PathVariable("revision") Integer revision);

	@ApiOperation(value = "Get swaggers associated with a data dictionary", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get swaggers associated with a data dictionary", response = DictionarySwagger.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/associate-data-dictionary/{dictionaryId}")
	public ResponseEntity<?> getSwaggerAssociatedWithDataDictionary(
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("dictionaryId") String dictionaryId);

	@ApiOperation(value = "Get swaggers associated with a schema name", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get swaggers associated with a schema name", response = DictionarySwagger.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('PRO','TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/associate-data-dictionary/{dictionaryId}/schemas/{schemaName}")
	public ResponseEntity<?> getSwaggerAssociatedWithSchemaName(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("dictionaryId") String dictionaryId, @PathVariable("schemaName") String schemaName);

}
