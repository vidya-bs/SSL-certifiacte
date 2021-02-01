package com.itorix.apiwiz.sso.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {


	;
	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{

			put("SSO-003", "Sorry! Incorrect email ID. Please enter the correct email ID.");
			put("SSO-002", "Sorry! The Account is locked due to incorrect login attempts. Contact your admin team.");
			put("SSO-004", "Sorry! The old password entered doesn't match the records.");
			put("SSO-005", "User already Exists.");
			put("SSO-008", "Sorry! Incorrect time range.");
			put("SSO-009", "Sorry! No records found for the search criteria.");
			put("SSO-010", "Sorry! Invalid login session. Please try and re-login.");
			put("SSO-011", "Sorry! The following domain %s is not allowed. Please contact the admin.");
			put("SSO-012", "Sorry! UserDomain Already Exists.");
			put("SSO-013", "Sorry! UserDomain Doesn't Exists.");
			put("SSO-014", "Sorry! Please contact admin to get account activated.");
			put("SSO-015", "Sorry! Please contact admin to get account unLocked.");
			put("SSO-016", "Invalid request data! Missing mandatory data to process the request.");
			put("SSO-018", "Workspace Id already in use");
			put("SSO-020", "Token expired or used previously");
			put("SSO-021", "email/loginId used is not available");
			put("SSO-022", "No workspace with the name provided");
			put("SSO-023", "Not an Admin to perform the operation");
			put("SSO-024", "workspace used is not active");
			put("SSO-028", "No permissions associated to the plan associated");
			put("SSO-029", "No permissions to perform the action");

			put("SSO-1008", "Sorry! The username entered already exists. Please try registering with a new loginid and email or login with the existing credentials.");
			put("SSO-1010", "Sorry! Insufficeint permissions to perform this activity.");
			put("SSO-1001", "Sorry! Couldn’t find the emaild provided in the request");
			put("SSO-1012", "provided UserId and password did not match");
			put("SSO-1004","Invalid request data! Missing mandatory data to process the request.");
			put("SSO-1011","Jsession provided is not available");
			put("SSO-1006","Invalid request data! Missing mandatory data to process the request.");

			put("SSO-1014","Login failed! Invalid or missing JSESSIONID.");
			put("SSO-1015","you don't have enough permissions to access the resource");
			put("SSO-1016","SAML configurations are incomplete/missing. Please configure before login using SAML.");
			put("SSO-1017","Login Id doesn't exist.");
			put("SSO-1018","Invalid Session! Please login and try again");
			put("SSO-1019","metadataUrl or metadata file is mandatory.");
			put("SSO-1020","SAML response from metadataUrl is empty.");
			put("SSO-1021","metadataUrl is missing in SAML configuration");
			put("SSO-1022","Invalid credentials.");
			put("SSO-1023","loginId or password is missing.");
			put("SSO-1024","LDAP configuration is incomplete/missing. Please configure before login using ldap credentials.");
			put("SSO-1025","Issue occured during session creation contact administrator.");
			put("SSO-1026","Ldap configuration is incomplete for the user. Please contact administrator");
			put("SSO-1027","Could not store configuration. Please contact administrator");
			put("SSO-1028","Could not read configuration. Please contact administrator");
			put("SSO-1029","Trial period expired. Please contact administrator");

			put("SSO-2","Login failed! Invalid or missing x-apikey.");
		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			/*Api Monitoring Error codes START */

			put("General-1000", 500);



			put("SSO-005", 400);
			put("SSO-003", 400);
			put("SSO-1001", 400);
			put("SSO-1010", 401);
			put("SSO-1004",400);
			put("SSO-1011",404);
			put("SSO-1006",400);
			put("SSO-1008",400);
			put("SSO-1012", 400);

			put("SSO-1014",401);
			put("SSO-1015",403);
			put("SSO-1016",500);
			put("SSO-1017",400);
			put("SSO-1018",400);
			put("SSO-1019",400);
			put("SSO-1020",400);
			put("SSO-1021",400);
			put("SSO-1022",400);
			put("SSO-1023",400);
			put("SSO-1024",400);
			put("SSO-1025",400);
			put("SSO-1026",400);
			put("SSO-1027",500);
			put("SSO-1028",500);
			put("SSO-1029",401);

			put("SSO-004", 400);
			put("SSO-002", 400);
			put("SSO-008", 400);
			put("SSO-009", 404);
			put("SSO-016", 400);
			put("SSO-018", 400);
			put("SSO-020", 400);
			put("SSO-022", 400);
			put("SSO-023", 401);
			put("SSO-024", 400);
			put("SSO-021", 400);
			put("SSO-011",403);
			put("SSO-012",400);
			put("SSO-013",400);
			put("SSO-014",400);
			put("SSO-015",400);
			put("SSO-010",403);
			put("SSO-028",400);
			put("SSO-029",401);
			put("SSO-2",403);
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
