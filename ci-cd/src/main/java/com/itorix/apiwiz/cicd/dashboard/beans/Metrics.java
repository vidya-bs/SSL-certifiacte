package com.itorix.apiwiz.cicd.dashboard.beans;
public class Metrics
{
    private String buildNumber;

    private Stages[] stages;

    public String getBuildNumber ()
    {
        return buildNumber;
    }

    public void setBuildNumber (String buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public Stages[] getStages ()
    {
        return stages;
    }

    public void setStages (Stages[] stages)
    {
        this.stages = stages;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [buildNumber = "+buildNumber+", stages = "+stages+"]";
    }
}
			
			