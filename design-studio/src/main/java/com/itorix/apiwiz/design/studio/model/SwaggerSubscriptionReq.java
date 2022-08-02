package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SwaggerSubscriptionReq {
	
	@JsonProperty("oas")
	String oas;
	
	@JsonProperty("swaggerId")
	String swaggerId;
	
	@JsonProperty("swaggerName")
	String swaggerName;
	
	@JsonProperty("name")
	String name;
	
	@JsonProperty("emailId")
	String emailId;

	@JsonProperty("type")
	String type;

}
