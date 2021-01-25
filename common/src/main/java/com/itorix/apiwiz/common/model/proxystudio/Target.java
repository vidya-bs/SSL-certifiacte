package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Target
{
    private String basePath;
    
    private String description;
    
	private String name;

    private Flows flows;
    private String oas;
    private String buildTargetArtifact;
    private String buildTargetArtifactType;

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getBasePath ()
    {
        return basePath;
    }

    public void setBasePath (String basePath)
    {
        this.basePath = basePath;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public Flows getFlows ()
    {
        return flows;
    }

    public void setFlows (Flows flows)
    {
        this.flows = flows;
    }

    @Override
    public String toString()
    {
        return "[basePath = "+basePath+", name = "+name+", flows = "+flows+"]";
    }

	public String getBuildTargetArtifact() {
		return buildTargetArtifact;
	}

	public void setBuildTargetArtifact(String buildTargetArtifact) {
		this.buildTargetArtifact = buildTargetArtifact;
	}

	public String getBuildTargetArtifactType() {
		return buildTargetArtifactType;
	}

	public void setBuildTargetArtifactType(String buildTargetArtifactType) {
		this.buildTargetArtifactType = buildTargetArtifactType;
	}

	public String getOas() {
		return oas;
	}

	public void setOas(String oas) {
		this.oas = oas;
	}
}