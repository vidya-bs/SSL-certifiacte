package com.itorix.apiwiz.identitymanagement.dao;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.SwaggerContacts;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.identitymanagement.model.ActivityLog;
import com.itorix.apiwiz.identitymanagement.model.ActivitylogResponse;
import com.itorix.apiwiz.identitymanagement.model.CancelSubscriptions;
import com.itorix.apiwiz.identitymanagement.model.Constants;
import com.itorix.apiwiz.identitymanagement.model.DBConfig;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.Plan;
import com.itorix.apiwiz.identitymanagement.model.ResetUserToken;
import com.itorix.apiwiz.identitymanagement.model.Roles;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.Subscription;
import com.itorix.apiwiz.identitymanagement.model.SubscriptionPrice;
import com.itorix.apiwiz.identitymanagement.model.UIMetadata;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserDetails;
import com.itorix.apiwiz.identitymanagement.model.UserDomains;
import com.itorix.apiwiz.identitymanagement.model.UserInfo;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.model.UserStatus;
import com.itorix.apiwiz.identitymanagement.model.UserWorkspace;
import com.itorix.apiwiz.identitymanagement.model.Users;
import com.itorix.apiwiz.identitymanagement.model.VerificationToken;
import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
public class IdentityManagementDao {

	private Logger logger = LoggerFactory.getLogger(IdentityManagementDao.class);
	@Autowired
	protected BaseRepository baseRepository;
	@Autowired
	protected HttpServletRequest request;
	@Autowired
	protected HttpServletResponse response;
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
	JfrogUtilImpl jfrogUtilImpl;
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
	

	@PostConstruct
	private void initDBProperties(){
		applicationProperties = getDBApplicationProperties();
		getRegionData();
		getPodHost();
	}

	private  void getRegionData() {
		String endPoint = applicationProperties.getAwsURL();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>( headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<String>() {});
			JsonNode json =  new ObjectMapper().readTree(response.getBody());
			applicationProperties.setRegion(json.get("region").asText());
			applicationProperties.setAvailabilityZone(json.get("availabilityZone").asText());
			applicationProperties.setPodIP(json.get("privateIp").asText());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} 
	}


	private  void getPodHost() {
		String endPoint = applicationProperties.getAwsPodURL();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>( headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<String>() {});
			applicationProperties.setPodHost(response.getBody());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} 
	}

	private ApplicationProperties getDBApplicationProperties(){
		List<DBConfig> dbConfigList = getDBProperties();
		Map<String,String> map = new HashMap<>();
		if(dbConfigList != null){
			for(DBConfig dbConfig:dbConfigList){
				map.put(dbConfig.getPropertyKey(),dbConfig.getPropertyValue());
			}
		}
		return populateDBApplicationProperties(map);
	}

	
	public UserSession authenticate(UserInfo userInfo , boolean preAuthenticated) throws Exception {
		logger.debug("UserService.authenticate : " + userInfo );
		UserSession userSession = null;
		if (preAuthenticated || userInfo.allowLogin() == true) {
			User user = findByEmailUserName(userInfo.getLoginId());
			Workspace workspace = getWorkspace(userInfo.getWorkspaceId());
			if (user == null ){
				throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1012"),"IDENTITY-1012");
			}
			if(workspace == null){
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_022"),"USER_022");
			}
			UserWorkspace userWorkspace = user.getUserWorkspace(userInfo.getWorkspaceId());
			if(userWorkspace == null || userWorkspace.getActive() != true){ //(!user.getUserWorkspace(userInfo.getWorkspaceId()).getActive())){
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_022"),"USER_022");
			}

			if(user.canLogin() != true){
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_002"),"USER_002");
			}

			if(preAuthenticated || rsaEncryption.decryptText(user.getPassword()).equals(rsaEncryption.decryptText(userInfo.getPassword())))
			{
				userSession = new UserSession(user);
				userSession.setRequestAttributes(request);
				userSession.setLoginId(user.getLoginId());
				userSession.setWorkspaceId(workspace.getName());
				userSession.setTenant(workspace.getTenant());
				userSession.setRoles(user.getUserWorkspace(workspace.getName()).getRoles());
				userSession.setUserType(user.getUserWorkspace(workspace.getName()).getUserType());
				String status = workspace.getStatus()!=null && workspace.getStatus() != "" ? workspace.getStatus(): "active";
				userSession.setStatus(status);
				user.setUserCount(0);
				saveUser(user);
				userSession.setPlanId(workspace.getPlanId());
				userSession.setPaymentSchedule(workspace.getPaymentSchedule());
				userSession.setSubscriptionId(workspace.getSubscriptionId());
				if(workspace.getIsTrial() == true){
					Date now = new Date();
					if(workspace.getExpiresOn() != null && now.compareTo(workspace.getExpiresOn()) > 0){
						long diff = workspace.getExpiresOn().getTime() - now.getTime(); 
						int days = 0;
						if(diff > 0)
							days = (int)diff / 1000 / 60 / 60 / 24;
						userSession.setIsTrial(true);
						userSession.setTrialPeriod(workspace.getTrialPeriod());
						userSession.setTrialExpired("true");
						userSession.setExpiresOn(String.valueOf(days));
						userSession.setStatus("cancelled");
					}else{
						long diff = workspace.getExpiresOn().getTime() - now.getTime(); 
						int days = 0;
						if(diff > 0)
							days = (int)diff / 1000 / 60 / 60 / 24;
						userSession.setIsTrial(true);
						userSession.setTrialPeriod(workspace.getTrialPeriod());
						userSession.setTrialExpired("false");
						userSession.setExpiresOn(String.valueOf(days));
						userSession.setStatus(status);
					}
				}
				masterMongoTemplate.save(userSession);
				return userSession;
			} else {
				if (user.getUserCount() < 5) {
					user.setUserCount(user.getUserCount() + 1);
					saveUser(user);
					throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1012"),"IDENTITY-1012");
				} else {
					user.setUserStatus(UserStatus.getStatus(UserStatus.LOCKED));
					saveUser(user);
					throw new ItorixException(ErrorCodes.errorMessage.get("USER_002"),"USER_002");
				}
			}
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1012"),"IDENTITY-1012");
	}

	public Object addUser(UserInfo userInfo) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		userInfo.setWorkspaceId(userSessionToken.getWorkspaceId());
		Workspace workspace = getWorkspace(userInfo.getWorkspaceId());
		if( userInfo.allowInviteUser() == false && workspace == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_022"),"USER_022");
		}
		User loginUser = findUserById(userSessionToken.getUserId());
		if(!loginUser.isWorkspaceAdmin(userInfo.getWorkspaceId()) == true){
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
		}
		boolean isNewUser = false;
		User user = findByUserEmail(userInfo.getEmail());
		if(user == null){
			isNewUser = true;
			user = new User();
			user.setEmail(userInfo.getEmail());
		}
		if(user.containsWorkspace(userInfo.getWorkspaceId()) != true){
			List<UserWorkspace> workspaces;
			if(user.getWorkspaces() == null)
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
		VerificationToken token = createVerificationToken("AddUserToWorkspace", user.getEmail());
		token.setWorkspaceId(userInfo.getWorkspaceId());
		token.setUserType(User.LABEL_MEMBER);
		saveVerificationToken(token);
		if(isNewUser)
			sendAddUserEmail(token, user, loginUser.getFirstName() + " " + loginUser.getLastName());
		else
			sendInviteUserEmail(token, user, loginUser.getFirstName() + " " + loginUser.getLastName());
		user = saveUser(user);
		return "";
	}

	public Object registerWithToken(UserInfo userInfo, VerificationToken token) throws ItorixException {
		if(userInfo.allowInviteRegistration() == true){
			User user = findByUserEmail(token.getUserEmail());
			if(user == null ){}
			VerifyToken(token);
			user.setLoginId(userInfo.getLoginId());
			user.setPassword(userInfo.getPassword());
			user.setFirstName(userInfo.getFirstName());
			user.setLastName(userInfo.getLastName());
			user.setRegionCode(userInfo.getRegionCode());
			user.setWorkPhone(userInfo.getWorkPhone());
			user.setCompany(userInfo.getCompany());
			user.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
			if(userInfo.getMetadata() != null)
				user.setMetadata(userInfo.getMetadata());
			for(UserWorkspace workspace : user.getWorkspaces())
				if(workspace.getWorkspace().getName().equals(token.getWorkspaceId())){
					workspace.getWorkspace().setStatus("active");
					workspace.setAcceptInvite(true);
					workspace.setActive(true);
				}
			user = saveUser(user);
			sendActivationEmail(user);
		}
		return "";
	}

	public void updateUser(UserInfo userInfo) throws ItorixException{
		if(userInfo.allowEditUser()){
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			User loginUser = findUserById(userSessionToken.getUserId());
			if( !loginUser.getEmail().equals(userInfo.getEmail()) && findByEmailUserName(userInfo.getEmail()) != null){
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_021"),"USER_021");
			}
			if(userInfo.getLoginId() != null && !loginUser.getLoginId().equals(userInfo.getLoginId()) && findByEmailUserName(userInfo.getLoginId()) != null){
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_021"),"USER_021");
			}
			if( userInfo.getLoginId() != null)
				loginUser.setLoginId(userInfo.getLoginId());
			if( userInfo.getEmail() != null)
				loginUser.setEmail(userInfo.getEmail());
			if( userInfo.getFirstName() != null)
				loginUser.setFirstName(userInfo.getFirstName());
			if( userInfo.getLastName() != null)
				loginUser.setLastName(userInfo.getLastName());
			if( userInfo.getRegionCode() != null)
				loginUser.setRegionCode(userInfo.getRegionCode());
			if( userInfo.getWorkPhone() != null)
				loginUser.setWorkPhone(userInfo.getWorkPhone());
			if( userInfo.getCompany() != null)
				loginUser.setCompany(userInfo.getCompany());
			loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
			saveUser(loginUser);
		}
	}

	public void updateUserSubscription(UserInfo userInfo) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
		saveUser(loginUser);
	}
	
	public void updateNewsSubscription(UserInfo userInfo) throws ItorixException{
		User loginUser = findByUserEmail(userInfo.getEmail());
		if(loginUser != null)
		{
			loginUser.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
			saveUser(loginUser);
		}
	}
	
	public Map<String, Object> getNewsSubscription(String email) throws ItorixException{
		Map<String, Object> usernewsLetter = new HashMap<>();
		User loginUser = findByUserEmail(email);
		if(loginUser != null)
		{
			usernewsLetter.put("subscribeNewsLetter", loginUser.getSubscribeNewsLetter());
			return usernewsLetter;
		}
		return usernewsLetter;
	}

	public User getUser(String userId) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true){
			User user = findUserById(userId);
			if(user.getUserWorkspace(workspaceId) != null)
				return user;
			else
				return null;
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
	}

	public void updateMetaData(Map<String, String> metadata){
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		loginUser.setMetadata(metadata);
		saveUser(loginUser);
	}

	public Map<String, String> getMetaData(){
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		return loginUser.getMetadata();
	}

	public List<SwaggerTeam> getTeams(String userId) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true){
			User user = findUserById(userId);
			if(user != null && user.containsWorkspace(workspaceId)){
				Query query = new Query().addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
				List<SwaggerTeam> teams = mongoTemplate.find(query, SwaggerTeam.class);
				if(teams != null)
					return trimTeams(teams, user);
				else 
					return new ArrayList<SwaggerTeam>();
			}
			else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
		}
	}

	private  List<SwaggerTeam> trimTeams( List<SwaggerTeam> teams, User user){
		for(SwaggerTeam team : teams){
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
			for(SwaggerContacts contact : team.getContacts()){
				String email = contact.getEmail();
				if(!email.equals(user.getEmail()))
					contacts.remove(contact);
			}
			team.setContacts(contacts);
		}
		return teams;
	}

	public void updateUserTeams(List<SwaggerTeam> teams, String userId) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(loginUser.getId().equals(userId) || loginUser.isWorkspaceAdmin(workspaceId) == true){
			User user = findUserById(userId);
			if(user != null && user.containsWorkspace(workspaceId)){
				Query query = new Query().addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
				List<SwaggerTeam> dBteams = mongoTemplate.find(query, SwaggerTeam.class);
				if(dBteams != null){
					Set<String> teamNames = new HashSet<String>();
					for(SwaggerTeam dBteam : dBteams)
						teamNames.add(dBteam.getName());
					Set<SwaggerTeam> newTeamNames = new HashSet<SwaggerTeam>();
					for(SwaggerTeam team : teams){
						boolean newTeam = true;
						for(SwaggerTeam dBteam : dBteams){
							if(team.getName().equals(dBteam.getName())){
								dBteam.updateContact(team.getContacts().get(0));
								teamNames.remove(dBteam.getName());
								baseRepository.save(dBteam);
								newTeam = false;
							}
						}
						if(newTeam)
							newTeamNames.add(team);
					}
					addUserTeam(newTeamNames, user);
					deleteUserTeams(teamNames, user);
				}else{
					throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
				}
			}
			else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
		}
	}

	private void addUserTeam(Set<SwaggerTeam> teams, User user){
		SwaggerContacts swaggerContact = new SwaggerContacts();
		swaggerContact.setEmail(user.getEmail());
		for(SwaggerTeam team: teams){
			Query query = new Query().addCriteria(Criteria.where("name").is(team.getName()));
			SwaggerTeam dBteam = mongoTemplate.findOne(query, SwaggerTeam.class);
			if(dBteam != null){
				dBteam.updateContact(team.getContacts().get(0));
				baseRepository.save(dBteam);
			}
		}
	}

	private void deleteUserTeams(Set<String> teams, User user){
		SwaggerContacts swaggerContact = new SwaggerContacts();
		swaggerContact.setEmail(user.getEmail());
		for(String teamName: teams){
			Query query = new Query().addCriteria(Criteria.where("name").is(teamName));
			SwaggerTeam dBteam = mongoTemplate.findOne(query, SwaggerTeam.class);
			dBteam.removeContact(swaggerContact);
			baseRepository.save(dBteam);
		}

	}

	public Object registerWithMail(UserInfo userInfo) throws ItorixException {
		String domainId = userInfo.getEmail().split("@")[1];
		boolean domainAllowed = isDomainAllowed(userInfo.getEmail());
		if(domainAllowed){
			User userByEmail = findByEmail(userInfo.getEmail());
			if (userByEmail == null) {
				User user = new User();
				user.setEmail(userInfo.getEmail());
				VerificationToken token = createVerificationToken("registerUser", user.getEmail());
				token.setWorkspaceId(userInfo.getWorkspaceId());
				saveVerificationToken(token);
				sendRegistrationEmail(token, user);
				user = saveUser(user);
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_005"),"USER_005");
			}
		} else{
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("USER_011"),domainId ),"USER_011");
		}
		return "";
	}

	public Object register(UserInfo userInfo, VerificationToken token) throws ItorixException {
		if(userInfo.allowUserRegistration() == true ){
			User user = findByEmail(token.getUserEmail());
			if(user.getUserStatus() != null && user.getUserStatus().equals("active")){
				//User user = new User();
				user.setFirstName(userInfo.getFirstName());
				user.setLoginId(userInfo.getLoginId());
				user.setLastName(userInfo.getLastName());
				user.setPassword(userInfo.getPassword());
				user.setRegionCode(userInfo.getRegionCode());
				user.setWorkPhone(userInfo.getWorkPhone());
				user.setCompany(userInfo.getCompany());
				user.setSubscribeNewsLetter(userInfo.getSubscribeNewsLetter());
				List<String> userRoles =  new ArrayList<>();
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
				user = saveUser(user);
				sendActivationEmail(user);
			}else{
				throw new ItorixException("user email is not verified","USER_005");
			}
		}
		return "";
	}
	
	private void sendActivationEmail(User user){
		try{
			List<String> toMailId =new ArrayList<>();
			EmailTemplate template =new EmailTemplate();
			template.setSubject(applicationProperties.getUserActivationMailSubject());
			toMailId.add(user.getEmail());
			template.setToMailId(toMailId);
			String messageBody=	MessageFormat.format(applicationProperties.getUserActivationMailBody(),user.getFirstName() +" "+user.getLastName(), applicationProperties.getAppURL());
			template.setBody(messageBody);
			mailUtil.sendEmail(template);
		} catch(Exception e){
			
		}
	}

	public VerificationToken password(User user) throws ItorixException {
		User userByEmail = findByEmail(user.getEmail());
		if (userByEmail != null) {
			VerificationToken token = createVerificationToken("resetPassword", user.getEmail());
			sendPassWordResetEmail(token, userByEmail);
			saveVerificationToken(token);
			return token;
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_003"),"USER_003");
		}
	}

	public User updatePassword(User user, VerificationToken token) throws ItorixException {
		if(token.isAlive() && !token.getUsed()){
			User userByEmail = findByEmail(user.getEmail());
			if (userByEmail != null) {
				userByEmail.setPassword(user.getPassword());
				userByEmail.setUserStatus("active"); 
				userByEmail = saveUser(userByEmail);
				token.setUsed(true);
				saveVerificationToken(token);
				return userByEmail;
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
			}
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_020"),"USER_020");
		}
	}

	public Object changePassword(User user) throws  Exception {
		if(user.getOldPassword() == null || user.getOldPassword().trim() == "" ||
				user.getNewPassword() == null || user.getNewPassword().trim() == "")
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_016"),"USER_016");
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User dbUser = findUserById(userSessionToken.getUserId());
		if(dbUser!=null){
			if ( rsaEncryption.decryptText(dbUser.getPassword()).equals(rsaEncryption.decryptText(user.getOldPassword()))) {
				dbUser.setPassword(user.getNewPassword());
				dbUser = saveUser(dbUser);
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_004"),"USER_004");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
		return "";
	}

	public void removeWorkspaceUser(String userId) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		String workspaceId = userSessionToken.getWorkspaceId();
		User user = findUserById(userId);
		if(user != null){
			if(loginUser.isWorkspaceAdmin(workspaceId)){
				List<UserWorkspace> workspaces = user.getWorkspaces();
				for(UserWorkspace workspace : workspaces)
					if(workspace.getWorkspace().getName().equals(workspaceId)){
						if( workspaces.size() == 0){
							removeUser(user);
							removeUnusedTokens(user);
						}
						else{
							workspaces.remove(workspace);
							user.setWorkspaces(workspaces);
							saveUser(user);
						}
						break;
					}
				user.setWorkspaces(workspaces);
				saveUser(user);
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
	}

	private void removeUnusedTokens(User user) {
		Query query = new Query(Criteria.where("userEmail").is(user.getEmail()).and("used").is(false));
		masterMongoTemplate.remove(query, VerificationToken.class);
	}

	private void removeUser(User user) {
		masterMongoTemplate.remove(user);

	}

	public void addSiteAdmin(String userId) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		String workspaceId = userSessionToken.getWorkspaceId();
		User user = findUserById(userId);
		if(user != null && user.getUserWorkspace(workspaceId)!= null){
			if(loginUser.isWorkspaceAdmin(workspaceId) == true){
				List<UserWorkspace> workspaces = user.getWorkspaces();
				for(UserWorkspace workspace : workspaces)
					if(workspace.getWorkspace().getName().equals(workspaceId)){
						workspace.setUserType("Site-Admin");
						break;
					}
				user.setWorkspaces(workspaces);
				saveUser(user);
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
	}

	public List<String> getAllUsersWithRoleDevOPS(){
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		List<User> users = findUsersByWorkspace(workspaceId);
		List<String> mailIds = new ArrayList<>();
		if(users != null)
			for(User user : users)
				mailIds.add(user.getEmail());
		return mailIds;
	}

	public void updateWorkspaceUserRoles(String userId, List<String> roles) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		User user = findUserById(userId);
		if(user != null){
			if(loginUser.isWorkspaceAdmin(workspaceId)){
				List<UserWorkspace> workspaces = user.getWorkspaces();
				for(UserWorkspace workspace : workspaces)
					if(workspace.getWorkspace().getName().equals(workspaceId)){
						workspace.setRoles(roles);
						break;
					}
				user.setWorkspaces(workspaces);
				saveUser(user);
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
	}

	public Workspace createWorkspace(UserInfo userInfo) throws ItorixException{
		return createWorkspace(userInfo, "ActivationPending");
	}

	public Workspace createActiveWorkspace(UserInfo userInfo) throws ItorixException{
		return createWorkspace(userInfo, "active");
	}

	public Workspace createWorkspace(UserInfo userInfo,String status) throws ItorixException{
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(userInfo.getWorkspaceId())));
		Workspace workspace = mongoTemplate.findOne(query, Workspace.class);
		if(workspace == null){
			workspace = new Workspace();
			workspace.setName(userInfo.getWorkspaceId());
			workspace.setPlanId(userInfo.getPlanId());
			workspace.setTenant(userInfo.getWorkspaceId());
			workspace.setStatus(status);
			workspace.setKey( UUID.randomUUID().toString());
			workspace.setRegionCode(userInfo.getRegionCode());
			workspace.setSubscriptionId(userInfo.getSubscriptionId());
			workspace.setPaymentSchedule(userInfo.getPaymentSchedule());
			workspace.setTrialPeriod(userInfo.getTrialPeriod());
			workspace.setIsTrial(userInfo.isTrial());
			workspace.setLicenceKey(UUID.randomUUID().toString());
			workspace.setSeats(userInfo.getSeats());
			if(userInfo.getTrialPeriod() != null ){
				try{
					int days = Integer.valueOf(userInfo.getTrialPeriod());
					Date vailidTill = Date.from(LocalDateTime.now().plusDays(days).atZone(ZoneId.systemDefault()).toInstant());
					workspace.setExpiresOn(vailidTill);
				}catch(Exception e){
					logger.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
			masterMongoTemplate.save(workspace);
			return workspace;
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_018"),"USER_018");
		}
	}

	public void recoverWorkspace(String email){
		User user = findByEmailUserName(email);
		String workspacesStr = "";
		int length = user.getWorkspaces().size();
		int index = 1;
		List<UserWorkspace> workspaces = user.getWorkspaces();
		for(UserWorkspace workspace : workspaces){
			workspacesStr = workspacesStr + workspace.getWorkspace().getName();
			if(index < length)
				workspacesStr = workspacesStr + ", ";
			index ++;
		}
		try {
			String bodyText = MessageFormat.format(applicationProperties.getRecoverWorkspaceBody(), user.getFirstName() +" "+user.getLastName(), workspacesStr, applicationProperties.getAppURL());
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			String subject = applicationProperties.getRecoverWorkspaceSubject();
			sendMail(subject, bodyText,toRecipients);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	private long getMinimumSeats(Workspace workspace){
		Subscription subscription = workspaceDao.getSubscription(workspace.getPlanId());
		List<SubscriptionPrice> prices = subscription.getSubscriptionPrices();
		if(workspace.getPaymentSchedule().equalsIgnoreCase("month")){
			SubscriptionPrice price = prices.stream().filter(o -> o.getPeriod().equalsIgnoreCase("MONTHLY")).collect(Collectors.toList()).get(0);
			return  Long.parseLong(price.getMinimumUnits());
		}
		else{
			SubscriptionPrice price = prices.stream().filter(o -> o.getPeriod().equalsIgnoreCase("YEARLY")).collect(Collectors.toList()).get(0);
			return Long.parseLong(price.getMinimumUnits());
		}
	}

	public Map<String, Object> validateWorkspace(String workspaceId){
		Workspace workspace = getWorkspace(workspaceId);
		Map<String, Object> response = new HashMap<String, Object>();
		if(workspace!=null){
			long usedSeats= workspaceDao.getUsedSeats(workspaceId);
			boolean allowDowngrade = false;
			long minimumSeats = getMinimumSeats(workspace);
			long seats = usedSeats < minimumSeats ? workspace.getSeats() - minimumSeats : workspace.getSeats() - usedSeats;
			allowDowngrade = seats >  0 ? true:false;
			boolean inviteUser = workspace.getSeats() - usedSeats > 0 ? true : false;
			response.put("status", "false");
			response.put("planId", workspace.getPlanId());
			response.put("allotedSeats", workspace.getSeats());
			response.put("currentSeats", usedSeats);
			response.put("allowDowngrade", allowDowngrade);
			response.put("inviteUser", inviteUser);
			response.put("remainingSeats", seats);
			response.put("ssoEnabled", workspace.getSsoEnabled());
			if(workspace.getSsoEnabled() == true){
				response.put("ssoHost", workspace.getSsoHost());
				response.put("ssoPath", workspace.getSsoPath());
			}
			
		}else{
			response.put("status", "true");
		}
		return response;
	}

	public Map<String, Object> validateSeats(long seats){
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		Workspace workspace = getWorkspace(workspaceId);
		Map<String, Object> response = new HashMap<String, Object>();
		long usedSeats= workspaceDao.getUsedSeats(workspaceId);
		if(seats > workspace.getSeats())
			response.put("status", "false");
		else
			response.put("status", "true");
		response.put("planId", workspace.getPlanId());
		response.put("allotedSeats", workspace.getSeats());
		response.put("currentSeats", usedSeats);
		response.put("remainingSeats", (workspace.getSeats() - usedSeats));
		return response;
	}

	public Map<String, String> validateUserId(String userId){
		User user = findByEmailUserName(userId);
		Map<String, String> response = new HashMap<String, String>();
		//String response = "{\"isAvailable\" : #status#}";
		if(user!=null){
			response.put("status", "false");
		}else{
			response.put("status", "true");
		}
		return response;
	}

	private User sendRegistrationEmail1(User user){
		try {
			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();
			user.setVerificationToken(randomUUIDString);
			user.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
			String link = applicationProperties.getAppUrl() + "/register/" + randomUUIDString + "/verify";
			EmailTemplate emailTemplate =new EmailTemplate();
			String bodyText = MessageFormat.format(applicationProperties.getRegistermailBody(), link,user.getFirstName() +" "+user.getLastName());
			ArrayList<String> toRecipients =new ArrayList<String>();
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

	public void sendPassWordResetEmail(VerificationToken token, User user){
		try {
			String link = applicationProperties.getAppURL() + "/reset-password/" + token.getId() ;
			String bodyText = MessageFormat.format(applicationProperties.getResetMailBody(), user.getFirstName() +" "+user.getLastName(), link);
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			String subject = applicationProperties.getResetSubject();
			sendMail(subject, bodyText,toRecipients);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void sendAddUserEmail(VerificationToken token, User user, String userName){
		try {
			String link = applicationProperties.getAppURL() + "/user/" + token.getId() ;
			String bodyText = MessageFormat.format(applicationProperties.getAddWorkspaceUserBody(), userName, token.getWorkspaceId(), link);
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			String subject = applicationProperties.getAddWorkspaceUserSubject();
			sendMail(subject, bodyText,toRecipients);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendInviteUserEmail(VerificationToken token, User user, String userName){
		try {
			String link = applicationProperties.getAppURL() + "/user-invited/" + token.getId() ;
			String bodyText = MessageFormat.format(applicationProperties.getInviteWorkspaceUserBody(), user.getFirstName() +" "+user.getLastName(), userName, token.getWorkspaceId(), link, link);
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			String subject = applicationProperties.getInviteWorkspaceUserSubject();
			sendMail(subject, bodyText,toRecipients);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void sendRegistrationEmail(VerificationToken token, User user){
		try {

			String link = applicationProperties.getAppURL() + "/register/" + token.getId() + "/verify";
			String bodyText = MessageFormat.format(applicationProperties.getRegistermailBody(), user.getEmail(),  link);
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			String subject = applicationProperties.getRegisterSubject();
			sendMail(subject, bodyText,toRecipients);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	private void sendMail(String subject, String body, ArrayList<String> toRecipients){
		try {
			EmailTemplate emailTemplate =new EmailTemplate();
			emailTemplate.setToMailId(toRecipients);
			emailTemplate.setSubject(subject);
			emailTemplate.setBody(body);
			mailUtil.sendEmail(emailTemplate);
		} catch (MessagingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public VerificationToken createVerificationToken(String type, String email){
		int TOKEN_VALID_DAYS = 7;
		String id = UUID.randomUUID().toString();
		Date created = new Date();
		Date validTill = DateUtils.addDays(new Date(), TOKEN_VALID_DAYS);
		VerificationToken token = new VerificationToken(id, type, created, validTill, email, null);
		return token;
	}

	public String VerifyToken(VerificationToken token) throws ItorixException{
		//boolean isAlive = token.isAlive();
		if(token.isAlive() == true){
			if( !token.getUsed()){
				if(token.getType().equals("registerUser")){
					return activateEmail(token);
				}
				if(token.getType().equals("AddUserToWorkspace")){
					return addUserToWorkspace(token);
				}
				if(token.getType().equals("resetPassword")){
					return userPasswordReset(token);
				}
			}
			else{
				throw new ItorixException(ErrorCodes.errorMessage.get("USER_020"),"USER_020");
			}
		}else{
			resendToken(token);
			return "tokenResent";
		}
		return "";
	}

	public void resendToken(VerificationToken token) throws ItorixException{
		User user = findByEmail(token.getUserEmail());
		VerificationToken newToken = createVerificationToken(token.getType(), token.getUserEmail());
		if(token.getType().equals("registerUser")){
			newToken.setWorkspaceId(token.getWorkspaceId());
			newToken.setUserType(token.getType());
			saveVerificationToken(newToken);
			sendRegistrationEmail(token, user);
		}
		if(token.getType().equals("AddUserToWorkspace")){
			newToken.setWorkspaceId(token.getWorkspaceId());
			newToken.setUserType(User.LABEL_MEMBER);
			saveVerificationToken(token);
			if(user.isNew() == true)
				sendAddUserEmail(token, user, " " );
			else
				sendInviteUserEmail(token, user, " ");
		}
		if(token.getType().equals("resetPassword")){
			sendPassWordResetEmail(newToken, user);
			saveVerificationToken(newToken);
		}
	}

	public void resendToken(UserInfo userInfo) throws ItorixException{
		if(userInfo.allowCreateToken() != true)
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1004"),"IDENTITY-1004");
		if(userInfo.getType().equals("password-reset") )
		{
			User user = new User();
			user.setEmail(userInfo.getEmail());
			password(user);
		}else if(userInfo.getType().equals("register")){
			//			if(userInfo.getWorkspaceId() == null)
			//				throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1004"),"IDENTITY-1004");
			User user = findByEmail(userInfo.getEmail());
			//			UserWorkspace workspace = user.getUserWorkspace(userInfo.getWorkspaceId());
			//			if(workspace != null && workspace.getUserType().equals("Site-Admin") && workspace.getAcceptInvite() == false){
			VerificationToken token = createVerificationToken("registerUser", user.getEmail());
			//				token.setWorkspaceId(userInfo.getWorkspaceId());
			//				token.setUserType("Site-Admin");
			saveVerificationToken(token);
			sendRegistrationEmail(token, user);
			//			}else {
			//				throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
			//			}
		}
	}
	
	public void resendInvite(UserInfo userInfo) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		boolean isNewUser = false;
		User user = findByUserEmail(userInfo.getEmail());
		if(user.getLoginId() == null){
			isNewUser = true;
		}
		VerificationToken token = createVerificationToken("AddUserToWorkspace", user.getEmail());
		token.setWorkspaceId(workspace.getName());
		token.setUserType(User.LABEL_MEMBER);
		saveVerificationToken(token);
		if(isNewUser)
			sendAddUserEmail(token, user, userSessionToken.getUsername());
		else
			sendInviteUserEmail(token, user, userSessionToken.getUsername());
	}
	
	private String activateEmail(VerificationToken token){
		User user = findByEmail(token.getUserEmail());
		user.setUserStatus("active");
		saveUser(user);
		token.setUsed(true);
		saveVerificationToken(token);
		return "activated";
	}

	private String activateUser(VerificationToken token){
		User user = findByEmail(token.getUserEmail());
		List<UserWorkspace> workspaces = user.getWorkspaces();
		for(UserWorkspace workspace : workspaces)
			if(workspace.getWorkspace().getName().equals(token.getWorkspaceId())){
				workspace.getWorkspace().setStatus("active");
				workspace.setAcceptInvite(true);
				workspace.setActive(true);
			}
		user.setWorkspaces(workspaces);
		saveUser(user);
		Workspace workspace = getWorkspace(token.getWorkspaceId());
		workspace.setStatus("active");
		masterMongoTemplate.save(workspace);
		token.setUsed(true);
		saveVerificationToken(token);
		return "activated";
	}

	private String addUserToWorkspace(VerificationToken token){
		User user = findByUserEmail(token.getUserEmail());
		if(user.getLoginId()!= null && user.getPassword() != null){
			List<UserWorkspace> workspaces = user.getWorkspaces();
			for(UserWorkspace workspace : workspaces)
				if(workspace.getWorkspace().getName().equals(token.getWorkspaceId()))
					if(workspace.getWorkspace().getName().equals(token.getWorkspaceId())){
						workspace.getWorkspace().setStatus("active");
						workspace.setAcceptInvite(true);
						workspace.setActive(true);
					}
			user.setWorkspaces(workspaces);
			saveUser(user);
			token.setUsed(true);
			saveVerificationToken(token);
			return "WorkspaceAdded";
		}else{
			return "registrationRequired";
		}
	}

	private String userPasswordReset(VerificationToken token){
		User user = findByEmail(token.getUserEmail());
		if(user.getLoginId()!= null && user.getPassword() != null){
			user.setUserStatus("inResetPassword");
			saveUser(user);
			return "resetPassword";
		}else{
			return "";
		}
	}

	public void saveVerificationToken(VerificationToken token){
		masterMongoTemplate.save(token);
	}

	public VerificationToken getVerificationToken(String tokenId){
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("id").is(tokenId)));
		VerificationToken token = masterMongoTemplate.findOne(query, VerificationToken.class);
		return token;
	}

	public void cancelSubscription(CancelSubscriptions subscription) throws ItorixException{
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(loginUser.isWorkspaceAdmin(subscription.getWorkspaceId())){
			masterMongoTemplate.save(subscription);
			Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
			workspace.setStatus("suspended");
			masterMongoTemplate.save(workspace);
		} else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_029"),"USER_029");
		}
	}


	public Object listUsers(String filterbynames) throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(!loginUser.isWorkspaceAdmin(workspaceId)){
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
		}
		Users allUsers = new Users();
		List<String> userName=new ArrayList<String>();
		List<UserDetails> userDetails = new ArrayList<UserDetails>();
		List<User> dbUsers = findUsersByWorkspace(workspaceId);
		List<User> dbActiveUsers = dbUsers;
		JSONArray namesList = new JSONArray();
		JSONObject userNamesList = new JSONObject();

		if (filterbynames != null && "true".equalsIgnoreCase(filterbynames)) {
			for (int i = 0; i < dbActiveUsers.size(); i++) {
				User user = dbUsers.get(i);
				JSONObject userNames = new JSONObject();
				userNames.put("displayName", user.getFirstName() + " " + user.getLastName());
				userNames.put("userId", user.getId());
				userNames.put("emailId",user.getEmail());
				namesList.add(userNames);
			}
			userNamesList.put("users", namesList);
			return userNamesList;

		} else if(filterbynames != null && "names".equalsIgnoreCase(filterbynames)){
			for (int i = 0; i < dbActiveUsers.size(); i++) {
				User user = dbUsers.get(i);
				userName.add(user.getFirstName() + " " + user.getLastName());
			}
			JSONObject userNames = new JSONObject();
			userNames.put("username", userName);
			return userNames;
		}
		else {
			int userSize = dbUsers.size();
			for (int i = 0; i < userSize; i++) {
				boolean canAdd = false;
				User user = dbUsers.get(i);
				UserDetails requiredDetails = new UserDetails();
				requiredDetails.setUserId(user.getId());
				requiredDetails.setFirstName(user.getFirstName());
				requiredDetails.setLastName(user.getLastName());
				requiredDetails.setEmail(user.getEmail());
				requiredDetails.setStatus(user.getUserStatus());
				List<UserWorkspace> workspaces = user.getWorkspaces();
				if(workspaces != null){
					List<UserInfo> userworkspaces = new ArrayList<>();
					for(UserWorkspace workspace : workspaces){
						if(workspace.getName().equals(workspaceId)){
							if(workspace.getAcceptInvite() || userSize == 1){
								canAdd = true;
								UserInfo userworkspace = new UserInfo();
								userworkspace.setName(workspace.getWorkspace().getName());
								userworkspace.setRoles(workspace.getRoles());
								userworkspace.setUserType(workspace.getUserType());
								userworkspace.setTeams(getTeams(user.getId()));
								userworkspaces.add(userworkspace);
							}
						}
					}
					requiredDetails.setWorkspaces(userworkspaces);
				}
				if(canAdd)
					userDetails.add(requiredDetails);
			}
			allUsers.setUsers(userDetails);
			return allUsers;
		}
	}

	public Object listUsers() throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String workspaceId = userSessionToken.getWorkspaceId();
		User loginUser = findUserById(userSessionToken.getUserId());
		if(!loginUser.isWorkspaceAdmin(workspaceId)){
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_023"),"USER_023");
		}
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
			requiredDetails.setStatus(user.getUserStatus());
			requiredDetails.setCts(user.getCts());
			requiredDetails.setCreatedUserName(user.getCreatedUserName());
			List<UserWorkspace> workspaces = user.getWorkspaces();
			if(workspaces != null){
				List<UserInfo> userworkspaces = new ArrayList<>();
				for(UserWorkspace workspace : workspaces){
					if(workspace.getName().equals(workspaceId)){
						if(!workspace.getAcceptInvite()){
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
			if(canAdd)
			userDetails.add(requiredDetails);
		}
		allUsers.setUsers(userDetails);
		return allUsers;

	}
	
	public void lockUser(String userId){
		User user = findUserById(userId);
		if(user != null){
			user.setUserStatus("Locked");
			saveUser(user);
		}
	}
	
	public void unLockUser(String userId){
		User user = findUserById(userId);
		if(user != null){
			user.setUserStatus("active");
			user.setUserCount(0);
			saveUser(user);
		}
	}

	public User findUserById(String userId){
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("id").is(userId)));
		User user = masterMongoTemplate.findOne(query, User.class);
		return user;
	}

	public List<User> findUsersByWorkspace(String workspaceId){
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("workspaces.workspace.name").is(workspaceId)));
		List<User> users = masterMongoTemplate.find(query, User.class);
		return users;
	}

	public Workspace getWorkspace(String workapaceId){
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workapaceId)));
		Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
		return workspace;
	}

	public User saveUser(User user) {
		user.setEmail(user.getEmail().toLowerCase());
		return baseRepository.save(user, masterMongoTemplate);
	}

	public User findByEmailUserName(String userId) {
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(userId),
				Criteria.where(User.LABEL_LOGINID).is(userId)));
		return  masterMongoTemplate.findOne(query, User.class);
	}

	public User findByUserEmail(String userId) {
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(userId)));
		return  masterMongoTemplate.findOne(query, User.class);
	}

	public User findByLogin(String userId) {
		return  findByEmailUserName(userId);
	}

	public User findByEmailUserName(String email, String userName) {
		Query query = new Query(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(email),
				Criteria.where(User.LABEL_LOGINID).is(userName)));
		User user = masterMongoTemplate.findOne(query, User.class);
		return user;
	}

	public User findByEmailAndPassword(String email, String password){
		Query query  = new Query();
		query.addCriteria(new Criteria().andOperator(Criteria.where(User.LABEL_EMAIL).is(email),
				Criteria.where(User.LABEL_PASSWORD).is(password)));
		return masterMongoTemplate.findOne(query, User.class);
	}

	public User findByEmail(String email) {
		Query query  = new Query();
		query.addCriteria(Criteria.where(User.LABEL_EMAIL).is(email));
		return masterMongoTemplate.findOne(query, User.class);
	}

	public UserSession findUserSession(String sessionId) {
		Query query  = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(sessionId)));
		return masterMongoTemplate.findOne(query, UserSession.class);
		//return  userSessionRepository.findOne(sessionId);
	}

	private boolean isDomainAllowed(User user){
		boolean isAllowed =false;
		if(user.getEmail()!=null){
			return isDomainAllowed(user.getEmail());
		}
		return isAllowed;
	}

	private boolean isDomainAllowed(String  eamil){
		boolean isAllowed =false;
		if(eamil!=null){
			isAllowed = true;
			String domainId = eamil.split("@")[1];
			String blockedDomainList=applicationProperties.getBlockedMailDomains();
			String[] blockedDomains = blockedDomainList.split(",");
			for (String domain : blockedDomains) {
				if(domain.equalsIgnoreCase(domainId)){
					isAllowed=false;
					break;
				}
			}
		}
		return isAllowed;
	}

	public void createUserDomains(UserDomains userDomains) throws ItorixException {
		UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
		if(dbUserDomains==null){
			userDomains.setName(UserDomains.NAME);
			baseRepository.save(userDomains);
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_012"),"USER_012");
		}
	}

	public void updateUserDomains(UserDomains userDomains) throws ItorixException {
		UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
		if(dbUserDomains!=null){
			dbUserDomains.setDomains(userDomains.getDomains());
			baseRepository.save(dbUserDomains);
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_013"),"USER_013");
		}
	}

	public Object getUserDomains() throws ItorixException{
		JSONObject obj =new JSONObject();
		UserDomains dbUserDomains = baseRepository.findOne("name", UserDomains.NAME, UserDomains.class);
		obj.put("domains", dbUserDomains.getDomains());
		return obj;
	}

	public void resendUserToken(ResetUserToken userToken) throws ItorixException {
		User user = findByEmail(userToken.getEmailID());
		if(user!=null){
			if(userToken.getType().equalsIgnoreCase("register")){
				user.setUserStatus(UserStatus.getStatus(UserStatus.PENDING));
				user.setUserCount(0);
				//user = mailUtil.sendEmailRegistrationLink(user);
				user = sendPassWordResetEmail(user);
			}else if(userToken.getType().equalsIgnoreCase("password-reset") && user.getUserStatus().equalsIgnoreCase(UserStatus.getStatus(UserStatus.ACTIVE))){
				user = sendPassWordResetEmail(user);
				user.setUserStatus(UserStatus.getStatus(UserStatus.INRESETPASSWORD));
			}
			user = baseRepository.save(user);
		}else {
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_003"),"USER_003");
		}
	}

	public User sendPassWordResetEmail(User user){

		try {
			ArrayList<String> toRecipients =new ArrayList<String>();
			toRecipients.add(user.getEmail());
			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();
			user.setVerificationToken(randomUUIDString);
			user.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
			String link = "http://" + applicationProperties.getVerificationLinkHostName() 
			+ applicationProperties.getVerificationLinkPort() + "/v1/user/resetpassword?verificationToken="
			+ randomUUIDString + "&emailId=" + user.getEmail() + "";
			String bodyText = MessageFormat.format(applicationProperties.getResetMailBody(), link,user.getFirstName() +" "+user.getLastName());
			EmailTemplate emailTemplate =new EmailTemplate();
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


	public Object getUserRoles(User user) throws ItorixException {
		User userByEmail = findByEmail(user.getEmail());
		//		List<String> userRoleslist;
		JSONObject userRoles = new JSONObject();
		if (userByEmail != null) {

		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
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
				//userByEmail = mailUtil.sendResetPasswordLink(userByEmail);
				userByEmail=sendPassWordResetEmail(userByEmail);
				userByEmail.setTokenValidUpto(DateUtils.addDays(new Date(), 1));
				userByEmail.setDisplayMessage(Constants.USER_VERIFICATION_LINK);
				userByEmail.setUserCount(userByEmail.getUserCount() + 1);
				userByEmail.setVerificationStatus("resendVerification");
			} else if (CurrentDate.compareTo(userByEmail.getTokenValidUpto()) > 0
					&& !(userByEmail.getUserCount() > 3)) {
				userByEmail.setDisplayMessage(Constants.USER_VERIFICATION_LINK);
				userByEmail=sendPassWordResetEmail(userByEmail);
				//userByEmail = mailUtil.sendResetPasswordLink(userByEmail);
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
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
		return userByEmail;
	}



	public Object updateUserStatus(String email, String interactionid) throws ItorixException {
		logger.debug("UserService.updateUserStatus : interactionid="+interactionid);

		if(email !=null){
			try {
				List<String> toMailId =new ArrayList<>();
				toMailId.add(email);
				User userDetails = findByEmail(email);
				EmailTemplate template =new EmailTemplate();
				template.setSubject(applicationProperties.getUserActivationMailSubject());
				template.setToMailId(toMailId);
				String messageBody=	MessageFormat.format(applicationProperties.getUserActivationMailBody(),userDetails.getFirstName() +" "+userDetails.getLastName());

				//template.setBody(applicationProperties.getUserActivationMailBody());
				template.setBody(messageBody);
				mailUtil.sendEmail(template);
				userDetails.setUserStatus(UserStatus.getStatus(UserStatus.ACTIVE));
				baseRepository.save(userDetails, masterMongoTemplate);

			} catch (MessagingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}else{
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1001"),"IDENTITY-1001");
		}
		return "";
	}
	public Object getUsers(String filterbynames) {


		Users allUsers = new Users();
		List<String> userName=new ArrayList<String>();
		List<UserDetails> userDetails = new ArrayList<UserDetails>();

		List<User> dbUsers = findAll();
		List<User> dbActiveUsers = dbUsers;//findAllActiveUsers();

		JSONArray namesList = new JSONArray();
		JSONObject userNamesList = new JSONObject();

		if (filterbynames != null && "true".equalsIgnoreCase(filterbynames)) {
			for (int i = 0; i < dbActiveUsers.size(); i++) {
				User user = dbUsers.get(i);
				JSONObject userNames = new JSONObject();
				userNames.put("displayName", user.getFirstName() + " " + user.getLastName());
				userNames.put("userId", user.getId());
				userNames.put("emailId",user.getEmail());
				namesList.add(userNames);

			}
			userNamesList.put("users", namesList);
			return userNamesList;

		} else if(filterbynames != null && "names".equalsIgnoreCase(filterbynames)){
			for (int i = 0; i < dbActiveUsers.size(); i++) {

				User user = dbUsers.get(i);
				userName.add(user.getFirstName() + " " + user.getLastName());
			}
			JSONObject userNames = new JSONObject();
			userNames.put("username", userName);

			return userNames;
		}
		else {
			for (int i = 0; i < dbUsers.size(); i++) {
				User user = dbUsers.get(i);
				UserDetails requiredDetails = new UserDetails();
				requiredDetails.setFirstName(user.getFirstName());
				requiredDetails.setLastName(user.getLastName());
				requiredDetails.setLoginId(user.getLoginId());
				requiredDetails.setEmail(user.getEmail());
				//				requiredDetails.setRoles(user.getRoles());
				//				requiredDetails.setType(user.getType());
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

	public Object logOut(String jsessionid) throws  ItorixException {
		UserSession userSessionToken = findUserSession(jsessionid);//userSessionRepository.findOne(jsessionid);
		if (userSessionToken == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1011"),"IDENTITY-1011");
		} else {
			Query query  = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(jsessionid)));
			//masterMongoTemplate.remove(query, UserSession.class);
			masterMongoTemplate.remove(userSessionToken);
		}
		return "";
	}

	public Object getActivityLogDetails(String jsessionid, String timeRange, String userId, int offset, int pageSize)
			throws  ItorixException, ParseException {
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
			for(ActivityLog log: list) {
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
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_008"),"USER_008");
		}
		return response;
	}

	public List<ActivityLog> findActivityByTimeRange(String timeRange, int offset, int pageSize) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String timeRanges[] = timeRange.split("~");
		Date startDate = format.parse(timeRanges[0]);
		Date endDate = format.parse(timeRanges[1]);
		long StartTime =DateUtil.getStartOfDay(startDate).getTime();
		long endDateTime =DateUtil.getEndOfDay(endDate).getTime();
		Query query = new Query(Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime))
				.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
		return baseRepository.find(query, ActivityLog.class);
	}

	public Long findActivityByTimeRangeCounter(String timeRange) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Query query = new Query();
		String timeRanges[] = timeRange.split("~");
		Date startDate = format.parse(timeRanges[0]);
		Date endDate = format.parse(timeRanges[1]);
		long StartTime =DateUtil.getStartOfDay(startDate).getTime();
		long endDateTime =DateUtil.getEndOfDay(endDate).getTime();
		query.addCriteria(Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime));
		return mongoTemplate.count(query, ActivityLog.class);
	}

	public List<ActivityLog> findActivityByTimeRangeByUser(String timeRange, String userId, int offset, int pageSize) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String timeRanges[] = timeRange.split("~");
		Date startDate = format.parse(timeRanges[0]);
		Date endDate = format.parse(timeRanges[1]);
		long StartTime =DateUtil.getStartOfDay(startDate).getTime();
		long endDateTime =DateUtil.getEndOfDay(endDate).getTime();
		Query query = new Query(Criteria.where(ActivityLog.USER_ID).is(userId)
				.and(ActivityLog.LAST_CHANGED_AT).gte(StartTime).lte(endDateTime))
				.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
		return baseRepository.find(query, ActivityLog.class);
	}

	public Long  findActivityByTimeRangeByUserCounter(String timeRange, String userId) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Query query = new Query();
		String timeRanges[] = timeRange.split("~");
		Date startDate = format.parse(timeRanges[0]);
		Date endDate = format.parse(timeRanges[1]);
		long StartTime =DateUtil.getStartOfDay(startDate).getTime();
		long endDateTime =DateUtil.getEndOfDay(endDate).getTime();
		query.addCriteria(new Criteria().andOperator(Criteria.where(ActivityLog.USER_ID).is(userId),
				Criteria.where(ActivityLog.LAST_CHANGED_AT).gte(StartTime)
				.lte(endDateTime)));
		return mongoTemplate.count(query, ActivityLog.class);
	}

	public User getUserById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		User user = masterMongoTemplate.findOne(query, User.class);
		return user;
	}

	public UserSession getUserSessionDetails(String sessionId, String appId){
		UserSession userSessionToken = (sessionId != null) ? findUserSession(sessionId) : null;
		if(appId != null && appId.equalsIgnoreCase("portal")){
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

	public boolean validateSession(String sessionId) throws ItorixException
	{
		UserSession userSessionToken = (sessionId != null) ? findUserSession(sessionId) : null;
		if(	userSessionToken==null)
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1011") ,"IDENTITY-1011");
		return (userSessionToken == null) ? false : (System.currentTimeMillis() - userSessionToken.getLoginTimestamp() <= Constants.MILLIS_PER_DAY)  ? true : false ;
	}

	public User getUserDetailsFromSessionID(String sessionId){
		UserSession userSessionToken = masterMongoTemplate.findById(sessionId, UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(),User.class);
		return user;

	}

	public List<String> getUserRoles(String sessionId){
		UserSession userSessionToken = masterMongoTemplate.findById(sessionId, UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(),User.class);
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();
		return roles;

	}
	public UIMetadata createUIUXMetadata(UIMetadata metadata){
		UIMetadata uIMetadata = getUIUXMetadata(metadata.getQuery());
		//		if(uIMetadata != null){
		//			uIMetadata.setMetadata(metadata.getMetadata());
		//			uIMetadata.setQuery(metadata.getQuery());
		//			Query query = new Query(Criteria.where("query").is(metadata.getQuery()));
		//			DBObject dbDoc = new BasicDBObject();
		//			masterMongoTemplate.getConverter().write(uIMetadata, dbDoc);
		//			Update update = Update.fromDBObject(dbDoc, "_id");
		//			masterMongoTemplate.updateFirst(query, update, UIMetadata.class);
		//		}
		//		else{
		//			masterMongoTemplate.save(metadata);
		//			uIMetadata = getUIUXMetadata(metadata.getQuery());
		//		}
		return uIMetadata;

	}

	public UIMetadata getUIUXMetadata(String query){
		Query dBquery = new Query(Criteria.where("query").is(query));
		List<UIMetadata> UIMetadata = masterMongoTemplate.find(dBquery, UIMetadata.class);
		if(UIMetadata != null && UIMetadata.size() > 0)
			return UIMetadata.get(0);
		else
			return null;
	}


	public void createPlanPermissions(Plan plan) throws ItorixException {
		if(validatePermission(plan.getUiPermissions())){
			Query dBquery = new Query(Criteria.where("planId").is(plan.getPlanId()));
			Plan dbPlan = masterMongoTemplate.findOne(dBquery, Plan.class);
			if(dbPlan == null )
				masterMongoTemplate.save(plan);
			else{
				dbPlan.setUiPermissions(plan.getUiPermissions());
				masterMongoTemplate.save(dbPlan);
			}
		}else{
			String message = "Invalid request data! Invalid permissions ";
			throw new ItorixException(message,"USER_016");
		}
	}

	private boolean validatePermission(String permission){
		if(permission == null || permission.trim() == ""){
			return false;
		}else{
			try {
				new ObjectMapper().readTree(permission);
				return true;
			} catch(Exception e){
				return false;
			}
		}
	}

	public String getPlanPermissions() throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		Query dBquery = new Query(Criteria.where("planId").is(workspace.getPlanId()));
		Plan dbPlan = masterMongoTemplate.findOne(dBquery, Plan.class);
		if(dbPlan == null || dbPlan.getUiPermissions() == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_028") ,"USER_028");
		}
		return dbPlan.getUiPermissions();
	}

	public Map<String,String> getRoleMapper() throws ItorixException{
		try {
			UIMetadata uiuxMetadata = getUIUXMetadata(UIMetadata.ROLE_MAPPER);
			return uiuxMetadata == null ? null : new ObjectMapper().readValue(uiuxMetadata.getMetadata() , new TypeReference<Map<String, String>>(){});
		} catch (IOException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("IDENTITY-1028"), "IDENTITY-1028");
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

	public List<DBConfig> getDBProperties(){
		return masterMongoTemplate.findAll(DBConfig.class);
	}
	
	
	private ApplicationProperties populateDBApplicationProperties(Map<String,String> map){
		applicationProperties.setApigeeHost(map.get("apigee.host"));
		applicationProperties.setUserName(map.get("app.mailutil.userName"));
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


}