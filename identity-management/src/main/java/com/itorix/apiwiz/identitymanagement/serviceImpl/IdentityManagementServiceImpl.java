package com.itorix.apiwiz.identitymanagement.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.mail.MailProperty;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.dao.RateLimitingDao;
import com.itorix.apiwiz.identitymanagement.dao.WorkspaceDao;
import com.itorix.apiwiz.identitymanagement.model.*;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.identitymanagement.service.IdentityManagmentService;
import com.itorix.apiwiz.ratelimit.model.RateLimitQuota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin // (exposedHeaders = "*")
@RestController
public class IdentityManagementServiceImpl implements IdentityManagmentService {

	@Autowired
	IdentityManagementDao identityManagementDao;
	@Autowired
	protected HttpServletResponse response;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	WorkspaceDao workspaceDao;

	@Autowired(required = false)
	private RateLimitingDao rateLimitingDao;

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/login", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<UserSession> authenticate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		return new ResponseEntity<UserSession>(identityManagementDao.authenticate(userInfo, false), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/login", produces = {"application/json"})
	public ResponseEntity<UserSession> getSession(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid)
			throws ItorixException, Exception {
		return new ResponseEntity<UserSession>(identityManagementDao.findUserSession(jsessionid), HttpStatus.OK);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/register", consumes = {"application/json"})
	public ResponseEntity<Object> register(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestParam(value = "token", required = false) String verificationToken,@RequestParam(value = "appType", required = false) String appType,
			@RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.validateUserFields(userInfo);
		if (verificationToken == null)
			return new ResponseEntity<Object>(identityManagementDao.registerWithMail(userInfo,appType), HttpStatus.CREATED);
		else {
			VerificationToken token = identityManagementDao.getVerificationToken(verificationToken);
			if (token != null)
				if (token.getType().equals("registerUser"))
					return new ResponseEntity<Object>(identityManagementDao.register(userInfo, token),
							HttpStatus.CREATED);
				else
					return new ResponseEntity<Object>(identityManagementDao.registerWithToken(userInfo, token),
							HttpStatus.CREATED);
			else
				throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
		}
	}

	@Override
	public ResponseEntity<?> createSaltMetaData(String interactionid, String xapikey,
			MetaData metadata) throws JsonProcessingException, ItorixException {
		identityManagementDao.createSaltMetaData(metadata);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(useUpdateKey = true )
	public ResponseEntity<?> updateAsServiceAccount(String interactionid, String xapikey, UserInfo userInfo)
			throws JsonProcessingException, ItorixException {
		identityManagementDao.updateAsServiceAccount(userInfo);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSaltMetaData(String interactionid, String xapikey)
			throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(identityManagementDao.getSaltMetaData(),HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/add", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> addUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.addUser(userInfo);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.updateUser(userInfo);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/meta-data", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUserMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Map<String, String> metadata)
			throws ItorixException, Exception {
		identityManagementDao.updateMetaData(metadata);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/meta-data")
	public @ResponseBody ResponseEntity<Object> getUserMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.getMetaData(), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/subscriptions", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUserSubscriptions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.updateUserSubscription(userInfo);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/newsletter/subscription", consumes = {
			"application/json"})
	public @ResponseBody ResponseEntity<Void> updateNewsSubscription(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		if (userInfo.getEmail() != null) {
			identityManagementDao.updateNewsSubscription(userInfo);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{emailId:.+}/newsletter")
	public @ResponseBody ResponseEntity<Object> getNewsSubscription(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@PathVariable(value = "emailId") String emailId) throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.getNewsSubscription(emailId), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{userId}")
	public @ResponseBody ResponseEntity<Object> getUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userId") String userId)
			throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.getUser(userId), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/workspace/seats-counts/{count}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateWorkspaceSeats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "count") long count)
			throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.validateSeats(count), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/resend-invitation")
	public @ResponseBody ResponseEntity<Void> resendInvite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.resendInvite(userInfo);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/tokens/{token}")
	public @ResponseBody ResponseEntity<Object> verifytoken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@PathVariable(value = "token") String token, @RequestParam(value = "type", required = false) String type)
			throws ItorixException, Exception {
		VerificationToken dbToken = identityManagementDao.getVerificationToken(token);
		if (dbToken == null)
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
		return new ResponseEntity<Object>(dbToken, HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/tokens")
	public @ResponseBody ResponseEntity<Object> processToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@RequestParam(value = "type", required = false) String type, @RequestBody VerificationToken token)
			throws ItorixException, Exception {
		VerificationToken dbToken = identityManagementDao.getVerificationToken(token.getId());
		if (dbToken == null)
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
		if (token.getUserEmail() != null && token.getUserEmail() != "")
			if (!dbToken.getUserEmail().equals(token.getUserEmail())) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1036"), "Identity-1036");
			}
		String status = identityManagementDao.VerifyToken(dbToken);
		token.setUserEmail(dbToken.getUserEmail());
		return new ResponseEntity<Object>(token, HttpStatus.OK);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/resend-token")
	public @ResponseBody ResponseEntity<Void> resendToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestParam(value = "appType",required = false) String appType,
			@RequestBody UserInfo userInfo)
			throws ItorixException, Exception {
		identityManagementDao.resendToken(userInfo,appType);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{email:.+}/recover-workspace")
	public @ResponseBody ResponseEntity<Void> recoverWorkspace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "email") String email)
			throws ItorixException, Exception {
		identityManagementDao.recoverWorkspace(email);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/workspace/{workspaceId}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateWorkspace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "workspaceId") String workspaceId)
			throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.validateWorkspace(workspaceId), HttpStatus.OK);
	}

  @UnSecure
  @RequestMapping(method = RequestMethod.GET, value = "/v1/users/workspace/{workspaceId}/check-workspace", produces = {
      "application/json"})
  public @ResponseBody ResponseEntity<Object> checkWorkspaceName(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "workspaceId") String workspaceId)
      throws ItorixException, Exception {
    return new ResponseEntity<Object>(identityManagementDao.checkWorkspace(workspaceId), HttpStatus.OK);
  }

  @UnSecure(useUpdateKey = true)
  @RequestMapping(method = RequestMethod.PUT, value = "/v1/users/workspace/restricted-names", produces = {
      "application/json"})
  public ResponseEntity<Object> restrictedWorkspaceNames(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "x-apikey") String apikey,
      @RequestBody String restrictedNames) {
    identityManagementDao.restrictedWorkspaceNames(restrictedNames);
    return new ResponseEntity<>(HttpStatus.OK);
  }
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/users/validate/{userId}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateUserId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "userId") String userId)
			throws ItorixException, Exception {
		return new ResponseEntity<Object>(identityManagementDao.validateUserId(userId), HttpStatus.OK);
	}

	@UnSecure
	public @ResponseBody ResponseEntity<Void> createUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserDomains userDomains)
			throws ItorixException, Exception {
		identityManagementDao.createUserDomains(userDomains);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@UnSecure
	public @ResponseBody ResponseEntity<Void> updateUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserDomains userDomains)
			throws ItorixException, Exception {
		identityManagementDao.updateUserDomains(userDomains);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@UnSecure
	public @ResponseBody ResponseEntity<Object> getUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getUserDomains(), HttpStatus.OK);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/user/resend-token", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Object> resendUserToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody ResetUserToken userToken)
			throws ItorixException {
		identityManagementDao.resendUserToken(userToken);
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure
	public @ResponseBody ResponseEntity<Void> removeUserSessions(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) {
		identityManagementDao.removeUserSessions();
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/activate", produces = {"application/json"})
	public ResponseEntity<String> verifyRegisteredEmailHash(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestParam("token") String verificationToken,
			@RequestParam(value = "email", required = false) String email) throws ItorixException, Exception {

		VerificationToken token = identityManagementDao.getVerificationToken(verificationToken);
		if (token == null)
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
		String status = identityManagementDao.VerifyToken(token);
		// String status =
		// identityManagementDao.verifyRegisteredEmailHash(user);
		if (status.equalsIgnoreCase("activated")) {
			response.setHeader("Location", applicationProperties.getUserActivationRedirectionLink());
		} else if (status.equalsIgnoreCase("verified")) {
			response.setHeader("Location", applicationProperties.getUserVerifiedRedirectionLink());
		} else if (status.equalsIgnoreCase("resendVerification")) {
			response.setHeader("Location", applicationProperties.getResendVerificationRedirectionLink());
		} else if (status.equalsIgnoreCase("blockUser")) {
			response.setHeader("Location", applicationProperties.getUserBlockingRedirectionLink());
		}
		return new ResponseEntity<String>(status, HttpStatus.FOUND);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{emailId:.+}/reset-password", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> password(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable("emailId") String emailId,
			@RequestParam(value = "reset", required = false) String reset) throws ItorixException {
		if (emailId != null) {
			User user = new User();
			user.setEmail(emailId);
			VerificationToken resetUserToken = identityManagementDao.password(user);
			resetUserToken.setId(null);
			return new ResponseEntity<Object>(resetUserToken, HttpStatus.OK);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1038"), "Identity-1038");
		}
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/reset-password", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> resetPassword(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "forceReset", required = false) boolean forceReset,
			@RequestBody User user) throws Exception {
		String verificationToken = user.getVerificationToken();
		if (user != null && user.getNewPassword() != null ) {
			if(forceReset){
				user.setPassword(user.getNewPassword());
				user = identityManagementDao.updatePasswordWithoutToken(user);
				return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
			} else if (verificationToken != null) {
				VerificationToken dbToken = identityManagementDao.getVerificationToken(verificationToken);
				if (dbToken == null)
					throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1009"), "Identity-1009");
				user.setEmail(dbToken.getUserEmail());
				user.setPassword(user.getNewPassword());
				user = identityManagementDao.updatePassword(user, dbToken);
				return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1040"), "Identity-1040");
			}
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1040"), "Identity-1040");
		}
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/workspace/enable-sso")
	public @ResponseBody ResponseEntity<Void> enableSso(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Workspace workspace)
			throws ItorixException, Exception {
		workspaceDao.enableSso(workspace);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> disableSso(String interactionid, String jsessionid, String workspaceName)
			throws ItorixException, Exception {
		workspaceDao.disableSso(workspaceName);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/connector/smtp")
	@Override
	public ResponseEntity<?> enableSMTPConnector(String interactionid, String jsessionid, MailProperty mailProperty)
			throws ItorixException {
		workspaceDao.updateSMTPConnector(mailProperty);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteSMTPConnector(String interactionid, String jsessionid) throws ItorixException {
		workspaceDao.deleteSMTPConnector();
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> getSMTPConnector(String interactionid, String jsessionid) {
		try {
			Object response = workspaceDao.getSMTPConnector();
			if(response != null)
				return new ResponseEntity<>(response, HttpStatus.OK);
			else return ResponseEntity.ok("{}");
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userId}/remove-workspace")
	public @ResponseBody ResponseEntity<Void> removeWorkspaceUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId)
			throws ItorixException, Exception {
		identityManagementDao.removeWorkspaceUser(userId);
		identityManagementDao.removeUserSession(userId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userId}/set-type")
	public @ResponseBody ResponseEntity<Void> addSiteAdmin(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId)
			throws ItorixException, Exception {
		identityManagementDao.addSiteAdmin(userId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/{userId}/roles")
	public @ResponseBody ResponseEntity<Void> updateWorkspaceUserRoles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId,
			@RequestBody UserInfo userInfo) throws ItorixException, Exception {
		if (userInfo != null && userInfo.getRoles() != null)
			identityManagementDao.updateWorkspaceUserRoles(userId, userInfo.getRoles());
		else
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1040"), "Identity-1040");
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> usersList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "filterbynames", required = false) String filterbynames) throws Exception {
		Object users = identityManagementDao.listUsers(filterbynames);
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/invited", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> invitedUsersList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object users = identityManagementDao.listUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/users/logout", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> logOut(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException {
		return new ResponseEntity<Object>(identityManagementDao.logOut(jsessionid), HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/activitylog", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> getActivityLogDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeRange") String timeRange, @RequestParam(value = "offset") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "userId", required = false) String userId) throws ItorixException, ParseException {

		return new ResponseEntity<Object>(
				identityManagementDao.getActivityLogDetails(jsessionid, timeRange, userId, offset, pageSize),
				HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/change-password", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> changePassword(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody User user,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		identityManagementDao.changePassword(user);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{userid}/teams", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> getUserTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getTeams(userId, false), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userid}/lock", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> lockUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception {
		identityManagementDao.lockUser(userId);
		identityManagementDao.removeUserSession(userId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userid}/unlock", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> unlockUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception {
		identityManagementDao.unLockUser(userId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/{userid}/teams", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> getUserTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId,
			@RequestBody List<SwaggerTeam> teams) throws Exception {
		identityManagementDao.updateUserTeams(teams, userId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, value = "/v1/uiux/dropdowns", produces = {"application/json"})
	public ResponseEntity<Object> createUIMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "q") String q,
			@RequestBody String metadata) throws Exception {
		UIMetadata uIMetadata = new UIMetadata(q, metadata);
		identityManagementDao.createUIUXMetadata(uIMetadata);
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/uiux/dropdowns", produces = {"application/json"})
	public ResponseEntity<Object> getUIMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "q") String q)
			throws Exception {
		UIMetadata uIMetadata = identityManagementDao.getUIUXMetadata(q);
		return new ResponseEntity<Object>(uIMetadata.getMetadata(), HttpStatus.OK);
	}

	@UnSecure(useUpdateKey = true)
	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/permissions", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createPlanPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestHeader(value = "x-planid") String planid,
			@RequestBody String permissions) throws Exception {
		Plan plan = new Plan();
		plan.setPlanId(planid);
		plan.setUiPermissions(permissions);
		identityManagementDao.createPlanPermissions(plan);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/permissions", produces = {"application/json"})
	public ResponseEntity<Object> getPlanPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getPlanPermissions(), HttpStatus.OK);
	}

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/subscriptionplans", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createSubscriptionPlans(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody List<Subscription> subscriptions)
			throws Exception {
		workspaceDao.createSubscriptionPlans(subscriptions);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/subscriptionplans", produces = {"application/json"})
	public ResponseEntity<Object> getSubscriptionPlans(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception {
		return new ResponseEntity<Object>(workspaceDao.getSubscriptions(), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/details", produces = {"application/json"})
	public ResponseEntity<Object> getSessionDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-sourcetype", required = false) String appId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getUserSessionDetails(jsessionid, appId),
				HttpStatus.OK);
	}

	@Override
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/sessions/validate")
	public @ResponseBody ResponseEntity<Object> validateUserSession(
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		Map isValid = new HashMap();
		isValid.put("isValid", identityManagementDao.validateSession(jsessionid));
		return new ResponseEntity<Object>(isValid, HttpStatus.OK);
	}

	@Override
	@UnSecure
	public @ResponseBody ResponseEntity<Object> getRoles(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getRoles(), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/jwks")
	public @ResponseBody ResponseEntity<Object> getPublicKey(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@RequestHeader(value = "x-source") String source, @RequestHeader(value = "x-tenant") String tenant)
			throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<Object>(workspaceDao.getPublicKey(tenant, source), headers, HttpStatus.OK);
	}

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/jwks")
	@Override
	public ResponseEntity<Object> updatePublicKey(String jsessionid, String interactionid, String apikey, String source,
			String tenant, String key) throws Exception {
		workspaceDao.updatePublicKey(tenant, source, key);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/accounts/webhooks")
	public @ResponseBody ResponseEntity<Void> accountWebhook(@RequestHeader(value = "signature") String signature,
			@RequestBody SubscriptionEvent subscriptionEvent) throws Exception {
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/static-content/{source}")
	public @ResponseBody ResponseEntity<Void> createLandingData(@RequestHeader(value = "x-apikey") String apikey,
			@PathVariable(value = "source") String source, @RequestBody String data) throws Exception {
		workspaceDao.createLandingData(source, data);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/static-content/{source}")
	public @ResponseBody ResponseEntity<Object> getLandingData(@RequestHeader(value = "x-apikey") String apikey,
			@PathVariable(value = "source") String source) throws Exception {
		return new ResponseEntity<Object>(workspaceDao.getLandingData(source), HttpStatus.OK);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/update-plan")
	public @ResponseBody ResponseEntity<Void> updateSubscription(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody UserInfo userInfo) throws Exception {
		if (userInfo.allowChangeSubscription()) {
			Workspace workspace = new Workspace();
			workspace.setName(userInfo.getWorkspaceId());
			workspace.setPlanId(userInfo.getPlanId());
			workspace.setSubscriptionId(userInfo.getSubscriptionId());
			workspace.setPaymentSchedule(userInfo.getPaymentSchedule());
			workspaceDao.updateWorkspaceSubscription(workspace);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/upgrade-seats")
	public @ResponseBody ResponseEntity<Void> updateSubscriptionSeats(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Workspace workspace) throws Exception {
		workspaceDao.addSeats(workspace.getSeats());
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/downgrade-seats")
	public @ResponseBody ResponseEntity<Void> downgradeSubscriptionSeats(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Workspace workspace) throws Exception {
		workspaceDao.removeSeats(workspace.getSeats());
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/accounts-cancellation")
	public @ResponseBody ResponseEntity<Void> cancelSubscription(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CancelSubscriptions subscription) throws Exception {
		workspaceDao.updateWorkspaceStatus(subscription.getStatus());
		identityManagementDao.cancelSubscription(subscription);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace")
	public @ResponseBody ResponseEntity<Void> updateWorkspaceStatus(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody UserInfo userInfo) throws Exception {
		workspaceDao.updateWorkspaceStatus(userInfo.getWorkspaceId(), userInfo.getStatus());
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/account")
	public @ResponseBody ResponseEntity<Void> updateUserAccount(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CancelSubscriptions subscription) throws Exception {
		identityManagementDao.cancelSubscription(subscription);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/accounts-delete")
	public @ResponseBody ResponseEntity<Void> cancelUserAccount(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CancelSubscriptions subscription) throws Exception {
		identityManagementDao.cancelSubscription(subscription);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Void> createOrUpdateDBProperties(@RequestHeader(value = "x-apikey") String apikey,
			@RequestBody DBConfig dbConfig) throws Exception {
		identityManagementDao.createOrUpdateDBProperties(dbConfig);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Object> getDBProperties(@RequestHeader(value = "x-apikey") String apikey) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getDBProperties(), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public @ResponseBody ResponseEntity<Object> health(
			@RequestHeader(value = "x-apikey", required = false) String apikey) throws Exception {
		return new ResponseEntity<Object>("ok", HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Object> createVideoCategories(String data) throws Exception {
		workspaceDao.createLandingData("video-category", data);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public @ResponseBody ResponseEntity<Object> getVideoCategories() throws Exception {
		return new ResponseEntity<Object>(workspaceDao.getLandingData("video-category"), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Object> createVideos(String data) throws Exception {
		workspaceDao.createLandingData("videos", data);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public @ResponseBody ResponseEntity<Object> getVideos(String category) throws Exception {
		return new ResponseEntity<>(workspaceDao.getVideos(category), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Object> getIdpMetadata(String workspaceId) throws Exception {
		return new ResponseEntity<>(workspaceDao.getIdpMetadata(workspaceId), HttpStatus.OK);
	}

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v2/users/subscriptionplans", produces = {"application/json"})
	public ResponseEntity<Object> getSubscriptionPlansV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception {
		return new ResponseEntity<Object>(workspaceDao.getSubscriptionsV2(), HttpStatus.OK);
	}

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v2/users/subscriptionplans", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createSubscriptionPlansV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody List<SubscriptionV2> subscriptions)
			throws Exception {
		workspaceDao.createSubscriptionPlansV2(subscriptions);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v2/users/permissions", produces = {"application/json"})
	public ResponseEntity<Object> getPlanPermissionsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "planId", required = false) String planId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getPlanPermissionsV2(planId), HttpStatus.OK);
	}

	@UnSecure(useUpdateKey = true)
	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v2/users/permissions", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createPlanPermissionsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestHeader(value = "x-planid") String planid,
			@RequestBody String permissions) throws Exception {
		PlanV2 plan = new PlanV2();
		plan.setPlanId(planid);
		plan.setUiPermissions(permissions);
		identityManagementDao.createPlanPermissionsV2(plan);
		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/app/menu", produces = {"application/json"})
	public ResponseEntity<Object> getMenu(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception {
		return new ResponseEntity<Object>(identityManagementDao.getMenu(), HttpStatus.OK);
	}

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/app/menu", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createMenu(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody String menu )
			throws Exception {
		Menu newMenu = new Menu();
		newMenu.setMenus(menu);
		identityManagementDao.createMenu(newMenu);
		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/rate-limit/quotas/tenant", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> addTenantQuotas(
			@RequestHeader(value = "x-apikey", required = true) String apikey,
			@RequestHeader(value = "x-tenant", required = true) String tenantId,
			@RequestBody RateLimitQuota quota) throws ItorixException{
		if (rateLimitingDao != null) {
			rateLimitingDao.addTenantQuotas(tenantId, quota);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Rate limit is disabled", HttpStatus.FORBIDDEN);
		}
	}

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/rate-limit/quotas/master", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> addMasterQuotas(
			@RequestHeader(value = "x-apikey", required = true) String apikey,
			@RequestBody List<RateLimitQuota> quotas) throws ItorixException {
		if (rateLimitingDao != null) {
			rateLimitingDao.addMasterQuotas(quotas);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Rate limit is disabled", HttpStatus.FORBIDDEN);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/rate-limit/usage", produces = {"application/json"})
	public ResponseEntity<?> getApplicationUsage(
			@RequestHeader(value = "JSESSIONID", required = true) String jsessionid
	) {
		if (rateLimitingDao != null) {
			return new ResponseEntity<>(rateLimitingDao.getApplicationUsage(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Rate limit is disabled", HttpStatus.FORBIDDEN);
		}
	}
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> createRolesMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody String metadata)
			throws JsonProcessingException, ItorixException {
		identityManagementDao.createRolesMetaData(metadata);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(useUpdateKey = true)
	public ResponseEntity<?> updateSocialLoginEnabledStatus(String apikey, MetaData metadata)
			throws ItorixException {
		if(metadata.getKey().equalsIgnoreCase("social-logins")){
			String providers = metadata.getMetadata();
			if(providers != null){
				identityManagementDao.updateSocialLoginEnabledStatus(providers);
				return new ResponseEntity<>("Social Login Providers Successfully Updated",HttpStatus.OK);
			}
			return new ResponseEntity<>("Social Login Providers Not Passed",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Invalid Key Name",HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> syncClientData(String interactionid, String jsessionid)
			throws JsonProcessingException, ItorixException {
		return identityManagementDao.syncClientData();
	}
}

