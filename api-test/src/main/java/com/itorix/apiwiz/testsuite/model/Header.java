
package com.itorix.apiwiz.testsuite.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {

    public Header(String name, String value , String description) {
		super();
		this.name = name;
		this.description = description;
		this.value = value;
	}

	public Header() {
		super();
		this.name = null;
		this.description = null;
		this.value = null;
	}

	@JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("value")
    private String value;

    @JsonProperty("isEncryption")
    private boolean encryption;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

	public boolean isEncryption() {
		return encryption;
	}

	public void setEncryption(boolean encryption) {
		this.encryption = encryption;
	}
}