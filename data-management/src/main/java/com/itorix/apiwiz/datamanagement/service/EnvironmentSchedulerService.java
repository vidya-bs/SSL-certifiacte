package com.itorix.apiwiz.datamanagement.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
public interface EnvironmentSchedulerService {

	/**
	 * <h1>http://hostname:port/v1/api/apigee/environment/schedules</h1>
	 * <p>
	 * createEnvironmentSchedule.
	 * </p>
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Create Environment Schedule", notes = "", code=201)
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = Void.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/environment/schedules")
	public ResponseEntity<Void>  createEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/environment/schedules</h1>
	 * <p>
	 * updateEnvironmentSchedule.
	 * </p>
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Update Environment Schedule", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/api/apigee/environment/schedules")
	public ResponseEntity<Void>  updateEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/environment/schedules</h1>
	 * <p>
	 * deleteEnvironmentSchedule.
	 * </p>
	 * @param interactionid
	 * @param scheduleModel
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Delete Environment Schedule", notes = "", code=201)
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Deletion Success", response = Void.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/environment/schedules")
	public ResponseEntity<Void>  deleteEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ScheduleModel scheduleModel, @RequestHeader(value = "jsessionid") String jsessionid)throws Exception ;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/environment/schedules</h1>
	 * <p>
	 * getEnvironmentSchedule.
	 * </p>
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Environment Schedule", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ScheduleModel.class,responseContainer = "List"),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/environment/schedules")
	public ResponseEntity<List<ScheduleModel>>  getEnvironmentSchedule(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;
	
}
