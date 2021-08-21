package com.itorix.apiwiz.identitymanagement.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.itorix.apiwiz.identitymanagement.model.Roles;
import com.itorix.apiwiz.identitymanagement.model.UserDefinedRoles;

@Component
public class IdentityManagementHelper {

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
}
