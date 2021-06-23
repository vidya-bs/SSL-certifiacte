package com.itorix.apiwiz.cicd.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.cicd.beans.Package;
import com.itorix.apiwiz.cicd.beans.PackageReviewComents;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.design.studio.model.SwaggerReviewComents;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "CI-CD", tags = "CI-CD")
public interface ManagePackageService {

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/packages", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createPackage(
			@RequestParam(value = "action", required = false) String action,
			@RequestBody Package packageRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,HttpServletRequest request);

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/packages", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> updatePackage(@RequestBody Package packageRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,HttpServletRequest request);

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/packages/{reqestId}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> deletePackage(
			@PathVariable ("reqestId") String reqestId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,HttpServletRequest request);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages")
	public ResponseEntity<?> getPackage(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			HttpServletRequest request);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/{reqestId}")
	public ResponseEntity<?> getPackage(
			@PathVariable ("reqestId") String reqestId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,HttpServletRequest request);

//	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/info",
//			 produces = { "application/json" })
//	public ResponseEntity<?> getPackageProjectData(
//			@RequestParam("projectname") String projectname,
//			@RequestHeader(value = "JSESSIONID") String jsessionId,
//			@RequestHeader(value = "interactionid", required = false) String interactionid,
//			HttpServletRequest request);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/info",
			 produces = { "application/json" })
	public ResponseEntity<?> getPackageProjectData(
			@RequestParam("proxyname") String proxyname,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request);

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/packages/{packageId}/{approveAction}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> approvePackage(
			@PathVariable ("packageId") String packageId,
			@PathVariable ("approveAction") String approveAction,
			@RequestBody Package packageRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,HttpServletRequest request);

	@ApiOperation(value = "Create Review Comment", notes = "", code=201)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = Void.class),
        @ApiResponse(code = 404, message = "No records found for selected swagger name - %s.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/packages/{packageId}/reviews")
	public ResponseEntity<Void> createReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "packageId") String packageId,
			@RequestBody PackageReviewComents packageReviewComments
			)throws Exception;

	@ApiOperation(value = "Update Review Comment", notes = "", code=204)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/packages/{packageId}/reviews/{commentId}")
	public ResponseEntity<Void> updateReviewComment(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "packageId") String packageId,
			@PathVariable(value = "commentId") String commentId,
			@RequestBody PackageReviewComents packageReviewComents)throws Exception;

	@ApiOperation(value = "Review Comment Replay", notes = "", code=201)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "No records found for selected review id - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/packages/{packageId}/reviews/{commentId}")
	public ResponseEntity<Void> reviewCommentReplay(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "packageId") String packageId,
			@PathVariable(value = "commentId") String commentId,
			@RequestBody PackageReviewComents packageReviewComents)throws Exception;

	@ApiOperation(value = "Update Swagger With new Revison", notes = "", code=200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerReviewComents.class,responseContainer="List"),
			@ApiResponse(code = 404, message = "No records found for selected swagger name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/{packageId}/reviews")
	public ResponseEntity<Object> getReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("packageId") String packageId)throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/{packageName}/validate")
	public ResponseEntity<Object> validateName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("packageName") String packageName)throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/names")
	public ResponseEntity<Object> getPackageNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid)throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/packages/search")
	public ResponseEntity<Object> searchPackage(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;


}
