package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PostgreSQLSSL {
  private String sslMode;
  private String sslClientcert;
  private String sslClientcertkey;
  private String sslRootcert;
  private String certRevocationlist;

  public PostgreSQLSSL() {
  }

  public PostgreSQLSSL(String ssl_mode, String ssl_clientcert, String ssl_clientcertkey,
                       String ssl_rootcert, String cert_revocationlist) {
    this.sslMode = ssl_mode;
    this.sslClientcert = ssl_clientcert;
    this.sslClientcertkey = ssl_clientcertkey;
    this.sslRootcert = ssl_rootcert;
    this.certRevocationlist = cert_revocationlist;
  }

  public String getSslMode() {
    return sslMode;
  }

  public void setSslMode(String sslMode) {
    this.sslMode = sslMode;
  }

  public String getSslClientcert() {
    return sslClientcert;
  }

  public void setSslClientcert(String sslClientcert) {
    this.sslClientcert = sslClientcert;
  }

  public String getSslClientcertkey() {
    return sslClientcertkey;
  }

  public void setSslClientcertkey(String sslClientcertkey) {
    this.sslClientcertkey = sslClientcertkey;
  }

  public String getSslRootcert() {
    return sslRootcert;
  }

  public void setSslRootcert(String sslRootcert) {
    this.sslRootcert = sslRootcert;
  }

  public String getCertRevocationlist() {
    return certRevocationlist;
  }

  public void setCertRevocationlist(String certRevocationlist) {
    this.certRevocationlist = certRevocationlist;
  }
}
