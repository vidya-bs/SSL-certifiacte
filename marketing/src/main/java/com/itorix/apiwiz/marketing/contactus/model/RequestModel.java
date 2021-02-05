package com.itorix.apiwiz.marketing.contactus.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestModel {

	public enum Type {
		@JsonProperty("EMAIL")
		email, @JsonProperty("SLACK")
		slack;
	}

	Type type;
	EmailTemplate emailContent;
	Map<String, String> metadata = new HashMap<>();

}
