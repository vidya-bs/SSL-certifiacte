package com.itorix.apiwiz.cicd.gocd.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"url",
	"destination",
	"filter",
	"invert_filter",
	"name",
	"auto_update",
	"branch",
	"submodule_folder",
	"shallow_clone",
	"username",
	"password"
})
public class Attributes {

	@JsonProperty("url")
	private String url;
	@JsonProperty("destination")
	private Object destination;
	@JsonProperty("filter")
	private Object filter;
	@JsonProperty("invert_filter")
	private Boolean invertFilter;
	@JsonProperty("name")
	private String name;
	@JsonProperty("auto_update")
	private Boolean autoUpdate;
	@JsonProperty("branch")
	private String branch;
	@JsonProperty("submodule_folder")
	private Object submoduleFolder;
	@JsonProperty("shallow_clone")
	private Boolean shallowClone;
	@JsonProperty("username")
	private String username;
	@JsonProperty("password")
	private String password;


	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("username")
	public String getUsername() {
		return username;
	}
	@JsonProperty("username")
	public void setUsername(String username) {
		this.username = username;
	}
	@JsonProperty("password")
	public String getPassword() {
		return password;
	}
	@JsonProperty("password")
	public void setPassword(String password) {
		this.password = password;
	}



	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	@JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("destination")
	public Object getDestination() {
		return destination;
	}

	@JsonProperty("destination")
	public void setDestination(Object destination) {
		this.destination = destination;
	}

	@JsonProperty("filter")
	public Object getFilter() {
		return filter;
	}

	@JsonProperty("filter")
	public void setFilter(Object filter) {
		this.filter = filter;
	}

	@JsonProperty("invert_filter")
	public Boolean getInvertFilter() {
		return invertFilter;
	}

	@JsonProperty("invert_filter")
	public void setInvertFilter(Boolean invertFilter) {
		this.invertFilter = invertFilter;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("auto_update")
	public Boolean getAutoUpdate() {
		return autoUpdate;
	}

	@JsonProperty("auto_update")
	public void setAutoUpdate(Boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	@JsonProperty("branch")
	public String getBranch() {
		return branch;
	}

	@JsonProperty("branch")
	public void setBranch(String branch) {
		this.branch = branch;
	}

	@JsonProperty("submodule_folder")
	public Object getSubmoduleFolder() {
		return submoduleFolder;
	}

	@JsonProperty("submodule_folder")
	public void setSubmoduleFolder(Object submoduleFolder) {
		this.submoduleFolder = submoduleFolder;
	}

	@JsonProperty("shallow_clone")
	public Boolean getShallowClone() {
		return shallowClone;
	}

	@JsonProperty("shallow_clone")
	public void setShallowClone(Boolean shallowClone) {
		this.shallowClone = shallowClone;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
