package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

public enum MySqlConfigType {
    TCPIP("tcpIp"),LDAP("ldap"),SSH("SSH"),LDAPSASLKERBEROS("ldapSaslKerberos"),NATIVEKERBEROS("nativeKerberos");

    private String value;
    MySqlConfigType(String value) {
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }
}