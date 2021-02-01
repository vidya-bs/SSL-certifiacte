package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiProducts
{
    private String status;

    private String apiproduct;

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getApiproduct ()
    {
        return apiproduct;
    }

    public void setApiproduct (String apiproduct)
    {
        this.apiproduct = apiproduct;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [status = "+status+", apiproduct = "+apiproduct+"]";
    }
}