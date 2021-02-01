package com.itorix.apiwiz.common.model.projectmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public enum ArtifactType {
	SOAPUI("SOAPUI"),
	Postman("Postman"),
	Testsuite("Testsuite");
	
	private String value;

    public String getResponse() {
        return value;
    }

    ArtifactType(String value){
        this.value = value;
    }
	
}
