package com.itorix.apiwiz.design.studio.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RowData {
	private String xpath;
	@JsonProperty("minOccurs")
	private String min;
	@JsonProperty("maxOccurs")
	private String max;
	private String xsdType;
	private String jsonType;
	private String jsonFormat;
	private String enums;
	private String minLength;
    private String maxLength;
    private String length;
    private String pattern;
    private String documentation;
    
    private List<String>  enumcell;

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getXsdType() {
		return xsdType;
	}

	public void setXsdType(String xsdType) {
		this.xsdType = xsdType;
	}

	public String getJsonType() {
		return jsonType;
	}

	public void setJsonType(String jsonType) {
		this.jsonType = jsonType;
	}

	public String getJsonFormat() {
		return jsonFormat;
	}

	public void setJsonFormat(String jsonFormat) {
		this.jsonFormat = jsonFormat;
	}

	public String getEnums() {
		return enums;
	}

	public void setEnums(String enums) {
		this.enums = enums;
	}

	public String getMinLength() {
		return minLength;
	}

	public void setMinLength(String minLength) {
		this.minLength = minLength;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public List<String> getEnumcell() {
		return enumcell;
	}

	public void setEnumcell(List<String> enumcell) {
		this.enumcell = enumcell;
	}
	
	


}
