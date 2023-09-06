package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

public enum MysqlSslAuthType {
    PREFERRED("PREFERRED"), REQUIRED("REQUIRED"), VERIFY_CA("VERIFY_CA"), VERIFY_IDENTITY("VERIFY_IDENTITY"), DISABLED("DISABLED");
    private String value;

    MysqlSslAuthType(String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
