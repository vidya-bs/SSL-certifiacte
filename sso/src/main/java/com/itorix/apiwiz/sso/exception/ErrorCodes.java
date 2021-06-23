package com.itorix.apiwiz.sso.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {


	;
	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{

			put("SSO-003", "Request validation failed. Email-Id validation failed.");
			put("SSO-002", "User account validation failed. The account is locked due to incorrect login attempts. Please contact your workspace admin.");
			put("SSO-004", "Sorry! The old password entered doesn't match the records.");
			put("SSO-005", "User already Exists.");
			put("SSO-008", "Sorry! Incorrect time range.");
			put("SSO-009", "Resource not found. No records found for the search criteria.");
			put("SSO-010", "User account validation failed. Session-Id is expired.");
			put("SSO-011", "User account validation failed. Domain name %s is not allowed. Please contact your workspace admin.");
			put("SSO-012", "Sorry! UserDomain Already Exists.");
			put("SSO-013", "Sorry! UserDomain Doesn't Exists.");
			put("SSO-014", "User account validation failed. Please contact your workspace admin to activate your account.");
			put("SSO-015", "User account validation failed. Please contact your workspace admin to unlock your access.");
			put("SSO-016", "Request validation failed. Please check the mandatory data fields and retry again.");
			put("SSO-018", "Resource validation failed. Workspace you are trying to register already exists.");
			put("SSO-020", "User account validation failed. One-time account token is invalid. Please contact your workspace admin.");
			put("SSO-021", "Request validation failed. Invalid login-id or email-id provided.");
			put("SSO-022", "Request validation failed. Workspace doesn't exist.");
			put("SSO-023", "Resource authorization validation failed. Account user is not a admin to perform this operation.");
			put("SSO-024", "User account validation failed. Workspace is not active.");
			put("SSO-028", "Resource authorization validation failed. Please contact your workspace admin.");
			put("SSO-029", "Resource authorization validation failed. Please contact your workspace admin.");

			put("SSO-1008", "Resource validation failed. Username or login-id you are trying to register already exists.");
			put("SSO-1010", "Resource authorization validation failed. Please contact your workspace admin.");
			put("SSO-1001", "Resource not found. Email-Id provided in the request doesn't exist.");
			put("SSO-1012", "User account validation failed. Invalid login credentials.");
			put("SSO-1004","Request validation failed. Please check the mandatory data fields and retry again.");
			put("SSO-1011","Request validation failed. Invalid session token.");
			put("SSO-1006","Request validation failed. Please check the mandatory data fields and retry again.");

			put("SSO-1014","Request validation failed. Invalid session token.");
			put("SSO-1015","Resource authorization validation failed. Please contact your workspace admin.");
			put("SSO-1016","Request validation failed. Invalid SAML configurations.");
			put("SSO-1017","Resource not found. Login-Id or email-Id provided in the request doesn't exist.");
			put("SSO-1018","User account validation failed. Session-Id is expired.");
			put("SSO-1019","Request validation failed. Invalid SAML configurations. Missing SAML metadata URL or metadata file.");
			put("SSO-1020","Request validation failed. Invalid SAML configurations.");
			put("SSO-1021","Request validation failed. Invalid SAML configurations. Missing SAML metadata URL or metadata file.");
			put("SSO-1022","User account validation failed. Invalid login credentials.");
			put("SSO-1023","loginId or password is missing.");
			put("SSO-1024","Request validation failed. Invalid ldap configurations.");
			put("SSO-1025","Internal server error. Please contact support for further instructions.");
			put("SSO-1026","Request validation failed. Invalid ldap configurations.");
			put("SSO-1027","Internal server error. Please contact support for further instructions.");
			put("SSO-1028","Internal server error. Please contact support for further instructions.");
			put("SSO-1029","User account validation failed. Trial period expired. Please contact support for further instructions.");

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
