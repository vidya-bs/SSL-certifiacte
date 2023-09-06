package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

public enum MongoDbAuthMechanism {

    DEFAULT("DEFAULT"),SCRAM_SHA_1("SCRAM-SHA-1"),SCRAM_SHA_256("SCRAM-SHA-256");

    private String value;
    private MongoDbAuthMechanism(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static String getConfigType(MongoDbAuthMechanism mongoConfigType) {
        String type = mongoConfigType.value;
        return type;
    }

}