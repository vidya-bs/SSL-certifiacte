package com.itorix.apiwiz.design.studio.serviceimpl;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.dto.ScmUploadDTO;
import com.itorix.apiwiz.design.studio.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Slf4j
public class SyncServiceImpl implements SyncService {
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String ASYNC = "async";
    private static final String GRAPHQL = "graphql";

    @Override
    public ResponseEntity<?> saveSync2RepoData(String interactionid, String jsessionid, String module, String id, int revision, ScmUploadDTO scmUploadDTO) throws Exception {
        Query query = new Query();
        String documentName = null;
        if (module.equalsIgnoreCase(ASYNC)) {
            documentName = "Design.AsyncApi.List";
            query.addCriteria(Criteria.where("asyncApiId").is(id).and("revision").is(revision));
            if (!checkIfExists(query, documentName)) {
                throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1013"),"AsyncApi-1013");
            }
        } else if (module.equalsIgnoreCase(GRAPHQL)) {
            query.addCriteria(Criteria.where("graphQLId").is(id).and("revision").is(revision));
            documentName = "Design.GraphQL.List";
            if (!checkIfExists(query, documentName)) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"), id, revision), "GraphQL-1000");
            }
        } else {
            throw new ItorixException("Invalid module name", "General-1001");
        }

        Update update = new Update();
        update.set("enableScm", scmUploadDTO.isEnableScm());
        update.set("repoName",scmUploadDTO.getRepoName());
        update.set("branch",scmUploadDTO.getBranch());
        update.set("hostUrl",scmUploadDTO.getHostUrl());
        update.set("folderName",scmUploadDTO.getFolderName());
        update.set("token",scmUploadDTO.getToken());
        update.set("commitMessage", scmUploadDTO.getCommitMessage());
        update.set("scmSource",scmUploadDTO.getScmSource());
        update.set("username",scmUploadDTO.getUsername());
        update.set("password",scmUploadDTO.getPassword());
        update.set("authType",scmUploadDTO.getAuthType());
        update.set("mts", System.currentTimeMillis());
        mongoTemplate.updateFirst(query,update, documentName);
        log.info("Updated sync to repo data for {} - {} revision:{}", module, id, revision);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private boolean checkIfExists(Query query, String documentName) {
        return mongoTemplate.exists(query, documentName);
    }
}
