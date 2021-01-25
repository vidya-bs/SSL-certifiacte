package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyForADeveloperAppResponse
{
    private String[] scopes;

    private String consumerKey;

    private String expiresAt;

    private String status;

    private String issuedAt;

    private String[] attributes;

    private String consumerSecret;

    private ApiProducts[] apiProducts;

    public String[] getScopes ()
    {
        return scopes;
    }

    public void setScopes (String[] scopes)
    {
        this.scopes = scopes;
    }

    public String getConsumerKey ()
    {
        return consumerKey;
    }

    public void setConsumerKey (String consumerKey)
    {
        this.consumerKey = consumerKey;
    }

    public String getExpiresAt ()
    {
        return expiresAt;
    }

    public void setExpiresAt (String expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getIssuedAt ()
    {
        return issuedAt;
    }

    public void setIssuedAt (String issuedAt)
    {
        this.issuedAt = issuedAt;
    }

    public String[] getAttributes ()
    {
        return attributes;
    }

    public void setAttributes (String[] attributes)
    {
        this.attributes = attributes;
    }

    public String getConsumerSecret ()
    {
        return consumerSecret;
    }

    public void setConsumerSecret (String consumerSecret)
    {
        this.consumerSecret = consumerSecret;
    }

    public ApiProducts[] getApiProducts ()
    {
        return apiProducts;
    }

    public void setApiProducts (ApiProducts[] apiProducts)
    {
        this.apiProducts = apiProducts;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [scopes = "+scopes+", consumerKey = "+consumerKey+", expiresAt = "+expiresAt+", status = "+status+", issuedAt = "+issuedAt+", attributes = "+attributes+", consumerSecret = "+consumerSecret+", apiProducts = "+apiProducts+"]";
    }
}