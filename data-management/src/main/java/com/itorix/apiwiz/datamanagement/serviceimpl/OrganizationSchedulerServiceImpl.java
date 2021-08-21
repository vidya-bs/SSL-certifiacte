package com.itorix.apiwiz.datamanagement.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.business.OrganizationSchedulerBusiness;
import com.itorix.apiwiz.data.management.model.ScheduleModel;
import com.itorix.apiwiz.datamanagement.service.OrganizationSchedulerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public class OrganizationSchedulerServiceImpl implements OrganizationSchedulerService {
	private static final Logger logger = LoggerFactory.getLogger(OrganizationSchedulerServiceImpl.class);

	@Autowired
	OrganizationSchedulerBusiness OrganizationSchedulerBusiness;

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
	@ApiOperation(value = "Create Organization Schedule", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> createOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception {
		String userId = OrganizationSchedulerBusiness.getUserId(jsessionid);
		if (userId == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1007"), "Apigee-1007");
		}
		scheduleModel.setUserId(userId);
		scheduleModel.setInteractionid(interactionid);
		OrganizationSchedulerBusiness.createOrganizationSchedule(scheduleModel);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

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
	@ApiOperation(value = "Update Organization Schedule", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> updateOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception {
		scheduleModel.setInteractionid(interactionid);
		OrganizationSchedulerBusiness.updateOrganizationSchedule(scheduleModel);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

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
	@ApiOperation(value = "Delete Organization Schedule", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Success", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<Void> deleteOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception {
		scheduleModel.setInteractionid(interactionid);
		OrganizationSchedulerBusiness.deleteOrganizationSchedule(scheduleModel);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

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
	@ApiOperation(value = "Get Organization Schedule", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ScheduleModel.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "v1/api/apigee/organization/schedules")
	public ResponseEntity<List<ScheduleModel>> getOrganizationSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception {
		List<ScheduleModel> list = new ArrayList<>();
		list = OrganizationSchedulerBusiness.getOrganizationSchedule(interactionid);
		return new ResponseEntity<List<ScheduleModel>>(list, HttpStatus.OK);
	}
}
