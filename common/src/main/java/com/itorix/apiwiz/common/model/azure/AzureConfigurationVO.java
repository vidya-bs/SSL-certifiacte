package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Azure.Configuration")
public class AzureConfigurationVO {
    private String connectorName;
    private String managementHost;
    private String gatewayHost;
    private String subscriptionId;
    private String resourceGroup;
    private String serviceName;
    private String sharedAccessToken;
    @JsonIgnore
    private String apiVersion = "2021-08-01";


    public AzureConfigurationVO() {
    }

    public AzureConfigurationVO(String connectorName, String managementHost, String gatewayHost, String subscriptionId, String resourceGroup, String serviceName, String sharedAccessToken, String apiVersion) {
        this.connectorName = connectorName;
        this.managementHost = managementHost;
        this.gatewayHost = gatewayHost;
        this.subscriptionId = subscriptionId;
        this.resourceGroup = resourceGroup;
        this.serviceName = serviceName;
        this.sharedAccessToken = sharedAccessToken;
        this.apiVersion = apiVersion;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getManagementHost() {
        return managementHost;
    }

    public void setManagementHost(String managementHost) {
        this.managementHost = managementHost;
    }

    public String getGatewayHost() {
        return gatewayHost;
    }

    public void setGatewayHost(String gatewayHost) {
        this.gatewayHost = gatewayHost;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSharedAccessToken() {
        return sharedAccessToken;
    }

    public void setSharedAccessToken(String sharedAccessToken) {
        this.sharedAccessToken = sharedAccessToken;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }


}