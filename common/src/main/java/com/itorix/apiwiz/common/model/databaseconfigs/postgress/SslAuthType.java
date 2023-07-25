package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

public enum SslAuthType {
    disable("disable"), allow("allow"), prefer("prefer"), require("require"), verify_ca("verify-ca"), verify_full("verify-full");;
    private String value;

    SslAuthType(String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
