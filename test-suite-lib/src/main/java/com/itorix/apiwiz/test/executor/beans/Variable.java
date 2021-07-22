package com.itorix.apiwiz.test.executor.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author vphani
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("value")
    private String value;

    @JsonProperty("isEncryption")
    private boolean encryption;

    @JsonProperty("runTimevalue")
    private String runTimevalue;

    private boolean ignoreCase;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Variable [name=");
        builder.append(name);
        builder.append(", reference=");
        builder.append(reference);
        builder.append(", value=");
        builder.append(value);
        builder.append(", ignoreCase=");
        builder.append(ignoreCase);
        builder.append("]");
        return builder.toString();
    }

    public boolean isEncryption() {
        return encryption;
    }

    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    public String getRunTimevalue() {
        return runTimevalue;
    }

    public void setRunTimevalue(String runTimevalue) {
        this.runTimevalue = runTimevalue;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}