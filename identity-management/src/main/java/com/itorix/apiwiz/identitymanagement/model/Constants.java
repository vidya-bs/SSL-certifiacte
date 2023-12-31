package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Constants {

	public static final String USER_REGISTR_DISPLAY_MESSAGE = "Thanks For registeration . Sent Verification Mail to Registered Mail email";
	public static final String USER_VERIFICATION_DISPLAY_MESSAGE = "Thanks For Verifying. Account will be activated once it is verified by admin";

	public static final String USER_LOCKED_DISPLAY_MESSAGE = "User is Locked. Please Contact Administrator";
	public static final String USER_VERIFICATION_LINK = "Please Click on new link for Verification";
	public static final String USER_BLOCKED_DISPLAY_MESSAGE = "User is Blocked. Please Contact Administrator";

	public static final String USER_BLOCKED_REASON_MESSAGE = "Exceeds Max reset password attempts";

	public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

	public static final String USER_REGISTR_PAGE = "session/register";

	public static final String[] SWAGGER_PROJECTION_FIELDS = new String[]{"swaggerId", "name", "status", "mts",
			"revision", "createdUserName", "createdBy"};

	public static final String STATUS = "status";

	public static final String REVISION = "revision";

	public static final String ORIGINAL_DOC = "originalDoc";

	public static final String SWAGGER_ID = "swaggerId";

	public static final String MAX_REVISION = "maxRevision";

	public static final String CREATED_BY = "createdBy";

	public static final String MTS_TO_DATE = "mtsToDate";

	public static final String[] GRAPHQL_PROJECTION_FIELDS = new String[]{"name","revision","graphQLSchema","graphQLId","status","cts",
			"mts","createdUserName","createdBy","modifiedUserName","modifiedBy"};

	public static final String GRAPHQL_ID = "graphQLId";

	public static final String COUNT = "count";
}
