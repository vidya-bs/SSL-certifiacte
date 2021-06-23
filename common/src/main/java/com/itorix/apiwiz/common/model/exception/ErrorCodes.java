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
	Identity-1001("Account Locked"),
	Identity-1000("Please enter the registered mail id"),
	USER_004("Please enter the correct password"),
	USER_006("User not found"),
	USER_007("User doesn't have previlage"),
	USER_005("User already Exists"),
	Identity-1003("Resource validation failed. Workspace you are trying to register already exists."),
	Swagger1009("Request validation failed. Team name already exists.");

	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			/*Api Monitoring Error codes START
			put("Monitoring-1000", "Invalid request data!  Missing mandatory data to process the request..");
			put("Monitoring-1001", "Project name %s already exists, use a different name.");
			put("Monitoring-1002", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1003", "Project name %s already exists, use a different name.");
			put("Monitoring-1004", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1005", "Project  and proxy  combination does not exists.");
			put("Monitoring-1006", "Invalid request data!  Missing mandatory data to process the request.");
			put("Monitoring-1007", "Project with name %s does not exists.");
			put("Monitoring-1008", "Invalid request data!  Missing mandatory data to process the request.");
			/*Api Monitoring Error codes END */

			put("ProxyGen-1000", "Invalid request data!  Missing mandatory data to process the request.");
			put("ProxyGen-1001", "There are no flows defined in the given file.");
			put("ProxyGen-1001", "Request validation failed. Resource or proxy name does not exist.");
			put("ProxyGen-1002", "Request validation failed. Exception connecting to apigee connector.");
			put("ProxyGen-1003", "Request validation failed. Resource or proxy name does not exist.");
			put("ProxyGen-1004", "Request validation failed. Resource or proxy name already exist.");
			put("ProxyGen-1004", "Request validation failed. Source control connector info are invalid.");

			put("Configuration-1000", "Request validation failed. Please check the mandatory data fields and retry again.");
			put("Config-1001", "There are no flows defined in the given file.");
			put("Configuration-1001", "Request validation failed. Resource or proxy name does not exist.");
			put("Configuration-1002", "Request validation failed. Exception connecting to apigee connector.");
			put("Configuration-1003", "Request validation failed. Resource or proxy name does not exist.");
			put("Configuration-1004", "Request validation failed. Resource or proxy name already exist.");
			put("Configuration-1005", "Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1006", "Request validation failed. Exception connecting to apigee connector.");
			put("Configuration-1007", "Request validation failed. Resource already exist.");
			put("Configuration-1008", "Request validation failed. Value for type can be KVM, Cache or TargetServer.");
			put("Configuration-1009", "Request validation failed. Resource already exist.");
			put("Configuration-1010", "Request validation failed. Resource with status approved cannot be updated.");

			put("Configuration-1011","Request validation failed. Cache name %cachename for Org %orgname and Env %env already exists.");
			put("Configuration-1012","Resource not found. Cache name %cachename for Org %orgname and Env %env doesn't exists.");
			put("Configuration-1013","Resource not found. Cache %cachename doesn't exists.");
			put("Configuration-1014","	Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1015","	Request validation failed. Exception connecting to apigee connector..");
			put("Configuration-1016","Request validation failed. Request validation failed. KVM name %KVMName for Org %orgname and Env %env already exists.");
			put("Configuration-1017","Resource not found. KVM name %KVMname for Org %orgname and Env %env doesn't exists.");
			put("Configuration-1018","Resource not found. KVM name %KVMname doesn't exist.");
			put("Configuration-1019","	Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1020","	Request validation failed. Exception connecting to apigee connector..");
			put("Configuration-1021","Request validation failed. Target name %TargetName for Org %orgname and Env %env already exists.");
			put("Configuration-1022","Resource not found. Target name %Targetname for Org %orgname and Env %env doesn't exists..");
			put("Configuration-1023","Resource not found. Target name %Targetname doesn't exist.");
			put("Configuration-1024","	Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1025","	Request validation failed. Exception connecting to apigee connector..");

			put("Configuration-1026","Request validation failed. Resource already exist..");
			put("Configuration-1027","Request validation failed. Value for type can be KVM, Cache or TargetServer.");
			put("Configuration-1028","Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1029","Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Configuration-1030","Request validation failed. Approval type provided doesn't match the enum type.");
			put("Configuration-1031","Resource authorization validation failed. Please contact your workspace admin.");

			put("Identity-1000", "Invalid email ID provided in the request, please use  a valid email ID.");
			put("Identity-1001", "User account validation failed. The account is locked due to incorrect login attempts. Please contact your workspace admin.");
			put("USER_004", "Sorry! The old password entered doesn't match the records.");
			put("USER_005", "User already Exists.");
			put("USER_008", "Sorry! Incorrect time range.");
			put("Identity-1002", "Resource not found. No records found for the search criteria.");
			put("Identity-1003", "Sorry! Invalid login session, the session you are using got expired, relogin to continue.");
			put("Identity-1004", "User account validation failed. Domain name %s is not allowed. Please contact your workspace admin.");
			put("USER_012", "Sorry! UserDomain Already Exists.");
			put("USER_013", "Sorry! UserDomain Doesn't Exists.");
			put("Identity-1005", "Contact your Org Admin to activate your account.");
			put("Identity-1006", "Contact your Org Admin to get your account unlocked.");
			put("Identity-1007", "Request validation failed. Please check the mandatory data fields and retry again.");
			put("Identity-1008", "Workspace you are trying to use is already in use.");
			put("Identity-1009", "User account validation failed. One-time account token is invalid. Please contact your workspace admin.");
			put("Identity-1010", "Not able to find the loginId/emailId provided, please register for login.");
			put("Identity-1011", "Request validation failed. Workspace doesn't exist. in the request.");
			put("Identity-1012", "User does not have Admin role to perform this operation.");
			put("Identity-1013", "User account validation failed. Workspace is not active..");
			put("Identity-1014", "No permissions to perform the operation you are trying to perform.");
			put("Identity-1015", "Resource authorization validation failed. Please contact your workspace admin.");
			put("Identity-1016", "User account validation failed. User account is locked. Please contact workspace admin.");
			put("Identity-1017", "Internal server error. Please contact support for further instructions.");

			put("Identity-1021", "Resource validation failed. Username or login-id you are trying to register already exists.");
			put("Identity-1022", "Resource authorization validation failed. Please contact your workspace admin.");
			put("Identity-1023", "Resource not found. Email-Id provided in the request doesn't exist.");
			put("Identity-1024", "User account validation failed. Invalid login credentials..");
			put("Identity-1025", "Request validation failed. Please check the mandatory data fields and retry again.");
			put("Identity-1026", "Request validation failed. Invalid session token.");
			put("Identity-1027", "Request validation failed. Please check the mandatory data fields and retry again.");

			put("Identity-1028","Request validation failed. Invalid session token.");
			put("Identity-1029","Resource authorization validation failed. Please contact your workspace admin..");
			put("Identity-1030","Request validation failed. Invalid SAML configurations.");
			put("Identity-1031","Not able to find the loginId provided, please register before login.");
			put("Identity-1032","User account validation failed. Session-Id is expired.");
			put("Identity-1033","Required data metadataUrl or metadata file is missing.");
			put("Identity-1034","Request validation failed. Invalid SAML configurations.");
			put("Identity-1035","Required data metadataUrl is missing in the SAML configuration.");
			put("Identity-1036","User account validation failed. Invalid login credentials..");
			put("IDENTITY-1023","loginId or password is missing.");
			put("Identity-1037","Request validation failed. Invalid ldap configurations.");
			put("Identity-1038","Internal server error. Please contact support for further instructions.");
			put("Identity-1039","Request validation failed. Invalid ldap configurations.");
			put("Identity-1040","Internal server error. Please contact support for further instructions.");
			put("Identity-1041","Internal server error. Please contact support for further instructions.");
			put("Identity-1042","Trial period expired. Please contact support to assist you further.");


			put("PROJECT_PLAN_TRACK_001", "Project already exist");
			put("ProjectPlan-1001", "Project with name %s does not exists.");
			put("ProjectPlan-1006","Project  and proxy  combination does not exists.");
			put("PERFORMANCE_MONITORING_SETUP", "Please do the Dash Board set up before executing ");
			put("PM_1000", "Project by name %s already exists.");
			put("PM_1001", "Project by name %s doesn't exists.");
			put("PM_1002", "Cobination of Project name  %s & Proxy name %s doesn't exists.");
			put("Connector-1000", "Postman file with the same name exists.");
			put("Connector-1001", "Environment file with the same name exists.");
			put("Connector-1002", "No Postman file found with the name provided.");
			put("Connector-1003", "No Environment file found with the name provided.");
			put("Connector-1004", "No data forund for the PostMan  and Environment  combination.");
			put("Connector-1005", "No Environment file found with the name provided.");
			put("CICD-1000", "Invalid characters found, only '-' is allowed in the project name");

			put("DataBackup-1000", "Request validation failed. Resource already exist.");
			put("DataBackup-1001", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");


			put("Swagger-1000", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Swagger-1002", "Resource not found. No records found for selected swagger name - %s with following revision - %s.");
			put("Swagger-1002", "Request validation failed. Swagger name - %s already exists.");
			put("Swagger-1003", "Request validation failed. Missing XSD document.");
			put("Swagger-1004", "Request validation failed. Missing XSD element name.");
			put("Swagger-1005", "Request validation failed. Invalid swagger file imported.");
			put("Swagger-1006", "Request validation failed. Invalid xpath generator file.");
			put("Swagger-1007", "Request validation failed. Invalid xpath entry - %s.");

			put("Teams-1000", "Team name already in use");
			put("Teams-1001", "Resource not found. No records found for selected team name - %s.");
			put("Teams-1002", "Resource authorization validation failed. You don't have enough permissions on this team - %s.");

			put("General-1000", "Internal server error. Please contact support for further instructions.");
			put("General-1001", "Sorry! Invalid request. Please correct the request and try again.");


			put("Apigee-1000", "Sorry! Invalid apigee organization.");
			put("Apigee-1001", "Sorry! Invalid apigee environment.");
			put("Apigee-1002", "Sorry! Apigee connection timeout error.");
			put("Apigee-1003", "Sorry! Apigee unauthorized user.");
			put("Apigee-1004", "Request validation failed. Pipeline name already exists.");
			put("Apigee-1005", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Apigee-1006", "Sorry! Insufficeint permissions to carry out this task.");
			put("Apigee-1007", "Sorry! There is no apigee credentails defined for the logged in user.");

			put("CodeCoverage-1000", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("CodeCoverage-1001", "Invalid request data!  Missing mandatory data to process the request.");
			put("PolicyPerformance-1000", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("PolicyPerformance-1001", "Invalid request data!  Missing mandatory data to process the request.");

			put("CICD-1001", "Request validation failed. Pipeline name already exists.");
			put("CICD-1002", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("CICD_006", "Invalid BackUpInterval ,Interval must be in Daily,Monthly,Weekly.");


			put("Identity-1018","User account validation failed. Session-Id is expired.");
			put("Identity-1019","Resource authorization validation failed. Please contact your workspace admin.");
			put("Identity-1020","Request authentication validation failed. API Key is invalid. ");

			put("ServiceRegistry-1000", "Resource not found. No records found for the given data %s.");
			put("ServiceRegistry-1001", "Request validation failed. Record with value %s already exists.");
			put("ServiceRegistry-1002", "Request validation failed. Resource already exist.");

			put("Testsuite-1","Testsuite execution is already in progress! Can't trigger a new one.");
			put("Testsuite-2","TestSuite is Paused. Unpause it to run");
			put("Testsuite-4","TestSuite agent configuration is missing");
			put("Testsuite-5","Internal error occured during testSuite agent call");
			put("Testsuite-6","Resource not found. Request validation failed. Please check the mandatory data fields and retry again.t");
			put("Testsuite-7","Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Testsuite-8","There are no test suites in running status to cancel.");
			put("Testsuite-9","Error encrypting the data. contact support.");
			put("Testsuite-10","No certificate found with the data provided.");
			put("Testsuite-11","Invalid request data!  Missing mandatory data to process the request : missing %s ");
			put("Testsuite-12","Request validation failed. Not a valid certificate document.");
			put("Testsuite-14","Error creating SSL connection.");
			put("Testsuite-15","No certificate found with the data provided.");
			put("Testsuite-16","Certificate can't be deleted. It is refered in testsuite(s) %s.");
			put("Testsuite-17","Variable decryption failed.Please contact support");
			put("Testsuite-18","No testcases peresent in the testsuite to execute.");

			put("Portfolio-1000", "Invalid request data!  Missing mandatory data to process the request.");
			put("Portfolio-1001","Sorry! No records found for the given Portfolio name  - %s.");
			put("Portfolio-1002","Prortfolio with the name %s already exists.");
			put("Portfolio-1003","Sorry! No records found for the given Portfolio ID  - %s.");
			put("Portfolio-1004", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("Portfolio-1005", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("Portfolio-1006", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("Portfolio-1007", "Document not available in the Request, please upload the document.");
			put("Portfolio-1008", "Invalid request data!  Missing mandatory data to process the request. %s missing");
			put("Portfolio-1009", "Error while uplading the file please contact the Support.");
			put("Portfolio-1010", "Required Image file is not available please upload the file before proceding.");
			put("Portfolio-1011", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("Portfolio-1012", "Sorry! No records found for the Portfolio and Project combination.");
			put("Portfolio-1013", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Portfolio-1014", "Sorry! Record already exist with the Name provided.");
			put("Portfolio-1015", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.");
			put("Portfolio-1016", "Internal server error. Please contact support for further instructions.");

			put("Monitor-1000", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("Monitor-1001","Error encrypting the data. contact support.");
			put("Monitor-1002","No certificate found with the data provided.");
			put("Monitor-1003","No certificate found with the data provided.");
			put("Monitor-1004","Request validation failed. Not a valid certificate document.");
			put("Monitor-1005","Request validation failed. Resource already exist..");
			put("Monitor-1006","Sorry! Record already exist with the Name provided.");


			put("MockServer-1000", "Request validation failed. Mock scenario with similar configuration already exist.");
			put("MockServer-1001", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");
			put("MockServer-1002", "Resource not found. Request validation failed. Please check the mandatory data fields and retry again..");

			put("Marketing-1000", "File couldn't be stored. Please contact support.");
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
			put("ProxyGen-1001", 404);
			put("ProxyGen-1002", 401);
			put("General-1000", 500);

			put("ProxyGen-1003", 404);
			put("ProxyGen-1004", 400);
			put("Configuration-1000", 400);
			put("Config-1001", 404);
			put("Configuration-1001", 404);
			put("Configuration-1002", 401);
			put("Configuration-1003", 404);
			put("Configuration-1004", 400);
			put("Configuration-1008",400);
			put("Configuration-1009", 400);
			put("Configuration-1010",400);
			put("Configuration-1011",400);
			put("Configuration-1012",404);
			put("Configuration-1013",404);
			put("Configuration-1015",503);
			put("Configuration-1014",400);
			put("Configuration-1016",400);
			put("Configuration-1017",404);
			put("Configuration-1018",400);
			put("Configuration-1019",404);
			put("Configuration-1020",503);
			put("Configuration-1021",400);
			put("Configuration-1022",404);
			put("Configuration-1024",400);
			put("Configuration-1025",503);

			put("USER_005", 400);
			put("Identity-1000", 400);
			put("Identity-1023", 400);
			put("Identity-1022", 401);
			put("Identity-1025",400);
			put("Identity-1026",404);
			put("Identity-1027",400);
			put("Identity-1021",400);
			put("Identity-1024", 400);

			put("Identity-1028",401);
			put("Identity-1029",403);
			put("Identity-1030",500);
			put("Identity-1031",400);
			put("Identity-1032",400);
			put("Identity-1033",400);
			put("Identity-1034",400);
			put("Identity-1035",400);
			put("Identity-1036",400);
			put("IDENTITY-1023",400);
			put("Identity-1037",400);
			put("Identity-1038",400);
			put("Identity-1039",400);
			put("Identity-1040",500);
			put("Identity-1041",500);
			put("Identity-1042",401);

			put("USER_004", 400);
			put("Identity-1001", 400);
			put("USER_008", 400);
			put("Identity-1002", 404);
			put("Identity-1007", 400);
			put("Identity-1008", 400);
			put("Identity-1009", 400);
			put("Identity-1011", 400);
			put("Identity-1012", 401);
			put("Identity-1013", 400);
			put("Identity-1010", 400);
			put("Identity-1004",403);
			put("USER_012",400);
			put("USER_013",400);
			put("Identity-1005",400);
			put("Identity-1006",400);
			put("Identity-1003",403);
			put("Identity-1014",400);
			put("Identity-1015",401);
			put("Identity-1016",403);
			put("Identity-1017",500);

			put("PROJECT_PLAN_TRACK_001", 400);
			put("ProjectPlan-1001", 400);
			put("ProjectPlan-1006",404);
			put("PERFORMANCE_MONITORING_SETUP", 400);
			put("DataBackup-1000", 400);
			put("DataBackup-1001", 400);

			put("PM_1000", 400);
			put("PM_1001", 400);
			put("PM_1002", 400);
			put("Connector-1000", 400);
			put("Connector-1001", 400);
			put("Connector-1002", 400);
			put("Connector-1003", 400);
			put("Connector-1004", 400);
			put("Connector-1005",400);
			put("CICD-1000",404);


			put("Swagger-1001",404);
			put("Swagger-1002",404);
			put("Swagger-1002",400);
			put("Swagger-1003",400);
			put("Swagger-1004",400);
			put("Swagger-1005",400);
			put("Swagger-1006",400);
			put("Swagger-1007",400);

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

			put("CodeCoverage-1000", 404);
			put("CodeCoverage-1001", 400);
			put("PolicyPerformance-1000",404);
			put("PolicyPerformance-1001", 400);
			put("CICD-1001",400);
			put("CICD-1002",400);
			put("CICD_006",400);


			put("Portfolio-1000",400);
			put("Portfolio-1001",400);
			put("Portfolio-1002",400);
			put("Portfolio-1003",400);

			put("Identity-1018",401);
			put("Identity-1019",401);
			put("Identity-1020", 401);
			put("ProxyGen-1004", 404);



			put("Configuration-1026",400);
			put("Configuration-1027",400);
			put("Configuration-1028",400);
			put("Configuration-1029",400);
			put("Configuration-1030",400);
			put("Configuration-1031",400);

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
			put("Testsuite-18",400);


			put("Portfolio-1004",404);
			put("Portfolio-1005",404);
			put("Portfolio-1006",404);
			put("Portfolio-1007",400);
			put("Portfolio-1008",400);
			put("Portfolio-1009",500);
			put("Portfolio-1010",400);
			put("Portfolio-1011",400);
			put("Portfolio-1012",400);
			put("Portfolio-1013",400);
			put("Portfolio-1014",400);
			put("Portfolio-1015",400);
			put("Portfolio-1016",500);


			put("Monitor-1000",404);
			put("Monitor-1001",400);
			put("Monitor-1000",404);
			put("Monitor-1003",400);
			put("Monitor-1004",500);
			put("Monitor-1005",400);
			put("Monitor-1006",400);

			put("MockServer-1000", 400);
			put("MockServer-1001",404);
			put("MockServer-1002",400);

			put("Marketing-1000",500);
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
