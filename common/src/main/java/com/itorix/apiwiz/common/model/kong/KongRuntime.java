package com.itorix.apiwiz.common.model.kong;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document("Connectors.Kong.Runtime.List")
@Data
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
}
