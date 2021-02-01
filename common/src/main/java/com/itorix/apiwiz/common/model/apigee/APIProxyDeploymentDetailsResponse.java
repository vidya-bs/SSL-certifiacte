package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIProxyDeploymentDetailsResponse
{
    private String organization;

    private Environment[] environment;

    private String name;

    public String getOrganization ()
    {
        return organization;
    }

    public void setOrganization (String organization)
    {
        this.organization = organization;
    }

    public Environment[] getEnvironment ()
    {
        return environment;
    }

    public void setEnvironment (Environment[] environment)
    {
        this.environment = environment;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [organization = "+organization+", environment = "+environment+", name = "+name+"]";
    }
}