package com.itorix.apiwiz.common.model.databaseconfigs.mysql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.AbstractObject;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Document(collection = "Connectors.MySql.Configuration")
public class MySQLConfiguration extends AbstractObject {

    private String name;
    private String description;
    private MySqlConfigType mySqlConfigType;
    private String mysqlHostname;
    private String mysqlPort;
    private String mysqlUserName;
    private String mysqlPassword;
    private String mysqlDatabaseName;
    private MySqlSSL ssl;

    private MySqlSSH ssh;

    private String relam;
    private String kdcServerhost;
    private String url;

    public String getRelam() {
        return relam;
    }

    public void setRelam(String relam) {
        this.relam = relam;
    }

    public String getKdcServerhost() {
        return kdcServerhost;
    }

    public void setKdcServerhost(String kdcServerhost) {
        this.kdcServerhost = kdcServerhost;
    }

    public MySQLConfiguration() {
    }

    public MySQLConfiguration(String name, String description, MySqlConfigType mySqlConfigType, String mysqlHostname, String mysqlPort, String mysqlUserName, String mysqlPassword, String mysqlDatabaseName, MySqlSSL ssl, MySqlSSH ssh, String relam, String kdcServer, String url) {
        this.name = name;
        this.description = description;
        this.mySqlConfigType = mySqlConfigType;
        this.mysqlHostname = mysqlHostname;
        this.mysqlPort = mysqlPort;
        this.mysqlUserName = mysqlUserName;
        this.mysqlPassword = mysqlPassword;
        this.mysqlDatabaseName = mysqlDatabaseName;
        this.ssl = ssl;
        this.ssh = ssh;
        this.relam = relam;
        this.kdcServerhost = kdcServer;
        this.url = url;
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

    public MySqlConfigType getMySqlConfigType() {
        return mySqlConfigType;
    }

    public void setMySqlConfigType(MySqlConfigType mySqlConfigType) {
        this.mySqlConfigType = mySqlConfigType;
    }

    public String getMysqlHostname() {
        return mysqlHostname;
    }

    public void setMysqlHostname(String mysqlHostname) {
        this.mysqlHostname = mysqlHostname;
    }

    public String getMysqlPort() {
        return mysqlPort;
    }

    public void setMysqlPort(String mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

    public String getMysqlUserName() {
        return mysqlUserName;
    }

    public void setMysqlUserName(String mysqlUserName) {
        this.mysqlUserName = mysqlUserName;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getMysqlDatabaseName() {
        return mysqlDatabaseName;
    }

    public void setMysqlDatabaseName(String mysqlDatabaseName) {
        this.mysqlDatabaseName = mysqlDatabaseName;
    }

    public MySqlSSL getSsl() {
        return ssl;
    }

    public void setSsl(MySqlSSL ssl) {
        this.ssl = ssl;
    }

    public MySqlSSH getSsh() {
        return ssh;
    }

    public void setSsh(MySqlSSH ssh) {
        this.ssh = ssh;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
