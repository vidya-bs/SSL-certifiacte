package com.itorix.apiwiz.common.model.kong;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document("Connectors.Kong.Runtime.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KongRuntime implements Serializable {
    private String name;
    private String kongAdminToken;
    private String kongAdminHost;
    private String tier;
    private String type;
    private int workspaceCount;

    //    private List<KongRuntimeGroup>runtimeGroups;
    public KongRuntime(String name, String kongAdminToken, String kongAdminHost,String type) {
        this.name = name;
        this.kongAdminToken = kongAdminToken;
        this.kongAdminHost = kongAdminHost;
        this.type = type;
    }

    public KongRuntime() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKongAdminToken() {
        return kongAdminToken;
    }

    public void setKongAdminToken(String kongAdminToken) {
        this.kongAdminToken = kongAdminToken;
    }

    public String getKongAdminHost() {
        return kongAdminHost;
    }

    public void setKongAdminHost(String kongAdminHost) {
        this.kongAdminHost = kongAdminHost;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWorkspaceCount() {
        return workspaceCount;
    }

    public void setWorkspaceCount(int workspaceCount) {
        this.workspaceCount = workspaceCount;
    }
}
