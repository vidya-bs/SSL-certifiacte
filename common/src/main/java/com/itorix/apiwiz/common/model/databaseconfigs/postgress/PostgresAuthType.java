package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

public enum PostgresAuthType {

    USERNAMEPASSWORD("usernamePassword"),RADIUS("Radius"),LDAP("ldap"),GSSAPI("GSSAPI"),PAM("pam");

    private String value;
    PostgresAuthType(String value) {
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }

    public void setValue(String value){
        this.value = value;
    }
}