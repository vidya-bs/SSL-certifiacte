package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

public enum SslAuthType {
    PREFERRED("PREFERRED"), REQUIRED("REQUIRED"), VERIFY_CA("VERIFY_CA"), VERIFY_IDENTITY("VERIFY_IDENTITY"), DISABLED("DISABLED");
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
