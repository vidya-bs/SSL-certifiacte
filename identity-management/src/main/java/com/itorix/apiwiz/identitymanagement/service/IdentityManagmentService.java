package com.itorix.apiwiz.identitymanagement.service;

import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.mail.MailProperty;
import com.itorix.apiwiz.identitymanagement.model.*;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public interface IdentityManagmentService {

	@ApiOperation(value = "userLogin", notes = "", code = 200, response = UserSession.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Login Sucessful", response = UserSession.class),
			@ApiResponse(code = 400, message = "Sorry! The username and password entered didn't match.", response = ErrorObj.class),
			@ApiResponse(code = 400, message = "User account validation failed. The account is locked due to incorrect login attempts. Please contact your workspace admin.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/login", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<UserSession> authenticate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/login", produces = {"application/json"})
	public @ResponseBody ResponseEntity<UserSession> getSession(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid) throws ItorixException, Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/register", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Object> register(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestParam(value = "token", required = false) String verificationToken, @RequestParam(value = "appType", required = false) String appType,
			@RequestBody UserInfo userInfo)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/add", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> addUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/meta-data", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUserMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Map<String, String> metadata)
			throws ItorixException, Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/meta-data")
	public @ResponseBody ResponseEntity<Object> getUserMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException, Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/subscriptions", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUserSubscriptions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/newsletter/subscription", consumes = {
			"application/json"})
	public @ResponseBody ResponseEntity<Void> updateNewsSubscription(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{emailId:.+}/newsletter")
	public @ResponseBody ResponseEntity<Object> getNewsSubscription(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@PathVariable(value = "emailId") String emailId) throws ItorixException, Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{userId}")
	public @ResponseBody ResponseEntity<Object> getUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userId") String userId)
			throws ItorixException, Exception;

//	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/workspace/seats-counts/{count}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateWorkspaceSeats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "count") long count)
			throws ItorixException, Exception;


	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/resend-invitation")
	public @ResponseBody ResponseEntity<Void> resendInvite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/tokens/{token}")
	public @ResponseBody ResponseEntity<Object> verifytoken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@PathVariable(value = "token") String token, @RequestParam(value = "type", required = false) String type)
			throws ItorixException, Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/tokens")
	public @ResponseBody ResponseEntity<Object> processToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@RequestParam(value = "type", required = false) String type, @RequestBody VerificationToken token)
			throws ItorixException, Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/resend-token")
	public @ResponseBody ResponseEntity<Void> resendToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserInfo userInfo)
			throws ItorixException, Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{email:.+}/recover-workspace")
	public @ResponseBody ResponseEntity<Void> recoverWorkspace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "email") String email)
			throws ItorixException, Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/workspace/{workspaceId}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateWorkspace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "workspaceId") String workspaceId)
			throws ItorixException, Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/users/validate/{userId}", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> validateUserId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable(value = "userId") String userId)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/workspace/enable-sso")
	public @ResponseBody ResponseEntity<Void> enableSso(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Workspace workspace)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/disable-sso/{workspaceName}")
	public @ResponseBody ResponseEntity<Void> disableSso(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable String workspaceName)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/connector/smtp")
	public @ResponseBody ResponseEntity<?> enableSMTPConnector(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody MailProperty mailProperty)
			throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/connector/smtp")
	public @ResponseBody ResponseEntity<?> deleteSMTPConnector(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/connector/smtp")
	public @ResponseBody ResponseEntity<?> getSMTPConnector(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userId}/remove-workspace")
	public @ResponseBody ResponseEntity<Void> removeWorkspaceUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userId}/set-type")
	public @ResponseBody ResponseEntity<Void> addSiteAdmin(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId)
			throws ItorixException, Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/{userId}/roles")
	public @ResponseBody ResponseEntity<Void> updateWorkspaceUserRoles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("userId") String userId,
			@RequestBody UserInfo userInfo) throws ItorixException, Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/activate", produces = {"application/json"})
	public ResponseEntity<String> verifyRegisteredEmailHash(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestParam("token") String verificationToken,
			@RequestParam(value = "email", required = false) String email) throws ItorixException, Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/domains", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> createUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserDomains userDomains)
			throws ItorixException, Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/domains", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody UserDomains userDomains) throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/domains", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> getUserDomains(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/user/resend-token", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> resendUserToken(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody ResetUserToken userToken)
			throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/users/sessions")
	public @ResponseBody ResponseEntity<Void> removeUserSessions(@RequestHeader(value = "JSESSIONID") String jsessionid,
																 @RequestHeader(value = "interactionid", required = false) String interactionid,
																 @RequestHeader(value = "x-apikey") String apikey);

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{emailId:.+}/reset-password", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> password(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @PathVariable("emailId") String emailId,
			@RequestParam(value = "reset", required = false) String reset) throws ItorixException;

	@UnSecure
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/reset-password", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> resetPassword(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody User user) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/users", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> usersList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "filterbynames", required = false) String filterbynames) throws Exception;

//	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/invited", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> invitedUsersList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/users/logout", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> logOut(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/activitylog", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> getActivityLogDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeRange") String timeRange, @RequestParam(value = "offset") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "userId", required = false) String userId) throws ItorixException, ParseException;


	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/change-password", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> changePassword(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody User user,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/{userid}/teams", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Object> getUserTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userid}/lock", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> lockUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/{userid}/unlock", produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> unlockUser(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/{userid}/teams", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> getUserTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "userid") String userId,
			@RequestBody List<SwaggerTeam> teams) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/uiux/dropdowns", produces = {"application/json"})
	public ResponseEntity<Object> createUIMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "q") String q,
			@RequestBody String metadata) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/uiux/dropdowns", produces = {"application/json"})
	public ResponseEntity<Object> getUIMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "q") String q)
			throws Exception;


	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/permissions", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createPlanPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestHeader(value = "x-planid") String planid,
			@RequestBody String permissions) throws Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/permissions", produces = {"application/json"})
	public ResponseEntity<Object> getPlanPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/subscriptionplans", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createSubscriptionPlans(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody List<Subscription> subscriptions)
			throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/subscriptionplans", produces = {"application/json"})
	public ResponseEntity<Object> getSubscriptionPlans(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/details", produces = {"application/json"})
	public ResponseEntity<Object> getSessionDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-sourcetype", required = false) String appId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/sessions/validate")
	public @ResponseBody ResponseEntity<Object> validateUserSession(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/roles")
	public @ResponseBody ResponseEntity<Object> getRoles(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/update-plan")
	public @ResponseBody ResponseEntity<Void> updateSubscription(@RequestHeader(value = "JSESSIONID") String jsessionid,
																 @RequestHeader(value = "interactionid", required = false) String interactionid,
																 @RequestBody UserInfo userInfo) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/upgrade-seats")
	public @ResponseBody ResponseEntity<Void> updateSubscriptionSeats(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Workspace workspace) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace/downgrade-seats")
	public @ResponseBody ResponseEntity<Void> downgradeSubscriptionSeats(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Workspace workspace) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/accounts-cancellation")
	public @ResponseBody ResponseEntity<Void> cancelSubscription(@RequestHeader(value = "JSESSIONID") String jsessionid,
																 @RequestHeader(value = "interactionid", required = false) String interactionid,
																 @RequestBody CancelSubscriptions subscription) throws Exception;

	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/workspace")
	public @ResponseBody ResponseEntity<Void> updateWorkspaceStatus(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody UserInfo userInfo) throws Exception;

	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/account")
	public @ResponseBody ResponseEntity<Void> updateUserAccount(@RequestHeader(value = "JSESSIONID") String jsessionid,
																@RequestHeader(value = "interactionid", required = false) String interactionid,
																@RequestBody CancelSubscriptions subscription) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/users/accounts-delete")
	public @ResponseBody ResponseEntity<Void> cancelUserAccount(@RequestHeader(value = "JSESSIONID") String jsessionid,
																@RequestHeader(value = "interactionid", required = false) String interactionid,
																@RequestBody CancelSubscriptions subscription) throws Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/jwks")
	public @ResponseBody ResponseEntity<Object> getPublicKey(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@RequestHeader(value = "x-source") String source, @RequestHeader(value = "x-tenant") String tenant)
			throws Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/users/jwks")
	public @ResponseBody ResponseEntity<Object> updatePublicKey(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey", required = false) String apikey,
			@RequestHeader(value = "x-source") String source, @RequestHeader(value = "x-tenant") String tenant,
			@RequestBody(required = true) String key) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/accounts/webhooks")
	public @ResponseBody ResponseEntity<Void> accountWebhook(@RequestHeader(value = "signature") String signature,
															 @RequestBody SubscriptionEvent subscriptionEvent) throws Exception;


	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/users/static-content/{source}")
	public @ResponseBody ResponseEntity<Void> createLandingData(@RequestHeader(value = "x-apikey") String apikey,
																@PathVariable(value = "source") String source, @RequestBody String data) throws Exception;


	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/users/static-content/{source}")
	public @ResponseBody ResponseEntity<Object> getLandingData(@RequestHeader(value = "x-apikey") String apikey,
															   @PathVariable(value = "source") String source) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config-property", consumes = "application/json")
	public ResponseEntity<Void> createOrUpdateDBProperties(@RequestHeader(value = "x-apikey") String apikey,
														   @RequestBody DBConfig dbConfig) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config-property")
	public ResponseEntity<Object> getDBProperties(@RequestHeader(value = "x-apikey") String apikey) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/health")
	public @ResponseBody ResponseEntity<Object> health(
			@RequestHeader(value = "x-apikey", required = false) String apikey) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/itorix-resources/videos/category")
	public @ResponseBody ResponseEntity<Object> createVideoCategories(@RequestBody String data) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/itorix-resources/videos/category")
	public @ResponseBody ResponseEntity<Object> getVideoCategories() throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/itorix-resources/videos")
	public @ResponseBody ResponseEntity<Object> createVideos(@RequestBody String data) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/itorix-resources/videos")
	public @ResponseBody ResponseEntity<Object> getVideos(@RequestParam(required = false) String category)
			throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/saml/idpMetadata/{workspaceId}")
	public @ResponseBody ResponseEntity<Object> getIdpMetadata(@PathVariable(required = true) String workspaceId)
			throws Exception;

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v2/users/permissions", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createPlanPermissionsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestHeader(value = "x-planid") String planid,
			@RequestBody String permissions) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v2/users/permissions", produces = {"application/json"})
	public ResponseEntity<Object> getPlanPermissionsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "planId", required = false) String planId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v2/users/subscriptionplans", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createSubscriptionPlansV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody List<SubscriptionV2> subscriptions)
			throws Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v2/users/subscriptionplans", produces = {"application/json"})
	public ResponseEntity<Object> getSubscriptionPlansV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/app/menu", produces = {"application/json"})
	public ResponseEntity<Object> getMenu(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey) throws Exception;

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/app/menu", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Void> createMenu(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody String menu)
			throws Exception;
}
