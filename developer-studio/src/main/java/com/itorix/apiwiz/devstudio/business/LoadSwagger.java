package com.itorix.apiwiz.devstudio.business;


import com.fasterxml.jackson.core.JsonProcessingException;

import net.sf.json.JSONException;

public interface LoadSwagger {
	

	public String loadProxySwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException;
	
	public String loadTargetSwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException;


}
