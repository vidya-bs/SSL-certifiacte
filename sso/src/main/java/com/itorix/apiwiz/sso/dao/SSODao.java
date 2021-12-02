package com.itorix.apiwiz.sso.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ItorixException;
import com.itorix.apiwiz.sso.model.*;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SSODao {

    private Logger logger = LoggerFactory.getLogger(SSODao.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @Qualifier("masterMongoTemplate")

    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Value("${itorix.sso.workspaceId}")
    private String workspaceId;

    public UserInfo createOrUpdateUser(SAMLCredential credentials) throws ItorixException {

        SAMLConfig samlConfig = getSamlConfig();
        if (samlConfig == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1016"), "SSO-1016");
        }

        String userId = credentials.getNameID().getValue();
        Query query = new Query(Criteria.where(User.LABEL_USER_ID).is(userId));
        User user = mongoTemplate.findOne(query, User.class);


        List<String> projectRoles = getProjectRoleForSaml(samlConfig, credentials);
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
            Workspace workspace = getWorkspace(workspaceId);
            List<UserWorkspace> workspaces = new ArrayList<>();
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setWorkspace(workspace);
            userWorkspace.setUserType("Member");
            userWorkspace.setRoles(projectRoles);
            userWorkspace.setActive(true);
            userWorkspace.setAcceptInvite(true);

            workspaces.add(userWorkspace);
            user.setWorkspaces(workspaces);
        } else if(user.getUserWorkspace(workspaceId) == null ) {
            Workspace workspace = getWorkspace(workspaceId);
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setWorkspace(workspace);
            userWorkspace.setUserType("Member");
            userWorkspace.setRoles(projectRoles);
            userWorkspace.setActive(true);
            userWorkspace.setAcceptInvite(true);
            user.getWorkspaces().add(userWorkspace);
        } else {
            if (projectRoles.size() > 1) {
                UserWorkspace userWorkspace = user.getUserWorkspace(workspaceId);
                userWorkspace.setRoles(projectRoles);
            }
        }
        mongoTemplate.save(user);
        return getUserInfo(user);
    }

    public Workspace getWorkspace(String workspaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workspaceId)));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        return workspace;
    }

    private UserInfo getUserInfo(User user) {
        UserWorkspace userWorkspace = user.getUserWorkspace(workspaceId);
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

    @SneakyThrows
    public void updateUser(User user, List<String> projectRoles) {
        Query query = new Query(Criteria.where("id").is(user.getId()));
        User existingUser = mongoTemplate.findOne(query, User.class);
        UserWorkspace userWorkspace = existingUser.getUserWorkspace(workspaceId);
        if(userWorkspace != null) {
            userWorkspace.setRoles(projectRoles);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-022"), "SSO-022");
        }
        mongoTemplate.save(existingUser);
    }

    public void createOrUpdateSamlConfig(SAMLConfig samlConfig) throws ItorixException {
        try {
            String jsonString = new ObjectMapper().writeValueAsString(samlConfig);
            UIMetadata metadata = new UIMetadata(UIMetadata.SAML_CONFIG, jsonString, workspaceId);
            createUIUXMetadata(metadata);
            Map<String, String> roleMapper = getRoleMapper(samlConfig.getRoles());
            jsonString = new ObjectMapper().writeValueAsString(roleMapper);
            metadata = new UIMetadata(UIMetadata.ROLE_MAPPER, jsonString, workspaceId);
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
            uIMetadata.setWorkspaceId(workspaceId);
            Query query = new Query(Criteria.where("query").is(metadata.getQuery()).and("workspaceId").is(workspaceId));
            Document dbDoc = new Document();
            masterMongoTemplate.getConverter().write(uIMetadata, dbDoc);
            Update update = Update.fromDocument(dbDoc, "_id");
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

    public List<String> getProjectRoleForSaml(SAMLConfig samlConfig, SAMLCredential credentials) {
        String samlAttribute = samlConfig.getGroup();
        Workspace workspace = getWorkspace(workspaceId);
        if (workspace.getIdpProvider().equals(IDPProvider.AZURE_AD)) {  //For Azure the User Group details are sent as roles
            samlAttribute = samlConfig.getUserRoles();
        }
        List<String> userAssertionRoles = new ArrayList<>();
        if (StringUtils.hasText(samlAttribute)) {
            String[] attributeAsStringArray = credentials.getAttributeAsStringArray(samlAttribute);
            if (attributeAsStringArray != null) {
                userAssertionRoles = Arrays.asList(attributeAsStringArray);
            }
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
        if (projectRoles.isEmpty()) {
            projectRoles.add(Roles.ANALYST.getValue());
        }
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
        SAMLConfig samlConfig = getSamlConfig();
        return samlConfig == null ? null : new String(samlConfig.getMetadata());
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
        Query dBquery = new Query(Criteria.where("query").is(query).and("workspaceId").is(workspaceId));
        List<UIMetadata> UIMetadata = masterMongoTemplate.find(dBquery, UIMetadata.class);
        if (UIMetadata != null && UIMetadata.size() > 0) {
            return UIMetadata.get(0);
        }
        else {
            return null;
        }
    }

}