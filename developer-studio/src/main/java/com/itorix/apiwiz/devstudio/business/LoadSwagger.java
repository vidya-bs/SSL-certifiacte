package com.itorix.apiwiz.devstudio.business;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import net.sf.json.JSONException;

@Component
public interface LoadSwagger {

	public String loadProxySwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException;

	public String loadTargetSwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException, org.json.JSONException, ItorixException;
}
