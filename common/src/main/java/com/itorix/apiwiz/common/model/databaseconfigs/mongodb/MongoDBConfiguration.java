package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.AbstractObject;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Document(collection = "Connectors.MongoDb.Configuration")
public class MongoDBConfiguration extends AbstractObject {

    private String name;
    private String description;
    private String host;
    private String scheme;
    private String url;
    private MongoAuthentication authentication;
    private MongoSSH ssh;
    private MongoSSL ssl;

    public MongoDBConfiguration() {
    }

    public MongoDBConfiguration(String name, String description, String host, String scheme, String url, MongoAuthentication authentication, MongoSSH ssh, MongoSSL ssl) {
        this.name = name;
        this.description = description;
        this.host = host;
        this.scheme = scheme;
        this.url = url;
        this.authentication = authentication;
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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MongoAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(
            MongoAuthentication authentication) {
        this.authentication = authentication;
    }

    public MongoSSH getSsh() {
        return ssh;
    }

    public void setSsh(MongoSSH ssh) {
        this.ssh = ssh;
    }

    public MongoSSL getSsl() {
        return ssl;
    }

    public void setSsl(MongoSSL ssl) {
        this.ssl = ssl;
    }
}
