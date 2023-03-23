package com.itorix.apiwiz.identitymanagement.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.UserInfo;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.model.social.MappedOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.identitymanagement.model.Roles;
import com.itorix.apiwiz.identitymanagement.model.UserDefinedRoles;

@Component
public class IdentityManagementHelper {

	@Autowired
	private IdentityManagementDao identityManagementDao;

	public Map<String, String> getRoleMapper(UserDefinedRoles roles) {

		Map<String, String> mapper = new HashMap<>();
		if (roles != null) {
			mapper.put(roles.getAdmin(), Roles.ADMIN.getValue());
			mapper.put(roles.getAnalyst(), Roles.ANALYST.getValue());
			mapper.put(roles.getDeveloper(), Roles.DEVELOPER.getValue());
			mapper.put(roles.getOperation(), Roles.OPERATION.getValue());
			mapper.put(roles.getPortal(), Roles.PORTAL.getValue());
			mapper.put(roles.getProjectAdmin(), Roles.PROJECT_ADMIN.getValue());
			mapper.put(roles.getQa(), Roles.QA.getValue());
			mapper.put(roles.getTest(), Roles.TEST.getValue());
			mapper.remove(null);
		}
		return mapper;
	}

	public String getUserType(List<String> projectRoles) {
		if (projectRoles.contains(Roles.ADMIN.getValue())) {
			return "Admin";
		} else {
			return "Non_Admin";
		}
	}

	public Map<String,Object> oauth2Validation(MappedOAuth2User mappedOAuth2User,String tenant){
		Map<String,Object> attributes = mappedOAuth2User.getAttributes();
		int oauth2Status = 200;
		String errorMessage = "Success";
		Object emailObject = attributes.get("email");
		String email = "";
		if(mappedOAuth2User.getEmail() != null && !mappedOAuth2User.getEmail().isEmpty()){
			email = mappedOAuth2User.getEmail();
		}
		Map<String,Object> validationResult = new HashMap<>();
		UserSession userSession = new UserSession();

		if(!email.isEmpty() || (emailObject != null && !emailObject.toString().isEmpty())){
			email = email.isEmpty()? emailObject.toString() : email;
			UserInfo userInfo = new UserInfo();
			if(!attributes.containsKey("login") && !email.isEmpty()){
				userInfo.setLoginId(email); //for google
			}else{
				userInfo.setLoginId(attributes.get("login").toString());
			}

			userInfo.setWorkspaceId(tenant);
			userInfo.setEmail(email);
			try{
				userSession = identityManagementDao.authenticate(userInfo,true);
			}catch (ItorixException itorixException){
				if(itorixException.getErrorCode().equalsIgnoreCase("Identity-1045")){
					//If User not mapped to workspace, prompt to get invited to workspace
					oauth2Status = 401;
					errorMessage = "You do not have the authorization to access this workspace. Please contact a workspace admin to invite your emailId";
				}else if(itorixException.getErrorCode().equalsIgnoreCase("Identity-1049")){
					oauth2Status = 400;
					errorMessage = itorixException.getMessage();
				} else{
					oauth2Status = 500;
					errorMessage = itorixException.getMessage();
				}
			}catch (Exception ex){
				oauth2Status = 500;
				errorMessage = ErrorCodes.SOCIAL_LOGIN_PROVISION_ERROR.message();
			}
		}else{
			oauth2Status = 500;
			errorMessage = ErrorCodes.SOCIAL_LOGIN_PROFILE_EMAIL_PRIVATE.message();
		}

		validationResult.put("oauth2Status",String.valueOf(oauth2Status));
		validationResult.put("oauth2ErrorMessage",errorMessage);
		if(userSession.getId() != null && !userSession.getId().isEmpty()){
			validationResult.put("x-token-v2",userSession.getId());
			String planId = userSession.getPlanId();
			if(planId != null && planId.equalsIgnoreCase("starter")){
				validationResult.put("redirect","astrum");
			}else{
				validationResult.put("redirect",tenant);
			}
		}
		validationResult.put("tenant",tenant);

		return validationResult;
	}
}
