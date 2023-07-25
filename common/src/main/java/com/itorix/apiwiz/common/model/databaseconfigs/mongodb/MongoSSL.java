package com.itorix.apiwiz.common.model.databaseconfigs.mongodb;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MongoSSL {

    private String certificateAuthority;
    private String clientKey;
    private String clientCertificate;
    private String clientKeyPassword;
    private boolean tlsInsecure;
    private boolean tlsAllowInvalidHostnames;
    private boolean tlsAllowInvalidCertificates;

    public MongoSSL() {
    }

    public MongoSSL( String certificateAuthority, String clientKey, String clientCertificate, String clientKeyPassword, boolean tlsInsecure, boolean tlsAllowInvalidHostnames, boolean tlsAllowInvalidCertificates) {
        this.certificateAuthority = certificateAuthority;
        this.clientKey = clientKey;
        this.clientCertificate = clientCertificate;
        this.clientKeyPassword = clientKeyPassword;
        this.tlsInsecure = tlsInsecure;
        this.tlsAllowInvalidHostnames = tlsAllowInvalidHostnames;
        this.tlsAllowInvalidCertificates = tlsAllowInvalidCertificates;
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

    public boolean isTlsInsecure() {
        return tlsInsecure;
    }

    public void setTlsInsecure(boolean tlsInsecure) {
        this.tlsInsecure = tlsInsecure;
    }

    public boolean isTlsAllowInvalidHostnames() {
        return tlsAllowInvalidHostnames;
    }

    public void setTlsAllowInvalidHostnames(boolean tlsAllowInvalidHostnames) {
        this.tlsAllowInvalidHostnames = tlsAllowInvalidHostnames;
    }

    public boolean isTlsAllowInvalidCertificates() {
        return tlsAllowInvalidCertificates;
    }

    public void setTlsAllowInvalidCertificates(boolean tlsAllowInvalidCertificates) {
        this.tlsAllowInvalidCertificates = tlsAllowInvalidCertificates;
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
}
