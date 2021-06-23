package com.itorix.apiwiz.common.model.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {
	/*Swagger1001("Couldn't find the swagger by name %s."),
	Swagger1002("Couldn't find the swagger version number %s for %s."),
	Swagger1003("Swagger by name {swaggerName} already exists."),
	Swagger1004("Please pass the XSD file for which the xpath needs to be generated."),
	Swagger1005("Please pass the element name for which xpath needs to be generated."),
	Swagger1006("Please pass the XSD file for which the swagger definitions needs to be generated."),
	Swagger1007("The Xpath file is in correct. Please correct and retry again."),*/

	Xpath1001("Couldn't find the Xpath by name %s."),

	USER_001("Invalid Credentials"),
	USER_002("Account Locked"),
	USER_003("Please enter the registered mail id"),
	USER_004("Please enter the correct password"),
	USER_006("User not found"),
	USER_007("User doesn't have previlage"),
	USER_005("User already Exists"),
	USER_010("workspace Id already in use"),
	Swagger1009("Swagger Team name is already avilable");

	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			/*Api Monitoring Error codes START */
			put("Monitoring-1000", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1001", "Project with name %s already exists.");
			put("Monitoring-1002", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1003", "Project with name %name% already exists.");
			put("Monitoring-1004", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1005", "Project  and proxy  combination does not exists.");
			put("Monitoring-1006", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1007", "Project with name %s does not exists.");
			put("Monitoring-1008", "Invalid request data!  Missing mandatory data to process the request.");
			/*Api Monitoring Error codes END */

			put("ProxyGen-1000", "Please check the request and retry again.");
			put("ProxyGen-1001", "There are no flows defined in the given file.");
			put("ProxyGen-1002", "Proxy Name not found.");
			put("ProxyGen-1003", "Invalid apigee credentials");
			//put("General-1000", "Internal server error. Please try after sometime.");
			put("ProxyGen-1004", "No data found.");
			put("ProxyGen-1005", "Record exists with same data.");
			put("ProxyGen-1006", "Not a valid SCM source");
			put("Config-1000", "Please check the request and retry again.");
			put("Config-1001", "There are no flows defined in the given file.");
			put("Config-1002", "Proxy Name not found.");
			put("Config-1003", "Invalid apigee credentials");
			put("Config-1004", "No data found.");
			put("Config-1005", "Record exists with same data.");
			put("Config-1006", "Insufficient Data in the Request.");
			put("Config-1007", "Insufficient Apigee permission.");
			put("Config-1008", "Resource already available.");
			put("Config-1009", "Invalid Service Request Type ,Type must be in TargetServer,Cache,KVM.");
			put("Config-1010", "Service Request already Exists");
			put("Config-1011", "Cannot update as it is in approve status");

			put("ConfigMgmt-1002","Cache name %cachename for Org %orgname and Env %env already exists.");
			put("ConfigMgmt-1003","Cache name %cachename for Org %orgname and Env %env does not exists.");
			put("ConfigMgmt-1004","Cache name %cachename does not exists.");
			put("ConfigMgmt-1006","	Invalid request data! Missing mandatory data to process the request.");
			put("ConfigMgmt-1007","	Invalid Apigee Credentials.");

			put("ConfigMgmt-1012","KVM name %KVMName for Org %orgname and Env %env already exists.");
			put("ConfigMgmt-1013","KVM name %KVMname for Org %orgname and Env %env does not exists.");
			put("ConfigMgmt-1015","KVM name %KVMname does not exists.");
			put("ConfigMgmt-1011","	Invalid request data! Missing mandatory data to process the request.");
			put("ConfigMgmt-1016","	Invalid Apigee Credentials.");

			put("ConfigMgmt-1022","Target name %TargetName for Org %orgname and Env %env already exists.");
			put("ConfigMgmt-1023","Target name %Targetname for Org %orgname and Env %env does not exists.");
			put("ConfigMgmt-1025","Target name %Targetname does not exists.");
			put("ConfigMgmt-1021","	Invalid request data! Missing mandatory data to process the request.");
			put("ConfigMgmt-1026","	Invalid Apigee Credentials.");

			put("CONFIG-1102","Service Request already Exists.");
			put("CONFIG-1103","Invalid Service Request Type.");
			put("CONFIG-1101","Invalid request data! Missing mandatory data to process the request.");
			put("CONFIG-1104","Requested service request is not available.");
			put("CONFIG-1105","invalid approval type in the request.");
			put("CONFIG-1106","InSufficeint Permissions to perform the operation.");

			put("USER_003", "Sorry! Incorrect email ID. Please enter the correct email ID.");
			put("USER_002", "Sorry! The Account is locked due to incorrect login attempts. Contact your admin team.");
			put("USER_004", "Sorry! The old password entered doesn't match the records.");
			put("USER_005", "User already Exists.");
			put("USER_008", "Sorry! Incorrect time range.");
			put("USER_009", "Sorry! No records found for the search criteria.");
			put("USER_010", "Sorry! Invalid login session. Please try and re-login.");
			put("USER_011", "Sorry! The following domain %s is not allowed. Please contact the admin.");
			put("USER_012", "Sorry! UserDomain Already Exists.");
			put("USER_013", "Sorry! UserDomain Doesn't Exists.");
			put("USER_014", "Sorry! Please contact admin to get account activated.");
			put("USER_015", "Sorry! Please contact admin to get account unLocked.");
			put("USER_016", "Invalid request data! Missing mandatory data to process the request.");
			put("USER_018", "Workspace Id already in use");
			put("USER_020", "Token expired or used previously");
			put("USER_021", "email/loginId used is not available");
			put("USER_022", "No workspace with the name provided");
			put("USER_023", "Not an Admin to perform the operation");
			put("USER_024", "workspace used is not active");
			put("USER_028", "No permissions associated to the plan associated");
			put("USER_029", "No permissions to perform the action");
			put("USER_030", "Looks like your account is locked, Please contact workspace admin.");
			put("USER_031", "Sorry! error occured while registring the user, please try afer sometime.");

			put("IDENTITY-1008", "Sorry! The username entered already exists. Please try registering with a new loginid and email or login with the existing credentials.");
			put("IDENTITY-1010", "Sorry! Insufficeint permissions to perform this activity.");
			put("IDENTITY-1001", "Sorry! Couldn’t find the emaild provided in the request");
			put("IDENTITY-1012", "provided UserId and password did not match");
			put("IDENTITY-1004","Invalid request data! Missing mandatory data to process the request.");
			put("IDENTITY-1011","Jsession provided is not available");
			put("IDENTITY-1006","Invalid request data! Missing mandatory data to process the request.");

			put("IDENTITY-1014","Login failed! Invalid or missing JSESSIONID.");
			put("IDENTITY-1015","you don't have enough permissions to access the resource");
			put("IDENTITY-1016","SAML configurations are incomplete/missing. Please configure before login using SAML.");
			put("IDENTITY-1017","Login Id doesn't exist.");
			put("IDENTITY-1018","Invalid Session! Please login and try again");
			put("IDENTITY-1019","metadataUrl or metadata file is mandatory.");
			put("IDENTITY-1020","SAML response from metadataUrl is empty.");
			put("IDENTITY-1021","metadataUrl is missing in SAML configuration");
			put("IDENTITY-1022","Invalid credentials.");
			put("IDENTITY-1023","loginId or password is missing.");
			put("IDENTITY-1024","LDAP configuration is incomplete/missing. Please configure before login using ldap credentials.");
			put("IDENTITY-1025","Issue occured during session creation contact administrator.");
			put("IDENTITY-1026","Ldap configuration is incomplete for the user. Please contact administrator");
			put("IDENTITY-1027","Could not store configuration. Please contact administrator");
			put("IDENTITY-1028","Could not read configuration. Please contact administrator");
			put("IDENTITY-1029","Trial period expired. Please contact administrator");


			put("PROJECT_PLAN_TRACK_001", "Project already exist");
			put("ProjectPlan-1001", "Project with name %s does not exists.");
			put("ProjectPlan-1006","Project  and proxy  combination does not exists.");
			put("PERFORMANCE_MONITORING_SETUP", "Please do the Dash Board set up before executing ");
			put("PM_1000", "Project by name %s already exists.");
			put("PM_1001", "Project by name %s doesn't exists.");
			put("PM_1002", "Cobination of Project name  %s & Proxy name %s doesn't exists.");
			put("POSTMAN_001", "Already PostMan exists");
			put("POSTMAN_002", "Already ENV exists");
			put("POSTMAN_003", "PostMan Not Found");
			put("POSTMAN_004", "Environment File Not Found");
			put("POSTMAN_005", "PostMan or Environment File Not Found");
			put("NO_ENV_FOUND", "Environments  Not Avaliable");
			put("CICD_001", "ProjectName Must Contain Hypen");

			put("DMB_0002", "Sorry! Scheduler exists for the selected organization and environment.");
			put("DMB_0003", "Sorry! No records exists for the given request criteria");


			put("Swagger-1001", "Sorry! No records found for the input data");
			put("Swagger-1002", "No records found for selected swagger name - %s with following revision - %s.");
			put("Swagger-1003", "Sorry! Swagger - %s already exists.");
			put("Swagger-1004", "Bad request! XSD file is required.");
			put("Swagger-1005", "Bad request! Please pass the element name from the selected XSD for which the Xpath needs to be generated.");
			put("Swagger-1006", "Sorry! Invalid Swagger. Please valiadate the swagger and import again.");
			put("Swagger-1007", "Sorry! Invalid template uploaded. Please upload a valid xpath file.");
			put("Swagger-1008", "Sorry! Invalid xpath - %s. Please correct and try again later.");

			put("Teams-1000", " Swagger Team name is already avilable");
			put("Teams-1001", "No records found for selected swagger team name - %s.");
			put("Teams-1002", " you don't have enough permissions on this team - %s");

			put("General-1000", "Sorry! Internal server error. Please try again later.");
			put("General-1001", "Sorry! Invalid request. Please correct the request and try again.");


			put("Apigee-1000", "Sorry! Invalid apigee organization.");
			put("Apigee-1001", "Sorry! Invalid apigee environment.");
			put("Apigee-1002", "Sorry! Apigee connection timeout error.");
			put("Apigee-1003", "Sorry! Apigee unauthorized user.");
			put("Apigee-1004", "Sorry! Records all already exist for given data.");
			put("Apigee-1005", "Sorry! No records found for the given data.");
			put("Apigee-1006", "Sorry! Insufficeint permissions to carry out this task.");
			put("Apigee-1007", "Sorry! There is no apigee credentails defined for the logged in user.");

			put("CodeCoverage-1001", "Sorry! No records found for the given record id.");
			put("CodeCoverage-1002", "Sorry! Inavalid request. Please correct the request and try again later.");
			put("PolicyPerformance-1001", "Sorry! No records found for the given record id.");
			put("PolicyPerformance-1002", "Sorry! Inavalid request. Please correct the request and try again later.");

			put("CICD_004", "Sorry! Records all already exist for given data.");
			put("CICD_005", "Sorry! No records found for the given data.");
			put("CICD_006", "Invalid BackUpInterval ,Interval must be in Daily,Monthly,Weekly.");

			put("Portfolio-1000", "Please check the request and retry again.");
			put("Portfolio-1002","Sorry! No records found for the given Portfolio name  - %s.");
			put("Portfolio-1003","Sorry! Portfolio name  - %s already exists.");
			put("Portfolio-1004","Sorry! No records found for the given Portfolio ID  - %s.");
			put("Session_01","Invalid Session! Please login and try again");
			put("Session_02","Unauthorized!! user");
			put("Session_03","Invalid key provided ");

			put("ServiceRegistry-1000", "Sorry! No records found for the given input value/values %s .");
			put("ServiceRegistry-1001", "Sorry! A record already present for input value/values %s .");
			put("ServiceRegistry-1002", "Sorry! Record already exist for given data.");

			put("Testsuite-1","Testsuite execution is already in progress! Can't trigger a new one");
			put("Testsuite-2","TestSuite is Paused. Unpause it to run");
			put("Testsuite-4","TestSuite agent configuration is missing");
			put("Testsuite-5","Internal error occured during testSuite agent call");
			put("Testsuite-6","Testsuite doesn't exist");
			put("Testsuite-7","Variable doesn't exist");
			put("Testsuite-8","There are no test suites in running status to cancel.");
			put("Testsuite-9","variable encryption failed.Please contact admin");
			put("Testsuite-10","Certificate is missing.");
			put("Testsuite-11","Mandatory parameter %s is missing");
			put("Testsuite-12","Certificate could not be stored. Possible reasons certificate is not of type jks or password is incorrect.");
			put("Testsuite-14","Error creating SSL connection.");
			put("Testsuite-15","Certificate does not exist.");
			put("Testsuite-16","Certificate can't be deleted. It is refered in testsuite(s) %s.");
			put("Testsuite-17","variable decryption failed.Please contact admin");


			put("Portfolio-1", "Portfolio does not exist");
			put("Portfolio-2", "Portfolio and product combination does not exist");
			put("Portfolio-4", "Portfolio and document combination does not exist");
			put("Portfolio-5", "Document is missing. Please upload");
			put("Portfolio-6", "Mandatory fields %s missing");
			put("Portfolio-7", "File couldn't be stored. Please contact admin");
			put("Portfolio-8", "Image is missing. Please upload");
			put("Portfolio-9", "Portfolio and service registry combination does not exist");
			put("Portfolio-10", "Portfolio and projects combination does not exist");
			put("Portfolio-11", "Portfolio, projects , proxy combination does not exist");
			put("Portfolio-12", "A record already exists with the same name");
			put("Portfolio-14", "Record does not exist");
			put("Portfolio-15", "Error occured when deleting files related to the document. Please contact admin");

			put("Monitor-Api-1", "Sorry! No records found.");
			put("Monitor-Api-2","variable encryption failed.Please contact admin");
			put("Monitor-Api-3","Certificate does not exist.");
			put("Monitor-Api-4","Certificate is missing.");
			put("Monitor-Api-5","Certificate could not be stored. Possible reasons certificate is not of type jks or password is incorrect.");
			put("Monitor-Api-6","Variable exists with the same name");
			put("Monitor-Api-7","A record exists with the same name");


			put("MockServer-1", "Expectation with the condition already exists");
			put("MockServer-2", "No record found");
			put("MockServer-4", "Group not found");
			
			put("Marketing-2", "File couldn't be stored. Please contact admin");
		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			/*Api Monitoring Error codes START */
			put("Monitoring-1000", 400);
			put("Monitoring-1001", 400);
			put("Monitoring-1002", 400);
			put("Monitoring-1003", 400);
			put("Monitoring-1004", 400);
			put("Monitoring-1005", 404);
			put("Monitoring-1006", 400);
			put("Monitoring-1007", 404);
			put("Monitoring-1008", 400);
			/*Api Monitoring Error codes END */

			put("ProxyGen-1000", 400);
			put("ProxyGen-1001", 404);
			put("ProxyGen-1002", 404);
			put("ProxyGen-1003", 401);
			put("General-1000", 500);

			put("ProxyGen-1004", 404);
			put("ProxyGen-1005", 400);
			put("Config-1000", 400);
			put("Config-1001", 404);
			put("Config-1002", 404);
			put("Config-1003", 401);
			put("Config-1004", 404);
			put("Config-1005", 400);
			put("Config-1009",400);
			put("Config-1010", 400);
			put("Config-1011",400);
			put("ConfigMgmt-1002",400);
			put("ConfigMgmt-1003",404);
			put("ConfigMgmt-1004",404);
			put("ConfigMgmt-1007",503);
			put("ConfigMgmt-1006",400);
			put("ConfigMgmt-1012",400);
			put("ConfigMgmt-1013",404);
			put("ConfigMgmt-1015",400);
			put("ConfigMgmt-1011",404);
			put("ConfigMgmt-1016",503);
			put("ConfigMgmt-1022",400);
			put("ConfigMgmt-1023",404);
			put("ConfigMgmt-1021",400);
			put("ConfigMgmt-1026",503);

			put("USER_005", 400);
			put("USER_003", 400);
			put("IDENTITY-1001", 400);
			put("IDENTITY-1010", 401);
			put("IDENTITY-1004",400);
			put("IDENTITY-1011",404);
			put("IDENTITY-1006",400);
			put("IDENTITY-1008",400);
			put("IDENTITY-1012", 400);

			put("IDENTITY-1014",401);
			put("IDENTITY-1015",403);
			put("IDENTITY-1016",500);
			put("IDENTITY-1017",400);
			put("IDENTITY-1018",400);
			put("IDENTITY-1019",400);
			put("IDENTITY-1020",400);
			put("IDENTITY-1021",400);
			put("IDENTITY-1022",400);
			put("IDENTITY-1023",400);
			put("IDENTITY-1024",400);
			put("IDENTITY-1025",400);
			put("IDENTITY-1026",400);
			put("IDENTITY-1027",500);
			put("IDENTITY-1028",500);
			put("IDENTITY-1029",401);

			put("USER_004", 400);
			put("USER_002", 400);
			put("USER_008", 400);
			put("USER_009", 404);
			put("USER_016", 400);
			put("USER_018", 400);
			put("USER_020", 400);
			put("USER_022", 400);
			put("USER_023", 401);
			put("USER_024", 400);
			put("USER_021", 400);
			put("USER_011",403);
			put("USER_012",400);
			put("USER_013",400);
			put("USER_014",400);
			put("USER_015",400);
			put("USER_010",403);
			put("USER_028",400);
			put("USER_029",401);
			put("USER_030",403);
			put("USER_031",500);

			put("PROJECT_PLAN_TRACK_001", 400);
			put("ProjectPlan-1001", 400);
			put("ProjectPlan-1006",404);
			put("PERFORMANCE_MONITORING_SETUP", 400);
			put("DMB_0002", 400);
			put("DMB_0003", 400);

			put("PM_1000", 400);
			put("PM_1001", 400);
			put("PM_1002", 400);
			put("POSTMAN_001", 400);
			put("POSTMAN_002", 400);
			put("POSTMAN_003", 400);
			put("POSTMAN_004", 400);
			put("POSTMAN_005", 400);
			put("NO_ENV_FOUND",400);
			put("CICD_001",404);


			put("Swagger-1001",404);
			put("Swagger-1002",404);
			put("Swagger-1003",400);
			put("Swagger-1004",400);
			put("Swagger-1005",400);
			put("Swagger-1006",400);
			put("Swagger-1007",400);
			put("Swagger-1008",400);

			put("Teams-1000",400);
			put("Teams-1001",404);
			put("Teams-1002",400);

			put("General-1000",500);
			put("General-1001",400);

			put("Apigee-1000",404);
			put("Apigee-1001",404);
			put("Apigee-1002",500);
			put("Apigee-1003",401);
			put("Apigee-1004",400);
			put("Apigee-1005",400);
			put("Apigee-1006",403);
			put("Apigee-1007",400);

			put("CodeCoverage-1001", 404);
			put("CodeCoverage-1002", 400);
			put("PolicyPerformance-1001",404);
			put("PolicyPerformance-1002", 400);
			put("CICD_004",400);
			put("CICD_005",400);
			put("CICD_006",400);


			put("Portfolio-1000",400);
			put("Portfolio-1002",400);
			put("Portfolio-1003",400);
			put("Portfolio-1004",400);

			put("Session_01",401);
			put("Session_02",401);
			put("Session_03", 401);
			put("ProxyGen-1006", 404);



			put("CONFIG-1102",400);
			put("CONFIG-1103",400);
			put("CONFIG-1101",400);
			put("CONFIG-1104",400);
			put("CONFIG-1105",400);
			put("CONFIG-1106",400);

			put("ServiceRegistry-1002", 400);
			put("ServiceRegistry-1001", 400);
			put("ServiceRegistry-1000", 404);

			put("Testsuite-1",409);
			put("Testsuite-2",400);
			put("Testsuite-4",400);
			put("Testsuite-5",500);
			put("Testsuite-6",400);
			put("Testsuite-7",400);
			put("Testsuite-8",400);
			put("Testsuite-9",500);
			put("Testsuite-10",400);
			put("Testsuite-11",400);
			put("Testsuite-12",400);
			put("Testsuite-14",500);
			put("Testsuite-15",404);
			put("Testsuite-16",400);
			put("Testsuite-17",500);


			put("Portfolio-1",404);
			put("Portfolio-2",404);
			put("Portfolio-4",404);
			put("Portfolio-5",400);
			put("Portfolio-6",400);
			put("Portfolio-7",500);
			put("Portfolio-8",400);
			put("Portfolio-9",400);
			put("Portfolio-10",400);
			put("Portfolio-11",400);
			put("Portfolio-12",400);
			put("Portfolio-14",400);
			put("Portfolio-15",500);


			put("Monitor-Api-1",404);
			put("Monitor-Api-2",400);
			put("Monitor-Api-1",404);
			put("Monitor-Api-4",400);
			put("Monitor-Api-5",500);
			put("Monitor-Api-6",400);
			put("Monitor-Api-7",400);

			put("MockServer-1", 400);
			put("MockServer-2",404);
			put("MockServer-4",400);
			
			put("Marketing-2",500);
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