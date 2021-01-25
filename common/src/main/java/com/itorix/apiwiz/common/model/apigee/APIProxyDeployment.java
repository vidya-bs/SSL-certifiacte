package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIProxyDeployment
{
	private String Environment;

    private String name;

    private String Organization;

    private Revision[] Revision;

    public String getEnvironment ()
    {
        return Environment;
    }

    public void setEnvironment (String Environment)
    {
        this.Environment = Environment;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getOrganization ()
    {
        return Organization;
    }

    public void setOrganization (String Organization)
    {
        this.Organization = Organization;
    }

    public Revision[] getRevision ()
    {
        return Revision;
    }

    public void setRevision (Revision[] Revision)
    {
        this.Revision = Revision;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Environment = "+Environment+", name = "+name+", Organization = "+Organization+", Revision = "+Revision+"]";
    }
}
