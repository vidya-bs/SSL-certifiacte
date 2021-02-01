package com.itorix.apiwiz.common.model.projectmanagement;

public enum BranchType {
	feature("feature"),
	master("master"),
	release("release");
	
	private String value;

    public String getResponse() {
        return value;
    }

    BranchType(String value){
        this.value = value;
    }
	
}
