package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Configuration
{
    private String basePath;

    private String[] steps;

    public String getBasePath ()
    {
        return basePath;
    }

    public void setBasePath (String basePath)
    {
        this.basePath = basePath;
    }

    public String[] getSteps ()
    {
        return steps;
    }

    public void setSteps (String[] steps)
    {
        this.steps = steps;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [basePath = "+basePath+", steps = "+steps+"]";
    }
}