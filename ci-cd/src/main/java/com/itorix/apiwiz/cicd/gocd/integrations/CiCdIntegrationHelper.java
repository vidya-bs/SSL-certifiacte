package com.itorix.apiwiz.cicd.gocd.integrations;

public class CiCdIntegrationHelper {
	
	public final static String TRIGGER_STAGE = "triggerStage";
	public final static String HEALTH = "health";
	public final static String CREATE_EDIT = "createEdit";
	public final static String DELETE = "delete";
	public final static String CRUISE_JOB_DURATION = "CruiseJobDuration";
	
	public static String getHeader(String operation, String version){
		if(version!= null && operation != null){
			if(operation.equals(TRIGGER_STAGE)){
				return getTriggerStageHeader(version);
			}
			if(operation.equals(HEALTH)){
				return getHealthHeader(version);
			}
			if(operation.equals(CREATE_EDIT)){
				return getCreateEditPipelineHeader(version);
			}
			if(operation.equals(DELETE)){
				return getDeletePipelineHeader(version);
			}
			if(operation.equals(CRUISE_JOB_DURATION)){
				return getCruiseJobHeader(version);
			}
		}
		return null;
	}
	
	private static String getTriggerStageHeader(String version){
		String accept = null;
		if(version.equalsIgnoreCase("18.1.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		if(version.equalsIgnoreCase("18.10.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		return accept;
	}
	
	private static String getHealthHeader(String version){
		String accept = null;
		if(version.equalsIgnoreCase("18.1.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		if(version.equalsIgnoreCase("18.10.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		return accept;
	}
	
	private static String getDeletePipelineHeader(String version){
		String accept = null;
		if(version.equalsIgnoreCase("18.1.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		if(version.equalsIgnoreCase("18.10.0")){
			accept = "application/vnd.go.cd.v6+json";
		}
		return accept;
	}
	
	private static String getCreateEditPipelineHeader(String version){
		String accept = null;
		if(version.equalsIgnoreCase("18.1.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		if(version.equalsIgnoreCase("18.10.0")){
			accept = "application/vnd.go.cd.v6+json";
		}
		return accept;
	}
	
	private static String getCruiseJobHeader(String version){
		String accept = null;
		if(version.equalsIgnoreCase("18.1.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		if(version.equalsIgnoreCase("18.10.0")){
			accept = "application/vnd.go.cd.v4+json";
		}
		return accept;
	}
	
	public static String getConfirmHeader(String version){
		String value = null;
		if(version.equalsIgnoreCase("18.10.0")){
			value = "X-GoCD-Confirm";
		}
		return value;
	}
}
