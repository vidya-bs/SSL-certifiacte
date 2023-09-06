package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

public enum MongoDbAuthType {

    none("none"),userNamePassword("userNamePassword"),x509("x509"),kerberos("kerberos"),ldap("ldap"),awsIAM("awsIAM");

    private String value;
    private MongoDbAuthType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static String getConfigType(MongoDbAuthType mongoConfigType) {
        String type = mongoConfigType.value;
        return type;
    }

}