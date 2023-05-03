package com.itorix.apiwiz.common.model.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {
	/*
	 * Swagger1001("Couldn't find the swagger by name %s."),
	 * Swagger1002("Couldn't find the swagger version number %s for %s."),
	 * Swagger1003("Swagger by name {swaggerName} already exists."),
	 * Swagger1004("Please pass the XSD file for which the xpath needs to be generated."
	 * ),
	 * Swagger1005("Please pass the element name for which xpath needs to be generated."
	 * ),
	 * Swagger1006("Please pass the XSD file for which the swagger definitions needs to be generated."
	 * ),
	 * Swagger1007("The Xpath file is in correct. Please correct and retry again."
	 * ),
	 */

	Xpath1001("Resource not found. Couldn't find the Xpath by name %s."),
	USER_001("Request validation failed. Invalid Credentials"),
	USER_002("Resource authorization validation failed. Account Locked"),
	USER_003("Request validation failed. Please enter the registered mail id"),
	USER_004("Request validation failed. Please enter the correct password"),
	USER_006("Resource not found. User not found"),
	USER_007("Resource authorization validation failed. User doesn't have privilege"),
	USER_005("Request validation failed. User already Exists"),
	USER_010("Request validation failed. workspace Id already in use"),
	Swagger1009("Request validation failed. Swagger Team name is already available"),
	CHECK_MANDATORY_DATA_FIELDS("Request validation failed. Please check the mandatory data fields and retry again."),
	PROXY_NOT_FOUND("Request validation failed. Resource or proxy name does not exist."),
	APIGEE_CONNECTION_FAILURE("Request validation failed. Could not connect to apigee connector."),
	GENERIC_RESOURCE_NOT_FOUND("Resource not found. Please check the request and retry again."),
	GENERIC_RESOURCE_ALREADY_EXISTS("Request validation failed. Resource already exist."),
	GENERIC_RESOURCE_AUTH_VALIDATION_FAILED("Resource authorization validation failed. Please contact your workspace admin."),
	GENERIC_INTERNAL_SERVER_ERROR("Internal server error. Please contact support for further instructions."),
	INVALID_SESSION_TOKEN("Request Validation Failed. Invalid Session Token"),
	SOCIAL_LOGIN_PROVISION_ERROR("Unable to find/provision user. Workspace Limit may have reached its capacity. Please contact support for further instructions."),
	SOCIAL_LOGIN_PROFILE_EMAIL_PRIVATE("Profile Email is private. Please update to public and try again.");

	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("ProxyGen-1000", CHECK_MANDATORY_DATA_FIELDS.message());
			put("ProxyGen-1001", PROXY_NOT_FOUND.message());
			put("ProxyGen-1002", APIGEE_CONNECTION_FAILURE.message());
			put("ProxyGen-1003", PROXY_NOT_FOUND.message());
			put("ProxyGen-1004", "Request validation failed. Resource or proxy name already exist.");
			put("ProxyGen-1005", "Request validation failed. Source control connector configuration is invalid.");


			put("Portal-1000", "Request validation failed. Invalid data");
			put("Portal-1001", "Request validation failed. Resource trying to retrieve does not exist");
			put("Configuration-1000", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1001", PROXY_NOT_FOUND.message());
			put("Configuration-1002", APIGEE_CONNECTION_FAILURE.message());
			put("Configuration-1003", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Configuration-1004", "Request validation failed. Resource or proxy name already exist.");
			put("Configuration-1005", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1006", APIGEE_CONNECTION_FAILURE.message());
			put("Configuration-1007", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Configuration-1008", "Request validation failed. Value for type can be KVM, Cache or TargetServer.");
			put("Configuration-1009", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Configuration-1010", "Request validation failed. Resource with status approved cannot be updated.");
			put("Configuration-1011", "Request validation failed. Cache name %cachename for Org %orgname and Env %env already exists.");
			put("Configuration-1012", "Resource not found. Cache name %cachename for Org %orgname and Env %env doesn't exists.");
			put("Configuration-1013", "Resource not found. Cache %cachename doesn't exists.");
			put("Configuration-1014", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1015", APIGEE_CONNECTION_FAILURE.message());
			put("Configuration-1016", "Request validation failed. KVM name %KVMName for Org %orgname and Env %env already exists.");
			put("Configuration-1017", "Resource not found. KVM name %KVMname for Org %orgname and Env %env doesn't exists.");
			put("Configuration-1018", "Resource not found. KVM name %KVMname doesn't exist.");
			put("Configuration-1019", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1020", APIGEE_CONNECTION_FAILURE.message());
			put("Configuration-1021", "Request validation failed. Target name %TargetName for Org %orgname and Env %env already exists.");
			put("Configuration-1022", "Resource not found. Target name %Targetname for Org %orgname and Env %env doesn't exists.");
			put("Configuration-1023", "Resource not found. Target name %Targetname doesn't exist.");
			put("Configuration-1024", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1025", APIGEE_CONNECTION_FAILURE.message());
			put("Configuration-1026", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Configuration-1027", "Request validation failed. Value for type can be KVM, Cache or TargetServer.");
			put("Configuration-1028", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Configuration-1029", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Configuration-1030", "Request validation failed. Approval type provided doesn't match the enum type.");
			put("Configuration-1031", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Configuration-1032", "Resource status cannot be updated.");
			put("Configuration-1033","This entity cannot be edited once published.");
			put("Configuration-1034", "Request validation failed.Approved resources cannot be deleted.");
			put("Configuration-1035", "Request validation failed.More than ten custom attributes are not allowed.");

			put("USER_012", "Request validation failed. UserDomain Already Exists.");
			put("USER_013", "Resource not found. UserDomain Doesn't Exists.");
			put("USER_004", "Request validation failed. The old password entered doesn't match the records.");
			put("USER_005", "Request validation failed. User already Exists.");
			put("USER_008", "Request validation failed. Incorrect time range.");

			put("Identity-1000", "Request validation failed. Could not validate Email-Id");
			put("Identity-1001", "User account validation failed. The account is locked due to incorrect login attempts. Please contact your workspace admin.");
			put("Identity-1002", "Resource not found. No records found for the search criteria.");
			put("Identity-1003", "User account validation failed. Session-Id is expired.");
			put("Identity-1004", "User account validation failed. Domain name %s is not allowed.");
			put("Identity-1005", "User account validation failed. Please contact your workspace admin to activate your account.");
			put("Identity-1006", "User account validation failed. Please contact your workspace admin to unlock your access.");
			put("Identity-1007", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Identity-1008", "Resource validation failed. Workspace you are trying to register already exists.");
			put("Identity-1009", "User account validation failed. One-time account token is invalid. Please contact your workspace admin.");
			put("Identity-1010", "Request validation failed. Invalid login-id or email-id provided.");
			put("Identity-1011", "Request validation failed. Workspace doesn't exist.");
			put("Identity-1012", "Resource authorization validation failed. Account user is not a admin to perform this operation.");
			put("Identity-1013", "User account validation failed. Workspace is not active.");
			put("Identity-1014", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1015", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1016", "User account validation failed. User account is locked. Please contact workspace admin.");
			put("Identity-1017", "Internal server error. Please contact support for further instructions. ");
			put("Identity-1018", "User account validation failed. Session-Id is expired.");
			put("Identity-1019", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1020", "Request authentication validation failed. API Key is invalid.");
			put("Identity-1021", "Resource validation failed. Username or login-id you are trying to register already exists.");
			put("Identity-1022", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1023", "Resource not found. Email-Id provided in the request doesn't exist.");
			put("Identity-1024", "User account validation failed. Invalid login credentials.");
			put("Identity-1025", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Identity-1026", INVALID_SESSION_TOKEN.message());
			put("Identity-1027", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Identity-1028", INVALID_SESSION_TOKEN.message());
			put("Identity-1029", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1030", "Request validation failed. Invalid SAML configurations.");
			put("Identity-1031", "Resource not found. Login-Id or email-Id provided in the request doesn't exist.");
			put("Identity-1032", INVALID_SESSION_TOKEN.message());
			put("Identity-1033", "Request validation failed. Invalid SAML configurations. Missing SAML metadata URL or metadata file.");
			put("Identity-1034", "Request validation failed. Invalid SAML configurations.");
			put("Identity-1035", "Request validation failed. Invalid SAML configurations. Missing SAML metadata URL or metadata file.");
			put("Identity-1036", "User account validation failed. Invalid login credentials.");
			put("Identity-1037", "Request validation failed. Invalid ldap configurations.");
			put("Identity-1038", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Identity-1039", "Request validation failed. Invalid ldap configurations.");
			put("Identity-1040", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Identity-1041", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Identity-1042", "User account validation failed. Trial period expired. Please contact support for further instructions.");
			put("Identity-1043", GENERIC_RESOURCE_AUTH_VALIDATION_FAILED.message());
			put("Identity-1044", "Request validation failed. Invalid workspace details provided.");
			put("Identity-1045", "Request validation failed. The user is not mapped to the workspace provided.");
			put("Identity-1046", "Request validation failed. The user account is locked out of the workspace.");
			put("Identity-1047", "User account validation failed. The user account is locked.");
			put("Identity-1048","User account validation failed. The user account is not Active.");
			put("Identity-1049","User account validation failed. This email id is already associated with another account on this workspace.");
			put("Identity-1050","User account validation failed. Password is expired");

			put("Connector-1000", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Connector-1001", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Connector-1002", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Connector-1003", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Connector-1004", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Connector-1005", GENERIC_RESOURCE_NOT_FOUND.message());

			put("DataBackup-1000", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("DataBackup-1001", GENERIC_RESOURCE_NOT_FOUND.message());

			put("CodeCoverage-1000", GENERIC_RESOURCE_NOT_FOUND.message());
			put("CodeCoverage-1001", CHECK_MANDATORY_DATA_FIELDS.message());
			put("PolicyPerformance-1000", GENERIC_RESOURCE_NOT_FOUND.message());
			put("PolicyPerformance-1001", CHECK_MANDATORY_DATA_FIELDS.message());

			put("Swagger-1000", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Swagger-1001", "Resource not found. No records found for selected swagger name - %s with following revision - %s.");
			put("Swagger-1002", "Request validation failed. Swagger name - %s already exists.");
			put("Swagger-1003", "Request validation failed. Missing XSD document.");
			put("Swagger-1004", "Request validation failed. Missing XSD element name.");
			put("Swagger-1005", "Request validation failed. Invalid swagger file imported.");
			put("Swagger-1006", "Request validation failed. Invalid xpath generator file.");
			put("Swagger-1007", "Request validation failed. Invalid xpath entry - %s.");
			put("Swagger-1008", "Request validation failed. Malformed URL Provided for Server Host - %s.");
			put("Swagger-1009", "Request validation failed. Incorrect OAS version. Please check the OAS version");
			put("Swagger-1010", "Request validation failed. File size should not be greater than 25MB");
			put("Swagger-1011", "Failed to import swagger. Swagger with same name already exists in a different team.");

			put("Teams-1000", "Request validation failed. Team name already exists.");
			put("Teams-1001", "Resource not found. No records found for selected team name - %s.");
			put("Teams-1002", "Resource authorization validation failed. You don't have enough permissions on this team - %s.");

			put("General-1000", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("General-1001", "Request validation failed. Please correct the request and try again.");

			put("Apigee-1000", "Request validation failed. Invalid apigee organization.");
			put("Apigee-1001", "Request validation failed. Invalid apigee environment.");
			put("Apigee-1002", "Network Error. The Apigee connection timed out.");
			put("Apigee-1003", "Resource authorization validation failed. Apigee user is unauthorized.");
			put("Apigee-1004", "Request validation failed. Pipeline name already exists.");
			put("Apigee-1005", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Apigee-1006", "Resource authorization validation failed. Insufficient permissions to carry out this task.");
			put("Apigee-1007", "Request validation failed. There is no apigee credentials defined for the logged in user.");

			put("CICD-1000", "Request validation failed. Project names can only contain following special character '-'.");
			put("CICD-1001", "Request validation failed. Pipeline name already exists.");
			put("CICD-1002", GENERIC_RESOURCE_NOT_FOUND.message());
			put("CICD-1003", "Error while creating pipeline, %s");
			put("CICD_006", "Request validation failed. BackUpInterval must be of type Daily,Monthly or Weekly.");
			put("CI-CD-GBTA500","Runtime logs error %s");

			put("ServiceRegistry-1000", "Resource not found. No records found for the given data %s.");
			put("ServiceRegistry-1001", "Request validation failed. Record with value %s already exists.");
			put("ServiceRegistry-1002", GENERIC_RESOURCE_ALREADY_EXISTS.message());

			put("Testsuite-1000", "Request validation failed. Testsuite execution is already in progress.");
			put("Testsuite-1001", "Request validation failed. Cannot trigger a testsuite with status paused.");
			put("Testsuite-1002", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1003", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1004", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Testsuite-1005", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Testsuite-1006", "Request validation failed. Please select a valid testsuite in running status to cancel.");
			put("Testsuite-1007", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1008", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Testsuite-1009", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Testsuite-1010", "Request validation failed. Not a valid certificate document.");
			put("Testsuite-1011", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1012", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Testsuite-1013", "Request validation failed. Certificate can't be deleted as it is referred in testsuite(s) %s.");
			put("Testsuite-1014", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1015", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Testsuite-1016", "Request validation failed. Cannot trigger testsuite.");

			put("Portfolio-1000", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Portfolio-1001", "Resource not found. No records found for the given portfolio - %s.");
			put("Portfolio-1002", "Request validation failed. Portfolio - %s already exist.");
			put("Portfolio-1003", "Resource not found. No records found for the given portfolio - %s.");
			put("Portfolio-1004", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1005", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1006", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1007", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Portfolio-1008", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Portfolio-1009", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Portfolio-1010", CHECK_MANDATORY_DATA_FIELDS.message());
			put("Portfolio-1011", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1012", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1013", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1014", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Portfolio-1015", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Portfolio-1016", GENERIC_INTERNAL_SERVER_ERROR.message());

			put("Monitor-1000", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Monitor-1001", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Monitor-1002", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Monitor-1003", GENERIC_RESOURCE_NOT_FOUND.message());
			put("Monitor-1004", "Request validation failed. Certificate document is invalid.");
			put("Monitor-1005", GENERIC_RESOURCE_ALREADY_EXISTS.message());
			put("Monitor-1006", GENERIC_RESOURCE_ALREADY_EXISTS.message());

			put("MockServer-1000", "Request validation failed. A mock scenario with similar configuration already exists.");
			put("MockServer-1001", GENERIC_RESOURCE_NOT_FOUND.message());
			put("MockServer-1002", GENERIC_RESOURCE_NOT_FOUND.message());
			put("MockServer-1003", "Request validation failed. A group with that name already exists.");

			put("Marketing-1000", GENERIC_INTERNAL_SERVER_ERROR.message());
			put("Marketing-1001","Please give publishing Date in the format dd-MM-yyyy");

			put("PROJECT_PLAN_TRACK_001", "Request validation failed. Project already exist");
			put("ProjectPlan-1001", "Resource not found. Project with name %s does not exists.");
			put("ProjectPlan-1006", "Resource not found. Project and proxy  combination does not exists.");
			put("PERFORMANCE_MONITORING_SETUP", "Request validation failed. Please do the Dash Board set up before executing ");
			put("PM_1000", "Request validation failed. Project by name %s already exists.");
			put("PM_1001", "Resource not found. Project by name %s doesn't exists.");
			put("PM_1002", "Resource not found. A combination of Project name  %s & Proxy name %s doesn't exist.");
			put("ScopeCategory-001", "Request validation failed. A records already exists for selected Category name - %s.");
			put("ScopeCategory-002", "Resource not found. No records found for selected Category name - %s.");
			put("Consent-003", "Resource not found. No consent API key found");
			put("Consent-004", "Request validation failed. Report can be generated only for 30 day duration");
			put("Consent-005", "Resource not found. No records found for the selected date range");
			put("SCM-001", "SCM Connection Failure");

			put("rate-limiting-1000", "Hazelcast connection is down. Please try after sometime.");
			put("rate-limiting-1001", "Invalid Quota. Quota is empty.");
			put("rate-limiting-1002", "Plan mismatch in quota and workspace.");
			put("rate-limiting-1003", "Failed to update quotas. Please try again.");
			put("rate-limiting-1004", "Quotas list is empty.");

			put("SCM-1010","swagger body is empty");
			put("SCM-1020","Swagger Name is empty");
			put("SCM-1030","Invalid Scm source");
			put("SCM-1040","Scm Repository name is empty");
			put("SCM-1050","Scm branch is empty");
			put("SCM-1060","Scm host url is empty");
			put("SCM-1070","Scm Token is empty");
			put("SCM-1080","Invalid Credentials");
			put("SCM-1090","Model Maps not found");
			put("SCM-1091","Dictionary Name is empty");

			//Monetization
			put("Monetization-1010","Cannot place purchase. Insufficient Wallet Balance");
			put("Monetization-1020","Could Not Complete Purchase. Please try again. If issue persists, contact the application support team.");
			put("Monetization-1030","Could Not Create Rate Plan. Please try again. If issue persists, contact the application support team.");
			put("Monetization-1040","Could Not Update Rate Plan. Please try again. If issue persists, contact the application support team.");
			put("Monetization-1050","Could Not Update Product Status. Please try again. If issue persists, contact the application support team.");

		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put("ProxyGen-1000", 400);
			put("ProxyGen-1001", 404);
			put("ProxyGen-1002", 401);
			put("ProxyGen-1003", 404);
			put("ProxyGen-1004", 400);
			put("ProxyGen-1005", 404);
			put("Portal-1000", 400);
			put("Portal-1001", 404);
			put("Configuration-1000", 400);
			put("Configuration-1001", 404);
			put("Configuration-1002", 404);
			put("Configuration-1003", 404);
			put("Configuration-1004", 404);
			put("Configuration-1005", 400);
			put("Configuration-1006", 400);
			put("Configuration-1007", 400);
			put("Configuration-1008", 400);
			put("Configuration-1009", 400);
			put("Configuration-1010", 400);
			put("Configuration-1011", 400);
			put("Configuration-1012", 404);
			put("Configuration-1013", 404);
			put("Configuration-1014", 400);
			put("Configuration-1015", 503);
			put("Configuration-1016", 400);
			put("Configuration-1017", 404);
			put("Configuration-1018", 400);
			put("Configuration-1019", 404);
			put("Configuration-1020", 503);
			put("Configuration-1021", 400);
			put("Configuration-1022", 404);
			put("Configuration-1023", 400);
			put("Configuration-1024", 400);
			put("Configuration-1025", 503);
			put("Configuration-1026", 400);
			put("Configuration-1027", 400);
			put("Configuration-1028", 400);
			put("Configuration-1029", 400);
			put("Configuration-1030", 400);
			put("Configuration-1031", 401);
			put("Configuration-1032", 400);
			put("Configuration-1033", 400);
			put("Configuration-1034", 400);
			put("Configuration-1035", 400);
			put("Config-1004", 500);

			put("Identity-1000", 400);
			put("Identity-1001", 400);
			put("Identity-1002", 400);
			put("Identity-1003", 400);
			put("Identity-1004", 400);
			put("Identity-1005", 400);
			put("Identity-1006", 404);
			put("Identity-1007", 403);
			put("Identity-1008", 403);
			put("Identity-1009", 400);
			put("Identity-1010", 400);
			put("Identity-1011", 400);
			put("Identity-1012", 400);
			put("Identity-1013", 400);
			put("Identity-1014", 400);
			put("Identity-1015", 400);
			put("Identity-1016", 400);
			put("Identity-1017", 400);
			put("Identity-1018", 401);
			put("Identity-1019", 401);
			put("Identity-1020", 401);
			put("Identity-1021", 400);
			put("Identity-1022", 401);
			put("Identity-1023", 400);
			put("Identity-1024", 400);
			put("Identity-1025", 404);
			put("Identity-1026", 401);
			put("Identity-1027", 400);
			put("Identity-1028", 401);
			put("Identity-1029", 400);
			put("Identity-1030", 500);
			put("Identity-1031", 400);
			put("Identity-1032", 401);
			put("Identity-1033", 400);
			put("Identity-1034", 400);
			put("Identity-1035", 400);
			put("Identity-1036", 400);
			put("Identity-1037", 400);
			put("Identity-1038", 400);
			put("Identity-1039", 400);
			put("Identity-1040", 500);
			put("Identity-1041", 500);
			put("Identity-1042", 403);
			put("Identity-1043", 403);
			put("Identity-1044", 400);
			put("Identity-1045", 400);
			put("Identity-1046", 400);
			put("Identity-1047", 400);
			put("Identity-1048",400);
			put("Identity-1049",400);
			put("Identity-1050",400);
			put("USER_005", 400);
			put("USER_008", 400);
			put("USER_004", 400);
			put("Connector-1000", 400);
			put("Connector-1001", 400);
			put("Connector-1002", 400);
			put("Connector-1003", 400);
			put("Connector-1004", 400);
			put("Connector-1005", 400);
			put("DataBackup-1000", 400);
			put("DataBackup-1001", 400);
			put("Swagger-1000", 404);
			put("Swagger-1001", 404);
			put("Swagger-1002", 400);
			put("Swagger-1003", 400);
			put("Swagger-1004", 400);
			put("Swagger-1005", 400);
			put("Swagger-1006", 400);
			put("Swagger-1007", 400);
			put("Swagger-1008", 400);
			put("Swagger-1009", 400);
			put("Swagger-1010",400);
			put("Swagger-1011", 400);
			put("Teams-1000", 400);
			put("Teams-1001", 404);
			put("Teams-1002", 400);
			put("CodeCoverage-1000", 404);
			put("CodeCoverage-1001", 400);
			put("PolicyPerformance-1000", 400);
			put("PolicyPerformance-1001", 404);
			put("CICD-1000", 400);
			put("CICD-1001", 400);
			put("CICD-1002", 400);
			put("CICD-1003", 400);
			put("CI-CD-GBTA500",400);
			put("ServiceRegistry-1000", 404);
			put("ServiceRegistry-1001", 400);
			put("ServiceRegistry-1002", 400);
			put("Testsuite-1000", 409);
			put("Testsuite-1001", 400);
			put("Testsuite-1002", 400);
			put("Testsuite-1003", 500);
			put("Testsuite-1004", 400);
			put("Testsuite-1005", 400);
			put("Testsuite-1006", 400);
			put("Testsuite-1007", 500);
			put("Testsuite-1008", 400);
			put("Testsuite-1009", 400);
			put("Testsuite-1010", 400);
			put("Testsuite-1011", 500);
			put("Testsuite-1012", 404);
			put("Testsuite-1013", 400);
			put("Testsuite-1014", 500);
			put("Testsuite-1015", 500);
			put("Testsuite-1016", 400);
			put("Portfolio-1000", 400);
			put("Portfolio-1001", 400);
			put("Portfolio-1002", 400);
			put("Portfolio-1003", 400);
			put("Portfolio-1004", 404);
			put("Portfolio-1005", 404);
			put("Portfolio-1006", 404);
			put("Portfolio-1007", 400);
			put("Portfolio-1008", 400);
			put("Portfolio-1009", 500);
			put("Portfolio-1010", 400);
			put("Portfolio-1011", 400);
			put("Portfolio-1012", 400);
			put("Portfolio-1013", 400);
			put("Portfolio-1014", 400);
			put("Portfolio-1015", 400);
			put("Portfolio-1016", 500);
			put("Monitor-1000", 404);
			put("Monitor-1001", 400);
			put("Monitor-1002", 404);
			put("Monitor-1003", 400);
			put("Monitor-1004", 500);
			put("Monitor-1005", 400);
			put("Monitor-1006", 400);
			put("MockServer-1000", 400);
			put("MockServer-1001", 404);
			put("MockServer-1002", 400);
			put("Marketing-1000", 500);
			put("Marketing-1001",500);
			put("MockServer-1003", 400);
			put("General-1000", 500);
			put("General-1001", 400);
			put("ScopeCategory-001", 400);
			put("ScopeCategory-002", 400);
			put("Consent-003", 400);
			put("Consent-004", 400);
			put("Consent-005", 400);
			put("SCM-001", 500);

			put("rate-limiting-1000", 500);
			put("rate-limiting-1001", 400);
			put("rate-limiting-1002", 400);
			put("rate-limiting-1003", 500);
			put("rate-limiting-1004", 400);

			put("SCM-1010",400);
			put("SCM-1020",400);
			put("SCM-1030",400);
			put("SCM-1040",400);
			put("SCM-1050",400);
			put("SCM-1060",400);
			put("SCM-1070",400);
			put("SCM-1080",500);
			put("SCM-1090",400);
			put("SCM-1091",400);

			put("Monetization-1010",400);
			put("Monetization-1020",500);
			put("Monetization-1030",500);
			put("Monetization-1040",500);
			put("Monetization-1050",500);

		}
	};
	private String message;

	public String message() {
		return message;
	}

	ErrorCodes(String message) {
		this.message = message;
	}
}
