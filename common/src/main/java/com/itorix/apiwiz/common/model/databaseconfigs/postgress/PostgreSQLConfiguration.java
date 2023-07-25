package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.AbstractObject;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Document(collection = "Connectors.PostgreSql.Configuration")
public class PostgreSQLConfiguration extends AbstractObject {

  private String name;
  private String description;
  private PostgresAuthType postgresAuthType;
  private String postgresqlHostname;
  private String postgresqlPort;
  private String postgresqlDatabase;
  private String postgresqlUsername;
  private String postgresqlPassword;
  private String postgresKerberosRelam;
  private String postgresKerberosKdcServer;
  private PostgreSQLSSH ssh;
  private PostgreSQLSSL ssl;

  public PostgreSQLConfiguration() {
  }

  public PostgreSQLConfiguration(String name, String description, PostgresAuthType postgresAuthType, String postgresqlHostname, String postgresqlPort, String postgresqlDatabase, String postgresqlUsername, String postgresqlPassword, String postgresKerberosRelam, String postgresKerberosKdcServer, PostgreSQLSSH ssh, PostgreSQLSSL ssl) {
    this.name = name;
    this.description = description;
    this.postgresAuthType = postgresAuthType;
    this.postgresqlHostname = postgresqlHostname;
    this.postgresqlPort = postgresqlPort;
    this.postgresqlDatabase = postgresqlDatabase;
    this.postgresqlUsername = postgresqlUsername;
    this.postgresqlPassword = postgresqlPassword;
    this.postgresKerberosRelam = postgresKerberosRelam;
    this.postgresKerberosKdcServer = postgresKerberosKdcServer;
    this.ssh = ssh;
    this.ssl = ssl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPostgresKerberosRelam() {
    return postgresKerberosRelam;
  }

  public void setPostgresKerberosRelam(String postgresKerberosRelam) {
    this.postgresKerberosRelam = postgresKerberosRelam;
  }

  public String getPostgresKerberosKdcServer() {
    return postgresKerberosKdcServer;
  }

  public void setPostgresKerberosKdcServer(String postgresKerberosKdcServer) {
    this.postgresKerberosKdcServer = postgresKerberosKdcServer;
  }

  public PostgresAuthType getPostgresAuthType() {
    return postgresAuthType;
  }

  public void setPostgresAuthType(PostgresAuthType postgresAuthType) {
    this.postgresAuthType = postgresAuthType;
  }

  public String getPostgresqlHostname() {
    return postgresqlHostname;
  }

  public void setPostgresqlHostname(String postgresqlHostname) {
    this.postgresqlHostname = postgresqlHostname;
  }

  public String getPostgresqlPort() {
    return postgresqlPort;
  }

  public void setPostgresqlPort(String postgresqlPort) {
    this.postgresqlPort = postgresqlPort;
  }

  public String getPostgresqlDatabase() {
    return postgresqlDatabase;
  }

  public void setPostgresqlDatabase(String postgresqlDatabase) {
    this.postgresqlDatabase = postgresqlDatabase;
  }

  public String getPostgresqlUsername() {
    return postgresqlUsername;
  }

  public void setPostgresqlUsername(String postgresqlUsername) {
    this.postgresqlUsername = postgresqlUsername;
  }

  public String getPostgresqlPassword() {
    return postgresqlPassword;
  }

  public void setPostgresqlPassword(String postgresqlPassword) {
    this.postgresqlPassword = postgresqlPassword;
  }

  public PostgreSQLSSH getSsh() {
    return ssh;
  }

  public void setSsh(PostgreSQLSSH ssh) {
    this.ssh = ssh;
  }

  public PostgreSQLSSL getSsl() {
    return ssl;
  }

  public void setSsl(PostgreSQLSSL ssl) {
    this.ssl = ssl;
  }
}
