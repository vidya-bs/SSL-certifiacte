package com.itorix.apiwiz.common.model.databaseconfigs.postgress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PostgreSQLSSL {
  private PostgresSslAuthType sslMode;
  private String sslClientcert;
  private String sslClientcertkey;
  private String sslRootcert;
  private String sslClientcertkeyPassWord;
  private String certRevocationlist;

  public PostgreSQLSSL() {
  }

  public PostgreSQLSSL(PostgresSslAuthType sslMode, String sslClientcert, String sslClientcertkey, String sslRootcert, String sslClientcertkeyPassWord, String certRevocationlist) {
    this.sslMode = sslMode;
    this.sslClientcert = sslClientcert;
    this.sslClientcertkey = sslClientcertkey;
    this.sslRootcert = sslRootcert;
    this.sslClientcertkeyPassWord = sslClientcertkeyPassWord;
    this.certRevocationlist = certRevocationlist;
  }

  public PostgresSslAuthType getSslMode() {
    return sslMode;
  }

  public void setSslMode(PostgresSslAuthType sslMode) {
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

  public String getSslClientcertkeyPassWord() {
    return sslClientcertkeyPassWord;
  }

  public void setSslClientcertkeyPassWord(String sslClientcertkeyPassWord) {
    this.sslClientcertkeyPassWord = sslClientcertkeyPassWord;
  }
}
