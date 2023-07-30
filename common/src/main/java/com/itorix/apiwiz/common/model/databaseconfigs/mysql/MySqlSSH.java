package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.databaseconfigs.SshAuthType;

@JsonInclude(Include.NON_NULL)
public class MySqlSSH {

  private SshAuthType sshAuthType;
  private String sshHostname;
  private String sshPassword;
  private String sshPassPhrase;
  private String sshKeyfile;
  private String sshUsername;
  private String sshPort;
  public MySqlSSH() {
  }

  public MySqlSSH(SshAuthType sshAuthType, String sshHostname, String sshPassword, String sshPassPhrase, String sshKeyfile, String sshUsername, String sshPort) {
    this.sshAuthType = sshAuthType;
    this.sshHostname = sshHostname;
    this.sshPassword = sshPassword;
    this.sshPassPhrase = sshPassPhrase;
    this.sshKeyfile = sshKeyfile;
    this.sshUsername = sshUsername;
    this.sshPort = sshPort;
  }

  public String getSshPassPhrase() {
    return sshPassPhrase;
  }

  public void setSshPassPhrase(String sshPassPhrase) {
    this.sshPassPhrase = sshPassPhrase;
  }

  public SshAuthType getSshAuthType() {
    return sshAuthType;
  }

  public void setSshAuthType(SshAuthType sshAuthType) {
    this.sshAuthType = sshAuthType;
  }

  public String getSshPort() {
    return sshPort;
  }

  public void setSshPort(String sshPort) {
    this.sshPort = sshPort;
  }

  public String getSshHostname() {
    return sshHostname;
  }

  public String getSshPassword() {
    return sshPassword;
  }

  public String getSshKeyfile() {
    return sshKeyfile;
  }

  public String getSshUsername() {
    return sshUsername;
  }

  public void setSshHostname(String sshHostname) {
    this.sshHostname = sshHostname;
  }

  public void setSshPassword(String sshPassword) {
    this.sshPassword = sshPassword;
  }

  public void setSshKeyfile(String sshKeyfile) {
    this.sshKeyfile = sshKeyfile;
  }

  public void setSshUsername(String sshUsername) {
    this.sshUsername = sshUsername;
  }

}
