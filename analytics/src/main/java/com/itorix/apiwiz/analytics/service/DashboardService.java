package com.itorix.apiwiz.analytics.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface DashboardService {

    @RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/landingpage", produces = {"application/json"})
    ResponseEntity<?> getWorkspaceDashboard(
            @RequestHeader(value = "interactionid", required = false) String interactionId,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestHeader(value = "userId") String userId)
            throws ItorixException;
}
