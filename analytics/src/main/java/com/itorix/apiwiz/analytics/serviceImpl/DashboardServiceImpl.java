package com.itorix.apiwiz.analytics.serviceImpl;

import com.itorix.apiwiz.analytics.businessImpl.LandingPageStatsImpl;
import com.itorix.apiwiz.analytics.service.DashboardService;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private LandingPageStatsImpl landingPageStatsImpl;

    @Override
    public ResponseEntity<?> getWorkspaceDashboard(String interactionId, String jsessionid, String userId) throws ItorixException {
       return new ResponseEntity<>(landingPageStatsImpl.getWorkspaceDashboard(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> generateDashboard(String interactionId, String jsessionid, String userId) throws ItorixException {
        landingPageStatsImpl.generateWorkspaceDashboard(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
