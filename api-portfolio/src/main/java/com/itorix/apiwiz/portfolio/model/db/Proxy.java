package com.itorix.apiwiz.portfolio.model.db;

import java.util.List;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;

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
public class Proxy {

	@Id
	private String id;
	private String name;
	private String summary;
	private String proxyVersion;
	List<String> basePaths;
	private boolean deprecate;
	private String gwProvider;
	@JsonProperty("apigeeConfig")
	JSONObject apigeeConfig;
}
