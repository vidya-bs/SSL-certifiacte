package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MongoSSL {
    boolean sslConnection;
    private String certificateAuthority;
    private String clientKey;
    private String clientCertificate;
    private String clientKeyPassword;
    private boolean tlsAllowInvalidHostnames;
    public MongoSSL() {
    }

    public MongoSSL(String certificateAuthority, String clientKey, String clientCertificate, String clientKeyPassword, boolean tlsAllowInvalidHostnames, boolean sslConnection) {
        this.certificateAuthority = certificateAuthority;
        this.clientKey = clientKey;
        this.clientCertificate = clientCertificate;
        this.clientKeyPassword = clientKeyPassword;
        this.tlsAllowInvalidHostnames = tlsAllowInvalidHostnames;
        this.sslConnection = sslConnection;
    }

    public String getCertificateAuthority() {
        return certificateAuthority;
    }

    public void setCertificateAuthority(String certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    public String getClientKeyPassword() {
        return clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        this.clientKeyPassword = clientKeyPassword;
    }

    public boolean isTlsAllowInvalidHostnames() {
        return tlsAllowInvalidHostnames;
    }

    public void setTlsAllowInvalidHostnames(boolean tlsAllowInvalidHostnames) {
        this.tlsAllowInvalidHostnames = tlsAllowInvalidHostnames;
    }
    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public boolean isSslConnection() {
        return sslConnection;
    }

    public void setSslConnection(boolean sslConnection) {
        this.sslConnection = sslConnection;
    }
}
