package com.itorix.apiwiz.identitymanagement.model.social;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class MappedOAuth2User implements OAuth2User {

	private OAuth2User oauth2User;
	private String email;

	public MappedOAuth2User(OAuth2User oauth2User) {
		this.oauth2User = oauth2User;
	}

	@Override
	public Map<String, Object> getAttributes() {
		Map<String,Object> attributeMap = new HashMap<>(oauth2User.getAttributes());
		//For LinkedIn, we need principal name to not be null
		if(attributeMap.containsKey("localizedFirstName")){ //Identify if its LinkedIn provider using this key
			attributeMap.put("name",attributeMap.get("localizedFirstName") + " " + attributeMap.get("localizedLastName"));
			attributeMap.put("user_name",attributeMap.get("localizedFirstName") + " " + attributeMap.get("localizedLastName"));
		}
		return attributeMap;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return oauth2User.getAuthorities();
	}

	@Override
	public String getName() {
		String name = oauth2User.getAttribute("name");
		if(name == null || name.isEmpty()){
			return (String) this.getAttributes().get("name");
		}
		return name;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
