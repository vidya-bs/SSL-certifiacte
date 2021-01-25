package com.itorix.apiwiz.testsuite.business.gocd.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "fetch_materials",
    "clean_working_directory",
    "never_cleanup_artifacts",
    "approval",
    "environment_variables",
    "jobs"
})
public class Stage {

    @JsonProperty("name")
    private String name;
    @JsonProperty("fetch_materials")
    private Boolean fetchMaterials;
    @JsonProperty("clean_working_directory")
    private Boolean cleanWorkingDirectory;
    @JsonProperty("never_cleanup_artifacts")
    private Boolean neverCleanupArtifacts;
    @JsonProperty("approval")
    private Approval approval;
    @JsonProperty("environment_variables")
    private List<Object> environmentVariables = null;
    @JsonProperty("jobs")
    private List<Job> jobs = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
    public Stage() {}
    
    public Stage(String name, boolean fetchMaterials, boolean cleanWorkingDirectory, boolean neverCleanupArtifacts, String approvalRequired) {
    		this.name = name;
    		this.fetchMaterials = fetchMaterials;
    		this.cleanWorkingDirectory = cleanWorkingDirectory;
    		this.neverCleanupArtifacts = neverCleanupArtifacts;
    		approval = new Approval();
    		approval.setType(approvalRequired);
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("fetch_materials")
    public Boolean getFetchMaterials() {
        return fetchMaterials;
    }

    @JsonProperty("fetch_materials")
    public void setFetchMaterials(Boolean fetchMaterials) {
        this.fetchMaterials = fetchMaterials;
    }

    @JsonProperty("clean_working_directory")
    public Boolean getCleanWorkingDirectory() {
        return cleanWorkingDirectory;
    }

    @JsonProperty("clean_working_directory")
    public void setCleanWorkingDirectory(Boolean cleanWorkingDirectory) {
        this.cleanWorkingDirectory = cleanWorkingDirectory;
    }

    @JsonProperty("never_cleanup_artifacts")
    public Boolean getNeverCleanupArtifacts() {
        return neverCleanupArtifacts;
    }

    @JsonProperty("never_cleanup_artifacts")
    public void setNeverCleanupArtifacts(Boolean neverCleanupArtifacts) {
        this.neverCleanupArtifacts = neverCleanupArtifacts;
    }

    @JsonProperty("approval")
    public Approval getApproval() {
        return approval;
    }

    @JsonProperty("approval")
    public void setApproval(Approval approval) {
        this.approval = approval;
    }

    @JsonProperty("environment_variables")
    public List<Object> getEnvironmentVariables() {
        return environmentVariables;
    }

    @JsonProperty("environment_variables")
    public void setEnvironmentVariables(List<Object> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @JsonProperty("jobs")
    public List<Job> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
