package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MongoSSH {
  private MongoDbSshAuthType sshAuthType;
  private String sshIdentityFile;
  private String sshPassphrase;
  private String sshHostname;
  private String sshPort;
  private String sshUsername;
  private String sshPassword;
  private String proxyHostname;
  private String proxyTunnelPort;
  private String proxyUserName;
  private String proxypassword;

  public MongoSSH() {
  }

  public MongoSSH(MongoDbSshAuthType sshAuthType, String sshIdentityFile, String sshPassphrase, String sshHostname, String sshPort, String sshUsername, String sshPassword, String proxyHostname, String proxyTunnelPort, String proxyUserName, String proxypassword) {
    this.sshAuthType = sshAuthType;
    this.sshIdentityFile = sshIdentityFile;
    this.sshPassphrase = sshPassphrase;
    this.sshHostname = sshHostname;
    this.sshPort = sshPort;
    this.sshUsername = sshUsername;
    this.sshPassword = sshPassword;
    this.proxyHostname = proxyHostname;
    this.proxyTunnelPort = proxyTunnelPort;
    this.proxyUserName = proxyUserName;
    this.proxypassword = proxypassword;
  }


  public String getProxyHostname() {
    return proxyHostname;
  }

  public void setProxyHostname(String proxyHostname) {
    this.proxyHostname = proxyHostname;
  }

  public String getProxyTunnelPort() {
    return proxyTunnelPort;
  }

  public void setProxyTunnelPort(String proxyTunnelPort) {
    this.proxyTunnelPort = proxyTunnelPort;
  }

  public String getProxyUserName() {
    return proxyUserName;
  }

  public void setProxyUserName(String proxyUserName) {
    this.proxyUserName = proxyUserName;
  }

  public String getProxypassword() {
    return proxypassword;
  }

  public void setProxypassword(String proxypassword) {
    this.proxypassword = proxypassword;
  }

  public void setSshAuthType(MongoDbSshAuthType sshAuthType) {
    this.sshAuthType = sshAuthType;
  }

  public MongoDbSshAuthType getSshAuthType() {
    return sshAuthType;
  }

  public String getSshIdentityFile() {
    return sshIdentityFile;
  }

  public void setSshIdentityFile(String sshIdentityFile) {
    this.sshIdentityFile = sshIdentityFile;
  }

  public String getSshPassphrase() {
    return sshPassphrase;
  }

  public void setSshPassphrase(String sshPassphrase) {
    this.sshPassphrase = sshPassphrase;
  }

  public String getSshHostname() {
    return sshHostname;
  }

  public void setSshHostname(String sshHostname) {
    this.sshHostname = sshHostname;
  }

  public String getSshPort() {
    return sshPort;
  }

  public void setSshPort(String sshPort) {
    this.sshPort = sshPort;
  }

  public String getSshUsername() {
    return sshUsername;
  }

  public void setSshUsername(String sshUsername) {
    this.sshUsername = sshUsername;
  }

  public String getSshPassword() {
    return sshPassword;
  }

  public void setSshPassword(String sshPassword) {
    this.sshPassword = sshPassword;
  }
}
