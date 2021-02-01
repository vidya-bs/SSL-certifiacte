package com.itorix.apiwiz.configmanagement.model.apigee;

public class SSLInfo
{
    private String enabled;

    private String trustStore;

    private String keyStore;

    private String clientAuthEnabled;

    private boolean ignoreValidationErrors;

    private String keyAlias;

    public String getEnabled ()
    {
        return enabled;
    }

    public void setEnabled (String enabled)
    {
        this.enabled = enabled;
    }

    public String getTrustStore ()
    {
        return trustStore;
    }

    public void setTrustStore (String trustStore)
    {
        this.trustStore = trustStore;
    }

    public String getKeyStore ()
    {
        return keyStore;
    }

    public void setKeyStore (String keyStore)
    {
        this.keyStore = keyStore;
    }

    public String getClientAuthEnabled ()
    {
        return clientAuthEnabled;
    }

    public void setClientAuthEnabled (String clientAuthEnabled)
    {
        this.clientAuthEnabled = clientAuthEnabled;
    }

    public boolean ignoreValidationErrors ()
    {
        return ignoreValidationErrors;
    }

    public void setIgnoreValidationErrors (boolean ignoreValidationErrors)
    {
        this.ignoreValidationErrors = ignoreValidationErrors;
    }

    public String getKeyAlias ()
    {
        return keyAlias;
    }

    public void setKeyAlias (String keyAlias)
    {
        this.keyAlias = keyAlias;
    }
}
