package com.itorix.apiwiz.cicd.dashboard.beans;

public class CicdDashBoardResponse {

	  private Projects[] projects;

	  public Projects[] getProjects ()
	    {
	        return projects;
	    }

	    public void setProjects (Projects[] projects)
	    {
	        this.projects = projects;
	    }

	    @Override
	    public String toString()
	    {
	        return "ClassPojo [projects = "+projects+"]";
	    }
	    
}
