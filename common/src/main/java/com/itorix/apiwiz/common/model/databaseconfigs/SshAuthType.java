package com.itorix.apiwiz.common.model.databaseconfigs;

public enum SshAuthType {
    PASSWORD("PASSWORD"), IDENTITYFILE("IDENTITYFILE");

    private String value;

    SshAuthType(String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
