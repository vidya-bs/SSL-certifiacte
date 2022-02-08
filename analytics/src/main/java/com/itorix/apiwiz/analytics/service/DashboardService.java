package com.itorix.apiwiz.analytics.service;

import com.itorix.apiwiz.analytics.model.WorkspaceDashboard;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface DashboardService {

    @ApiOperation(value = "Get Workspace Dashboard")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = WorkspaceDashboard.class),
            @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
    @RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/landingpage", produces = {"application/json"})
    ResponseEntity<?> getWorkspaceDashboard(
            @RequestHeader(value = "interactionid", required = false) String interactionId,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestHeader(value = "userId", required = false) String userId)
            throws ItorixException;
}
