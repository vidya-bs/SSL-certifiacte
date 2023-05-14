package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.itorix.apiwiz.design.studio.model.Status;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;

public class StatusHistory {


    private Status status;
    private String message;
    private Long cts;
    private String lastModifiedBy;

    public StatusHistory() {}
    public StatusHistory(Status status, String message) {
        this.status = status;
        this.message = message;
        this.cts = System.currentTimeMillis();
        this.lastModifiedBy = ServiceRequestContextHolder.getContext().getUserSessionToken().getUsername();
    }
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public Long getCts() {
        return cts;
    }

    public void setCts(Long cts) {
        this.cts = cts;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
