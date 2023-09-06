package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.databaseconfigs.SshAuthType;

@JsonInclude(Include.NON_NULL)
public class PostgreSQLSSH {
  private String sshUsername;
  private SshAuthType sshAuthenticationType;
  private String sshIdentityfile;
  private String sshPassword;

  private String sshPassphrase;
  private String sshTunnelhost;
  private String sshTunnelport;
  public PostgreSQLSSH() {
  }

  public PostgreSQLSSH(String sshUsername, SshAuthType sshAuthenticationType, String sshIdentityfile, String sshPassword, String sshPassphrase, String sshTunnelhost, String sshTunnelport) {
    this.sshUsername = sshUsername;
    this.sshAuthenticationType = sshAuthenticationType;
    this.sshIdentityfile = sshIdentityfile;
    this.sshPassword = sshPassword;
    this.sshPassphrase = sshPassphrase;
    this.sshTunnelhost = sshTunnelhost;
    this.sshTunnelport = sshTunnelport;
  }

  public String getSshPassphrase() {
    return sshPassphrase;
  }

  public void setSshPassphrase(String sshPassphrase) {
    this.sshPassphrase = sshPassphrase;
  }

  public String getSshUsername() {
    return sshUsername;
  }

  public void setSshUsername(String sshUsername) {
    this.sshUsername = sshUsername;
  }

  public SshAuthType getSshAuthenticationType() {
    return sshAuthenticationType;
  }

  public void setSshAuthenticationType(SshAuthType sshAuthenticationType) {
    this.sshAuthenticationType = sshAuthenticationType;
  }

  public String getSshIdentityfile() {
    return sshIdentityfile;
  }

  public void setSshIdentityfile(String sshIdentityfile) {
    this.sshIdentityfile = sshIdentityfile;
  }

  public String getSshPassword() {
    return sshPassword;
  }

  public void setSshPassword(String sshPassword) {
    this.sshPassword = sshPassword;
  }

  public String getSshTunnelhost() {
    return sshTunnelhost;
  }

  public void setSshTunnelhost(String sshTunnelhost) {
    this.sshTunnelhost = sshTunnelhost;
  }

  public String getSshTunnelport() {
    return sshTunnelport;
  }

  public void setSshTunnelport(String sshTunnelport) {
    this.sshTunnelport = sshTunnelport;
  }

}
