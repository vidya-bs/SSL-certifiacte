package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MongoAuthentication {
  private MongoDbAuthType mongoDbAuthType;

  private String username;
  private String password;
  private String authenticationDatabase;
  private MongoDbAuthMechanism authenticationMechanism;
  private String kerberosUserPrincipal;
  private String kerberosUserPassword;
  private String kerberosServerUrl;
  private String kerberosServiceName;
  private String kerberosServiceRealm;
  private String ldapUsername;
  private String ldapPassword;
  private String awsAccesskey;
  private String awsSecretaccesskey;
  private String awsSessiontoken;

  public MongoAuthentication() {
  }

  public MongoAuthentication(MongoDbAuthType mongoDbAuthType, String username, String password, String authenticationDatabase, MongoDbAuthMechanism authenticationMechanism, String kerberosUserPrincipal, String kerberosUserPassword, String kerberosServerUrl, String kerberosServiceName, String kerberosServiceRealm, String ldapUsername, String ldapPassword, String awsAccesskey, String awsSecretaccesskey, String awsSessiontoken) {
    this.mongoDbAuthType = mongoDbAuthType;
    this.username = username;
    this.password = password;
    this.authenticationDatabase = authenticationDatabase;
    this.authenticationMechanism = authenticationMechanism;
    this.kerberosUserPrincipal = kerberosUserPrincipal;
    this.kerberosUserPassword = kerberosUserPassword;
    this.kerberosServerUrl = kerberosServerUrl;
    this.kerberosServiceName = kerberosServiceName;
    this.kerberosServiceRealm = kerberosServiceRealm;
    this.ldapUsername = ldapUsername;
    this.ldapPassword = ldapPassword;
    this.awsAccesskey = awsAccesskey;
    this.awsSecretaccesskey = awsSecretaccesskey;
    this.awsSessiontoken = awsSessiontoken;
  }

  public MongoDbAuthType getMongoDbAuthType() {
    return mongoDbAuthType;
  }

  public void setMongoDbAuthType(MongoDbAuthType mongoDbAuthType) {
    this.mongoDbAuthType = mongoDbAuthType;
  }

  public String getKerberosUserPrincipal() {
    return kerberosUserPrincipal;
  }

  public String getKerberosUserPassword() {
    return kerberosUserPassword;
  }

  public String getKerberosServerUrl() {
    return kerberosServerUrl;
  }

  public void setKerberosServerUrl(String kerberosServerUrl) {
    this.kerberosServerUrl = kerberosServerUrl;
  }
  public void setKerberosUserPassword(String kerberosUserPassword) {
    this.kerberosUserPassword = kerberosUserPassword;
  }
  public void setKerberosUserPrincipal(String kerberosUserPrincipal) {
    this.kerberosUserPrincipal = kerberosUserPrincipal;
  }

  public String getKerberosServiceName() {
    return kerberosServiceName;
  }

  public void setKerberosServiceName(String kerberosServiceName) {
    this.kerberosServiceName = kerberosServiceName;
  }

  public String getKerberosServiceRealm() {
    return kerberosServiceRealm;
  }

  public void setKerberosServiceRealm(String kerberosServiceRealm) {
    this.kerberosServiceRealm = kerberosServiceRealm;
  }

  public String getLdapUsername() {
    return ldapUsername;
  }

  public void setLdapUsername(String ldapUsername) {
    this.ldapUsername = ldapUsername;
  }

  public String getLdapPassword() {
    return ldapPassword;
  }

  public void setLdapPassword(String ldapPassword) {
    this.ldapPassword = ldapPassword;
  }

  public String getAwsAccesskey() {
    return awsAccesskey;
  }

  public void setAwsAccesskey(String awsAccesskey) {
    this.awsAccesskey = awsAccesskey;
  }

  public String getAwsSecretaccesskey() {
    return awsSecretaccesskey;
  }

  public void setAwsSecretaccesskey(String awsSecretaccesskey) {
    this.awsSecretaccesskey = awsSecretaccesskey;
  }

  public String getAwsSessiontoken() {
    return awsSessiontoken;
  }

  public void setAwsSessiontoken(String awsSessiontoken) {
    this.awsSessiontoken = awsSessiontoken;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

  public MongoDbAuthMechanism getAuthenticationMechanism() {
    return authenticationMechanism;
  }

  public void setAuthenticationMechanism(MongoDbAuthMechanism authenticationMechanism) {
    this.authenticationMechanism = authenticationMechanism;
  }
}
