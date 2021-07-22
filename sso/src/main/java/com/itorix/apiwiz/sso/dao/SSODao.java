package com.itorix.apiwiz.sso.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ItorixException;
import com.itorix.apiwiz.sso.model.Roles;
import com.itorix.apiwiz.sso.model.SAMLConfig;
import com.itorix.apiwiz.sso.model.UIMetadata;
import com.itorix.apiwiz.sso.model.User;
import com.itorix.apiwiz.sso.model.UserDefinedRoles;
import com.itorix.apiwiz.sso.model.UserInfo;
import com.itorix.apiwiz.sso.model.UserWorkspace;
import com.itorix.apiwiz.sso.model.Workspace;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SSODao {

    private Logger logger = LoggerFactory.getLogger(SSODao.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    public UserInfo createOrUpdateUser(SAMLCredential credentials) throws ItorixException {

        SAMLConfig samlConfig = getSamlConfig();
        if (samlConfig == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1016"), "SSO-1016");
        }

        String userId = credentials.getNameID().getValue();
        Query query = new Query(Criteria.where(User.LABEL_USER_ID).is(userId));
        User user = mongoTemplate.findOne(query, User.class);
        List<String> projectRoles = getProjectRoleForSaml(samlConfig.getGroup(), credentials);
        if (user == null) {
            user = new User();

            if (samlConfig.getFirstName() != null) {
                user.setFirstName(credentials.getAttributeAsString(samlConfig.getFirstName()));
            }

            if (samlConfig.getLastName() != null) {
                user.setLastName(credentials.getAttributeAsString(samlConfig.getLastName()));
            }

            if (samlConfig.getEmailId() != null) {
                user.setEmail(credentials.getAttributeAsString(samlConfig.getEmailId()));
            }

            if (samlConfig.getLoginId() != null) {
                user.setLoginId(credentials.getAttributeAsString(samlConfig.getEmailId()));
            }

            user.setUserId(credentials.getNameID().getValue());
            user.setUserStatus("Active");
            Workspace workspace = getWorkspace(getSamlConfig().getWorkspaceId());
            List<UserWorkspace> workspaces = new ArrayList<>();
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setWorkspace(workspace);
            userWorkspace.setUserType("Member");
            userWorkspace.setRoles(projectRoles);
            userWorkspace.setActive(true);

            workspaces.add(userWorkspace);
            user.setWorkspaces(workspaces);

        } else {
            if (projectRoles.size() > 1) {
                user.getWorkspaces().get(0).setRoles(projectRoles);
                updateUser(user, projectRoles);
            }
        }
        mongoTemplate.save(user);
        return getUserInfo(user);
    }

    public Workspace getWorkspace(String workapaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workapaceId)));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        return workspace;
    }

    private UserInfo getUserInfo(User user) {
        UserWorkspace userWorkspace = user.getWorkspaces().get(0);
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginId(user.getLoginId());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoles(userWorkspace.getRoles());
        userInfo.setPlanId(userWorkspace.getPlanId());
        userInfo.setUserType(userWorkspace.getUserType());
        userInfo.setUserStatus(user.getUserStatus());
        userInfo.setWorkspaceId(userWorkspace.getWorkspace().getName());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        return userInfo;
    }

    public void updateUser(User user, List<String> projectRoles) {
        Query query = new Query(Criteria.where("id").is(user.getId()));
        DBObject dbDoc = new BasicDBList();
        mongoTemplate.getConverter().write(projectRoles, dbDoc);
        Update update = Update.fromDBObject(dbDoc);
        mongoTemplate.upsert(query, update, "workspaces.0.roles");

    }

    public void createOrUpdateSamlConfig(SAMLConfig samlConfig) throws ItorixException {
        try {

            String jsonString = new ObjectMapper().writeValueAsString(samlConfig);
            UIMetadata metadata = new UIMetadata(UIMetadata.SAML_CONFIG, jsonString);
            createUIUXMetadata(metadata);
            Map<String, String> roleMapper = getRoleMapper(samlConfig.getRoles());
            jsonString = new ObjectMapper().writeValueAsString(roleMapper);
            metadata = new UIMetadata(UIMetadata.ROLE_MAPPER, jsonString);
            createUIUXMetadata(metadata);

        } catch (JsonProcessingException e) {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1027"), "SSO-1027");
        }
    }

    public UIMetadata createUIUXMetadata(UIMetadata metadata) {
        UIMetadata uIMetadata = getUIUXMetadata(metadata.getQuery());
        if (uIMetadata != null) {
            uIMetadata.setMetadata(metadata.getMetadata());
            uIMetadata.setQuery(metadata.getQuery());
            Query query = new Query(Criteria.where("query").is(metadata.getQuery()));
            DBObject dbDoc = new BasicDBObject();
            masterMongoTemplate.getConverter().write(uIMetadata, dbDoc);
            Update update = Update.fromDBObject(dbDoc, "_id");
            masterMongoTemplate.updateFirst(query, update, UIMetadata.class);
        } else {
            masterMongoTemplate.save(metadata);
            uIMetadata = getUIUXMetadata(metadata.getQuery());
        }
        return uIMetadata;

    }

    public SAMLConfig getSamlConfig() {

        UIMetadata uiuxMetadata = getUIUXMetadata(UIMetadata.SAML_CONFIG);
        try {
            return uiuxMetadata == null ? null
                    : new ObjectMapper().readValue(uiuxMetadata.getMetadata(), SAMLConfig.class);
        } catch (IOException e) {
            return null;

        }
    }

    public List<String> getProjectRoleForSaml(String samlGroupName, SAMLCredential credentials) {

        List<String> userAssertionRoles = null;
        if (StringUtils.hasText(samlGroupName)) {
            Attribute attribute = credentials.getAttribute(samlGroupName);
            if (attribute != null)
                userAssertionRoles = credentials.getAttribute(samlGroupName).getAttributeValues().stream()
                        .map(s -> ((XSStringImpl) s).getValue()).collect(Collectors.toList());
        }
        return getProjectRole(userAssertionRoles);
    }

    public List<String> getProjectRole(List<String> userDefinedRoles) {
        List<String> projectRoles = new ArrayList<>();

        try {
            Map<String, String> ssoRoleMappers = getRoleMapper();

            if (ssoRoleMappers != null && !ssoRoleMappers.isEmpty() && userDefinedRoles != null) {
                userDefinedRoles.forEach(s -> {
                    if (ssoRoleMappers.containsKey(s)) {
                        projectRoles.add(ssoRoleMappers.get(s));
                    }
                });
            }
        } catch (ItorixException e) {
            logger.error("error when getting project roles", e);
        }
        projectRoles.add(Roles.DEFAULT.getValue());
        return projectRoles;
    }

    public Map<String, String> getRoleMapper() throws ItorixException {
        try {
            UIMetadata uiuxMetadata = getUIUXMetadata(UIMetadata.ROLE_MAPPER);
            return uiuxMetadata == null ? null : new ObjectMapper().readValue(uiuxMetadata.getMetadata(),
                    new TypeReference<Map<String, String>>() {
                    });
        } catch (IOException e) {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1028"), "SSO-1028");
        }
    }

    public String getSSOMetadata() throws ItorixException {
        SAMLConfig metadata = getSamlConfig();
        return metadata == null ? null : new String(metadata.getMetadata());
    }

    public Map<String, String> getRoleMapper(UserDefinedRoles roles) {

        Map<String, String> mapper = new HashMap<>();
        if (roles != null) {
            mapper.put(roles.getAdmin(), Roles.ADMIN.getValue());
            mapper.put(roles.getAnalyst(), Roles.ANALYST.getValue());
            mapper.put(roles.getDeveloper(), Roles.DEVELOPER.getValue());
            mapper.put(roles.getOperation(), Roles.OPERATION.getValue());
            mapper.put(roles.getPortal(), Roles.PORTAL.getValue());
            mapper.put(roles.getProjectAdmin(), Roles.PROJECT_ADMIN.getValue());
            mapper.put(roles.getQa(), Roles.QA.getValue());
            mapper.put(roles.getTest(), Roles.TEST.getValue());
            mapper.remove(null);
        }
        return mapper;
    }

    public String getUserType(List<String> projectRoles) {
        if (projectRoles.contains(Roles.ADMIN.getValue())) {
            return "Admin";
        } else {
            return "Non_Admin";
        }
    }

    public UIMetadata getUIUXMetadata(String query) {
        Query dBquery = new Query(Criteria.where("query").is(query));
        List<UIMetadata> UIMetadata = masterMongoTemplate.find(dBquery, UIMetadata.class);
        if (UIMetadata != null && UIMetadata.size() > 0)
            return UIMetadata.get(0);
        else
            return null;
    }
}