package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MySqlSSL {

  private MysqlSslAuthType sslMode;
  private String sslKeyfile;
  private String sslCertfile;
  private String sslCafile;
  private String sslCipher;

  public MySqlSSL() {
  }

  public MySqlSSL(MysqlSslAuthType sslMode, String sslKeyfile, String sslCertfile, String sslCafile, String sslCipher) {
    this.sslMode = sslMode;
    this.sslKeyfile = sslKeyfile;
    this.sslCertfile = sslCertfile;
    this.sslCafile = sslCafile;
    this.sslCipher = sslCipher;
  }

  public MysqlSslAuthType getSslMode() {
    return sslMode;
  }

  public void setSslMode(MysqlSslAuthType sslMode) {
    this.sslMode = sslMode;
  }

  public String getSslKeyfile() {
    return sslKeyfile;
  }

  public void setSslKeyfile(String sslKeyfile) {
    this.sslKeyfile = sslKeyfile;
  }

  public String getSslCertfile() {
    return sslCertfile;
  }

  public void setSslCertfile(String sslCertfile) {
    this.sslCertfile = sslCertfile;
  }

  public String getSslCafile() {
    return sslCafile;
  }

  public void setSslCafile(String sslCafile) {
    this.sslCafile = sslCafile;
  }

  public String getSslCipher() {
    return sslCipher;
  }

  public void setSslCipher(String sslCipher) {
    this.sslCipher = sslCipher;
  }
}
