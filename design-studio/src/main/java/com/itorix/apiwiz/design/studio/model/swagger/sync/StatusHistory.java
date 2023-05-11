package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.itorix.apiwiz.design.studio.model.Status;

public class StatusHistory {


    private Status status;
    private String message;
    private Long mts;

    private String userName;

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
    public Long getMts() {
        return mts;
    }

    public void setMts(Long mts) {
        this.mts = mts;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
