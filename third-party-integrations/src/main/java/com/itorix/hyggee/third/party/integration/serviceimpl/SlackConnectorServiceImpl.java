package com.itorix.hyggee.third.party.integration.serviceimpl;

import com.itorix.apiwiz.common.model.slack.SlackWorkspace;
import com.itorix.apiwiz.common.util.slack.SlackUtil;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.hyggee.third.party.integration.service.SlackConnectorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Service
@CrossOrigin
@RestController
public class SlackConnectorServiceImpl implements SlackConnectorService {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    SlackUtil slackUtil;

    @Autowired
    @Qualifier("masterMongoTemplate")
    private MongoTemplate masterTemplate;

    private static final Logger logger = LoggerFactory.getLogger(SlackConnectorServiceImpl.class);


    @Override
    public ResponseEntity<?> installSlack(String interactionid, String jsessionid, SlackWorkspace slackWorkspace) throws Exception {
        logger.info("Session ID : {}", jsessionid);
        UserSession userSession = masterTemplate.findById(jsessionid, UserSession.class);
        logger.info("user session:{}", userSession);
        TenantContext.setCurrentTenant(userSession.getTenant());
        logger.info("installSlack {}", slackWorkspace);
        String tenant = TenantContext.getCurrentTenant();
        if (slackWorkspace.getWorkspaceName() == null || slackWorkspace.getToken() == null || slackWorkspace.getChannelList() == null)
            return new ResponseEntity<>("Please check the mandatory data fields", HttpStatus.INTERNAL_SERVER_ERROR);
        mongoTemplate.save(slackWorkspace);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getAllWorkspaces(String interactionid, String jsessionid) throws Exception {
        logger.info("getAllWorkspaces");
        UserSession userSession = masterTemplate.findById(jsessionid, UserSession.class);
        logger.info("user session:{}", userSession);
        TenantContext.setCurrentTenant(userSession.getTenant());
        String tenant = TenantContext.getCurrentTenant();
        Query query = new Query();
        List<SlackWorkspace> existingWorkspaces = mongoTemplate.findAll(SlackWorkspace.class);
        if (existingWorkspaces.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(existingWorkspaces, org.springframework.http.HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getWorkspaceById(String interactionid, String jsessionid, String workspaceId) throws Exception {
        logger.info("getWorkspaceById{}", workspaceId);
        UserSession userSession = masterTemplate.findById(jsessionid, UserSession.class);
        logger.info("user session:{}", userSession);
        TenantContext.setCurrentTenant(userSession.getTenant());
        String tenant = TenantContext.getCurrentTenant();
        Query query = new Query().addCriteria(Criteria.where("_id").is(workspaceId));
        SlackWorkspace slackWorkspace = mongoTemplate.findOne(query, SlackWorkspace.class);
        if (slackWorkspace != null) {
            return new ResponseEntity<>(slackWorkspace,
                    org.springframework.http.HttpStatus.OK);
        }
        return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);

    }

    @Override
    public ResponseEntity<?> updateSlackWorkspace(String interactionid, String jsessionid, String workspaceId, SlackWorkspace slackWorkspace) throws Exception {
        logger.info("editWorkspaceById{}", slackWorkspace);
        UserSession userSession = masterTemplate.findById(jsessionid, UserSession.class);
        logger.info("user session:{}", userSession);
        TenantContext.setCurrentTenant(userSession.getTenant());
        String tenant = TenantContext.getCurrentTenant();
        String slackWorkspaceName = slackWorkspace.getWorkspaceName();
        List<SlackWorkspace> workspaces = mongoTemplate.findAll(SlackWorkspace.class);
        if (workspaces.stream()
                .anyMatch(w -> StringUtils.equals(w.getWorkspaceName(), slackWorkspaceName))) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
//        Query query = new Query().addCriteria(Criteria.where("_id").is(workspaceId));
//        SlackWorkspace existingWorkspace = mongoTemplate.findOne(query, SlackWorkspace.class);
        SlackWorkspace existingWorkspace=workspaces.get(0);

        if (existingWorkspace!=null) {
            if (!StringUtils.isEmpty(slackWorkspace.getToken())) {
                existingWorkspace.setToken(slackWorkspace.getToken());
            }

            if (!StringUtils.isEmpty(slackWorkspace.getWorkspaceName())) {
                existingWorkspace.setWorkspaceName(slackWorkspace.getWorkspaceName());
            }

            if (slackWorkspace.getChannelList()!=null) {
                existingWorkspace.setChannelList(slackWorkspace.getChannelList());
            }
            mongoTemplate.save(existingWorkspace);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> deleteSlackWorkspace(String interactionid, String jsessionid, String workspaceId) throws Exception {
        logger.info("deleteWorkspaceById{}", workspaceId);
        UserSession userSession = masterTemplate.findById(jsessionid, UserSession.class);
        logger.info("user session:{}", userSession);
        TenantContext.setCurrentTenant(userSession.getTenant());
        String tenant = TenantContext.getCurrentTenant();
        Query query = new Query().addCriteria(Criteria.where("_id").is(workspaceId));
        SlackWorkspace slackWorkspace = mongoTemplate.findOne(query, SlackWorkspace.class);
        if (slackWorkspace == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        mongoTemplate.findAndRemove(query, SlackWorkspace.class);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
