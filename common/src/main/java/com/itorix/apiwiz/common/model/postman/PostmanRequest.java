package com.itorix.apiwiz.common.model.postman;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanRequest {
	public String id;
	public String headers; // String of \n separated headers with : separate
							// name:value.
	public String url;
	public String preRequestScript;
	public String method;
	public Object data; // Either String of escaped-JSON or [] empty array (for
						// GET)
	public Object rawModeData;
	public String dataMode;
	public String name;
	public String description;
	public String descriptionFormat;
	public Long time;
	public Integer version;
	public Object responses;
	public String tests;
	public Boolean synced;

	public String getData(PostmanVariables var) {
		Object dataToUse = null;
		if(dataMode!=null)
		dataToUse = dataMode.equals("raw") ? rawModeData : data;

		if (dataToUse instanceof String) {
			String result = (String) dataToUse;
			result = var.replace(result);
			return result;
		} else if (dataToUse instanceof ArrayList
				&& dataMode.equals("urlencoded")) {
			ArrayList<Map<String, String>> formData = (ArrayList<Map<String, String>>) dataToUse;
			return urlFormEncodeData(var, formData);
		} else { // empty array
			return "";
		}
	}

	public String urlFormEncodeData(PostmanVariables var,
			ArrayList<Map<String, String>> formData) {
		String result = "";
		int i = 0;
		for (Map<String, String> m : formData) {
			result += m.get("key") + "="
					+ URLEncoder.encode(var.replace(m.get("value")));
			if (i < formData.size() - 1) {
				result += "&";
			}
		}
		return result;
	}

	public String getUrl(PostmanVariables var) {
		return var.replace(this.url);
	}

	public HttpHeaders getHeaders(PostmanVariables var) {
		HttpHeaders result = new HttpHeaders();
		if (this.headers == null || this.headers.isEmpty()) {
			return result;
		}
		String h = var.replace(headers);
		String[] splitHeads = h.split("\n");
		for (String hp : splitHeads) {
			String[] pair = hp.split(":");
			String key = pair[0].trim();
			String val = pair[1].trim();
			result.set(key, val);
		}
		return result;
	}
}
