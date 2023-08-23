package com.itorix.apiwiz.identitymanagement.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.SwaggerContacts;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.identitymanagement.model.*;
import com.mongodb.client.result.UpdateResult;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class IdentityManagementDao {

    private Logger logger = LoggerFactory.getLogger(IdentityManagementDao.class);
    @Autowired
    protected BaseRepository baseRepository;
    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected HttpServletResponse response;

    public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RSAEncryption rsaEncryption;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private WorkspaceDao workspaceDao;

    @Value("${itorix.core.user.management.redirection.user.add:https://{0}/user/{1}")
    private String addUserURL;

    @Value("${itorix.core.user.management.redirection.user.invite:https://{0}/user-invited/{1}")
    private String inviteUserURL;

    @Value("${itorix.core.user.management.redirection.password.reset:https://{0}/reset-password/{1}")
    private String resetPasswordURL;

    @Value("${itorix.core.user.management.redirection.user.register:https://{0}/register/{1}/verify}")
    private String registerUserURL;
    private static final String MONGODB = "mongodb";
    private static final String POSTGRES = "postgres";
    @PostConstruct
    private void initDBProperties() {
        applicationProperties = getDBApplicationProperties();
        getRegionData();
        getPodHost();
    }

	private void getRegionData() {
		String endPoint = applicationProperties.getAwsURL();
    //check for null
      if(endPoint==null){
          return;
      }
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			logger.debug("Making a call to {}", endPoint);
			ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity,
					new ParameterizedTypeReference<String>() {
					});
			JsonNode json = new ObjectMapper().readTree(response.getBody());
			applicationProperties.setRegion(json.get("region").asText());
			applicationProperties.setAvailabilityZone(json.get("availabilityZone").asText());
			applicationProperties.setPodIP(json.get("privateIp").asText());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
	}

	private void getPodHost() {
		String endPoint = applicationProperties.getAwsPodURL();
    //check for null
      if(endPoint==null){
          return;
      }
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			logger.debug("Making a call to {}", endPoint);
			ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity,
					new ParameterizedTypeReference<String>() {
					});
			applicationProperties.setPodHost(response.getBody());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
	}

    private ApplicationProperties getDBApplicationProperties() {
        List<DBConfig> dbConfigList = getDBProperties();
        Map<String, String> map = new HashMap<>();
        if (dbConfigList != null) {
            for (DBConfig dbConfig : dbConfigList) {
                map.put(dbConfig.getPropertyKey(), dbConfig.getPropertyValue());
            }
        }
        return populateDBApplicationProperties(map);
    }

	public UserSession authenticate(UserInfo userInfo, boolean preAuthenticated) throws Exception {
		logger.debug("UserService.authenticate : " + userInfo);
		UserSession userSession = null;
		if (preAuthenticated || userInfo.allowLogin()) {
			logger.debug("Authenticating user session");
            User user = !preAuthenticated ?findByEmailUserName(userInfo.getLoginId()) : findByEmail(userInfo.getEmail());
			Workspace workspace = getWorkspace(userInfo.getWorkspaceId().toLowerCase());
            if (workspace == null) {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1044"), "Identity-1044");
            }
            if(!workspace.getStatus().equalsIgnoreCase("active")){
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1013"), "Identity-1013");
            }
			if (user == null) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1045"), "Identity-1045");
			}
			UserWorkspace userWorkspace = user.getUserWorkspace(userInfo.getWorkspaceId().toLowerCase());
			if (userWorkspace == null) { // (!user.getUserWorkspace(userInfo.getWorkspaceId()).getActive())){
				throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1045"), "Identity-1045");
			}
        if ((!userWorkspace.getActive() && userWorkspace.getAcceptInvite())) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1046"), "Identity-1046");
        }
        if (userWorkspace == null || userWorkspace.getActive() != true) { // (!user.getUserWorkspace(userInfo.getWorkspaceId()).getActive())){
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1044"), "Identity-1044");
        }

        if(user.getUserStatus().equalsIgnoreCase("Locked"))
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1047"), "Identity-1047");


        if (user.canLogin() != true) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1048"), "Identity-1048");
        }

        String userHashedPassword = "";
        String userInfoHashedPassword = "";
        if(userInfo.getPassword()!=null && user.getPassword()!=null){
            String userInfoPassword = rsaEncryption.decryptText(userInfo.getPassword());
            userHashedPassword = user.getPassword();
            userInfoHashedPassword = getHashedValue(userInfoPassword);
        }
        if (!preAuthenticated && !user.isServiceAccount()) {
            if(user.getPasswordLastChangedDate()==0)
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1050"),"Identity-1050");
            long passwordLastChangedDate = user.getPasswordLastChangedDate();
            long thirtyDay = Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond();
            if(passwordLastChangedDate<thirtyDay) {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1050"),"Identity-1050");
            } else {
                System.out.println("The given epoch time is within 30 days from the current time.");
                logger.info("The given epoch time is within 30 days from the current time.");
            }
        }
        if (preAuthenticated || userHashedPassword.equals(userInfoHashedPassword)) {
            userSession = new UserSession(user);
            userSession.setRequestAttributes(request);
            userSession.setLoginId(user.getLoginId());
            userSession.setWorkspaceId(workspace.getName());
            userSession.setTenant(workspace.getTenant());
            userSession.setRoles(user.getUserWorkspace(workspace.getName()).getRoles());
            userSession.setUserType(user.getUserWorkspace(workspace.getName()).getUserType());
            String status = workspace.getStatus() != null && workspace.getStatus() != ""
                ? workspace.getStatus()
                : "active";
            userSession.setStatus(status);
            user.setUserCount(0);
            user.setLastLoginTime(workspace.getName());
            saveUser(user);
            userSession.setPlanId(workspace.getPlanId());
            userSession.setPaymentSchedule(workspace.getPaymentSchedule());
            userSession.setSubscriptionId(workspace.getSubscriptionId());
            if (workspace.getIsTrial() == true) {
                Date now = new Date();
                if (workspace.getExpiresOn() != null && now.compareTo(workspace.getExpiresOn()) > 0) {
                    long diff = workspace.getExpiresOn().getTime() - now.getTime();
                    int days = 0;
                    if (diff > 0)
                        days = (int) diff / 1000 / 60 / 60 / 24;
                    userSession.setIsTrial(true);
                    userSession.setTrialPeriod(workspace.getTrialPeriod());
                    userSession.setTrialExpired("true");
                    userSession.setExpiresOn(String.valueOf(days));
                    userSession.setStatus("cancelled");
                } else {
                    long diff = workspace.getExpiresOn().getTime() - now.getTime();
                    int days = 0;
                    if (diff > 0)
                        days = (int) diff / 1000 / 60 / 60 / 24;
                    userSession.setIsTrial(true);
                    userSession.setTrialPeriod(workspace.getTrialPeriod());
                    userSession.setTrialExpired("false");
                    userSession.setExpiresOn(String.valueOf(days));
                    userSession.setStatus(status);
                }
            }
            masterMongoTemplate.save(userSession);
            return userSession;
        }
        else {
            if (user.getUserCount() < 5) {
                user.setUserCount(user.getUserCount() + 1);
                saveUser(user);
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1036"), "Identity-1036");
            } else {
                user.setUserStatus(UserStatus.getStatus(UserStatus.LOCKED));
                List<UserWorkspace> userWorkspaceList=user.getWorkspaces();
                userWorkspaceList.forEach(w->w.setActive(false));
                saveUser(user);
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1047"), "Identity-1047");
            }
        }

        }
        throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1036"), "Identity-1036");
    }

    public void createSaltMetaData(MetaData metadataStr) {
        Query query = new Query().addCriteria(Criteria.where("key").is("salt"));
        MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
        if (metaData != null) {
            Update update = new Update();
            update.set("metadata", metadataStr.getKey());
            masterMongoTemplate.updateFirst(query, update, MetaData.class);
        } else
            masterMongoTemplate.save(new MetaData("salt", metadataStr.getKey()));
    }

    public MetaData getSaltMetaData() {
        Query query = new Query().addCriteria(Criteria.where("key").is("salt"));
        return masterMongoTemplate.findOne(query, MetaData.class);
    }

    public void registerOAuth2User(UserInfo userInfo) throws ItorixException {
        userInfo.setWorkspaceId(userInfo.getWorkspaceId());
        Workspace workspace = getWorkspace(userInfo.getWorkspaceId().toLowerCase());

        User loginUser = findByEmailUserName(userInfo.getLoginId());
        if (loginUser != null && !loginUser.isWorkspaceAdmin(userInfo.getWorkspaceId().toLowerCase()) == true) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
        }
        boolean isNewUser = false;
        User user = findByUserEmail(userInfo.getEmail());
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setEmail(userInfo.getEmail());
        }
        if (user.containsWorkspace(userInfo.getWorkspaceId().toLowerCase()) != true) {
            List<UserWorkspace> workspaces;
            if (user.getWorkspaces() == null)
                workspaces = new ArrayList<>();
            else
                workspaces = user.getWorkspaces();
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setWorkspace(workspace);
            userWorkspace.setUserType(User.LABEL_MEMBER);

            userWorkspace.setCreatedUserName(userInfo.getFirstName() + " " + userInfo.getLastName());
            userWorkspace.setCts(System.currentTimeMillis());
            userWorkspace.setRoles(Arrays.asList("Developer"));
            workspaces.add(userWorkspace);
            user.setWorkspaces(workspaces);
        }
        VerificationToken token = createVerificationToken("AddUserToWorkspace", user.getEmail(),null);
        token.setUsed(false);
        token.setWorkspaceId(userInfo.getWorkspaceId().toLowerCase());
        token.setUserType(User.LABEL_MEMBER);
        saveVerificationToken(token);


        user.setLoginId(userInfo.getLoginId());
        user.setPassword(userInfo.getPassword());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setRegionCode("");
        user.setWorkPhone("");
        user.setCompany("");
        user.setSubscribeNewsLetter(true);
        user.setUserStatus("active");
        if (userInfo.getMetadata() != null)
            user.setMetadata(userInfo.getMetadata());
        for (UserWorkspace userWorkspace : user.getWorkspaces())
            if (userWorkspace.getWorkspace().getName().equals(token.getWorkspaceId().toLowerCase())) {
                userWorkspace.getWorkspace().setStatus("active");
                userWorkspace.setAcceptInvite(true);
                userWorkspace.setActive(true);
            }
        user = saveUser(user);
        token.setUsed(true);
        saveVerificationToken(token);
    }

    public Object addUser(UserInfo userInfo) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        userInfo.setWorkspaceId(userSessionToken.getWorkspaceId());
        Workspace workspace = getWorkspace(userInfo.getWorkspaceId().toLowerCase());
        if (userInfo.allowInviteUser() == false && workspace == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1039"), "Identity-1039");
        }
        User loginUser = findUserById(userSessionToken.getUserId());
        if (!loginUser.isWorkspaceAdmin(userInfo.getWorkspaceId().toLowerCase()) == true) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
        }
        boolean isNewUser = false;
        User user = findByUserEmail(userInfo.getEmail());
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setEmail(userInfo.getEmail());
        }
        if (user.containsWorkspace(userInfo.getWorkspaceId().toLowerCase()) != true) {
            List<UserWorkspace> workspaces;
            if (user.getWorkspaces() == null)
                workspaces = new ArrayList<>();
            else
                workspaces = user.getWorkspaces();
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setWorkspace(workspace);
            userWorkspace.setUserType(User.LABEL_MEMBER);
            userWorkspace.setCreatedUserName(userSessionToken.getFirstName() + " " + userSessionToken.getLastName());
            userWorkspace.setCts(System.currentTimeMillis());
            userWorkspace.setRoles(userInfo.getRoles());
            workspaces.add(userWorkspace);
            user.setWorkspaces(workspaces);
        }
        VerificationToken token = createVerificationToken("AddUserToWorkspace", user.getEmail(),null);
        token.setWorkspaceId(userInfo.getWorkspaceId().toLowerCase());
        token.setUserType(User.LABEL_MEMBER);
        saveVerificationToken(token);
        if (isNewUser)
            sendAddUserEmail(token, user, loginUser.getFirstName() + " " + loginUser.getLastName());
        else
            sendInviteUserEmail(token, user, loginUser.getFirstName() + " " + loginUser.getLastName());
        user = saveUser(user);
        return "";
    }

    public Object registerWithToken(UserInfo userInfo, VerificationToken token) throws ItorixException {
        if (userInfo.allowInviteRegistration() == true) {
            User user = findByUserEmail(token.getUserEmail());
            if (user == null) {
            }
            VerifyToken(token);
            user.setLoginId(userInfo.getLoginId());
            user.setPassword(userInfo.getPassword());
            user.setFirstName(userInfo.getFirstName());
            user.setLastName(userInfo.getLastName());
            user.setRegionCode(userInfo.getRegionCode());
            user.setWorkPhone(userInfo.getWorkPhone());
            user.setCompany(userInfo.getCompany());
            user.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
            user.setIsServiceAccount(userInfo.isServiceAccount());
            user.setUserStatus("active");
            if (userInfo.getMetadata() != null)
                user.setMetadata(userInfo.getMetadata());
            for (UserWorkspace workspace : user.getWorkspaces())
                if (workspace.getWorkspace().getName().equals(token.getWorkspaceId().toLowerCase())) {
                    workspace.getWorkspace().setStatus("active");
                    workspace.setAcceptInvite(true);
                    workspace.setActive(true);
                }
            user.setPasswordLastChangedDate(System.currentTimeMillis());
            user = saveUser(user);
            token.setUsed(true);
            saveVerificationToken(token);
            sendActivationEmail(user);
        }
        return "";
    }

    public void updateUser(UserInfo userInfo) throws ItorixException {
        if (userInfo.allowEditUser()) {
            UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
            User loginUser = findUserById(userSessionToken.getUserId());
            if (!loginUser.getEmail().equals(userInfo.getEmail()) && findByEmailUserName(userInfo.getEmail()) != null) {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1036"), "Identity-1036");
            }
            if (userInfo.getLoginId() != null && !loginUser.getLoginId().equals(userInfo.getLoginId())
                    && findByEmailUserName(userInfo.getLoginId()) != null) {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1036"), "Identity-1036");
            }
            if (userInfo.getLoginId() != null)
                loginUser.setLoginId(userInfo.getLoginId());
            if (userInfo.getEmail() != null)
                loginUser.setEmail(userInfo.getEmail());
            if (userInfo.getFirstName() != null)
                loginUser.setFirstName(userInfo.getFirstName());
            if (userInfo.getLastName() != null)
                loginUser.setLastName(userInfo.getLastName());
            if (userInfo.getRegionCode() != null)
                loginUser.setRegionCode(userInfo.getRegionCode());
            if (userInfo.getWorkPhone() != null)
                loginUser.setWorkPhone(userInfo.getWorkPhone());
            if (userInfo.getCompany() != null)
                loginUser.setCompany(userInfo.getCompany());
            loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
            saveUser(loginUser);
        }
    }

    public void updateUserSubscription(UserInfo userInfo) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
        saveUser(loginUser);
    }

    public void updateNewsSubscription(UserInfo userInfo) throws ItorixException {
        User loginUser = findByUserEmail(userInfo.getEmail());
        if (loginUser != null) {
            loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
            saveUser(loginUser);
        }
    }

    public Map<String, Object> getNewsSubscription(String email) throws ItorixException {
        Map<String, Object> usernewsLetter = new HashMap<>();
        User loginUser = findByUserEmail(email);
        if (loginUser != null) {
            usernewsLetter.put("subscribeNewsLetter", loginUser.getSubscribeNewsLetter());
            return usernewsLetter;
        }
        return usernewsLetter;
    }

    public User getUser(String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true) {
            User user = findUserById(userId);
            if (user.getUserWorkspace(workspaceId) != null)
                return user;
            else
                return null;
        }
        throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
    }

    public void updateMetaData(Map<String, String> metadata) {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        loginUser.setMetadata(metadata);
        saveUser(loginUser);
    }

    public Map<String, String> getMetaData() {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        return loginUser.getMetadata();
    }

    public List<SwaggerTeam> getTeams(String userId, boolean avoidCheck) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (avoidCheck || loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true) {
            User user = findUserById(userId);
            if (user != null && user.containsWorkspace(workspaceId)) {
                Query query = new Query().addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
                List<SwaggerTeam> teams = mongoTemplate.find(query, SwaggerTeam.class);
                if (teams != null)
                    return trimTeams(teams, user);
                else
                    return new ArrayList<SwaggerTeam>();
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
        }
    }

    private List<SwaggerTeam> trimTeams(List<SwaggerTeam> teams, User user) {
        for (SwaggerTeam team : teams) {
            team.setProjects(null);
            team.setSwagger3(null);
            team.setSwaggers(null);
            team.setModifiedBy(null);
            team.setCreatedBy(null);
            team.setCreatedUserName(null);
            team.setModifiedUserName(null);
            team.setMts(null);
            team.setCts(null);
            List<SwaggerContacts> contacts = new ArrayList<SwaggerContacts>();
            contacts.addAll(team.getContacts());
            for (SwaggerContacts contact : team.getContacts()) {
                String email = contact.getEmail();
                if (!email.equals(user.getEmail()))
                    contacts.remove(contact);
            }
            team.setContacts(contacts);
        }
        return teams;
    }

    public void updateUserTeams(List<SwaggerTeam> teams, String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true) {
            User user = findUserById(userId);
            if (user != null && user.containsWorkspace(workspaceId)) {
                Query query = new Query().addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
                List<SwaggerTeam> dBteams = mongoTemplate.find(query, SwaggerTeam.class);
                if (dBteams != null) {
                    Set<String> teamNames = new HashSet<String>();
                    for (SwaggerTeam dBteam : dBteams)
                        teamNames.add(dBteam.getName());
                    Set<SwaggerTeam> newTeamNames = new HashSet<SwaggerTeam>();
                    for (SwaggerTeam team : teams) {
                        boolean newTeam = true;
                        for (SwaggerTeam dBteam : dBteams) {
                            if (team.getName().equals(dBteam.getName())) {
                                dBteam.updateContact(team.getContacts().get(0));
                                teamNames.remove(dBteam.getName());
                                baseRepository.save(dBteam);
                                newTeam = false;
                            }
                        }
                        if (newTeam)
                            newTeamNames.add(team);
                    }
                    addUserTeam(newTeamNames, user);
                    deleteUserTeams(teamNames, user);
                } else {
                    throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1042"), "Identity-1042");
                }
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1042"), "Identity-1042");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1042"), "Identity-1042");
        }
    }

    private void addUserTeam(Set<SwaggerTeam> teams, User user) {
        SwaggerContacts swaggerContact = new SwaggerContacts();
        swaggerContact.setEmail(user.getEmail());
        for (SwaggerTeam team : teams) {
            Query query = new Query().addCriteria(Criteria.where("name").is(team.getName()));
            SwaggerTeam dBteam = mongoTemplate.findOne(query, SwaggerTeam.class);
            if (dBteam != null) {
                dBteam.updateContact(team.getContacts().get(0));
                baseRepository.save(dBteam);
            }
        }
    }

    private void deleteUserTeams(Set<String> teams, User user) {
        SwaggerContacts swaggerContact = new SwaggerContacts();
        swaggerContact.setEmail(user.getEmail());
        for (String teamName : teams) {
            Query query = new Query().addCriteria(Criteria.where("name").is(teamName));
            SwaggerTeam dBteam = mongoTemplate.findOne(query, SwaggerTeam.class);
            dBteam.removeContact(swaggerContact);
            baseRepository.save(dBteam);
        }
    }

    public Object registerWithMail(UserInfo userInfo,String appType) throws ItorixException {
        String domainId = userInfo.getEmail().split("@")[1];
        boolean domainAllowed = isDomainAllowed(userInfo.getEmail());
        if (domainAllowed) {
            User userByEmail = findByEmail(userInfo.getEmail());
            if (userByEmail == null) {
                User user = new User();
                user.setEmail(userInfo.getEmail());
                VerificationToken token = createVerificationToken("registerUser", user.getEmail(),appType);
                saveVerificationToken(token);
                if (sendRegistrationEmail(token, user)){
                    user.setPasswordLastChangedDate(System.currentTimeMillis());
                    user = saveUser(user);
                }

                else
                    throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1031"), "Identity-1031");
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("USER_005"), "USER_005");
            }
        } else {
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Identity-1038"), domainId),
                    "Identity-1038");
        }
        return "";
    }

    public Object register(UserInfo userInfo, VerificationToken token) throws ItorixException {
        if (userInfo.allowUserRegistration() == true) {
            User user = findByEmail(token.getUserEmail());
            if (user.getUserStatus() != null && user.getUserStatus().equals("active")) {
                // User user = new User();
                user.setFirstName(userInfo.getFirstName());
                user.setLoginId(userInfo.getLoginId());
                user.setLastName(userInfo.getLastName());
                user.setPassword(userInfo.getPassword());
                user.setRegionCode(userInfo.getRegionCode());
                user.setWorkPhone(userInfo.getWorkPhone());
                user.setCompany(userInfo.getCompany());
                user.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
                user.setIsServiceAccount(userInfo.isServiceAccount());
                List<String> userRoles = new ArrayList<>();
                for (Roles role : Roles.values())
                    userRoles.add(role.getValue());
                user.setUserCount(0);
                Workspace workspace = createActiveWorkspace(userInfo);
                List<UserWorkspace> workspaces = new ArrayList<>();
                UserWorkspace userWorkspace = new UserWorkspace();
                userWorkspace.setWorkspace(workspace);
                userWorkspace.setUserType("Site-Admin");
                userWorkspace.setRoles(userRoles);
                userWorkspace.setActive(true);
                userWorkspace.setAcceptInvite(true);
                workspaces.add(userWorkspace);
                user.setWorkspaces(workspaces);
                user.setPasswordLastChangedDate(System.currentTimeMillis());
                user = saveUser(user);
                token.setUsed(true);
                saveVerificationToken(token);
                sendActivationEmail(user);
            } else {
                throw new ItorixException("user email is not verified", "USER_005");
            }
        }
        return "";
    }

    private void sendActivationEmail(User user) {
        try {
            List<String> toMailId = new ArrayList<>();
            EmailTemplate template = new EmailTemplate();
            template.setSubject(applicationProperties.getUserActivationMailSubject());
            toMailId.add(user.getEmail());
            template.setToMailId(toMailId);
            String messageBody = MessageFormat.format(applicationProperties.getUserActivationMailBody(),
                    user.getFirstName() + " " + user.getLastName(), applicationProperties.getAppURL());
            template.setBody(messageBody);
            mailUtil.sendEmail(template);
        } catch (Exception e) {

        }
    }

    public VerificationToken password(User user) throws ItorixException {
        User userByEmail = findByEmail(user.getEmail());
        if (userByEmail != null) {
            VerificationToken token = createVerificationToken("resetPassword", user.getEmail(), null);
            sendPassWordResetEmail(token, userByEmail);
            saveVerificationToken(token);
            return token;
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1000"), "Identity-1000");
        }
    }

    public User updatePassword(User user, VerificationToken token) throws ItorixException {
        if (token.isAlive() && !token.getUsed()) {
            User userByEmail = findByEmail(user.getEmail());
            if (userByEmail != null) {
                userByEmail.setPassword(user.getPassword());
                userByEmail.setUserStatus("active");
                userByEmail.setPasswordLastChangedDate(System.currentTimeMillis());
                userByEmail = saveUser(userByEmail);
                token.setUsed(true);
                saveVerificationToken(token);
                return userByEmail;
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
        }
    }

    public Object changePassword(User user) throws Exception {
        if (user.getOldPassword() == null || user.getOldPassword().trim() == "" || user.getNewPassword() == null
                || user.getNewPassword().trim() == "")
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1007"), "Identity-1007");
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User dbUser = findUserById(userSessionToken.getUserId());
        if (dbUser != null) {
            String hashedDbPassword = dbUser.getPassword();
            String hashedUserOldPassword = getHashedValue(rsaEncryption.decryptText(user.getOldPassword()));
            String hashedNewPassword = getHashedValue(rsaEncryption.decryptText(user.getNewPassword()));

            if (hashedDbPassword.equals(hashedUserOldPassword)) {
                dbUser.setPasswordLastChangedDate(System.currentTimeMillis());
                dbUser.setPassword(hashedNewPassword);
                dbUser = saveUser(dbUser);
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("USER_004"), "USER_004");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
        return "";
    }

    public void removeWorkspaceUser(String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        String workspaceId = userSessionToken.getWorkspaceId();
        User user = findUserById(userId);
        if (user != null) {
            if (loginUser.isWorkspaceAdmin(workspaceId)) {
                List<UserWorkspace> workspaces = user.getWorkspaces();
                for (UserWorkspace workspace : workspaces)
                    if (workspace.getWorkspace().getName().equals(workspaceId)) {
                        if (workspaces.size() == 1) {
                            removeUser(user);
                            removeUnusedTokens(user);
                        } else {
                            workspaces.remove(workspace);
                            user.setWorkspaces(workspaces);
                            saveUser(user);
                            removeUnusedTokens(user);
                        }
                        break;
                    }
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
    }

    private void removeUnusedTokens(User user) {
        Query query = new Query(Criteria.where("userEmail").is(user.getEmail()).and("used").is(false));
        masterMongoTemplate.remove(query, VerificationToken.class);
    }

    private void removeUser(User user) {
        masterMongoTemplate.remove(user);
    }

    public void addSiteAdmin(String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        String workspaceId = userSessionToken.getWorkspaceId();
        User user = findUserById(userId);
        if (user != null && user.getUserWorkspace(workspaceId) != null) {
            if (loginUser.isWorkspaceAdmin(workspaceId) == true) {
                List<UserWorkspace> workspaces = user.getWorkspaces();
                for (UserWorkspace workspace : workspaces)
                    if (workspace.getWorkspace().getName().equals(workspaceId)) {
                        workspace.setUserType("Site-Admin");
                        break;
                    }
                user.setWorkspaces(workspaces);
                saveUser(user);
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
    }

    public List<String> getAllUsersWithRoleDevOPS() {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        List<User> users = findUsersByWorkspace(workspaceId);
        List<String> mailIds = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                UserWorkspace userWorkspace = user.getUserWorkspace(workspaceId);
                if (userWorkspace != null) {
                    if (userWorkspace.getAcceptInvite() != false) {
                        mailIds.add(user.getEmail());
                    }
                }
            }
                return mailIds;
            }
        return mailIds;
    }

    public void updateWorkspaceUserRoles(String userId, List<String> roles) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        User user = findUserById(userId);
        if (user != null) {
            if (loginUser.isWorkspaceAdmin(workspaceId)) {
                List<UserWorkspace> workspaces = user.getWorkspaces();
                for (UserWorkspace workspace : workspaces)
                    if (workspace.getWorkspace().getName().equals(workspaceId)) {
                        workspace.setRoles(roles);
                        break;
                    }
                user.setWorkspaces(workspaces);
                saveUser(user);
            } else {
                throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
    }

    public Workspace createWorkspace(UserInfo userInfo) throws ItorixException {
        return createWorkspace(userInfo, "ActivationPending");
    }

    public Workspace createActiveWorkspace(UserInfo userInfo) throws ItorixException {
        return createWorkspace(userInfo, "active");
    }

    public Workspace createWorkspace(UserInfo userInfo, String status) throws ItorixException {
        Query query = new Query();
        query.addCriteria(
                new Criteria().orOperator(Criteria.where("name").is(userInfo.getWorkspaceId())));
        Workspace workspace = mongoTemplate.findOne(query, Workspace.class);
        if (workspace == null) {
            workspace = new Workspace();
            workspace.setName(userInfo.getWorkspaceId());
            workspace.setPlanId(userInfo.getPlanId());
            workspace.setTenant(userInfo.getWorkspaceId());
            workspace.setStatus(status);
            workspace.setKey(UUID.randomUUID().toString());
            workspace.setRegionCode(userInfo.getRegionCode());
            workspace.setSubscriptionId(userInfo.getSubscriptionId());
            workspace.setPaymentSchedule(userInfo.getPaymentSchedule());
            workspace.setTrialPeriod(userInfo.getTrialPeriod());
            workspace.setIsTrial(userInfo.isTrial());
            workspace.setLicenceKey(UUID.randomUUID().toString());
            workspace.setSeats(userInfo.getSeats());
            if (userInfo.getTrialPeriod() != null) {
                try {
                    int days = Integer.valueOf(userInfo.getTrialPeriod());
                    Date vailidTill = Date
                            .from(LocalDateTime.now().plusDays(days).atZone(ZoneId.systemDefault()).toInstant());
                    workspace.setExpiresOn(vailidTill);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
            workspace.setCts(System.currentTimeMillis());
            workspace.setMts(System.currentTimeMillis());
            masterMongoTemplate.save(workspace);
            return workspace;
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1035"), "Identity-1035");
        }
    }

    public void recoverWorkspace(String email) throws ItorixException{
        User user = findByEmailUserName(email);
        if(user!=null) {
            StringBuilder workspacesStr = new StringBuilder();
            int length = user.getWorkspaces().size();
            int index = 1;
            List<UserWorkspace> workspaces = user.getWorkspaces();
            for (UserWorkspace workspace : workspaces) {
                workspacesStr.append(workspace.getWorkspace().getName());
                if (index < length)
                    workspacesStr.append(", ");
                index++;
            }
            try {
                String bodyText = MessageFormat.format(applicationProperties.getRecoverWorkspaceBody(),
                        user.getFirstName() + " " + user.getLastName(), workspacesStr, applicationProperties.getAppURL());
                ArrayList<String> toRecipients = new ArrayList<String>();
                toRecipients.add(user.getEmail());
                sendMail(applicationProperties.getRecoverWorkspaceSubject(), bodyText, toRecipients);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
    }

    private long getMinimumSeats(Workspace workspace) {
        Subscription subscription = workspaceDao.getSubscription(workspace.getPlanId());
        List<SubscriptionPrice> prices = subscription.getSubscriptionPrices();
        String subscriptionPrice = subscription.getPricing().replaceAll("\\$", "");
        String subscriptionPrice1=subscriptionPrice.trim();
        boolean subscriptionValue=subscriptionPrice.equalsIgnoreCase("0");
        boolean subscriptionVal=subscriptionPrice1.equalsIgnoreCase("Custom");
        if (!subscriptionValue && !subscriptionVal){
            if (workspace.getPaymentSchedule().equalsIgnoreCase("month")
                    || workspace.getPaymentSchedule().equalsIgnoreCase("monthly")) {
                SubscriptionPrice price = prices.stream().filter(o -> o.getPeriod().equalsIgnoreCase("MONTHLY"))
                        .collect(Collectors.toList()).get(0);
                return Long.parseLong(price.getMinimumUnits());
            } else {
                SubscriptionPrice price = prices.stream().filter(o -> o.getPeriod().equalsIgnoreCase("YEARLY"))
                        .collect(Collectors.toList()).get(0);
                return Long.parseLong(price.getMinimumUnits());
            }
        } else {
            return Long.parseLong("1");
        }
    }

    public Map<String, Object> validateWorkspace(String workspaceId) {
        Workspace workspace = getWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<String, Object>();
        if (workspace != null) {
            long usedSeats = workspaceDao.getUsedSeats(workspaceId);
            boolean allowDowngrade = false;
            long minimumSeats = getMinimumSeats(workspace);
            long seats = usedSeats < minimumSeats
                    ? workspace.getSeats() - minimumSeats
                    : workspace.getSeats() - usedSeats;
            allowDowngrade = seats > 0 ? true : false;
            boolean inviteUser = workspace.getSeats() - usedSeats > 0 ? true : false;
            response.put("status", "false");
            response.put("planId", workspace.getPlanId());
            response.put("allotedSeats", workspace.getSeats());
            response.put("currentSeats", usedSeats);
            response.put("allowDowngrade", allowDowngrade);
            response.put("inviteUser", inviteUser);
            response.put("remainingSeats", seats);
            response.put("ssoEnabled", workspace.getSsoEnabled());
            response.put("tenantId", workspace.getKey());
            if (workspace.getSsoEnabled() == true) {
                response.put("ssoHost", workspace.getSsoHost());
                response.put("ssoPath", workspace.getSsoPath());
                response.put("idpProvider", workspace.getIdpProvider());
            }

        } else {
            response.put("status", "true");
        }
        return response;
    }
    public Map<String, Object> checkWorkspace(String workspaceId) throws JsonProcessingException {
        Workspace workspace = getWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<>();
        response.put("isValid",workspaceId.matches("^[a-z]++(?:-[a-z]++)*+$") && workspace == null && !getRestrictedWorkspaceNames().contains(workspaceId));
        return response;
    }

    public Map<String, Object> validateSeats(long seats) {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        Workspace workspace = getWorkspace(workspaceId);
        Map<String, Object> response = new HashMap<String, Object>();
        long usedSeats = workspaceDao.getUsedSeats(workspaceId);
        if (seats > workspace.getSeats())
            response.put("status", "false");
        else
            response.put("status", "true");
        response.put("planId", workspace.getPlanId());
        response.put("allotedSeats", workspace.getSeats());
        response.put("currentSeats", usedSeats);
        response.put("remainingSeats", (workspace.getSeats() - usedSeats));
        return response;
    }

    public Map<String, String> validateUserId(String userId) {
        User user = findByEmailUserName(userId);
        Map<String, String> response = new HashMap<String, String>();
        // String response = "{\"isAvailable\" : #status#}";
        if (user != null) {
            response.put("status", "false");
        } else {
            response.put("status", "true");
        }
        return response;
    }

    private User sendRegistrationEmail1(User user) {
        try {
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            user.setVerificationToken(randomUUIDString);
            user.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
            String link = applicationProperties.getAppUrl() + "/register/" + randomUUIDString + "/verify";
            EmailTemplate emailTemplate = new EmailTemplate();
            String bodyText = MessageFormat.format(applicationProperties.getRegistermailBody(), link,
                    user.getFirstName() + " " + user.getLastName());
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            emailTemplate.setToMailId(toRecipients);
            emailTemplate.setSubject(applicationProperties.getRegisterSubject());
            emailTemplate.setBody(bodyText);
            mailUtil.sendEmail(emailTemplate);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return user;
    }

    public void sendPassWordResetEmail(VerificationToken token, User user) {
        try {
            String link = applicationProperties.getAppURL() + "/reset-password/" + token.getId();
            String bodyText = MessageFormat.format(applicationProperties.getResetMailBody(),
                    user.getFirstName() + " " + user.getLastName(), link);
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            String subject = applicationProperties.getResetSubject();
            sendMail(subject, bodyText, toRecipients);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void sendAddUserEmail(VerificationToken token, User user, String userName) {
        try {
            String link = applicationProperties.getAppURL() + "/user/" + token.getId();
            String bodyText = MessageFormat.format(applicationProperties.getAddWorkspaceUserBody(), userName,
                    token.getWorkspaceId(), link);
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            String subject = applicationProperties.getAddWorkspaceUserSubject();
            sendMail(subject, bodyText, toRecipients);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInviteUserEmail(VerificationToken token, User user, String userName) {
        try {
            String encodedEmail= URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            String link = applicationProperties.getAppURL() + "/user-invited/" + token.getId() +"?email="+encodedEmail;
            String bodyText = MessageFormat.format(applicationProperties.getInviteWorkspaceUserBody(),
                    user.getFirstName() + " " + user.getLastName(), userName, token.getWorkspaceId(), link, link);
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            String subject = applicationProperties.getInviteWorkspaceUserSubject();
            sendMail(subject, bodyText, toRecipients);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public boolean sendRegistrationEmail(VerificationToken token, User user) {
        try {
            String link;
            if(token.getAppType()!= null){
                link = applicationProperties.getAppURL() + "/register/" + token.getId() + "/verify" + "?appType="+ token.getAppType();
            }else {
                link = applicationProperties.getAppURL() + "/register/" + token.getId() + "/verify";
            }
            String bodyText = MessageFormat.format(applicationProperties.getRegistermailBody(), "", link);
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            String subject = applicationProperties.getRegisterSubject();
            sendMail(subject, bodyText, toRecipients);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private void sendMail(String subject, String body, ArrayList<String> toRecipients) {
        try {
            EmailTemplate emailTemplate = new EmailTemplate();
            emailTemplate.setToMailId(toRecipients);
            emailTemplate.setSubject(subject);
            emailTemplate.setBody(body);
            mailUtil.sendEmail(emailTemplate);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public VerificationToken createVerificationToken(String type, String email,String appType) {
        int TOKEN_VALID_DAYS = 7;
        String id = UUID.randomUUID().toString();
        Date created = new Date();
        Date validTill = DateUtils.addDays(new Date(), TOKEN_VALID_DAYS);
        VerificationToken token = new VerificationToken(id, type, created, validTill, email, null,appType);
        return token;
    }

    public String VerifyToken(VerificationToken token) throws ItorixException {
        // boolean isAlive = token.isAlive();
        try {
            if (token.isAlive() == true) {
                if (!token.getUsed()) {
                    if (token.getType().equals("registerUser")) {
                        return activateEmail(token);
                    }
                    if (token.getType().equals("AddUserToWorkspace")) {
                        return addUserToWorkspace(token);
                    }
                    if (token.getType().equals("resetPassword")) {
                        return userPasswordReset(token);
                    }
                } else {
                    throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
                }
            } else {
                resendToken(token);
                return "tokenResent";
            }
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
        }

        return "";
    }

    public void resendToken(VerificationToken token) throws ItorixException {
        User user = findByEmail(token.getUserEmail());
        VerificationToken newToken = createVerificationToken(token.getType(), token.getUserEmail(), null);
        if (token.getType().equals("registerUser")) {
            newToken.setWorkspaceId(token.getWorkspaceId());
            newToken.setUserType(token.getType());
            saveVerificationToken(newToken);
            sendRegistrationEmail(token, user);
        }
        if (token.getType().equals("AddUserToWorkspace")) {
            newToken.setWorkspaceId(token.getWorkspaceId());
            newToken.setUserType(User.LABEL_MEMBER);
            saveVerificationToken(token);
            if (user.isNew() == true)
                sendAddUserEmail(token, user, " ");
            else
                sendInviteUserEmail(token, user, " ");
        }
        if (token.getType().equals("resetPassword")) {
            sendPassWordResetEmail(newToken, user);
            saveVerificationToken(newToken);
        }
    }

    public VerificationToken password(User user,String workspaceId,String appType) throws ItorixException {
        User userByEmail = findByEmail(user.getEmail());
        if (userByEmail != null) {
            VerificationToken token = createVerificationToken("resetPassword", user.getEmail(),appType);
            sendPassWordResetEmail(token, userByEmail,workspaceId);
            saveVerificationToken(token);
            return token;
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1000"), "Identity-1000");
        }
    }

    public void sendPassWordResetEmail(VerificationToken token, User user,String workspaceId) {
        try {
            String link = applicationProperties.getAppURL() + "/reset-password/"+token.getId();
            if(token.getAppType()!=null){
                link += "?appType="+token.getAppType();
            }
            if(workspaceId!=null){
                link += "&workspaceId="+workspaceId;
            }
            String bodyText = MessageFormat.format(applicationProperties.getResetMailBody(),
                    user.getFirstName() + " " + user.getLastName(), link);
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            String subject = applicationProperties.getResetSubject();
            sendMail(subject, bodyText, toRecipients);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void resendToken(UserInfo userInfo,String appType) throws ItorixException {
        if (userInfo.allowCreateToken() != true)
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1038"), "Identity-1038");
        if (userInfo.getType().equals("password-reset")) {
            User user = new User();
            user.setEmail(userInfo.getEmail());
            password(user,userInfo.getWorkspaceId(),appType);
        } else if (userInfo.getType().equals("register")) {
            User user = findByEmail(userInfo.getEmail());
            VerificationToken token = createVerificationToken("registerUser", user.getEmail(),null);
            saveVerificationToken(token);
            sendRegistrationEmail(token, user);
        }
    }


    public void resendInvite(UserInfo userInfo) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
        boolean isNewUser = false;
        User user = findByUserEmail(userInfo.getEmail());
        if (user.getLoginId() == null) {
            isNewUser = true;
        }
        VerificationToken token = createVerificationToken("AddUserToWorkspace", user.getEmail(),null);
        token.setWorkspaceId(workspace.getName());
        token.setUserType(User.LABEL_MEMBER);
        saveVerificationToken(token);
        if (isNewUser)
            sendAddUserEmail(token, user, userSessionToken.getUsername());
        else
            sendInviteUserEmail(token, user, userSessionToken.getUsername());
    }

    private String activateEmail(VerificationToken token) {
        User user = findByEmail(token.getUserEmail());
        user.setUserStatus("active");
        saveUser(user);
        //token.setUsed(true);
        saveVerificationToken(token);
        return "activated";
    }

    private String activateUser(VerificationToken token) {
        User user = findByEmail(token.getUserEmail());
        List<UserWorkspace> workspaces = user.getWorkspaces();
        for (UserWorkspace workspace : workspaces)
            if (workspace.getWorkspace().getName().equals(token.getWorkspaceId())) {
                workspace.getWorkspace().setStatus("active");
                workspace.setAcceptInvite(true);
                workspace.setActive(true);
            }
        user.setWorkspaces(workspaces);
        saveUser(user);
        Workspace workspace = getWorkspace(token.getWorkspaceId());
        workspace.setStatus("active");
        workspace.setMts(System.currentTimeMillis());
        masterMongoTemplate.save(workspace);
        token.setUsed(true);
        saveVerificationToken(token);
        return "activated";
    }

    private String addUserToWorkspace(VerificationToken token) {
        User user = findByUserEmail(token.getUserEmail());
        if (user.getLoginId() != null && user.getPassword() != null) {
            List<UserWorkspace> workspaces = user.getWorkspaces();
            for (UserWorkspace workspace : workspaces)
                if (workspace.getWorkspace().getName().equals(token.getWorkspaceId()))
                    if (workspace.getWorkspace().getName().equals(token.getWorkspaceId())) {
                        workspace.getWorkspace().setStatus("active");
                        workspace.setAcceptInvite(true);
                        workspace.setActive(true);
                    }
            user.setWorkspaces(workspaces);
            saveUser(user);
            //token.setUsed(true);
            //saveVerificationToken(token);
            return "WorkspaceAdded";
        } else {
            return "registrationRequired";
        }
    }

    private String userPasswordReset(VerificationToken token) {
        User user = findByEmail(token.getUserEmail());
        if (user.getLoginId() != null && user.getPassword() != null) {
            user.setUserStatus("inResetPassword");
            saveUser(user);
            return "resetPassword";
        } else {
            return "";
        }
    }

    public void saveVerificationToken(VerificationToken token) {
        masterMongoTemplate.save(token);
    }

    public VerificationToken getVerificationToken(String tokenId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("id").is(tokenId)));
        VerificationToken token = masterMongoTemplate.findOne(query, VerificationToken.class);
        return token;
    }

    public void cancelSubscription(CancelSubscriptions subscription) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (loginUser.isWorkspaceAdmin(userSessionToken.getWorkspaceId())) {
            subscription.setUserName(userSessionToken.getUsername());
            subscription.setUserEmail(userSessionToken.getEmail());
            masterMongoTemplate.save(subscription);
            Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
            workspace.setStatus("cancel");
            workspace.setMts(System.currentTimeMillis());
            masterMongoTemplate.save(workspace);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1042"), "Identity-1042");
        }
    }

    public Object listUsers(String filterbynames) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        Users allUsers = new Users();
        List<String> userName = new ArrayList<String>();
        List<UserDetails> userDetails = new ArrayList<UserDetails>();
        List<User> dbUsers = findUsersByWorkspace(workspaceId);
        List<User> dbActiveUsers = dbUsers;
        JSONArray namesList = new JSONArray();
        JSONObject userNamesList = new JSONObject();

        if (filterbynames != null && "true".equalsIgnoreCase(filterbynames)) {
            for (int i = 0; i < dbActiveUsers.size(); i++) {
                User user = dbUsers.get(i);
                if (user != null && user.getFirstName() != null) {
                    JSONObject userNames = new JSONObject();
                    String lastName = user.getLastName() == null ? "" : user.getLastName();
                    userNames.put("displayName", user.getFirstName() + " " + lastName);
                    userNames.put("userId", user.getId());
                    userNames.put("emailId", user.getEmail());
                    namesList.add(userNames);
                }
            }
            userNamesList.put("users", namesList);
            return userNamesList;

        } else if (filterbynames != null && "names".equalsIgnoreCase(filterbynames)) {
            for (int i = 0; i < dbActiveUsers.size(); i++) {
                User user = dbUsers.get(i);
                userName.add(user.getFirstName() + " " + user.getLastName());
            }
            JSONObject userNames = new JSONObject();
            userNames.put("username", userName);
            return userNames;
        } else {
            int userSize = dbUsers.size();
            Set<String> loginUserTeamNames = getUserTeamNames(getTeams(loginUser.getId(), false));
            for (int i = 0; i < userSize; i++) {
                boolean canAdd = false;
                User user = dbUsers.get(i);
                UserDetails requiredDetails = new UserDetails();
                requiredDetails.setUserId(user.getId());
                requiredDetails.setFirstName(user.getFirstName());
                requiredDetails.setLastName(user.getLastName());
                requiredDetails.setEmail(user.getEmail());
                requiredDetails.setStatus(user.getUserWorkspace(workspaceId).getActive() ? "active" : "locked");
                List<UserWorkspace> workspaces = user.getWorkspaces();
                if (workspaces != null) {
                    List<UserInfo> userworkspaces = new ArrayList<>();
                    for (UserWorkspace workspace : workspaces) {
                        if (workspace.getName().equals(workspaceId)) {
                            if (workspace.getAcceptInvite() || userSize == 1) {
                                canAdd = true;
                                UserInfo userworkspace = new UserInfo();
                                userworkspace.setName(workspace.getWorkspace().getName());
                                userworkspace.setRoles(workspace.getRoles());
                                userworkspace.setUserType(workspace.getUserType());
                                List<SwaggerTeam> userTeams = getTeams(user.getId(), true);
                                if (loginUser.isWorkspaceAdmin(userSessionToken.getWorkspaceId()) || user.getId().equals(loginUser.getId())) {
                                    userworkspace.setTeams(userTeams);
                                    userworkspaces.add(userworkspace);
                                } else if (CollectionUtils.containsAny(getUserTeamNames(userTeams), loginUserTeamNames)) {
                                    List<SwaggerTeam> userTeamsList = new ArrayList<>();
                                    userTeams.forEach(x -> {
                                            if(loginUserTeamNames.contains(x.getName())) {
                                                userTeamsList.add(x);
                                            }
                                        }
                                    );
                                    userworkspace.setTeams(userTeamsList);
                                    userworkspaces.add(userworkspace);
                                } else {
                                    canAdd = false;
                                }
                            }
                        }
                    }
                    requiredDetails.setWorkspaces(userworkspaces);
                }
                if (canAdd)
                    userDetails.add(requiredDetails);
            }
            allUsers.setUsers(userDetails);
            return allUsers;
        }
    }

    public Object listUsers() throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        logger.debug("workspace : {}", workspaceId);
        User loginUser = findUserById(userSessionToken.getUserId());
        Users allUsers = new Users();
        List<UserDetails> userDetails = new ArrayList<UserDetails>();
        List<User> dbUsers = findUsersByWorkspace(workspaceId);
        int userSize = dbUsers.size();
        for (int i = 0; i < userSize; i++) {
            boolean canAdd = false;
            User user = dbUsers.get(i);
            UserDetails requiredDetails = new UserDetails();
            requiredDetails.setUserId(user.getId());
            requiredDetails.setFirstName(user.getFirstName());
            requiredDetails.setLastName(user.getLastName());
            requiredDetails.setEmail(user.getEmail());
            // requiredDetails.setStatus(user.getUserStatus());
            requiredDetails.setStatus(user.getUserWorkspace(workspaceId).getActive() ? "active" : "locked");
            requiredDetails.setCts(user.getCts() != null ? user.getCts() : user.getMts() != null ? user.getMts() : System.currentTimeMillis());
            requiredDetails.setCreatedUserName(user.getCreatedUserName());
            List<UserWorkspace> workspaces = user.getWorkspaces();
            if (workspaces != null) {
                List<UserInfo> userworkspaces = new ArrayList<>();
                for (UserWorkspace workspace : workspaces) {
                    if (workspace.getWorkspace().getName().equals(workspaceId)) {
                        if (!workspace.getAcceptInvite()) {
                            canAdd = true;
                            UserInfo userworkspace = new UserInfo();
                            userworkspace.setName(workspace.getWorkspace().getName());
                            userworkspace.setRoles(workspace.getRoles());
                            userworkspace.setUserType(workspace.getUserType());
                            requiredDetails.setCts(workspace.getCts());
                            requiredDetails.setCreatedUserName(workspace.getCreatedUserName());
                            userworkspaces.add(userworkspace);
                        }
                    }
                }
                requiredDetails.setWorkspaces(userworkspaces);
            }
            if (canAdd)
                userDetails.add(requiredDetails);
        }
        allUsers.setUsers(userDetails);
        return allUsers;
    }

    public void lockUser(String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (!loginUser.isWorkspaceAdmin(workspaceId)) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
        }
        User user = findUserById(userId);
        if (user.getUserWorkspace(workspaceId) != null) {
            user.getUserWorkspace(workspaceId).setActive(false);
            saveUser(user);
        }
    }

    public void unLockUser(String userId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        String workspaceId = userSessionToken.getWorkspaceId();
        User loginUser = findUserById(userSessionToken.getUserId());
        if (!loginUser.isWorkspaceAdmin(workspaceId)) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1043"), "Identity-1043");
        }
        User user = findUserById(userId);
        if (user.getUserWorkspace(workspaceId) != null) {
            user.setUserStatus(UserStatus.ACTIVE.getValue());
            user.setUserCount(0);
            user.getUserWorkspace(workspaceId).setActive(true);
            saveUser(user);
        }
    }

    public User findUserById(String userId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("id").is(userId)));
        User user = masterMongoTemplate.findOne(query, User.class);
        return user;
    }

    public List<User> findUsersByWorkspace(String workspaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("workspaces.workspace.name").is(workspaceId)))
                .with(Sort.by(Direction.DESC, "mts"));
        List<User> users = masterMongoTemplate.find(query, User.class);
        return users;
    }

    public Workspace getWorkspace(String workspaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workspaceId)));
        return masterMongoTemplate.findOne(query, Workspace.class);
    }

    public String getHashedValue(String password){
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is("salt"));
        MetaData metadata = masterMongoTemplate.findOne(query,MetaData.class);
        if(metadata!=null){
            password +=metadata.getMetadata();
        }
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            password = hexString.toString();
        } catch (Exception e) {
            logger.error("Exception occurred while hashing password");
            return password;
        }
        return password;
    }

    public User saveUser(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        String password = null;
        try{
            if(user.getPassword()!=null){
                password = rsaEncryption.decryptText(user.getPassword());
            }
        } catch (Exception e) {
            logger.error("Cannot decrypt value : "+user.getPassword());
        }
        if(password!=null){
            user.setPassword(getHashedValue(password));
            user.setPasswordLastChangedDate(System.currentTimeMillis());
        }
        return baseRepository.save(user, masterMongoTemplate);
    }
    public User findByEmailUserName(String userId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(userId),
                Criteria.where(User.LABEL_LOGINID).is(userId)));
        return masterMongoTemplate.findOne(query, User.class);
    }

    public User findByUserEmail(String userId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(userId)));
        return masterMongoTemplate.findOne(query, User.class);
    }

    public User findByLogin(String userId) {
        return findByEmailUserName(userId);
    }

    public User findByEmailUserName(String email, String userName) {
        Query query = new Query(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(email),
                Criteria.where(User.LABEL_LOGINID).is(userName)));
        User user = masterMongoTemplate.findOne(query, User.class);
        return user;
    }

    public User findByEmailAndPassword(String email, String password) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where(User.LABEL_EMAIL).is(email),
                Criteria.where(User.LABEL_PASSWORD).is(password)));
        return masterMongoTemplate.findOne(query, User.class);
    }

    public User findByEmail(String email) {
        Query query = new Query();
        query.addCriteria(Criteria.where(User.LABEL_EMAIL).is(email));
        return masterMongoTemplate.findOne(query, User.class);
    }

    public UserSession findUserSession(String sessionId) {
        Query query = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(sessionId)));
        return masterMongoTemplate.findOne(query, UserSession.class);
        // return userSessionRepository.findOne(sessionId);
    }

    private boolean isDomainAllowed(User user) {
        boolean isAllowed = false;
        if (user.getEmail() != null) {
            return isDomainAllowed(user.getEmail());
        }
        return isAllowed;
    }

    private boolean isDomainAllowed(String eamil) {
        boolean isAllowed = false;
        if (eamil != null) {
            isAllowed = true;
            String domainId = eamil.split("@")[1];
            String blockedDomainList = applicationProperties.getBlockedMailDomains();
            String[] blockedDomains = blockedDomainList.split(",");
            for (String domain : blockedDomains) {
                if (domain.equalsIgnoreCase(domainId)) {
                    isAllowed = false;
                    break;
                }
            }
        }
        return isAllowed;
    }

    public void createUserDomains(UserDomains userDomains) throws ItorixException {
        UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
        if (dbUserDomains == null) {
            userDomains.setName(UserDomains.NAME);
            baseRepository.save(userDomains);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("USER_012"), "USER_012");
        }
    }

    public void updateUserDomains(UserDomains userDomains) throws ItorixException {
        UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
        if (dbUserDomains != null) {
            dbUserDomains.setDomains(userDomains.getDomains());
            baseRepository.save(dbUserDomains);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("USER_013"), "USER_013");
        }
    }

    public Object getUserDomains() throws ItorixException {
        JSONObject obj = new JSONObject();
        UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
        obj.put("domains", dbUserDomains.getDomains());
        return obj;
    }

    public void resendUserToken(ResetUserToken userToken) throws ItorixException {
        User user = findByEmail(userToken.getEmailID());
        if (user != null) {
            if (userToken.getType().equalsIgnoreCase("register")) {
                user.setUserStatus(UserStatus.getStatus(UserStatus.PENDING));
                user.setUserCount(0);
                // user = mailUtil.sendEmailRegistrationLink(user);
                user = sendPassWordResetEmail(user);
            } else if (userToken.getType().equalsIgnoreCase("password-reset")
                    && user.getUserStatus().equalsIgnoreCase(UserStatus.getStatus(UserStatus.ACTIVE))) {
                user = sendPassWordResetEmail(user);
                user.setUserStatus(UserStatus.getStatus(UserStatus.INRESETPASSWORD));
            }
            user = baseRepository.save(user);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1000"), "Identity-1000");
        }
    }

    public User sendPassWordResetEmail(User user) {

        try {
            ArrayList<String> toRecipients = new ArrayList<String>();
            toRecipients.add(user.getEmail());
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            user.setVerificationToken(randomUUIDString);
            user.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
            String link = "http://" + applicationProperties.getVerificationLinkHostName()
                    + applicationProperties.getVerificationLinkPort() + "/v1/user/resetpassword?verificationToken="
                    + randomUUIDString + "&emailId=" + user.getEmail() + "";
            String bodyText = MessageFormat.format(applicationProperties.getResetMailBody(), link,
                    user.getFirstName() + " " + user.getLastName());
            EmailTemplate emailTemplate = new EmailTemplate();
            emailTemplate.setSubject(applicationProperties.getResetSubject());
            emailTemplate.setBody(bodyText);
            emailTemplate.setToMailId(toRecipients);
            mailUtil.sendEmail(emailTemplate);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return user;
    }

    public void removeUserSessions() {
        List<UserSession> dbUserSessions = baseRepository.findAll(UserSession.class);
        for (UserSession userSession : dbUserSessions) {
            if (System.currentTimeMillis() - userSession.getLoginTimestamp() >= Constants.MILLIS_PER_DAY) {
                baseRepository.delete(userSession.getId(), UserSession.class);
            }
        }
    }
    public void removeUserSession(String userId) {
        long timeStamp = System.currentTimeMillis();
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("loginTimestamp").gte(timeStamp-MILLIS_PER_DAY).lte(timeStamp));
        List<UserSession> dbUserSessions = masterMongoTemplate.find(query, UserSession.class);
        for (UserSession userSession : dbUserSessions) {
            masterMongoTemplate.remove(userSession);
        }
    }
    public Object getUserRoles(User user) throws ItorixException {
        User userByEmail = findByEmail(user.getEmail());
        // List<String> userRoleslist;
        JSONObject userRoles = new JSONObject();
        if (userByEmail != null) {

        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }

        return userRoles;
    }

    public Object verifyResetEmailHash(User user) {
        Date CurrentDate = new Date();
        User userByEmail = findByEmail(user.getEmail());
        if (userByEmail.getVerificationToken().equalsIgnoreCase(user.getVerificationToken())
                && userByEmail.getTokenValidUpto() != null
                && CurrentDate.compareTo(userByEmail.getTokenValidUpto()) < 0) {
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            userByEmail.setVerificationToken(randomUUIDString);
            userByEmail.setVerificationStatus("resetSuccessful");
            userByEmail.setUserStatus(UserStatus.getStatus(UserStatus.ACTIVE));
        } else {
            if (userByEmail.getUserCount() <= 3 && CurrentDate.compareTo(userByEmail.getTokenValidUpto()) < 0) {
                // userByEmail = mailUtil.sendResetPasswordLink(userByEmail);
                userByEmail = sendPassWordResetEmail(userByEmail);
                userByEmail.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
                userByEmail.setDisplayMessage(Constants.USER_VERIFICATION_LINK);
                userByEmail.setUserCount(userByEmail.getUserCount() + 1);
                userByEmail.setVerificationStatus("resendVerification");
            } else if (CurrentDate.compareTo(userByEmail.getTokenValidUpto()) > 0
                    && !(userByEmail.getUserCount() > 3)) {
                userByEmail.setDisplayMessage(Constants.USER_VERIFICATION_LINK);
                userByEmail = sendPassWordResetEmail(userByEmail);
                // userByEmail = mailUtil.sendResetPasswordLink(userByEmail);
                userByEmail.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
                userByEmail.setVerificationStatus("resendVerification");
            } else if (userByEmail.getUserCount() > 3) {
                userByEmail.setUserStatus(UserStatus.getStatus(UserStatus.LOCKED));
                userByEmail.setDisplayMessage(Constants.USER_BLOCKED_DISPLAY_MESSAGE);
                userByEmail.setReason(Constants.USER_BLOCKED_REASON_MESSAGE);
                userByEmail.setVerificationStatus("blockUser");
            }
        }
        userByEmail = baseRepository.save(userByEmail, masterMongoTemplate);
        return userByEmail;
    }

    public User updatePassword(User user) throws ItorixException {
        User userByEmail = findByEmail(user.getEmail());
        if (userByEmail != null && userByEmail.getVerificationToken().equals(user.getVerificationToken())) {
            userByEmail.setPassword(user.getPassword());
            userByEmail.setVerificationToken("");
            userByEmail.setUserStatus(UserStatus.getStatus(UserStatus.ACTIVE));
            userByEmail = saveUser(userByEmail);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
        return userByEmail;
    }

    public Object updateUserStatus(String email, String interactionid) throws ItorixException {
        logger.debug("UserService.updateUserStatus : interactionid=" + interactionid);

        if (email != null) {
            try {
                List<String> toMailId = new ArrayList<>();
                toMailId.add(email);
                User userDetails = findByEmail(email);
                EmailTemplate template = new EmailTemplate();
                template.setSubject(applicationProperties.getUserActivationMailSubject());
                template.setToMailId(toMailId);
                String messageBody = MessageFormat.format(applicationProperties.getUserActivationMailBody(),
                        userDetails.getFirstName() + " " + userDetails.getLastName());

                // template.setBody(applicationProperties.getUserActivationMailBody());
                template.setBody(messageBody);
                mailUtil.sendEmail(template);
                userDetails.setUserStatus(UserStatus.getStatus(UserStatus.ACTIVE));
                baseRepository.save(userDetails, masterMongoTemplate);

            } catch (MessagingException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
        return "";
    }

    public Object getUsers(String filterbynames) {

        Users allUsers = new Users();
        List<String> userName = new ArrayList<String>();
        List<UserDetails> userDetails = new ArrayList<UserDetails>();

        List<User> dbUsers = findAll();
        List<User> dbActiveUsers = dbUsers; // findAllActiveUsers();

        JSONArray namesList = new JSONArray();
        JSONObject userNamesList = new JSONObject();

        if (filterbynames != null && "true".equalsIgnoreCase(filterbynames)) {
            for (int i = 0; i < dbActiveUsers.size(); i++) {
                User user = dbUsers.get(i);
                JSONObject userNames = new JSONObject();
                userNames.put("displayName", user.getFirstName() + " " + user.getLastName());
                userNames.put("userId", user.getId());
                userNames.put("emailId", user.getEmail());
                namesList.add(userNames);
            }
            userNamesList.put("users", namesList);
            return userNamesList;

        } else if (filterbynames != null && "names".equalsIgnoreCase(filterbynames)) {
            for (int i = 0; i < dbActiveUsers.size(); i++) {

                User user = dbUsers.get(i);
                userName.add(user.getFirstName() + " " + user.getLastName());
            }
            JSONObject userNames = new JSONObject();
            userNames.put("username", userName);

            return userNames;
        } else {
            for (int i = 0; i < dbUsers.size(); i++) {
                User user = dbUsers.get(i);
                UserDetails requiredDetails = new UserDetails();
                requiredDetails.setFirstName(user.getFirstName());
                requiredDetails.setLastName(user.getLastName());
                requiredDetails.setLoginId(user.getLoginId());
                requiredDetails.setEmail(user.getEmail());
                // requiredDetails.setRoles(user.getRoles());
                // requiredDetails.setType(user.getType());
                requiredDetails.setStatus(user.getUserStatus());
                userDetails.add(requiredDetails);
            }
            allUsers.setUsers(userDetails);
            return allUsers;
        }
    }

    public List<User> findAll() {
        return masterMongoTemplate.findAll(User.class);
    }

    public Object logOut(String jsessionid) throws ItorixException {
        UserSession userSessionToken = findUserSession(jsessionid); // userSessionRepository.findOne(jsessionid);
        if (userSessionToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1039"), "Identity-1039");
        } else {
            Query query = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(jsessionid)));
            // masterMongoTemplate.remove(query, UserSession.class);
            masterMongoTemplate.remove(userSessionToken);
        }
        return "";
    }

    public Object getActivityLogDetails(String jsessionid, String timeRange, String userId, int offset, int pageSize)
            throws ItorixException, ParseException {
        List<ActivityLog> list;
        ActivitylogResponse response = new ActivitylogResponse();
        if (timeRange.contains("~")) {
            Long counter = 1L;
            if (userId != null) {
                list = findActivityByTimeRangeByUser(timeRange, userId, offset, pageSize);
                counter = findActivityByTimeRangeByUserCounter(timeRange, userId);
            } else {
                list = findActivityByTimeRange(timeRange, offset, pageSize);
                counter = findActivityByTimeRangeCounter(timeRange);
            }
            for (ActivityLog log : list) {
                log.setCts(null);
                log.setMts(null);
                log.setCreatedBy(null);
                log.setModifiedBy(null);
                log.setCreatedUserName(null);
                log.setModifiedUserName(null);
            }
            Pagination pagination = new Pagination();
            pagination.setOffset(offset);
            pagination.setTotal(counter);
            pagination.setPageSize(pageSize);
            response.setData(list);
            response.setPagination(pagination);
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("USER_008"), "USER_008");
        }
        return response;
    }

    public List<ActivityLog> findActivityByTimeRange(String timeRange, int offset, int pageSize) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        String timeRanges[] = timeRange.split("~");
        Long StartTime = Long.parseLong(timeRanges[0]);
        Long endDateTime = Long.parseLong(timeRanges[1]);
        Query query = new Query(Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime))
                .with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return baseRepository.find(query, ActivityLog.class);
    }

    public Long findActivityByTimeRangeCounter(String timeRange) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Query query = new Query();
        String timeRanges[] = timeRange.split("~");
        Long StartTime = Long.parseLong(timeRanges[0]);
        Long endDateTime = Long.parseLong(timeRanges[1]);
        query.addCriteria(Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime));
        return mongoTemplate.count(query, ActivityLog.class);
    }

    public List<ActivityLog> findActivityByTimeRangeByUser(String timeRange, String userId, int offset, int pageSize)
            throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        String timeRanges[] = timeRange.split("~");
        Long StartTime = Long.parseLong(timeRanges[0]);
        Long endDateTime = Long.parseLong(timeRanges[1]);
        long currentDate = DateUtil.getEndOfDay(new Date()).getTime();
        if (endDateTime > currentDate)
            endDateTime = currentDate;
        Query query = new Query(Criteria.where(ActivityLog.USER_ID).is(userId).and(ActivityLog.LAST_CHANGED_AT)
                .gte(StartTime).lte(endDateTime)).with(Sort.by(Direction.DESC, "_id"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        return baseRepository.find(query, ActivityLog.class);
    }

    public Long findActivityByTimeRangeByUserCounter(String timeRange, String userId) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Query query = new Query();
        String timeRanges[] = timeRange.split("~");
        Long StartTime = Long.parseLong(timeRanges[0]);
        Long endDateTime = Long.parseLong(timeRanges[1]);
        query.addCriteria(new Criteria().andOperator(Criteria.where(ActivityLog.USER_ID).is(userId),
                Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime)));
        return mongoTemplate.count(query, ActivityLog.class);
    }

    public User getUserById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        User user = masterMongoTemplate.findOne(query, User.class);
        return user;
    }

    public UserSession getUserSessionDetails(String sessionId, String appId) {
        UserSession userSessionToken = (sessionId != null) ? findUserSession(sessionId) : null;
        if (appId != null && appId.equalsIgnoreCase("portal")) {
            UserSession session = new UserSession();
            session.setEmail(userSessionToken.getEmail());
            session.setUsername(userSessionToken.getUsername());
            session.setFirstName(userSessionToken.getFirstName());
            session.setLastName(userSessionToken.getLastName());
            session.setLoginTimestamp(userSessionToken.getLoginTimestamp());
            return session;
        }
        return userSessionToken;
    }

    public boolean validateSession(String sessionId) throws ItorixException {
        UserSession userSessionToken = (sessionId != null) ? findUserSession(sessionId) : null;
        if (userSessionToken == null)
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1039"), "Identity-1039");
        return (userSessionToken == null)
                ? false
                : (System.currentTimeMillis() - userSessionToken.getLoginTimestamp() <= Constants.MILLIS_PER_DAY)
                ? true
                : false;
    }

    public User getUserDetailsFromSessionID(String sessionId) {
        UserSession userSessionToken = masterMongoTemplate.findById(sessionId, UserSession.class);
        User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
        return user;
    }

    public List<String> getUserRoles(String sessionId) {
        UserSession userSessionToken = masterMongoTemplate.findById(sessionId, UserSession.class);
        User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
        List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();
        return roles;
    }

    public UIMetadata createUIUXMetadata(UIMetadata metadata) {
        UIMetadata uIMetadata = getUIUXMetadata(metadata.getQuery());
        // if(uIMetadata != null){
        // uIMetadata.setMetadata(metadata.getMetadata());
        // uIMetadata.setQuery(metadata.getQuery());
        // Query query = new
        // Query(Criteria.where("query").is(metadata.getQuery()));
        // DBObject dbDoc = new BasicDBObject();
        // masterMongoTemplate.getConverter().write(uIMetadata, dbDoc);
        // Update update = Update.fromDBObject(dbDoc, "_id");
        // masterMongoTemplate.updateFirst(query, update, UIMetadata.class);
        // }
        // else{
        // masterMongoTemplate.save(metadata);
        // uIMetadata = getUIUXMetadata(metadata.getQuery());
        // }
        return uIMetadata;
    }

    public UIMetadata getUIUXMetadata(String query) {
        Query dBquery = new Query(Criteria.where("query").is(query));
        List<UIMetadata> UIMetadata = masterMongoTemplate.find(dBquery, UIMetadata.class);
        if (UIMetadata != null && UIMetadata.size() > 0)
            return UIMetadata.get(0);
        else
            return null;
    }

    public void createPlanPermissions(Plan plan) throws ItorixException {
        if (validatePermission(plan.getUiPermissions())) {
            Query dBquery = new Query(Criteria.where("planId").is(plan.getPlanId()));
            Plan dbPlan = masterMongoTemplate.findOne(dBquery, Plan.class);
            if (dbPlan == null)
                masterMongoTemplate.save(plan);
            else {
                dbPlan.setUiPermissions(plan.getUiPermissions());
                masterMongoTemplate.save(dbPlan);
            }
        } else {
            String message = "Invalid request data! Invalid permissions ";
            throw new ItorixException(message, "Identity-1007");
        }
    }

    private boolean validatePermission(String permission) {
        if (permission == null || permission.trim() == "") {
            return false;
        } else {
            try {
                new ObjectMapper().readTree(permission);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public String getPlanPermissions() throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
        String workspaceId = workspace.getPlanId();
        if (workspaceId.equalsIgnoreCase("growth")) {
            workspaceId = "enterprise";
        } else if(workspaceId.equalsIgnoreCase("starter")){
            workspaceId = "basic";
        }
        Query dBquery = new Query(Criteria.where("planId").is(workspaceId));
        Plan dbPlan = masterMongoTemplate.findOne(dBquery, Plan.class);
        if (dbPlan == null || dbPlan.getUiPermissions() == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1041"), "Identity-1041");
        }
        return dbPlan.getUiPermissions();
    }

    public Map<String, String> getRoleMapper() throws ItorixException {
        try {
            UIMetadata uiuxMetadata = getUIUXMetadata(UIMetadata.ROLE_MAPPER);
            return uiuxMetadata == null
                    ? null
                    : new ObjectMapper().readValue(uiuxMetadata.getMetadata(),
                    new TypeReference<Map<String, String>>() {
                    });
        } catch (IOException e) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1041"), "Identity-1041");
        }
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

    public void createOrUpdateDBProperties(DBConfig dbConfig) {

        masterMongoTemplate.save(dbConfig);
    }

    public List<DBConfig> getDBProperties() {
        return masterMongoTemplate.findAll(DBConfig.class);
    }

    private ApplicationProperties populateDBApplicationProperties(Map<String, String> map) {
        if (map.get("apigee.host") != null)
            applicationProperties.setApigeeHost(map.get("apigee.host"));
        if (map.get("app.mailutil.userName") != null)
            applicationProperties.setUserName(map.get("app.mailutil.userName"));
        if (map.get("itorix.core.gocd.proxy.scm.username") != null)
            applicationProperties.setProxyScmUserName(map.get("itorix.core.gocd.proxy.scm.username"));
        applicationProperties.setProxyScmPassword(map.get("itorix.core.gocd.proxy.scm.password"));
        applicationProperties.setBuildScmType(map.get("itorix.core.gocd.build.scm.type"));
        applicationProperties.setBuildScmUserName(map.get("itorix.core.gocd.build.scm.username"));
        applicationProperties.setBuildScmPassword(map.get("itorix.core.gocd.build.scm.password"));
        applicationProperties.setBuildScmUrl(map.get("itorix.core.gocd.build.scm.url"));
        applicationProperties.setBuildScmBranch(map.get("itorix.core.gocd.build.scm.branch"));
        applicationProperties.setApigeeUserName(map.get("itorix.core.gocd.apigee.username"));
        applicationProperties.setApigeePassword(map.get("itorix.core.gocd.apigee.password"));
        applicationProperties.setBuildScmUserType(map.get("itorix.core.gocd.build.scm.userType"));
        applicationProperties.setBuildScmToken(map.get("itorix.core.gocd.build.scm.token"));
        applicationProperties.setProxyScmUserType(map.get("itorix.core.gocd.proxy.scm.userType"));
        applicationProperties.setProxyScmToken(map.get("itorix.core.gocd.proxy.scm.token"));
        return applicationProperties;
    }

    public void createPlanPermissionsV2(PlanV2 plan) throws ItorixException {
        if (validatePermission(plan.getUiPermissions())) {
            Query query = new Query(Criteria.where("planId").is(plan.getPlanId()));
            PlanV2 planV2 = masterMongoTemplate.findOne(query, PlanV2.class);
            if (planV2 == null)
                masterMongoTemplate.save(plan);
            else {
                planV2.setUiPermissions(plan.getUiPermissions());
                masterMongoTemplate.save(planV2);
            }
        } else {
            String message = "Invalid request data! Invalid permissions ";
            throw new ItorixException(message, "Identity-1007");
        }
    }

    public String getPlanPermissionsV2(String planId) throws ItorixException {
        UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
        Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
        if (planId == null) {
            planId = workspace.getPlanId();
        }
        Query query = new Query(Criteria.where("planId").is(planId));

        PlanV2 planV2 = masterMongoTemplate.findOne(query, PlanV2.class);
        if (planV2 == null || planV2.getUiPermissions() == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1041"), "Identity-1041");
        }
        return planV2.getUiPermissions();
    }

    public void createMenu(Menu menu) throws ItorixException {
        masterMongoTemplate.save(menu);
    }

    public JsonNode getMenu() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Menu menu = masterMongoTemplate.findById("menu",Menu.class);
        if(menu != null){
            return objectMapper.readTree(menu.getMenus());
        }
        return null;
    }

    public void createRolesMetaData(String metadataStr) {
        Query query = new Query().addCriteria(Criteria.where("key").is("roles"));
        MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
        if (metaData != null) {
            logger.debug("Updating masterMongoTemplate");
            Update update = new Update();
            update.set("metadata", metadataStr);
            masterMongoTemplate.updateFirst(query, update, MetaData.class);
        } else
            masterMongoTemplate.save(new MetaData("roles", metadataStr));
    }

    public List<String> getRoles() {
        List<String> roles = new ArrayList<String>();
        Query query = new Query().addCriteria(Criteria.where("key").is("roles"));
        MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
        if (metaData != null) {
            JSONObject jsonObject = JSONObject.fromObject(metaData.getMetadata());
            JSONArray jsonArray = jsonObject.getJSONArray("roles");
            for(Object role : jsonArray){
                roles.add(role.toString());
            }
        }else {
            roles.add("Developer");
            roles.add("Admin");
            roles.add("Portal");
            roles.add("Analyst");
            roles.add("Project-Admin");
            roles.add("Site-Admin");
            roles.add("Operation");
            roles.add("Test");
        }
        return roles;
    }

    private Set<String> getUserTeamNames(List<SwaggerTeam> swaggerTeams) {
        Set<String> teamNames = new HashSet<>();
        if (!swaggerTeams.isEmpty()) {
            swaggerTeams.forEach(x -> teamNames.add(x.getName()));
        }
        return teamNames;
    }

    public UpdateResult updateSocialLoginEnabledStatus(String providers){
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is("social-logins"));
        Update update = new Update();
        update.set("metadata",providers);
        return masterMongoTemplate.upsert(query,update,MetaData.class);
    }

    public ResponseEntity<?> syncClientData() {
        ClientData clientData = new ClientData();
        try {
            List<Workspace> allWorkspaces = masterMongoTemplate.findAll(Workspace.class);
            if (allWorkspaces.size() > 0) {
                allWorkspaces.forEach(ws -> {
                    try {
                        clientData.setClientSecret(ws.getKey());
                    } catch (Exception e) {
                        logger.error("Exception occurred while encrypting workspace key");
                    }
                    clientData.setId(ws.getName());
                    clientData.setDisabled(false);
                    masterMongoTemplate.save(clientData);
                });
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error while syncing client data",
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Synced client data successfully",HttpStatus.OK);

    }

    public User updatePasswordWithoutToken(User user) throws ItorixException {
        User userByEmail = findByEmail(user.getEmail());
        if (userByEmail != null) {
            boolean isValid = validateUser(user, userByEmail);
            if(!isValid){
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Identity-1051"), "Users old password is incorrect!"), "Identity-1051");
            }
            userByEmail.setPassword(user.getPassword());
            userByEmail.setUserStatus("active");
            userByEmail.setPasswordLastChangedDate(System.currentTimeMillis());
            userByEmail = saveUser(userByEmail);
            return userByEmail;
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1023"), "Identity-1023");
        }
    }

    public boolean validateUser(User user, User userByEmail) throws ItorixException {
        String oldPassword = null;
        String originalPassword = userByEmail.getPassword();
        if (user.getOldPassword() != null) {
            try {
                oldPassword = rsaEncryption.decryptText(user.getOldPassword());
            } catch (Exception ex) {
                logger.error("Enable to decrypt the password for user - {}", user.getEmail());
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Identity-1051"), "Unable to verify users old password!"), "Identity-1051");
            }
        } else {
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Identity-1051"), "old password is required to reset password"), "Identity-1051");
        }
        String hashedOldPassword = getHashedValue(oldPassword);

        if(hashedOldPassword.equals(originalPassword)){
            return true;
        } else {
            if (originalPassword != null) {
                try {
                    originalPassword = rsaEncryption.decryptText(originalPassword);
                } catch (Exception ex) {
                    logger.error("Enable to decrypt the password for user - {}", user.getEmail());
                }
            } else {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Identity-1051"), "Invalid user details"), "Identity-1051");
            }
            if(originalPassword.equals(oldPassword)){
                return true;
            }
        }
        return false;
    }

    public void validateUserFields(UserInfo userInfo) throws ItorixException {
            String nameRegexPattern = "^[a-zA-Z]+$";
            String loginIdPattern = "^[a-zA-Z\\d.-]+$";
            String workspaceIdPattern = "^[a-z]++(?:-[a-z]++)*+$";
            String emailPattern = "^[A-Za-z\\d+_.@()-]+$";
            //String specialCharacters ="^[&,:;=?#|'<>^*()%!]+$";

            Pattern pattern = Pattern.compile(nameRegexPattern);
            //Pattern specialCharacterPattern = Pattern.compile(specialCharacters);
            Matcher matcher;

            //checking first name
            if(userInfo.getFirstName()!=null){
                matcher = pattern.matcher(userInfo.getFirstName());
                if(!matcher.matches()){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"first name. Only alphabets are allowed"),"Identity-1052");
                }

                if(userInfo.getFirstName().length()>24){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1054"),"first name","24"),"Identity-1054");
                }
            }
            //checking last name
            if(userInfo.getLastName()!=null){
                matcher = pattern.matcher(userInfo.getLastName());
                if(!matcher.matches()){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"last name. Only alphabets are allowed"),"Identity-1052");
                }

                if(userInfo.getLastName().length()>24){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1054"),"last name","24"),"Identity-1054");
                }
            }
            //checking loginId
            if(userInfo.getLoginId()!=null){
                pattern = Pattern.compile(loginIdPattern);
                matcher = pattern.matcher(userInfo.getLoginId());
                if(!matcher.matches()){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"loginId. Special characters are not allowed"),"Identity-1052");
                }

                if(userInfo.getLoginId().length()<10){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1053"),"loginId","10"),"Identity-1053");
                }
                if(userInfo.getLoginId().length()>20){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1054"),"loginId","20"),"Identity-1054");
                }
            }
            //checking workspaceId
            if(userInfo.getWorkspaceId()!=null){
                pattern = Pattern.compile(workspaceIdPattern);
                matcher = pattern.matcher(userInfo.getWorkspaceId());
                if(!matcher.matches()){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"workspaceId. Only lowercase with hyphens is allowed."),"Identity-1052");
                }

            }
            //checking seats
            Object seats = userInfo.getSeats();
            if(!(seats instanceof Long)){
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                    ("Identity-1052"),"seats. Only numeric values are allowed"),"Identity-1052");
            }
            //checking planId
            if(userInfo.getPlanId()!=null){
                String planId = userInfo.getPlanId();
                boolean notValid = true;
                switch (planId) {
                    case "starter":
                    case "growth" :
                    case "enterprise":
                        notValid = false;
                        break;
                }
                if(notValid)
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"planId"),"Identity-1052");
            }
            //checking email
            if(userInfo.getEmail()!=null){
                pattern = Pattern.compile(emailPattern);
                matcher = pattern.matcher(userInfo.getEmail());
                if(!matcher.matches()){
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                        ("Identity-1052"),"email. Special characters are not allowed"),"Identity-1052");
                }
            }

            if(userInfo.getPassword()!=null){
                try{
                    String password = rsaEncryption.decryptText(userInfo.getPassword());
                    if(password.length()<8){
                        throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                            ("Identity-1053"),"password","8"),"Identity-1053");
                    }
                    if(password.length()>14){
                        throw new ItorixException(String.format(ErrorCodes.errorMessage.get
                            ("Identity-1054"),"password","14"),"Identity-1054");
                    }
                } catch (Exception e) {
                    logger.error("Cannot decrypt hashed value");
                }
            }
    }

    public void restrictedWorkspaceNames(String restrictedNames) {
        Query query = new Query().addCriteria(Criteria.where("key").is("restrictedNames"));
        MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
        if (metaData != null) {
            logger.debug("Updating restricted Names master Mongo Template");
            Update update = new Update();
            update.set("metadata", restrictedNames);
            masterMongoTemplate.updateFirst(query, update, MetaData.class);
        } else
            masterMongoTemplate.save(new MetaData("restrictedNames", restrictedNames));
    }
    public HashSet<String> getRestrictedWorkspaceNames() throws JsonProcessingException {
        Query query = new Query().addCriteria(Criteria.where("key").is("restrictedNames"));
        logger.debug("Retrieving query to find restricted metadata by ID");
        MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
        if (metaData != null) {
            try {
                return new ObjectMapper().readValue(metaData.getMetadata(), HashSet.class);
            } catch (Exception ex) {
                logger.error("Error while converting static restricted name", ex);
                return new HashSet<>();
            }
        }
        return new HashSet<>();
    }

    public void addCleanUpDocument(List<SchedulerDocumentDTO> schedulerDocumentDTOList) {
        logger.debug("Adding Document to clean up scheduler list...");
        for (SchedulerDocumentDTO schedulerDocumentDTO : schedulerDocumentDTOList) {
            if ((schedulerDocumentDTO.getDb().equalsIgnoreCase(MONGODB) ||
                    schedulerDocumentDTO.getDb().equalsIgnoreCase(POSTGRES))) {
                if (schedulerDocumentDTO.isMasterDb()) {
                    masterMongoTemplate.save(new SchedulerDocument(schedulerDocumentDTO));
                } else {
                    mongoTemplate.save(new SchedulerDocument(schedulerDocumentDTO));
                }
            }
        }
    }

    public List<SchedulerDocument> getCleanUpDocument() {
        logger.debug("Getting Document to clean up scheduler list...");
        return mongoTemplate.findAll(SchedulerDocument.class);
    }

    public void deleteCleanUpDocument(String documentName) {
        logger.debug("Deleting Document {} from clean up scheduler list...", documentName);
        Query query = new Query().addCriteria(Criteria.where("key").is(documentName.replace(".", "_")));
        mongoTemplate.remove(query, SchedulerDocument.class);
    }
}
