package com.itorix.apiwiz.design.studio.model;

public class XmlSchemaVo {
    
	private String include;
    private String xpath;
    private String minOccurs;
    private String maxOccurs;
    private String xsdType;
    private String jsonType;
    private String jsonFormat;
    private String enums;
    private String minLength;
    private String maxLength;
    private String length;
    private String pattern;
    private String documentation;
    
    public String getInclude() {
		return include;
	}
	public void setInclude(String include) {
		this.include = include;
	}
	public String getXpath() {
        return xpath;
    }
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
    public String getMinOccurs() {
        return minOccurs;
    }
    public void setMinOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }
    public String getMaxOccurs() {
        return maxOccurs;
    }
    public void setMaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
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

    public String toString(){
        
        xsdType = (xsdType != null) ?xsdType : "";
        jsonType = (jsonType != null) ?jsonType : "";
        jsonFormat = (jsonFormat != null) ?jsonFormat : "";
        enums = (enums != null) ?enums : "";
        minLength = (minLength != null) ?minLength : "";
        maxLength = (maxLength != null) ?maxLength : "";
        length = (length != null) ?length : "";
        pattern = (pattern != null) ?pattern : "";
        documentation = (documentation != null) ?documentation : "";
        
        return "|"+xpath+"|"+minOccurs+"|"+maxOccurs+"|"+xsdType+"|"+jsonType+"|"+jsonFormat+"|"+enums+"|"+minLength+"|"+maxLength+"|"+length+"|"+pattern+"|"+documentation;  
       } 
}
