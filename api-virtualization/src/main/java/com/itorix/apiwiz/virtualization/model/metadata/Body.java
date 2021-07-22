package com.itorix.apiwiz.virtualization.model.metadata;

public class Body {
	private String not;

	private String pattern;

	private String xmlSchema;

	private String json;

	private String subString;

	private String string;

	private String matchType;

	private String jsonSchema;

	private String xml;

	private Parameters[] parameters;

	private String type;

	private String xpath;

	public String getNot() {
		return not;
	}

	public void setNot(String not) {
		this.not = not;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getXmlSchema() {
		return xmlSchema;
	}

	public void setXmlSchema(String xmlSchema) {
		this.xmlSchema = xmlSchema;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getSubString() {
		return subString;
	}

	public void setSubString(String subString) {
		this.subString = subString;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

	public String getJsonSchema() {
		return jsonSchema;
	}

	public void setJsonSchema(String jsonSchema) {
		this.jsonSchema = jsonSchema;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public Parameters[] getParameters() {
		return parameters;
	}

	public void setParameters(Parameters[] parameters) {
		this.parameters = parameters;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	@Override
	public String toString() {
		return "ClassPojo [not = " + not + ", pattern = " + pattern + ", xmlSchema = " + xmlSchema + ", json = " + json
				+ ", subString = " + subString + ", string = " + string + ", matchType = " + matchType
				+ ", jsonSchema = " + jsonSchema + ", xml = " + xml + ", parameters = " + parameters + ", type = "
				+ type + ", xpath = " + xpath + "]";
	}
}
