package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

public enum MongoDbSshAuthType {
    PASSWORD("PASSWORD"), IDENTITYFILE("IDENTITYFILE"), SOCKS5("SOCKS5");

    private String value;

    MongoDbSshAuthType(String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
