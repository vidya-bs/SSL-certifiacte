package com.itorix.apiwiz.common.model.apigee.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metrics {

	
	 private Values[] values;

	    private String name;

	    public Values[] getValues ()
	    {
	        return values;
	    }

	    public void setValues (Values[] values)
	    {
	        this.values = values;
	    }

	    public String getName ()
	    {
	        return name;
	    }

	    public void setName (String name)
	    {
	        this.name = name;
	    }

}
