package com.itorix.apiwiz.datamanagement.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.data.management.model.ScheduleModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public interface OrganizationSchedulerService {

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organization/schedules</h1>
	 *
	 * <p>
	 * createOrganizationSchedule.
	 *
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Create Organization Schedule", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> createOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organization/schedules</h1>
	 *
	 * <p>
	 * updateOrganizationSchedule.
	 *
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Update Organization Schedule", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> updateOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organization/schedules</h1>
	 *
	 * <p>
	 * deleteOrganizationSchedule.
	 *
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Delete Organization Schedule", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Success", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> deleteOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organization/schedules</h1>
	 *
	 * <p>
	 * getOrganizationSchedule.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Organization Schedule", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ScheduleModel.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<List<ScheduleModel>> getOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;
}
